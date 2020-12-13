package pl.wardrobes.gcode.image

import pl.wardrobes.gcode.Point
import java.io.File

interface ImageReader {

    fun read(imageFile: File): List<Point>
}