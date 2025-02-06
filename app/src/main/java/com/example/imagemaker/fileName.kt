package com.example.imagemaker

import android.net.Uri

fun fileNameFromUri(uri: Uri?):String?{
    if (uri != null && uri?.toString()!="") {
        var fileName = uri.path.toString()
        if (fileName.length > 0 && fileName.lastIndexOf("/") > -1)
            return fileName.substring(fileName.lastIndexOf("/") + 1)  //имя файла
    }

    return "tmp_image.jpeg"
}