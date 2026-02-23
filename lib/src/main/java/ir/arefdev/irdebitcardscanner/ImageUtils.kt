package ir.arefdev.irdebitcardscanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class ImageUtils {

    companion object {
        @JvmStatic
        fun drawBoxesOnImage(frame: Bitmap, boxes: List<DetectedBox>, expiryBox: DetectedBox?): Bitmap {
            val paint = Paint(0)
            paint.color = Color.GREEN
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f

            val mutableBitmap = frame.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)
            for (box in boxes) {
                canvas.drawRect(box.getRect().getNewInstance(), paint)
            }

            paint.color = Color.RED
            if (expiryBox != null) {
                canvas.drawRect(expiryBox.getRect().getNewInstance(), paint)
            }

            return mutableBitmap
        }
    }
}
