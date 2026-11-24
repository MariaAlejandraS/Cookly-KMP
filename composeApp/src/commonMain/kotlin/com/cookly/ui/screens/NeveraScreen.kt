package com.cookly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cookly.data.model.Product
import com.cookly.data.model.daysLeft
import com.cookly.data.model.foodCategory
import com.cookly.data.model.status
import com.cookly.ui.components.*
import com.cookly.ui.theme.CooklyColors
import com.cookly.ui.theme.LocalCookly
import com.cookly.viewmodel.NeveraViewModel
import com.cookly.viewmodel.StatusFilter

/** Pantalla 3: la Nevera. Lista ordenada por vencimiento, con búsqueda, filtros y botón para agregar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeveraScreen(
    viewModel: NeveraViewModel,
    userName: String,
    onAdd: () -> Unit,
    onEdit: (Product) -> Unit,
    onScan: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    // Estado local de la vista: qué producto se va a borrar mientras se muestra la confirmación.
    var toDelete by remember { mutableStateOf<Product?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.all.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = state.search,
                        onValueChange = viewModel::onSearchChange,
                        placeholder = { Text("Buscar en mi nevera") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item { SummaryCard(userName, state.all.size, state.soonCount, state.expiredCount) }
            if (state.all.isNotEmpty()) {
                item {
                    SegmentedChips(
                        options = StatusFilter.entries,
                        selected = state.filter,
                        label = { it.label },
                        onSelect = viewModel::onFilterChange,
                    )
                }
            }
            when {
                state.loaded && state.all.isEmpty() -> item {
                    IllustratedEmpty(
                        icon = Icons.Filled.Kitchen,
                        title = "Tu nevera está vacía",
                        message = "Agrega un alimento o escanea su código.",
                        primaryTitle = "Agregar producto",
                        onPrimary = onAdd,
                        secondaryTitle = "Escanear código",
                        onSecondary = onScan,
                    )
                }
                state.visible.isEmpty() && state.all.isNotEmpty() -> item {
                    Text(
                        "No hay productos con ese filtro.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    )
                }
                else -> items(state.visible, key = { it.id }) { product ->
                    // Deslizar a la izquierda pide confirmación para eliminar (como swipeActions en iOS).
                    val swipe = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) toDelete = product
                            false // la fila vuelve a su lugar; se borra solo si confirma
                        },
                    )
                    SwipeToDismissBox(
                        state = swipe,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = { DeleteBackground() },
                    ) {
                        ProductCard(
                            product = product,
                            statusPill = { StatusPill(product.daysLeft(state.today), product.status(state.today, state.warningDays)) },
                            onClick = { onEdit(product) },
                        )
                    }
                }
            }
        }

        // Botón flotante + con el degradado de marca
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(CooklyColors.brandGradient)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Add, contentDescription = "Agregar producto", tint = CooklyColors.OnGradient, modifier = Modifier.size(30.dp)) }
    }

    toDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar este producto?") },
            text = { Text("Se borrará \"${product.name}\" de tu nevera.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(product); toDelete = null }) {
                    Text("Eliminar", color = LocalCookly.current.danger)
                }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun DeleteBackground() {
    Row(
        Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(CooklyColors.Primary).padding(end = 24.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color.White)
            Text("Eliminar", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SummaryCard(userName: String, total: Int, soon: Int, expired: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CooklyColors.brandGradient)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Hola, $userName",
            color = CooklyColors.OnGradient,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Row(Modifier.fillMaxWidth()) {
            Counter("Total", total, Modifier.weight(1f))
            Counter("Por vencer", soon, Modifier.weight(1f))
            Counter("Vencidos", expired, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Counter(label: String, value: Int, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", color = CooklyColors.OnGradient, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, color = CooklyColors.OnGradient, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProductCard(product: Product, statusPill: @Composable () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalCookly.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(product.imageUrl, product.foodCategory.icon(), size = 52)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val opened = if (product.isOpened) " · abierto" else ""
                Text(
                    "${product.foodCategory.label} · x${product.quantity}$opened",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            statusPill()
        }
    }
}
