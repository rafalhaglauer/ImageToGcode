package pl.wardrobes.gcode.writer

import pl.wardrobes.gcode.Point
import java.util.*

object GCodePathWriter : PathWriter {

    private const val minDistance = 2 // 1^2 + 1^2 -- think about sqrt from this value!

    override fun buildString(path: List<Point>): String = buildString {
        var lastPoint: Point? = null
        path.forEach { currentPoint ->
            if (lastPoint == null || dist(requireNotNull(lastPoint), currentPoint) > minDistance) {
                appendln("G0 X${formatPoint(currentPoint.xValue)} Y${formatPoint(currentPoint.yValue)} Z10")
            }
            appendln("G1 X${formatPoint(currentPoint.xValue)} Y${formatPoint(currentPoint.yValue)} Z3")
            lastPoint = currentPoint
        }
        append("G0 Z10")
    }

    private fun dist(p1: Point, p2: Point): Float {
        return (p1.xValue - p2.xValue) * (p1.xValue - p2.xValue) + (p1.yValue - p2.yValue) * (p1.yValue - p2.yValue)
    }
}

fun formatPoint(value: Float) = String.format(Locale.US, "%.1f", value)

// TODO analize!