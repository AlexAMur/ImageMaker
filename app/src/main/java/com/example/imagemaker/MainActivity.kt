package com.example.imagemaker

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.imagemaker.ui.theme.ImageMakerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


val Context.dataStore  by preferencesDataStore(name = "settings")
var settings = Settings()
class MainActivity : ComponentActivity() {
    lateinit var fusedLocationClient: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //получаем настройки
        val corut = CoroutineScope(Dispatchers.IO).launch {
            settings=getSettings(applicationContext)
        }
        getlocation(this)

       setContent {
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
                        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { it ->
                            mainUri = it
                            GetContentExample(this, mainUri, imageUriPodpis)
                        }
                    } else {
                        Snackbar {
                            Text(this.resources.getString(R.string.not_support_format))
                        }
                        //Toast.makeText(this, "Не поддерживаемый формат.", Toast.LENGTH_LONG).show()
                    }
                }
                intent?.action == Intent.ACTION_MAIN -> {

                GetContentExample(this, mainUri, imageUriPodpis)

                        }

                 }

                }

            }
        }
    }
override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            saveSettings( applicationContext, settings)
        }
    }
}

suspend fun  getSettings(context: Context):Settings{
    val settings = Settings()
//    if (context.dataStore.data == null){
//        Toast.makeText(context, "Not settings",Toast.LENGTH_LONG).show()
//    }
    try {
        context.dataStore.data.map {
            if (it[stringPreferencesKey(Settings::uri.name)] !="")
            settings.uri = Uri.parse(it[stringPreferencesKey(Settings::uri.name)])
            else
                settings.uri=null
            settings.x = (it[stringPreferencesKey(Settings::x.name)])?.toInt() ?: 0
            settings.y = (it[stringPreferencesKey(Settings::y.name)])?.toInt() ?: 0
        }.first()
    } catch (e: Exception){
        Log.e("ImageMaker",e.message.toString())
        Toast.makeText(context, "Not settings",Toast.LENGTH_LONG).show()
    }finally {
        return settings
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

//suspend fun getImage(context: Context, uri: Uri):Bitmap {
fun getImage(context: Context, uri: Uri):Bitmap {
    try {
        val inputstrim = context.contentResolver.openInputStream(uri)
        val  bitmap=BitmapFactory.decodeStream(inputstrim)
        inputstrim?.close()
        return  bitmap
    }
    catch ( fileError : FileNotFoundException){
        Log.e("ImageMaker" ,fileError.message?:"empty")
    }
   return Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
}


fun getScreenSize(context: Context):Pair<Int, Int>{
    val width =context.resources.displayMetrics.widthPixels
    val height =context.resources.displayMetrics.heightPixels
    return Pair(width, height)
}
fun saveFileToDownloads(context: Context, fileName: String, bitmap: Bitmap) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
        // Запрашиваем разрешение
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1000 // Код запроса разрешения
        )
    } else {
        // Если разрешение уже предоставлено, продолжаем
        writeFile(fileName, bitmap)
    }
    // Проверяем разрешение на запись во внешнее хранилище
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
        // Запрашиваем разрешение
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1000 // Код запроса разрешения
        )
    } else {
        // Если разрешение уже предоставлено, продолжаем
        writeFile(fileName, bitmap)
    }
}

private fun writeFile(fileName: String, fileData: Bitmap) {
    // Получаем путь к папке Download
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val file = File(downloadDir, fileName)

    //try {
        // Открываем поток для записи
        val outStream=FileOutputStream(file)
        fileData .compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
  /*  } catch (e: IOException) {
        e.printStackTrace()
    }*/
}
@Throws (IOException::class)
fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String?):Uri? {
    var outUri: Uri? =null
    val content = ContentValues().apply {
        if (fileName != null)
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        else
            put(MediaStore.MediaColumns.DISPLAY_NAME, "temp_filename.jpg")
        }
         outUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)

        if (outUri != null){
        val outStream = context.contentResolver.openOutputStream(outUri)
         if (outStream == null)
            return null
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
    }
    return outUri
}
//отправка на почту
fun sendBitmap(context: Context, uri: Uri){

  val intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_EMAIL, arrayOf("adress@gmail.com"))
      putExtra(Intent.EXTRA_SUBJECT,"act")
     type=context.resources.getString(R.string.MIME_jpeg)
  }
    context.startActivity(Intent.createChooser(intent,null))
}

fun selectImage(context: Context,launcher: ManagedActivityResultLauncher<Intent, ActivityResult>){
    val intent =  Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        addCategory(Intent.CATEGORY_OPENABLE)

    }
    launcher.launch(intent)
}
