package com.lankascan.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(){
 val context = LocalContext.current
 var scannedBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
 var pdfFile by remember { mutableStateOf<File?>(null) }
 var isProcessing by remember { mutableStateOf(false) }

 val docDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
 val existingPdfs = remember { mutableStateOf(listOf<File>()) }
 LaunchedEffect(Unit){
  if(docDir.exists()) existingPdfs.value = docDir.listFiles()?.filter{ it.extension=="pdf" }?.sortedByDescending{ it.lastModified() } ?: emptyList()
 }

 // GOOGLE ML KIT DOCUMENT SCANNER - This is BETTER than CamScanner
 val scannerOptions = remember {
  GmsDocumentScannerOptions.Builder()
   .setGalleryImportAllowed(true)
   .setPageLimit(20)
   .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
   .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
   .build()
 }
 val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }

 val scannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){ result ->
  if(result.resultCode == android.app.Activity.RESULT_OK){
   val res = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
   res?.pages?.let { pages ->
    isProcessing = true
    // Convert pages to bitmaps
    val bitmaps = mutableListOf<Bitmap>()
    pages.forEach { page ->
     try{
      val bmp = androidx.core.graphics.drawable.toBitmap(context.contentResolver.loadThumbnail(page.imageUri, android.util.Size(1200,1600), null))
      bitmaps.add(bmp)
     } catch(e:Exception){
      // fallback load via input stream
      try{
       val input = context.contentResolver.openInputStream(page.imageUri)
       val bmp = android.graphics.BitmapFactory.decodeStream(input)
       input?.close()
       if(bmp!=null) bitmaps.add(bmp)
      }catch(_:Exception){}
     }
    }
    scannedBitmaps = bitmaps
    // Save PDF if Google already gave PDF
    res.pdf?.let { pdf ->
     try{
      val outFile = File(docDir, "Scan_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.pdf")
      if(!docDir.exists()) docDir.mkdirs()
      context.contentResolver.openInputStream(pdf.uri)?.use { ins ->
       FileOutputStream(outFile).use { outs -> ins.copyTo(outs) }
      }
      pdfFile = outFile
      existingPdfs.value = docDir.listFiles()?.filter{ it.extension=="pdf" }?.sortedByDescending{ it.lastModified() } ?: emptyList()
      Toast.makeText(context, "PDF Saved: ${outFile.name}", Toast.LENGTH_LONG).show()
     }catch(e:Exception){}
    }
    isProcessing = false
   }
  }
 }

 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
   Column{ Text("LankaScan", style=MaterialTheme.typography.headlineMedium, color=Color.White); Text("ULTIMATE • Better than CamScanner", style=MaterialTheme.typography.labelSmall, color=Color(0xFF3B82F6)) }
   Icon(Icons.Default.Verified, null, tint=Color(0xFF3B82F6))
  }
  Spacer(Modifier.height(16.dp))

  // SCANNED PREVIEW
  if(scannedBitmaps.isNotEmpty()){
   Card(shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E)), modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(12.dp)){
     Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween){ Text("${scannedBitmaps.size} pages scanned", color=Color.White); TextButton(onClick={ scannedBitmaps=emptyList(); pdfFile=null }){ Text("Clear") } }
     Spacer(Modifier.height(8.dp))
     LazyRow(horizontalArrangement=Arrangement.spacedBy(10.dp)){
      itemsIndexed(scannedBitmaps){ _, bmp -> Image(bitmap=bmp.asImageBitmap(), contentDescription=null, modifier=Modifier.size(100.dp).background(Color.White, RoundedCornerShape(8.dp))) }
     }
     Spacer(Modifier.height(12.dp))
     Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
      Button(onClick={
       val file = createPdfFromBitmaps(context, scannedBitmaps, "LankaScan_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}")
       pdfFile = file
       existingPdfs.value = docDir.listFiles()?.filter{ it.extension=="pdf" }?.sortedByDescending{ it.lastModified() } ?: emptyList()
       Toast.makeText(context, "Saved to Documents/LankaScan/${file.name}", Toast.LENGTH_LONG).show()
      }, modifier=Modifier.weight(1f)){ Icon(Icons.Default.PictureAsPdf, null); Spacer(Modifier.width(6.dp)); Text("Save PDF") }
      if(pdfFile!=null){ FilledTonalButton(onClick={ /* share */ }, modifier=Modifier.weight(1f)){ Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("Share") } }
     }
    }
   }
   Spacer(Modifier.height(16.dp))
  }

  // DOCUMENT LIST
  Text("Your Documents", color=Color.White, style=MaterialTheme.typography.titleMedium)
  Spacer(Modifier.height(10.dp))
  if(existingPdfs.value.isEmpty() && scannedBitmaps.isEmpty()){
   Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Icon(Icons.Default.DocumentScanner, null, tint=Color.Gray, modifier=Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No documents yet", color=Color.White); Text("Tap Scan to start with Google AI", color=Color.Gray, style=MaterialTheme.typography.bodySmall) } }
  } else {
   LazyColumn(modifier=Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(10.dp)){
    items(existingPdfs.value){ file ->
     Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
      Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment=Alignment.CenterVertically){
       Icon(Icons.Default.PictureAsPdf, null, tint=Color(0xFF3B82F6), modifier=Modifier.size(40.dp))
       Spacer(Modifier.width(12.dp))
       Column(Modifier.weight(1f)){ Text(file.nameWithoutExtension, color=Color.White, maxLines=1); Text("${file.length()/1024} kB • ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(file.lastModified()))}", color=Color.Gray, style=MaterialTheme.typography.bodySmall) }
       IconButton(onClick={}){ Icon(Icons.Default.MoreVert, null, tint=Color.Gray) }
      }
     }
    }
   }
  }

  Spacer(Modifier.height(12.dp))
  // BIG SCAN BUTTON - GOOGLE AI
  Button(
   onClick={
    scanner.getStartScanIntent(context).addOnSuccessListener { intentSender ->
     scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
    }.addOnFailureListener {
     Toast.makeText(context, "Scanner not available: ${it.message}", Toast.LENGTH_SHORT).show()
    }
   },
   modifier=Modifier.fillMaxWidth().height(64.dp),
   shape=RoundedCornerShape(32.dp),
   colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF3B82F6))
  ){
   if(isProcessing) CircularProgressIndicator(color=Color.White, modifier=Modifier.size(20.dp)) else Icon(Icons.Default.DocumentScanner, null, modifier=Modifier.size(28.dp))
   Spacer(Modifier.width(12.dp))
   Column(horizontalAlignment=Alignment.Start){
    Text("Scan with Google AI", style=MaterialTheme.typography.titleMedium)
    Text("Auto crop • Shadow removal • HD", style=MaterialTheme.typography.labelSmall)
   }
  }
  Spacer(Modifier.height(8.dp))
  Text("Powered by ML Kit Document Scanner • Offline • No Ads • 100% Open Source", color=Color.Gray, style=MaterialTheme.typography.labelSmall, modifier=Modifier.align(Alignment.CenterHorizontally))
 }
}

fun createPdfFromBitmaps(context: Context, bitmaps: List<Bitmap>, name: String): File {
 val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
 if(!dir.exists()) dir.mkdirs()
 val doc = PdfDocument()
 bitmaps.forEachIndexed { i, bmp ->
  val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i+1).create()
  val page = doc.startPage(pageInfo)
  page.canvas.drawBitmap(bmp, 0f, 0f, null)
  doc.finishPage(page)
 }
 val file = File(dir, if(name.endsWith(".pdf")) name else "$name.pdf")
 FileOutputStream(file).use { doc.writeTo(it) }
 doc.close()
 return file
}
