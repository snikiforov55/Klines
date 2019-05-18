package render.shapes


import com.jogamp.opengl.GL.GL_CW
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GLES2
import com.jogamp.opengl.math.Matrix4
import render.base.Color4F
import render.base.Point3D
import render.base.RenderBase
import render.base.Shape
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

class LineRender : RenderBase<Line>(){
    /**
     * Vertex Shader
     */
    override val vertexShaderCode ="""
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec2 a_TexCoordinate;
        uniform   vec2 a_RadiusThickness;
        varying   vec2 v_TexCoordinate;
        varying  float v_Radius;
        varying  float v_Thickness;
        void main() {
            v_Radius    = a_RadiusThickness.x;
            v_Thickness = a_RadiusThickness.y;
            v_TexCoordinate = a_TexCoordinate;
            gl_Position = uMVPMatrix * vPosition;
        }
    """
    /**
     * Fragment shader
     *
     */
    override val fragmentShaderCode ="""
                uniform vec4 vColor;
                uniform vec4 vColorShadow;
                uniform int  isShadow;
                varying vec2 v_TexCoordinate;
                varying  float v_Radius;
                varying  float v_Thickness;
                void main() {
                  float radius = 0.125;
                  float distTop    = distance(vec2(0.125, 0.875), v_TexCoordinate);
                  float distBottom = distance(vec2(0.125, 0.125), v_TexCoordinate);
                  float delta = 0.03 + 0.1*(1.0 - smoothstep(0.01, 0.015, v_Thickness));

                  float alphaMainLeft  = smoothstep(0.0, delta, v_TexCoordinate.x);
                  float alphaMainRight = 1.0 - smoothstep(delta, 0.0, 0.25 - v_TexCoordinate.x);
                  float alphaTop       = smoothstep(0.0, delta, radius - distTop);
                  float alphaBottom    = smoothstep(0.0, delta, radius - distBottom);

                  float alpha   = (v_TexCoordinate.x <= 0.125) ? alphaMainLeft  : alphaMainRight;
                  alpha         = (v_TexCoordinate.y >  0.875 ) ? alphaTop       : alpha;
                  alpha         = (v_TexCoordinate.y <  0.125 ) ? alphaBottom    : alpha;

                  if(alpha <= 0.1) discard;
                  gl_FragColor   = (isShadow == 1) ? vColorShadow : vColor;
                  gl_FragColor.a = alpha;
                }
    """

    private val textureCoordinates = arrayOf(
         // Top
         0.0f, 0.875f, // bottom left
         0.0f, 1.000f, // top left
        0.25f, 0.875f, // bottom right
        0.25f, 0.875f, // bottom right
         0.0f, 1.000f, // top left
        0.25f, 1.000f, // top right
         // Main
         0.0f, 0.125f, // bottom left
         0.0f, 0.875f, // top left
        0.25f, 0.125f, // bottom right
        0.25f, 0.125f, // bottom right
         0.0f, 0.875f, // top left
        0.25f, 0.875f, // top right
        // bottom
         0.0f, 0.000f, // bottom left
         0.0f, 0.125f, // top left
        0.25f, 0.000f, // bottom right
        0.25f, 0.000f, // bottom right
         0.0f, 0.125f, // top left
        0.25f, 0.125f  // top right

    )
    private val textureBuffer  = // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(textureCoordinates.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                textureCoordinates.forEach { t -> put(t) }
                // set the buffer to read the first coordinate
                position(0)
            }
        }
    override fun draw(gl : GL2, mvpMatrix: FloatArray, shape : Line, isShadow : Int) {
        // get handle to vertex shader's vPosition member
        gl.glGetAttribLocation(mProgram, "vPosition").also { pos ->

            // Enable a handle to the triangle vertices
            gl.glEnableVertexAttribArray(pos)

            // Prepare the triangle coordinate data
            gl.glVertexAttribPointer(
                pos,
                CoordsPerVertex,
                GL2ES2.GL_FLOAT,
                false,
                vertexStride,
                shape.vertexBuffer()
            )
            gl.glGetAttribLocation(mProgram, "a_TexCoordinate").also { th ->
                // Enable a handle to the triangle vertices
                gl.glEnableVertexAttribArray(th)
                // Prepare the triangle coordinate data
                gl.glVertexAttribPointer(
                    th,
                    2,//CoordsPerVertex,
                    GL2ES2.GL_FLOAT,
                    false,
                    2*4,//vertexStride,
                    textureBuffer
                )
                // get handle to fragment shader's vColor member
                mColorHandle = gl.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                    // Set color for drawing the triangle
                    gl.glUniform4fv(colorHandle, 1, shape.colorBuffer(), 0)
                }
                gl.glGetUniformLocation(mProgram, "vColorShadow").also { colorHandle ->
                    // Set color for drawing the triangle
                    gl.glUniform4fv(colorHandle, 1, shape.colorShadowBuffer(), 0)
                }
                gl.glGetUniformLocation(mProgram, "isShadow").also { shadowHandle ->
                    // Set color for drawing the triangle
                    gl.glUniform1i(shadowHandle,isShadow)
                }
                gl.glGetUniformLocation(mProgram, "a_RadiusThickness").also { r ->
                    gl.glEnableVertexAttribArray(r)
                    gl.glUniform2f(r, shape.r().toFloat(), shape.thickness().toFloat())
                }
                mModelMatrix.loadIdentity()
                mModelMatrix.multMatrix(mvpMatrix)


                val worldMatrix = Matrix4()
                worldMatrix.loadIdentity()
                worldMatrix.translate(shape.shift().x.toFloat(),shape.shift().y.toFloat(),shape.shift().z.toFloat())
                worldMatrix.translate(shape.start().x.toFloat(),shape.start().y.toFloat(),0.0f)
                mModelMatrix.multMatrix(worldMatrix)

                val localMatrixRotate = Matrix4()
                localMatrixRotate.loadIdentity()
                localMatrixRotate.rotate(shape.angleRad().toFloat(), 0.0f, 0.0f, 1.0f)
                val localMatrixTranslate = Matrix4()
                localMatrixTranslate.loadIdentity()
                val localMatrixScale = Matrix4()
                localMatrixScale.loadIdentity()
                if(isShadow === 1){
                    val dx = 0.01f
                    val scaleX = 1.0f + dx/shape.thickness().toFloat()
                    val scaleY = 1.0f + dx/shape.r().toFloat()
                    //localMatrix.translate(-dx/2.0f, -dx/2.0f, 0.0f)
                    localMatrixTranslate.translate(
                        -shape.thickness().toFloat() / 2.0f - dx/2.0f,
                        -shape.thickness().toFloat() / 2.0f - dx/2.0f,
                        0.0f
                    )
                    localMatrixScale.scale(scaleX, scaleY, 1.0f)//(1.2 * shape.thickness()/shape.r()).toFloat(), 1.0f)

                } else {
                    localMatrixTranslate.translate(
                        -shape.thickness().toFloat() / 2.0f,
                        -shape.thickness().toFloat() / 2.0f,
                        0.0f
                    )
                }
                mModelMatrix.multMatrix(localMatrixRotate)
                mModelMatrix.multMatrix(localMatrixTranslate)
                mModelMatrix.multMatrix(localMatrixScale)
                // get handle to shape's transformation matrix
                mMVPMatrixHandle = gl.glGetUniformLocation(mProgram, "uMVPMatrix").also { matrixHandle ->
                    // Pass the projection and view transformation to the shader
                    gl.glUniformMatrix4fv(matrixHandle, 1, false, mModelMatrix.matrix, 0)
                }
                gl.glEnable(GL2ES2.GL_BLEND)
                gl.glBlendFunc(GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA)
                gl.glEnable(GLES2.GL_CULL_FACE)
                gl.glFrontFace(GL_CW)
                // Draw the triangle
                gl.glDrawArrays(GL2ES2.GL_TRIANGLES, 0, shape.vertexCount())
                gl.glDisableVertexAttribArray(th)
                gl.glDisableVertexAttribArray(pos)
            }
        }
    }
}

fun createLine(_startX: Double, _startY: Double, _endX : Double, _endY : Double, _thickness : Double,
               _layer : Double, _color : Color4F) : Line{

    val r = max(0.001, sqrt( (_endX - _startX)*(_endX - _startX) + (_endY - _startY)*(_endY - _startY)))
    val thickness = min(r/2.0, _thickness)
    var angle = -acos((_endY - _startY)/r)
    angle = if(_endX - _startX < 0.0){-angle}else{angle}

    val dx = thickness / 2.0
    val th = thickness
//    val points = arrayOf(
//        // Top part
//        Point3D(x = -dx, y =    r, z = _layer), // bottom right
//        Point3D(x = -dx, y = dx+r, z = _layer), // top left
//        Point3D(x =  dx, y =    r, z = _layer), // bottom left
//        Point3D(x =  dx, y =    r, z = _layer), // bottom right
//        Point3D(x = -dx, y = dx+r, z = _layer), // top right
//        Point3D(x =  dx, y = dx+r, z = _layer),  // top left
//        // Main line
//        Point3D(x = -dx, y =  0.0, z = _layer), // bottom right
//        Point3D(x = -dx, y =    r, z = _layer), // top left
//        Point3D(x =  dx, y =  0.0, z = _layer), // bottom left
//        Point3D(x =  dx, y =  0.0, z = _layer), // bottom right
//        Point3D(x = -dx, y =    r, z = _layer), // top right
//        Point3D(x =  dx, y =    r, z = _layer),  // top left
//        // Bottom part
//        Point3D(x = -dx, y =  -dx, z = _layer), // bottom right
//        Point3D(x = -dx, y =  0.0, z = _layer), // top left
//        Point3D(x =  dx, y =  -dx, z = _layer), // bottom left
//        Point3D(x =  dx, y =  -dx, z = _layer), // bottom right
//        Point3D(x = -dx, y =  0.0, z = _layer), // top right
//        Point3D(x =  dx, y =  0.0, z = _layer)  // top left
//    )
    val points = arrayOf(
        // Top part
        Point3D(x = 0.0, y =    dx+r, z = _layer), // bottom left
        Point3D(x = 0.0, y = dx+dx+r, z = _layer), // top left
        Point3D(x =  th, y =    dx+r, z = _layer), // bottom right
        Point3D(x =  th, y =    dx+r, z = _layer), // bottom right
        Point3D(x = 0.0, y = dx+dx+r, z = _layer), // top right
        Point3D(x =  th, y = dx+dx+r, z = _layer), // top left
        // Main line
        Point3D(x = 0.0, y =       dx, z = _layer), // bottom left
        Point3D(x = 0.0, y =   dx + r, z = _layer), // top left
        Point3D(x =  th, y =       dx, z = _layer), // bottom right
        Point3D(x =  th, y =       dx, z = _layer), // bottom right
        Point3D(x = 0.0, y =     dx+r, z = _layer), // top right
        Point3D(x =  th, y =     dx+r, z = _layer),  // top left
        // Bottom part
        Point3D(x = 0.0, y =      0.0, z = _layer), // bottom left
        Point3D(x = 0.0, y =       dx, z = _layer), // top left
        Point3D(x =  th, y =      0.0, z = _layer), // bottom right
        Point3D(x =  th, y =      0.0, z = _layer), // bottom right
        Point3D(x = 0.0, y =       dx, z = _layer), // top left
        Point3D(x =  th, y =       dx, z = _layer)  // top right
    )

    return Line(points, thickness, r, angle, Point3D(_startX, _startY, _layer), _color)
}

class Line(override val points : Array<Point3D>,
           private var thickness : Double = 0.01,
           private var r : Double = 0.1,
           private var angle : Double = 0.0,
           private var start : Point3D,
           _c : Color4F) : Shape(color4f = _c) {

    fun thickness() = thickness
    fun r() = r
    fun angleRad() = angle
    fun angleDeg() = angle*180.0/PI
    fun start() = start

    init {
        doInit()
    }
}