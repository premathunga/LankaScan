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
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

@Composable fun HomeScreen(onScan:()->Unit, onOpenPdf:(File)->Unit){
 val docDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LankaScan")
 val files = remember { mutableStateOf(emptyList<File>()) }
 LaunchedEffect(Unit){
  if(docDir.exists()) files.value = docDir.listFiles()?.filter{ it.extension=="pdf" }?.sortedByDescending{ it.lastModified() } ?: emptyList()
 }
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
   Text("LankaScan", style=MaterialTheme.typography.headlineMedium, color=Color.White)
   Icon(Icons.Default.Settings, null, tint=Color.White)
  }
  Spacer(Modifier.height(16.dp))
  OutlinedTextField(value="", onValueChange={}, placeholder={Text("Search documents")}, leadingIcon={Icon(Icons.Default.Search,null)}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp), colors=TextFieldDefaults.colors(focusedContainerColor=Color(0xFF1E242E), unfocusedContainerColor=Color(0xFF1E242E), focusedTextColor=Color.White, unfocusedTextColor=Color.White))
  Spacer(Modifier.height(16.dp))
  if(files.value.isEmpty()){
   Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center){ Text("No documents yet.\nTap Scan to start.", color=Color.Gray) }
  } else {
   LazyColumn(modifier=Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(10.dp)){
    items(files.value){ file ->
     Card(shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E)), onClick={ onOpenPdf(file) }){
      Row(Modifier.padding(14.dp), verticalAlignment=Alignment.CenterVertically){
       Icon(Icons.Default.Description, null, tint=Color(0xFF3B82F6), modifier=Modifier.size(40.dp))
       Spacer(Modifier.width(12.dp))
       Column{ Text(file.nameWithoutExtension, color=Color.White); Text("${file.length()/1024} kB • ${java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(file.lastModified()))}", color=Color.Gray, style=MaterialTheme.typography.bodySmall) }
      }
     }
    }
   }
  }
  Spacer(Modifier.height(12.dp))
  Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Button(onClick=onScan, modifier=Modifier.weight(1f).height(56.dp), shape=RoundedCornerShape(28.dp)){ Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Scan") }
   FilledTonalButton(onClick={}, modifier=Modifier.weight(1f).height(56.dp), shape=RoundedCornerShape(28.dp)){ Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Import") }
  }
 }
}
