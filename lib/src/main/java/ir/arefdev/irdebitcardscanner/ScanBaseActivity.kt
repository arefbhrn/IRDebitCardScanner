package ir.arefdev.irdebitcardscanner

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.RectF
import android.hardware.Camera
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import com.arefbhrn.irdebitcardscanner.R
import java.io.File
import java.io.IOException
import java.util.concurrent.Semaphore

/**
 * Any classes that subclass this must:
 *
 * (1) set mIsPermissionCheckDone after the permission check is done, which should be sometime
 * before "onResume" is called
 *
 * (2) Call setViewIds to set these resource IDs and initalize appropriate handlers
 */
@Suppress("DEPRECATION")
abstract class ScanBaseActivity : Activity(), Camera.PreviewCallback,
    View.OnClickListener, OnScanListener, OnObjectListener, OnCameraOpenListener {

    companion object {
        const val IS_OCR = "is_ocr"
        const val RESULT_FATAL_ERROR = "result_fatal_error"
        const val RESULT_CAMERA_OPEN_ERROR = "result_camera_open_error"

        private var machineLearningThread: MachineLearningThread? = null

        @JvmStatic
        fun warmUp(context: Context) {
            getMachineLearningThread().warmUp(context)
        }

        @JvmStatic
        fun getMachineLearningThread(): MachineLearningThread {
            if (machineLearningThread == null) {
                machineLearningThread = MachineLearningThread()
                Thread(machineLearningThread).start()
            }
            return machineLearningThread!!
        }
    }

    private var mCamera: Camera? = null
    private lateinit var mOrientationEventListener: OrientationEventListener
    private val mMachineLearningSemaphore = Semaphore(1)
    private var mRotation: Int = 0
    private var mSentResponse = false
    private var mIsActivityActive = false
    private val numberResults = HashMap<String, Int>()
    private val expiryResults = HashMap<Expiry, Int>()
    private var firstResultMs: Long = 0
    private var mFlashlightId: Int = 0
    private var mCardNumberId: Int = 0
    private var mExpiryId: Int = 0
    private var mTextureId: Int = 0
    private var mRoiCenterYRatio: Float = 0f
    private var mCameraThread: CameraThread? = null
    private var mIsOcr = true

    var wasPermissionDenied = false
    var denyPermissionTitle: String = ""
    var denyPermissionMessage: String = ""
    var denyPermissionButton: String = ""

    // set when this activity posts to the machineLearningThread
    var mPredictionStartMs: Long = 0
    // Child classes must set to ensure proper flaslight handling
    var mIsPermissionCheckDone = false
    protected var mShowNumberAndExpiryAsScanning = true

    protected var objectDetectFile: File? = null

    var errorCorrectionDurationMs: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        denyPermissionTitle = getString(R.string.deny_permission_title)
        denyPermissionMessage = getString(R.string.deny_permission_message)
        denyPermissionButton = getString(R.string.deny_permission_button)

        mIsOcr = intent.getBooleanExtra(IS_OCR, true)

        mOrientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                orientationChanged(orientation)
            }
        }
    }

    inner class MyGlobalListenerClass(
        private val cardRectangleId: Int,
        private val overlayId: Int
    ) : ViewTreeObserver.OnGlobalLayoutListener {

        override fun onGlobalLayout() {
            val xy = IntArray(2)
            val view = findViewById<View>(cardRectangleId)
            view.getLocationInWindow(xy)

            // convert from DP to pixels
            val radius = (11 * Resources.getSystem().displayMetrics.density).toInt()
            val rect = RectF(
                xy[0].toFloat(), xy[1].toFloat(),
                (xy[0] + view.width).toFloat(),
                (xy[1] + view.height).toFloat()
            )
            val overlay = findViewById<Overlay>(overlayId)
            overlay.setCircle(rect, radius)

            mRoiCenterYRatio = (xy[1] + view.height * 0.5f) / overlay.height
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mIsPermissionCheckDone = true
        } else {
            wasPermissionDenied = true
            val builder = AlertDialog.Builder(this)
            builder.setMessage(denyPermissionMessage).setTitle(denyPermissionTitle)
            builder.setPositiveButton(denyPermissionButton) { _, _ ->
                // just let the user click on the back button manually
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onCameraOpen(camera: Camera?) {
        if (camera == null) {
            val intent = Intent()
            intent.putExtra(RESULT_CAMERA_OPEN_ERROR, true)
            setResult(RESULT_CANCELED, intent)
            finish()
        } else if (!mIsActivityActive) {
            camera.release()
        } else {
            mCamera = camera
            setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera!!)
            // Create our Preview view and set it as the content of our activity.
            val cameraPreview = CameraPreview(this, this)
            val preview = findViewById<FrameLayout>(mTextureId)
            preview.addView(cameraPreview)
            mCamera!!.setPreviewCallback(this)
        }
    }

    protected fun startCamera() {
        numberResults.clear()
        expiryResults.clear()
        firstResultMs = 0
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable()
        }

        try {
            if (mIsPermissionCheckDone) {
                if (mCameraThread == null) {
                    mCameraThread = CameraThread()
                    mCameraThread!!.start()
                }
                mCameraThread!!.startCamera(this)
            }
        } catch (e: Exception) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.busy_camera).setTitle(R.string.busy_camera_title)
            builder.setPositiveButton(R.string.deny_permission_button) { _, _ -> finish() }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onPause() {
        super.onPause()
        mCamera?.let {
            it.stopPreview()
            it.setPreviewCallback(null)
            it.release()
            mCamera = null
        }
        mOrientationEventListener.disable()
        mIsActivityActive = false
    }

    override fun onResume() {
        super.onResume()

        mIsActivityActive = true
        firstResultMs = 0
        numberResults.clear()
        expiryResults.clear()
        mSentResponse = false

        findViewById<View>(mCardNumberId)?.visibility = View.INVISIBLE
        findViewById<View>(mExpiryId)?.visibility = View.INVISIBLE

        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun setViewIds(flashlightId: Int, cardRectangleId: Int, overlayId: Int, textureId: Int, cardNumberId: Int, expiryId: Int) {
        mFlashlightId = flashlightId
        mTextureId = textureId
        mCardNumberId = cardNumberId
        mExpiryId = expiryId
        findViewById<View>(flashlightId)?.setOnClickListener(this)
        findViewById<View>(cardRectangleId).viewTreeObserver
            .addOnGlobalLayoutListener(MyGlobalListenerClass(cardRectangleId, overlayId))
    }

    fun orientationChanged(orientation: Int) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        val adjusted = (orientation + 45) / 90 * 90
        val rotation: Int = if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (info.orientation - adjusted + 360) % 360
        } else {
            (info.orientation + adjusted) % 360
        }

        mCamera?.let {
            try {
                val params = it.parameters
                params.setRotation(rotation)
                it.parameters = params
            } catch (e: Throwable) {
                // This gets called often so we can just swallow it and wait for the next one
                e.printStackTrace()
            }
        }
    }

    fun setCameraDisplayOrientation(activity: Activity, cameraId: Int, camera: Camera) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        val degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        val result: Int = if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            val r = (info.orientation + degrees) % 360
            (360 - r) % 360  // compensate the mirror
        } else {
            (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
        mRotation = result
    }

    override fun onPreviewFrame(bytes: ByteArray, camera: Camera) {
        if (mMachineLearningSemaphore.tryAcquire()) {
            val mlThread = getMachineLearningThread()

            val parameters = camera.parameters
            val width = parameters.previewSize.width
            val height = parameters.previewSize.height
            val format = parameters.previewFormat

            mPredictionStartMs = SystemClock.uptimeMillis()

            if (mIsOcr) {
                mlThread.post(bytes, width, height, format, mRotation, this, applicationContext, mRoiCenterYRatio)
            } else {
                mlThread.post(bytes, width, height, format, mRotation, this, applicationContext, mRoiCenterYRatio, objectDetectFile)
            }
        }
    }

    override fun onClick(view: View) {
        mCamera?.let {
            if (mFlashlightId == view.id) {
                val parameters = it.parameters
                if (parameters.flashMode == Camera.Parameters.FLASH_MODE_TORCH) {
                    parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
                } else {
                    parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                }
                it.parameters = parameters
                it.startPreview()
            }
        }
    }

    override fun onBackPressed() {
        if (!mSentResponse && mIsActivityActive) {
            mSentResponse = true
            val intent = Intent()
            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }

    @VisibleForTesting
    fun incrementNumber(number: String) {
        numberResults[number] = (numberResults[number] ?: 0) + 1
    }

    @VisibleForTesting
    fun incrementExpiry(expiry: Expiry) {
        expiryResults[expiry] = (expiryResults[expiry] ?: 0) + 1
    }

    @VisibleForTesting
    fun getNumberResult(): String? {
        var result: String? = null
        var maxValue = 0
        for ((number, count) in numberResults) {
            if (count > maxValue) {
                result = number
                maxValue = count
            }
        }
        return result
    }

    @VisibleForTesting
    fun getExpiryResult(): Expiry? {
        var result: Expiry? = null
        var maxValue = 0
        for ((expiry, count) in expiryResults) {
            if (count > maxValue) {
                result = expiry
                maxValue = count
            }
        }
        return result
    }

    private fun setValueAnimated(textView: TextView, value: String) {
        if (textView.visibility != View.VISIBLE) {
            textView.visibility = View.VISIBLE
            textView.alpha = 0f
        }
        textView.text = value
    }

    protected abstract fun onCardScanned(numberResult: String?, month: String?, year: String?)

    protected fun setNumberAndExpiryAnimated(duration: Long) {
        val numberResult = getNumberResult() ?: return
        val expiryResult = getExpiryResult()
        val textView = findViewById<TextView>(mCardNumberId)
        setValueAnimated(textView, DebitCardUtils.format(numberResult))

        if (expiryResult != null && duration >= (errorCorrectionDurationMs / 2)) {
            val expiryTextView = findViewById<TextView>(mExpiryId)
            setValueAnimated(expiryTextView, expiryResult.format())
        }
    }

    override fun onFatalError() {
        val intent = Intent()
        intent.putExtra(RESULT_FATAL_ERROR, true)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onPrediction(
        number: String?,
        expiry: Expiry?,
        bitmap: Bitmap,
        digitBoxes: List<DetectedBox>,
        expiryBox: DetectedBox?
    ) {
        if (!mSentResponse && mIsActivityActive) {
            if (number != null && firstResultMs == 0L) {
                firstResultMs = SystemClock.uptimeMillis()
            }

            if (number != null) incrementNumber(number)
            if (expiry != null) incrementExpiry(expiry)

            val duration = SystemClock.uptimeMillis() - firstResultMs
            if (firstResultMs != 0L && mShowNumberAndExpiryAsScanning) {
                setNumberAndExpiryAnimated(duration)
            }

            if (firstResultMs != 0L && duration >= errorCorrectionDurationMs) {
                mSentResponse = true
                val numberResult = getNumberResult()
                val expiryResult = getExpiryResult()
                val month = expiryResult?.getMonth()?.toString()
                val year = expiryResult?.getYear()?.toString()
                onCardScanned(numberResult, month, year)
            }
        }

        mMachineLearningSemaphore.release()
    }

    override fun onObjectFatalError() {
        Log.d("ScanBaseActivity", "onObjectFatalError for object detection")
    }

    override fun onPrediction(bitmap: Bitmap, imageWidth: Int, imageHeight: Int) {
        if (!mSentResponse && mIsActivityActive) {
            // do something with the prediction
        }
        mMachineLearningSemaphore.release()
    }

    /**
     * A basic Camera preview class
     */
    inner class CameraPreview(
        context: Context,
        private val mPreviewCallback: Camera.PreviewCallback
    ) : SurfaceView(context), Camera.AutoFocusCallback, SurfaceHolder.Callback {

        private val mHolder: SurfaceHolder = holder

        init {
            mHolder.addCallback(this)
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

            val params = mCamera!!.parameters
            val focusModes = params.supportedFocusModes
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
            params.setRecordingHint(true)
            mCamera!!.parameters = params
        }

        override fun onAutoFocus(success: Boolean, camera: Camera) {}

        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                mCamera ?: return
                mCamera!!.setPreviewDisplay(holder)
                mCamera!!.startPreview()
            } catch (e: IOException) {
                Log.d("CameraCaptureActivity", "Error setting camera preview: ${e.message}")
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            if (mHolder.surface == null) return

            try {
                mCamera!!.stopPreview()
            } catch (e: Exception) {
                // ignore: tried to stop a non-existent preview
            }

            try {
                mCamera!!.setPreviewDisplay(mHolder)
                mCamera!!.setPreviewCallback(mPreviewCallback)
                mCamera!!.startPreview()
            } catch (e: Exception) {
                Log.d("CameraCaptureActivity", "Error starting camera preview: ${e.message}")
            }
        }
    }
}
