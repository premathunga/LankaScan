package com.lankascan.ui.screens
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.lankascan.data.ScanSession
import java.io.File

@Composable fun PdfViewerScreen(onBack:()->Unit){
 val path=ScanSession.currentPdfPath
 var bitmaps by remember{mutableStateOf(listOf<android.graphics.Bitmap>())}
 var error by remember{mutableStateOf<String?>(null)}
 LaunchedEffect(path){
  if(path==null){ error="No PDF selected"; return@LaunchedEffect }
  try{
   val file=File(path); val pfd=ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY); val renderer=PdfRenderer(pfd)
   val list=mutableListOf<android.graphics.Bitmap>()
   for(i in 0 until renderer.pageCount){ val page=renderer.openPage(i); val bmp=android.graphics.Bitmap.createBitmap(page.width, page.height, android.graphics.Bitmap.Config.ARGB_8888); page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); list.add(bmp); page.close() }
   renderer.close(); pfd.close(); bitmaps=list
  }catch(e:Exception){ error=e.message }
 }
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){ TextButton(onClick=onBack){Text("Back")}; Text("PDF Viewer", color=Color.White); Text("${bitmaps.size} pages", color=Color.Gray) }
  if(error!=null){ Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Text("Error: $error", color=Color.Red) } }
  else if(bitmaps.isEmpty()){ Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ CircularProgressIndicator(color=Color(0xFF4ADE80)) } }
  else { LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)){ items(bitmaps.size){ idx-> Card(Modifier.fillMaxWidth()){ Image(bitmap=bitmaps[idx].asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxWidth()) } } } }
 }
}
