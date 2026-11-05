package com.lankascan.ui.screens
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HomeScreen(onScan:()->Unit){
 val dir=File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
 val files=remember{mutableStateOf(listOf<File>())}
 LaunchedEffect(Unit){ if(dir.exists()) files.value=dir.listFiles()?.filter{it.extension=="pdf"}?.sortedByDescending{it.lastModified()}?: emptyList() }
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
   Column{ Text("LankaScan", style=MaterialTheme.typography.headlineMedium, color=Color.White); Text("100% OFFLINE • No Ads", style=MaterialTheme.typography.labelSmall, color=Color(0xFF4ADE80)) }
   Icon(Icons.Default.Settings, null, tint=Color.White)
  }
  Spacer(Modifier.height(16.dp))
  OutlinedTextField(value="", onValueChange={}, placeholder={Text("Search")}, leadingIcon={Icon(Icons.Default.Search,null)}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp), colors=TextFieldDefaults.colors(focusedContainerColor=Color(0xFF1E242E), unfocusedContainerColor=Color(0xFF1E242E), focusedTextColor=Color.White, unfocusedTextColor=Color.White))
  Spacer(Modifier.height(16.dp))
  if(files.value.isEmpty()){
   Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Icon(Icons.Default.Scanner, null, tint=Color.Gray, modifier=Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No documents", color=Color.White); Text("Tap Scan - 100% offline", color=Color.Gray, style=MaterialTheme.typography.bodySmall) } }
  } else {
   LazyColumn(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(10.dp)){
    items(files.value){ file->
     Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E)), modifier=Modifier.fillMaxWidth()){
      Row(Modifier.padding(14.dp), verticalAlignment=Alignment.CenterVertically){
       Icon(Icons.Default.Description, null, tint=Color(0xFF3B82F6), modifier=Modifier.size(40.dp))
       Spacer(Modifier.width(12.dp))
       Column{ Text(file.nameWithoutExtension, color=Color.White, maxLines=1); Text("${file.length()/1024} kB", color=Color.Gray, style=MaterialTheme.typography.bodySmall) }
      }
     }
    }
   }
  }
  Spacer(Modifier.height(12.dp))
  Button(onClick=onScan, modifier=Modifier.fillMaxWidth().height(64.dp), shape=RoundedCornerShape(32.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4ADE80), contentColor=Color.Black)){
   Icon(Icons.Default.PhotoCamera, null, modifier=Modifier.size(28.dp)); Spacer(Modifier.width(12.dp)); Column{ Text("Scan Document", style=MaterialTheme.typography.titleMedium); Text("Offline • Magic Filter • HD", style=MaterialTheme.typography.labelSmall) }
  }
 }
}
