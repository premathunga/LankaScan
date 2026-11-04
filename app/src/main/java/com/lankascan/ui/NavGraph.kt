package com.lankascan.ui
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.lankascan.ui.screens.*
@Composable fun NavGraph(){ val nav=rememberNavController(); NavHost(navController=nav, startDestination="home"){ composable("home"){ HomeScreen(onScan={nav.navigate("camera")}) }; composable("camera"){ CameraScreen(onCaptured={nav.navigate("crop")}, onClose={nav.popBackStack()}) }; composable("crop"){ CropScreen(onConfirm={nav.navigate("review")}) }; composable("review"){ ReviewScreen(onCreatePdf={nav.navigate("export")}) }; composable("export"){ ExportScreen(onDone={nav.popBackStack("home", false)}) } } }
