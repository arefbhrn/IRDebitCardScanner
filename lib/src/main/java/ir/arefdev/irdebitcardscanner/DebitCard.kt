package ir.arefdev.irdebitcardscanner

class DebitCard(@JvmField val number: String, @JvmField val expiryMonth: Int, @JvmField val expiryYear: Int) {

    fun last4(): String = number.substring(number.length - 4)

    fun expiryForDisplay(): String? {
        if (isExpiryValid()) return null

        var month = expiryMonth.toString()
        if (month.length == 1) month = "0$month"
        var year = expiryYear.toString()
        if (year.length == 4) year = year.substring(2)

        return "$month/$year"
    }

    fun isExpiryValid(): Boolean = expiryMonth !in 1..12 || expiryYear <= 0
}
