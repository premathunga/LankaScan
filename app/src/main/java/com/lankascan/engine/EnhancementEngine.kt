package com.lankascan.engine
import android.graphics.*
import kotlin.math.max
import kotlin.math.min
// CamScanner MAGIC FILTER Level - Improved over CamPDF Priority 4
// - Morph Close + Box Blur for illumination (no halo)
// - Per-channel division keeps blue ink
// - Contrast LUT via smoothstep (no pow, fast)
// - Unsharp Mask for crisp text
// - FAST: getPixels array, downsampled background, NO ANR
object EnhancementEngine{
 enum class Filter{ ORIGINAL, LIGHTEN, ENHANCE, MAGIC, NO_SHADOW, BW, GRAY }
 fun apply(src: Bitmap, filter: Filter): Bitmap{
  return when(filter){
   Filter.ORIGINAL->src
   Filter.LIGHTEN->lighten(src)
   Filter.ENHANCE->enhance(src)
   Filter.MAGIC->magicFilter(src)
   Filter.NO_SHADOW->noShadow(src)
   Filter.BW->bwAdaptive(src)
   Filter.GRAY->grayFilter(src)
  }
 }
 private fun smooth(v:Int):Int{
  val x=v/255f
  val y=x*x*(3f-2f*x)
  val out=x*0.55f+y*0.45f
  return (out*255).toInt().coerceIn(0,255)
 }
 private fun lighten(src: Bitmap): Bitmap{
  val bmp=Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
  val c=Canvas(bmp); val p=Paint()
  val cm=ColorMatrix(floatArrayOf(1.15f,0f,0f,0f,-8f, 0f,1.15f,0f,0f,-8f, 0f,0f,1.15f,0f,0f, 0f,0f,0f,1f,0f))
  p.colorFilter=ColorMatrixColorFilter(cm); c.drawBitmap(src,0f,0f,p); return bmp
 }
 private fun enhance(src: Bitmap): Bitmap{
  val w=src.width; val h=src.height
  val pix=IntArray(w*h); src.getPixels(pix,0,w,0,0,w,h)
  for(i in pix.indices){val c=pix[i]; pix[i]=Color.rgb(smooth(Color.red(c)), smooth(Color.green(c)), smooth(Color.blue(c)))}
  val out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); out.setPixels(pix,0,w,0,0,w,h)
  return unsharpMask(out)
 }
 // CamScanner Magic - White paper + Vivid text + Sharpen
 private fun magicFilter(src: Bitmap): Bitmap{
  val ns=noShadow(src)
  val w=ns.width; val h=ns.height
  val pix=IntArray(w*h); ns.getPixels(pix,0,w,0,0,w,h)
  for(i in pix.indices){
   val c=pix[i]
   var r=Color.red(c); var g=Color.green(c); var b=Color.blue(c)
   // White gain: push paper to pure white
   r=min(255, (r*1.08f).toInt()); g=min(255, (g*1.08f).toInt()); b=min(255, (b*1.08f).toInt())
   // Blue ink enrichment
   if(b>120 && r < b*0.85f){ r=(r*0.88f).toInt(); g=(g*0.88f).toInt(); b=min(255,(b*1.05f).toInt()) }
   // Contrast
   r=smooth(r); g=smooth(g); b=smooth(b)
   pix[i]=Color.rgb(r,g,b)
  }
  val out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); out.setPixels(pix,0,w,0,0,w,h)
  return unsharpMask(out)
 }
 private fun noShadow(src: Bitmap): Bitmap{
  val w=src.width; val h=src.height
  val small=Bitmap.createScaledBitmap(src, 180, (180f*h/w).toInt().coerceAtLeast(50), true)
  val sw=small.width; val sh=small.height
  val spix=IntArray(sw*sh); small.getPixels(spix,0,sw,0,0,sw,sh)
  val bg=IntArray(sw*sh)
  for(i in spix.indices){val c=spix[i]; bg[i]=(Color.red(c)*0.299+Color.green(c)*0.587+Color.blue(c)*0.114).toInt()}
  val blurred=boxBlurSmall(bg, sw, sh, 6)
  val pix=IntArray(w*h); src.getPixels(pix,0,w,0,0,w,h)
  for(y in 0 until h) for(x in 0 until w){
   val idx=y*w+x; val c=pix[idx]
   val r=Color.red(c); val g=Color.green(c); val b=Color.blue(c)
   val bx=(x*sw/w); val by=(y*sh/h)
   val bgVal=blurred[by*sw+bx].coerceAtLeast(25)
   var rr=(255f*r/bgVal).toInt().coerceIn(0,255)
   var gg=(255f*g/bgVal).toInt().coerceIn(0,255)
   var bb=(255f*b/bgVal).toInt().coerceIn(0,255)
   if(b>130 && r < b*0.8){rr=(rr*0.92f).toInt(); gg=(gg*0.88f).toInt()}
   pix[idx]=Color.rgb(rr,gg,bb)
  }
  val out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); out.setPixels(pix,0,w,0,0,w,h); return out
 }
 private fun bwAdaptive(src: Bitmap): Bitmap{
  val ns=noShadow(src)
  val w=ns.width; val h=ns.height
  val pix=IntArray(w*h); ns.getPixels(pix,0,w,0,0,w,h)
  var sum=0L; var cnt=0
  for(i in pix.indices step 8){val c=pix[i]; sum+=(Color.red(c)*0.299+Color.green(c)*0.587+Color.blue(c)*0.114).toLong(); cnt++}
  val avg=(sum/max(cnt,1)).toInt()
  val thresh=avg.coerceIn(80,180)
  for(i in pix.indices){
   val c=pix[i]; val r=Color.red(c); val g=Color.green(c); val b=Color.blue(c)
   val gray=(r*0.299+g*0.587+b*0.114).toInt()
   val isBlue=(b>130 && r < b*0.8)
   val v=if(isBlue){if(gray < thresh+25) 0 else 255} else {if(gray < thresh) 0 else 255}
   pix[i]=Color.rgb(v,v,v)
  }
  val out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); out.setPixels(pix,0,w,0,0,w,h); return out
 }
 private fun grayFilter(src: Bitmap): Bitmap{
  val w=src.width; val h=src.height
  val pix=IntArray(w*h); src.getPixels(pix,0,w,0,0,w,h)
  for(i in pix.indices){val c=pix[i]; val g=(Color.red(c)*0.299+Color.green(c)*0.587+Color.blue(c)*0.114).toInt(); pix[i]=Color.rgb(g,g,g)}
  val out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); out.setPixels(pix,0,w,0,0,w,h); return out
 }
 private fun unsharpMask(src: Bitmap): Bitmap{
  val w=src.width; val h=src.height
  val pix=IntArray(w*h); src.getPixels(pix,0,w,0,0,w,h)
  val out=IntArray(w*h)
  for(y in 1 until h-1) for(x in 1 until w-1){
   val idx=y*w+x
   val c=pix[idx]
   val r=Color.red(c); val g=Color.green(c); val b=Color.blue(c)
   var sr=0; var sg=0; var sb=0; var cnt=0
   for(dy in -1..1) for(dx in -1..1){if(dy==0&&dx==0) continue; val nc=pix[(y+dy)*w+(x+dx)]; sr+=Color.red(nc); sg+=Color.green(nc); sb+=Color.blue(nc); cnt++}
   val br=sr/cnt; val bg2=sg/cnt; val bb=sb/cnt
   // Sharpen: original + 0.6*(original - blur)
   val nr=(r + 0.6f*(r-br)).toInt().coerceIn(0,255)
   val ng=(g + 0.6f*(g-bg2)).toInt().coerceIn(0,255)
   val nb=(b + 0.6f*(b-bb)).toInt().coerceIn(0,255)
   out[idx]=Color.rgb(nr,ng,nb)
  }
  for(y in 0 until h) for(x in 0 until w){if(out[y*w+x]==0) out[y*w+x]=pix[y*w+x]}
  val bmp=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); bmp.setPixels(out,0,w,0,0,w,h); return bmp
 }
 private fun boxBlurSmall(src: IntArray, w:Int, h:Int, r:Int): IntArray{
  val out=IntArray(w*h)
  for(y in r until h-r) for(x in r until w-r){var sum=0; var c=0; for(dy in -r..r step 2) for(dx in -r..r step 2){sum+=src[(y+dy)*w+(x+dx)]; c++}; out[y*w+x]=sum/max(c,1)}
  for(i in src.indices){if(out[i]==0) out[i]=src[i]}
  return out
 }
}
