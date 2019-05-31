package render.base

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class Point3D(val x: Double, val y : Double, val z : Double){
    fun flatten() : List<Double> = listOf(x, y, z)
}
class Point2D(val x : Double, val y : Double)
class Color4F(val r: Float = 0.0f, val g: Float = 0.0f, val b: Float = 0.0f, val a: Float = 1.0f){
    fun toArray() = floatArrayOf(r, g, b, a)
}
class Vector2(b : Point3D, e : Point3D){
    val x = e.x - b.x
    val y = e.y - b.y
}

fun dot(v1 : Vector2, v2 : Vector2) : Double{
    return v1.x*v2.x+v1.y*v2.y
}

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

abstract class Shape(protected var color4f : Color4F = Color4F(0.63671875f, 0.76953125f, 0.22265625f, 1.0f),
                     private var colorShadow4f: Color4F = Color4F(0.0f,0.0f,0.0f,1.0f)
): ShapeInterface {
    protected var shift  : Point3D = Point3D(0.0, 0.0, 0.0)

    protected var layer  = 0.0

    private lateinit var vertexBuffer : FloatBuffer
    protected abstract val points : Array<Point3D>

    fun doInit(){
        vertexBuffer  = // (number of coordinate values * 4 bytes per float)
                ByteBuffer.allocateDirect(this.bufferSizeFloat() * 4).run {
                    // use the device hardware's native byte order
                    order(ByteOrder.nativeOrder())
                    // create a floating point buffer from the ByteBuffer
                    asFloatBuffer().apply {
                        // add the coordinates to the FloatBuffer
                        flatten().forEach { p -> put(p.toFloat()) }
                        // set the buffer to read the first coordinate
                        position(0)
                    }
                }
    }
    override fun vertexBuffer() : FloatBuffer = vertexBuffer
    override fun shift() : Point3D = shift
    override fun move(pos : Point3D){shift = pos}
    override fun rotate(deg : Double){}
    override fun colorBuffer(): FloatArray = color4f.toArray()
    override fun colorShadowBuffer(): FloatArray = colorShadow4f.toArray()
    override fun flatten() : List<Double> {
        return points.toList().map{p->p.flatten()}.flatten()
    }
    override fun bufferSizeFloat() : Int = points.size*3
    override fun vertexCount() : Int = points.size
    override fun setColor(r: Float, g: Float, b: Float, a: Float){
        color4f = Color4F(r, g, b, a)
    }
    override fun setColor(_color : Color4F){
        color4f = _color
    }

}



