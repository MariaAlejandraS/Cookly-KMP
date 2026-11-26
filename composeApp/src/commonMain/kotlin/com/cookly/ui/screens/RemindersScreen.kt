package com.cookly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cookly.data.model.Dates
import com.cookly.data.model.Reminder
import com.cookly.data.model.repeats
import com.cookly.ui.components.*
import com.cookly.ui.theme.CooklyColors
import com.cookly.ui.theme.LocalCookly
import com.cookly.viewmodel.ALL_DAYS
import com.cookly.viewmodel.RemindersViewModel
import com.cookly.viewmodel.WEEKDAYS
import com.cookly.viewmodel.WEEKEND

/** Pantalla 6: Recordatorios de comidas (de una vez o repetidos). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: RemindersViewModel) {
    val reminders by viewModel.list.collectAsState()
    val form by viewModel.form.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Recordatorios", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Pill(Dates.longToday(), CooklyColors.Primary, androidx.compose.ui.graphics.Color.White)
            }
        }
        item {
            SectionCard {
                Text("Nuevo recordatorio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                CooklyTextField(form.title, viewModel::onTitleChange, "Ej. Desayuno", error = form.error)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.presets.forEach { preset ->
                        SelectChip(
                            text = "${preset.title} ${Dates.time(preset.hour, preset.minute)}",
                            selected = form.title == preset.title && form.hour == preset.hour && form.minute == preset.minute,
                            onClick = { viewModel.applyPreset(preset) },
                        )
                    }
                }
                SegmentedChips(
                    options = listOf(false, true),
                    selected = form.repeats,
                    label = { if (it) "Se repite" else "Una vez" },
                    onSelect = viewModel::onRepeatsChange,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (form.repeats) "Hora" else "Fecha y hora", Modifier.weight(1f))
                    if (!form.repeats) {
                        OutlinedButton(onClick = { showDatePicker = true }) { Text(Dates.format(form.onceEpochDay), color = LocalCookly.current.ink) }
                        Spacer(Modifier.width(8.dp))
                    }
                    OutlinedButton(onClick = { showTimePicker = true }) { Text(Dates.time(form.hour, form.minute), color = LocalCookly.current.ink) }
                }
                if (form.repeats) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        RemindersViewModel.dayLetters.forEachIndexed { index, letter ->
                            DayCircle(letter, selected = form.daysMask and (1 shl index) != 0) { viewModel.toggleDay(index) }
                        }
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectChip("Todos los días", form.daysMask == ALL_DAYS) { viewModel.setDays(ALL_DAYS) }
                        SelectChip("Entre semana", form.daysMask == WEEKDAYS) { viewModel.setDays(WEEKDAYS) }
                        SelectChip("Fines de semana", form.daysMask == WEEKEND) { viewModel.setDays(WEEKEND) }
                    }
                }
                PrimaryButton("Agregar recordatorio", onClick = viewModel::add, icon = Icons.Filled.Add)
            }
        }
        if (reminders.isEmpty()) {
            item {
                Text(
                    "Aún no hay recordatorios.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }
        }
        items(reminders, key = { it.id }) { reminder ->
            ReminderCard(reminder, onToggle = { viewModel.setEnabled(reminder, it) }, onDelete = { viewModel.delete(reminder) })
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = form.hour, initialMinute = form.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { viewModel.onTimeChange(timeState.hour, timeState.minute); showTimePicker = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timeState) },
        )
    }
    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = Dates.toMillis(form.onceEpochDay))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { viewModel.onDateChange(Dates.fromMillis(it)) }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = dateState) }
    }
}

@Composable
private fun DayCircle(letter: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (selected) CooklyColors.brandGradient else androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.surfaceVariant))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            letter,
            fontWeight = FontWeight.Bold,
            color = if (selected) CooklyColors.OnGradient else LocalCookly.current.ink,
        )
    }
}

@Composable
private fun ReminderCard(reminder: Reminder, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Filled.Restaurant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.title, fontWeight = FontWeight.SemiBold)
                Text(
                    Dates.time(reminder.hour, reminder.minute),
                    style = MaterialTheme.typography.titleLarge,
                    color = LocalCookly.current.brandForeground,
                    fontWeight = FontWeight.Bold,
                )
                val detail = if (reminder.repeats) RemindersViewModel.describeDays(reminder.daysMask)
                else "Una vez · ${reminder.onceEpochDay?.let { Dates.format(it) } ?: ""}"
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = reminder.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = CooklyColors.Primary, checkedThumbColor = androidx.compose.ui.graphics.Color.White),
            )
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, contentDescription = "Borrar") }
        }
    }
}
