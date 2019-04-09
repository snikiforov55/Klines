package render.shapes

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GLES2
import render.base.Color4F
import render.base.Point3D
import render.base.RenderBase
import render.base.Shape
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class LineRender() : RenderBase<Circle>(){
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
                  // --- with proper sorting it is not needed :) if(a <= 0.5) discard;
                  gl_FragColor = vColor;
                  gl_FragColor.a = a;
                }
    """

    private val textureCoordinates = arrayOf(
         0.0f, 0.0f, // bottom left
         0.0f, 1.0f, // top left
        0.25f, 0.0f, // bottom right
        0.25f, 0.0f, // bottom right
         0.0f, 1.0f, // top left
        0.25f, 1.0f  // top right
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
    override fun draw(gl : GL2, mvpMatrix: FloatArray, shape : Circle) {
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
                gl.glGetUniformLocation(mProgram, "a_RadiusThickness").also { r ->
                    gl.glEnableVertexAttribArray(r)
                    gl.glUniform2f(r, shape.radius().toFloat(), shape.thickness().toFloat())
                }
                mModelMatrix.loadIdentity()
                mTranslateMatrix.loadIdentity()
                mTranslateMatrix.translate(
                    shape.shift().x.toFloat(),
                    shape.shift().y.toFloat(),
                    shape.shift().z.toFloat()
                )
                mModelMatrix.multMatrix(mvpMatrix)
                mModelMatrix.multMatrix(mTranslateMatrix)

                // get handle to shape's transformation matrix
                mMVPMatrixHandle = gl.glGetUniformLocation(mProgram, "uMVPMatrix").also { matrixHandle ->
                    // Pass the projection and view transformation to the shader
                    gl.glUniformMatrix4fv(matrixHandle, 1, false, mModelMatrix.matrix, 0)
                }
                gl.glEnable(GL2ES2.GL_BLEND)
                gl.glBlendFunc(GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA)
                gl.glEnable(GLES2.GL_CULL_FACE)
                gl.glEnable(GLES2.GL_DEPTH_TEST)
                // Draw the triangle
                gl.glDrawArrays(GL2ES2.GL_TRIANGLES, 0, shape.vertexCount())
                gl.glDisableVertexAttribArray(th)
                gl.glDisableVertexAttribArray(pos)
            }
        }
    }
}

class Line : Shape {
    private var thickness : Double = 0.01
    private var r : Double = 0.1
    private var angle : Double = 0.0


    override var points : Array<Point3D> = arrayOf(
        Point3D(-1.0, -1.0, 0.0), // bottom right
        Point3D(-1.0,  1.0, 0.0), // top left
        Point3D( 1.0, -1.0, 0.0), // bottom left
        Point3D( 1.0, -1.0, 0.0), // bottom right
        Point3D(-1.0,  1.0, 0.0), // top right
        Point3D( 1.0,  1.0, 0.0)  // top left
    )
    constructor(_start : Point3D, _end : Point3D, _thickness : Double, _color : Color4F, _layer : Double = 1.0){
        initInternals(_start, _end, _thickness, _color, _layer)

    }
    private fun initInternals(_start : Point3D, _end : Point3D, _thickness : Double, _color : Color4F, _layer : Double){
        layer = _layer
        r = max(0.001, sqrt( (_end.x - _start.x)*(_end.x - _start.x) + (_end.y - _start.y)*(_end.y - _start.y)))
        thickness = min(r/2.0, thickness)
        angle = acos((_end.y - _start.y)/r)

        val dx = _thickness / 2.0
        points = arrayOf(
        Point3D(x = -dx, y = -thickness , z = layer), // bottom right
        Point3D(x = -dx, y = r+thickness, z = layer), // top left
        Point3D(x =  dx, y = -thickness , z = layer), // bottom left
        Point3D(x =  dx, y = -thickness , z = layer), // bottom right
        Point3D(x = -dx, y = r+thickness, z = layer), // top right
        Point3D(x =  dx, y = -thickness , z = layer)  // top left
        )
        setColor(_color)
    }
}