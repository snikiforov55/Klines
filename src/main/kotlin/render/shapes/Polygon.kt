package render.shapes

import arrow.core.Option
import arrow.core.Some
import arrow.core.None
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GLES2
import render.base.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
https://www.gamedev.net/articles/programming/graphics/polygon-triangulation-r3334

create a list of the vertices (perferably in CCW order, starting anywhere)
while true
  for every vertex
    let pPrev = the previous vertex in the list
    let pCur = the current vertex;
    let pNext = the next vertex in the list
    if the vertex is not an interior vertex (the wedge product of (pPrev - pCur) and (pNext - pCur) <= 0, for CCW winding);
      continue;
    if there are any vertices in the polygon inside the triangle made by the current vertex and the two adjacent ones
      continue;
    create the triangle with the points pPrev, pCur, pNext, for a CCW triangle;
    remove pCur from the list;
  if no triangles were made in the above for loop
    break;
 */
fun createPolygon(
    shift: Point3D,
    points: Array<Point3D>,
    _color: Color4F,
    _layer: Double
) : Option<Polygon> {
    var tail = points.toMutableList()
    val res = mutableListOf<Triangle>()
    var cycle : Int = tail.size
    while(tail.size > 2 || cycle < 0){ // Check if there is enough vertexes and if algorithm is not in endless loop
        val triad = tail.take(3).toTypedArray()
        val triangle = Triangle(triad)
        tail = tail.drop(3).toMutableList()
        val inner = triangle.isInner()
        val otherNotIn = tail.filter{p -> triangle.belongs(p)}.isEmpty()
        if(( inner|| tail.isEmpty()) && otherNotIn) {
            res.add(triangle)
            if(tail.isNotEmpty()) {
                tail.add(0, triad.last())
                tail.add(triad.first())
            }
        }
        else{
            tail.add(0, triad[2])
            tail.add(0, triad[1])
            tail.add(triad[0])
        }
        cycle -= 1
    }
    return  if(res.isEmpty())  None
            else Some(Polygon(shift = shift,
        pts = res.map{t->t.flatten()}.toTypedArray().flatten().toTypedArray(), color = _color, layer = _layer
    ))
}

class Polygon(
    shift: Point3D,
    pts: Array<Point3D>,
    color: Color4F,
    layer: Double = 1.0
) : ShapeWrapper(color) {
    override val points  : Array<Point3D> = pts
    val textureBuffer =  // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(points.size * 2 * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            val tex = List(size = points.size / 3){listOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)}.flatten().toFloatArray()
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                tex.forEach { p -> put(p.toFloat()) }
                // set the buffer to read the first coordinate
                position(0)
        }
    }
    init {
        this.layer = layer.toInt()
        this.shift = shift
        doInit()
    }
}
class PolygonRender : RenderBase<Polygon>() {
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
                //varying  float v_Thickness;
                void main() {
                  //float dist = distance(vec2(0.5, 0.5), v_TexCoordinate) / 0.5 * v_Radius;
                  //float edgeWidth = max(v_Radius * 0.008, 0.005);
                  //float outerEdge = smoothstep( -edgeWidth,
                  //                               0.0,
                  //                               dist - v_Radius
                  //                            );
                  //float innerEdge = smoothstep(v_Thickness - edgeWidth,
                  //                             v_Thickness,
                  //                             v_Radius - dist
                  //                            );
                  //innerEdge = v_Thickness < 0.0 ? 0.0 : innerEdge;
                  //float a = 1.0 - outerEdge - innerEdge;
                  float a = distance(vec2(0.0, 0.0), v_TexCoordinate);
                  //if(a <= 0.3) discard;
                  gl_FragColor = vColor;
                  //gl_FragColor.a = a;
                }
    """

    override fun draw(gl : GL2, mvpMatrix: FloatArray, shape : Polygon, isShadow : Int) {
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
                    (2)*4,//vertexStride,
                    shape.textureBuffer
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
                gl.glFrontFace(GL.GL_CW)
                // Draw the triangle
                gl.glDrawArrays(GL2ES2.GL_TRIANGLES, 0, shape.vertexCount())
                gl.glDisableVertexAttribArray(th)
                gl.glDisableVertexAttribArray(pos)
            }
        }
    }

}
