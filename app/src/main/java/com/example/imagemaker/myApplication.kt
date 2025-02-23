package com.example.imagemaker

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class myApplication: Application() {
    var settings: Settings?  = null
    var coroutineJob:Job? = null
    override fun onCreate() {
        super.onCreate()
        //получаем настройки
         coroutineJob = CoroutineScope(Dispatchers.IO).launch {
            settings = getSettings(applicationContext)
        }
    }
}