/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package ir.arefdev.irdebitcardscanner

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer

/**
 * Classifies images with Tensorflow Lite.
 */
@Suppress("LeakingThis")
abstract class ImageClassifier @Throws(IOException::class) constructor(context: Context) {

    companion object {
        private const val TAG = "CardScan"
        private const val DIM_BATCH_SIZE = 1
        private const val DIM_PIXEL_SIZE = 3
    }

    private val intValues = IntArray(getImageSizeX() * getImageSizeY())
    private val tfliteOptions = Interpreter.Options()
    private var tfliteModel: MappedByteBuffer? = null
    protected lateinit var tflite: Interpreter
    protected var imgData: ByteBuffer? = null

    init {
        initModel(context)
    }

    @Throws(IOException::class)
    private fun initModel(context: Context) {
        tfliteModel = loadModelFile(context)
        tflite = Interpreter(tfliteModel!!, tfliteOptions)
        imgData = ByteBuffer.allocateDirect(
            DIM_BATCH_SIZE
                    * getImageSizeX()
                    * getImageSizeY()
                    * DIM_PIXEL_SIZE
                    * getNumBytesPerChannel()
        )
        imgData!!.order(ByteOrder.nativeOrder())
    }

    fun classifyFrame(bitmap: Bitmap) {
        if (!::tflite.isInitialized) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.")
        }
        convertBitmapToByteBuffer(bitmap)
        runInference()
    }

    private fun recreateInterpreter() {
        if (::tflite.isInitialized) {
            tflite.close()
            tflite = Interpreter(tfliteModel!!, tfliteOptions)
        }
    }

    fun useCPU() {
        tfliteOptions.setUseNNAPI(false)
        recreateInterpreter()
    }

    fun useGpu() {
        // GPU delegate disabled
    }

    fun useNNAPI() {
        tfliteOptions.setUseNNAPI(true)
        recreateInterpreter()
    }

    fun setNumThreads(numThreads: Int) {
        tfliteOptions.setNumThreads(numThreads)
        recreateInterpreter()
    }

    fun close() {
        tflite.close()
        tfliteModel = null
    }

    @Throws(IOException::class)
    abstract fun loadModelFile(context: Context): MappedByteBuffer

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        if (imgData == null) return
        imgData!!.rewind()

        val resizedBitmap = bitmap.scale(getImageSizeX(), getImageSizeY(), false)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        var pixel = 0
        for (i in 0 until getImageSizeX()) {
            for (j in 0 until getImageSizeY()) {
                val v = intValues[pixel++]
                addPixelValue(v)
            }
        }
    }

    protected abstract fun getImageSizeX(): Int

    protected abstract fun getImageSizeY(): Int

    protected abstract fun getNumBytesPerChannel(): Int

    protected abstract fun addPixelValue(pixelValue: Int)

    protected abstract fun runInference()
}
