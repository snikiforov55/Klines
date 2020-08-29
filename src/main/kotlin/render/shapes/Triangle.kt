package render.shapes

import render.base.Point3D
import render.base.Vector2
import render.base.dot

class Triangle{
    val first : Point3D
    val second : Point3D
    val third : Point3D
    constructor(_first : Point3D, _second : Point3D, _third : Point3D){
        first = _first
        second = _second
        third = _third
    }
    constructor(points : Array<Point3D>){
        first = points[0]
        second = points[1]
        third = points[2]
    }
    fun flatten() : Array<Point3D>{
        return arrayOf(first, second, third)
    }
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
        val v0 = Vector2(first,  third)
        val v1 = Vector2(first, second)
        val v2 = Vector2(first,  p)
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
}