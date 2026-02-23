package ir.arefdev.irdebitcardscanner

import android.hardware.Camera

internal interface OnCameraOpenListener {
    fun onCameraOpen(camera: Camera?)
}
