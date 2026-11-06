package com.lankascan.ui.screens
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.File
@Composable fun CameraScreen(onCaptured:(Bitmap)->Unit, onClose:()->Unit){
 val context=LocalContext.current
 val lifecycleOwner=LocalLifecycleOwner.current
 val previewView=remember{PreviewView(context)}
 var imageCapture by remember{mutableStateOf<ImageCapture?>(null)}
 LaunchedEffect(Unit){
  val future=ProcessCameraProvider.getInstance(context)
  future.addListener({
   val provider=future.get()
   val preview=Preview.Builder().build().also{it.setSurfaceProvider(previewView.surfaceProvider)}
   val capture=ImageCapture.Builder().build()
   imageCapture=capture
   try{provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)}catch(_:Exception){}
  }, ContextCompat.getMainExecutor(context))
 }
 Box(Modifier.fillMaxSize().background(Color.Black)){
  AndroidView(factory={previewView}, modifier=Modifier.fillMaxSize())
  Box(Modifier.fillMaxSize()){
   Box(Modifier.align(Alignment.Center).fillMaxWidth(0.85f).fillMaxHeight(0.65f).border(2.dp, Color.White, RoundedCornerShape(20.dp)))
   Text("OFFLINE PRO SCANNER", color=Color.White, modifier=Modifier.align(Alignment.TopCenter).padding(top=60.dp).background(Color(0xAA000000), RoundedCornerShape(20.dp)).padding(horizontal=18.dp, vertical=8.dp))
  }
  TextButton(onClick=onClose, modifier=Modifier.align(Alignment.TopStart).padding(12.dp)){Text("Close", color=Color.White)}
  Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(32.dp), horizontalArrangement=Arrangement.Center){
   Button(onClick={
    val file=File.createTempFile("scan_", ".jpg", context.cacheDir)
    val opts=ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture?.takePicture(opts, ContextCompat.getMainExecutor(context), object: ImageCapture.OnImageSavedCallback{
     override fun onImageSaved(o: ImageCapture.OutputFileResults){ val bmp=BitmapFactory.decodeFile(file.absolutePath); onCaptured(bmp) }
     override fun onError(e: androidx.camera.core.ImageCaptureException){}
    })
   }, modifier=Modifier.size(80.dp), shape=CircleShape, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4ADE80))){}
  }
 }
}
