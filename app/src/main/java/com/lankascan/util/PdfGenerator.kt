package com.lankascan.util
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
object PdfGenerator{ fun createPdf(bitmaps: List<Bitmap>, outFile: File){ val doc=PdfDocument(); bitmaps.forEachIndexed{i,bmp-> val pageInfo=PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i+1).create(); val page=doc.startPage(pageInfo); page.canvas.drawBitmap(bmp, 0f, 0f, null); doc.finishPage(page) }; FileOutputStream(outFile).use{ doc.writeTo(it) }; doc.close() } }
