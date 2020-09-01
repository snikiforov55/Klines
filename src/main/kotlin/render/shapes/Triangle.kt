package render.shapes

import arrow.core.None
import arrow.core.Some
import render.base.*

class TriangleRender : RenderBase<Triangle>(){

}

data class Triangle(val first : Point3D,
                   val second : Point3D,
                   val third : Point3D,
                   val layer : Int = 1): Shape(), ShapeInterface{
    companion object{
        fun figure( first : Point3D, second : Point3D, third : Point3D, layer : Int, shift : Point3D, color4f : Color4F) =
            if(length2D(first, second) < 0.001 || length2D(second, third) < 0.001 || length2D(third, first) < 0.001) None
            else Some(Figure(Triangle(first,second, third, layer),shift,color4f))
    }

    private val points : List<Point3D> = listOf(first, second, third)
    fun isInner() : Boolean{
        // compute via wedge product, where
        // U/\V = U.x * V.y - U.y * V.x
        return ((second.x - first.x) * (third.y - second.y) - (second.y - first.y) * (third.x - second.x)) < 0
    }
    fun belongs(p: Point3D) : Boolean{
        // Barycentric Technique
        // http://blackpawn.com/texts/pointinpoly/default.html
        //
        // Compute vectors
        val v0 = Vector2.of(first,  third)
        val v1 = Vector2.of(first, second)
        val v2 = Vector2.of(first,  p)
        // Compute dot products
        val dot00 = dot(v0, v0)
        val dot01 = dot(v0, v1)
        val dot02 = dot(v0, v2)
        val dot11 = dot(v1, v1)
        val dot12 = dot(v1, v2)
        // Compute barycentric coordinates
        val invDenom = 1 / (dot00 * dot11 - dot01 * dot01)
        val u = (dot11 * dot02 - dot01 * dot12) * invDenom
        val v = (dot00 * dot12 - dot01 * dot02) * invDenom
        // Check if point is in triangle
        return (u >= 0) && (v >= 0) && (u + v < 1)
    }
    override fun points(): List<Point3D> = points
}

