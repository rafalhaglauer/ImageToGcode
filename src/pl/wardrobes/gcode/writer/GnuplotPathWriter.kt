package pl.wardrobes.gcode.writer

import pl.wardrobes.gcode.Point

object GnuplotPathWriter : PathWriter {

    override fun buildString(path: List<Point>): String = path.joinToString(separator = "\n") {
        "${formatPoint(it.xValue)} -${formatPoint(it.yValue)}"
    }
}