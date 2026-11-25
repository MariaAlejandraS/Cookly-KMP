package com.cookly.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Utilidades de fecha compartidas (kotlinx-datetime funciona en todas las plataformas). */
object Dates {
    private const val MILLIS_PER_DAY = 86_400_000L
    private val weekdays = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    private val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
    )

    fun today(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays()

    fun format(epochDay: Int): String {
        val d = LocalDate.fromEpochDays(epochDay)
        return "${d.dayOfMonth}/${d.monthNumber}/${d.year}"
    }

    /** "Lunes, 23 de noviembre" */
    fun longToday(): String {
        val d = LocalDate.fromEpochDays(today())
        return "${weekdays[d.dayOfWeek.ordinal]}, ${d.dayOfMonth} de ${months[d.monthNumber - 1]}"
    }

    fun toMillis(epochDay: Int): Long = epochDay * MILLIS_PER_DAY
    fun fromMillis(millis: Long): Int = (millis / MILLIS_PER_DAY).toInt()

    fun expiryLabel(daysLeft: Int): String = when {
        daysLeft < -1 -> "Venció hace ${-daysLeft} días"
        daysLeft == -1 -> "Venció ayer"
        daysLeft == 0 -> "Vence hoy"
        daysLeft == 1 -> "Vence mañana"
        else -> "$daysLeft días"
    }

    fun time(hour: Int, minute: Int): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}
