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
import kotlinx.coroutines.launch

//@Composable
@SuppressLint("SuspiciousIndentation")
fun getLocation(context: Context, scope: CoroutineScope?,
                snackBarHostState: SnackbarHostState?,
                coordinate: MutableState<Coordinate>?,
                gpsLocationListener: LocationListener?) {
    if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {


        }else{
           /* val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
              if (locationManager.isLocationEnabled){
                if (gpsLocationListener != null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        5000, 10f,  gpsLocationListener)
                }

               // val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
              //  locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, null


                }
                else{
                  /*  LaunchedEffect(scope){
                        launch {
                        snackBarHostState?.showSnackbar(context.resources.getString(R.string.EnableLocation))
                        }
                    }*/
                }
                
            */
            }
   //return Coordinate(longitude = 0.0, latitude = 0.0)
}
