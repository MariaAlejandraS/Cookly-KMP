package com.cookly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cookly.data.model.Dates
import com.cookly.data.model.ProductDraft
import com.cookly.data.network.OffProduct
import com.cookly.data.model.foodCategory
import com.cookly.platformName
import com.cookly.ui.components.*
import com.cookly.ui.scan.rememberBarcodeScanner
import com.cookly.ui.theme.CooklyColors
import com.cookly.ui.theme.LocalCookly
import com.cookly.viewmodel.ScanState
import com.cookly.viewmodel.ScanViewModel

/**
 * Pantalla 5: Escanear. En Android abre la cámara; en Desktop se escribe el código.
 * Luego consulta Open Food Facts (red con Ktor) y muestra una tarjeta según el estado.
 */
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onEditDraft: (ProductDraft) -> Unit,
    onShowDetail: (OffProduct) -> Unit,
) {
    val ui by viewModel.ui.collectAsState()
    val openCamera = rememberBarcodeScanner(onScanned = viewModel::handle)

    // Si la API trae el producto sin nombre, se abre el formulario para completarlo.
    LaunchedEffect(ui.state) {
        val state = ui.state
        if (state is ScanState.NeedsDetails) {
            viewModel.rescan()
            onEditDraft(state.draft)
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Escanear", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        val canType = ui.state is ScanState.Idle || ui.state is ScanState.Failed || ui.state is ScanState.NotFound
        if (canType) {
            if (openCamera != null) CameraCard(onClick = openCamera)
            ManualEntry(
                code = ui.code,
                onCodeChange = viewModel::onCodeChange,
                onSearch = viewModel::searchTyped,
                onSample = { viewModel.onCodeChange(it); viewModel.handle(it) },
                hasCamera = openCamera != null,
            )
        }

        when (val state = ui.state) {
            ScanState.Idle, is ScanState.NeedsDetails -> Unit
            ScanState.Loading -> LoadingCard()
            is ScanState.Preview -> PreviewCard(
                state = state,
                quantity = ui.quantity,
                onQuantityChange = viewModel::onQuantityChange,
                onAdd = viewModel::addToFridge,
                onEdit = { viewModel.draftForPreview()?.let(onEditDraft) },
                onDetail = { onShowDetail(state.off) },
                onRescan = viewModel::rescan,
            )
            is ScanState.Added -> SectionCard {
                Text("Agregado a la nevera", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(state.product.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SecondaryButton("Deshacer", onClick = viewModel::undo)
                PrimaryButton("Escanear otro", onClick = viewModel::rescan)
            }
            is ScanState.NotFound -> SectionCard {
                Text("No encontramos este código", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "El código ${state.code} no está en Open Food Facts. Puedes agregarlo a mano.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PrimaryButton("Agregar manualmente", onClick = {
                    viewModel.rescan()
                    onEditDraft(ProductDraft(barcode = state.code))
                })
                SecondaryButton("Escanear otro", onClick = viewModel::rescan)
            }
            is ScanState.Failed -> SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = LocalCookly.current.danger)
                    Spacer(Modifier.width(8.dp))
                    Text("No se pudo buscar", fontWeight = FontWeight.Bold)
                }
                Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                PrimaryButton("Reintentar", onClick = viewModel::retry)
                SecondaryButton("Escanear otro", onClick = viewModel::rescan)
            }
        }
    }
}

@Composable
private fun CameraCard(onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CooklyColors.brandGradient)
            .clickable(onClick = onClick)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = CooklyColors.OnGradient, modifier = Modifier.size(56.dp))
        Text("Escanear con la cámara", color = CooklyColors.OnGradient, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Apunta al código de barras", color = CooklyColors.OnGradient.copy(alpha = 0.75f))
    }
}

@Composable
private fun ManualEntry(
    code: String,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSample: (String) -> Unit,
    hasCamera: Boolean,
) {
    SectionCard {
        Text(
            if (hasCamera) "¿No escanea? Ingresa el código" else "Ingresa el código de barras",
            fontWeight = FontWeight.Bold,
        )
        if (!hasCamera) {
            Text(
                "En ${platformName()} no hay cámara de escaneo: escribe el código y Cookly trae sus datos de Open Food Facts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CooklyTextField(code, onCodeChange, "Código de barras", keyboardType = KeyboardType.Number, leadingIcon = Icons.Filled.QrCode)
        PrimaryButton("Buscar", onClick = onSearch, icon = Icons.Filled.Search)
        Text("Prueba con:", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectChip("Nutella · 3017624010701", selected = false) { onSample("3017624010701") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectChip("Coca-Cola · 5449000000996", selected = false) { onSample("5449000000996") }
        }
    }
}

@Composable
private fun LoadingCard() {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(24.dp), color = CooklyColors.Primary)
            Spacer(Modifier.width(12.dp))
            Text("Buscando producto…")
        }
        Text("Nombre del producto", color = MaterialTheme.colorScheme.outline)
        Text("Categoría", color = MaterialTheme.colorScheme.outline)
    }
}

/** Tarjeta de confirmación: foto, nombre, categoría, fecha sugerida, cantidad y las cuatro acciones. */
@Composable
private fun PreviewCard(
    state: ScanState.Preview,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onDetail: () -> Unit,
    onRescan: () -> Unit,
) {
    val p = LocalCookly.current
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProductImage(state.off.imageUrl, state.category.icon(), size = 72)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(state.off.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(state.category.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Pill("Vence en ${state.category.minDays} días (sugerido)", p.successSoft, p.success)
            }
        }
        if (state.alreadyInFridge) {
            Pill("Ya está en tu nevera", p.warningSoft, p.warning)
        }
        Stepper("Cantidad: $quantity", quantity, onQuantityChange, 1..99)
        PrimaryButton("Agregar a la nevera", onClick = onAdd)
        SecondaryButton("Editar", onClick = onEdit)
        SecondaryButton("Ver detalle", onClick = onDetail)
        TextButton(onClick = onRescan, modifier = Modifier.fillMaxWidth()) {
            Text("Escanear otro", color = p.brandForeground, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}
