package ir.arefdev.irdebitcardscanner

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import android.graphics.Bitmap
import android.util.Log

/**
 * This class is not thread safe, make sure that all methods run on the same thread.
 */
internal class OCR {

    companion object {
        private var findFour: FindFourModel? = null
        private var recognizedDigitsModel: RecognizedDigitsModel? = null
        private const val USE_GPU = false

        fun isInit(): Boolean = findFour != null && recognizedDigitsModel != null
    }

    var digitBoxes: List<DetectedBox> = ArrayList()
    var expiryBox: DetectedBox? = null
    var expiry: Expiry? = null
    var hadUnrecoverableException = false

    private fun detectBoxes(image: Bitmap): ArrayList<DetectedBox> {
        val boxes = ArrayList<DetectedBox>()
        val ff = findFour!!
        for (row in 0 until ff.rows) {
            for (col in 0 until ff.cols) {
                if (ff.hasDigits(row, col)) {
                    val confidence = ff.digitConfidence(row, col)
                    val imageSize = CGSize(image.width.toFloat(), image.height.toFloat())
                    val box = DetectedBox(row, col, confidence, ff.rows, ff.cols, ff.boxSize, ff.cardSize, imageSize)
                    boxes.add(box)
                }
            }
        }
        return boxes
    }

    private fun detectExpiry(image: Bitmap): ArrayList<DetectedBox> {
        val boxes = ArrayList<DetectedBox>()
        val ff = findFour!!
        for (row in 0 until ff.rows) {
            for (col in 0 until ff.cols) {
                if (ff.hasExpiry(row, col)) {
                    val confidence = ff.expiryConfidence(row, col)
                    val imageSize = CGSize(image.width.toFloat(), image.height.toFloat())
                    val box = DetectedBox(row, col, confidence, ff.rows, ff.cols, ff.boxSize, ff.cardSize, imageSize)
                    boxes.add(box)
                }
            }
        }
        return boxes
    }

    private fun runModel(image: Bitmap): String? {
        val ff = findFour!!
        val rdm = recognizedDigitsModel!!
        ff.classifyFrame(image)
        var boxes = detectBoxes(image)
        val expiryBoxes = detectExpiry(image)
        val postDetection = PostDetectionAlgorithm(boxes, ff)

        val recognizeNumbers = RecognizeNumbers(image, ff.rows, ff.cols)
        val lines = postDetection.horizontalNumbers()
        var number = recognizeNumbers.number(rdm, lines)

        if (number == null) {
            val verticalLines = postDetection.verticalNumbers()
            number = recognizeNumbers.number(rdm, verticalLines)
            lines.addAll(verticalLines)
        }

        boxes = ArrayList()
        for (numbers in lines) {
            boxes.addAll(numbers)
        }

        this.expiry = null
        if (expiryBoxes.isNotEmpty()) {
            expiryBoxes.sort()
            val expiryBox = expiryBoxes[expiryBoxes.size - 1]
            this.expiry = Expiry.from(rdm, image, expiryBox.getRect())
            if (this.expiry != null) {
                this.expiryBox = expiryBox
            } else {
                this.expiryBox = null
            }
        }

        this.digitBoxes = boxes

        return number
    }

    private fun hasOpenGl31(context: Context): Boolean {
        val openGlVersion = 0x00030001
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configInfo: ConfigurationInfo = activityManager.deviceConfigurationInfo
        return if (configInfo.reqGlEsVersion != ConfigurationInfo.GL_ES_VERSION_UNDEFINED) {
            configInfo.reqGlEsVersion >= openGlVersion
        } else {
            false
        }
    }

    @Synchronized
    fun predict(image: Bitmap, context: Context): String? {
        val NUM_THREADS = 4
        try {
            var createdNewModel = false

            if (findFour == null) {
                findFour = FindFourModel(context)
                findFour!!.setNumThreads(NUM_THREADS)
                createdNewModel = true
            }

            if (recognizedDigitsModel == null) {
                recognizedDigitsModel = RecognizedDigitsModel(context)
                recognizedDigitsModel!!.setNumThreads(NUM_THREADS)
                createdNewModel = true
            }

            if (createdNewModel && hasOpenGl31(context) && USE_GPU) {
                try {
                    findFour!!.useGpu()
                    recognizedDigitsModel!!.useGpu()
                } catch (e: Throwable) {
                    Log.i("Ocr", "useGpu exception, falling back to CPU", e)
                    findFour = FindFourModel(context)
                    recognizedDigitsModel = RecognizedDigitsModel(context)
                }
            }

            return try {
                runModel(image)
            } catch (e: Throwable) {
                Log.i("Ocr", "runModel exception, retry prediction", e)
                findFour = FindFourModel(context)
                recognizedDigitsModel = RecognizedDigitsModel(context)
                runModel(image)
            }
        } catch (e: Throwable) {
            Log.e("Ocr", "unrecoverable exception on Ocr", e)
            hadUnrecoverableException = true
            return null
        }
    }
}
