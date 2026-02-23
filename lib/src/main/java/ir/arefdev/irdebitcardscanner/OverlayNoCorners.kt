package ir.arefdev.irdebitcardscanner

import android.content.Context
import android.util.AttributeSet

class OverlayNoCorners(context: Context, attrs: AttributeSet) : Overlay(context, attrs) {
    init {
        drawCorners = false
    }
}
