package com.lankascan.data
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
object ScanSession{
 val pages=mutableStateListOf<Bitmap>()
 var lastCaptured:Bitmap?=null
 fun add(b:Bitmap){pages.add(b)}
 fun clear(){pages.clear(); lastCaptured=null}
 fun replace(i:Int,b:Bitmap){if(i in pages.indices) pages[i]=b}
}
