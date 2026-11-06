package com.lankascan.data
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
object ScanSession{
 val pages=mutableStateListOf<Bitmap>()
 var lastCaptured:Bitmap?=null
 var currentPdfPath:String?=null
 var signature:Bitmap?=null
 fun add(b:Bitmap){pages.add(b)}
 fun clear(){pages.clear(); lastCaptured=null}
 fun replace(i:Int,b:Bitmap){if(i in pages.indices) pages[i]=b}
 fun remove(i:Int){if(i in pages.indices) pages.removeAt(i)}
 fun move(from:Int,to:Int){if(from in pages.indices && to in pages.indices){val item=pages.removeAt(from); pages.add(to,item)}}
}
