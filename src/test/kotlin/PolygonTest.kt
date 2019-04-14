package render

import render.shapes.Polygon
import org.junit.Test

class PolygonTest {

    val polygon = Polygon()

    @Test
    fun flatten() {
        val vertexes = polygon.flatten()
        assert(vertexes.size == 9)
    }

}