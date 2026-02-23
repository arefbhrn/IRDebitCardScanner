package ir.arefdev.irdebitcardscanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import androidx.core.graphics.createBitmap
import java.io.File
import java.util.LinkedList

@Suppress("DEPRECATION")
class MachineLearningThread : Runnable {

    class RunArguments {

        val mFrameBytes: ByteArray?
        val mBitmap: Bitmap?
        val mScanListener: OnScanListener?
        val mObjectListener: OnObjectListener?
        val mContext: Context
        val mWidth: Int
        val mHeight: Int
        val mFormat: Int
        val mSensorOrientation: Int
        val mRoiCenterYRatio: Float
        val mIsOcr: Boolean
        val mObjectDetectFile: File?

        constructor(
            frameBytes: ByteArray?,
            width: Int,
            height: Int,
            format: Int,
            sensorOrientation: Int,
            scanListener: OnScanListener?,
            context: Context,
            roiCenterYRatio: Float
        ) {
            mFrameBytes = frameBytes
            mBitmap = null
            mWidth = width
            mHeight = height
            mFormat = format
            mScanListener = scanListener
            mContext = context
            mSensorOrientation = sensorOrientation
            mRoiCenterYRatio = roiCenterYRatio
            mIsOcr = true
            mObjectListener = null
            mObjectDetectFile = null
        }

        constructor(
            frameBytes: ByteArray?,
            width: Int,
            height: Int,
            format: Int,
            sensorOrientation: Int,
            objectListener: OnObjectListener?,
            context: Context,
            roiCenterYRatio: Float,
            objectDetectFile: File?
        ) {
            mFrameBytes = frameBytes
            mBitmap = null
            mWidth = width
            mHeight = height
            mFormat = format
            mScanListener = null
            mContext = context
            mSensorOrientation = sensorOrientation
            mRoiCenterYRatio = roiCenterYRatio
            mIsOcr = false
            mObjectListener = objectListener
            mObjectDetectFile = objectDetectFile
        }

        // this should only be used for testing
        constructor(bitmap: Bitmap?, scanListener: OnScanListener?, context: Context) {
            mFrameBytes = null
            mBitmap = bitmap
            mWidth = bitmap?.width ?: 0
            mHeight = bitmap?.height ?: 0
            mFormat = 0
            mScanListener = scanListener
            mContext = context
            mSensorOrientation = 0
            mRoiCenterYRatio = 0f
            mIsOcr = true
            mObjectListener = null
            mObjectDetectFile = null
        }

        // this should only be used for testing
        constructor(
            bitmap: Bitmap?,
            objectListener: OnObjectListener?,
            context: Context,
            objectDetectFile: File?
        ) {
            mFrameBytes = null
            mBitmap = bitmap
            mWidth = bitmap?.width ?: 0
            mHeight = bitmap?.height ?: 0
            mFormat = 0
            mScanListener = null
            mContext = context
            mSensorOrientation = 0
            mRoiCenterYRatio = 0f
            mIsOcr = false
            mObjectListener = objectListener
            mObjectDetectFile = objectDetectFile
        }
    }

    private val queue: LinkedList<RunArguments> = LinkedList()

    @Synchronized
    fun warmUp(context: Context) {
        if (OCR.isInit() || queue.isNotEmpty()) return
        val args = RunArguments(null, 0, 0, 0, 90, null as OnScanListener?, context, 0.5f)
        queue.push(args)
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as Object).notify()
    }

    @Synchronized
    fun post(bitmap: Bitmap, scanListener: OnScanListener, context: Context) {
        val args = RunArguments(bitmap, scanListener, context)
        queue.push(args)
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as Object).notify()
    }

    @Synchronized
    fun post(
        bytes: ByteArray,
        width: Int,
        height: Int,
        format: Int,
        sensorOrientation: Int,
        scanListener: OnScanListener,
        context: Context,
        roiCenterYRatio: Float
    ) {
        val args = RunArguments(bytes, width, height, format, sensorOrientation, scanListener, context, roiCenterYRatio)
        queue.push(args)
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as Object).notify()
    }

    @Synchronized
    fun post(bitmap: Bitmap, objectListener: OnObjectListener, context: Context, objectDetectFile: File?) {
        val args = RunArguments(bitmap, objectListener, context, objectDetectFile)
        queue.push(args)
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as Object).notify()
    }

    @Synchronized
    fun post(
        bytes: ByteArray,
        width: Int,
        height: Int,
        format: Int,
        sensorOrientation: Int,
        objectListener: OnObjectListener,
        context: Context,
        roiCenterYRatio: Float,
        objectDetectFile: File?
    ) {
        val args = RunArguments(bytes, width, height, format, sensorOrientation, objectListener, context, roiCenterYRatio, objectDetectFile)
        queue.push(args)
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as Object).notify()
    }

    // from https://stackoverflow.com/questions/43623817/android-yuv-nv12-to-rgb-conversion-with-renderscript
    private fun yuvToRgb(yuvByteArray: ByteArray, w: Int, h: Int, ctx: Context): Bitmap {
        val rs = RenderScript.create(ctx)
        val yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

        val yuvType = Type.Builder(rs, Element.U8(rs)).setX(yuvByteArray.size)
        val inAlloc = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT)

        val rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(w).setY(h)
        val outAlloc = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT)

        inAlloc.copyFrom(yuvByteArray)

        yuvToRgbIntrinsic.setInput(inAlloc)
        yuvToRgbIntrinsic.forEach(outAlloc)
        val bmp = createBitmap(w, h)
        outAlloc.copyTo(bmp)

        yuvToRgbIntrinsic.destroy()
        rs.destroy()
        inAlloc.destroy()
        outAlloc.destroy()
        return bmp
    }

    private fun getBitmap(
        bytes: ByteArray,
        width: Int,
        height: Int,
        format: Int,
        sensorOrientation: Int,
        roiCenterYRatio: Float,
        ctx: Context,
        isOcr: Boolean
    ): Bitmap {
        val bitmap = yuvToRgb(bytes, width, height, ctx)

        val orientation = sensorOrientation % 360

        val h: Double
        val w: Double
        val x: Int
        val y: Int

        when (orientation) {
            0 -> {
                w = bitmap.width.toDouble()
                h = if (isOcr) w * 302.0 / 480.0 else w
                x = 0
                y = Math.round(bitmap.height.toDouble() * roiCenterYRatio - h * 0.5).toInt()
            }

            90 -> {
                h = bitmap.height.toDouble()
                w = if (isOcr) h * 302.0 / 480.0 else h
                y = 0
                x = Math.round(bitmap.width.toDouble() * roiCenterYRatio - w * 0.5).toInt()
            }

            180 -> {
                w = bitmap.width.toDouble()
                h = if (isOcr) w * 302.0 / 480.0 else w
                x = 0
                y = Math.round(bitmap.height.toDouble() * (1.0 - roiCenterYRatio) - h * 0.5).toInt()
            }

            else -> {
                h = bitmap.height.toDouble()
                w = if (isOcr) h * 302.0 / 480.0 else h
                x = Math.round(bitmap.width.toDouble() * (1.0 - roiCenterYRatio) - w * 0.5).toInt()
                y = 0
            }
        }

        var cx = x
        var cy = y

        // make sure that our crop stays within the image
        if (cx < 0) cx = 0
        if (cy < 0) cy = 0
        if ((cx + w) > bitmap.width) cx = bitmap.width - w.toInt()
        if ((cy + h) > bitmap.height) cy = bitmap.height - h.toInt()

        val croppedBitmap = Bitmap.createBitmap(bitmap, cx, cy, w.toInt(), h.toInt())

        val matrix = Matrix()
        matrix.postRotate(orientation.toFloat())
        val bm = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true)

        croppedBitmap.recycle()
        bitmap.recycle()

        return bm
    }

    @Synchronized
    private fun getNextImage(): RunArguments {
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        while (queue.isEmpty()) {
            try {
                (this as Object).wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return queue.pop()
    }

    private fun runObjectModel(bitmap: Bitmap, args: RunArguments) {
        if (args.mObjectDetectFile == null) {
            Handler(Looper.getMainLooper()).post {
                args.mObjectListener?.onPrediction(bitmap, bitmap.width, bitmap.height)
            }
            return
        }

        Handler(Looper.getMainLooper()).post {
            try {
                args.mObjectListener?.onPrediction(bitmap, bitmap.width, bitmap.height)
            } catch (e: Throwable) {
                // prevent callbacks from crashing the app, swallow it
                e.printStackTrace()
            }
        }
    }

    private fun runOcrModel(bitmap: Bitmap, args: RunArguments) {
        val ocr = OCR()
        val number = ocr.predict(bitmap, args.mContext)
        val hadUnrecoverableException = ocr.hadUnrecoverableException
        Handler(Looper.getMainLooper()).post {
            try {
                if (args.mScanListener != null) {
                    if (hadUnrecoverableException) {
                        args.mScanListener.onFatalError()
                    } else {
                        args.mScanListener.onPrediction(number, ocr.expiry, bitmap, ocr.digitBoxes, ocr.expiryBox)
                    }
                }
            } catch (e: Throwable) {
                // prevent callbacks from crashing the app, swallow it
                e.printStackTrace()
            }
        }
    }

    private fun runModel() {
        val args = getNextImage()

        val bm: Bitmap = if (args.mFrameBytes != null) {
            getBitmap(
                args.mFrameBytes, args.mWidth, args.mHeight, args.mFormat,
                args.mSensorOrientation, args.mRoiCenterYRatio, args.mContext, args.mIsOcr
            )
        } else if (args.mBitmap != null) {
            args.mBitmap
        } else {
            val b = createBitmap(480, 302)
            val canvas = Canvas(b)
            val paint = Paint()
            paint.color = Color.GRAY
            canvas.drawRect(0f, 0f, 480f, 302f, paint)
            b
        }

        if (args.mIsOcr) {
            runOcrModel(bm, args)
        } else {
            runObjectModel(bm, args)
        }
    }

    override fun run() {
        while (true) {
            try {
                runModel()
            } catch (e: Throwable) {
                // center field exception handling, make sure that the ml thread keeps running
                e.printStackTrace()
            }
        }
    }
}
