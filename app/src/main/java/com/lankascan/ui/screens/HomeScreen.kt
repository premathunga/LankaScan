package com.lankascan.ui.screens
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.lankascan.util.PdfGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HomeScreen(onScan:()->Unit, onOpenPdf:(String)->Unit, onSettings:()->Unit){
 val context=LocalContext.current
 var search by remember{mutableStateOf("")}
 var files by remember{mutableStateOf(listOf<File>())}
 var refresh by remember{mutableStateOf(0)}
 LaunchedEffect(refresh){ files=PdfGenerator.listPdfs() }
 val filtered=if(search.isBlank()) files else files.filter{it.name.contains(search, ignoreCase=true)}
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
   Column{ Text("LankaScan Pro", style=MaterialTheme.typography.headlineMedium, color=Color.White); Text("100% OFFLINE • No Ads • No Internet", style=MaterialTheme.typography.labelSmall, color=Color(0xFF4ADE80)) }
   TextButton(onClick=onSettings){Text("Settings", color=Color.White)}
  }
  Spacer(Modifier.height(12.dp))
  Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)){
   Card(Modifier.weight(1f), shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){ Column(Modifier.padding(12.dp)){ Text("${files.size}", color=Color.White, style=MaterialTheme.typography.headlineSmall); Text("Documents", color=Color.Gray, style=MaterialTheme.typography.labelSmall) } }
   Card(Modifier.weight(1f), shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){ Column(Modifier.padding(12.dp)){ Text("${files.sumOf{it.length()}/1024} KB", color=Color.White, style=MaterialTheme.typography.headlineSmall); Text("Total Size", color=Color.Gray, style=MaterialTheme.typography.labelSmall) } }
  }
  Spacer(Modifier.height(12.dp))
  OutlinedTextField(value=search, onValueChange={search=it}, placeholder={Text("Search documents...")}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp))
  Spacer(Modifier.height(12.dp))
  if(filtered.isEmpty()){
   Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("No documents yet", color=Color.Gray); Text("Tap Scan to create your first PDF", color=Color.Gray, style=MaterialTheme.typography.bodySmall) } }
  } else {
   LazyColumn(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(10.dp)){
    items(filtered){ file->
     Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E)), modifier=Modifier.fillMaxWidth().clickable{onOpenPdf(file.absolutePath)}){
      Row(Modifier.padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically){
       Column(Modifier.weight(1f)){ Text(file.nameWithoutExtension, color=Color.White); Text("${file.length()/1024} kB • ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))}", color=Color.Gray, style=MaterialTheme.typography.bodySmall) }
       Column(horizontalAlignment=Alignment.End){
        TextButton(onClick={ val uri=FileProvider.getUriForFile(context, context.packageName+".provider", file); val intent=Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); context.startActivity(Intent.createChooser(intent, "Share PDF")) }){Text("Share", color=Color(0xFF4ADE80))}
        TextButton(onClick={ file.delete(); refresh++ }){Text("Delete", color=Color.Red)}
       }
      }
     }
    }
   }
  }
  Spacer(Modifier.height(12.dp))
  Button(onClick=onScan, modifier=Modifier.fillMaxWidth().height(64.dp), shape=RoundedCornerShape(32.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4ADE80), contentColor=Color.Black)){Text("Scan Document  •  OFFLINE PRO")}
  Spacer(Modifier.height(6.dp))
  Text("Engine: Blur+Sobel+Contour+Hull + MorphClose+BlueInk+Contrast • A4 Standard", color=Color.Gray, style=MaterialTheme.typography.labelSmall)
 }
}
