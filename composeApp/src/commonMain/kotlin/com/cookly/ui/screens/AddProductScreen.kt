package com.cookly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cookly.data.model.Dates
import com.cookly.ui.components.*
import com.cookly.viewmodel.AddProductViewModel

/** Pantalla 4: Agregar o editar un producto. Formulario con TextField, categorías, contador, interruptores y fecha. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    onClose: () -> Unit,
    onSaved: (product: com.cookly.data.model.Product, isNew: Boolean) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditing) "Editar producto" else "Nuevo producto") },
                navigationIcon = { TextButton(onClick = onClose) { Text("Cancelar", color = com.cookly.ui.theme.LocalCookly.current.brandForeground) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard {
                CooklyTextField(state.name, viewModel::onNameChange, "Nombre (ej. Leche entera)", error = state.error)
                Text("Categoría", fontWeight = FontWeight.SemiBold)
                CategoryGrid(selected = state.category, onSelect = viewModel::onCategoryChange)
                Stepper("Cantidad: ${state.quantity}", state.quantity, viewModel::onQuantityChange, 1..99)
                SwitchRow("Empaque abierto", state.isOpened, viewModel::onOpenedChange)
            }

            SectionCard {
                Text("Vencimiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val today = Dates.today()
                val days = state.expirationEpochDay - today
                StatusPill(days, when {
                    days < 0 -> com.cookly.data.model.ExpiryStatus.EXPIRED
                    days <= 3 -> com.cookly.data.model.ExpiryStatus.SOON
                    else -> com.cookly.data.model.ExpiryStatus.FRESH
                })
                SwitchRow("Sugerir fecha por categoría", state.autoDate, viewModel::onAutoDateChange)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vence el", Modifier.weight(1f))
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.Event, contentDescription = null, tint = com.cookly.ui.theme.CooklyColors.Primary)
                        Spacer(Modifier.width(6.dp))
                        Text(Dates.format(state.expirationEpochDay), color = com.cookly.ui.theme.LocalCookly.current.ink)
                    }
                }
            }

            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Filled.Info, size = 40)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Recomendado: ${state.category.minDays} a ${state.category.maxDays} días en refrigeración. " +
                            "La fecha sugerida usa el mínimo; ajústala según el empaque.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            PrimaryButton("Guardar", onClick = { viewModel.save(onSaved = onSaved) })
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = Dates.toMillis(state.expirationEpochDay))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onDateChange(Dates.fromMillis(it)) }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = com.cookly.ui.theme.CooklyColors.Primary, checkedThumbColor = androidx.compose.ui.graphics.Color.White),
        )
    }
}
