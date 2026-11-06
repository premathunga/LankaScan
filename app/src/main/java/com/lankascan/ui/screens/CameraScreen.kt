package com.lankascan.ui.screens
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable fun CameraScreen(onCaptured:(Bitmap)->Unit, onClose:()->Unit){
 val context=LocalContext.current
 val lifecycleOwner=LocalLifecycleOwner.current
 var status by remember{mutableStateOf("Starting camera...")}
 var hasPermission by remember{mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)}
 val permLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ hasPermission=it; if(!it) status="Permission denied - Use Gallery" }
 var tempFile by remember{mutableStateOf<File?>(null)}
 val systemCameraLauncher=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){ success->
  if(success && tempFile!=null){ val bmp=BitmapFactory.decodeFile(tempFile!!.absolutePath); if(bmp!=null){ Toast.makeText(context,"Captured!",Toast.LENGTH_SHORT).show(); onCaptured(bmp) } else status="Decode failed" } else status="Cancelled"
 }
 val galleryLauncher=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri->
  if(uri!=null){ try{ val s=context.contentResolver.openInputStream(uri); val bmp=BitmapFactory.decodeStream(s); if(bmp!=null) onCaptured(bmp) }catch(e:Exception){ status="Gallery error: ${e.message}" } }
 }
 LaunchedEffect(Unit){ if(!hasPermission) permLauncher.launch(Manifest.permission.CAMERA) }
 val previewView=remember{PreviewView(context).apply{ scaleType=PreviewView.ScaleType.FILL_CENTER; implementationMode=PreviewView.ImplementationMode.COMPATIBLE }}
 var imageCapture by remember{mutableStateOf<ImageCapture?>(null)}
 LaunchedEffect(hasPermission){
  if(!hasPermission) return@LaunchedEffect
  try{
   val future=ProcessCameraProvider.getInstance(context)
   future.addListener({
    try{ val provider=future.get(); val preview=Preview.Builder().build().also{it.setSurfaceProvider(previewView.surfaceProvider)}; val capture=ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build(); imageCapture=capture; provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture); status="Camera ready - Tap blue button" }catch(e:Exception){ status="CameraX failed - Use SYS button"; e.printStackTrace() }
   }, ContextCompat.getMainExecutor(context))
  }catch(e:Exception){ status="Camera init failed" }
 }
 Box(Modifier.fillMaxSize().background(Color.Black)){
  AndroidView(factory={previewView}, modifier=Modifier.fillMaxSize())
  Box(Modifier.fillMaxSize()){
   Box(Modifier.align(Alignment.Center).fillMaxWidth(0.85f).fillMaxHeight(0.65f).border(2.dp, Color(0xFF2F7CFF), RoundedCornerShape(20.dp)))
   Text("Place document inside frame", color=Color.White, modifier=Modifier.align(Alignment.TopCenter).padding(top=90.dp))
   Text(status, color=Color.Yellow, modifier=Modifier.align(Alignment.TopCenter).padding(top=130.dp).background(Color(0xAA000000), RoundedCornerShape(8.dp)).padding(8.dp))
  }
  TextButton(onClick=onClose, modifier=Modifier.align(Alignment.TopStart).padding(top=8.dp, start=4.dp)){Text("X", color=Color.White, style=MaterialTheme.typography.headlineMedium)}
  Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom=24.dp), horizontalAlignment=Alignment.CenterHorizontally){
   Row(Modifier.fillMaxWidth().padding(horizontal=32.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){
    Button(onClick={galleryLauncher.launch("image/*")}, modifier=Modifier.size(56.dp), shape=CircleShape, colors=ButtonDefaults.buttonColors(containerColor=Color.DarkGray)){Text("IMG", color=Color.White)}
    Button(onClick={
     if(imageCapture!=null){
      val file=File.createTempFile("scan_", ".jpg", context.cacheDir); val opts=ImageCapture.OutputFileOptions.Builder(file).build()
      imageCapture?.takePicture(opts, ContextCompat.getMainExecutor(context), object: ImageCapture.OnImageSavedCallback{
       override fun onImageSaved(o: ImageCapture.OutputFileResults){ val bmp=BitmapFactory.decodeFile(file.absolutePath); if(bmp!=null) onCaptured(bmp) else status="Capture failed - Try SYS" }
       override fun onError(e: androidx.camera.core.ImageCaptureException){ status="Capture error: ${e.message}" }
      })
     } else { val f=File.createTempFile("sys_", ".jpg", context.cacheDir); tempFile=f; val uri=FileProvider.getUriForFile(context, context.packageName+".provider", f); systemCameraLauncher.launch(uri) }
    }, modifier=Modifier.size(80.dp), shape=CircleShape, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF2F7CFF))){}
    Button(onClick={ val f=File.createTempFile("sys_", ".jpg", context.cacheDir); tempFile=f; val uri=FileProvider.getUriForFile(context, context.packageName+".provider", f); systemCameraLauncher.launch(uri) }, modifier=Modifier.size(56.dp), shape=CircleShape, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4ADE80))){Text("SYS", color=Color.Black)}
   }
   Spacer(Modifier.height(8.dp))
   Text("IMG=Gallery  Blue=Scan  SYS=System Camera (100% works)", color=Color.White, style=MaterialTheme.typography.labelSmall)
  }
 }
}
