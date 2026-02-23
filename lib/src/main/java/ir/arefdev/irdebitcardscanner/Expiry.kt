package ir.arefdev.irdebitcardscanner

import android.graphics.Bitmap

class Expiry private constructor(
    private val string: String,
    private val month: Int,
    private val year: Int
) {

    companion object {
        @JvmStatic
        fun from(model: RecognizedDigitsModel, image: Bitmap, box: CGRect): Expiry? {
            val digits = RecognizedDigits.from(model, image, box)
            val string = digits.stringResult()

            if (string.length != 6) return null

            val monthString = string.substring(4)
            val yearString = string.substring(0, 3)

            return try {
                val month = monthString.toInt()
                val year = yearString.toInt()

                if (month !in 1..12) return null

                val fullYear = (if (year > 90) 1300 else 1400) + year

                Expiry(string, month, fullYear)
            } catch (e: NumberFormatException) {
                null
            }
        }
    }

    fun format(): String {
        val result = StringBuilder()
        for (i in string.indices) {
            if (i == 4) result.append("/")
            result.append(string[i])
        }
        return result.toString()
    }

    fun getString(): String = string

    fun getYear(): Int = year

    fun getMonth(): Int = month
}
