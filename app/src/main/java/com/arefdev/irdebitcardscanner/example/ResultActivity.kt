package com.arefdev.irdebitcardscanner.example

import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ir.arefdev.irdebitcardscanner.DebitCard

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val number = intent.getStringExtra("cardNumber")
        val expiryMonth = intent.getIntExtra("cardExpiryMonth", 0)
        val expiryYear = intent.getIntExtra("cardExpiryYear", 0)
        val card = DebitCard(number!!, expiryMonth, expiryYear)

        val cardInputWidget = findViewById<TextView>(R.id.card_input_widget)

        val formattedCardNumber = StringBuilder()
        for (i in 0..<card.number.length) {
            if (i == 4 || i == 8 || i == 12) {
                formattedCardNumber.append(" ")
            }
            formattedCardNumber.append(card.number.get(i))
        }
        var txt = formattedCardNumber.toString()

        if (!TextUtils.isEmpty(card.expiryForDisplay())) {
            txt += " \t " + card.expiryForDisplay()
        }
        cardInputWidget.text = txt
    }
}
