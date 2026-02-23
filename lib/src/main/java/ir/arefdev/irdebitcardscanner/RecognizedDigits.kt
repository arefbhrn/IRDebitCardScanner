package ir.arefdev.irdebitcardscanner

import android.graphics.Bitmap

internal class RecognizedDigits private constructor(
    private val digits: ArrayList<Int>,
    private val confidence: ArrayList<Float>
) {

    companion object {

        private const val kNumPredictions = RecognizedDigitsModel.kNumPredictions
        private const val kBackgroundClass = 10
        private const val kDigitMinConfidence = 0.15f

        fun from(model: RecognizedDigitsModel, image: Bitmap, box: CGRect): RecognizedDigits {
            val frame = Bitmap.createBitmap(image, Math.round(box.x), Math.round(box.y), box.width.toInt(), box.height.toInt())
            model.classifyFrame(frame)

            val digits = ArrayList<Int>()
            val confidence = ArrayList<Float>()

            for (col in 0 until kNumPredictions) {
                val argAndConf = model.argAndValueMax(col)
                if (argAndConf.confidence < kDigitMinConfidence) {
                    digits.add(kBackgroundClass)
                } else {
                    digits.add(argAndConf.argMax)
                }
                confidence.add(argAndConf.confidence)
            }

            return RecognizedDigits(digits, confidence)
        }
    }

    private fun nonMaxSuppression(): ArrayList<Int> {
        val digits = ArrayList<Int>(this.digits)
        val confidence = ArrayList<Float>(this.confidence)

        // greedy non-max suppression
        for (idx in 0 until (kNumPredictions - 1)) {
            if (digits[idx] != kBackgroundClass && digits[idx + 1] != kBackgroundClass) {
                if (confidence[idx] < confidence[idx + 1]) {
                    digits[idx] = kBackgroundClass
                    confidence[idx] = 1.0f
                } else {
                    digits[idx + 1] = kBackgroundClass
                    confidence[idx + 1] = 1.0f
                }
            }
        }

        return digits
    }

    fun stringResult(): String {
        val digits = nonMaxSuppression()
        val result = StringBuilder()
        for (digit in digits) {
            if (digit != kBackgroundClass) result.append(digit)
        }
        return result.toString()
    }

    fun four(): String {
        val digits = nonMaxSuppression()
        var result = stringResult()

        if (result.length < 4) return ""

        // since we know that we have too many digits, trim from the outer most digits. Since we
        // designed our detection model to center digits, this should work
        var fromLeft = true
        var leftIdx = 0
        var rightIdx = digits.size - 1
        while (result.length > 4) {
            if (fromLeft) {
                if (digits[leftIdx] != kBackgroundClass) {
                    result = result.substring(1)
                    digits[leftIdx] = kBackgroundClass
                }
                fromLeft = false
                leftIdx += 1
            } else {
                if (digits[rightIdx] != kBackgroundClass) {
                    result = result.substring(0, result.length - 1)
                    digits[rightIdx] = kBackgroundClass
                }
                fromLeft = true
                rightIdx -= 1
            }
        }

        // as a last error check make sure that all the digits are equally
        // spaced and reject the whole lot if they aren't.
        val positions = ArrayList<Int>()
        for (idx in digits.indices) {
            if (digits[idx] != kBackgroundClass) positions.add(idx)
        }
        val deltas = ArrayList<Int>()
        for (idx in 1 until positions.size) {
            deltas.add(positions[idx] - positions[idx - 1])
        }

        deltas.sort()
        val maxDelta = deltas[deltas.size - 1]
        val minDelta = deltas[0]

        if (maxDelta > (minDelta + 1)) return ""

        return result
    }
}
