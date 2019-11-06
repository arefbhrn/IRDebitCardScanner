package ir.arefdev.irdebitcardscanner;

import android.content.Context;
import android.util.AttributeSet;

public class OverlayNoCorners extends Overlay {

	public OverlayNoCorners(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.drawCorners = false;
	}

}
