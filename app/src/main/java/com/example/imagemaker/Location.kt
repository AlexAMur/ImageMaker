package com.example.imagemaker

import android.content.Context
import  android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.core.app.ActivityCompat


@Composable
fun Getlocation(context: Context){
//context.fus


        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
           getPermissionlocation()
        }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    Text("Coordinate:dol-${location?.longitude} shir-${location?.latitude}")
}
@Composable
fun getPermissionlocation():Boolean{
    var isPermission= false
    val permissionlauncer=rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){permissions->
        when{
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                isPermission=true
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                isPermission=true
            }
            else->{
                isPermission=false
            }
        }

    }
    SideEffect {

        permissionlauncer.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

    return false
}
