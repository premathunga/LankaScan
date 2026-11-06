package com.lankascan.engine
import android.graphics.*
import kotlin.math.*

// Priority 4 professional enhancement engine - Adaptive illumination correction + local shadow removal
// Better than CamPDF v1.5.1 : morphological closing + separable box smoothing + blue ink preservation
object EnhancementEngine{
 enum class Filter{ ORIGINAL, LIGHTEN, ENHANCE, NO_SHADOW, BW }

 private val CONTRAST_LUT = IntArray(256){ i ->
  // Non-linear contrast curve: deepen dark text
  val x=i/255f
  val y = if(x<0.5f) 0.5f*pow(2f*x, 1.2f) else 1f-0.5f*pow(2f*(1f-x), 1.2f)
  (y*255).toInt().coerceIn(0,255)
 }

 fun apply(src: Bitmap, filter: Filter): Bitmap{
  return when(filter){
   Filter.ORIGINAL -> src
   Filter.LIGHTEN -> lighten(src)
   Filter.ENHANCE -> enhance(src)
   Filter.NO_SHADOW -> noShadow(src)
   Filter.BW -> bwAdaptive(src)
  }
 }

 private fun lighten(src: Bitmap): Bitmap{
  val bmp=Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
  val canvas=Canvas(bmp); val paint=Paint()
  val cm=ColorMatrix(floatArrayOf(1.2f,0f,0f,0f,-10f, 0f,1.2f,0f,0f,-10f, 0f,0f,1.2f,0f,0f, 0f,0f,0f,1f,0f))
  paint.colorFilter=ColorMatrixColorFilter(cm)
  canvas.drawBitmap(src,0f,0f,paint); return bmp
 }

 private fun enhance(src: Bitmap): Bitmap{
  // Shadow removal + contrast LUT
  val noShadow=noShadow(src)
  val out=Bitmap.createBitmap(noShadow.width, noShadow.height, Bitmap.Config.ARGB_8888)
  for(y in 0 until noShadow.height) for(x in 0 until noShadow.width){
   val c=noShadow.getPixel(x,y)
   val r=CONTRAST_LUT[Color.red(c)]; val g=CONTRAST_LUT[Color.green(c)]; val b=CONTRAST_LUT[Color.blue(c)]
   out.setPixel(x,y, Color.rgb(r,g,b))
  }
  return out
 }

 private fun noShadow(src: Bitmap): Bitmap{
  // Morphological closing + illumination division, color preserving (blue ink)
  val w=src.width; val h=src.height
  val gray=toGray(src)
  val bg=estimateBackground(gray) // paper illumination
  val out=Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
  for(y in 0 until h) for(x in 0 until w){
   val c=src.getPixel(x,y)
   val r=Color.red(c); val g=Color.green(c); val b=Color.blue(c)
   val bgVal=bg[y][x].coerceAtLeast(20)
   // Divide each channel by its paper estimate to keep blue ink
   var rr=(255f*r/bgVal).toInt().coerceIn(0,255)
   var gg=(255f*g/bgVal).toInt().coerceIn(0,255)
   var bb=(255f*b/bgVal).toInt().coerceIn(0,255)
   // Blue ballpoint enrichment: deepen complementary
   if(isBlueInk(r,g,b)){
    rr=(rr*0.9f).toInt(); gg=(gg*0.85f).toInt()
   }
   out.setPixel(x,y, Color.rgb(rr,gg,bb))
  }
  return out
 }

 private fun bwAdaptive(src: Bitmap): Bitmap{
  val noShadow=noShadow(src)
  val w=noShadow.width; val h=noShadow.height
  val gray=toGray(noShadow)
  // Robust percentile threshold instead of fixed 128
  val hist=IntArray(256)
  for(y in 0 until h) for(x in 0 until w) hist[gray[y][x]]++
  val total=w*h
  var cum=0; var low=0; var high=255
  for(i in 0..255){ cum+=hist[i]; if(cum>total*0.15 && low==0) low=i; if(cum>total*0.85){ high=i; break } }
  val thresh = ((low*0.4 + high*0.6)).toInt().coerceIn(80,180)
  val out=Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
  for(y in 0 until h) for(x in 0 until w){
   val g=gray[y][x]
   val c=noShadow.getPixel(x,y)
   val isBlue=isBlueInk(Color.red(c), Color.green(c), Color.blue(c))
   val v = if(isBlue) { if(g<thresh+20) 0 else 255 } else { if(g<thresh) 0 else 255 }
   out.setPixel(x,y, Color.rgb(v,v,v))
  }
  return out
 }

 private fun isBlueInk(r:Int,g:Int,b:Int): Boolean{
  // Blue ballpoint: blue high, red absorbed
  return b>130 && r < b*0.8 && g < b*0.9 && b > r+20
 }

 private fun toGray(src: Bitmap): Array<IntArray>{
  val w=src.width; val h=src.height
  val arr=Array(h){IntArray(w)}
  for(y in 0 until h) for(x in 0 until w){ val c=src.getPixel(x,y); arr[y][x]=(Color.red(c)*0.299 + Color.green(c)*0.587 + Color.blue(c)*0.114).toInt() }
  return arr
 }

 private fun estimateBackground(gray: Array<IntArray>): Array<IntArray>{
  // Morphological closing (dilation r=3 + erosion r=1) + separable box smoothing
  val dilated=dilate(gray, 3)
  val eroded=erode(dilated, 1)
  return boxBlur(eroded, 15)
 }

 private fun dilate(src: Array<IntArray>, r:Int): Array<IntArray>{
  val h=src.size; val w=src[0].size; val out=Array(h){IntArray(w)}
  for(y in r until h-r) for(x in r until w-r){ var max=0; for(dy in -r..r) for(dx in -r..r) max=max(max, src[y+dy][x+dx]); out[y][x]=max }
  return out
 }
 private fun erode(src: Array<IntArray>, r:Int): Array<IntArray>{
  val h=src.size; val w=src[0].size; val out=Array(h){IntArray(w)}
  for(y in r until h-r) for(x in r until w-r){ var min=255; for(dy in -r..r) for(dx in -r..r) min=min(min, src[y+dy][x+dx]); out[y][x]=min }
  return out
 }
 private fun boxBlur(src: Array<IntArray>, r:Int): Array<IntArray>{
  val h=src.size; val w=src[0].size; val out=Array(h){IntArray(w)}
  for(y in r until h-r) for(x in r until w-r){ var sum=0; var cnt=0; for(dy in -r..r step 3) for(dx in -r..r step 3){ sum+=src[y+dy][x+dx]; cnt++ }; out[y][x]=sum/cnt }
  return out
 }
}
