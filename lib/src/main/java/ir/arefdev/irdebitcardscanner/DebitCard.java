package ir.arefdev.irdebitcardscanner;

public class DebitCard {

	public final String number;
	public final int expiryMonth;
	public final int expiryYear;

	public DebitCard(String number, int expiryMonth, int expiryYear) {
		this.number = number;
		this.expiryMonth = expiryMonth;
		this.expiryYear = expiryYear;
	}

	public String last4() {
		return number.substring(number.length() - 4);
	}

	public String expiryForDisplay() {
		if (isExpiryValid()) {
			return null;
		}

		String month = String.valueOf(expiryMonth);
		if (month.length() == 1) {
			month = "0" + month;
		}
		String year = String.valueOf(expiryYear);
		if (year.length() == 4) {
			year = year.substring(2);
		}

		return month + "/" + year;
	}

	public boolean isExpiryValid() {
		return expiryMonth <= 0 || expiryMonth > 12 || expiryYear <= 0;
	}
}