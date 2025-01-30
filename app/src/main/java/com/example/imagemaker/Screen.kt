package com.example.imagemaker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

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
                settings.uri=imageUriPodpis
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