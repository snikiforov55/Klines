package render.base

import java.nio.FloatBuffer

interface ShapeInterface {
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
