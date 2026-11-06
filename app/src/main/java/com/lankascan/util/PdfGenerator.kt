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
   // ISO A4 595x842 if near A4 aspect 1.33-1.5
   val aspect=bmp.height.toFloat()/bmp.width
   val pageW=if(aspect in 1.33f..1.5f) 595 else bmp.width
   val pageH=if(aspect in 1.33f..1.5f) 842 else bmp.height
   val info=PdfDocument.PageInfo.Builder(pageW, pageH, i+1).create()
   val page=doc.startPage(info)
   // Center keep proportions
   val scale=minOf(pageW.toFloat()/bmp.width, pageH.toFloat()/bmp.height)
   val dw=bmp.width*scale; val dh=bmp.height*scale
   val dx=(pageW-dw)/2; val dy=(pageH-dh)/2
   page.canvas.save(); page.canvas.translate(dx,dy); page.canvas.scale(scale,scale)
   page.canvas.drawBitmap(bmp,0f,0f,null); page.canvas.restore()
   doc.finishPage(page)
  }
  val file=File(dir, if(name.endsWith(".pdf")) name else "$name.pdf")
  FileOutputStream(file).use{doc.writeTo(it)}; doc.close(); return file
 }
}
