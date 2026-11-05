package com.lankascan.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable fun HomeScreen(onScan:()->Unit){
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
   Text("All Documents", style=MaterialTheme.typography.headlineMedium, color=Color.White)
   Icon(Icons.Default.Settings, null, tint=Color.White)
  }
  Spacer(Modifier.height(16.dp))
  TextField(value="", onValueChange={}, placeholder={Text("Search documents...")}, leadingIcon={Icon(Icons.Default.Search, null)}, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp), colors=TextFieldDefaults.colors(focusedContainerColor=Color(0xFF1E242E), unfocusedContainerColor=Color(0xFF1E242E)))
  Spacer(Modifier.height(16.dp))
  Card(shape=RoundedCornerShape(24.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
   Row(Modifier.padding(12.dp), verticalAlignment=Alignment.CenterVertically){
    Card(Modifier.size(64.dp), shape=RoundedCornerShape(12.dp)){ Box(Modifier.fillMaxSize().background(Color.White)) }
    Spacer(Modifier.width(12.dp))
    Column{ Text("Scan_20261104_1847", color=Color.White); Text("Nov 4, 2026 • 1 page", color=Color.Gray, style=MaterialTheme.typography.bodySmall) }
   }
  }
  Spacer(Modifier.weight(1f))
  Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Button(onClick=onScan, modifier=Modifier.weight(1f).height(56.dp), shape=RoundedCornerShape(28.dp)){ Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Scan") }
   FilledTonalButton(onClick={}, modifier=Modifier.weight(1f).height(56.dp), shape=RoundedCornerShape(28.dp)){ Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Import") }
  }
 }
}
