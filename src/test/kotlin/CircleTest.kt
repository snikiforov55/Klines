package render

import render.shapes.Circle
import render.base.Point3D
import org.junit.Test
import render.base.ShapeWrapper

class CircleTest {

    val circle = ShapeWrapper(Circle(origin = Point3D(0.0,0.0,0.0), radius = 0.2, thickness = 0.05))

    @Test
    fun move() {
        circle.move(Point3D(-1.0, 0.5, 1.0))
        val s = circle.shift()
        assert(s.x == -1.0)
        assert(s.y ==  0.5)
        assert(s.z ==  1.0)
    }

    @Test
    fun rotate() {
    }

    @Test
    fun bufferSizeFloat() {
        assert(circle.bufferSizeFloat() == circle.vertexCount()*3)
    }

    @Test
    fun flatten() {
        assert(circle.flatten().size == circle.vertexCount()*3)
    }

    @Test
    fun vertexBuffer() {
        val rem = circle.vertexBuffer().remaining()
        assert( rem == (circle.bufferSizeFloat()))
    }

    @Test
    fun vertexCount() {
        assert(circle.vertexCount() == 6)
    }
}