package com.example.imagemaker

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import java.io.FileNotFoundException
import java.io.IOException

fun getScreenSize(context: Context):Pair<Int, Int>{
    val displayMetrics = context.resources.displayMetrics
    //displayMetrics.
    val width =context.resources.displayMetrics.widthPixels
    val height =context.resources.displayMetrics.heightPixels
    return Pair(width, height)
}


fun getImage(context: Context, uri: Uri):Bitmap {
    try {
        val inputstrim = context.contentResolver.openInputStream(uri)
        val  bitmap= BitmapFactory.decodeStream(inputstrim)
        inputstrim?.close()
        return  bitmap
    }
    catch ( fileError : FileNotFoundException){
        Log.e("ImageMaker" ,fileError.message?:"empty")
    }
    return Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
}

@Throws (IOException::class)
fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String?): Uri? {
    var outUri: Uri? =null
    val content = ContentValues().apply {
        if (fileName != null)
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        else
            put(MediaStore.MediaColumns.DISPLAY_NAME, "temp_filename.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, context.getString(R.string.MIME_jpeg))
    }
    outUri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)

    if (outUri != null){
        val outStream = context.contentResolver.openOutputStream(outUri) ?: return null
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
    }
    return outUri
}
//отправка на почту
fun sendBitmap(context: Context, uri: Uri, mailTo: String){
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_EMAIL, arrayOf(mailTo))
        putExtra(Intent.EXTRA_SUBJECT,"act")
        type=context.resources.getString(R.string.MIME_jpeg)
    }
    context.startActivity(Intent.createChooser(intent,null))
}

fun selectImage(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>){
    val intent =  Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        addCategory(Intent.CATEGORY_OPENABLE)

    }
    launcher.launch(intent)
}

