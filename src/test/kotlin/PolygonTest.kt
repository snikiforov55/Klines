package render

import arrow.core.getOrElse
import render.shapes.Polygon
import org.junit.Test
import render.base.Color4F
import render.base.Point3D
import render.base.Triangle
import render.shapes.createPolygon

class PolygonTest {

    val polygon = Polygon(arrayOf(Point3D(0.0, 0.0, 0.0),
        Point3D(1.0, 0.0, 0.0),
        Point3D(1.0, 1.0, 0.0   )
    ), Color4F(), 1.0)

    val triangle = Triangle(Point3D(0.0, 2.0, 0.0), Point3D(3.0, 4.0, 0.0), Point3D(2.0, 1.0, 0.0))

    @Test
    fun flatten() {
        val vertexes = polygon.flatten()
        //assert(vertexes.size == 9)
    }
    @Test
    fun simpleTriangle(){
        val polygon = createPolygon(arrayOf(Point3D(0.0, 0.0, 0.0),
            Point3D(1.0, 0.0, 0.0),
            Point3D(1.0, 1.0, 0.0   )
        ), Color4F(), 1.0)
        assert(polygon.nonEmpty())
    }
    @Test
    fun simpleSquare(){
        val polygon = createPolygon(arrayOf(
            Point3D(0.0, 0.0, 0.0),
            Point3D(0.0, 1.0, 0.0),
            Point3D(1.0, 1.0, 0.0),
            Point3D(1.0, 0.0, 0.0)
        ), Color4F(), 1.0)
        assert(polygon.map{p->p.flatten().size == 18}.getOrElse { false })
    }
    @Test
    fun house(){
        val polygon = createPolygon(arrayOf(
            Point3D(0.0, 0.0, 0.0),
            Point3D(0.0, 1.0, 0.0),
            Point3D(1.0, 2.0, 0.0),
            Point3D(2.0, 1.0, 0.0),
            Point3D(2.0, 0.0, 0.0)
        ), Color4F(), 1.0)
        assert(polygon.map{p->p.flatten().size == 27}.getOrElse { false })
    }
    @Test
    fun triangleIn1(){
        assert(triangle.belongs(Point3D(1.0, 2.0, 0.0)))
    }
    @Test
    fun triangleIn2(){
        assert(triangle.belongs(Point3D(2.0, 3.0, 0.0)))
    }
    @Test
    fun triangleOut1(){
        assert(!triangle.belongs(Point3D(0.0, 1.0, 0.0)))
    }
    @Test
    fun triangleOut2(){
        assert(!triangle.belongs(Point3D(0.0, 3.0, 0.0)))
    }
    @Test
    fun triangleOut3(){
        assert(!triangle.belongs(Point3D(-1.0, 1.0, 0.0)))
    }
    @Test
    fun triangleOut4(){
        assert(!triangle.belongs(Point3D(1.0, 3.0, 0.0)))
    }
    @Test
    fun triangleOut5(){
        assert(!triangle.belongs(Point3D(3.0, 2.0, 0.0)))
    }
    @Test
    fun isNotCW(){
        val triangle = Triangle(Point3D(0.0, 0.0, 0.0),
            Point3D(1.0, 1.0, 0.0),
            Point3D(-1.0, 2.0, 0.0))
        assert(!triangle.isInner())
    }
    @Test
    fun isCW(){
        val triangle = Triangle(
            Point3D(1.0, 1.0, 0.0),
            Point3D(-1.0, 2.0, 0.0),
            Point3D(2.0, 2.0, 0.0))
        assert(triangle.isInner())
    }
}