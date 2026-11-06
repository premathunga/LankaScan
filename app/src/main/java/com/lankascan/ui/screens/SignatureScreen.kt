package com.lankascan.ui.screens
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color as CColor
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
@Composable fun SignatureScreen(onSave:(Bitmap)->Unit, onBack:()->Unit){
 var paths by remember{mutableStateOf(listOf<List<Offset>>())}
 var currentPath by remember{mutableStateOf<List<Offset>>(emptyList())}
 Column(Modifier.fillMaxSize().background(CColor(0xFF0F1115)).padding(16.dp)){
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween){TextButton(onClick=onBack){Text("Back")}; Text("Draw Signature", color=CColor.White)}
  Spacer(Modifier.height(16.dp))
  Box(Modifier.fillMaxWidth().weight(1f).background(CColor.White, RoundedCornerShape(16.dp)).pointerInput(Unit){
   detectDragGestures(
    onDragStart={currentPath=listOf(it)},
    onDragEnd={paths=paths+listOf(currentPath); currentPath=emptyList()},
    onDrag={_, drag->currentPath=currentPath+listOf(currentPath.last()+drag)}
   )
  }){
   Canvas(modifier=Modifier.fillMaxSize()){
    paths.forEach{path->
     if(path.size>1) for(i in 0 until path.size-1) drawLine(color=CColor.Black, start=path[i], end=path[i+1], strokeWidth=4f)
    }
    if(currentPath.size>1) for(i in 0 until currentPath.size-1) drawLine(color=CColor.Black, start=currentPath[i], end=currentPath[i+1], strokeWidth=4f)
   }
  }
  Spacer(Modifier.height(12.dp))
  Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)){
   Button(onClick={paths=emptyList(); currentPath=emptyList()}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=CColor.DarkGray)){Text("Clear")}
   Button(onClick={
    val bmp=Bitmap.createBitmap(800, 300, Bitmap.Config.ARGB_8888)
    val c=Canvas(bmp); c.drawColor(Color.WHITE)
    val paint=Paint().apply{color=Color.BLACK; strokeWidth=6f; style=Paint.Style.STROKE; isAntiAlias=true}
    // Draw paths scaled to bitmap
    paths.forEach{path->
     val p=Path()
     path.forEachIndexed{i, off->if(i==0) p.moveTo(off.x*800/1000f, off.y*300/1000f) else p.lineTo(off.x*800/1000f, off.y*300/1000f)}
     c.drawPath(p, paint)
    }
    onSave(bmp)
   }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=CColor(0xFF4ADE80), contentColor=CColor.Black)){Text("Save Signature")}
  }
 }
}
