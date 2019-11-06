package ir.arefdev.irdebitcardscanner.example;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ir.arefdev.irdebitcardscanner.DebitCard;

public class ResultActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);

		String number = getIntent().getStringExtra("cardNumber");
		int expiryMonth = getIntent().getIntExtra("cardExpiryMonth", 0);
		int expiryYear = getIntent().getIntExtra("cardExpiryYear", 0);
		DebitCard card = new DebitCard(number, expiryMonth, expiryYear);

		TextView cardInputWidget = findViewById(R.id.card_input_widget);

		StringBuilder formattedCardNumber = new StringBuilder();
		for (int i = 0; i < card.number.length(); i++) {
			if (i == 4 || i == 8 || i == 12) {
				formattedCardNumber.append(" ");
			}
			formattedCardNumber.append(card.number.charAt(i));
		}
		String txt = formattedCardNumber.toString();

		if (!TextUtils.isEmpty(card.expiryForDisplay())) {
			txt += " \t " + card.expiryForDisplay();
		}
		cardInputWidget.setText(txt);
	}
}
