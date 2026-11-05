package com.lankascan.ui
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lankascan.ui.screens.*
@Composable fun NavGraph(){
 val nav=rememberNavController()
 NavHost(navController=nav, startDestination="home"){
  composable("home"){ HomeScreen(onScan={nav.navigate("camera")}) }
  composable("camera"){ CameraScreen(onCaptured={bmp-> com.lankascan.data.ScanSession.lastCaptured=bmp; nav.navigate("crop")}, onClose={nav.popBackStack()}) }
  composable("crop"){ CropScreen(onConfirm={cropped-> com.lankascan.data.ScanSession.lastCropped=cropped; com.lankascan.data.ScanSession.add(cropped); nav.navigate("review"){popUpTo("home")}}, onBack={nav.popBackStack()}) }
  composable("review"){ ReviewScreen(onCreatePdf={nav.navigate("export")}, onBack={nav.navigate("camera")}) }
  composable("export"){ ExportScreen(onDone={com.lankascan.data.ScanSession.clear(); nav.popBackStack("home",false)}) }
 }
}
