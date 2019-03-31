package render.shapes

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
    constructor(lb : Point3D, ct : Point3D, rb : Point3D) {
        points = arrayOf(ct, lb, rb)
        doInit()
    }
}

class TriangleRender : RenderBase<Triangle>() {

}