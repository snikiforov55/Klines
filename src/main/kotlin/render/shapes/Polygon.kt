package render.shapes


import com.jogamp.opengl.GL2
import arrow.core.Option
import arrow.core.Some
import arrow.core.None
import com.jogamp.opengl.GL
import com.jogamp.opengl.GLES2
import com.jogamp.opengl.GLES3
import render.base.*


/*
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

fun createPolygon(points  : Array<Point3D>, _color : Color4F, _layer : Double) : Option<Polygon> {
    var tail = points.toMutableList()
    val res = mutableListOf<Triangle>()
    while(tail.size > 2){
        val triad = tail.take(3).toTypedArray()
        val triangle = Triangle(triad)
        tail = tail.drop(3).toMutableList()
        if((triangle.isInner() || tail.isEmpty()) && tail.filter{p -> triangle.belongs(p)}.isEmpty()) {
            res.add(triangle)
            if(tail.isNotEmpty()) {
                tail.add(0, triad.last())
                tail.add(triad.first())
            }
        }
        else{
            tail.add(0, triad.last())
            tail.add(0, triad.dropLast(1).last())
            tail.add(triad.first())
        }
    }
    return  if(res.isEmpty())  None
            else Some(Polygon(pts = res.map{t->t.flatten()}.toTypedArray().flatten().toTypedArray(),
        color = _color, layer = _layer))
}

class Polygon : Shape {
    override val points  : Array<Point3D>
    constructor(pts : Array<Point3D>, color : Color4F, layer : Double = 1.0) {
        points = pts
        doInit()
        setColor(color)
        this.layer = layer
    }
}

class PolygonRender() : RenderBase<Polygon>() {
    override fun draw(gl: GL2, mvpMatrix: FloatArray, shape: Polygon, isShadow: Int) {

        // get handle to vertex shader's vPosition member
        gl.glGetAttribLocation(mProgram, "vPosition").also {

            // Enable a handle to the triangle vertices
            gl.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            gl.glVertexAttribPointer(
                it,
                CoordsPerVertex,
                GLES3.GL_FLOAT,
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
                shape.shift().z.toFloat()
            )
            mModelMatrix.multMatrix(mvpMatrix)
            mModelMatrix.multMatrix(mTranslateMatrix)

            // get handle to shape's transformation matrix
            mMVPMatrixHandle = gl.glGetUniformLocation(mProgram, "uMVPMatrix").also { matrixHandle ->
                // Pass the projection and view transformation to the shader
                gl.glUniformMatrix4fv(matrixHandle, 1, false, mModelMatrix.matrix, 0)
            }
            gl.glEnable(GLES2.GL_CULL_FACE)
            gl.glEnable(GLES2.GL_DEPTH_TEST)
            gl.glEnable(GLES2.GL_BLEND)
            gl.glBlendFunc(GLES2.GL_SRC_ALPHA, GLES2.GL_ONE_MINUS_SRC_ALPHA)
            gl.glFrontFace(GL.GL_CW)
            // Draw the triangle
            gl.glDrawArrays(GLES3.GL_TRIANGLES, 0, shape.vertexCount())
            // Disable vertex array
            gl.glDisableVertexAttribArray(it)
        }
    }
}