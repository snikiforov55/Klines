package render.base


import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLES3.*
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLES2
import com.jogamp.opengl.math.Matrix4
import java.nio.ByteBuffer
import java.nio.IntBuffer

abstract class RenderBase<S : ShapeInterface>() {

    protected   var mProgram: Int =0
    var vertexShader: Int = 0
    var fragmentShader: Int = 0
    //protected   var mPositionHandle: Int = 0
    protected   var mColorHandle: Int = 0
    protected   val CoordsPerVertex = 3
    protected   val vertexStride: Int = CoordsPerVertex * 4 // 4 bytes per vertex

    // Use to access and set the view transformation
    protected var mMVPMatrixHandle: Int = 0
    protected var mTranslateMatrix: Matrix4 = Matrix4()
    protected var mModelMatrix : Matrix4 = Matrix4()
    open protected val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }

    """
    open protected val fragmentShaderCode = """
        //precision mediump float;
        uniform vec4 vColor;
        void main() {
          gl_FragColor = vColor;
        }

        """

    init{
        //Matrix4.setIdentityM(mModelMatrix, 0)
    }

    private fun loadShader(gl: GL2, type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)

        return gl.glCreateShader(type).also { shader ->
            // add the source code to the shader and compile it
            val lines = arrayOf(shaderCode)
            val l = IntBuffer.allocate(1)
            l.put(0,lines[0].length)
            gl.glShaderSource(shader, 1, lines, l)

            gl.glCompileShader(shader)
            val res = IntArray(1)
            gl.glGetShaderiv(shader, GL_COMPILE_STATUS, res, 0)
            if(res[0] == 0){
                print("Error compile shader: ")
                println(res[0])
                println(shaderCode)
                throw Exception("Error compile shader")
            }
        }
    }
    fun doInit(gl: GL2) {
         val vertexShader:   Int = loadShader(gl, GL_VERTEX_SHADER, vertexShaderCode)
         val fragmentShader: Int = loadShader(gl, GL_FRAGMENT_SHADER, fragmentShaderCode)

         // create empty OpenGL ES Program
         mProgram = gl.glCreateProgram().also {
             // add the vertex shader to program
             gl.glAttachShader(it, vertexShader)
             // add the fragment shader to program
             gl.glAttachShader(it, fragmentShader)
             // creates OpenGL ES program executables
             gl.glLinkProgram(it)
             var res = IntArray(1)
             res[0] = 255
             gl.glGetProgramiv(it, GL_LINK_STATUS, res, 0)
             if(res[0] == 0){

                 val log = getProgramInfoLog(gl, it)
                 gl.glDeleteProgram(it)
                 mProgram = 0
                 println("Error link shader. " + log)
                 throw Exception("Error link shader " + log)
             }
         }
    }
    fun getProgramInfoLog(gl: GL2, shader: Int): String {
        val l = IntBuffer.allocate(1)
        gl.glGetShaderiv(shader, GL2.GL_INFO_LOG_LENGTH, l)
        val infoLogLength = l[0]
        val bufferInfoLog = ByteBuffer.allocate(infoLogLength)
        gl.glGetProgramInfoLog(shader, infoLogLength, l, bufferInfoLog)
        val bytes = ByteArray(infoLogLength)
        return String(bytes)
    }
    fun useProgram(gl : GL2){
        mProgram.also {
            // Add program to OpenGL ES environment
            gl.glUseProgram(mProgram)
        }
    }
    open fun draw(gl : GL2, mvpMatrix: FloatArray, shape : S, isShadow : Int = 0) {

        // get handle to vertex shader's vPosition member
        gl.glGetAttribLocation(mProgram, "vPosition").also {

            // Enable a handle to the triangle vertices
            gl.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            gl.glVertexAttribPointer(
                it,
                CoordsPerVertex,
                GL_FLOAT,
                false,
                vertexStride,
                shape.vertexBuffer()
            )
            // get handle to fragment shader's vColor member
            mColorHandle = gl.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                // Set color for drawing the triangle
                gl.glUniform4fv(colorHandle, 1, shape.colorBuffer(), 0)
            }
            mModelMatrix.loadIdentity()
            mTranslateMatrix.loadIdentity()
            mTranslateMatrix.translate(
                shape.shift().x.toFloat(),
                shape.shift().y.toFloat(),
                shape.shift().z.toFloat())
            mModelMatrix.multMatrix(mvpMatrix)
            mModelMatrix.multMatrix(mTranslateMatrix)

            // get handle to shape's transformation matrix
            mMVPMatrixHandle = gl.glGetUniformLocation(mProgram, "uMVPMatrix").also{matrixHandle ->
                // Pass the projection and view transformation to the shader
                gl.glUniformMatrix4fv(matrixHandle, 1, false, mModelMatrix.matrix, 0)
            }
            gl.glEnable(GLES2.GL_CULL_FACE)
            gl.glEnable(GLES2.GL_DEPTH_TEST)
            gl.glEnable(GLES2.GL_BLEND)
            gl.glBlendFunc(GLES2.GL_SRC_ALPHA, GLES2.GL_ONE_MINUS_SRC_ALPHA)
            gl.glFrontFace(GL.GL_CW)
            // Draw the triangle
            gl.glDrawArrays(GL_TRIANGLES, 0, shape.vertexCount())
            // Disable vertex array
            gl.glDisableVertexAttribArray(it)
        }
    }
}