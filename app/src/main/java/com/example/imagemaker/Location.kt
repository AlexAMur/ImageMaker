package com.example.imagemaker

import android.content.Context
import  android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.core.app.ActivityCompat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

//@Composable

@Composable
fun getPermissionLocation(context: Context):Boolean {

    if(ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED){
        return true
    }
    else{
        return getPermission()
    }
}

@Composable
fun getPermission():Boolean{
    var isPermission= false
    val permissionlauncer=rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){permissions->
        val granted =permissions.values.reduce{acc, next->(acc && next)}
        if(granted){
            isPermission = true
        }
        else{
            isPermission =false
        }
//        when{
//            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)->{
//                isPermission=true
//                return@rememberLauncherForActivityResult
//            }
//            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
//                isPermission=true
//                return@rememberLauncherForActivityResult
//            }
//            else->{
//                return@rememberLauncherForActivityResult
//            }
//        }

    }
    SideEffect {
            permissionlauncer.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ))
        }
    return isPermission
}
