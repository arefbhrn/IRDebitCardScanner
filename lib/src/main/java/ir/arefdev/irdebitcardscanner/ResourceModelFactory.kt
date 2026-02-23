package ir.arefdev.irdebitcardscanner

import android.content.Context
import android.content.res.AssetFileDescriptor
import com.arefbhrn.irdebitcardscanner.R
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

internal object ResourceModelFactory {

    @Throws(IOException::class)
    fun loadFindFourFile(context: Context): MappedByteBuffer =
        loadModelFromResource(context, R.raw.findfour)

    @Throws(IOException::class)
    fun loadRecognizeDigitsFile(context: Context): MappedByteBuffer =
        loadModelFromResource(context, R.raw.fourrecognize)

    @Throws(IOException::class)
    private fun loadModelFromResource(context: Context, resource: Int): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.resources.openRawResourceFd(resource)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val result = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        inputStream.close()
        fileDescriptor.close()
        return result
    }
}
