package ir.arefdev.irdebitcardscanner

internal object DebitCardUtils {

    private const val BANK_SLUG_ANSAR = "b_ansar"
    private const val BANK_SLUG_AYANDE = "b_ayande"
    private const val BANK_SLUG_DEY = "b_dey"
    private const val BANK_SLUG_EGHTESAD_NOVIN = "b_eghtesad_novin"
    private const val BANK_SLUG_GARDESH = "b_gardeshgari"
    private const val BANK_SLUG_GHAVAMIN = "b_ghavamin"
    private const val BANK_SLUG_HEKMAT = "b_hekmat"
    private const val BANK_SLUG_IRAN_VENEZUELA = "b_iran_venezuela"
    private const val BANK_SLUG_IRANZAMIN = "b_iranzamin"
    private const val BANK_SLUG_KARAFARIN = "b_karafarin"
    private const val BANK_SLUG_KESHAVARZI = "b_keshavarzi"
    private const val BANK_SLUG_KHAVARMIANE = "b_khavarmiane"
    private const val BANK_SLUG_MASKAN = "b_maskan"
    private const val BANK_SLUG_MEHR_EGHTESAD = "b_mehr_eqtesad"
    private const val BANK_SLUG_MEHR_IRAN = "b_mehr_iran"
    private const val BANK_SLUG_MELLAT = "b_mellat"
    private const val BANK_SLUG_MELLI = "b_melli"
    private const val BANK_SLUG_PARSIAN = "b_parsian"
    private const val BANK_SLUG_PASARGAD = "b_pasargad"
    private const val BANK_SLUG_POST = "b_post"
    private const val BANK_SLUG_REFAH = "b_refah"
    private const val BANK_SLUG_RESALAT = "b_resalat"
    private const val BANK_SLUG_SADERAT = "b_saderat"
    private const val BANK_SLUG_SAMAN = "b_saman"
    private const val BANK_SLUG_SANAT_MADAN = "b_sanat_madan"
    private const val BANK_SLUG_SARMAYE = "b_sarmaye"
    private const val BANK_SLUG_SEPAH = "b_sepah"
    private const val BANK_SLUG_SHAHR = "b_shahr"
    private const val BANK_SLUG_SINA = "b_sina"
    private const val BANK_SLUG_TAAVON = "b_taavon"
    private const val BANK_SLUG_TEJARAT = "b_tejarat"
    private const val BANK_SLUG_TOSEE_SADERAT = "b_tosee_saderat"
    private const val BANK_SLUG_ASKARIE = "io_askarie"
    private const val BANK_SLUG_ETEBARI_TOSE = "io_etebari_tose"
    private const val BANK_SLUG_KOSAR = "io_kosar"
    private const val BANK_SLUG_SAMEN = "io_samen"

    private val CARD_NUMBER_STARTER = mutableMapOf<String, String>()
    private val MANUAL_CARD_NUMBER_STARTER = mutableMapOf<String, String>()
    private var initialized = false

    private fun init() {
        if (!initialized) {
            initialized = true
            CARD_NUMBER_STARTER["627381"] = BANK_SLUG_ANSAR
            CARD_NUMBER_STARTER["636214"] = BANK_SLUG_AYANDE
            CARD_NUMBER_STARTER["502938"] = BANK_SLUG_DEY
            CARD_NUMBER_STARTER["627412"] = BANK_SLUG_EGHTESAD_NOVIN
            CARD_NUMBER_STARTER["505416"] = BANK_SLUG_GARDESH
            CARD_NUMBER_STARTER["639599"] = BANK_SLUG_GHAVAMIN
            CARD_NUMBER_STARTER["636949"] = BANK_SLUG_HEKMAT
//			CARD_NUMBER_STARTER[""] = BANK_SLUG_IRAN_VENEZUELA
            CARD_NUMBER_STARTER["505785"] = BANK_SLUG_IRANZAMIN
            CARD_NUMBER_STARTER["627488"] = BANK_SLUG_KARAFARIN
            CARD_NUMBER_STARTER["502910"] = BANK_SLUG_KARAFARIN
            CARD_NUMBER_STARTER["603770"] = BANK_SLUG_KESHAVARZI
            CARD_NUMBER_STARTER["639217"] = BANK_SLUG_KESHAVARZI
            CARD_NUMBER_STARTER["585947"] = BANK_SLUG_KHAVARMIANE
            CARD_NUMBER_STARTER["628023"] = BANK_SLUG_MASKAN
            CARD_NUMBER_STARTER["639370"] = BANK_SLUG_MEHR_EGHTESAD
            CARD_NUMBER_STARTER["606373"] = BANK_SLUG_MEHR_IRAN
            CARD_NUMBER_STARTER["610433"] = BANK_SLUG_MELLAT
            CARD_NUMBER_STARTER["991975"] = BANK_SLUG_MELLAT
            CARD_NUMBER_STARTER["603799"] = BANK_SLUG_MELLI
            CARD_NUMBER_STARTER["622106"] = BANK_SLUG_PARSIAN
            CARD_NUMBER_STARTER["639194"] = BANK_SLUG_PARSIAN
            CARD_NUMBER_STARTER["627884"] = BANK_SLUG_PARSIAN
            CARD_NUMBER_STARTER["639347"] = BANK_SLUG_PASARGAD
            CARD_NUMBER_STARTER["502229"] = BANK_SLUG_PASARGAD
            CARD_NUMBER_STARTER["627760"] = BANK_SLUG_POST
            CARD_NUMBER_STARTER["589463"] = BANK_SLUG_REFAH
            CARD_NUMBER_STARTER["504172"] = BANK_SLUG_RESALAT
            CARD_NUMBER_STARTER["603769"] = BANK_SLUG_SADERAT
            CARD_NUMBER_STARTER["621986"] = BANK_SLUG_SAMAN
            CARD_NUMBER_STARTER["627961"] = BANK_SLUG_SANAT_MADAN
            CARD_NUMBER_STARTER["639607"] = BANK_SLUG_SARMAYE
            CARD_NUMBER_STARTER["589210"] = BANK_SLUG_SEPAH
            CARD_NUMBER_STARTER["502806"] = BANK_SLUG_SHAHR
            CARD_NUMBER_STARTER["504706"] = BANK_SLUG_SHAHR
            CARD_NUMBER_STARTER["639346"] = BANK_SLUG_SINA
            CARD_NUMBER_STARTER["502908"] = BANK_SLUG_TAAVON
            CARD_NUMBER_STARTER["627353"] = BANK_SLUG_TEJARAT
            CARD_NUMBER_STARTER["585983"] = BANK_SLUG_TEJARAT
            CARD_NUMBER_STARTER["627648"] = BANK_SLUG_TOSEE_SADERAT
            CARD_NUMBER_STARTER["207177"] = BANK_SLUG_TOSEE_SADERAT
            CARD_NUMBER_STARTER["606265"] = BANK_SLUG_ASKARIE
            CARD_NUMBER_STARTER["628157"] = BANK_SLUG_ETEBARI_TOSE
            CARD_NUMBER_STARTER["505801"] = BANK_SLUG_KOSAR
//			CARD_NUMBER_STARTER[""] = BANK_SLUG_SAMEN
        }
    }

    fun addCardNumberStarters(starters: Map<String, String>) {
        init()
        MANUAL_CARD_NUMBER_STARTER.putAll(starters)
    }

    fun getBankSlugFromCardNumber(cardNumber: String): String? {
        init()

        if (cardNumber.length < 6) return null

        val prefix = cardNumber.substring(0, 6)
        if (CARD_NUMBER_STARTER.containsKey(prefix)) return CARD_NUMBER_STARTER[prefix]
        if (MANUAL_CARD_NUMBER_STARTER.containsKey(prefix)) return MANUAL_CARD_NUMBER_STARTER[prefix]

        return null
    }

    fun isCardNumberValid(cardNumber: String): Boolean = luhnCheck(cardNumber)

    // Iranian IBAN: IR + 2 check digits + 22-digit BBAN = 26 chars
    // Rearrange for MOD-97: BBAN + "1827" (I=18, R=27) + check_digits
    // Valid if rearranged_number mod 97 == 1
    fun isIbanValid(iban: String?): Boolean {
        if (iban == null) return false

        val cleaned = iban.replace(" ", "").uppercase()

        if (cleaned.length != 26 || !cleaned.startsWith("IR")) return false

        for (i in 2 until cleaned.length) {
            if (!cleaned[i].isDigit()) return false
        }

        // rearranged = iban[4..25] + "1827" + iban[2..3]
        val rearranged = cleaned.substring(4) + "1827" + cleaned.substring(2, 4)

        var mod = 0
        for (ch in rearranged) {
            mod = (mod * 10 + (ch - '0')) % 97
        }
        return mod == 1
    }

    // https://en.wikipedia.org/wiki/Luhn_algorithm#Java
    fun luhnCheck(ccNumber: String?): Boolean {
        if (ccNumber == null || ccNumber.length != 16 || getBankSlugFromCardNumber(ccNumber) == null) {
            return false
        }

        var sum = 0
        var alternate = false
        for (i in ccNumber.length - 1 downTo 0) {
            var n = ccNumber.substring(i, i + 1).toInt()
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    fun format(number: String): String {
        if (number.length == 16) return format16(number)
        return number
    }

    private fun format16(number: String): String {
        val result = StringBuilder()
        for (i in number.indices) {
            if (i == 4 || i == 8 || i == 12) result.append(" ")
            result.append(number[i])
        }
        return result.toString()
    }
}
