package ir.arefdev.irdebitcardscanner

class DetectedBox(
    val row: Int,
    val col: Int,
    private val confidence: Float,
    numRows: Int,
    numCols: Int,
    boxSize: CGSize,
    cardSize: CGSize,
    imageSize: CGSize
) : Comparable<DetectedBox> {

    private val rect: CGRect

    init {
        // Resize the box to transform it from the model's coordinates into
        // the image's coordinates
        val w = boxSize.width * imageSize.width / cardSize.width
        val h = boxSize.height * imageSize.height / cardSize.height
        val x = (imageSize.width - w) / (numCols - 1).toFloat() * col.toFloat()
        val y = (imageSize.height - h) / (numRows - 1).toFloat() * row.toFloat()
        rect = CGRect(x, y, w, h)
    }

    override fun compareTo(other: DetectedBox): Int = confidence.compareTo(other.confidence)

    fun getRect(): CGRect = rect
}
