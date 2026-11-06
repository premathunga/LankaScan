package com.lankascan.ui
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lankascan.ui.screens.*
@Composable fun NavGraph(){
 val nav=rememberNavController()
 NavHost(navController=nav, startDestination="home"){
  composable("home"){HomeScreen(onScan={nav.navigate("camera")}, onOpenPdf={p->com.lankascan.data.ScanSession.currentPdfPath=p; nav.navigate("viewer")}, onSettings={nav.navigate("settings")}, onSignature={nav.navigate("signature")})}
  composable("camera"){CameraScreen(onCaptured={bmp->com.lankascan.data.ScanSession.lastCaptured=bmp; nav.navigate("crop")}, onClose={nav.popBackStack()})}
  composable("crop"){CropScreen(onConfirm={c->com.lankascan.data.ScanSession.add(c); nav.navigate("review"){popUpTo("home")}}, onBack={nav.popBackStack()})}
  composable("review"){ReviewScreen(onCreatePdf={nav.navigate("export")}, onBack={nav.navigate("camera")})}
  composable("export"){ExportScreen(onDone={com.lankascan.data.ScanSession.clear(); nav.popBackStack("home",false)})}
  composable("viewer"){PdfViewerScreen(onBack={nav.popBackStack()})}
  composable("settings"){SettingsScreen(onBack={nav.popBackStack()})}
  composable("signature"){SignatureScreen(onSave={b->com.lankascan.data.ScanSession.signature=b; nav.popBackStack()}, onBack={nav.popBackStack()})}
 }
}
