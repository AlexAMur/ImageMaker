package com.example.imagemaker

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException
import kotlin.math.abs

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
fun selectMailMarket(arrayMarket: Array<Market>,coordinates:  Pair<Double, Double>){
    val delta = arrayOf<Array<Double>>()
    for (i in 0..arrayMarket.size){
        delta[i][0]=abs(arrayMarket[i].longitude-coordinates.first)
        delta[i][1]=abs(arrayMarket[i].latitude-coordinates.second)
    }
    var minOne=delta[0][0]
    var minTwo=delta[0][0]
    var minThree=delta[0][0]
    val threeMinIndex=arrayOf(-1,-1,-1)

    for (i in 0.. delta.size){
        if (minOne >= delta[i][0]){
            if(minOne <= minTwo){
                val tmp=minOne
                minOne=minTwo
                minTwo=tmp
                val index=threeMinIndex[0]
                threeMinIndex[0]=threeMinIndex[1]
                threeMinIndex[1]=index
            }
            if (minTwo <= minThree){
                val tmp=minTwo
                minTwo=minThree
                minThree=tmp
                val index=threeMinIndex[1]
                threeMinIndex[1]=threeMinIndex[2]
                threeMinIndex[2]=index
            }
            minOne=delta[i][0]
           threeMinIndex[0]= i
        }
    }
}
fun selectMail(arrayMarket: Array<Array<Double>>,coordinates:  Pair<Double, Double>){
    var delta = emptyArray<Array<Double>>()

    for (i in 0..arrayMarket.size){
        val array = arrayOf(0.0,0.0)
        array[0]=abs(arrayMarket[i][0]-coordinates.first)
        array[1]=abs(arrayMarket[i][1]-coordinates.second)
        delta+=array
    }
    var minOne=delta[0][0]
    var minTwo=delta[0][0]
    var minThree=delta[0][0]
    val threeMinIndex=arrayOf(-1,-1,-1)

    for (i in 0.. delta.size){
        if (minOne >= delta[i][0]){
            if(minOne <= minTwo){
                val tmp=minOne
                minOne=minTwo
                minTwo=tmp
                val index=threeMinIndex[0]
                threeMinIndex[0]=threeMinIndex[1]
                threeMinIndex[1]=index
            }
            if (minTwo <= minThree){
                val tmp=minTwo
                minTwo=minThree
                minThree=tmp
                val index=threeMinIndex[1]
                threeMinIndex[1]=threeMinIndex[2]
                threeMinIndex[2]=index
            }
            minOne=delta[i][0]
            threeMinIndex[0]= i
        }
    }

}