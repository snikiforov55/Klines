package render.base

import kotlin.math.sqrt

data class Point3D(val x: Double, val y : Double, val z : Double){
    fun flatten() : List<Double> = listOf(x, y, z)
}
data class Point2D(val x : Double, val y : Double)
data class Color4F(val r: Float = 0.0f, val g: Float = 0.0f, val b: Float = 0.0f, val a: Float = 1.0f){
    fun toArray() = floatArrayOf(r, g, b, a)
}

class Vector2(val x : Double, val y : Double){
    companion object{
        fun of(b : Point3D, e : Point3D) : Vector2 = Vector2(x = e.x - b.x, y = e.y - b.y)
    }
}


fun dot(v1 : Vector2, v2 : Vector2) : Double{
    return v1.x*v2.x+v1.y*v2.y
}
fun length2D(startX: Double, startY: Double, endX : Double, endY : Double) : Double =
    sqrt( (endX - startX)*(endX - startX) + (endY - startY)*(endY - startY))


