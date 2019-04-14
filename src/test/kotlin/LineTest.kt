package render

import render.shapes.Line
import org.junit.Test
import render.base.Color4F
import render.shapes.createLine

class LineTest {

    val line = createLine(0.0, 0.0, 0.0, 0.5, 0.1, 1.0, Color4F(1.0f,1.0f,1.0f,1.0f))

    @Test
    fun flatten() {
        val vertexes = line.flatten()
        assert(vertexes.size == 18*3)
    }

}