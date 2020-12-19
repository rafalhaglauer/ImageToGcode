package pl.wardrobes.gcode

import pl.wardrobes.gcode.image.JpgImageReader
import pl.wardrobes.gcode.writer.GCodePathWriter
import pl.wardrobes.gcode.writer.GnuplotPathWriter
import java.io.File
import java.util.*

fun main() {
    Converter().test()
}

//const val OFFSET_X = 140
//const val OFFSET_Y = -7
//const val OFFSET_X = 20
//const val OFFSET_Y = 20
const val OFFSET_X = 0
const val OFFSET_Y = 0

const val SCALE = 0.05F

const val DRILLING_SIZE = 5f / SCALE

const val FILE_NAME = "choinka"

class Converter {

    fun test() {
        val currentTime = Date()
        val code = buildString {
            appendln("%")
            appendln("S1")
            appendln("M3")
            appendln("F140")
            val imageFile = File("C:\\Users\\Rafal\\Desktop\\ozdoby\\$FILE_NAME.bmp")
            val points = JpgImageReader.read(imageFile)
            val pointsGroups = points.group()
            val firstPoint = pointsGroups.first().first()
            appendln("G0 X${firstPoint.xValue * SCALE + OFFSET_X} Y${firstPoint.yValue * SCALE + OFFSET_Y} Z50")
            pointsGroups.forEach { currentGroup ->
                val path = createPath(currentGroup).map {
                    Point(
                        xValue = ((it.xValue * SCALE + OFFSET_X) * 10).toInt() / 10f,
                        yValue = ((it.yValue * SCALE + OFFSET_Y)  * 10).toInt() / 10f
                    )
                }.distinct()
                appendln(GCodePathWriter.buildString(path))
                println(GnuplotPathWriter.buildString(path))
            }
            println("pointsGroups.size: ${pointsGroups.size}")
            val allPoints = pointsGroups.flatten()
            val xValues = allPoints.map { it.xValue * SCALE + OFFSET_X }
            val yValues = allPoints.map { it.yValue * SCALE + OFFSET_Y }
            val xMax = requireNotNull(xValues.max())
            val xMin = requireNotNull(xValues.min())
            val yMax = requireNotNull(yValues.max())
            val yMin = requireNotNull(yValues.min())
            println("Min X: $xMin - Max X: $xMax - Width: ${xMax - xMin}")
            println("Min Y: $yMin - Max Y: $yMax - Height: ${yMax - yMin}")
            appendln("M2")
            appendln("%")
        }

        val codeFile = File("C:\\Users\\Rafal\\Desktop\\ozdoby\\$FILE_NAME.ngc")
        codeFile.writeText(code)
        println("Took: ${(Date().time - currentTime.time) / 1000}s")
    }
}

data class Point(
    val xValue: Float,
    val yValue: Float
)

private fun List<Point>.group(): List<List<Point>> {
    val pointsGroups = mutableListOf<MutableList<Point>>()
    val minDistance = 2 // 1^2 + 1^2 -- think about sqrt from this value!
    forEach { currentPoint ->
        val group = pointsGroups.find { currentGroup -> currentGroup.any { dist(it, currentPoint) < minDistance } }
        if (group == null) pointsGroups.add(mutableListOf(currentPoint)) else group.add(currentPoint)
    }
    return pointsGroups.mergeGroups()
}

private fun List<List<Point>>.mergeGroups(): List<List<Point>> {
    val pointsGroups = mutableListOf<MutableList<Point>>()
    val minDistance = 2 // 1^2 + 1^2 -- think about sqrt from this value!
    forEach { currentGroup ->
        val group = pointsGroups.find { group ->
            group.any { point1 -> currentGroup.any { point2 -> dist(point1, point2) < minDistance } }
        }
        if (group == null) pointsGroups.add(currentGroup.toMutableList()) else group.addAll(currentGroup)
    }
    return if (size == pointsGroups.size) pointsGroups else pointsGroups.mergeGroups()
}

private fun createPath(restOfPoints: List<Point>, startPoint: Point? = null): List<Point> {
    val modifiedPoints = mutableListOf<Point>()

    val xValues = restOfPoints.map { it.xValue }.distinct()

    xValues.forEach { xValue ->
        val yValuesForX = restOfPoints.filter { it.xValue == xValue }.map { it.yValue }

        yValuesForX.forEach { yValue ->
            if (
                !restOfPoints.contains(Point(xValue - 1, yValue)) ||
                !restOfPoints.contains(Point(xValue + 1, yValue)) ||
                !restOfPoints.contains(Point(xValue, yValue - 1)) ||
                !restOfPoints.contains(Point(xValue, yValue + 1))
            ) {
                modifiedPoints.add(Point(xValue = xValue, yValue = yValue))
            }
        }
    }

    val closedPath = getClosedPathOf(modifiedPoints, startPoint ?: modifiedPoints.findBottomMostPoint())

    val nextPoints = restOfPoints.toMutableList()
    val halfDrillingSize = DRILLING_SIZE / 2
    val toolPath = halfDrillingSize * halfDrillingSize
    closedPath.forEach { currentPoint -> nextPoints.removeAll { dist(it, currentPoint) < toolPath } }

    return if (nextPoints.isEmpty()) {
        closedPath
    } else {
        closedPath + createPath(
            nextPoints,
            startPoint = requireNotNull(nextPoints.minBy { dist(closedPath.last(), it) })
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
// TODO <= 2 instead of < 2!