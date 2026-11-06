package com.lankascan.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.lankascan.data.ScanSession
import com.lankascan.engine.EnhancementEngine
import com.lankascan.util.ImageProcessor
@Composable fun ReviewScreen(onCreatePdf:()->Unit, onBack:()->Unit){
 var selectedIdx by remember{mutableStateOf(0)}
 var current by remember{mutableStateOf(ScanSession.pages.firstOrNull())}
 LaunchedEffect(selectedIdx){current=ScanSession.pages.getOrNull(selectedIdx)}
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ TextButton(onClick=onBack){Text("Back")}; Text("Review ${ScanSession.pages.size} • Pro", color=Color.White); Text("${selectedIdx+1}/${ScanSession.pages.size}", color=Color.Gray) }
  Spacer(Modifier.height(12.dp))
  Card(Modifier.fillMaxWidth().weight(1f), shape=RoundedCornerShape(16.dp)){ current?.let{Image(bitmap=it.asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxSize())} ?: Box(Modifier.fillMaxSize().background(Color.White)) }
  Spacer(Modifier.height(12.dp))
  LazyRow(horizontalArrangement=Arrangement.spacedBy(10.dp)){
   itemsIndexed(ScanSession.pages){idx,bmp-> Card(Modifier.size(80.dp).clickable{selectedIdx=idx}, shape=RoundedCornerShape(12.dp)){ Image(bitmap=bmp.asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxSize()) } }
  }
  Spacer(Modifier.height(12.dp))
  Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceAround){
   TextButton(onClick={current?.let{val r=ImageProcessor.rotate(it,90f); ScanSession.replace(selectedIdx,r); current=r}}){Text("Rotate", color=Color.White)}
   TextButton(onClick={current?.let{val f=ImageProcessor.applyFilter(it, EnhancementEngine.Filter.ENHANCE); ScanSession.replace(selectedIdx,f); current=f}}){Text("Enhance", color=Color.White)}
   TextButton(onClick={current?.let{val f=ImageProcessor.applyFilter(it, EnhancementEngine.Filter.NO_SHADOW); ScanSession.replace(selectedIdx,f); current=f}}){Text("NoShadow", color=Color.White)}
   TextButton(onClick={current?.let{val f=ImageProcessor.applyFilter(it, EnhancementEngine.Filter.BW); ScanSession.replace(selectedIdx,f); current=f}}){Text("B/W", color=Color.White)}
   TextButton(onClick={ if(ScanSession.pages.size>1){ ScanSession.remove(selectedIdx); selectedIdx=0; current=ScanSession.pages.firstOrNull() } }){Text("Delete", color=Color.Red)}
  }
  Spacer(Modifier.height(12.dp))
  Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)){
   Button(onClick={ if(selectedIdx>0){ ScanSession.move(selectedIdx, selectedIdx-1); selectedIdx-- } }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.DarkGray)){Text("Move Left")}
   Button(onClick={ if(selectedIdx<ScanSession.pages.size-1){ ScanSession.move(selectedIdx, selectedIdx+1); selectedIdx++ } }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.DarkGray)){Text("Move Right")}
  }
  Spacer(Modifier.height(12.dp))
  Button(onClick=onCreatePdf, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4ADE80), contentColor=Color.Black)){Text("Create A4 PDF  •  ${ScanSession.pages.size} pages")}
 }
}
