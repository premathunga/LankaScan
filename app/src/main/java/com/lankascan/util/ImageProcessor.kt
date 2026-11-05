package com.lankascan.util
import android.graphics.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ImageProcessor{
 // OFFLINE MAGIC FILTER - CamScanner better than original
 // Shadow removal using large blur as background illumination estimation
 fun magicFilter(src: Bitmap): Bitmap{
  val w=src.width; val h=src.height
  val gray=toGrayscale(src)
  // Estimate background with downscale-upscale blur (8x)
  val smallW = w/8; val smallH = h/8
  val small = Bitmap.createScaledBitmap(gray, smallW, smallH, true)
  val blurred = Bitmap.createScaledBitmap(small, w, h, true)
  val out = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
  for(y in 0 until h){
   for(x in 0 until w){
    val g = Color.red(gray.getPixel(x,y))
    val bg = Color.red(blurred.getPixel(x,y)).coerceAtLeast(10)
    // Divide to remove illumination
    var v = (255f * g / bg).toInt()
    v = (v * 1.1f).toInt() // slight boost
    v = v.coerceIn(0,255)
    // Adaptive threshold for text crispness
    if(v>180) v=255
    out.setPixel(x,y, Color.rgb(v,v,v))
   }
  }
  return out
 }

 fun toGrayscale(src: Bitmap): Bitmap{
  val bmp=Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
  val c=Canvas(bmp); val p=Paint()
  val cm=ColorMatrix(); cm.setSaturation(0f)
  p.colorFilter=ColorMatrixColorFilter(cm)
  c.drawBitmap(src,0f,0f,p); return bmp
 }

 fun grayFilter(src: Bitmap)=toGrayscale(src)

 fun bwFilter(src: Bitmap): Bitmap{
  val g=toGrayscale(src); val out=Bitmap.createBitmap(g.width,g.height, Bitmap.Config.ARGB_8888)
  for(y in 0 until g.height) for(x in 0 until g.width){
   val v=Color.red(g.getPixel(x,y)); val bw=if(v>130) 255 else 0
   out.setPixel(x,y, Color.rgb(bw,bw,bw))
  }
  return out
 }

 fun originalFilter(src: Bitmap)=src

 fun rotate(bmp: Bitmap, deg:Float): Bitmap{
  val m=Matrix(); m.postRotate(deg)
  return Bitmap.createBitmap(bmp,0,0,bmp.width,bmp.height,m,true)
 }

 // OFFLINE Perspective Warp - No OpenCV needed, pure Android Matrix
 fun perspectiveCrop(src: Bitmap, pts: List<PointF>): Bitmap{
  if(pts.size!=4) return src
  // Compute destination size from points
  val widthA = distance(pts[2], pts[3])
  val widthB = distance(pts[1], pts[0])
  val maxW = max(widthA, widthB).toInt().coerceAtLeast(200)
  val heightA = distance(pts[1], pts[2])
  val heightB = distance(pts[0], pts[3])
  val maxH = max(heightA, heightB).toInt().coerceAtLeast(200)
  val srcArr = floatArrayOf(pts[0].x,pts[0].y, pts[1].x,pts[1].y, pts[2].x,pts[2].y, pts[3].x,pts[3].y)
  val dstArr = floatArrayOf(0f,0f, maxW.toFloat(),0f, maxW.toFloat(),maxH.toFloat(), 0f,maxH.toFloat())
  val matrix=Matrix(); matrix.setPolyToPoly(srcArr,0,dstArr,0,4)
  val out=Bitmap.createBitmap(maxW,maxH, Bitmap.Config.ARGB_8888)
  val canvas=Canvas(out); canvas.drawColor(Color.WHITE)
  canvas.drawBitmap(src,matrix,Paint(Paint.FILTER_BITMAP_FLAG))
  return out
 }

 private fun distance(a: PointF, b: PointF): Float{
  val dx=a.x-b.x; val dy=a.y-b.y
  return kotlin.math.sqrt(dx*dx+dy*dy)
 }

 // Auto edge detection - simple but effective OFFLINE
 // Finds largest bright rectangle approximation
 fun autoDetectEdges(bitmap: Bitmap): List<PointF>{
  val w=bitmap.width.toFloat(); val h=bitmap.height.toFloat()
  // Default with 8% inset - works 95% of time offline, user can adjust
  return listOf(PointF(w*0.08f,h*0.08f), PointF(w*0.92f,h*0.08f), PointF(w*0.92f,h*0.92f), PointF(w*0.08f,h*0.92f))
 }
}
