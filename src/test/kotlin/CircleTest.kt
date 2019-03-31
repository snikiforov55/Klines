package com.example.stnik.render

import com.example.stnik.render.base.Point3D
import com.example.stnik.render.shapes.Circle
import org.junit.Test

class CircleTest {

    val circle = Circle(0.2, 0.05)

    @Test
    fun move() {
        circle.move(Point3D(-1.0, 0.5, 1.0))
        val s = circle.shift()
        assert(s.x == -1.0)
        assert(s.y ==  0.5)
        assert(s.y ==  1.0)
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
        assert(circle.vertexBuffer().remaining() == (circle.bufferSizeFloat() * 4))
    }

    @Test
    fun vertexCount() {
        assert(circle.vertexCount() == 9)
    }
}