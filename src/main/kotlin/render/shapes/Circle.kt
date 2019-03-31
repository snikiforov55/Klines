package render.shapes

import com.jogamp.opengl.GL2ES2.*
import com.jogamp.opengl.math.Matrix4
import render.base.Point3D
import render.base.RenderBase
import render.base.Shape
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Circle( private var radius : Double, private var thickness : Double) : Shape(){
    override var points : Array<Point3D> =  arrayOf(
        Point3D(radius, -radius, 0.0), // bottom right
        Point3D(-radius, radius, 0.0), // top left
        Point3D(-radius, -radius, 0.0), // bottom left
        Point3D(radius, -radius, 0.0), // bottom right
        Point3D(radius, radius, 0.0), // top right
        Point3D(-radius, radius, 0.0)  // top left
    )
    init{
        doInit()
    }
    fun thickness() : Double = thickness
    fun radius() : Double = radius
    fun setRadius(_r : Double){
        radius = _r
        points = arrayOf(
            Point3D(radius, -radius, 0.0), // bottom right
            Point3D(-radius, radius, 0.0), // top left
            Point3D(-radius, -radius, 0.0), // bottom left
            Point3D(radius, -radius, 0.0), // bottom right
            Point3D(radius, radius, 0.0), // top right
            Point3D(-radius, radius, 0.0)  // top left
        )
        doInit()
    }
}

class CircleRender : RenderBase<Circle>(){
    /**
     * Vertex Shader
     */
    override val vertexShaderCode =
        "uniform mat4 uMVPMatrix;      \n" +
                "attribute vec4 vPosition;     \n" +
                "attribute vec2 a_TexCoordinate; \n" +
                "uniform   vec2 a_RadiusThickness;"+
                "varying   vec2 v_TexCoordinate;" +
                "varying  float v_Radius;"+
                "varying  float v_Thickness;"+
                "void main() {" +
                "   v_Radius    = a_RadiusThickness.x;"+
                "   v_Thickness = a_RadiusThickness.y;"+
                "  v_TexCoordinate = a_TexCoordinate;" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    /**
     * Fragment shader
     *
     *
     * Antialiasing and thickness of the ring are provided by a combination of edges.
     * Inner edge:     __________________
     *             ___/
     *                | thickness |______
     * Outer edge: _______________/
     *
     * The filled circle is generated if Thickness < 0.0 or Thickness > Radius
     *
     */
    override val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "varying vec2 v_TexCoordinate;" +
                "varying  float v_Radius;"+
                "varying  float v_Thickness;"+
                "void main() {" +
                "  float dist = distance(vec2(0.5, 0.5), v_TexCoordinate) / 0.5 * v_Radius;"+
                "  float edgeWidth = max(v_Radius * 0.015, 0.005);"+
                "  float outerEdge = smoothstep( -edgeWidth," +
                "                                 0.0," +
                "                                 dist - v_Radius" +
                "                              );"+
                "  float innerEdge = smoothstep(v_Thickness - edgeWidth," +
                "                               v_Thickness," +
                "                               v_Radius - dist" +
                "                              );" +
                "  innerEdge = v_Thickness < 0.0 ? 0.0 : innerEdge;"+
                "  float a = 1.0 - outerEdge - innerEdge;"+
                "  if(a <= 0.5) discard;"+
                "  gl_FragColor = vColor;" +
                "  gl_FragColor.a = a;" +
                "}"

    private val textureCoordinates = arrayOf(
        1.0f, 0.0f, // bottom right
        0.0f, 1.0f, // top left
        0.0f, 0.0f, // bottom left
        1.0f, 0.0f, // bottom right
        1.0f, 1.0f, // top right
        0.0f, 1.0f  // top left
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
    override fun draw(mvpMatrix: FloatArray, shape : Circle) {
        // get handle to vertex shader's vPosition member
        glGetAttribLocation(mProgram, "vPosition").also { pos ->

            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(pos)

            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                pos,
                CoordsPerVertex,
                GL_FLOAT,
                false,
                vertexStride,
                shape.vertexBuffer()
            )
            glGetAttribLocation(mProgram, "a_TexCoordinate").also { th ->

                // Enable a handle to the triangle vertices
                glEnableVertexAttribArray(th)

                // Prepare the triangle coordinate data
                glVertexAttribPointer(
                    th,
                    2,//CoordsPerVertex,
                    GL_FLOAT,
                    false,
                    2*4,//vertexStride,
                    textureBuffer
                )
                // get handle to fragment shader's vColor member
                mColorHandle = glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                    // Set color for drawing the triangle
                    glUniform4fv(colorHandle, 1, shape.colorBuffer(), 0)
                }
                glGetUniformLocation(mProgram, "a_RadiusThickness").also { r ->
                    glEnableVertexAttribArray(r)
                    glUniform2f(r, shape.radius().toFloat(), shape.thickness().toFloat())
                }
                Matrix.setIdentityM(mModelMatrix, 0)
                Matrix.setIdentityM(mTranslateMatrix, 0)
                Matrix.translateM(
                    mTranslateMatrix,
                    0,
                    shape.shift().x.toFloat(),
                    shape.shift().y.toFloat(),
                    shape.shift().z.toFloat()
                )
                Matrix.multiplyMM(mModelMatrix, 0, mvpMatrix, 0, mTranslateMatrix, 0)

                // get handle to shape's transformation matrix
                mMVPMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix").also { matrixHandle ->
                    // Pass the projection and view transformation to the shader
                    glUniformMatrix4fv(matrixHandle, 1, false, mModelMatrix, 0)
                }
                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                // Draw the triangle
                glDrawArrays(GL_TRIANGLES, 0, shape.vertexCount())

                glDisableVertexAttribArray(th)
                glDisableVertexAttribArray(pos)
            }
        }
    }
}