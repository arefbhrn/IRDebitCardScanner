package ir.arefdev.irdebitcardscanner

import android.content.Context
import android.util.AttributeSet
import com.arefbhrn.irdebitcardscanner.R

class OverlayWhite(context: Context, attrs: AttributeSet) : Overlay(context, attrs) {

    private var backgroundColorId = R.color.irdcs_white_background
    private var cornerColorId = R.color.irdcs_gray

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        cornerDp = 3
    }

    fun setColorIds(backgroundColorId: Int, cornerColorId: Int) {
        this.backgroundColorId = backgroundColorId
        this.cornerColorId = cornerColorId
        postInvalidate()
    }

    override fun getBackgroundColorId(): Int = backgroundColorId

    override fun getCornerColorId(): Int = cornerColorId
}
