package com.lankascan.util
import android.graphics.*
import com.lankascan.engine.EnhancementEngine
import kotlin.math.max
object ImageProcessor{
 fun applyFilter(src: Bitmap, filter: EnhancementEngine.Filter): Bitmap = EnhancementEngine.apply(src, filter)
 fun perspectiveCrop(src: Bitmap, pts: List<PointF>): Bitmap{
  if(pts.size!=4) return src
  val maxW= max(distance(pts[2],pts[3]), distance(pts[1],pts[0])).toInt().coerceAtLeast(200)
  val maxH= max(distance(pts[1],pts[2]), distance(pts[0],pts[3])).toInt().coerceAtLeast(200)
  val srcArr=floatArrayOf(pts[0].x,pts[0].y, pts[1].x,pts[1].y, pts[2].x,pts[2].y, pts[3].x,pts[3].y)
  val dstArr=floatArrayOf(0f,0f, maxW.toFloat(),0f, maxW.toFloat(),maxH.toFloat(), 0f,maxH.toFloat())
  val matrix=Matrix(); matrix.setPolyToPoly(srcArr,0,dstArr,0,4)
  val out=Bitmap.createBitmap(maxW,maxH, Bitmap.Config.ARGB_8888); val canvas=Canvas(out); canvas.drawColor(Color.WHITE); canvas.drawBitmap(src,matrix,Paint(Paint.FILTER_BITMAP_FLAG)); return out
 }
 fun rotate(bmp: Bitmap, deg:Float): Bitmap{ val m=Matrix(); m.postRotate(deg); return Bitmap.createBitmap(bmp,0,0,bmp.width,bmp.height,m,true) }
 private fun distance(a:PointF,b:PointF):Float{ val dx=a.x-b.x; val dy=a.y-b.y; return kotlin.math.sqrt(dx*dx+dy*dy) }
}
