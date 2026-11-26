package com.cookly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cookly.platformName
import com.cookly.ui.components.*
import com.cookly.ui.theme.LocalCookly
import com.cookly.viewmodel.ProfileViewModel

/** Pantalla 7: Perfil. Nombre, modo oscuro, días de aviso, Acerca de y cerrar sesión. */
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onAbout: () -> Unit) {
    val user by viewModel.user.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val warningDays by viewModel.warningDays.collectAsState()
    var nameDraft by remember(user?.id) { mutableStateOf(user?.name.orEmpty()) }
    var confirmLogout by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(user?.name.orEmpty(), size = 88)
        Text(user?.name.orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("@" + user?.username.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionCard {
            CooklyTextField(nameDraft, { nameDraft = it }, "Tu nombre")
            if (nameDraft.trim() != user?.name && nameDraft.isNotBlank()) {
                PrimaryButton("Guardar nombre", onClick = { viewModel.saveName(nameDraft) })
            }
        }

        SectionCard {
            Text("Preferencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SwitchRow("Modo oscuro", darkMode, viewModel::setDarkMode)
            Stepper("Avisarme $warningDays días antes", warningDays, viewModel::setWarningDays, 1..7)
            Text(
                "Tema actual: ${if (darkMode) "Oscuro" else "Claro"} · ${platformName()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(Modifier.clickable(onClick = onAbout)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Filled.Info, size = 40)
                Spacer(Modifier.width(12.dp))
                Text("Acerca de Cookly", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        // Cerrar sesión: cápsula roja suave, igual que en iOS
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CircleShape)
                .background(LocalCookly.current.dangerSoft)
                .clickable { confirmLogout = true },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = LocalCookly.current.danger)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", color = LocalCookly.current.danger, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus datos quedan guardados en este dispositivo.") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; viewModel.logout() }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } },
        )
    }
}
