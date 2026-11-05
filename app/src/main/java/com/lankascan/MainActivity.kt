package com.lankascan
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
@Composable fun LankaTheme(c: @Composable ()->Unit){ MaterialTheme(colorScheme = darkColorScheme(), content=c) }
class MainActivity: ComponentActivity(){ override fun onCreate(s: Bundle?){ super.onCreate(s); setContent{ LankaTheme{ com.lankascan.ui.NavGraph() } } } }
