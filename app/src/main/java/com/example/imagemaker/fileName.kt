package com.example.imagemaker

import android.net.Uri

fun fileNameFromUri(uri: Uri):String?{
   var fileName = uri.path.toString()
    if (fileName.length > 0 && fileName.lastIndexOf("/") > -1)
      return   fileName.substring(fileName.lastIndexOf("/") + 1)  //имя файла
    else
        return "tmp_image.jpeg"
}