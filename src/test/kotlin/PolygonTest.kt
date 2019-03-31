package com.example.stnik.render

import com.example.stnik.render.base.Point3D
import com.example.stnik.render.shapes.Circle
import com.example.stnik.render.shapes.Polygon
import org.junit.Test

class PolygonTest {

    val polygon = Polygon()

    @Test
    fun flatten() {
        val vertexes = polygon.flatten()
        assert(vertexes.size == 9)
    }

}