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
import com.lankascan.engine.DocumentDetector
import com.lankascan.engine.EnhancementEngine
import com.lankascan.util.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CropScreen(onConfirm:(Bitmap)->Unit, onBack:()->Unit){
 val fullSrc=ScanSession.lastCaptured ?: return
 val src=remember(fullSrc){ImageProcessor.downscale(fullSrc, 1200)}
 var viewSize by remember{mutableStateOf(Offset.Zero)}
 var points by remember{mutableStateOf<List<PointF>>(emptyList())}
 var preview by remember{mutableStateOf(src)}
 var filterMode by remember{mutableStateOf(EnhancementEngine.Filter.MAGIC)}
 var isDetecting by remember{mutableStateOf(true)}
 var isProcessing by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 // Detect in background - NO ANR
 LaunchedEffect(src){
  isDetecting=true
  val detected=withContext(Dispatchers.Default){DocumentDetector.detectDocument(src)}
  // Convert to view coords (view is 0.25 scale in old code, now use full src coords mapped to view later)
  // Store in src coords, display will map via viewSize
  points=detected
  isDetecting=false
  isProcessing=true
  val first=withContext(Dispatchers.Default){
   val cropped=ImageProcessor.perspectiveCrop(src, detected)
   ImageProcessor.applyFilter(cropped, filterMode)
  }
  preview=first
  isProcessing=false
 }
 // Filter change in background
 LaunchedEffect(filterMode){
  if(points.isEmpty() || isDetecting) return@LaunchedEffect
  // Skip first run: preview already MAGIC, but re-run is ok - it will be fast on 1200px
  isProcessing=true
  val res=withContext(Dispatchers.Default){
   try{
    // points are in src coords, need to crop directly
    val cropped=ImageProcessor.perspectiveCrop(src, points)
    ImageProcessor.applyFilter(cropped, filterMode)
   }catch(e:Exception){src}
  }
  preview=res
  isProcessing=false
 }
 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115))){
  Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){
   TextButton(onClick=onBack){Text("Back")}
   Text("Adjust - MAGIC FILTER", color=Color.White)
   Button(onClick={onConfirm(preview)}, enabled=!isDetecting && !isProcessing){Text("Confirm")}
  }
  if(isDetecting){
   Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
     CircularProgressIndicator(color=Color(0xFF4ADE80))
     Spacer(Modifier.height(12.dp))
     Text("Detecting document edges...", color=Color.White)
     Text("Background thread - No hang", color=Color.Gray, style=MaterialTheme.typography.bodySmall)
    }
   }
  } else {
   Box(Modifier.weight(1f).fillMaxWidth().onSizeChanged{viewSize=Offset(it.width.toFloat(), it.height.toFloat())}){
    Image(bitmap=preview.asImageBitmap(), contentDescription=null, modifier=Modifier.fillMaxSize())
    if(isProcessing){Box(Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment=Alignment.Center){CircularProgressIndicator(color=Color(0xFF4ADE80))}}
    // Corners: map src coords to view coords
    points.forEachIndexed{idx, pt->
     val vx=if(viewSize.x==0f) pt.x*0.25f else pt.x/viewSize.x*src.width*0f + pt.x/src.width*viewSize.x
     val vy=if(viewSize.y==0f) pt.y*0.25f else pt.y/src.height*viewSize.y
     var offset by remember(idx, viewSize){mutableStateOf(Offset(vx, vy))}
     LaunchedEffect(vx, vy){offset=Offset(vx, vy)}
     Box(Modifier.offset{IntOffset((offset.x-20).roundToInt(), (offset.y-20).roundToInt())}.size(40.dp).background(Color(0xFF4ADE80), RoundedCornerShape(20.dp)).pointerInput(idx){
      detectDragGestures{_, drag->
       offset+=drag
       val newSrcX=offset.x/viewSize.x*src.width
       val newSrcY=offset.y/viewSize.y*src.height
       val nl=points.toMutableList(); nl[idx]=PointF(newSrcX, newSrcY); points=nl
      }
     })
    }
   }
  }
  Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement=Arrangement.spacedBy(6.dp)){
   listOf("ORIGINAL","MAGIC","ENHANCE","NO_SHADOW","BW","GRAY").forEach{name->
    val f=EnhancementEngine.Filter.valueOf(name)
    FilterChip(onClick={filterMode=f}, label={Text(name.take(5))}, selected=(filterMode==f), enabled=!isDetecting, modifier=Modifier.weight(1f))
   }
  }
  if(isProcessing) Text("Applying Magic Filter in background...", color=Color.Gray, style=MaterialTheme.typography.labelSmall, modifier=Modifier.padding(start=16.dp, bottom=8.dp))
 }
}
