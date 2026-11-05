package com.lankascan.ui.screens
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.lankascan.data.ScanSession
import com.lankascan.util.ImageProcessor

@Composable fun ReviewScreen(onCreatePdf:()->Unit, onBack:()->Unit){
 var selectedIdx by remember { mutableStateOf(0) }
 var currentBitmap by remember { mutableStateOf(ScanSession.pages.firstOrNull()) }

 LaunchedEffect(selectedIdx){
  currentBitmap = ScanSession.pages.getOrNull(selectedIdx)
 }

 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
   TextButton(onClick=onBack){ Text("Back") }
   Text("Review (${ScanSession.pages.size} pages)", color=Color.White, style=MaterialTheme.typography.titleMedium)
   Text("${selectedIdx+1}/${ScanSession.pages.size}", color=Color.Gray)
  }
  Spacer(Modifier.height(12.dp))
  Card(Modifier.fillMaxWidth().weight(1f), shape=RoundedCornerShape(16.dp)){
   currentBitmap?.let{ Image(bitmap=it.asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxSize()) } ?: Box(Modifier.fillMaxSize().background(Color.White))
  }
  Spacer(Modifier.height(12.dp))
  LazyRow(horizontalArrangement=Arrangement.spacedBy(10.dp)){
   itemsIndexed(ScanSession.pages){ idx, bmp ->
    Card(Modifier.size(80.dp).background(if(idx==selectedIdx) Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(12.dp)), shape=RoundedCornerShape(12.dp), onClick={ selectedIdx=idx }){
     Image(bitmap=bmp.asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxSize())
    }
   }
   item{
    Card(Modifier.size(80.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E)), onClick={}){
     Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Icon(Icons.Default.Add, null, tint=Color(0xFF3B82F6)) }
    }
   }
  }
  Spacer(Modifier.height(12.dp))
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround){
   FilterButton(Icons.Default.Refresh, "Rotate"){ currentBitmap?.let{ val r = ImageProcessor.rotate(it, 90f); ScanSession.replace(selectedIdx, r); currentBitmap=r } }
   FilterButton(Icons.Default.ContentCut, "Crop"){ }
   FilterButton(Icons.Default.Brush, "Magic"){ currentBitmap?.let{ val f = ImageProcessor.magicFilter(it); ScanSession.replace(selectedIdx, f); currentBitmap=f } }
   FilterButton(Icons.Default.Brush, "B/W"){ currentBitmap?.let{ val f = ImageProcessor.blackWhite(it); ScanSession.replace(selectedIdx, f); currentBitmap=f } }
  }
  Spacer(Modifier.height(12.dp))
  Button(onClick=onCreatePdf, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp)){ Text("Create PDF (${ScanSession.pages.size})") }
 }
}

@Composable fun FilterButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick:()->Unit){
 TextButton(onClick=onClick){
  Column(horizontalAlignment=Alignment.CenterHorizontally){ Icon(icon, null, tint=Color.White); Text(label, color=Color.White, style=MaterialTheme.typography.labelSmall) }
 }
}
