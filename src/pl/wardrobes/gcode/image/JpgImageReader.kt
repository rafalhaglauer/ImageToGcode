package pl.wardrobes.gcode.image

import pl.wardrobes.gcode.Point
import java.io.File
import javax.imageio.ImageIO

private const val BLACK_RGB_VALUE = -16777216

object JpgImageReader : ImageReader {

    override fun read(imageFile: File): List<Point> = mutableListOf<Point>().apply {
        ImageIO.read(imageFile).run {
            (0 until width).forEach { horizontalIndex ->
                (0 until height).forEach { verticalIndex ->
                    val rgbValue = getRGB(horizontalIndex, verticalIndex)
                    if (rgbValue == BLACK_RGB_VALUE) {
                        add(
                            Point(
                                xValue = horizontalIndex.toFloat(),
                                yValue = verticalIndex.toFloat()
                            )
                        )
                    }
                }
            }
        }
    }
}