package render.shapes

import com.jogamp.opengl.GL2
import render.base.Color4F
import render.base.Point3D
import render.base.RenderBase
import render.base.Shape

class Triangle : Shape {
    override val points : Array<Point3D>
    constructor(){
        points = arrayOf(
            Point3D(-0.1, -0.07, 0.0),
            Point3D(0.0, 0.14, 0.0),
            Point3D(0.1, -0.07, 0.0)
        )
        doInit()
    }
    constructor(lb : Point3D, ct : Point3D, rb : Point3D, color4F: Color4F, layer: Double = 0.0) {
        points = arrayOf(
            Point3D(lb.x, lb.y, layer),
            Point3D(ct.x, ct.y, layer),
            Point3D(rb.x, rb.y, layer)
        )
        setColor(color4F.r, color4F.g, color4F.b, color4F.a)
        doInit()
    }
    constructor(sh : Point3D, lb : Point3D, ct : Point3D, rb : Point3D, color4F: Color4F, _layer: Double = 0.0) {
        layer = _layer
        points = arrayOf(
            Point3D(lb.x, lb.y, layer),
            Point3D(ct.x, ct.y, layer),
            Point3D(rb.x, rb.y, layer)
        )
        shift = sh

        setColor(color4F.r, color4F.g, color4F.b, color4F.a)
        doInit()
    }
}

class TriangleRender() : RenderBase<Triangle>() {

}