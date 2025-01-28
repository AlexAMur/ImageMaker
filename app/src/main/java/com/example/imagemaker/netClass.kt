package com.example.imagemaker

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.InetAddresses
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat.getSystemService
import java.net.Inet4Address


class netClass {

    fun getIP4Address(context: Context){
        val connectivityManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val currentNet = connectivityManager.scanResults
       val ipv4Address =
        return ipv4Address
    }
    fun checkWIFI(){

    }
}