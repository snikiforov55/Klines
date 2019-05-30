package render.shapes


import arrow.core.Option
import arrow.core.Some
import arrow.core.None
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
class PolygonRender : RenderBase<Polygon>()
