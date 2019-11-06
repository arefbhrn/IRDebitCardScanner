package ir.arefdev.irdebitcardscanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Organize the boxes to find possible numbers.
 * <p>
 * After running detection, the post processing algorithm will try to find
 * sequences of boxes that are plausible card numbers. The basic techniques
 * that it uses are non-maximum suppression and depth first search on box
 * sequences to find likely numbers. There are also a number of heuristics
 * for filtering out unlikely sequences.
 */
class PostDetectionAlgorithm {

	private final int kDeltaRowForCombine = 2;
	private final int kDeltaColForCombine = 2;

	private ArrayList<DetectedBox> sortedBoxes;
	private final int numRows;
	private final int numCols;

	private static Comparator<DetectedBox> colCompare = new Comparator<DetectedBox>() {
		@Override
		public int compare(DetectedBox o1, DetectedBox o2) {
			return (o1.col < o2.col) ? -1 : ((o1.col == o2.col) ? 0 : 1);
		}
	};

	private static Comparator<DetectedBox> rowCompare = new Comparator<DetectedBox>() {
		@Override
		public int compare(DetectedBox o1, DetectedBox o2) {
			return (o1.row < o2.row) ? -1 : ((o1.row == o2.row) ? 0 : 1);
		}
	};

	PostDetectionAlgorithm(ArrayList<DetectedBox> boxes, FindFourModel findFour) {
		this.numCols = findFour.cols;
		this.numRows = findFour.rows;

		this.sortedBoxes = new ArrayList<>();
		Collections.sort(boxes);
		Collections.reverse(boxes);
		for (DetectedBox box : boxes) {
			int kMaxBoxesToDetect = 20;
			if (this.sortedBoxes.size() >= kMaxBoxesToDetect) {
				break;
			}
			this.sortedBoxes.add(box);
		}
	}

	ArrayList<ArrayList<DetectedBox>> horizontalNumbers() {
		ArrayList<DetectedBox> boxes = this.combineCloseBoxes(kDeltaRowForCombine,
				kDeltaColForCombine);
		int kNumberWordCount = 4;
		ArrayList<ArrayList<DetectedBox>> lines = this.findHorizontalNumbers(boxes, kNumberWordCount);

		ArrayList<ArrayList<DetectedBox>> linesOut = new ArrayList<>();
		// boxes should be roughly evenly spaced, reject any that aren't
		for (ArrayList<DetectedBox> line : lines) {
			ArrayList<Integer> deltas = new ArrayList<>();
			for (int idx = 0; idx < (line.size() - 1); idx++) {
				deltas.add(line.get(idx + 1).col - line.get(idx).col);
			}

			Collections.sort(deltas);
			int maxDelta = deltas.get(deltas.size() - 1);
			int minDelta = deltas.get(0);

			if ((maxDelta - minDelta) <= 2) {
				linesOut.add(line);
			}
		}

		return linesOut;
	}

	ArrayList<ArrayList<DetectedBox>> verticalNumbers() {
		ArrayList<DetectedBox> boxes = this.combineCloseBoxes(kDeltaRowForCombine,
				kDeltaColForCombine);
		ArrayList<ArrayList<DetectedBox>> lines = this.findVerticalNumbers(boxes);

		ArrayList<ArrayList<DetectedBox>> linesOut = new ArrayList<>();
		// boxes should be roughly evenly spaced, reject any that aren't
		for (ArrayList<DetectedBox> line : lines) {
			ArrayList<Integer> deltas = new ArrayList<>();
			for (int idx = 0; idx < (line.size() - 1); idx++) {
				deltas.add(line.get(idx + 1).row - line.get(idx).row);
			}

			Collections.sort(deltas);
			int maxDelta = deltas.get(deltas.size() - 1);
			int minDelta = deltas.get(0);

			if ((maxDelta - minDelta) <= 2) {
				linesOut.add(line);
			}
		}

		return linesOut;
	}

	private boolean horizontalPredicate(DetectedBox currentWord, DetectedBox nextWord) {
		int kDeltaRowForHorizontalNumbers = 1;
		int deltaRow = kDeltaRowForHorizontalNumbers;
		return nextWord.col > currentWord.col && nextWord.row >= (currentWord.row - deltaRow) &&
				nextWord.row <= (currentWord.row + deltaRow);
	}

	private boolean verticalPredicate(DetectedBox currentWord, DetectedBox nextWord) {
		int kDeltaColForVerticalNumbers = 1;
		int deltaCol = kDeltaColForVerticalNumbers;
		return nextWord.row > currentWord.row && nextWord.col >= (currentWord.col - deltaCol) &&
				nextWord.col <= (currentWord.col + deltaCol);
	}

	private void findNumbers(ArrayList<DetectedBox> currentLine, ArrayList<DetectedBox> words,
							 boolean useHorizontalPredicate, int numberOfBoxes,
							 ArrayList<ArrayList<DetectedBox>> lines) {
		if (currentLine.size() == numberOfBoxes) {
			lines.add(currentLine);
			return;
		}

		if (words.size() == 0) {
			return;
		}

		DetectedBox currentWord = currentLine.get(currentLine.size() - 1);
		if (currentWord == null) {
			return;
		}


		for (int idx = 0; idx < words.size(); idx++) {
			DetectedBox word = words.get(idx);
			if (useHorizontalPredicate && horizontalPredicate(currentWord, word)) {
				ArrayList<DetectedBox> newCurrentLine = new ArrayList<>(currentLine);
				newCurrentLine.add(word);
				findNumbers(newCurrentLine, dropFirst(words, idx + 1), useHorizontalPredicate,
						numberOfBoxes, lines);
			} else if (verticalPredicate(currentWord, word)) {
				ArrayList<DetectedBox> newCurrentLine = new ArrayList<>(currentLine);
				newCurrentLine.add(word);
				findNumbers(newCurrentLine, dropFirst(words, idx + 1), useHorizontalPredicate,
						numberOfBoxes, lines);
			}
		}
	}

	private ArrayList<DetectedBox> dropFirst(ArrayList<DetectedBox> boxes, int n) {
		ArrayList<DetectedBox> result = new ArrayList<>();
		for (int idx = n; idx < boxes.size(); idx++) {
			result.add(boxes.get(idx));
		}
		return result;
	}

	// Note: this is simple but inefficient. Since we're dealing with small
	// lists (eg 20 items) it should be fine
	private ArrayList<ArrayList<DetectedBox>> findHorizontalNumbers(ArrayList<DetectedBox> words,
																	int numberOfBoxes) {
		Collections.sort(words, colCompare);
		ArrayList<ArrayList<DetectedBox>> lines = new ArrayList<>();
		for (int idx = 0; idx < words.size(); idx++) {
			ArrayList<DetectedBox> currentLine = new ArrayList<>();
			currentLine.add(words.get(idx));
			findNumbers(currentLine, dropFirst(words, idx + 1), true,
					numberOfBoxes, lines);
		}

		return lines;
	}

	private ArrayList<ArrayList<DetectedBox>> findVerticalNumbers(ArrayList<DetectedBox> words) {
		int numberOfBoxes = 4;
		Collections.sort(words, rowCompare);
		ArrayList<ArrayList<DetectedBox>> lines = new ArrayList<>();
		for (int idx = 0; idx < words.size(); idx++) {
			ArrayList<DetectedBox> currentLine = new ArrayList<>();
			currentLine.add(words.get(idx));
			findNumbers(currentLine, dropFirst(words, idx + 1), false,
					numberOfBoxes, lines);
		}

		return lines;
	}

	/**
	 * Combine close boxes favoring high confidence boxes.
	 */
	private ArrayList<DetectedBox> combineCloseBoxes(int deltaRow, int deltaCol) {
		boolean[][] cardGrid = new boolean[this.numRows][this.numCols];
		for (int row = 0; row < this.numRows; row++) {
			for (int col = 0; col < this.numCols; col++) {
				cardGrid[row][col] = false;
			}
		}

		for (DetectedBox box : this.sortedBoxes) {
			cardGrid[box.row][box.col] = true;
		}

		// since the boxes are sorted by confidence, go through them in order to
		// result in only high confidence boxes winning. There are corner cases
		// where this will leave extra boxes, but that's ok because we don't
		// need to be perfect here
		for (DetectedBox box : this.sortedBoxes) {
			if (!cardGrid[box.row][box.col]) {
				continue;
			}
			for (int row = (box.row - deltaRow); row <= (box.row + deltaRow); row++) {
				for (int col = (box.col - deltaCol); col <= (box.col + deltaCol); col++) {
					if (row >= 0 && row < this.numRows && col >= 0 && col < this.numCols) {
						cardGrid[row][col] = false;
					}
				}
			}

			// add this box back
			cardGrid[box.row][box.col] = true;
		}

		ArrayList<DetectedBox> combinedBoxes = new ArrayList<>();
		for (DetectedBox box : this.sortedBoxes) {
			if (cardGrid[box.row][box.col]) {
				combinedBoxes.add(box);
			}
		}

		return combinedBoxes;
	}
}
