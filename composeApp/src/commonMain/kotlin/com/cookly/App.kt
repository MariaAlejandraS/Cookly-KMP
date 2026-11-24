package com.cookly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cookly.data.AppContainer
import com.cookly.navigation.AuthNavHost
import com.cookly.navigation.MainNavHost
import com.cookly.ui.theme.CooklyTheme

/**
 * Punto de entrada COMPARTIDO de la interfaz. Android (MainActivity) y Desktop (main.kt)
 * solo llaman App(container): todo lo que se ve sale de aquí.
 */
@Composable
fun App(container: AppContainer) {
    val darkMode by container.prefs.darkMode.collectAsState()
    val user by container.auth.currentUser.collectAsState()
    var restored by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        container.auth.restoreSession()
        restored = true
    }

    CooklyTheme(darkTheme = darkMode) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // En Desktop la ventana puede ser ancha: el contenido se limita a 600 dp centrado.
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Box(Modifier.widthIn(max = 600.dp).fillMaxSize()) {
                    val current = user
                    when {
                        !restored -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        current == null -> AuthNavHost(container)
                        // key: al cambiar de cuenta se crean de cero todas las pantallas y ViewModels.
                        else -> key(current.id) { MainNavHost(container, current) }
                    }
                }
            }
        }
    }
}
