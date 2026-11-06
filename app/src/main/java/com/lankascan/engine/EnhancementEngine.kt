package com.lankascan.engine
import android.graphics.*
import kotlin.math.max
import kotlin.math.min

// Pro Enhancement: Morph Close + Box Smoothing + Blue Ink + Smooth Contrast (NO pow() to avoid build error)
object EnhancementEngine{
 enum class Filter{ ORIGINAL, LIGHTEN, ENHANCE, NO_SHADOW, BW }
 private fun smoothContrast(v:Int): Int{
  val x=v/255f
  // Smoothstep: x*x*(3-2x) - deepens dark text without pow
  val y = x*x*(3f - 2f*x)
  // Blend: 60% original + 40% smooth for natural look
  val out = x*0.6f + y*0.4f
  return (out*255).toInt().coerceIn(0,255)
 }
 fun apply(src: Bitmap, filter: Filter): Bitmap{
  return when(filter){ Filter.ORIGINAL -> src; Filter.LIGHTEN -> lighten(src); Filter.ENHANCE -> enhance(src); Filter.NO_SHADOW -> noShadow(src); Filter.BW -> bwAdaptive(src) }
 }
 private fun lighten(src: Bitmap): Bitmap{
  val bmp=Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
  val canvas=Canvas(bmp); val paint=Paint()
  val cm=ColorMatrix(floatArrayOf(1.2f,0f,0f,0f,-10f, 0f,1.2f,0f,0f,-10f, 0f,0f,1.2f,0f,0f, 0f,0f,0f,1f,0f))
  paint.colorFilter=ColorMatrixColorFilter(cm); canvas.drawBitmap(src,0f,0f,paint); return bmp
 }
 private fun enhance(src: Bitmap): Bitmap{
  val ns=noShadow(src)
  val out=Bitmap.createBitmap(ns.width, ns.height, Bitmap.Config.ARGB_8888)
  for(y in 0 until ns.height) for(x in 0 until ns.width){ val c=ns.getPixel(x,y); out.setPixel(x,y, Color.rgb(smoothContrast(Color.red(c)), smoothContrast(Color.green(c)), smoothContrast(Color.blue(c)))) }
  return out
 }
 private fun noShadow(src: Bitmap): Bitmap{
  val w=src.width; val h=src.height
  val gray=toGray(src); val bg=estimateBackground(gray)
  val out=Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
  for(y in 0 until h) for(x in 0 until w){
   val c=src.getPixel(x,y); val r=Color.red(c); val g=Color.green(c); val b=Color.blue(c)
   val bgVal=bg[y][x].coerceAtLeast(20)
   var rr=(255f*r/bgVal).toInt().coerceIn(0,255)
   var gg=(255f*g/bgVal).toInt().coerceIn(0,255)
   var bb=(255f*b/bgVal).toInt().coerceIn(0,255)
   if(isBlueInk(r,g,b)){ rr=(rr*0.9f).toInt(); gg=(gg*0.85f).toInt() }
   out.setPixel(x,y, Color.rgb(rr,gg,bb))
  }
  return out
 }
 private fun bwAdaptive(src: Bitmap): Bitmap{
  val ns=noShadow(src); val w=ns.width; val h=ns.height; val gray=toGray(ns)
  val hist=IntArray(256); for(y in 0 until h) for(x in 0 until w) hist[gray[y][x]]++
  val total=w*h; var cum=0; var low=0; var high=255
  for(i in 0..255){ cum+=hist[i]; if(cum>total*0.15 && low==0) low=i; if(cum>total*0.85){ high=i; break } }
  val thresh=((low*0.4 + high*0.6)).toInt().coerceIn(80,180)
  val out=Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
  for(y in 0 until h) for(x in 0 until w){ val g=gray[y][x]; val c=ns.getPixel(x,y); val isBlue=isBlueInk(Color.red(c), Color.green(c), Color.blue(c)); val v=if(isBlue){ if(g<thresh+20) 0 else 255 } else { if(g<thresh) 0 else 255 }; out.setPixel(x,y, Color.rgb(v,v,v)) }
  return out
 }
 private fun isBlueInk(r:Int,g:Int,b:Int): Boolean{ return b>130 && r < b*0.8 && g < b*0.9 && b > r+20 }
 private fun toGray(src: Bitmap): Array<IntArray>{ val w=src.width; val h=src.height; val arr=Array(h){IntArray(w)}; for(y in 0 until h) for(x in 0 until w){ val c=src.getPixel(x,y); arr[y][x]=(Color.red(c)*0.299 + Color.green(c)*0.587 + Color.blue(c)*0.114).toInt() }; return arr }
 private fun estimateBackground(gray: Array<IntArray>): Array<IntArray>{ val dilated=dilate(gray, 3); val eroded=erode(dilated, 1); return boxBlur(eroded, 15) }
 private fun dilate(src: Array<IntArray>, r:Int): Array<IntArray>{ val h=src.size; val w=src[0].size; val out=Array(h){IntArray(w)}; for(y in r until h-r) for(x in r until w-r){ var mx=0; for(dy in -r..r) for(dx in -r..r) mx=max(mx, src[y+dy][x+dx]); out[y][x]=mx }; return out }
 private fun erode(src: Array<IntArray>, r:Int): Array<IntArray>{ val h=src.size; val w=src[0].size; val out=Array(h){IntArray(w)}; for(y in r until h-r) for(x in r until w-r){ var mn=255; for(dy in -r..r) for(dx in -r..r) mn=min(mn, src[y+dy][x+dx]); out[y][x]=mn }; return out }
 private fun boxBlur(src: Array<IntArray>, r:Int): Array<IntArray>{ val h=src.size; val w=src[0].size; val out=Array(h){IntArray(w)}; for(y in r until h-r) for(x in r until w-r){ var sum=0; var cnt=0; for(dy in -r..r step 3) for(dx in -r..r step 3){ sum+=src[y+dy][x+dx]; cnt++ }; out[y][x]=sum/max(cnt,1) }; return out }
}
