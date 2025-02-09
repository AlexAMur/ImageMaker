package com.example.imagemaker

import android.content.Context
import android.util.Log
import androidx.compose.ui.layout.LayoutCoordinates
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException

fun readJsonFile(context: Context,fileName:String):String {
    try {
        return context.assets.open(fileName)
            .bufferedReader().use {
                it.readText()
            }
    }catch (e:FileNotFoundException){
        Log.e("IM",context.resources.getString(R.string.FileNotFound)+" $fileName")
    }
    return ""
}
fun createArrayMarket( stringJson: String):Array<Market>{
    val gson = Gson()
    val marketListType = object : TypeToken<Array<Market>>() {}.type
    val listMarket:Array<Market> =gson.fromJson(stringJson, marketListType)
    return listMarket
}
fun selectMailMarket(arrayMarket: Array<Market>,coordinates:  Pair<Double, Double>):String{
    val size=arrayMarket.size
    val endList=
        return ""
}