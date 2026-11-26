package com.cookly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cookly.platformName
import com.cookly.ui.components.*

/** Pantalla 8: Descripción de la app y créditos con los nombres del equipo. */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Acerca de") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BrandHeader(subtitle = "Versión 1.0 · Kotlin Multiplataforma")
            Text(
                "Nevera digital: controla los vencimientos de tus alimentos, busca productos por código " +
                    "de barras en Open Food Facts y planea tus comidas con recordatorios.",
            )

            Text("Equipo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Member("María Alejandra Solarte")
            Member("Ronal Santiago Valdez Jurado")

            Text("Tecnologías", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Corriendo en: ${platformName()}", fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Kotlin Multiplatform", "Compose Multiplatform", "Navigation Compose",
                    "Room (SQLite)", "Ktor", "Multiplatform Settings", "MVVM",
                ).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }

            SectionCard {
                Text("Curso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Labeled("Asignatura", "Electiva Desarrollo de Aplicaciones Móviles")
                Labeled("Profesor", "Ph.D. Cristhian Figueroa")
                Labeled("Universidad", "Universidad del Cauca")
            }
        }
    }
}

@Composable
private fun Member(name: String) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name, size = 48)
            Spacer(Modifier.width(14.dp))
            Text(name, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Labeled(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
