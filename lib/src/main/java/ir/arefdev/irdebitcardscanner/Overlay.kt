package ir.arefdev.irdebitcardscanner

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Xfermode
import android.util.AttributeSet
import android.view.View
import com.arefbhrn.irdebitcardscanner.R

// adapted from this: https://medium.com/@rgomez/android-how-to-draw-an-overlay-with-a-transparent-hole-471af6cf3953
open class Overlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var rect: RectF? = null
    private val oval = RectF()
    private var radius: Int = 0
    private val xfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    var cornerDp = 6
    var drawCorners = true

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    protected open fun getBackgroundColorId(): Int = R.color.irdcs_camera_background

    protected open fun getCornerColorId(): Int = R.color.irdcs_corner_color

    fun setCircle(rect: RectF, radius: Int) {
        this.rect = rect
        this.radius = radius
        postInvalidate()
    }

    private fun dpToPx(dp: Int): Int =
        (dp * Resources.getSystem().displayMetrics.density).toInt()

    @Suppress("DEPRECATION")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rect = this.rect ?: return

        val paintAntiAlias = Paint(Paint.ANTI_ALIAS_FLAG)
        paintAntiAlias.color = resources.getColor(getBackgroundColorId())
        paintAntiAlias.style = Paint.Style.FILL
        canvas.drawPaint(paintAntiAlias)

        paintAntiAlias.xfermode = xfermode
        canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paintAntiAlias)

        if (!drawCorners) return

        val paint = Paint()
        paint.color = resources.getColor(getCornerColorId())
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpToPx(cornerDp).toFloat()

        // top left
        val lineLength = dpToPx(20)
        var x = rect.left - dpToPx(1)
        var y = rect.top - dpToPx(1)
        oval.left = x
        oval.top = y
        oval.right = x + 2 * radius
        oval.bottom = y + 2 * radius
        canvas.drawArc(oval, 180f, 90f, false, paint)
        canvas.drawLine(oval.left, oval.bottom - radius, oval.left, oval.bottom - radius + lineLength, paint)
        canvas.drawLine(oval.right - radius, oval.top, oval.right - radius + lineLength, oval.top, paint)

        // top right
        x = rect.right + dpToPx(1) - 2 * radius
        y = rect.top - dpToPx(1)
        oval.left = x
        oval.top = y
        oval.right = x + 2 * radius
        oval.bottom = y + 2 * radius
        canvas.drawArc(oval, 270f, 90f, false, paint)
        canvas.drawLine(oval.right, oval.bottom - radius, oval.right, oval.bottom - radius + lineLength, paint)
        canvas.drawLine(oval.right - radius, oval.top, oval.right - radius - lineLength, oval.top, paint)

        // bottom right
        x = rect.right + dpToPx(1) - 2 * radius
        y = rect.bottom + dpToPx(1) - 2 * radius
        oval.left = x
        oval.top = y
        oval.right = x + 2 * radius
        oval.bottom = y + 2 * radius
        canvas.drawArc(oval, 0f, 90f, false, paint)
        canvas.drawLine(oval.right, oval.bottom - radius, oval.right, oval.bottom - radius - lineLength, paint)
        canvas.drawLine(oval.right - radius, oval.bottom, oval.right - radius - lineLength, oval.bottom, paint)

        // bottom left
        x = rect.left - dpToPx(1)
        y = rect.bottom + dpToPx(1) - 2 * radius
        oval.left = x
        oval.top = y
        oval.right = x + 2 * radius
        oval.bottom = y + 2 * radius
        canvas.drawArc(oval, 90f, 90f, false, paint)
        canvas.drawLine(oval.left, oval.bottom - radius, oval.left, oval.bottom - radius - lineLength, paint)
        canvas.drawLine(oval.right - radius, oval.bottom, oval.right - radius + lineLength, oval.bottom, paint)
    }
}
