package render.shapes

import com.jogamp.opengl.GL2
import render.base.Point3D
import render.base.RenderBase
import render.base.Shape

class Polygon : Shape {
    override val points  : Array<Point3D>
    val          texture : Array<Double>
    constructor(){
        points = arrayOf(
            Point3D(-0.5, -0.5, 0.0),
            Point3D(-0.5, 0.5, 0.0),
            Point3D(0.5, -0.0, 0.0)
        )
        texture = arrayOf(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
        doInit()
    }
    constructor(pts : Array<Point3D>) {
        points = pts
        texture = arrayOf(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
        doInit()
    }
}

class PolygonRender() : RenderBase<Polygon>() {

}