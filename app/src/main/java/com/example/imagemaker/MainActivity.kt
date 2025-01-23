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
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //получаем настройки
        val corut = CoroutineScope(Dispatchers.IO).launch {
            settings=getSettings(applicationContext)
        }

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
    if (context.dataStore.data == null){
        Toast.makeText(context, "Not settings",Toast.LENGTH_LONG).show()
    }
    try {
        context.dataStore.data.map {
            settings.uri = Uri.parse(it[stringPreferencesKey(Settings::uri.name)])
            settings.x = (it[stringPreferencesKey(Settings::x.name)])?.toInt() ?: 0
            settings.y = (it[stringPreferencesKey(Settings::y.name)])?.toInt() ?: 0
        }.first()
    } catch (e: Exception){
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
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
 fun GetContentExample(context: Context, mainUri: Uri?,
                              UriPodpis: Uri? ) {
    var imageUriPodpis by remember { mutableStateOf<Uri?>(UriPodpis) }
    var fileName = ""
    var imageUri_main by remember { mutableStateOf<Uri?>(null) }
    if (mainUri != null)
        imageUri_main = mainUri  //тут по кругу
    var editImage by remember { mutableStateOf<Boolean>(value = false) }
    var bitmapPodpis = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    var mbitmap = bitmapPodpis.copy(Bitmap.Config.ARGB_8888, true)

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            imageUriPodpis = it.data?.data
            if (imageUriPodpis != null) {
                val contentResolver = context.contentResolver

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(imageUriPodpis!!, takeFlags)
            }
        }


//    val permissionOpenLauncher =
//        rememberLauncherForActivityResult (ActivityResultContracts.CreateDocument(context.resources.getString(R.string.MIME_jpeg))) {
//           // здесь сохранить файл с помощью uri
//                 if (fileName !="")
//                     saveFileToDownloads(context ,fileName, mbitmap)
//                else
//                    saveFileToDownloads(context ,"imageMaker_tmp.jpeg", mbitmap)
//                // app.
//        }

    /*    val requestPermissionLauncher =
        rememberLauncherForActivityResult (
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            //if (isGranted) {
             //CoroutineScope(Dispatchers.IO).launch {
                bitmapPodpis = getImage(context, imageUriPodpis!!)
           // }
                // app.
          //  } else Toast.makeText(context,
              //                      "Для автоматического открытия изображения подкписи требуется разрешение на доступ к файлам",
                //                    Toast.LENGTH_LONG).show()
        }*/

    val launcher_Pod =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val contentResolver = context.contentResolver

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                imageUriPodpis = uri

            }
            settings.uri = imageUriPodpis
            //val contentResolver =context.contentResolver

//        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
            //    contentResolver.takePersistableUriPermission(imageUriPodpis!!, takeFlags)
            //saveSettings(context, Settings(imageUriPodpis.toString()) )
        }
    val launcher_main =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri_main = uri
                fileName = uri.path.toString()
                editImage = false
                if (fileName.length > 0 && fileName.lastIndexOf("/") > -1)
                    fileName = fileName.substring(fileName.lastIndexOf("/") + 1)  //имя файла
            }
        }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
        // Screen content
    Column {
        Row {
            Button(onClick = { launcher_main.launch(context.resources.getString(R.string.MIME_jpeg)) }) {
                Text(text = "Load Image")
            }

            Button(onClick = { selectImage(context, launcher) }) {
                Text(text = "Select podpis.")
            }
        }
        if (imageUri_main != null) {
            val inputstrim = context.contentResolver.openInputStream(imageUri_main!!)
            //launcher_Pod.launch("image/*")
            //  val istrimPod = context.contentResolver.openInputStream(imageUri_Pod!!)
            val bitmap = BitmapFactory.decodeStream(inputstrim)
            mbitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (imageUriPodpis != null) {
                val paint = Paint()
                val canvas = Canvas(mbitmap.asImageBitmap())
                bitmapPodpis = getImage(context, imageUriPodpis!!)
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                        // CoroutineScope(Dispatchers.IO).launch {
                        bitmapPodpis = getImage(context, imageUriPodpis!!)
                        //   }
                        //Log.d("ExampleScreen","Code requires permission")
                    }

                    else -> {
                        // Asking for permission
                        //requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                val screenSize = getScreenSize(context)
                val newWidth = bitmapPodpis.width * screenSize.first / mbitmap.width
                val newHeight = bitmapPodpis.height * screenSize.second / mbitmap.height
                val scalePod = Bitmap.createScaledBitmap(
                    bitmapPodpis, (newWidth * 0.6).toInt(),
                    (newHeight * 0.6).toInt(), true
                )
                val offset1 = Offset(
                    mbitmap.width.toFloat() / 4 * 3 + 80,
                    mbitmap.height.toFloat() / 4 * 3 - 80
                )
                canvas.drawImage(scalePod.asImageBitmap(), offset1, paint)
                canvas.save()
                editImage = true
            }
            Image(painter = BitmapPainter(mbitmap.asImageBitmap()), contentDescription = "Image")
            if (editImage) {
                Row {
                    Button(onClick = {
                        saveBitmap(context, mbitmap, fileName)
                        scope.launch {
                            snackbarHostState.showSnackbar("${context.resources.getString(R.string.saveMassage)} $fileName.")
                        }
                    }) {
                        Text("Save")
                    }

                }
            }
            Button(onClick = {
                val uri = saveBitmap(context, mbitmap, fileName)
                if (uri != null) {
                    sendBitmap(context, uri)
                }

            }) {
                Icon(Icons.Filled.Share, contentDescription = "Поделиться")
            }
        }

    }
}
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
