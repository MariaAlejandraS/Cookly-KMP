package com.cookly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cookly.data.auth.Validators
import com.cookly.ui.components.*
import com.cookly.viewmodel.AuthViewModel

/** Pantalla 1: Inicio de sesión. */
@Composable
fun LoginScreen(viewModel: AuthViewModel, onGoToRegister: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BrandHeader()
        BenefitRow(Icons.Filled.CalendarMonth, "Controla vencimientos", "Ves qué se acaba primero.")
        BenefitRow(Icons.Filled.QrCode, "Busca productos reales", "Datos de Open Food Facts.")
        BenefitRow(Icons.Filled.Restaurant, "Planea tus comidas", "Recordatorios junto a la nevera.")

        CooklyTextField(
            value = state.username,
            onValueChange = viewModel::onUsernameChange,
            label = "Usuario",
            error = state.usernameError,
            leadingIcon = Icons.Filled.Person,
        )
        PasswordField(state.password, viewModel::onPasswordChange)
        ErrorText(state.message)

        PrimaryButton("Iniciar sesión", onClick = viewModel::login, enabled = state.canLogin)
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        TextButton(onClick = onGoToRegister, modifier = Modifier.fillMaxWidth()) {
            Text("¿No tienes cuenta? Regístrate", fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Pantalla 2: Registro. Si sale bien, la sesión queda abierta y la App pasa sola a la Nevera. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registro") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BrandHeader()
            CooklyTextField(state.name, viewModel::onNameChange, "Nombre", error = state.nameError, leadingIcon = Icons.Filled.Badge)
            CooklyTextField(state.username, viewModel::onUsernameChange, "Usuario", error = state.usernameError, leadingIcon = Icons.Filled.Person)
            PasswordField(state.password, viewModel::onPasswordChange, error = state.passwordError)
            Text(Validators.PASSWORD_HINT, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton("Crear cuenta", onClick = viewModel::register, enabled = !state.busy)
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("¿Ya tienes cuenta? Inicia sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, title: String, detail: String) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
