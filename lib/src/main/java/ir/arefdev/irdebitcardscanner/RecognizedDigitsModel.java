package ir.arefdev.irdebitcardscanner;

import android.content.Context;

import java.io.IOException;
import java.nio.MappedByteBuffer;

class RecognizedDigitsModel extends ImageClassifier {

	static final int kNumPredictions = 17;
	private final int classes = 11;

	/**
	 * An array to hold inference results, to be feed into Tensorflow Lite as outputs. This isn't part
	 * of the super class, because we need a primitive array here.
	 */
	private float[][][][] labelProbArray;

	RecognizedDigitsModel(Context context) throws IOException {
		super(context);
		labelProbArray = new float[1][1][kNumPredictions][classes];
	}

	class ArgMaxAndConfidence {
		final int argMax;
		final float confidence;

		ArgMaxAndConfidence(int argMax, float confidence) {
			this.argMax = argMax;
			this.confidence = confidence;
		}
	}

	ArgMaxAndConfidence argAndValueMax(int col) {
		int maxIdx = -1;
		float maxValue = (float) -1.0;
		for (int idx = 0; idx < classes; idx++) {
			float value = this.labelProbArray[0][0][col][idx];
			if (value > maxValue) {
				maxIdx = idx;
				maxValue = value;
			}
		}

		return new ArgMaxAndConfidence(maxIdx, maxValue);
	}

	@Override
	MappedByteBuffer loadModelFile(Context context) throws IOException {
		return ResourceModelFactory.getInstance().loadRecognizeDigitsFile(context);
	}

	@Override
	protected int getImageSizeX() {
		return 80;
	}

	@Override
	protected int getImageSizeY() {
		return 36;
	}

	@Override
	protected int getNumBytesPerChannel() {
		return 4;
	}

	@Override
	protected void addPixelValue(int pixelValue) {
		imgData.putFloat(((pixelValue >> 16) & 0xFF) / 255.f);
		imgData.putFloat(((pixelValue >> 8) & 0xFF) / 255.f);
		imgData.putFloat((pixelValue & 0xFF) / 255.f);
	}

	@Override
	protected void runInference() {
		tflite.run(imgData, labelProbArray);
	}
}
