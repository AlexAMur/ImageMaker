package com.example.imagemaker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.MailTo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.datastore.preferences.preferencesDataStore
import com.example.imagemaker.ui.theme.ImageMakerTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalStateException
import kotlin.coroutines.CoroutineContext

class MainActivity : ComponentActivity() {

    var settings:Settings =Settings()
    var scope:CoroutineScope? =null
    var snackBarHostState: SnackbarHostState? = null
    var coordinate: MutableState<Coordinate>? =null

    var gpsLocationListener: LocationListener? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app =(application as MyApplication)
        settings = app.settings?:Settings()

        setContent {

             scope = rememberCoroutineScope()
             snackBarHostState = remember { SnackbarHostState() }
             coordinate = remember { mutableStateOf(Coordinate()) }


            var imageUriPodpis by remember { mutableStateOf<Uri?>(null) }
            var mainUri: Uri? = null
            if (settings.uri != null) {
                imageUriPodpis = settings.uri
            }
            ImageMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
            val intent = intent
            when {
                intent?.action == Intent.ACTION_SEND -> {
                    if (this.resources.getString(R.string.MIME_jpeg) == intent.type) {

                    var tmpUri:Uri? =null
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                              tmpUri=intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                        else
                                tmpUri =intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)

                        tmpUri?.let { it ->
                            mainUri = it

                            GetContentExample(this, mainUri, imageUriPodpis, settings,scope,snackBarHostState!!)
                        }
                    } else {
                        Snackbar {
                            Text(this.resources.getString(R.string.not_support_format))
                        }
                    }
                }
                intent?.action == Intent.ACTION_MAIN -> {
                    GetContentExample(this, mainUri, imageUriPodpis, settings,scope,snackBarHostState!!)
                    }

                 }

                }

            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (settings!= (application as MyApplication).settings){
            CoroutineScope(Dispatchers.IO).launch {
                saveSettings( applicationContext, settings)
            }
        }
    }

    override fun onStart() {
        super.onStart()

          getLocation(this, scope,snackBarHostState, coordinate, gpsLocationListener)

    }

    override fun onPause() {
        super.onPause()
        gpsLocationListener =null
    }

    override fun onResume() {
        super.onResume()
        gpsLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val tmpCoordinate =Coordinate(longitude = location.longitude,
                    latitude = location.latitude)
                Log.e("ImageMLocation","Определение координат!!!!!!!!!!!!!!" )
                coordinate?.value = tmpCoordinate
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }
}
suspend fun saveSettings(context: Context, settings: Settings){
// сохранение настроек
    if(settings.uri!=null) {
        context.dataStore.edit {
            it[stringPreferencesKey(Settings::uri.name)] = settings.uri.toString()
            it[stringPreferencesKey(Settings::x.name)] = settings.x.toString()
            it[stringPreferencesKey(Settings::y.name)] = settings.y.toString()
        }
    }
}

