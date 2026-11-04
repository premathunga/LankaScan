package com.lankascan.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

@Composable
fun ExportScreen(onDone: ()->Unit){
    Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
        Card(Modifier.size(80.dp), shape=RoundedCornerShape(40.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF3B82F6))){ Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Icon(Icons.Default.Check, null, tint=Color.White, modifier=Modifier.size(40.dp)) } }
        Spacer(Modifier.height(16.dp))
        Text("PDF created", color=Color.White, style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
            Column(Modifier.padding(16.dp)){
                OutlinedTextField(value="Scan_20261104_2017", onValueChange={}, label={Text("File name")}, modifier=Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Row{ Card(Modifier.size(64.dp)){ Box(Modifier.background(Color.White).fillMaxSize()) }; Spacer(Modifier.width(12.dp)); Column{ Text("1 page", color=Color.White); Text("436 kB", color=Color.Gray) } }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ FilterChip(onClick={}, label={Text("High")}, selected=false); FilterChip(onClick={}, label={Text("Balanced")}, selected=true); FilterChip(onClick={}, label={Text("Compact")}, selected=false) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(12.dp)){
            Button(onClick={}, modifier=Modifier.weight(1f).height(52.dp), shape=RoundedCornerShape(26.dp)){ Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Share") }
            FilledTonalButton(onClick={}, modifier=Modifier.weight(1f).height(52.dp), shape=RoundedCornerShape(26.dp)){ Text("Save to p...") }
        }
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(onClick={}, modifier=Modifier.fillMaxWidth().height(52.dp), shape=RoundedCornerShape(26.dp)){ Text("Open") }
        Spacer(Modifier.weight(1f))
        Button(onClick=onDone, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp)){ Text("Done") }
    }
}
