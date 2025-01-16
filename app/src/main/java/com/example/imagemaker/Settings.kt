package com.example.imagemaker

import android.net.Uri

data class Settings(
    var x :Int =-1,
    var y :Int =-1,
    var uri: Uri? =null
)
