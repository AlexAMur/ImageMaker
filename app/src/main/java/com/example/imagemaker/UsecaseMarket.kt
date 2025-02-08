package com.example.imagemaker

import android.content.Context
import java.io.FileNotFoundException

fun readJsonFile(context: Context,fileName:String):String {
    try {
        return context.assets.open(fileName)
            .bufferedReader().use {
                it.readText()
            }
    }catch (e:FileNotFoundException){

    }
    finally {
        return "empty"
    }
}