package com.example.imagemaker

import android.content.Context
import  android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun Getlocation(context: Context, scope: CoroutineScope, snackbarHostState: SnackbarHostState):Coordinate{
    if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
           if(getPermissionlocation()){
               Getlocation(context, scope, snackbarHostState)
           }

        }else{
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isLocationEnabled){
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                Text("Coordinate:dol-${location?.longitude} shir-${location?.latitude}")
                return Coordinate(longitude = location?.longitude?:0.0, latitude = location?.latitude?:0.0)
                }
                else{
                       LaunchedEffect(scope){
                           launch {
                               snackbarHostState.showSnackbar(context.resources.getString(R.string.EnableLocation))
                           }
                       }
                }

            }
   return Coordinate(longitude = 0.0, latitude = 0.0)
}
@Composable
fun getPermissionlocation():Boolean{
    var isPermission= false
    val permissionlauncer=rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){permissions->
        when{
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                isPermission=true
                return@rememberLauncherForActivityResult
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                isPermission=true
                return@rememberLauncherForActivityResult
            }
            else->{
                return@rememberLauncherForActivityResult
            }
        }

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
