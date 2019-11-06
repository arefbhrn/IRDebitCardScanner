package ir.arefdev.irdebitcardscanner;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is not thread safe, make sure that all methods run on the same thread.
 */
class OCR {

	private static FindFourModel findFour = null;
	private static RecognizedDigitsModel recognizedDigitsModel = null;
	List<DetectedBox> digitBoxes = new ArrayList<>();
	DetectedBox expiryBox = null;
	Expiry expiry = null;
	boolean hadUnrecoverableException = false;
	private static boolean USE_GPU = false;

	static boolean isInit() {
		return findFour != null && recognizedDigitsModel != null;
	}

	private ArrayList<DetectedBox> detectBoxes(Bitmap image) {
		ArrayList<DetectedBox> boxes = new ArrayList<>();
		for (int row = 0; row < findFour.rows; row++) {
			for (int col = 0; col < findFour.cols; col++) {
				if (findFour.hasDigits(row, col)) {
					float confidence = findFour.digitConfidence(row, col);
					CGSize imageSize = new CGSize(image.getWidth(), image.getHeight());
					DetectedBox box = new DetectedBox(row, col, confidence, findFour.rows,
							findFour.cols, findFour.boxSize, findFour.cardSize, imageSize);
					boxes.add(box);
				}
			}
		}
		return boxes;
	}

	private ArrayList<DetectedBox> detectExpiry(Bitmap image) {
		ArrayList<DetectedBox> boxes = new ArrayList<>();
		for (int row = 0; row < findFour.rows; row++) {
			for (int col = 0; col < findFour.cols; col++) {
				if (findFour.hasExpiry(row, col)) {
					float confidence = findFour.expiryConfidence(row, col);
					CGSize imageSize = new CGSize(image.getWidth(), image.getHeight());
					DetectedBox box = new DetectedBox(row, col, confidence, findFour.rows,
							findFour.cols, findFour.boxSize, findFour.cardSize, imageSize);
					boxes.add(box);
				}
			}
		}
		return boxes;
	}

	private String runModel(Bitmap image) {
		findFour.classifyFrame(image);
		ArrayList<DetectedBox> boxes = detectBoxes(image);
		ArrayList<DetectedBox> expiryBoxes = detectExpiry(image);
		PostDetectionAlgorithm postDetection = new PostDetectionAlgorithm(boxes, findFour);

		RecognizeNumbers recognizeNumbers = new RecognizeNumbers(image, findFour.rows, findFour.cols);
		ArrayList<ArrayList<DetectedBox>> lines = postDetection.horizontalNumbers();
		String number = recognizeNumbers.number(recognizedDigitsModel, lines);

		if (number == null) {
			ArrayList<ArrayList<DetectedBox>> verticalLines = postDetection.verticalNumbers();
			number = recognizeNumbers.number(recognizedDigitsModel, verticalLines);
			lines.addAll(verticalLines);
		}

		boxes = new ArrayList<>();
		for (ArrayList<DetectedBox> numbers : lines) {
			boxes.addAll(numbers);
		}

		this.expiry = null;
		if (expiryBoxes.size() > 0) {
			Collections.sort(expiryBoxes);
			DetectedBox expiryBox = expiryBoxes.get(expiryBoxes.size() - 1);
			this.expiry = Expiry.from(recognizedDigitsModel, image, expiryBox.getRect());
			if (this.expiry != null) {
				this.expiryBox = expiryBox;
			} else {
				this.expiryBox = null;
			}
		}

		this.digitBoxes = boxes;

		return number;
	}

	private boolean hasOpenGl31(Context context) {
		int openGlVersion = 0x00030001;
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo configInfo = activityManager.getDeviceConfigurationInfo();
		if (configInfo.reqGlEsVersion != ConfigurationInfo.GL_ES_VERSION_UNDEFINED) {
			return configInfo.reqGlEsVersion >= openGlVersion;
		} else {
			return false;
		}
	}

	synchronized String predict(Bitmap image, Context context) {
		final int NUM_THREADS = 4;
		try {
			boolean createdNewModel = false;

			if (findFour == null) {
				findFour = new FindFourModel(context);
				findFour.setNumThreads(NUM_THREADS);
				createdNewModel = true;
			}

			if (recognizedDigitsModel == null) {
				recognizedDigitsModel = new RecognizedDigitsModel(context);
				recognizedDigitsModel.setNumThreads(NUM_THREADS);
				createdNewModel = true;
			}

			if (createdNewModel && hasOpenGl31(context) && USE_GPU) {
				try {
					findFour.useGpu();
					recognizedDigitsModel.useGpu();
				} catch (Error | Exception e) {
					Log.i("Ocr", "useGpu exception, falling back to CPU", e);
					findFour = new FindFourModel(context);
					recognizedDigitsModel = new RecognizedDigitsModel(context);
				}
			}

			try {
				return runModel(image);
			} catch (Error | Exception e) {
				Log.i("Ocr", "runModel exception, retry prediction", e);
				findFour = new FindFourModel(context);
				recognizedDigitsModel = new RecognizedDigitsModel(context);
				return runModel(image);
			}
		} catch (Error | Exception e) {
			Log.e("Ocr", "unrecoverable exception on Ocr", e);
			hadUnrecoverableException = true;
			return null;
		}
	}
}
