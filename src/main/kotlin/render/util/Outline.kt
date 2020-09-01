package render.util

import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLES2
import com.jogamp.opengl.math.Matrix4
import render.base.Color4F
import render.base.Program
import render.base.Shader

class Outline(gl : GL2){

    private val program : Program = Program(gl,
            arrayOf(
                Shader(GL2.GL_VERTEX_SHADER,"""
                    uniform mat4 uMVPMatrix;
                    attribute vec4 vPosition;
                    void main() {
                        gl_Position = uMVPMatrix * vPosition;
                    }
                """),
                Shader(GL2.GL_FRAGMENT_SHADER, """
                    uniform vec4 vColor;
                    void main() {
                        gl_FragColor = vColor;
                    }
                """)
            )
        )
fun outline(_gl : GL2, _mvpMatrix : Matrix4, _color : Color4F, _render : (GL2, Matrix4, Int)->Unit){
    _gl.glEnable(GL_DEPTH_TEST)
    _gl.glEnable(GL_STENCIL_TEST)
    _gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)

    _gl.glStencilFunc(GL_ALWAYS, 1, 0xFF)
    _gl.glStencilMask(0xFF)
    _gl.glEnable(GL2.GL_DEPTH_TEST)
    _render(_gl, _mvpMatrix, 0)
    _gl.glStencilFunc(GL_NOTEQUAL, 1, 0xFF)
    _gl.glStencilMask(0x00)
    _gl.glDisable(GL_DEPTH_TEST)



    _render(_gl, _mvpMatrix, 1)

    _gl.glStencilMask(0x00)
    _gl.glEnable(GL_DEPTH_TEST)

}
}