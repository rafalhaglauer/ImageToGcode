package pl.wardrobes.gcode.writer

import pl.wardrobes.gcode.Point

interface PathWriter {

    fun buildString(path: List<Point>): String
}