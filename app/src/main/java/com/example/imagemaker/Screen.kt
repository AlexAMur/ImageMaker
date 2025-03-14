package com.example.imagemaker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.KeyEventDispatcher.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GetContentExample(
    context: Context, mainUri: Uri?,
    UriPodpis: Uri?, settings: Settings, scope:CoroutineScope?,
    snackBarHostState: SnackbarHostState,
    mailTo: String
) {

    //val coordinate = remember { mutableStateOf(Coordinate()) }
    var imageUriPodpis by remember { mutableStateOf<Uri?>(UriPodpis) }
    var fileName:String? = null
    var imageUri_main by remember { mutableStateOf<Uri?>(null) }
    if (mainUri != null){
        imageUri_main = mainUri  //тут по кругу
        fileName=fileNameFromUri(imageUri_main)
      }
    var editImage by remember { mutableStateOf<Boolean>(value = false) }
    var bitmapPodpis = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    var mbitmap = bitmapPodpis.copy(Bitmap.Config.ARGB_8888, true)

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            imageUriPodpis = it.data?.data
            if (imageUriPodpis != null) {
                settings.uri=imageUriPodpis
                val contentResolver = context.contentResolver

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(imageUriPodpis!!, takeFlags)
            }

        }
//    val launcher_Pod =
//        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            if (uri != null) {
//                val contentResolver = context.contentResolver
//
//                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
//                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//// Check for the freshest data.
//                contentResolver.takePersistableUriPermission(uri, takeFlags)
//                imageUriPodpis = uri
//
//            }
//            settings.uri = imageUriPodpis
//            //val contentResolver =context.contentResolver
//
////        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//// Check for the freshest data.
//            //    contentResolver.takePersistableUriPermission(imageUriPodpis!!, takeFlags)
//            //saveSettings(context, Settings(imageUriPodpis.toString()) )
//        }
    val launcher_main =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri_main = uri
                editImage = false
                fileName = fileNameFromUri(uri)
            }
        }


    val launcher_l =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            var streamInput: InputStream? = null
            var streamOut: FileOutputStream? = null
            if (uri != null) {
                try {
                    val contentResolver = context.contentResolver
                    streamInput = contentResolver.openInputStream(uri)

                    val file = File(context.filesDir,context.getString(R.string.fileName))
                    streamOut=file.outputStream()
                    val buffer = ByteArray(1024)
                    var length: Int

                    while (streamInput!!.read(buffer).also { length = it } > 0)
                    {
                        streamOut.write(buffer, 0, length)

                    }

                }
                catch (error: FileNotFoundException){
                    Toast.makeText(context,
                        context.resources.getString(R.string.FileError),
                        Toast.LENGTH_LONG).show()
                }
                finally {
                    streamInput?.close()
                    streamOut?.close()
                }


            }

        }
  //+++++++++++++++++++++++++++++++++++++++



    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }){
        Column {

       /*     if (isGooglePlayServicesAvailable(context)==SERVICE_INVALID) {
                LaunchedEffect(scope) {
                    snackbarHostState.showSnackbar("Google play service не поддерживаются!")
                }
            }*/

          //  getLocation(context,scope, snackBarHostState,coordinate)

             //aaa listMarket = createArrayMarket( readJsonFile(context, "magazin.json"))
            // mailTo = selectMailMarket(listMarket,coordinate.value)//Pair(30.35687977917871,59.932240884442095))
            //Text("Координаты Д: ${coordinate.value.longitude} Ш: ${coordinate.value.latitude}")
            Text("mailTo: $mailTo", modifier = Modifier.fillMaxWidth().padding(10.dp,5.dp,5.dp,10.dp))

            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = {

                       launcher_l.launch(context.resources.getString(R.string.MIME_File))

                   }) {
                    Text(text = "Загузить магазины")
                }
                Button(onClick = { launcher_main.launch(context.resources.getString(R.string.MIME_jpeg)) }) {
                    Text(text = "Выбрать акт")
                }

                Button(onClick = { selectImage(launcher) }) {
                    Text(text = "Подписать")
                }
//                Button(onClick = {
//                        //протестить координаты
//                        val listMarket = createArrayMarket( readJsonFile(context, "magazin.json"))
//                        val mailTo = selectMailMarket(listMarket,coordinate.value)
//                }) {
//                    Icon(Icons.Filled.Share, contentDescription = "Поделиться")
//                }


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
                    Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.Center) {
                        Button(modifier = Modifier.padding(start = 10.dp, end = 20.dp),onClick = {
                            saveBitmap(context, mbitmap, fileName)
                            scope?.launch {
                                snackBarHostState.showSnackbar("${context.resources.getString(R.string.saveMassage)} $fileName.")
                            }
                        }) {
                            Text("Сохранить")
                        }
                        Button(modifier = Modifier.padding(start = 20.dp, end = 10.dp), onClick = {
                            val uri = saveBitmap(context, mbitmap, fileName)
                            if (uri != null) {
                                //протестить координаты
                               // val listMarket = createArrayMarket( readJsonFile(context, "magazin.json"))
                                //val mailTo = selectMailMarket(listMarket,coordinate.value)//Pair(30.35687977917871,59.932240884442095))
                                sendBitmap(context, uri, mailTo)
                            }

                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Поделиться")
                        }
                    }
                }
            }

        }
    }
}