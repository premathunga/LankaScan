package com.lankascan.ui.screens
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File
@Composable fun HomeScreen(onScan:()->Unit){
 val dir=File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
 val files=remember{mutableStateOf(listOf<File>())}
 LaunchedEffect(Unit){ if(dir.exists()) files.value=dir.listFiles()?.filter{it.extension=="pdf"}?.sortedByDescending{it.lastModified()}?: emptyList() }
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Column{ Text("LankaScan Pro", style=MaterialTheme.typography.headlineMedium, color=Color.White); Text("100% OFFLINE • No Ads • Better than Claude's CamPDF", style=MaterialTheme.typography.labelSmall, color=Color(0xFF4ADE80)) }
  Spacer(Modifier.height(16.dp))
  if(files.value.isEmpty()){
   Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center){ Text("No documents yet. Tap Scan.", color=Color.Gray) }
  } else {
   LazyColumn(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(10.dp)){
    items(files.value){ file->
     Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E)), modifier=Modifier.fillMaxWidth()){
      Row(Modifier.padding(14.dp)){ Column{ Text(file.nameWithoutExtension, color=Color.White); Text("${file.length()/1024} kB • PDF • A4", color=Color.Gray, style=MaterialTheme.typography.bodySmall) } }
     }
    }
   }
  }
  Spacer(Modifier.height(12.dp))
  Button(onClick=onScan, modifier=Modifier.fillMaxWidth().height(64.dp), shape=RoundedCornerShape(32.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4ADE80), contentColor=Color.Black)){
   Text("Scan Document • OFFLINE PRO")
  }
  Spacer(Modifier.height(6.dp))
  Text("Engine: Blur+Sobel+Contour+Hull+MorphClose+BlueInk • A4 Standardized", color=Color.Gray, style=MaterialTheme.typography.labelSmall)
 }
}
