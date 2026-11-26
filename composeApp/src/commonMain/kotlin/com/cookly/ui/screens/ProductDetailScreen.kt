package com.cookly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cookly.data.model.FoodCategory
import com.cookly.data.model.ProductDraft
import com.cookly.ui.components.*
import com.cookly.ui.theme.CooklyColors
import com.cookly.ui.theme.LocalCookly
import com.cookly.viewmodel.ProductDetailViewModel

/** Pantalla 6: Detalle del producto con los datos de Open Food Facts. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onClose: () -> Unit,
    onAdd: (ProductDraft) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val product = state.product
    val category = FoodCategory.fromOffTags(product.categoriesTags)
    val p = LocalCookly.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle") },
                navigationIcon = { TextButton(onClick = onClose) { Text("Cerrar", color = p.brandForeground) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
                PrimaryButton(
                    "Agregar a mi nevera",
                    icon = Icons.Filled.Add,
                    onClick = {
                        onAdd(
                            ProductDraft(
                                name = product.usableName.orEmpty(),
                                category = category,
                                barcode = product.code,
                                imageUrl = product.imageUrl,
                            )
                        )
                    },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Foto grande sobre un fondo suave de la marca
            Box(
                Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.matchParentSize().alpha(0.35f).background(CooklyColors.brandGradient))
                Icon(Icons.Filled.Image, contentDescription = null, tint = p.brandForeground, modifier = Modifier.size(48.dp))
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.displayName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    )
                }
            }

            Text(product.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            product.brands?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                product.nutriscoreGrade?.takeIf { it.length == 1 }?.let { NutriScoreBadge(it) }
                product.novaGroup?.let { Pill("NOVA $it", p.surface, p.ink) }
                product.quantity?.let { Pill(it, p.surface, p.ink) }
            }

            InfoCard(
                "Categoría en Cookly",
                "${category.label}. Recomendado: ${category.minDays} a ${category.maxDays} días en refrigeración.",
            )
            product.ingredientsText?.takeIf { it.isNotBlank() }?.let { InfoCard("Ingredientes", it) }
            product.allergens?.takeIf { it.isNotBlank() }?.let { InfoCard("Alérgenos", it.replace("en:", "").replace(",", ", ")) }

            if (state.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = CooklyColors.Primary)
                    Spacer(Modifier.width(10.dp))
                    Text("Cargando detalle…")
                }
            }
            state.error?.let { ErrorText(it) }
            Text(
                "Datos de Open Food Facts (ODbL).",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
