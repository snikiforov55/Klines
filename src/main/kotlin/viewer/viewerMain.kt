package viewer

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.jogamp.newt.event.KeyEvent
import com.jogamp.newt.event.KeyListener
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.*
import com.jogamp.opengl.math.Matrix4
import com.jogamp.opengl.util.Animator
import render.base.Color4F
import render.base.Point3D
import render.base.Figure
import render.shapes.*
import render.util.Outline
import kotlin.math.min
import kotlin.math.max


fun main(args: Array<String>) {
    Viewer()
}

class Viewer : GLEventListener, KeyListener {

    private val window : GLWindow
    private val animator : Animator
    private val triangleRender = TriangleRender()
    private val circleRender   = CircleRender()
    private val lineRender     = LineRender()
    private var outliner : Option<Outline> = None
    private val polygonRender = PolygonRender()

    private val mProjectionMatrix = Matrix4()
    private val mViewMatrix       = Matrix4()
    private val mMVPMatrix        = Matrix4()

    private val triangleShapes : Array<Figure<Triangle>> = arrayOf(
        Figure(Triangle(
            Point3D(-0.2,-0.15, 0.0),
            Point3D( 0.0, 0.3, 0.0),
            Point3D( 0.2,-0.1, 0.0),
            4
            ),
            shift = Point3D(0.50, 0.50, 0.0),
            color4f = Color4F( 0.8f,0.1f,0.1f,1.0f)),
        Figure(Triangle(
            Point3D(-0.2,-0.15, 0.0),
            Point3D( 0.0, 0.3, 0.0),
            Point3D( 0.2,-0.1, 0.0),
            3),
            shift = Point3D(0.48, 0.48, 0.0),
            color4f = Color4F( 0.5f,0.1f,0.1f,1.0f)),
        Figure(Triangle(
            Point3D(-0.2,-0.15, 0.0),
            Point3D( 0.0, 0.3, 0.0),
            Point3D( 0.2,-0.1, 0.0),
            2),
            shift = Point3D(0.46, 0.46, 0.0),
            color4f = Color4F( 0.3f,0.1f,0.1f,1.0f)),
        Figure(Triangle(
            Point3D(-0.2,-0.15, 0.0),
            Point3D( 0.0, 0.3, 0.0),
            Point3D( 0.2,-0.1, 0.0),
            1),
            shift = Point3D(0.44, 0.44, 0.0),
            color4f = Color4F( 0.15f,0.1f,0.1f,1.0f))
    )
    private val circles : Array<Option<Figure<Circle>>> = arrayOf(
        createCircle(Point3D(-0.60,-0.03, 0.0),0.1, 0.08, Color4F(0.5f, 0.2f, 0.8f, 1.0f), 1),
        createCircle(Point3D(-0.63,-0.03, 0.0),0.1, 0.08, Color4F(0.5f, 0.2f, 0.6f, 1.0f), 2),
        createCircle(Point3D(-0.66,-0.06, 0.0),0.1, 0.08, Color4F(0.5f, 0.2f, 0.4f, 1.0f), 3),
        createCircle(Point3D(-0.69,-0.09, 0.0),0.1, 0.08, Color4F(0.5f, 0.2f, 0.2f, 1.0f), 4)
        )
    private val center = createCircle(Point3D(-0.00,-0.00, 0.0),0.08, 0.02, Color4F(0.5f, 0.5f, 0.5f, 1.0f), 1)
    private val lines : Array<Figure<Line>> = arrayOf(
        createLine(0.0, 0.1,  0.0,  0.5, 0.015, 9, Color4F(0.6f, 0.9f, 0.1f, 1.0f)),
        createLine(0.1, 0.1,  0.2,  0.4, 0.010, 8, Color4F(0.1f, 0.7f, 0.1f, 1.0f)),
        createLine(0.1, 0.0,  0.5,  0.0, 0.015, 6, Color4F(0.1f, 0.6f, 0.1f, 1.0f)),
        createLine(0.1, -0.1,  0.5,  -0.5, 0.020, 10, Color4F(0.1f, 0.5f, 0.1f, 1.0f)),
        createLine(0.0, -0.1,  0.0,  -0.5, 0.015, 4, Color4F(0.1f, 0.4f, 0.1f, 1.0f)),
        createLine(-0.1, -0.1, -0.5, -0.5, 0.005, 3, Color4F(0.1f, 0.3f, 0.1f, 1.0f)),
        createLine(-0.1, 0.0,  -0.5, -0.0, 0.015, 2, Color4F(0.1f, 0.2f, 0.1f, 1.0f)),
        createLine(-0.1, 0.1,  -0.5,  0.5, 0.035, 2, Color4F(0.1f, 0.8f, 0.1f, 1.0f))
    )
    private val linesHalo : Array<Figure<Line>> = arrayOf(
        createLine(0.7, -0.6,  0.8,  -0.2, 0.06, 1, Color4F(0.6f, 0.2f, 0.1f, 1.0f)),
        createLine(0.6, -0.4,  0.9,  -0.4, 0.06, 1, Color4F(0.1f, 0.9f, 0.1f, 1.0f)),
        createLine(0.7, -0.6,  0.6,  -0.2, 0.06, 1, Color4F(0.3f, 0.3f, 0.7f, 1.0f)),
        createLine(0.4, -0.8,  0.4,  -0.0, 0.06, 3, Color4F(0.3f, 0.3f, 0.7f, 1.0f))
    )
    private val polygons = arrayOf(

        createPolygon(
            Point3D(0.0, 0.4, 1.0),
            arrayOf(Point3D(-0.8, 0.0, 1.0),
                Point3D(-0.8, 0.1, 1.0),
                Point3D(-0.7, 0.2, 1.0),
                Point3D(-0.6, 0.1, 1.0),
                Point3D(-0.6, 0.0, 1.0)
            ), Color4F(0.6f, 0.2f, 0.1f, 1.0f), 1
        ),
        createPolygon(
            Point3D(0.0, 0.6, 1.0),
            arrayOf(
                Point3D(0.0, 0.0, 1.0),
                Point3D(0.1, 0.1, 1.0),
                Point3D(-0.1, 0.2, 1.0),
                Point3D(0.2, 0.2, 1.0),
                Point3D(0.101, 0.1, 1.0),
                Point3D(0.2, 0.1, 1.0),
                Point3D(0.2, 0.0, 1.0)
                ), Color4F(0.2f, 0.4f, 0.1f, 1.0f), 1
        )
    )
    private var shift_x : Double = 0.0
    private var shift_y : Double = 0.0

    init {
        val cap = GLCapabilities(GLProfile.get(GLProfile.GL2))
        cap.stencilBits = 8
        window = GLWindow.create(cap)
        animator = Animator(window)
        with(window) {
            setSize(800, 800); setPosition(100, 50)
            isUndecorated = false; isAlwaysOnTop = false; isFullscreen = false; isPointerVisible = true
            confinePointer(false)
            title = "hello"
            contextCreationFlags = GLContext.CTX_OPTION_DEBUG
            isVisible = true

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
        gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f)
        // Use culling to remove back faces.
        gl.glEnable(GLES2.GL_CULL_FACE)
        gl.glEnable(GLES2.GL_DEPTH_TEST)
        gl.glEnable(GLES2.GL_BLEND)
        gl.glBlendFunc(GLES2.GL_SRC_ALPHA, GLES2.GL_ONE_MINUS_SRC_ALPHA)
        triangleRender.doInit(gl)
        circleRender.doInit(gl)
        lineRender.doInit(gl)
        outliner = Some(Outline(gl))
        polygonRender.doInit(gl)
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

    override fun display(drawable: GLAutoDrawable){
        val gl = drawable.gl.gL2
        with(gl) {
            // Set Background color
            glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
            // Redraw background color
            glClear(GL2.GL_DEPTH_BUFFER_BIT or GL2.GL_COLOR_BUFFER_BIT or GL2.GL_STENCIL_BUFFER_BIT)

            triangleRender.useProgram(gl)
            triangleShapes.forEach { t -> triangleRender.draw(gl = gl, mvpMatrix = mMVPMatrix.matrix, figure = t) }

            circleRender.useProgram(gl)
            circles.forEach { c -> c.map{ cc->circleRender.draw(gl = gl, mvpMatrix = mMVPMatrix.matrix, figure = cc)}}
            center.map{c->circleRender.draw(gl = gl, mvpMatrix = mMVPMatrix.matrix, figure = c)}

            polygonRender.useProgram(gl)
            polygons.forEach {polygon->polygon.map{ p->polygonRender.draw(gl, mMVPMatrix.matrix, p) }}

            outliner.map {
                it.outline(gl, mMVPMatrix, Color4F(1.0f, 1.0f, 1.0f, 1.0f),
                    {_gl : GL2, _mvp : Matrix4, _shadow : Int ->
                        lineRender.useProgram(_gl)
                        linesHalo.forEach { l ->
                            lineRender.draw(gl = gl, mvpMatrix = _mvp.matrix, figure = l, isShadow = _shadow)
                        }
                    }
                )
            }
            gl.glEnable(GL.GL_DEPTH_TEST)
            gl.glDisable(GL.GL_STENCIL_TEST)
            //gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
            lineRender.useProgram(gl)
            lines.forEach { l -> lineRender.draw(gl = gl, mvpMatrix = mMVPMatrix.matrix, figure = l) }
        }
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int){

        with(drawable.gl.gL2) {
            /**
             * Just the glViewport for this sample, normally here you update your
             * projection matrix.
             */
            glViewport(x, y, width, height)
            //val clamp = max(_min, min(_v, _max))

            val ratio = max(0.001f, min(width.toFloat(), Float.MAX_VALUE)) /
                        max(0.001f, min(height.toFloat(), Float.MAX_VALUE))

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            mProjectionMatrix.loadIdentity()
            mProjectionMatrix.makeOrtho(-1f, 1f, -1f, 1f, 0.0f, 100.0f)
            // Set the camera position (View matrix)
//            mViewMatrix.setLookAtM(
//                0f, 0f, 10.1f,
//                0f, 0f, 0f,
//                0f, 1.0f, 0.0f)
            mViewMatrix.loadIdentity()
            mViewMatrix.rotate(0.0f, //kotlin.math.PI.toFloat(),
                0.0f, 1.0f, 0.0f)
            mViewMatrix.translate(0.0f, 0.0f, -10.0f)
            // Calculate the projection and view transformation
            mMVPMatrix.loadIdentity()
            mMVPMatrix.multMatrix(mProjectionMatrix)
            mMVPMatrix.multMatrix(mViewMatrix)
        }
    }

    override fun dispose(drawable: GLAutoDrawable){

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
        if (e.keyCode == KeyEvent.VK_ESCAPE) {
            animator.remove(window)
            window.destroy()
        }
        if(e.keyCode == KeyEvent.VK_LEFT){
            shift_x -= 0.01
        }
        if(e.keyCode == KeyEvent.VK_RIGHT){
            shift_x += 0.01
        }
        lines.forEach { c ->
            c.move(Point3D(shift_x, shift_y, 0.0))
        }
    }
    override fun keyReleased(e: KeyEvent) {
    }
}