package com.example.imagemaker

import android.location.Location
import android.net.Uri

data class Settings(
    var x :Int =-1,
    var y :Int =-1,
    var uri: Uri? =null,
    var permissionLocationDenied: Boolean = false
)
