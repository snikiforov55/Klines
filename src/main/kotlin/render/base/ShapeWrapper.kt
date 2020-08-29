package render.base

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer



interface ShapeWrapperInterface {
    fun move(pos : Point3D)
    fun rotate(deg : Double)
    fun bufferSizeFloat() : Int
    fun flatten() : List<Double>
    fun shift() : Point3D
    fun vertexBuffer() : FloatBuffer
    fun vertexCount() : Int
    fun colorBuffer() : FloatArray
    fun colorShadowBuffer(): FloatArray
    fun setColor(r: Float, g: Float, b: Float, a: Float)
    fun setColor(_color : Color4F)
}


class ShapeWrapper<S:ShapeInterface>(val shape: S,
                                     var shift  : Point3D = Point3D(0.0, 0.0, 0.0),
                                     var color4f : Color4F = Color4F(0.63671875f, 0.76953125f, 0.22265625f, 1.0f),
                            private var colorShadow4f: Color4F = Color4F(0.0f,0.0f,0.0f,1.0f)
): ShapeWrapperInterface {

    private var vertexBuffer : FloatBuffer
    init {
        val capacity = shape.points().size * 3 * 4
        // (number of coordinate values * 4 bytes per float)
        vertexBuffer = ByteBuffer.allocateDirect(capacity).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                shape.points().forEach { p -> p.flatten().forEach { p -> put(p.toFloat()) } }
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
        return shape.points().map{p-> p.flatten()}.flatten()
    }
    override fun bufferSizeFloat() : Int = shape.points().size*3
    override fun vertexCount() : Int = shape.points().size
    override fun setColor(r: Float, g: Float, b: Float, a: Float){
        color4f = Color4F(r, g, b, a)
    }
    override fun setColor(_color : Color4F){
        color4f = _color
    }

}



