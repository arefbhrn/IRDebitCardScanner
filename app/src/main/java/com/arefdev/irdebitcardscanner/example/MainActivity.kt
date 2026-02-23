package com.arefdev.irdebitcardscanner.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ir.arefdev.irdebitcardscanner.ScanActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val TAG: String = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        ScanActivity.warmUp(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.scan_button -> {
                ScanActivity.start(this)
            }

            R.id.scanCardDebug -> {
                ScanActivity.startDebug(this)
            }

            R.id.scanCardAltText -> {
                ScanActivity.start(
                    this, "Debit Card Scan",
                    "Position your card in the frame so the card number is visible"
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (ScanActivity.isScanResult(requestCode)) {
            if (resultCode == RESULT_OK && data != null) {
                val scanResult = ScanActivity.debitCardFromResult(data)

                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("cardNumber", scanResult!!.number)
                intent.putExtra("cardExpiryMonth", scanResult.expiryMonth)
                intent.putExtra("cardExpiryYear", scanResult.expiryYear)
                startActivity(intent)
            } else if (resultCode == ScanActivity.RESULT_CANCELED) {
                val fatalError = data!!.getBooleanExtra(ScanActivity.RESULT_FATAL_ERROR, false)
                if (fatalError) {
                    Log.d(TAG, "fatal error")
                } else {
                    Log.d(TAG, "The user pressed the back button")
                }
            }
        }
    }
}
