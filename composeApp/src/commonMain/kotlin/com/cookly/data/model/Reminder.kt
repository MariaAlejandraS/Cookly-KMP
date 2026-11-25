package com.cookly.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Recordatorio de comida. Tabla de Room. */
@Entity(tableName = "reminders", indices = [Index("ownerId")])
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val title: String,
    val hour: Int,
    val minute: Int,
    /** Días de la semana como bits: lunes = 1, martes = 2, miércoles = 4 ... domingo = 64. 0 = una sola vez. */
    val daysMask: Int,
    /** Fecha (día época) cuando es de una sola vez. */
    val onceEpochDay: Int? = null,
    val enabled: Boolean = true,
)

val Reminder.repeats: Boolean get() = daysMask != 0
