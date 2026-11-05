package com.lankascan.util
import android.graphics.*
import kotlin.math.max
import kotlin.math.min

object ImageProcessor {

 // Magic Filter - Shadow removal + Contrast boost (CamScanner style)
 fun magicFilter(src: Bitmap): Bitmap {
  val w = src.width
  val h = src.height
  val gray = toGrayscale(src)
  val blurred = fastBlur(gray, 15)
  val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(result)
  val paint = Paint()
  // Increase contrast and remove shadow: result = 255 * (gray / blurred) with clamp
  for(y in 0 until h step 1){
   for(x in 0 until w step 1){
    val g = Color.red(gray.getPixel(x,y))
    val b = Color.red(blurred.getPixel(x,y)).coerceAtLeast(1)
    var v = (255f * g / b).toInt()
    v = min(255, max(0, v))
    // boost contrast
    v = ((v - 128) * 1.2f + 128).toInt().coerceIn(0,255)
    result.setPixel(x,y, Color.rgb(v,v,v))
   }
  }
  return result
 }

 fun toGrayscale(src: Bitmap): Bitmap {
  val bmp = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
  val c = Canvas(bmp)
  val paint = Paint()
  val cm = ColorMatrix()
  cm.setSaturation(0f)
  paint.colorFilter = ColorMatrixColorFilter(cm)
  c.drawBitmap(src, 0f, 0f, paint)
  return bmp
 }

 fun blackWhite(src: Bitmap): Bitmap {
  val gray = toGrayscale(src)
  val out = Bitmap.createBitmap(gray.width, gray.height, Bitmap.Config.ARGB_8888)
  for(y in 0 until gray.height){
   for(x in 0 until gray.width){
    val v = Color.red(gray.getPixel(x,y))
    val bw = if(v>128) 255 else 0
    out.setPixel(x,y, Color.rgb(bw,bw,bw))
   }
  }
  return out
 }

 // Simple box blur approximation for shadow map
 private fun fastBlur(src: Bitmap, radius: Int): Bitmap {
  val w = src.width
  val h = src.height
  val smallW = w / 8
  val smallH = h / 8
  val small = Bitmap.createScaledBitmap(src, smallW, smallH, true)
  return Bitmap.createScaledBitmap(small, w, h, true)
 }

 // Perspective transform using 4 points
 fun perspectiveCrop(src: Bitmap, pts: List<PointF>): Bitmap {
  if(pts.size!=4) return src
  val w = max( (pts[0].x - pts[1].x).let{ kotlin.math.abs(it) }, (pts[2].x - pts[3].x).let{ kotlin.math.abs(it) } ).toInt().coerceAtLeast(200)
  val h = max( (pts[0].y - pts[3].y).let{ kotlin.math.abs(it) }, (pts[1].y - pts[2].y).let{ kotlin.math.abs(it) } ).toInt().coerceAtLeast(200)
  val dstW = max(w, h).coerceAtMost(src.width)
  val dstH = dstW
  // src points order: TL, TR, BR, BL
  val srcPts = floatArrayOf(pts[0].x, pts[0].y, pts[1].x, pts[1].y, pts[2].x, pts[2].y, pts[3].x, pts[3].y)
  val dstPts = floatArrayOf(0f,0f, dstW.toFloat(),0f, dstW.toFloat(), dstH.toFloat(), 0f, dstH.toFloat())
  val matrix = Matrix()
  matrix.setPolyToPoly(srcPts, 0, dstPts, 0, 4)
  val out = Bitmap.createBitmap(dstW, dstH, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(out)
  canvas.drawColor(Color.WHITE)
  canvas.drawBitmap(src, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
  return out
 }

 fun rotate(bmp: Bitmap, degrees: Float): Bitmap {
  val m = Matrix(); m.postRotate(degrees)
  return Bitmap.createBitmap(bmp, 0,0,bmp.width,bmp.height,m,true)
 }
}
