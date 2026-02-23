package ir.arefdev.irdebitcardscanner

import android.content.Context
import java.io.IOException
import java.nio.MappedByteBuffer

class RecognizedDigitsModel @Throws(IOException::class) constructor(context: Context) : ImageClassifier(context) {

    companion object {
        const val kNumPredictions = 17
    }

    private val classes = 11
    private val labelProbArray = Array(1) { Array(1) { Array(kNumPredictions) { FloatArray(classes) } } }

    inner class ArgMaxAndConfidence(val argMax: Int, val confidence: Float)

    fun argAndValueMax(col: Int): ArgMaxAndConfidence {
        var maxIdx = -1
        var maxValue = -1f
        for (idx in 0 until classes) {
            val value = labelProbArray[0][0][col][idx]
            if (value > maxValue) {
                maxIdx = idx
                maxValue = value
            }
        }
        return ArgMaxAndConfidence(maxIdx, maxValue)
    }

    @Throws(IOException::class)
    override fun loadModelFile(context: Context): MappedByteBuffer =
        ResourceModelFactory.loadRecognizeDigitsFile(context)

    override fun getImageSizeX(): Int = 80

    override fun getImageSizeY(): Int = 36

    override fun getNumBytesPerChannel(): Int = 4

    override fun addPixelValue(pixelValue: Int) {
        imgData!!.putFloat(((pixelValue shr 16) and 0xFF) / 255f)
        imgData!!.putFloat(((pixelValue shr 8) and 0xFF) / 255f)
        imgData!!.putFloat((pixelValue and 0xFF) / 255f)
    }

    override fun runInference() {
        tflite.run(imgData, labelProbArray)
    }
}
