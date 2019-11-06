package ir.arefdev.irdebitcardscanner;

import android.graphics.Bitmap;

import java.util.List;

interface OnScanListener {

	void onPrediction(final String number, final Expiry expiry, final Bitmap bitmap,
					  final List<DetectedBox> digitBoxes, final DetectedBox expiryBox);

	void onFatalError();

}
