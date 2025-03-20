package com.example.imagemaker

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.KeyEventDispatcher.Component
import java.security.Permission

fun fileNameFromUri(uri: Uri?):String?{
    if (uri != null && uri?.toString()!="") {
        var fileName = uri.path.toString()
        if (fileName.length > 0 && fileName.lastIndexOf("/") > -1)
            return fileName.substring(fileName.lastIndexOf("/") + 1)  //имя файла
    }

    return "tmp_image.jpeg"
}
fun checkPermission(context: Context):Boolean{
    val requestPermissionLauncher =
        (context as ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){ isGranted: Boolean ->
            if (isGranted) {

                return@registerForActivityResult

            }
            else {
                Toast.makeText(context,context.getString(R.string.PermissionStorage),
                    Toast.LENGTH_LONG).show()
            }

        }
    when{
        ContextCompat.checkSelfPermission(
        context.applicationContext,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED->{

       }

    else->{
        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    }
return true
}