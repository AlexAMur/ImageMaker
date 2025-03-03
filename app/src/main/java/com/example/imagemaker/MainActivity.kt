package com.example.imagemaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.imagemaker.ui.theme.ImageMakerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    var settings: Settings = Settings()
    var scope: CoroutineScope? = null
    var snackBarHostState: SnackbarHostState? = null
    var coordinate: MutableState<Coordinate>? = null
    var gpsLocationListener: LocationListener? = null



    @SuppressLint("SuspiciousIndentation", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = (application as MyApplication)
        settings = app.settings ?: Settings()

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ){ isGranted: Boolean ->
        if (isGranted) {
            app.locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        } else {
           Toast.makeText(this,"НЕт", Toast.LENGTH_LONG).show()
        }
    }

    when{ ContextCompat.checkSelfPermission(
        applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED->{
    }
        else->{
             requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

        setContent {

            scope = rememberCoroutineScope()
            snackBarHostState = remember { SnackbarHostState() }
            coordinate = remember { mutableStateOf(Coordinate()) }
            var mailTo  by remember { mutableStateOf("")  }
            var imageUriPodpis by remember { mutableStateOf<Uri?>(null) }
            var mainUri: Uri? = null
            if (settings.uri != null) {
                imageUriPodpis = settings.uri
            }


            ImageMakerTheme {
//                if (getPermission()){
//
//                    if (app.locationManager?.isLocationEnabled == true){
//                        gpsLocationListener = object : LocationListener {
//                            override fun onLocationChanged(location: Location) {
//                                val tmpCoordinate = Coordinate(
//                                    longitude = location.longitude,
//                                    latitude = location.latitude
//                                )
//                                Log.e("ImageMLocation", "Определение координат!!!")
//                                coordinate?.value = tmpCoordinate
//                                val market =(application as MyApplication).listMarket
//                                mailTo = selectMailMarket(market!!, coordinate?.value!!)//Pair(30.35687977917871,59.932240884442095))
//                            }
//                            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//                            override fun onProviderEnabled(provider: String) {}
//                            override fun onProviderDisabled(provider: String) {}
//                        }
//                        if (gpsLocationListener != null) {
//                            app.locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                                5000, 10f,  gpsLocationListener!!)
//                        }
//                    }
//                    else{
//                        LaunchedEffect(scope){
//                            launch {
//                                snackBarHostState?.showSnackbar(applicationContext.resources.getString(R.string.EnableLocation))
//                            }
//                        }
//                    }
//                }
//                else {
//                    LaunchedEffect(scope) {
//                        launch {
//                            snackBarHostState?.showSnackbar(applicationContext.resources.getString(R.string.PermissionLocation))
//                        }
//                    }
//                }





                    // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val intent = intent

                    when {
                        intent?.action == Intent.ACTION_SEND -> {
                            if (this.resources.getString(R.string.MIME_jpeg) == intent.type) {

                                var tmpUri: Uri? = null
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                                    tmpUri =
                                        intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                                else
                                    tmpUri = intent.getParcelableExtra(
                                        Intent.EXTRA_STREAM,
                                        Uri::class.java
                                    )

                                tmpUri?.let { it ->
                                    mainUri = it

                                    GetContentExample(
                                        this,
                                        mainUri,
                                        imageUriPodpis,
                                        settings,
                                        scope,
                                        snackBarHostState!!,
                                        mailTo
                                    )
                                }
                            } else {
                                Snackbar {
                                    Text(this.resources.getString(R.string.not_support_format))
                                }
                            }
                        }

                        intent?.action == Intent.ACTION_MAIN -> {
                            GetContentExample(
                                this,
                                mainUri,
                                imageUriPodpis,
                                settings,
                                scope,
                                snackBarHostState!!,
                                mailTo
                            )
                        }

                    }

                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settings != (application as MyApplication).settings) {
            CoroutineScope(Dispatchers.IO).launch {
                saveSettings(applicationContext, settings)
            }
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
      //  getLocation(this, scope, snackBarHostState, coordinate, gpsLocationListener)
    }
}
suspend fun saveSettings(context: Context, settings: Settings) {
// сохранение настроек
    if (settings.uri != null) {
        context.dataStore.edit {
            it[stringPreferencesKey(Settings::uri.name)] = settings.uri.toString()
            it[stringPreferencesKey(Settings::x.name)] = settings.x.toString()
            it[stringPreferencesKey(Settings::y.name)] = settings.y.toString()
        }
    }
}

