package ir.arefdev.irdebitcardscanner

import android.graphics.RectF

class CGRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    fun getNewInstance(): RectF = RectF(x, y, x + width, y + height)
}
