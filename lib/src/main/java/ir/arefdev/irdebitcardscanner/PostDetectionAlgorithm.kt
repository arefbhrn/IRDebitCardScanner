package ir.arefdev.irdebitcardscanner

/**
 * Organize the boxes to find possible numbers.
 *
 * After running detection, the post processing algorithm will try to find
 * sequences of boxes that are plausible card numbers. The basic techniques
 * that it uses are non-maximum suppression and depth first search on box
 * sequences to find likely numbers. There are also a number of heuristics
 * for filtering out unlikely sequences.
 */
internal class PostDetectionAlgorithm(boxes: ArrayList<DetectedBox>, findFour: FindFourModel) {

    private val kDeltaRowForCombine = 2
    private val kDeltaColForCombine = 2

    private val sortedBoxes = ArrayList<DetectedBox>()
    private val numRows: Int = findFour.rows
    private val numCols: Int = findFour.cols

    private val colCompare = Comparator<DetectedBox> { o1, o2 ->
        o1.col.compareTo(o2.col)
    }

    private val rowCompare = Comparator<DetectedBox> { o1, o2 ->
        o1.row.compareTo(o2.row)
    }

    init {
        val sorted = ArrayList(boxes)
        sorted.sort()
        sorted.reverse()
        for (box in sorted) {
            val kMaxBoxesToDetect = 20
            if (this.sortedBoxes.size >= kMaxBoxesToDetect) break
            this.sortedBoxes.add(box)
        }
    }

    fun horizontalNumbers(): ArrayList<ArrayList<DetectedBox>> {
        val boxes = combineCloseBoxes(kDeltaRowForCombine, kDeltaColForCombine)
        val kNumberWordCount = 4
        val lines = findHorizontalNumbers(boxes, kNumberWordCount)

        val linesOut = ArrayList<ArrayList<DetectedBox>>()
        // boxes should be roughly evenly spaced, reject any that aren't
        for (line in lines) {
            val deltas = ArrayList<Int>()
            for (idx in 0 until (line.size - 1)) {
                deltas.add(line[idx + 1].col - line[idx].col)
            }

            deltas.sort()
            val maxDelta = deltas[deltas.size - 1]
            val minDelta = deltas[0]

            if ((maxDelta - minDelta) <= 2) {
                linesOut.add(line)
            }
        }

        return linesOut
    }

    fun verticalNumbers(): ArrayList<ArrayList<DetectedBox>> {
        val boxes = combineCloseBoxes(kDeltaRowForCombine, kDeltaColForCombine)
        val lines = findVerticalNumbers(boxes)

        val linesOut = ArrayList<ArrayList<DetectedBox>>()
        // boxes should be roughly evenly spaced, reject any that aren't
        for (line in lines) {
            val deltas = ArrayList<Int>()
            for (idx in 0 until (line.size - 1)) {
                deltas.add(line[idx + 1].row - line[idx].row)
            }

            deltas.sort()
            val maxDelta = deltas[deltas.size - 1]
            val minDelta = deltas[0]

            if ((maxDelta - minDelta) <= 2) {
                linesOut.add(line)
            }
        }

        return linesOut
    }

    private fun horizontalPredicate(currentWord: DetectedBox, nextWord: DetectedBox): Boolean {
        val kDeltaRowForHorizontalNumbers = 1
        return nextWord.col > currentWord.col &&
                nextWord.row >= (currentWord.row - kDeltaRowForHorizontalNumbers) &&
                nextWord.row <= (currentWord.row + kDeltaRowForHorizontalNumbers)
    }

    private fun verticalPredicate(currentWord: DetectedBox, nextWord: DetectedBox): Boolean {
        val kDeltaColForVerticalNumbers = 1
        return nextWord.row > currentWord.row &&
                nextWord.col >= (currentWord.col - kDeltaColForVerticalNumbers) &&
                nextWord.col <= (currentWord.col + kDeltaColForVerticalNumbers)
    }

    private fun findNumbers(
        currentLine: ArrayList<DetectedBox>,
        words: ArrayList<DetectedBox>,
        useHorizontalPredicate: Boolean,
        numberOfBoxes: Int,
        lines: ArrayList<ArrayList<DetectedBox>>
    ) {
        if (currentLine.size == numberOfBoxes) {
            lines.add(currentLine)
            return
        }

        if (words.isEmpty()) return

        val currentWord = currentLine[currentLine.size - 1]

        for (idx in words.indices) {
            val word = words[idx]
            if (useHorizontalPredicate && horizontalPredicate(currentWord, word)) {
                val newCurrentLine = ArrayList(currentLine)
                newCurrentLine.add(word)
                findNumbers(newCurrentLine, dropFirst(words, idx + 1), useHorizontalPredicate, numberOfBoxes, lines)
            } else if (verticalPredicate(currentWord, word)) {
                val newCurrentLine = ArrayList(currentLine)
                newCurrentLine.add(word)
                findNumbers(newCurrentLine, dropFirst(words, idx + 1), useHorizontalPredicate, numberOfBoxes, lines)
            }
        }
    }

    private fun dropFirst(boxes: ArrayList<DetectedBox>, n: Int): ArrayList<DetectedBox> {
        val result = ArrayList<DetectedBox>()
        for (idx in n until boxes.size) {
            result.add(boxes[idx])
        }
        return result
    }

    // Note: this is simple but inefficient. Since we're dealing with small
    // lists (e.g. 20 items) it should be fine
    private fun findHorizontalNumbers(words: ArrayList<DetectedBox>, numberOfBoxes: Int): ArrayList<ArrayList<DetectedBox>> {
        words.sortWith(colCompare)
        val lines = ArrayList<ArrayList<DetectedBox>>()
        for (idx in words.indices) {
            val currentLine = ArrayList<DetectedBox>()
            currentLine.add(words[idx])
            findNumbers(currentLine, dropFirst(words, idx + 1), true, numberOfBoxes, lines)
        }
        return lines
    }

    private fun findVerticalNumbers(words: ArrayList<DetectedBox>): ArrayList<ArrayList<DetectedBox>> {
        val numberOfBoxes = 4
        words.sortWith(rowCompare)
        val lines = ArrayList<ArrayList<DetectedBox>>()
        for (idx in words.indices) {
            val currentLine = ArrayList<DetectedBox>()
            currentLine.add(words[idx])
            findNumbers(currentLine, dropFirst(words, idx + 1), false, numberOfBoxes, lines)
        }
        return lines
    }

    /**
     * Combine close boxes favoring high confidence boxes.
     */
    private fun combineCloseBoxes(deltaRow: Int, deltaCol: Int): ArrayList<DetectedBox> {
        val cardGrid = Array(numRows) { BooleanArray(numCols) { false } }

        for (box in sortedBoxes) {
            cardGrid[box.row][box.col] = true
        }

        // since the boxes are sorted by confidence, go through them in order to
        // result in only high confidence boxes winning.
        for (box in sortedBoxes) {
            if (!cardGrid[box.row][box.col]) continue
            for (row in (box.row - deltaRow)..(box.row + deltaRow)) {
                for (col in (box.col - deltaCol)..(box.col + deltaCol)) {
                    if (row in 0..<numRows && col >= 0 && col < numCols) {
                        cardGrid[row][col] = false
                    }
                }
            }
            // add this box back
            cardGrid[box.row][box.col] = true
        }

        val combinedBoxes = ArrayList<DetectedBox>()
        for (box in sortedBoxes) {
            if (cardGrid[box.row][box.col]) combinedBoxes.add(box)
        }

        return combinedBoxes
    }
}
