package com.lankascan.util
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {
 fun createPdf(context: Context, bitmaps: List<Bitmap>, fileName: String, quality: Int = 80): File {
  val doc = PdfDocument()
  bitmaps.forEachIndexed { i, bmp ->
   val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i+1).create()
   val page = doc.startPage(pageInfo)
   page.canvas.drawBitmap(bmp, 0f, 0f, null)
   doc.finishPage(page)
  }
  val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
  if(!dir.exists()) dir.mkdirs()
  val safeName = if(fileName.endsWith(".pdf")) fileName else "$fileName.pdf"
  val outFile = File(dir, safeName)
  FileOutputStream(outFile).use { doc.writeTo(it) }
  doc.close()
  return outFile
 }
}
