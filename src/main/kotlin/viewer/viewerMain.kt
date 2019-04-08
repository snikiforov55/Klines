package viewer

import com.jogamp.newt.event.KeyEvent
import com.jogamp.newt.event.KeyListener
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.*
import com.jogamp.opengl.util.Animator
import render.shapes.CircleRender
import render.shapes.TriangleRender


fun main(args: Array<String>) {
    Viewer()
}

class Viewer : GLEventListener, KeyListener {

    private val window = GLWindow.create(GLCapabilities(GLProfile.get(GLProfile.GL2)))
    private val animator = Animator(window)
    private var triangleRender = TriangleRender()
    private var circleRender   = CircleRender()

    init {
        with(window) {
            setSize(1024, 768); setPosition(100, 50)
            isUndecorated = false; isAlwaysOnTop = false; isFullscreen = false; isPointerVisible = true
            confinePointer(false); title = "hello"; contextCreationFlags = GLContext.CTX_OPTION_DEBUG; isVisible = true
            addGLEventListener(this@Viewer)
            addKeyListener(this@Viewer)
        }
        animator.start()
    }
    private var start = System.currentTimeMillis()

    override fun init(drawable: GLAutoDrawable) {
        val gl = drawable.gl.gL2
        start = System.currentTimeMillis()
        // Set the background frame color
        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
//      // Use culling to remove back faces.
//        gl.glEnable(GLES2.GL_CULL_FACE)
//        gl.glEnable(GLES2.GL_BLEND)
//        gl.glEnable(GLES2.GL_DEPTH_TEST)
//        gl.glBlendFunc(GLES2.GL_SRC_ALPHA, GLES2.GL_ONE_MINUS_SRC_ALPHA)
//
        triangleRender.doInit(gl)
        circleRender.doInit(gl)
    }

//    private fun initDebug(gl: GL4) {
//
//        window.context.addGLDebugListener(GlDebugOutput())
//
//        with(gl) {
//            // Turn off all the debug
//            glDebugMessageControl(
//                GL_DONT_CARE, // source
//                GL_DONT_CARE, // type
//                GL_DONT_CARE, // severity
//                0, // count
//                null, // id
//                false) // enabled
//            // Turn on all OpenGL Errors, shader compilation/linking errors, or highly-dangerous undefined behavior
//            glDebugMessageControl(
//                GL_DONT_CARE, // source
//                GL_DONT_CARE, // type
//                GL_DEBUG_SEVERITY_HIGH, // severity
//                0, // count
//                null, // id
//                true) // enabled
//            // Turn on all major performance warnings, shader compilation/linking warnings or the use of deprecated functions
//            glDebugMessageControl(
//                GL_DONT_CARE, // source
//                GL_DONT_CARE, // type
//                GL_DEBUG_SEVERITY_MEDIUM, // severity
//                0, // count
//                null, // id
//                true) // enabled
//        }
//    }

    override fun display(drawable: GLAutoDrawable): Unit {
        with(drawable.gl.gL2) {
        // Set Background color
        glClearColor(0.4f, 0.8f, 0.2f, 1.0f)
        // Redraw background color
        glClear(GL2.GL_DEPTH_BUFFER_BIT or GL2.GL_COLOR_BUFFER_BIT)
//
//        //triangleRender.useProgram()
//        //triangleRender.draw(mMVPMatrix, ballModel.triangle)
//        //triangleRender.draw(mMVPMatrix, ballModel.triangle1)
//
//        circleRender.useProgram()
//        circleRender.draw(mMVPMatrix, ballModel.left())
//        circleRender.draw(mMVPMatrix, ballModel.right())
//        //circleRender.draw(mMVPMatrix, ballModel.circle)


//

//
//            run {
//                // update matrix based on time
//                var now = System.currentTimeMillis()
//                val diff = (now - start) / 1000f
//                /**
//                 * Here we build the matrix that will multiply our original vertex
//                 * positions. We scale, halving it, and rotate it.
//                 */
//                scale = FloatUtil.makeScale(scale, true, 0.5f, 0.5f, 0.5f)
//                zRotation = FloatUtil.makeRotationEuler(zRotation, 0, 0f, 0f, diff)
//                modelToClip = FloatUtil.multMatrix(scale, zRotation)
//
//                transformPointer.asFloatBuffer().put(modelToClip)
//            }
//            glUseProgram(programName)
//            glBindVertexArray(vertexArrayName.get(0))
//
//            glBindBufferBase(
//                GL_UNIFORM_BUFFER, // Target
//                Semantic.Uniform.TRANSFORM0, // index
//                bufferName.get(Buffer.TRANSFORM)) // buffer
//
//            glDrawElements(
//                GL_TRIANGLES, // primitive mode
//                elementCount, // element count
//                GL_UNSIGNED_SHORT, // element type
//                0) // element offset}
        }
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit {

        with(drawable.gl.gL2) {
            /**
             * Just the glViewport for this sample, normally here you update your
             * projection matrix.
             */
            glViewport(x, y, width, height)

//            val ratio = MathUtils.clamp(_width.toFloat(), 0.0001F, Float.MAX_VALUE) /
//                    MathUtils.clamp(_height.toFloat(), 0.0001F, Float.MAX_VALUE)
//
//            // this projection matrix is applied to object coordinates
//            // in the onDrawFrame() method
//            //Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 3f)
//            Matrix.orthoM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.1f, 100.0f)
//            // Set the camera position (View matrix)
//            Matrix.setLookAtM(mViewMatrix, 0,
//                0f, 0f, 10.1f,
//                0f, 0f, 0f,
//                0f, 1.0f, 0.0f)
//
//            // Calculate the projection and view transformation
//            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
//            ballModel.reshape(_width.toFloat(), _height.toFloat(), ratio)
        }
    }

    override fun dispose(drawable: GLAutoDrawable): Unit {

//        with(drawable.gl.gL4) {
//
//            glUnmapNamedBuffer(bufferName.get(Buffer.TRANSFORM))
//
//            glDeleteProgram(programName)
//            glDeleteVertexArrays(1, vertexArrayName)
//            glDeleteBuffers(Buffer.MAX, bufferName)
//        }
//
//        BufferUtils.destroyDirectBuffer(vertexArrayName)
//        BufferUtils.destroyDirectBuffer(bufferName)
//
//        BufferUtils.destroyDirectBuffer(clearColor)
//        BufferUtils.destroyDirectBuffer(clearDepth)

        System.exit(0)
    }


    override fun keyPressed(e: KeyEvent) {
        if (e.keyCode === KeyEvent.VK_ESCAPE) {
            animator.remove(window)
            window.destroy()
        }
    }

    override fun keyReleased(e: KeyEvent) {
    }
}