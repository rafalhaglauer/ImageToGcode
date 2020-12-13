package pl.wardrobes.gcode.writer

import pl.wardrobes.gcode.Point

object GCodePathWriter : PathWriter {

    override fun buildString(path: List<Point>): String = buildString {
        val firstPoint = path.first()
        appendln("G0 X${firstPoint.xValue} Y${firstPoint.yValue} Z50")
        appendln("G0 X${firstPoint.xValue} Y${firstPoint.yValue} Z20")
        appendln(path.joinToString(separator = "\n") { "G1 X${it.xValue} Y${it.yValue} Z14" })
        append("G0 Z20")
    }
}