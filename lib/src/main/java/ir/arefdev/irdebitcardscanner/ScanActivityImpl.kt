package ir.arefdev.irdebitcardscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.arefbhrn.irdebitcardscanner.R

class ScanActivityImpl : ScanBaseActivity() {

    companion object {
        private const val TAG = "ScanActivityImpl"

        const val SCAN_CARD_TEXT = "scanCardText"
        const val POSITION_CARD_TEXT = "positionCardText"

        const val RESULT_CARD_NUMBER = "cardNumber"
        const val RESULT_EXPIRY_MONTH = "expiryMonth"
        const val RESULT_EXPIRY_YEAR = "expiryYear"

        private var startTimeMs: Long = 0
    }

    private lateinit var mDebugImageView: ImageView
    private var mInDebugMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.irdcs_activity_scan_card)

        val scanCardText = intent.getStringExtra(SCAN_CARD_TEXT)
        if (!TextUtils.isEmpty(scanCardText)) {
            (findViewById<TextView>(R.id.scanCard)).text = scanCardText
        }

        val positionCardText = intent.getStringExtra(POSITION_CARD_TEXT)
        if (!TextUtils.isEmpty(positionCardText)) {
            (findViewById<TextView>(R.id.positionCard)).text = positionCardText
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 110)
            } else {
                mIsPermissionCheckDone = true
            }
        } else {
            // no permission checks
            mIsPermissionCheckDone = true
        }

        findViewById<View>(R.id.closeButton).setOnClickListener {
            onBackPressed()
        }

        mDebugImageView = findViewById(R.id.debugImageView)
        mInDebugMode = intent.getBooleanExtra("debug", false)
        if (!mInDebugMode) {
            mDebugImageView.visibility = View.INVISIBLE
        }
        setViewIds(R.id.flashlightButton, R.id.cardRectangle, R.id.shadedBackground, R.id.texture, R.id.cardNumber, R.id.expiry)
    }

    override fun onCardScanned(numberResult: String?, month: String?, year: String?) {
        val intent = Intent()
        intent.putExtra(RESULT_CARD_NUMBER, numberResult)
        intent.putExtra(RESULT_EXPIRY_MONTH, month)
        intent.putExtra(RESULT_EXPIRY_YEAR, year)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onPrediction(
        number: String?,
        expiry: Expiry?,
        bitmap: Bitmap,
        digitBoxes: List<DetectedBox>,
        expiryBox: DetectedBox?
    ) {
        if (mInDebugMode) {
            mDebugImageView.setImageBitmap(ImageUtils.drawBoxesOnImage(bitmap, digitBoxes, expiryBox))
            Log.d(TAG, "Prediction (ms): ${SystemClock.uptimeMillis() - mPredictionStartMs}")
            if (startTimeMs != 0L) {
                Log.d(TAG, "time to first prediction: ${SystemClock.uptimeMillis() - startTimeMs}")
                startTimeMs = 0
            }
        }

        super.onPrediction(number, expiry, bitmap, digitBoxes, expiryBox)
    }
}
