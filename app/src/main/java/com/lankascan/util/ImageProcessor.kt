package com.lankascan.util
import android.graphics.*
import com.lankascan.engine.EnhancementEngine
import kotlin.math.max
object ImageProcessor{
 fun applyFilter(src: Bitmap, filter: EnhancementEngine.Filter): Bitmap = EnhancementEngine.apply(src, filter)
 fun downscale(src: Bitmap, maxDim:Int=1600): Bitmap{
  if(src.width<=maxDim && src.height<=maxDim) return src
  val s=maxDim.toFloat()/max(src.width, src.height)
  return Bitmap.createScaledBitmap(src, (src.width*s).toInt(), (src.height*s).toInt(), true)
 }
 fun perspectiveCrop(src: Bitmap, pts: List<PointF>): Bitmap{
  if(pts.size!=4) return src
  // Validate convexity + area (CamPDF Priority 3)
  val area=kotlin.math.abs((pts[1].x-pts[0].x)*(pts[3].y-pts[0].y)-(pts[3].x-pts[0].x)*(pts[1].y-pts[0].y))
  if(area < 10000) return src
  val maxW=max(dist(pts[2],pts[3]), dist(pts[1],pts[0])).toInt().coerceAtLeast(200)
  val maxH=max(dist(pts[1],pts[2]), dist(pts[0],pts[3])).toInt().coerceAtLeast(200)
  val srcArr=floatArrayOf(pts[0].x,pts[0].y, pts[1].x,pts[1].y, pts[2].x,pts[2].y, pts[3].x,pts[3].y)
  val dstArr=floatArrayOf(0f,0f, maxW.toFloat(),0f, maxW.toFloat(),maxH.toFloat(), 0f,maxH.toFloat())
  val matrix=Matrix(); matrix.setPolyToPoly(srcArr,0,dstArr,0,4)
  val out=Bitmap.createBitmap(maxW,maxH, Bitmap.Config.ARGB_8888)
  val canvas=Canvas(out); canvas.drawColor(Color.WHITE); canvas.drawBitmap(src,matrix,Paint(Paint.FILTER_BITMAP_FLAG))
  // Border trim: remove 1.5% leftover background strips (CamPDF v1.5.1)
  val trimX=(maxW*0.015f).toInt(); val trimY=(maxH*0.015f).toInt()
  return Bitmap.createBitmap(out, trimX, trimY, maxW-trimX*2, maxH-trimY*2)
 }
 fun rotate(bmp: Bitmap, deg:Float): Bitmap{val m=Matrix(); m.postRotate(deg); return Bitmap.createBitmap(bmp,0,0,bmp.width,bmp.height,m,true)}
 fun addSignature(page: Bitmap, sign: Bitmap): Bitmap{
  val out=page.copy(Bitmap.Config.ARGB_8888, true)
  val c=Canvas(out)
  val sw=(page.width*0.25f).toInt(); val sh=(sign.height*sw/sign.width)
  val scaled=Bitmap.createScaledBitmap(sign, sw, sh, true)
  c.drawBitmap(scaled, (page.width-sw-40).toFloat(), (page.height-sh-40).toFloat(), null)
  return out
 }
 private fun dist(a:PointF,b:PointF):Float{val dx=a.x-b.x; val dy=a.y-b.y; return kotlin.math.sqrt(dx*dx+dy*dy)}
}
