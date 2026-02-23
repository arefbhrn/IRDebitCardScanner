package ir.arefdev.irdebitcardscanner

import android.graphics.Bitmap

interface OnScanListener {

    fun onPrediction(
        number: String?,
        expiry: Expiry?,
        bitmap: Bitmap,
        digitBoxes: List<DetectedBox>,
        expiryBox: DetectedBox?
    )

    fun onFatalError()
}
