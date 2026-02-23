package ir.arefdev.irdebitcardscanner

import android.graphics.Bitmap

internal class RecognizeNumbers(private val image: Bitmap, numRows: Int, numCols: Int) {

    private val recognizedDigits: Array<Array<RecognizedDigits?>> = Array(numRows) { arrayOfNulls(numCols) }

    fun number(model: RecognizedDigitsModel, lines: ArrayList<ArrayList<DetectedBox>>): String? {
        for (line in lines) {
            val candidateNumber = StringBuilder()

            for (word in line) {
                val recognized = cachedDigits(model, word) ?: return null
                candidateNumber.append(recognized.stringResult())
            }

            if (candidateNumber.length == 16 && DebitCardUtils.luhnCheck(candidateNumber.toString())) {
                return candidateNumber.toString()
            }
        }

        return null
    }

    private fun cachedDigits(model: RecognizedDigitsModel, box: DetectedBox): RecognizedDigits? {
        if (recognizedDigits[box.row][box.col] == null) {
            recognizedDigits[box.row][box.col] = RecognizedDigits.from(model, image, box.getRect())
        }
        return recognizedDigits[box.row][box.col]
    }
}
