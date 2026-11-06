package com.lankascan.ui.screens
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lankascan.data.ScanSession
import com.lankascan.util.PdfGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ExportScreen(onDone:()->Unit){
 val context=LocalContext.current
 var fileName by remember{mutableStateOf(SimpleDateFormat("Scan_yyyyMMdd_HHmm", Locale.US).format(Date()))}
 var quality by remember{mutableStateOf("Balanced")}
 var created by remember{mutableStateOf<File?>(null)}
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
  Text(if(created==null) "Export A4 PDF • OFFLINE" else "PDF Created!", color=Color.White, style=MaterialTheme.typography.headlineMedium)
  Spacer(Modifier.height(16.dp))
  Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
   Column(Modifier.padding(16.dp)){
    OutlinedTextField(value=fileName, onValueChange={fileName=it}, label={Text("File name")}, modifier=Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Text("Quality • A4 Standardized", color=Color.White)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp), modifier=Modifier.padding(top=8.dp)){
     listOf("High","Balanced","Compact").forEach{ q-> FilterChip(onClick={quality=q}, label={Text(q)}, selected=quality==q) }
    }
    Text("Pages: ${ScanSession.pages.size} • Documents/LankaScan/ • Engine: MorphClose+BlueInk+ContrastLUT", color=Color.Gray, style=MaterialTheme.typography.bodySmall)
   }
  }
  Spacer(Modifier.height(16.dp))
  if(created==null){
   Button(onClick={ try{ val f=PdfGenerator.createPdf(ScanSession.pages.toList(), fileName, quality); created=f; Toast.makeText(context, "Saved: ${f.name}", Toast.LENGTH_LONG).show() }catch(e:Exception){} }, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4ADE80), contentColor=Color.Black)){Text("Save PDF")}
  } else {
   Text(created!!.absolutePath, color=Color.Gray, style=MaterialTheme.typography.bodySmall)
   Spacer(Modifier.height(12.dp))
   Button(onClick={onDone()}, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp)){Text("Back to Home")}
  }
 }
}
