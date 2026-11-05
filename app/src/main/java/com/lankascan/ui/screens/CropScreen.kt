package com.lankascan.ui.screens
import android.graphics.PointF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lankascan.data.ScanSession
import com.lankascan.util.ImageProcessor
import kotlin.math.roundToInt

@Composable fun CropScreen(onConfirm:(android.graphics.Bitmap)->Unit, onBack:()->Unit){
 val src=ScanSession.lastCaptured ?: return
 var viewSize by remember{mutableStateOf(Offset.Zero)}
 // Auto detected edges - OFFLINE
 var points by remember{mutableStateOf(ImageProcessor.autoDetectEdges(src).map{PointF(it.x*0.3f, it.y*0.3f)})}
 var preview by remember{mutableStateOf(src)}
 var filterMode by remember{mutableStateOf("Original")}

 Box(Modifier.fillMaxSize().background(Color(0xFF0F1115))){
  Column(Modifier.fillMaxSize()){
   Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically){
    TextButton(onClick=onBack){Text("Back")}
    Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("Adjust Edges", color=Color.White, style=MaterialTheme.typography.titleLarge); Text("Drag corners • 100% OFFLINE", color=Color(0xFF4ADE80), style=MaterialTheme.typography.labelSmall) }
    TextButton(onClick={
     val scaled=points.map{PointF(it.x / viewSize.x * src.width, it.y / viewSize.y * src.height)}
     val cropped=ImageProcessor.perspectiveCrop(src, scaled)
     val finalBmp=when(filterMode){ "Magic"->ImageProcessor.magicFilter(cropped); "Gray"->ImageProcessor.grayFilter(cropped); "BW"->ImageProcessor.bwFilter(cropped); else->cropped }
     onConfirm(finalBmp)
    }){Text("Confirm")}
   }
   Box(Modifier.weight(1f).fillMaxWidth().onSizeChanged{viewSize=Offset(it.width.toFloat(), it.height.toFloat())}){
    Image(bitmap=preview.asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxSize())
    points.forEachIndexed{idx, pt->
     var offset by remember{mutableStateOf(Offset(pt.x, pt.y))}
     Box(Modifier.offset{IntOffset((offset.x-20).roundToInt(), (offset.y-20).roundToInt())}.size(40.dp).background(Color(0xFF4ADE80), RoundedCornerShape(20.dp)).pointerInput(idx){detectDragGestures{_,drag->offset+=drag; points=points.toMutableList().also{it[idx]=PointF(offset.x, offset.y)}}})
    }
   }
   Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)){
    listOf("Original","Magic","Gray","BW").forEach{ mode->
     FilterChip(onClick={
      filterMode=mode
      val scaled=points.map{PointF(it.x / viewSize.x * src.width, it.y / viewSize.y * src.height)}
      val cropped=ImageProcessor.perspectiveCrop(src, scaled)
      preview=when(mode){ "Magic"->ImageProcessor.magicFilter(cropped); "Gray"->ImageProcessor.grayFilter(cropped); "BW"->ImageProcessor.bwFilter(cropped); else->cropped }
     }, label={Text(mode)}, selected=filterMode==mode, modifier=Modifier.weight(1f))
    }
   }
   Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement=Arrangement.spacedBy(12.dp)){
    FilledTonalButton(onClick={points=ImageProcessor.autoDetectEdges(src).map{PointF(it.x*0.3f, it.y*0.3f)}}, modifier=Modifier.weight(1f)){Text("Auto")}
    FilledTonalButton(onClick={points=listOf(PointF(0f,0f), PointF(viewSize.x,0f), PointF(viewSize.x, viewSize.y), PointF(0f, viewSize.y))}, modifier=Modifier.weight(1f)){Text("Full")}
    Button(onClick={preview=src; filterMode="Original"}, modifier=Modifier.weight(1f)){Text("Reset")}
   }
  }
 }
}
