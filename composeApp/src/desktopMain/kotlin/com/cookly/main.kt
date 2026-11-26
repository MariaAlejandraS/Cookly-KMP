package com.cookly

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.cookly.data.AppContainer

/** Punto de entrada en Desktop: abre una ventana y muestra la misma App compartida. */
fun main() = application {
    val container = remember { AppContainer(createDatabase()) }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Cookly",
        state = rememberWindowState(width = 430.dp, height = 680.dp),
    ) {
        App(container)
    }
}
