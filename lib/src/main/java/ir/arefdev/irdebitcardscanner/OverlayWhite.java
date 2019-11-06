package ir.arefdev.irdebitcardscanner;

import android.content.Context;
import android.util.AttributeSet;

public class OverlayWhite extends Overlay {

	int backgroundColorId = R.color.irdcs_white_background;
	int cornerColorId = R.color.irdcs_gray;

	public OverlayWhite(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		cornerDp = 3;
	}

	public void setColorIds(int backgroundColorId, int cornerColorId) {
		this.backgroundColorId = backgroundColorId;
		this.cornerColorId = cornerColorId;
		postInvalidate();
	}

	@Override
	protected int getBackgroundColorId() {
		return backgroundColorId;
	}

	@Override
	protected int getCornerColorId() {
		return cornerColorId;
	}
}
