package pl.wardrobes.gcode

import java.io.File
import javax.imageio.ImageIO

fun main() {
    Converter().test()
}

const val BLACK_RGB_VALUE = -16777216

//const val OFFSET_X = 140
const val OFFSET_X = 139
const val OFFSET_Y = -7

const val DRILLING_SIZE = 5f

class Converter {

    fun test() {

        val code = buildString {
            appendln("%")
            appendln("S1")
            appendln("M3")
            appendln("F140")
            (1..5).forEach { fileName ->
                val imageFile = File("C:\\Users\\Rafal\\Desktop\\lapa\\1$fileName.jpg")

                val points = mutableListOf<Point>()

                ImageIO.read(imageFile).run {
                    (0 until width).forEach { horizontalIndex ->
                        (0 until height).forEach { verticalIndex ->
                            val rgbValue = getRGB(horizontalIndex, verticalIndex)
                            if (rgbValue == BLACK_RGB_VALUE) {
                                points.add(
                                    Point(
                                        xValue = (horizontalIndex / 10F) + OFFSET_X,
                                        yValue = (verticalIndex / 10F) + OFFSET_Y
                                    )
                                )
                            }
                        }
                    }
                }

                val path = createPath(points)
                val firstPoint = path.first()
                appendln("G0 Z20")
                appendln("G0 X${firstPoint.xValue} Y${firstPoint.yValue} Z20")
                path.forEach {
                    println("${it.xValue} -${it.yValue}")
                    appendln("G1 X${it.xValue} Y${it.yValue} Z14")
                }
                println("FileName: $fileName")
                appendln("G0 Z20")
            }
            appendln("M2")
            appendln("%")
        }

        val codeFile = File("C:\\Users\\Rafal\\Desktop\\lapa\\lapa_nowy_algorytm_2.ngc")
        codeFile.writeText(code)
    }
}

data class Point(
    val xValue: Float,
    val yValue: Float
)

private fun createPath(restOfPoints: List<Point>, startPoint: Point? = null): List<Point> {
    val modifiedPoints = mutableListOf<Point>()

    val xValues = restOfPoints.map { it.xValue }.distinct()

    val xMin = xValues.min()
    val xMax = xValues.max()

    xValues.forEach { xValue ->
        val yValuesForX = restOfPoints.filter { it.xValue == xValue }.map { it.yValue }

        if (xValue == xMin || xValue == xMax) {
            modifiedPoints.addAll(yValuesForX.map {
                Point(xValue = xValue, yValue = it)
            })
        } else {
            val min = requireNotNull(yValuesForX.min())
            val max = requireNotNull(yValuesForX.max())
            modifiedPoints.add(Point(xValue = xValue, yValue = min))
            modifiedPoints.add(Point(xValue = xValue, yValue = max))
        }
    }

    val closedPath = getClosedPathOf(modifiedPoints, startPoint ?: modifiedPoints.findBottomMostPoint())

    val firstPointOfClosedPath = closedPath.first()

    val nextPoints = restOfPoints.toMutableList()
    closedPath.forEach { currentPoint -> nextPoints.removeAll { dist(it, currentPoint) < (DRILLING_SIZE / 2) } }

    return if (nextPoints.isEmpty()) {
        closedPath.plus(firstPointOfClosedPath)
    } else {
        closedPath.plus(firstPointOfClosedPath) + createPath(
            nextPoints,
            startPoint = requireNotNull(nextPoints.minBy { dist(firstPointOfClosedPath, it) })
        )
    }
}

private fun dist(p1: Point, p2: Point): Float {
    return (p1.xValue - p2.xValue) * (p1.xValue - p2.xValue) + (p1.yValue - p2.yValue) * (p1.yValue - p2.yValue)
}

fun getClosedPathOf(points: List<Point>, startPoint: Point): List<Point> {
    val leftPoints = points.toMutableList()
    val path = mutableListOf<Point>()

    leftPoints.remove(startPoint)
    path.add(startPoint)

    while (leftPoints.isNotEmpty()) {
        val lastPoint = path.last()
        val closestPoint = requireNotNull(leftPoints.minBy { currentPoint -> dist(lastPoint, currentPoint) })
        leftPoints.remove(closestPoint)
        if (dist(lastPoint, closestPoint) < 5 || !path.any { dist(it, closestPoint) < (DRILLING_SIZE / 2) }) {
            path.add(closestPoint)
        }
    }
    return path
}

fun List<Point>.findBottomMostPoint(): Point {
    val yMin = map { it.yValue }.min()
    return requireNotNull(filter { it.yValue == yMin }.minBy { it.xValue })
}