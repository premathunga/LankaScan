package com.lankascan.ui.screens
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lankascan.data.ScanSession
import com.lankascan.util.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ExportScreen(onDone:()->Unit){
 val context = LocalContext.current
 var fileName by remember { mutableStateOf(SimpleDateFormat("Scan_yyyyMMdd_HHmm", Locale.US).format(Date())) }
 var quality by remember { mutableStateOf("Balanced") }
 var createdFile by remember { mutableStateOf<java.io.File?>(null) }

 Column(Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
  Card(Modifier.size(80.dp), shape=RoundedCornerShape(40.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF3B82F6))){ Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Icon(Icons.Default.Check, null, tint=Color.White, modifier=Modifier.size(40.dp)) } }
  Spacer(Modifier.height(16.dp))
  Text(if(createdFile==null) "Export PDF" else "PDF Created!", color=Color.White, style=MaterialTheme.typography.headlineMedium)
  Spacer(Modifier.height(16.dp))
  Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFF1E242E))){
   Column(Modifier.padding(16.dp)){
    OutlinedTextField(value=fileName, onValueChange={ fileName=it }, label={Text("File name")}, modifier=Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Text("Quality", color=Color.White)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp), modifier=Modifier.padding(top=8.dp)){
     listOf("High","Balanced","Compact").forEach{ q ->
      FilterChip(onClick={ quality=q }, label={Text(q)}, selected=quality==q)
     }
    }
    Spacer(Modifier.height(8.dp))
    Text("Pages: ${ScanSession.pages.size} • Location: Documents/LankaScan/", color=Color.Gray, style=MaterialTheme.typography.bodySmall)
   }
  }
  Spacer(Modifier.height(16.dp))
  if(createdFile==null){
   Button(onClick={
    try{
     val f = PdfGenerator.createPdf(context, ScanSession.pages.toList(), fileName)
     createdFile=f
     Toast.makeText(context, "Saved to ${f.absolutePath}", Toast.LENGTH_LONG).show()
    } catch(e:Exception){ Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
   }, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp)){ Text("Save PDF") }
  } else {
   Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Button(onClick={ /* share */ }, modifier=Modifier.weight(1f).height(52.dp), shape=RoundedCornerShape(26.dp)){ Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Share") }
    FilledTonalButton(onClick={ onDone() }, modifier=Modifier.weight(1f).height(52.dp), shape=RoundedCornerShape(26.dp)){ Text("Done") }
   }
   Spacer(Modifier.height(8.dp))
   Text(createdFile!!.absolutePath, color=Color.Gray, style=MaterialTheme.typography.bodySmall)
   Spacer(Modifier.height(8.dp))
   Button(onClick={ onDone() }, modifier=Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(28.dp)){ Text("Back to Home") }
  }
 }
}
