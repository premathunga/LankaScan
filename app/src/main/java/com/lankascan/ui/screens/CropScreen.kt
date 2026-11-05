package com.lankascan.ui.screens
import android.graphics.Bitmap
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

@Composable fun CropScreen(onConfirm:(Bitmap)->Unit, onBack:()->Unit){
 val src = ScanSession.lastCaptured ?: return
 var viewSize by remember { mutableStateOf(Offset.Zero) }
 var points by remember {
  mutableStateOf(listOf(PointF(80f,80f), PointF(920f,80f), PointF(920f,1220f), PointF(80f,1220f)))
 }
 var preview by remember { mutableStateOf(src) }

 Box(Modifier.fillMaxSize().background(Color(0xFF0F1115))){
  Column(Modifier.fillMaxSize()){
   Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){
    TextButton(onClick=onBack){ Text("Back") }
    Text("Adjust Edges", color=Color.White, style=MaterialTheme.typography.titleLarge)
    TextButton(onClick={
     val scaledPts = points.map{ PointF(it.x / viewSize.x * src.width, it.y / viewSize.y * src.height) }
     val cropped = ImageProcessor.perspectiveCrop(src, scaledPts)
     onConfirm(cropped)
    }){ Text("Confirm") }
   }

   Box(Modifier.weight(1f).fillMaxWidth().onSizeChanged{ viewSize = Offset(it.width.toFloat(), it.height.toFloat()) }){
    Image(bitmap=preview.asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxSize())
    // 4 draggable handles
    points.forEachIndexed { idx, pt ->
     var offset by remember { mutableStateOf(Offset(pt.x, pt.y)) }
     Box(Modifier.offset{ IntOffset((offset.x-20).roundToInt(), (offset.y-20).roundToInt()) }.size(40.dp).background(Color(0xFF3B82F6), RoundedCornerShape(20.dp)).pointerInput(idx){
      detectDragGestures{ _, drag -> offset+=drag; points = points.toMutableList().also{ it[idx]=PointF(offset.x, offset.y) } }
     })
    }
   }

   Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement=Arrangement.spacedBy(12.dp)){
    FilledTonalButton(onClick={
     points = listOf(PointF(0f,0f), PointF(viewSize.x,0f), PointF(viewSize.x, viewSize.y), PointF(0f, viewSize.y))
    }, modifier=Modifier.weight(1f)){ Text("Full") }
    FilledTonalButton(onClick={
     preview = ImageProcessor.magicFilter(src)
    }, modifier=Modifier.weight(1f)){ Text("Magic Filter") }
    Button(onClick={ preview = src }, modifier=Modifier.weight(1f)){ Text("Original") }
   }
  }
 }
}
