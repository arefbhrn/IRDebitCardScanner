package ir.arefdev.irdebitcardscanner

import android.app.Activity
import android.content.Intent
import android.text.TextUtils

/**
 * The ScanActivity class provides the main interface to the scanning functionality. To use this
 * activity, call the [ScanActivity.start] method and override
 * onActivityResult in your own activity to get the result of the scan.
 */
class ScanActivity {

    companion object {
        private const val REQUEST_CODE = 51234

        const val RESULT_CANCELED = Activity.RESULT_CANCELED

        @JvmField
        var RESULT_FATAL_ERROR = ScanBaseActivity.RESULT_FATAL_ERROR

        /**
         * Starts a ScanActivityImpl activity, using [activity] as a parent.
         */
        @JvmStatic
        fun start(activity: Activity) {
            ScanBaseActivity.warmUp(activity.applicationContext)
            val intent = Intent(activity, ScanActivityImpl::class.java)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }

        /**
         * Starts a scan activity and customizes the text that it displays.
         */
        @JvmStatic
        fun start(activity: Activity, scanCardText: String?, positionCardText: String?) {
            ScanBaseActivity.warmUp(activity.applicationContext)
            val intent = Intent(activity, ScanActivityImpl::class.java)
            intent.putExtra(ScanActivityImpl.SCAN_CARD_TEXT, scanCardText)
            intent.putExtra(ScanActivityImpl.POSITION_CARD_TEXT, positionCardText)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }

        /**
         * Initializes the machine learning models and GPU hardware for faster scan performance.
         */
        @JvmStatic
        fun warmUp(activity: Activity) {
            ScanBaseActivity.warmUp(activity.applicationContext)
        }

        /**
         * Starts the scan activity and turns on a small debugging window in the bottom left.
         */
        @JvmStatic
        fun startDebug(activity: Activity) {
            ScanBaseActivity.warmUp(activity.applicationContext)
            val intent = Intent(activity, ScanActivityImpl::class.java)
            intent.putExtra("debug", true)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }

        /**
         * A helper method to use within your onActivityResult method to check if the result is
         * from our scan activity.
         */
        @JvmStatic
        fun isScanResult(requestCode: Int): Boolean = requestCode == REQUEST_CODE

        /**
         * Adds custom card number prefix-to-bank-slug mappings, merged on top of the built-in ones.
         */
        @JvmStatic
        fun addCardNumberStarters(starters: Map<String, String>) {
            DebitCardUtils.addCardNumberStarters(starters)
        }

        /**
         * Validates an Iranian IBAN (26 characters: IR + 2 check digits + 22-digit BBAN).
         */
        @JvmStatic
        fun isIbanValid(iban: String?): Boolean = DebitCardUtils.isIbanValid(iban)

        @JvmStatic
        fun debitCardFromResult(intent: Intent): DebitCard? {
            val number = intent.getStringExtra(ScanActivityImpl.RESULT_CARD_NUMBER)
            val month = intent.getIntExtra(ScanActivityImpl.RESULT_EXPIRY_MONTH, 0)
            val year = intent.getIntExtra(ScanActivityImpl.RESULT_EXPIRY_YEAR, 0)

            if (TextUtils.isEmpty(number)) return null

            return DebitCard(number!!, month, year)
        }
    }
}
