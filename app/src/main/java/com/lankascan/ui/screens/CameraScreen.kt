package com.lankascan.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable fun CameraScreen(onCaptured:()->Unit, onClose:()->Unit){
 Box(Modifier.fillMaxSize().background(Color.Black)){
  Box(Modifier.fillMaxSize()){
   Box(Modifier.align(Alignment.Center).fillMaxWidth(0.9f).fillMaxHeight(0.7f).border(2.dp, Color.White, RoundedCornerShape(24.dp)))
   Text("Place the document inside the frame", color=Color.White, modifier=Modifier.align(Alignment.TopCenter).padding(top=80.dp).background(Color(0x88000000), RoundedCornerShape(16.dp)).padding(horizontal=16.dp, vertical=6.dp))
  }
  IconButton(onClick=onClose, modifier=Modifier.align(Alignment.TopStart).padding(8.dp)){ Icon(Icons.Default.Close, null, tint=Color.White) }
  Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(32.dp), Arrangement.SpaceBetween, Alignment.CenterVertically){
   IconButton(onClick={}){ Icon(Icons.Default.PhotoLibrary, null, tint=Color.White, modifier=Modifier.size(32.dp)) }
   Button(onClick=onCaptured, modifier=Modifier.size(80.dp), shape=CircleShape, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF3B82F6))){}
   Spacer(Modifier.size(32.dp))
  }
 }
}
