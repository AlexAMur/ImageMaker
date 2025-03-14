package com.example.imagemaker

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.cos
import kotlin.math.sqrt

const val half_circle=180
const val PI = 3.141592653589793
const val gradus_paralel= 111321.377778
const val gradus_meridian=111134.861111

fun readJsonFile(context: Context,fileName:String):String? {
    try {
        return File(context.filesDir,fileName)
            .bufferedReader().use {
                it.readText()
            }
    }catch (e:FileNotFoundException){
        Log.e("IM",context.resources.getString(R.string.FileNotFound)+" $fileName")
    }
    return null
}
fun createArrayMarket( stringJson: String):Array<Market>{
    val gson = Gson()
    val marketListType = object : TypeToken<Array<Market>>() {}.type
    val listMarket:Array<Market> =gson.fromJson(stringJson, marketListType)
    return listMarket
}
fun selectMailMarket(arrayMarket: Array<Market>?,coordinates:Coordinate  ):String{
    if (arrayMarket!= null){
        var delta = emptyArray<Double>()
        for (i in 0..arrayMarket.size-1){
            var x =((arrayMarket[i].longitude-coordinates.longitude))*
                       (cos((arrayMarket[i].latitude+coordinates.latitude)/2*PI/half_circle) * gradus_paralel)
            x= x*x
            var y =(arrayMarket[i].latitude-coordinates.latitude) * gradus_meridian
            y=y*y
             delta+= sqrt(x+y)
         }
        val min=delta.min()
        for(i in 0..delta.size-1){
            if(min == delta[i])
                return arrayMarket[i].mailTo
        }
    }
    return "@krasnoe-beloe.ru"
}


