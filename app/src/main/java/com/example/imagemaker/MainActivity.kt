package com.example.imagemaker


import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
        CoroutineScope(Dispatchers.IO).launch {
            settings=getSettings(applicationContext)
        }
        setContent {
            var imageUriPodpis by remember { mutableStateOf<Uri?>(null) }
            var mainUri :Uri? =null

            val intent = getIntent()
            when{
                intent?.action == Intent.ACTION_SEND->{
                    if ("image/jpeg" == intent.type){
                        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {it->
                            // Update UI to reflect image being shared
                            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                            mainUri =it
                        }
                    }
                    else{
                        Toast.makeText(this, "Не поддерживаемый формат. ", Toast.LENGTH_LONG).show()
                    }
                }
            }


            if (settings?.uri != null){
                imageUriPodpis=settings?.uri
            }
            ImageMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GetContentExample(this ,mainUri, imageUriPodpis)
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
fun isValidUri(uriString: String): Boolean {
    return try {
        val uri = Uri.parse(uriString)
        uri.scheme != null && uri.host != null
    } catch (e: Exception) {
        false
    }
}
fun doesUriExist(contentResolver: ContentResolver, uri: Uri): Boolean {
    return try {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.count > 0
        } ?: false
    } catch (e: Exception) {
        false
    }
}
suspend fun  getSettings(context: Context):Settings{
    val settings = Settings()
    context.dataStore.data.map {
        if (isValidUri(it[stringPreferencesKey(Settings::uri.name)]?:"")){
        settings.uri= Uri.parse(it[stringPreferencesKey(Settings::uri.name)])
        }
        settings.x =  (it[stringPreferencesKey(Settings::x.name)])?.toInt() ?: 0
        settings.y =  (it[stringPreferencesKey(Settings::y.name)])?.toInt() ?: 0
    }.first()

    return settings
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


fun getImage(context: Context, uri: Uri):Bitmap {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
        }
    }
    try {
       // val  file =uri.toFile()
        //val inputstrim=file.outputStream()
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


@Composable
fun GetContentExample(context: Context, mainUri: Uri?,
                      UriPodpis: Uri? ){
    var imageUriPodpis  by remember { mutableStateOf<Uri?>(UriPodpis) }
    var imageUri_main by remember { mutableStateOf<Uri?>(null) }
    if (mainUri != null)
            imageUri_main = mainUri  //тут по кругу
    var editImage by remember { mutableStateOf<Boolean>(value = false)}
    var bitmapPodpis= Bitmap.createBitmap(300,300,Bitmap.Config.ARGB_8888)
    var mbitmap = bitmapPodpis.copy(Bitmap.Config.ARGB_8888, true)
    val permissionOpenLauncher =
        rememberLauncherForActivityResult (
            ActivityResultContracts.CreateDocument("image/jpeg")
        ) {
           // здесь сохранить файл с помощью uri
                saveFileToDownloads(context ,"test.jpeg", mbitmap)
                // app.
        }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult (
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                bitmapPodpis = getImage(context, imageUriPodpis!!)
                // app.
            } else Toast.makeText(context,
                                    "Для автоматического открытия изображения подкписи требуется разрешение на доступ к файлам",
                                    Toast.LENGTH_LONG
                                  ).show()
        }
    val launcher_Pod = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri!= null)
            imageUriPodpis=uri
        settings.uri = imageUriPodpis
        //val contentResolver =context.contentResolver

//        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
    //    contentResolver.takePersistableUriPermission(imageUriPodpis!!, takeFlags)
        //saveSettings(context, Settings(imageUriPodpis.toString()) )
    }
    val launcher_main = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri_main=uri
        editImage=false
        //return@rememberLauncherForActivityResult
    }
    Column {
        Row {
            Button(onClick = { launcher_main.launch("image/*") }) {
                Text(text = "Load Image")
            }
            Button(onClick = { launcher_Pod.launch("image/*") }) {
                Text(text = "Select image podpis.")
            }
        }
        if (imageUri_main != null) {
            val inputstrim = context.contentResolver.openInputStream(imageUri_main!!)
            //launcher_Pod.launch("image/*")
            //  val istrimPod = context.contentResolver.openInputStream(imageUri_Pod!!)
            val bitmap = BitmapFactory.decodeStream(inputstrim)
             mbitmap= bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if(imageUriPodpis!=null) {
                val paint = Paint()
                val canvas = Canvas(mbitmap.asImageBitmap())
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                         bitmapPodpis = getImage(context, imageUriPodpis!!)
                        //Log.d("ExampleScreen","Code requires permission")
                    }
                    else -> {
                        // Asking for permission
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                    mbitmap.width.toFloat() / 4 * 3 + 75,
                    mbitmap.height.toFloat() / 4 * 3 - 80
                )
                canvas.drawImage(scalePod.asImageBitmap(), offset1, paint)
                canvas.save()
                editImage=true
            }
            Image(painter = BitmapPainter(mbitmap.asImageBitmap()), contentDescription = "Image")
            if (editImage){
                Row{
                    Button(onClick = {
                        saveBitmap(context, mbitmap, "test.jpeg")
                    }) {
                        Text("Save")
                    }
                    Button(onClick = {


                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Поделиться")
                    }
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
fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String):Boolean{
    return try {
        val content = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "test.jpeg")
        }
        val outUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)

        if (outUri != null){
        val outStream = context.contentResolver.openOutputStream(outUri)
         if (outStream == null)
             false
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream!!)
        outStream.flush()
        outStream.close()
        true
        }
        else false
    }
    catch (e: IOException){
        e.printStackTrace()
        false
    }
}
