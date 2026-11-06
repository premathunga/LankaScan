package com.lankascan.ui.screens
import android.graphics.PointF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lankascan.data.ScanSession
import com.lankascan.engine.DocumentDetector
import com.lankascan.util.ImageProcessor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CropScreen(onConfirm:(android.graphics.Bitmap)->Unit, onBack:()->Unit){
 val src=ScanSession.lastCaptured ?: return
 var viewSize by remember{mutableStateOf(Offset.Zero)}
 var points by remember{mutableStateOf(DocumentDetector.detectDocument(src).map{PointF(it.x*0.25f, it.y*0.25f)})}
 var preview by remember{mutableStateOf(src)}
 var filterMode by remember{mutableStateOf(com.lankascan.engine.EnhancementEngine.Filter.NO_SHADOW)}

 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115))){
  Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement=Arrangement.SpaceBetween){
   TextButton(onClick=onBack){Text("Back")}
   Text("Adjust • OFFLINE AI", color=Color.White)
   TextButton(onClick={
    val scaled=points.map{PointF(it.x / viewSize.x * src.width, it.y / viewSize.y * src.height)}
    val cropped=ImageProcessor.perspectiveCrop(src, scaled)
    val finalBmp=ImageProcessor.applyFilter(cropped, filterMode)
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
  Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement=Arrangement.spacedBy(6.dp)){
   listOf("ORIGINAL","LIGHTEN","ENHANCE","NO_SHADOW","BW").forEach{ name->
    val f=com.lankascan.engine.EnhancementEngine.Filter.valueOf(name)
    FilterChip(onClick={
     filterMode=f
     val scaled=points.map{PointF(it.x / viewSize.x * src.width, it.y / viewSize.y * src.height)}
     val cropped=ImageProcessor.perspectiveCrop(src, scaled)
     preview=ImageProcessor.applyFilter(cropped, f)
    }, label={Text(name.take(6))}, selected=filterMode==f, modifier=Modifier.weight(1f))
   }
  }
 }
}
