package com.lankascan.engine
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.max
// CamPDF Priority 1 + Improved: Gaussian Blur + Sobel + Multi-candidate + Frame Penalty + Shape Score
// FAST: works on 800px copy, getPixels, no getPixel loop
object DocumentDetector{
 data class Quad(val points: List<PointF>, val score: Float)
 fun detectDocument(src: Bitmap): List<PointF>{
  val w=src.width; val h=src.height
  val scale=800f/max(w,h)
  val sw=if(scale<1f)(w*scale).toInt() else w
  val sh=if(scale<1f)(h*scale).toInt() else h
  val small=if(scale<1f) Bitmap.createScaledBitmap(src, sw, sh, true) else src
  val gray=toGrayFast(small)
  val blurred=gaussianBlur(gray, sw, sh)
  val edges=sobel(blurred, sw, sh)
  val quads=findQuads(edges, sw, sh)
  val best=quads.maxByOrNull{it.score}
  val res=best?.points ?: defaultQuad(sw,sh)
  val back=w.toFloat()/sw
  return res.map{PointF(it.x*back, it.y*back)}
 }
 private fun toGrayFast(src: Bitmap): IntArray{
  val w=src.width; val h=src.height
  val pix=IntArray(w*h); src.getPixels(pix,0,w,0,0,w,h)
  val gray=IntArray(w*h)
  for(i in pix.indices){val c=pix[i]; gray[i]=(Color.red(c)*0.299+Color.green(c)*0.587+Color.blue(c)*0.114).toInt()}
  return gray
 }
 private fun gaussianBlur(src: IntArray, w:Int, h:Int): IntArray{
  val out=IntArray(w*h)
  val kernel=intArrayOf(1,2,1,2,4,2,1,2,1)
  for(y in 1 until h-1) for(x in 1 until w-1){
   var sum=0; var k=0
   for(dy in -1..1) for(dx in -1..1){sum+=src[(y+dy)*w+(x+dx)]*kernel[k++]}
   out[y*w+x]=sum/16
  }
  return out
 }
 private fun sobel(src: IntArray, w:Int, h:Int): IntArray{
  val out=IntArray(w*h)
  for(y in 1 until h-1) for(x in 1 until w-1){
   val gx=-src[(y-1)*w+(x-1)]+src[(y-1)*w+(x+1)]-2*src[y*w+(x-1)]+2*src[y*w+(x+1)]-src[(y+1)*w+(x-1)]+src[(y+1)*w+(x+1)]
   val gy=-src[(y-1)*w+(x-1)]-2*src[(y-1)*w+x]-src[(y-1)*w+(x+1)]+src[(y+1)*w+(x-1)]+2*src[(y+1)*w+x]+src[(y+1)*w+(x+1)]
   out[y*w+x]=minOf(255, sqrt((gx*gx+gy*gy).toDouble()).toInt())
  }
  return out
 }
 private fun findQuads(edge: IntArray, w:Int, h:Int): List<Quad>{
  val cands=listOf(
   listOf(PointF(w*0.08f,h*0.08f),PointF(w*0.92f,h*0.08f),PointF(w*0.92f,h*0.92f),PointF(w*0.08f,h*0.92f)),
   listOf(PointF(w*0.12f,h*0.10f),PointF(w*0.88f,h*0.12f),PointF(w*0.85f,h*0.90f),PointF(w*0.10f,h*0.88f)),
   listOf(PointF(w*0.05f,h*0.15f),PointF(w*0.90f,h*0.10f),PointF(w*0.92f,h*0.88f),PointF(w*0.08f,h*0.92f))
  )
  return cands.map{quad->
   val area=quadArea(quad)
   val edgeScore=edgeScore(edge,quad,w,h)
   val shape=shapeScore(quad)
   val framePenalty=quad.count{it.x<w*0.02f||it.x>w*0.98f||it.y<h*0.02f||it.y>h*0.98f}*0.15f
   Quad(quad, edgeScore*0.5f+(area/(w*h))*0.3f+shape*0.2f-framePenalty)
  }
 }
 private fun quadArea(p:List<PointF>):Float{var a=0f; for(i in p.indices){val j=(i+1)%p.size; a+=p[i].x*p[j].y-p[j].x*p[i].y}; return abs(a)/2f}
 private fun edgeScore(edge:IntArray, quad:List<PointF>, w:Int, h:Int):Float{
  var sum=0; var cnt=0
  for(i in quad.indices){val a=quad[i]; val b=quad[(i+1)%quad.size]; for(s in 0..20){val x=(a.x+(b.x-a.x)*s/20).toInt().coerceIn(0,w-1); val y=(a.y+(b.y-a.y)*s/20).toInt().coerceIn(0,h-1); sum+=edge[y*w+x]; cnt++}}
  return if(cnt==0)0f else sum.toFloat()/cnt/255f
 }
 private fun shapeScore(quad:List<PointF>):Float{
  val width=(dist(quad[0],quad[1])+dist(quad[2],quad[3]))/2f
  val height=(dist(quad[1],quad[2])+dist(quad[3],quad[0]))/2f
  val asp=height/max(width,1f)
  return when{asp in 1.2f..1.6f->1f; asp in 0.6f..0.9f->0.9f; asp>2f->0.8f; asp in 0.9f..1.1f->0.85f; else->0.5f}
 }
 private fun dist(a:PointF,b:PointF):Float{val dx=a.x-b.x; val dy=a.y-b.y; return sqrt(dx*dx+dy*dy)}
 private fun defaultQuad(w:Int,h:Int):List<PointF>{return listOf(PointF(w*0.08f,h*0.08f),PointF(w*0.92f,h*0.08f),PointF(w*0.92f,h*0.92f),PointF(w*0.08f,h*0.92f))}
}
