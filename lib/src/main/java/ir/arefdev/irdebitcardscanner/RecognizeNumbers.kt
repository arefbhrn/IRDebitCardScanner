package ir.arefdev.irdebitcardscanner;

import android.graphics.Bitmap;

import java.util.ArrayList;

class RecognizeNumbers {

	private RecognizedDigits[][] recognizedDigits;
	private final Bitmap image;

	RecognizeNumbers(Bitmap image, int numRows, int numCols) {
		this.image = image;
		this.recognizedDigits = new RecognizedDigits[numRows][numCols];
	}

	String number(RecognizedDigitsModel model, ArrayList<ArrayList<DetectedBox>> lines) {
		for (ArrayList<DetectedBox> line : lines) {
			StringBuilder candidateNumber = new StringBuilder();

			for (DetectedBox word : line) {
				RecognizedDigits recognized = this.cachedDigits(model, word);
				if (recognized == null) {
					return null;
				}

				candidateNumber.append(recognized.stringResult());
			}

			if (candidateNumber.length() == 16 && DebitCardUtils.luhnCheck(candidateNumber.toString())) {
				return candidateNumber.toString();
			}
		}

		return null;
	}

	private RecognizedDigits cachedDigits(RecognizedDigitsModel model, DetectedBox box) {
		if (this.recognizedDigits[box.row][box.col] == null) {
			this.recognizedDigits[box.row][box.col] = RecognizedDigits.from(model, image, box.getRect());
		}

		return this.recognizedDigits[box.row][box.col];
	}

}
