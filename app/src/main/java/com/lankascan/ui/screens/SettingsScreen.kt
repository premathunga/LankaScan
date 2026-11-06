package com.lankascan.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SettingsScreen(onBack:()->Unit){
 var lang by remember{mutableStateOf("Sinhala")}
 var dark by remember{mutableStateOf(true)}
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween){TextButton(onClick=onBack){Text("Back")}; Text("Settings", color=Color.White)}
  Spacer(Modifier.height(16.dp))
  Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
   Column(Modifier.padding(16.dp)){
    Text("About LankaScan Pro v2.0", color=Color.White, style=MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Text("100% Offline, No Ads, No Internet Permission", color=Color.Gray)
    Text("No Firebase, No Analytics, Open Source", color=Color.Gray)
    Text("Engine: Gaussian + Sobel + Quad + MorphClose + BlueInk + Unsharp", color=Color.Gray)
    Text("CamScanner Level Magic Filter, A4 Standard", color=Color.Gray)
    Text("Based on Claude CamPDF - Improved Engine", color=Color(0xFF4ADE80))
   }
  }
  Spacer(Modifier.height(12.dp))
  Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
   Column(Modifier.padding(16.dp)){
    Text("Language: " + lang, color=Color.White)
    Row(Modifier.padding(top=8.dp), Arrangement.spacedBy(8.dp)){
     listOf("Sinhala","English","Tamil").forEach{l->FilterChip(onClick={lang=l}, label={Text(l)}, selected=(lang==l))}
    }
   }
  }
  Spacer(Modifier.height(12.dp))
  Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
   Column(Modifier.padding(16.dp)){
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween){
     Text("Dark Theme", color=Color.White)
     Switch(checked=dark, onCheckedChange={dark=it})
    }
   }
  }
 }
}
