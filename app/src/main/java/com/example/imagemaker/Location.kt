package com.example.imagemaker

import android.content.Context
import  android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.core.app.ActivityCompat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun getLocation(context: Context, scope: CoroutineScope, snackbarHostState: SnackbarHostState, coordinate: MutableState<Coordinate>):Coordinate{
    if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {


        }else{
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpslocation: Location?=null
        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val tmpCoordinate =Coordinate(longitude = location.longitude, latitude = location.latitude)
                coordinate.value = tmpCoordinate
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
            if (locationManager.isLocationEnabled){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f,  gpsLocationListener)

               // val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
              //  locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, null
                return Coordinate(longitude = gpslocation?.longitude?:0.0, latitude = gpslocation?.latitude?:0.0)
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
//@Composable
//fun getPermissionlocation():Boolean{
//    var isPermission= false
//    val permissionlauncer=rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()){permissions->
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
//
//    }
//    SideEffect {
//        permissionlauncer.launch(
//            arrayOf(
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//            ))
//    }
//    return isPermission
//}

