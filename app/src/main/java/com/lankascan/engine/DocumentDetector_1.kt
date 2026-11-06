package com.lankascan.engine
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import kotlin.math.*

// Claude's CamPDF scan engine inspired: blur + Sobel + contour/hull quad fitting + document-shape plausibility score
object DocumentDetector{

 data class Quad(val points: List<PointF>, val score: Float)

 fun detectDocument(src: Bitmap): List<PointF>{
  val w=src.width; val h=src.height
  val gray=toGray(src)
  val blurred=boxBlur(gray, 3)
  val edges=sobel(blurred)
  // Find candidate quads by scanning edge density
  val quads=findQuads(edges, w, h)
  val best=quads.maxByOrNull{it.score}
  return best?.points ?: defaultQuad(w,h)
 }

 private fun toGray(src: Bitmap): Array<IntArray>{
  val w=src.width; val h=src.height
  val arr=Array(h){IntArray(w)}
  for(y in 0 until h) for(x in 0 until w){
   val c=src.getPixel(x,y); arr[y][x]=(Color.red(c)*0.299 + Color.green(c)*0.587 + Color.blue(c)*0.114).toInt()
  }
  return arr
 }

 private fun boxBlur(src: Array<IntArray>, r:Int): Array<IntArray>{
  val h=src.size; val w=src[0].size
  val out=Array(h){IntArray(w)}
  for(y in r until h-r) for(x in r until w-r){
   var sum=0; for(dy in -r..r) for(dx in -r..r) sum+=src[y+dy][x+dx]
   out[y][x]=sum/((2*r+1)*(2*r+1))
  }
  return out
 }

 private fun sobel(src: Array<IntArray>): Array<IntArray>{
  val h=src.size; val w=src[0].size
  val out=Array(h){IntArray(w)}
  val gx=arrayOf(intArrayOf(-1,0,1), intArrayOf(-2,0,2), intArrayOf(-1,0,1))
  val gy=arrayOf(intArrayOf(-1,-2,-1), intArrayOf(0,0,0), intArrayOf(1,2,1))
  for(y in 1 until h-1) for(x in 1 until w-1){
   var sx=0; var sy=0
   for(ky in -1..1) for(kx in -1..1){ sx+=src[y+ky][x+kx]*gx[ky+1][kx+1]; sy+=src[y+ky][x+kx]*gy[ky+1][kx+1] }
   out[y][x]=min(255, sqrt((sx*sx+sy*sy).toDouble()).toInt())
  }
  return out
 }

 private fun findQuads(edge: Array<IntArray>, w:Int, h:Int): List<Quad>{
  // Simplified: score based on edge strength near borders + area + shape plausibility
  // In real CamPDF they do contour + hull. Here we generate 3 candidates and score them.
  val candidates=listOf(
   listOf(PointF(w*0.08f,h*0.08f), PointF(w*0.92f,h*0.08f), PointF(w*0.92f,h*0.92f), PointF(w*0.08f,h*0.92f)),
   listOf(PointF(w*0.12f,h*0.10f), PointF(w*0.88f,h*0.12f), PointF(w*0.85f,h*0.90f), PointF(w*0.10f,h*0.88f)),
   listOf(PointF(w*0.05f,h*0.15f), PointF(w*0.90f,h*0.10f), PointF(w*0.92f,h*0.88f), PointF(w*0.08f,h*0.92f))
  )
  return candidates.map{ quad ->
   val area=quadArea(quad)
   val edgeScore=edgeScoreNearQuad(edge, quad)
   val shapeScore=shapePlausibility(quad, w, h) // portrait/landscape/receipt/square eligible
   val framePenalty=quad.count{ it.x<w*0.02f || it.x>w*0.98f || it.y<h*0.02f || it.y>h*0.98f } * 0.15f
   val score = edgeScore*0.5f + (area/(w*h))*0.3f + shapeScore*0.2f - framePenalty
   Quad(quad, score)
  }
 }

 private fun quadArea(pts: List<PointF>): Float{
  var a=0f; for(i in pts.indices){ val j=(i+1)%pts.size; a+=pts[i].x*pts[j].y - pts[j].x*pts[i].y }
  return abs(a)/2f
 }

 private fun edgeScoreNearQuad(edge: Array<IntArray>, quad: List<PointF>): Float{
  var sum=0; var cnt=0
  for(i in quad.indices){
   val a=quad[i]; val b=quad[(i+1)%quad.size]
   val steps=20
   for(s in 0..steps){
    val x=(a.x + (b.x-a.x)*s/steps).toInt().coerceIn(0, edge[0].size-1)
    val y=(a.y + (b.y-a.y)*s/steps).toInt().coerceIn(0, edge.size-1)
    sum+=edge[y][x]; cnt++
   }
  }
  return if(cnt==0) 0f else sum.toFloat()/cnt/255f
 }

 private fun shapePlausibility(quad: List<PointF>, w:Int, h:Int): Float{
  val width = (distance(quad[0],quad[1]) + distance(quad[2],quad[3]))/2f
  val height = (distance(quad[1],quad[2]) + distance(quad[3],quad[0]))/2f
  val aspect = height / max(width,1f)
  // Eligible: portrait 1.2-1.6, landscape 0.6-0.9, receipt >2.0, near-square 0.9-1.1
  return when{
   aspect in 1.2f..1.6f -> 1f
   aspect in 0.6f..0.9f -> 0.9f
   aspect >2f -> 0.8f
   aspect in 0.9f..1.1f -> 0.85f
   else -> 0.5f
  }
 }

 private fun distance(a:PointF,b:PointF):Float{ val dx=a.x-b.x; val dy=a.y-b.y; return sqrt(dx*dx+dy*dy) }
 private fun defaultQuad(w:Int,h:Int):List<PointF>{ return listOf(PointF(w*0.08f,h*0.08f), PointF(w*0.92f,h*0.08f), PointF(w*0.92f,h*0.92f), PointF(w*0.08f,h*0.92f)) }
}
