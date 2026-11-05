package com.lankascan
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import com.lankascan.ui.HomeScreen
class MainActivity: ComponentActivity(){
 override fun onCreate(savedInstanceState: Bundle?){
  super.onCreate(savedInstanceState)
  setContent{ MaterialTheme(colorScheme=darkColorScheme()){ HomeScreen() } }
 }
}
