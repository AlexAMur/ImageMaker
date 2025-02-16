package com.example.imagemaker

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt


const val radus = 6371008
const val half_circle=180
const val PI = 3.141592653589793
const val gradus_paralel= 111321.377778
const val gradus_meridian=111134.861111

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
fun selectMailMarket(arrayMarket: Array<Market>,coordinates:Coordinate  ):String{
    val delta = arrayOf(0.0)

    for (i in 0..arrayMarket.size-1){
        var tmpArray= arrayOf(0.0)
        var x =((arrayMarket[i].longitude-coordinates.longitude))*
                       (cos((arrayMarket[i].latitude+coordinates.latitude)/2*PI/half_circle) * gradus_paralel)
        x= x*x
        var y =(arrayMarket[i].latitude-coordinates.latitude) * gradus_meridian
        y=y*y
        delta[i]= sqrt(x+y)
    }
    val min=delta.min()
    for(i in 0..delta.size-1){
        if(min == delta[i])
            return arrayMarket[i].mailTo
    }
    return "@krasnoe-beloe.ru"
}
fun selectMail(arrayMarket: Array<Array<Double>>,coordinates:  Pair<Double, Double>):String{
    var delta = emptyArray<Array<Double>>()

    for (i in 0..arrayMarket.size-1){
        val array = arrayOf(0.0,0.0)
        array[0]=abs(arrayMarket[i][0]-coordinates.first)
        array[1]=abs(arrayMarket[i][1]-coordinates.second)
        delta+=array
    }
    var minOne=delta[0][0]
    var minTwo=delta[0][0]
    var minThree=delta[0][0]
    val threeMinIndex=arrayOf(-1,-1,-1)
    for (i in 0.. delta.size-1){
        if (minOne >= delta[i][0]){
            minOne=delta[i][0]
            threeMinIndex[0]= i
            if(minOne < minTwo){
                val tmp=minOne
                minOne=minTwo
                minTwo=tmp
                val index=threeMinIndex[0]
                threeMinIndex[0]=threeMinIndex[1]
                threeMinIndex[1]=index
            }
            if (minTwo < minThree){
                val tmp=minTwo
                minTwo=minThree
                minThree=tmp
                val index=threeMinIndex[1]
                threeMinIndex[1]=threeMinIndex[2]
                threeMinIndex[2]=index
            }
        }
    }
    minOne=delta[threeMinIndex[0]][0]+delta[threeMinIndex[0]][1]
    minTwo=delta[threeMinIndex[1]][0]+delta[threeMinIndex[1]][1]
    minThree=delta[threeMinIndex[2]][0]+delta[threeMinIndex[2]][1]
    if (minOne > minTwo){
        if (minTwo > minThree)
           return "dolgota ${arrayMarket[threeMinIndex[2]][0]} , ${arrayMarket[threeMinIndex[2]][1]}"
        else
          return  "dolgota ${arrayMarket[threeMinIndex[1]][0]} , ${arrayMarket[threeMinIndex[1]][1]}"
    }
    else{
    }
    if(minOne > minThree)
        return "dolgota ${arrayMarket[threeMinIndex[2]][0]} , ${arrayMarket[threeMinIndex[2]][1]}"
    else
      return  "dolgota ${arrayMarket[threeMinIndex[0]][0]} , ${arrayMarket[threeMinIndex[0]][1]}"
}