package com.lankascan.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

@Composable
fun CropScreen(onConfirm: ()->Unit){
    Box(Modifier.fillMaxSize().background(Color(0xFF0F1115))){
        Text("Adjust edges", color=Color.White, modifier=Modifier.align(Alignment.TopStart).padding(16.dp), style=MaterialTheme.typography.titleLarge)
        // Image with 4 blue handles overlay - placeholder
        Box(Modifier.align(Alignment.Center).fillMaxWidth().fillMaxHeight(0.7f).background(Color.Gray)){
            // OpenCV perspective handles would be drawn here
            // Four circles at corners
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)){
            FilledTonalButton(onClick = {}, modifier=Modifier.weight(1f)){ Text("Auto") }
            FilledTonalButton(onClick = {}, modifier=Modifier.weight(1f)){ Text("Full") }
            Button(onClick = onConfirm, modifier=Modifier.weight(1f)){ Text("Confirm") }
        }
    }
}
