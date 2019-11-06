package ir.arefdev.irdebitcardscanner.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import ir.arefdev.irdebitcardscanner.DebitCard;
import ir.arefdev.irdebitcardscanner.ScanActivity;

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

	public static final String TAG = "LaunchActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);

		ScanActivity.warmUp(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.scan_button) {
			ScanActivity.start(this);
		} else if (v.getId() == R.id.scanCardDebug) {
			ScanActivity.startDebug(this);
		} else if (v.getId() == R.id.scanCardAltText) {
			ScanActivity.start(this, "Debit Card Scan",
					"Position your card in the frame so the card number is visible");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (ScanActivity.isScanResult(requestCode)) {
			if (resultCode == Activity.RESULT_OK && data != null) {
				DebitCard scanResult = ScanActivity.debitCardFromResult(data);

				Intent intent = new Intent(this, ResultActivity.class);
				intent.putExtra("cardNumber", scanResult.number);
				intent.putExtra("cardExpiryMonth", scanResult.expiryMonth);
				intent.putExtra("cardExpiryYear", scanResult.expiryYear);
				startActivity(intent);
			} else if (resultCode == ScanActivity.RESULT_CANCELED) {
				boolean fatalError = data.getBooleanExtra(ScanActivity.RESULT_FATAL_ERROR, false);
				if (fatalError) {
					Log.d(TAG, "fatal error");
				} else {
					Log.d(TAG, "The user pressed the back button");
				}
			}
		}
	}
}
