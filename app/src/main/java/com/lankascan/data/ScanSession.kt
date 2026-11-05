package com.lankascan.data
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
object ScanSession {
 val pages = mutableStateListOf<Bitmap>()
 var lastCaptured: Bitmap? = null
 fun add(bmp: Bitmap){ pages.add(bmp) }
 fun clear(){ pages.clear(); lastCaptured=null }
 fun replace(index: Int, bmp: Bitmap){ if(index in pages.indices) pages[index]=bmp }
}
