package com.cookly

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cookly.data.AppContainer

/** Punto de entrada en Android: crea las dependencias y muestra la App compartida. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = CooklyAndroid.container(applicationContext)
        setContent { App(container) }
    }
}

/** Guarda un único AppContainer para toda la app (sobrevive a la rotación de pantalla). */
object CooklyAndroid {
    private var instance: AppContainer? = null

    fun container(context: Context): AppContainer =
        instance ?: AppContainer(createDatabase(context)).also { instance = it }
}
