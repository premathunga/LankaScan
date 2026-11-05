package com.lankascan.util
import android.graphics.*
import kotlin.math.max
import kotlin.math.min
object ImageProcessor{
 fun magicFilter(src: Bitmap): Bitmap{
  val w=src.width; val h=src.height
  val gray=toGrayscale(src)
  val small=Bitmap.createScaledBitmap(gray, w/8, h/8, true)
  val blurred=Bitmap.createScaledBitmap(small, w, h, true)
  val out=Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
  for(y in 0 until h){ for(x in 0 until w){
   val g=Color.red(gray.getPixel(x,y)); val bg=Color.red(blurred.getPixel(x,y)).coerceAtLeast(10)
   var v=(255f*g/bg).toInt().coerceIn(0,255); if(v>180) v=255
   out.setPixel(x,y, Color.rgb(v,v,v))
  }}
  return out
 }
 fun toGrayscale(src: Bitmap): Bitmap{
  val bmp=Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
  val c=Canvas(bmp); val p=Paint(); val cm=ColorMatrix(); cm.setSaturation(0f); p.colorFilter=ColorMatrixColorFilter(cm); c.drawBitmap(src,0f,0f,p); return bmp
 }
 fun bwFilter(src: Bitmap): Bitmap{
  val g=toGrayscale(src); val out=Bitmap.createBitmap(g.width,g.height, Bitmap.Config.ARGB_8888)
  for(y in 0 until g.height) for(x in 0 until g.width){ val v=Color.red(g.getPixel(x,y)); val bw=if(v>130) 255 else 0; out.setPixel(x,y, Color.rgb(bw,bw,bw)) }
  return out
 }
 fun rotate(bmp: Bitmap, deg:Float): Bitmap{ val m=Matrix(); m.postRotate(deg); return Bitmap.createBitmap(bmp,0,0,bmp.width,bmp.height,m,true) }
 fun perspectiveCrop(src: Bitmap, pts: List<PointF>): Bitmap{
  if(pts.size!=4) return src
  val maxW= max(distance(pts[2],pts[3]), distance(pts[1],pts[0])).toInt().coerceAtLeast(200)
  val maxH= max(distance(pts[1],pts[2]), distance(pts[0],pts[3])).toInt().coerceAtLeast(200)
  val srcArr=floatArrayOf(pts[0].x,pts[0].y, pts[1].x,pts[1].y, pts[2].x,pts[2].y, pts[3].x,pts[3].y)
  val dstArr=floatArrayOf(0f,0f, maxW.toFloat(),0f, maxW.toFloat(),maxH.toFloat(), 0f,maxH.toFloat())
  val matrix=Matrix(); matrix.setPolyToPoly(srcArr,0,dstArr,0,4)
  val out=Bitmap.createBitmap(maxW,maxH, Bitmap.Config.ARGB_8888); val canvas=Canvas(out); canvas.drawColor(Color.WHITE); canvas.drawBitmap(src,matrix,Paint(Paint.FILTER_BITMAP_FLAG)); return out
 }
 private fun distance(a:PointF,b:PointF):Float{ val dx=a.x-b.x; val dy=a.y-b.y; return kotlin.math.sqrt(dx*dx+dy*dy) }
 fun autoDetectEdges(w:Float,h:Float):List<PointF>{ return listOf(PointF(w*0.08f,h*0.08f), PointF(w*0.92f,h*0.08f), PointF(w*0.92f,h*0.92f), PointF(w*0.08f,h*0.92f)) }
}
