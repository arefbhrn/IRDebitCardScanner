package ir.arefdev.irdebitcardscanner;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * The ScanActivity class provides the main interface to the scanning functionality. To use this
 * activity, call the {@link ScanActivity#start(Activity)} method and override
 * onActivityResult in your own activity to get the result of the scan.
 */
public class ScanActivity {

	private static final int REQUEST_CODE = 51234;
	public static final int RESULT_CANCELED = ScanActivityImpl.RESULT_CANCELED;
	public static String RESULT_FATAL_ERROR = ScanBaseActivity.RESULT_FATAL_ERROR;

	/**
	 * Starts a ScanActivityImpl activity, using {@param activity} as a parent.
	 *
	 * @param activity the parent activity that is waiting for the result of the ScanActivity
	 */
	public static void start(@NonNull Activity activity) {
		ScanBaseActivity.warmUp(activity.getApplicationContext());
		Intent intent = new Intent(activity, ScanActivityImpl.class);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Starts a scan activity and customizes the text that it displays.
	 *
	 * @param activity         the parent activity that is waiting for the result of the ScanActivity
	 * @param scanCardText     the large text above the card rectangle
	 * @param positionCardText the small text below the card rectangle
	 */
	public static void start(@NonNull Activity activity, String scanCardText, String positionCardText) {
		ScanBaseActivity.warmUp(activity.getApplicationContext());
		Intent intent = new Intent(activity, ScanActivityImpl.class);
		intent.putExtra(ScanActivityImpl.SCAN_CARD_TEXT, scanCardText);
		intent.putExtra(ScanActivityImpl.POSITION_CARD_TEXT, positionCardText);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Initializes the machine learning models and GPU hardware for faster scan performance.
	 * <p>
	 * This optional static method initializes the machine learning models and GPU hardware in a
	 * background thread so that when the ScanActivity starts it can complete its first scan
	 * quickly. App builders can choose to not call this method and they can call it multiple
	 * times safely.
	 * <p>
	 * This method is thread safe.
	 *
	 * @param activity the activity that invokes this method, which the library uses to get
	 *                 an application context.
	 */
	public static void warmUp(@NonNull Activity activity) {
		ScanBaseActivity.warmUp(activity.getApplicationContext());
	}

	/**
	 * Starts the scan activity and turns on a small debugging window in the bottom left.
	 * <p>
	 * This debugging activity helps designers see some of the machine learning model's internals
	 * by showing boxes around digits and expiry dates that it detects.
	 *
	 * @param activity the parent activity that is waiting for the result of the ScanActivity
	 */
	public static void startDebug(@NonNull Activity activity) {
		ScanBaseActivity.warmUp(activity.getApplicationContext());
		Intent intent = new Intent(activity, ScanActivityImpl.class);
		intent.putExtra("debug", true);
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * A helper method to use within your onActivityResult method to check if the result is from our
	 * scan activity.
	 *
	 * @param requestCode the requestCode passed into the onActivityResult method
	 * @return true if the requestCode matches the requestCode we use for ScanActivity instances
	 */
	public static boolean isScanResult(int requestCode) {
		return requestCode == REQUEST_CODE;
	}

	public static DebitCard debitCardFromResult(Intent intent) {
		String number = intent.getStringExtra(ScanActivityImpl.RESULT_CARD_NUMBER);
		int month = intent.getIntExtra(ScanActivityImpl.RESULT_EXPIRY_MONTH, 0);
		int year = intent.getIntExtra(ScanActivityImpl.RESULT_EXPIRY_YEAR, 0);

		if (TextUtils.isEmpty(number)) {
			return null;
		}

		return new DebitCard(number, month, year);
	}
}
