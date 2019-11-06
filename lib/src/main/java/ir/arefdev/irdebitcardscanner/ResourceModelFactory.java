package ir.arefdev.irdebitcardscanner;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

class ResourceModelFactory {

	private static ResourceModelFactory instance;

	static ResourceModelFactory getInstance() {
		if (instance == null) {
			instance = new ResourceModelFactory();
		}

		return instance;
	}

	MappedByteBuffer loadFindFourFile(Context context) throws IOException {
		return loadModelFromResource(context, R.raw.findfour);
	}

	MappedByteBuffer loadRecognizeDigitsFile(Context context) throws IOException {
		return loadModelFromResource(context, R.raw.fourrecognize);
	}

	private MappedByteBuffer loadModelFromResource(Context context, int resource) throws IOException {
		AssetFileDescriptor fileDescriptor = context.getResources().openRawResourceFd(resource);
		FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
		FileChannel fileChannel = inputStream.getChannel();
		long startOffset = fileDescriptor.getStartOffset();
		long declaredLength = fileDescriptor.getDeclaredLength();
		MappedByteBuffer result = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
		inputStream.close();
		fileDescriptor.close();
		return result;
	}
}
