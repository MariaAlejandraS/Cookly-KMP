package com.cookly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookly.data.model.Dates
import com.cookly.data.model.Reminder
import com.cookly.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealPreset(val title: String, val hour: Int, val minute: Int)

data class ReminderForm(
    val title: String = "Desayuno",
    val hour: Int = 7,
    val minute: Int = 30,
    val repeats: Boolean = false,
    val daysMask: Int = ALL_DAYS,
    val onceEpochDay: Int = Dates.today(),
    val error: String? = null,
)

const val ALL_DAYS = 0b1111111
const val WEEKDAYS = 0b0011111
const val WEEKEND = 0b1100000

class RemindersViewModel(
    private val reminders: ReminderRepository,
    private val ownerId: Long,
) : ViewModel() {

    val presets = listOf(MealPreset("Desayuno", 7, 30), MealPreset("Almuerzo", 12, 30), MealPreset("Cena", 19, 0))

    /** La lista viene directo de Room como Flow: se actualiza sola al agregar, activar o borrar. */
    val list: StateFlow<List<Reminder>> = reminders.observe(ownerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(ReminderForm())
    val form: StateFlow<ReminderForm> = _form.asStateFlow()

    fun onTitleChange(value: String) = _form.update { it.copy(title = value, error = null) }

    fun applyPreset(preset: MealPreset) = _form.update {
        it.copy(title = preset.title, hour = preset.hour, minute = preset.minute, error = null)
    }

    fun onTimeChange(hour: Int, minute: Int) = _form.update { it.copy(hour = hour, minute = minute) }

    fun onRepeatsChange(value: Boolean) = _form.update { it.copy(repeats = value) }

    fun onDateChange(epochDay: Int) = _form.update { it.copy(onceEpochDay = epochDay) }

    /** Prende o apaga un día. Cada día es un bit: lunes = bit 0 ... domingo = bit 6. */
    fun toggleDay(index: Int) = _form.update { it.copy(daysMask = it.daysMask xor (1 shl index)) }

    fun setDays(mask: Int) = _form.update { it.copy(daysMask = mask) }

    fun add() {
        val f = _form.value
        val title = f.title.trim()
        when {
            title.isEmpty() -> { _form.update { it.copy(error = "Escribe un nombre para el recordatorio.") }; return }
            f.repeats && f.daysMask == 0 -> { _form.update { it.copy(error = "Elige al menos un día.") }; return }
        }
        viewModelScope.launch {
            reminders.add(
                Reminder(
                    ownerId = ownerId,
                    title = title,
                    hour = f.hour,
                    minute = f.minute,
                    daysMask = if (f.repeats) f.daysMask else 0,
                    onceEpochDay = if (f.repeats) null else f.onceEpochDay,
                )
            )
        }
    }

    fun setEnabled(reminder: Reminder, enabled: Boolean) {
        viewModelScope.launch { reminders.setEnabled(reminder, enabled) }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch { reminders.delete(reminder) }
    }

    companion object {
        val dayLetters = listOf("L", "M", "X", "J", "V", "S", "D")

        fun describeDays(mask: Int): String = when (mask) {
            ALL_DAYS -> "Todos los días"
            WEEKDAYS -> "Entre semana"
            WEEKEND -> "Fines de semana"
            else -> dayLetters.filterIndexed { i, _ -> mask and (1 shl i) != 0 }.joinToString(" ")
        }
    }
}
