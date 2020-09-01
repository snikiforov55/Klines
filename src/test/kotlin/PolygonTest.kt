package render

import arrow.core.getOrElse
import render.shapes.Polygon
import org.junit.Test
import render.base.Color4F
import render.base.Point3D
import render.base.Vector2
import render.shapes.Triangle


class PolygonTest {

    val polygon = Polygon(listOf(Point3D(0.0, 0.0, 0.0),
            Point3D(1.0, 0.0, 0.0),
            Point3D(1.0, 1.0, 0.0   )
        ), 1)

    val triangle = Triangle(Point3D(0.0, 2.0, 0.0), Point3D(3.0, 4.0, 0.0), Point3D(2.0, 1.0, 0.0))

    @Test
    fun flatten() {
        val vertexes = polygon.points()
        //assert(vertexes.size == 9)
    }
    @Test
    fun simpleTriangle(){
        val polygon = Polygon.figure(Point3D(0.0, 0.0, 1.0),
             arrayOf(Point3D(0.0, 0.0, 0.0),
                Point3D(1.0, 0.0, 0.0),
                Point3D(1.0, 1.0, 0.0   )
            ), Color4F(), 1 )
        assert(polygon.nonEmpty())
    }
    @Test
    fun simpleSquare(){
        val polygon = Polygon.figure(Point3D(0.0, 0.0, 1.0),
             arrayOf(
                Point3D(0.0, 0.0, 0.0),
                Point3D(0.0, 1.0, 0.0),
                Point3D(1.0, 1.0, 0.0),
                Point3D(1.0, 0.0, 0.0)
            ), Color4F(), 1
        )
        assert(polygon.map{p->p.flatten().size == 18}.getOrElse { false })
    }
    @Test
    fun house(){
        val polygon = Polygon.figure(Point3D(0.0, 0.0, 1.0),
             arrayOf(
                Point3D(0.0, 0.0, 0.0),
                Point3D(0.0, 1.0, 0.0),
                Point3D(1.0, 2.0, 0.0),
                Point3D(2.0, 1.0, 0.0),
                Point3D(2.0, 0.0, 0.0)
            ), Color4F(), 1
        )
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
    @Test
    fun zipNext(){
        val points = listOf(Point3D(1.0, 1.0, 0.0),
            Point3D(-1.0, 2.0, 0.0),
            Point3D(2.0, 2.0, 0.0))
        val vect = points.zipWithNext{p1,p2 -> Vector2.of(p1, p2)}
        assert(vect.size == 2)
    }
}