package com.cookly.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

/** Rutas de la app. La navegación vive en commonMain: la misma en Android y Desktop. */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Nevera : Screen("nevera")
    data object AddProduct : Screen("add_product")
    data object Scan : Screen("scan")
    data object Detail : Screen("detail")
    data object Reminders : Screen("reminders")
    data object Profile : Screen("profile")
    data object About : Screen("about")
}

/** Las cuatro pestañas de la barra inferior (las mismas de la versión iOS). */
enum class AppTab(val screen: Screen, val label: String, val icon: ImageVector) {
    NEVERA(Screen.Nevera, "Nevera", Icons.Filled.Kitchen),
    SCAN(Screen.Scan, "Escanear", Icons.Filled.QrCodeScanner),
    REMINDERS(Screen.Reminders, "Recordatorios", Icons.Filled.Notifications),
    PROFILE(Screen.Profile, "Perfil", Icons.Filled.Person),
}
