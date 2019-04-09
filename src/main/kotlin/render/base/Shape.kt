package render.base

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class Point3D(val x: Double, val y : Double, val z : Double){
    fun flatten() : List<Double> = listOf(x, y, z)
}
class Point2D(val x : Double, val y : Double)
class Color4F(val r: Float = 0.0f, val g: Float = 0.0f, val b: Float = 0.0f, val a: Float = 1.0f)

abstract class Shape : ShapeInterface {

    protected var origin : Point3D = Point3D(0.0, 0.0, 0.0)
    protected var shift  : Point3D = Point3D(0.0, 0.0, 0.0)
    protected var color    = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
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
    override fun colorBuffer(): FloatArray = color
    override fun flatten() : List<Double> {
        return points.toList().map{p->p.flatten()}.flatten()
    }
    override fun bufferSizeFloat() : Int = points.size*3
    override fun vertexCount() : Int = points.size
    override fun setColor(r: Float, g: Float, b: Float, a: Float){
        color = floatArrayOf(r, g, b, a)
    }
    override fun setColor(_color : Color4F){
        color = floatArrayOf(_color.r, _color.g, _color.b, _color.a)
    }
}



