package ir.arefdev.irdebitcardscanner;

import android.graphics.Bitmap;

public class Expiry {

	private String string;
	private int month;
	private int year;

	public static Expiry from(RecognizedDigitsModel model, Bitmap image, CGRect box) {
		RecognizedDigits digits = RecognizedDigits.from(model, image, box);
		String string = digits.stringResult();

		if (string.length() != 6) {
			return null;
		}

		String monthString = string.substring(4);
		String yearString = string.substring(0, 3);

		try {
			int month = Integer.parseInt(monthString);
			int year = Integer.parseInt(yearString);

			if (month <= 0 || month > 12) {
				return null;
			}

			int fullYear = (year > 90 ? 1300 : 1400) + year;

			Expiry expiry = new Expiry();
			expiry.month = month;
			expiry.year = fullYear;
			expiry.string = string;

			return expiry;
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	public String format() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			if (i == 4) {
				result.append("/");
			}
			result.append(string.charAt(i));
		}

		return result.toString();
	}

	public String getString() {
		return string;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}
}
