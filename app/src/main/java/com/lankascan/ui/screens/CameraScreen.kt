package com.lankascan.ui.screens
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.File

@Composable fun CameraScreen(onCaptured:(Bitmap)->Unit, onClose:()->Unit){
 val context=LocalContext.current
 val lifecycleOwner=LocalLifecycleOwner.current
 var hasPermission by remember{mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)}
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ granted-> hasPermission=granted }
 LaunchedEffect(Unit){ if(!hasPermission) launcher.launch(Manifest.permission.CAMERA) }

 if(!hasPermission){
  Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment=Alignment.Center){
   Column(horizontalAlignment=Alignment.CenterHorizontally){
    Text("Camera permission needed", color=Color.White)
    Spacer(Modifier.height(12.dp))
    Button(onClick={launcher.launch(Manifest.permission.CAMERA)}){Text("Grant Camera Permission")}
    TextButton(onClick=onClose){Text("Close", color=Color.White)}
   }
  }
  return
 }

 val previewView=remember{PreviewView(context).apply{ scaleType=PreviewView.ScaleType.FILL_CENTER; implementationMode=PreviewView.ImplementationMode.COMPATIBLE }}
 var imageCapture by remember{mutableStateOf<ImageCapture?>(null)}
 var isBound by remember{mutableStateOf(false)}

 DisposableEffect(lifecycleOwner){
  val future=ProcessCameraProvider.getInstance(context)
  val executor=ContextCompat.getMainExecutor(context)
  val listener=Runnable{
   try{
    val provider=future.get()
    val preview=Preview.Builder().build().also{it.setSurfaceProvider(previewView.surfaceProvider)}
    val capture=ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    imageCapture=capture
    provider.unbindAll()
    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
    isBound=true
   }catch(e:Exception){ e.printStackTrace() }
  }
  future.addListener(listener, executor)
  onDispose{ try{ future.get().unbindAll() }catch(_:Exception){} }
 }

 Box(Modifier.fillMaxSize().background(Color.Black)){
  AndroidView(factory={previewView}, modifier=Modifier.fillMaxSize(), onRelease={ it -> it as PreviewView; try{ it.bitmap }catch(_:Exception){} })

  // Frame overlay - exactly like your screenshot: blue border
  Box(Modifier.fillMaxSize()){
   Box(Modifier.align(Alignment.Center).fillMaxWidth(0.85f).fillMaxHeight(0.65f).border(2.dp, Color(0xFF2F7CFF), RoundedCornerShape(20.dp)))
   Text("Place document inside frame", color=Color.White, modifier=Modifier.align(Alignment.TopCenter).padding(top=90.dp))
   if(!isBound){
    Box(Modifier.align(Alignment.Center).background(Color(0xAA000000), RoundedCornerShape(8.dp)).padding(12.dp)){Text("Starting camera...", color=Color.White)}
   }
  }

  // Top bar X
  TextButton(onClick=onClose, modifier=Modifier.align(Alignment.TopStart).padding(top=8.dp, start=4.dp)){Text("X", color=Color.White, style=MaterialTheme.typography.headlineMedium)}

  // Bottom blue button like screenshot
  Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom=32.dp), contentAlignment=Alignment.Center){
   Button(onClick={
    val file=File.createTempFile("scan_", ".jpg", context.cacheDir)
    val opts=ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture?.takePicture(opts, ContextCompat.getMainExecutor(context), object: ImageCapture.OnImageSavedCallback{
     override fun onImageSaved(o: ImageCapture.OutputFileResults){
      val bmp=BitmapFactory.decodeFile(file.absolutePath)
      if(bmp!=null) onCaptured(bmp)
     }
     override fun onError(e: androidx.camera.core.ImageCaptureException){ e.printStackTrace() }
    })
   }, modifier=Modifier.size(80.dp), shape=CircleShape, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF2F7CFF))){}
  }
 }
}
