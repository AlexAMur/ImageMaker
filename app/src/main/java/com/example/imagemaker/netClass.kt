package com.example.imagemaker

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService


class netClass {

    fun getIP4Address(context: Context){
        val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
        val currentNet = connectivityManager.getActiveNetwork()
       val ipv4Address = getIP4Address()
    }
    fun checkWIFI(){

    }
}