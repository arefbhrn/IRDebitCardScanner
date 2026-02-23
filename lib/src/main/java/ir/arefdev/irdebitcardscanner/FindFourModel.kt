/* Copyright 2018 The TensorFlow Authors. All Rights Reserved.

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
import java.io.IOException
import java.nio.MappedByteBuffer

/**
 * This classifier works with the float MobileNet model.
 */
internal class FindFourModel @Throws(IOException::class) constructor(context: Context) : ImageClassifier(context) {

    val rows = 34
    val cols = 51
    val boxSize = CGSize(80f, 36f)
    val cardSize = CGSize(480f, 302f)

    private val labelProbArray = Array(1) { Array(rows) { Array(cols) { FloatArray(3) } } }

    fun hasDigits(row: Int, col: Int): Boolean = digitConfidence(row, col) >= 0.5f

    fun hasExpiry(row: Int, col: Int): Boolean = expiryConfidence(row, col) >= 0.5f

    fun digitConfidence(row: Int, col: Int): Float {
        val digitClass = 1
        return labelProbArray[0][row][col][digitClass]
    }

    fun expiryConfidence(row: Int, col: Int): Float {
        val expiryClass = 2
        return labelProbArray[0][row][col][expiryClass]
    }

    @Throws(IOException::class)
    override fun loadModelFile(context: Context): MappedByteBuffer =
        ResourceModelFactory.loadFindFourFile(context)

    override fun getImageSizeX(): Int = 480

    override fun getImageSizeY(): Int = 302

    override fun getNumBytesPerChannel(): Int = 4 // Float.SIZE / Byte.SIZE

    override fun addPixelValue(pixelValue: Int) {
        imgData!!.putFloat(((pixelValue shr 16) and 0xFF) / 255f)
        imgData!!.putFloat(((pixelValue shr 8) and 0xFF) / 255f)
        imgData!!.putFloat((pixelValue and 0xFF) / 255f)
    }

    override fun runInference() {
        tflite.run(imgData, labelProbArray)
    }
}
