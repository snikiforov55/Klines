package render.shapes

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2.*
import com.jogamp.opengl.GLES2
import render.base.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CircleRender() : RenderBase<Circle>(){
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
    override val fragmentShaderCode ="""
                //precision mediump float;
                uniform vec4 vColor;
                varying vec2 v_TexCoordinate;
                varying  float v_Radius;
                varying  float v_Thickness;
                void main() {
                  float dist = distance(vec2(0.5, 0.5), v_TexCoordinate) / 0.5 * v_Radius;
                  float edgeWidth = max(v_Radius * 0.008, 0.005);
                  float outerEdge = smoothstep( -edgeWidth,
                                                 0.0,
                                                 dist - v_Radius
                                              );
                  float innerEdge = smoothstep(v_Thickness - edgeWidth,
                                               v_Thickness,
                                               v_Radius - dist
                                              );
                  innerEdge = v_Thickness < 0.0 ? 0.0 : innerEdge;
                  float a = 1.0 - outerEdge - innerEdge;
                  if(a <= 0.3) discard;
                  gl_FragColor = vColor;
                  gl_FragColor.a = a;
                }
    """

    private val textureCoordinates = arrayOf(
        0.0f, 0.0f, // bottom right
        0.0f, 1.0f, // top left
        1.0f, 0.0f, // bottom left
        1.0f, 0.0f, // bottom right
        0.0f, 1.0f, // top right
        1.0f, 1.0f  // top left
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
    override fun draw(gl : GL2, mvpMatrix: FloatArray, figure : Figure<Circle>, isShadow : Int) {
        // get handle to vertex shader's vPosition member
        gl.glGetAttribLocation(mProgram, "vPosition").also { pos ->

            // Enable a handle to the triangle vertices
            gl.glEnableVertexAttribArray(pos)

            // Prepare the triangle coordinate data
            gl.glVertexAttribPointer(
                pos,
                CoordsPerVertex,
                GL_FLOAT,
                false,
                vertexStride,
                figure.vertexBuffer()
            )
            gl.glGetAttribLocation(mProgram, "a_TexCoordinate").also { th ->

                // Enable a handle to the triangle vertices
                gl.glEnableVertexAttribArray(th)

                // Prepare the triangle coordinate data
                gl.glVertexAttribPointer(
                    th,
                    2,//CoordsPerVertex,
                    GL_FLOAT,
                    false,
                    2*4,//vertexStride,
                    textureBuffer
                )
                // get handle to fragment shader's vColor member
                mColorHandle = gl.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                    // Set color for drawing the triangle
                    gl.glUniform4fv(colorHandle, 1, figure.colorBuffer(), 0)
                }
                gl.glGetUniformLocation(mProgram, "a_RadiusThickness").also { r ->
                    gl.glEnableVertexAttribArray(r)
                    gl.glUniform2f(r, figure.shape.radius.toFloat(), figure.shape.thickness.toFloat())
                }
                mModelMatrix.loadIdentity()
                mTranslateMatrix.loadIdentity()
                mTranslateMatrix.translate(
                    figure.shift().x.toFloat(),
                    figure.shift().y.toFloat(),
                    figure.shift().z.toFloat()
                )
                mModelMatrix.multMatrix(mvpMatrix)
                mModelMatrix.multMatrix(mTranslateMatrix)

                // get handle to shape's transformation matrix
                mMVPMatrixHandle = gl.glGetUniformLocation(mProgram, "uMVPMatrix").also { matrixHandle ->
                    // Pass the projection and view transformation to the shader
                    gl.glUniformMatrix4fv(matrixHandle, 1, false, mModelMatrix.matrix, 0)
                }
                gl.glEnable(GL_BLEND)
                gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                gl.glEnable(GLES2.GL_CULL_FACE)
                gl.glEnable(GLES2.GL_DEPTH_TEST)
                gl.glFrontFace(GL.GL_CW)
                // Draw the triangle
                gl.glDrawArrays(GL_TRIANGLES, 0, figure.vertexCount())
                gl.glDisableVertexAttribArray(th)
                gl.glDisableVertexAttribArray(pos)
            }
        }
    }
}
data class Circle(val origin : Point3D,
                  val radius : Double,
                  val thickness : Double,
                  val layer : Int = 0 ) : Shape(), ShapeInterface {
    companion object{
        fun figure(_sh : Point3D, _radius : Double, _thickness : Double,
                  _color : Color4F = Color4F(),
                  _layer : Int) : Option<Figure<Circle>> {
            return if(_thickness < 0.005 || _radius < 0.01) None
            else  Some(Figure(
                shape = Circle(origin = _sh, thickness = _thickness,radius = _radius, layer = _layer),
                color4f = _color,
                shift = _sh))
        }
    }
    private val points : List<Point3D> = listOf(
        Point3D(-radius, -radius, layer.toDouble()), // bottom right
        Point3D(-radius,  radius, layer.toDouble()), // top left
        Point3D( radius, -radius, layer.toDouble()), // bottom left
        Point3D( radius, -radius, layer.toDouble()), // bottom right
        Point3D(-radius,  radius, layer.toDouble()), // top right
        Point3D( radius,  radius, layer.toDouble())  // top left
    )
    override fun points(): List<Point3D> = points
}