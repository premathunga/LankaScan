package com.lankascan.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable fun ReviewScreen(onCreatePdf:()->Unit){ Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp)){ Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween){ Text("Review pages", color=Color.White, style=MaterialTheme.typography.titleLarge); Text("Page 1 of 1", color=Color.Gray) }; Spacer(Modifier.height(12.dp)); Card(Modifier.fillMaxWidth().weight(1f), shape=RoundedCornerShape(16.dp)){ Box(Modifier.fillMaxSize().background(Color.White)) }; Spacer(Modifier.height(12.dp)); Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){ Card(Modifier.size(80.dp)){ Box(Modifier.background(Color.White).fillMaxSize()) }; Card(Modifier.size(80.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){ Icon(Icons.Default.AddAPhoto, null, tint=Color(0xFF3B82F6), modifier=Modifier.padding(24.dp)) } }; Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround){ TextButton(onClick={}){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Icon(Icons.Default.AddAPhoto, null, tint=Color.White); Text("Add", color=Color.White) } }; TextButton(onClick={}){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Icon(Icons.Default.Refresh, null, tint=Color.White); Text("Rotate", color=Color.White) } }; TextButton(onClick={}){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Icon(Icons.Default.Crop, null, tint=Color.White); Text("Crop", color=Color.White) } }; TextButton(onClick={}){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Icon(Icons.Default.AutoFixHigh, null, tint=Color.White); Text("Filter", color=Color.White) } } }; Button(onClick=onCreatePdf, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp)){ Text("Create PDF (1)") } } }
