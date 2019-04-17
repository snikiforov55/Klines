package render.base

import arrow.core.Either
import com.jogamp.opengl.GL2
import java.nio.ByteBuffer
import java.nio.IntBuffer

class Program(private val gl : GL2, private val shaders : Array<Shader>) {
    private val mProgram : Int

    init {
//        val vertexShader:   Int = loadShader(gl, GL2.GL_VERTEX_SHADER, shaderCodeVertex)
//        val fragmentShader: Int = loadShader(gl, GL2.GL_FRAGMENT_SHADER, shaderCodeFragment)

        // create empty OpenGL ES Program
        mProgram = gl.glCreateProgram().also {
            shaders.forEach { s->             // add the vertex shader to program
                s.loadShader(gl).map {id ->
                    gl.glAttachShader(it, id)
                }.mapLeft { s-> println(s) }
            }
            // creates OpenGL ES program executables
            gl.glLinkProgram(it)
            var res = IntArray(1)
            res[0] = 255
            gl.glGetProgramiv(it, GL2.GL_LINK_STATUS, res, 0)
            if(res[0] == 0){

                val log = getProgramInfoLog(gl, it)
                gl.glDeleteProgram(it)
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
    fun id() : Either<String, Int> {
        return when(mProgram){
          0 -> Either.Left("Invalid program")
          else -> Either.Right(mProgram)
        }
    }
}