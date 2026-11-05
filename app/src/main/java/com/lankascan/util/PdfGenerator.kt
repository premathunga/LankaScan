package com.lankascan.util
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
object PdfGenerator{
 fun createPdf(bitmaps: List<Bitmap>, name: String): File{
  val dir=File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
  if(!dir.exists()) dir.mkdirs()
  val doc=PdfDocument()
  bitmaps.forEachIndexed{i,bmp->
   val info=PdfDocument.PageInfo.Builder(bmp.width,bmp.height,i+1).create()
   val page=doc.startPage(info); page.canvas.drawBitmap(bmp,0f,0f,null); doc.finishPage(page)
  }
  val file=File(dir, if(name.endsWith(".pdf")) name else "$name.pdf")
  FileOutputStream(file).use{doc.writeTo(it)}; doc.close(); return file
 }
}
