package ir.arefdev.irdebitcardscanner;

import java.util.HashMap;

class DebitCardUtils {

	private static final String BANK_SLUG_ANSAR = "b_ansar";
	private static final String BANK_SLUG_AYANDE = "b_ayande";
	private static final String BANK_SLUG_DEY = "b_dey";
	private static final String BANK_SLUG_EGHTESAD_NOVIN = "b_eghtesad_novin";
	private static final String BANK_SLUG_GARDESH = "b_gardeshgari";
	private static final String BANK_SLUG_GHAVAMIN = "b_ghavamin";
	private static final String BANK_SLUG_HEKMAT = "b_hekmat";
	private static final String BANK_SLUG_IRAN_VENEZUELA = "b_iran_venezuela";
	private static final String BANK_SLUG_IRANZAMIN = "b_iranzamin";
	private static final String BANK_SLUG_KARAFARIN = "b_karafarin";
	private static final String BANK_SLUG_KESHAVARZI = "b_keshavarzi";
	private static final String BANK_SLUG_KHAVARMIANE = "b_khavarmiane";
	private static final String BANK_SLUG_MASKAN = "b_maskan";
	private static final String BANK_SLUG_MEHR_EGHTESAD = "b_mehr_eqtesad";
	private static final String BANK_SLUG_MEHR_IRAN = "b_mehr_iran";
	private static final String BANK_SLUG_MELLAT = "b_mellat";
	private static final String BANK_SLUG_MELLI = "b_melli";
	private static final String BANK_SLUG_PARSIAN = "b_parsian";
	private static final String BANK_SLUG_PASARGAD = "b_pasargad";
	private static final String BANK_SLUG_POST = "b_post";
	private static final String BANK_SLUG_REFAH = "b_refah";
	private static final String BANK_SLUG_RESALAT = "b_resalat";
	private static final String BANK_SLUG_SADERAT = "b_saderat";
	private static final String BANK_SLUG_SAMAN = "b_saman";
	private static final String BANK_SLUG_SANAT_MADAN = "b_sanat_madan";
	private static final String BANK_SLUG_SARMAYE = "b_sarmaye";
	private static final String BANK_SLUG_SEPAH = "b_sepah";
	private static final String BANK_SLUG_SHAHR = "b_shahr";
	private static final String BANK_SLUG_SINA = "b_sina";
	private static final String BANK_SLUG_TAAVON = "b_taavon";
	private static final String BANK_SLUG_TEJARAT = "b_tejarat";
	private static final String BANK_SLUG_TOSEE_SADERAT = "b_tosee_saderat";
	private static final String BANK_SLUG_ASKARIE = "io_askarie";
	private static final String BANK_SLUG_ETEBARI_TOSE = "io_etebari_tose";
	private static final String BANK_SLUG_KOSAR = "io_kosar";
	private static final String BANK_SLUG_SAMEN = "io_samen";

	private static HashMap<String, String> CARD_NUMBER_STARTER = new HashMap<>();

	private static void init() {
		if (CARD_NUMBER_STARTER.isEmpty()) {
			CARD_NUMBER_STARTER.put("627381", BANK_SLUG_ANSAR);
			CARD_NUMBER_STARTER.put("636214", BANK_SLUG_AYANDE);
			CARD_NUMBER_STARTER.put("502938", BANK_SLUG_DEY);
			CARD_NUMBER_STARTER.put("627412", BANK_SLUG_EGHTESAD_NOVIN);
			CARD_NUMBER_STARTER.put("505416", BANK_SLUG_GARDESH);
			CARD_NUMBER_STARTER.put("639599", BANK_SLUG_GHAVAMIN);
			CARD_NUMBER_STARTER.put("636949", BANK_SLUG_HEKMAT);
//			CARD_NUMBER_STARTER.put("", BANK_SLUG_IRAN_VENEZUELA);
			CARD_NUMBER_STARTER.put("505785", BANK_SLUG_IRANZAMIN);
			CARD_NUMBER_STARTER.put("627488", BANK_SLUG_KARAFARIN);
			CARD_NUMBER_STARTER.put("502910", BANK_SLUG_KARAFARIN);
			CARD_NUMBER_STARTER.put("603770", BANK_SLUG_KESHAVARZI);
			CARD_NUMBER_STARTER.put("639217", BANK_SLUG_KESHAVARZI);
//			CARD_NUMBER_STARTER.put("", BANK_SLUG_KHAVARMIANE);
			CARD_NUMBER_STARTER.put("628023", BANK_SLUG_MASKAN);
			CARD_NUMBER_STARTER.put("639370", BANK_SLUG_MEHR_EGHTESAD);
			CARD_NUMBER_STARTER.put("606373", BANK_SLUG_MEHR_IRAN);
			CARD_NUMBER_STARTER.put("610433", BANK_SLUG_MELLAT);
			CARD_NUMBER_STARTER.put("991975", BANK_SLUG_MELLAT);
			CARD_NUMBER_STARTER.put("603799", BANK_SLUG_MELLI);
			CARD_NUMBER_STARTER.put("622106", BANK_SLUG_PARSIAN);
			CARD_NUMBER_STARTER.put("639194", BANK_SLUG_PARSIAN);
			CARD_NUMBER_STARTER.put("627884", BANK_SLUG_PARSIAN);
			CARD_NUMBER_STARTER.put("639347", BANK_SLUG_PASARGAD);
			CARD_NUMBER_STARTER.put("502229", BANK_SLUG_PASARGAD);
			CARD_NUMBER_STARTER.put("627760", BANK_SLUG_POST);
			CARD_NUMBER_STARTER.put("589463", BANK_SLUG_REFAH);
			CARD_NUMBER_STARTER.put("504172", BANK_SLUG_RESALAT);
			CARD_NUMBER_STARTER.put("603769", BANK_SLUG_SADERAT);
			CARD_NUMBER_STARTER.put("621986", BANK_SLUG_SAMAN);
			CARD_NUMBER_STARTER.put("627961", BANK_SLUG_SANAT_MADAN);
			CARD_NUMBER_STARTER.put("639607", BANK_SLUG_SARMAYE);
			CARD_NUMBER_STARTER.put("589210", BANK_SLUG_SEPAH);
			CARD_NUMBER_STARTER.put("502806", BANK_SLUG_SHAHR);
			CARD_NUMBER_STARTER.put("504706", BANK_SLUG_SHAHR);
			CARD_NUMBER_STARTER.put("639346", BANK_SLUG_SINA);
			CARD_NUMBER_STARTER.put("502908", BANK_SLUG_TAAVON);
			CARD_NUMBER_STARTER.put("627353", BANK_SLUG_TEJARAT);
			CARD_NUMBER_STARTER.put("585983", BANK_SLUG_TEJARAT);
			CARD_NUMBER_STARTER.put("627648", BANK_SLUG_TOSEE_SADERAT);
			CARD_NUMBER_STARTER.put("207177", BANK_SLUG_TOSEE_SADERAT);
			CARD_NUMBER_STARTER.put("606265", BANK_SLUG_ASKARIE);
			CARD_NUMBER_STARTER.put("628157", BANK_SLUG_ETEBARI_TOSE);
			CARD_NUMBER_STARTER.put("505801", BANK_SLUG_KOSAR);
//			CARD_NUMBER_STARTER.put("", BANK_SLUG_SAMEN);
		}
	}

	public static String getBankSlugFromCardNumber(String cardNumber) {
		init();

		if (cardNumber.length() < 6)
			return null;

		if (CARD_NUMBER_STARTER.containsKey(cardNumber.substring(0, 6)))
			return CARD_NUMBER_STARTER.get(cardNumber.substring(0, 6));

		return null;
	}

	public static boolean isCardNumberValid(String cardNumber) {
		return luhnCheck(cardNumber);
	}

	// https://en.wikipedia.org/wiki/Luhn_algorithm#Java
	static boolean luhnCheck(String ccNumber) {
		if (ccNumber == null || ccNumber.length() != 16 || getBankSlugFromCardNumber(ccNumber) == null) {
			return false;
		}

		int sum = 0;
		boolean alternate = false;
		for (int i = ccNumber.length() - 1; i >= 0; i--) {
			int n = Integer.parseInt(ccNumber.substring(i, i + 1));
			if (alternate) {
				n *= 2;
				if (n > 9) {
					n = (n % 10) + 1;
				}
			}
			sum += n;
			alternate = !alternate;
		}
		return (sum % 10 == 0);
	}

	public static String format(String number) {
		if (number.length() == 16) {
			return format16(number);
		}

		return number;
	}

	private static String format16(String number) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number.length(); i++) {
			if (i == 4 || i == 8 || i == 12) {
				result.append(" ");
			}
			result.append(number.charAt(i));
		}

		return result.toString();
	}
}
