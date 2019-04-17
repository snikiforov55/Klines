package render.base

import arrow.core.Either
import com.jogamp.opengl.GL2
import java.nio.IntBuffer

class Shader(private val type : Int, private val shaderCode : String = """
                uniform mat4 uMVPMatrix;
                attribute vec4 vPosition;
                void main() {
                    gl_Position = uMVPMatrix * vPosition;
                }
                """)
{
    private var id : Int = 0
    fun loadShader(_gl: GL2) : Either<String, Int> {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        _gl.glCreateShader(type).also { shader ->
            // add the source code to the shader and compile it
            val lines = arrayOf(shaderCode)
            val l = IntBuffer.allocate(1)
            l.put(0, lines[0].length)
            _gl.glShaderSource(shader, 1, lines, l)

            _gl.glCompileShader(shader)
            val res = IntArray(1)
            _gl.glGetShaderiv(shader, GL2.GL_COMPILE_STATUS, res, 0)
            id = res[0]

            if ( id == 0) return Either.Left("Error compile shader: \n\"" + shaderCode + "\"")
            else return Either.Right(id)
        }
    }
    fun id() = id
}