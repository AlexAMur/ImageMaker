package com.example.imagemaker

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.lang.NullPointerException

val Context.dataStore  by preferencesDataStore(name = "settings")
class MyApplication: Application() {
    var settings: Settings?  = Settings()
    var listMarket: Array<Market>? = null // by remember { mutableStateOf<Array<Market>>(emptyArray()) }//
    override fun onCreate() {
        super.onCreate()
        //получаем настройки
            runBlocking {// CoroutineScope(Dispatchers.IO).launch {
                settings = getSettings(applicationContext.dataStore)

            }
       listMarket = createArrayMarket( readJsonFile(applicationContext, "magazin.json"))
    }
private suspend fun  getSettings(context: DataStore<Preferences>):Settings{
    val settings = Settings()
  //
    try {
        context.data.map {
            if (it[stringPreferencesKey(Settings::uri.name)] !="")
                settings.uri = Uri.parse(it[stringPreferencesKey(Settings::uri.name)])
            else
                settings.uri=null
            settings.x = (it[stringPreferencesKey(Settings::x.name)])?.toInt() ?: 0
            settings.y = (it[stringPreferencesKey(Settings::y.name)])?.toInt() ?: 0
        }.first()
    } catch (e: NullPointerException){
        Log.e("ImageMaker",e.message.toString())
        //Toast.makeText(context, "Not settings", Toast.LENGTH_LONG).show()
    }finally {
        return settings
    }

}
}