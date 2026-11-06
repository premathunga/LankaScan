package com.lankascan.util
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
object PdfGenerator{
 fun createPdf(bitmaps: List<Bitmap>, name: String, quality: String="Balanced"): File{
  val dir=File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
  if(!dir.exists()) dir.mkdirs()
  val doc=PdfDocument()
  bitmaps.forEachIndexed{i,bmp->
   var useBmp=bmp
   if(quality=="Compact"){ val scale=0.6f; useBmp=Bitmap.createScaledBitmap(bmp, (bmp.width*scale).toInt().coerceAtLeast(200), (bmp.height*scale).toInt().coerceAtLeast(200), true) }
   else if(quality=="High"){ useBmp=bmp }
   val aspect=useBmp.height.toFloat()/useBmp.width
   val pageW=if(aspect in 1.33f..1.5f) 595 else useBmp.width
   val pageH=if(aspect in 1.33f..1.5f) 842 else useBmp.height
   val info=PdfDocument.PageInfo.Builder(pageW, pageH, i+1).create()
   val page=doc.startPage(info)
   val scale=minOf(pageW.toFloat()/useBmp.width, pageH.toFloat()/useBmp.height)
   val dw=useBmp.width*scale; val dh=useBmp.height*scale
   val dx=(pageW-dw)/2; val dy=(pageH-dh)/2
   page.canvas.save(); page.canvas.translate(dx,dy); page.canvas.scale(scale,scale)
   page.canvas.drawBitmap(useBmp,0f,0f,null); page.canvas.restore()
   doc.finishPage(page)
  }
  val file=File(dir, if(name.endsWith(".pdf")) name else "$name.pdf")
  FileOutputStream(file).use{doc.writeTo(it)}; doc.close(); return file
 }
 fun listPdfs(): List<File>{
  val dir=File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
  if(!dir.exists()) return emptyList()
  return dir.listFiles()?.filter{it.extension=="pdf"}?.sortedByDescending{it.lastModified()} ?: emptyList()
 }
}
