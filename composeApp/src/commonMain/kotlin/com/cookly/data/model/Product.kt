package com.cookly.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un alimento de la nevera. Es una tabla de Room (SQLite).
 * La fecha se guarda como "día época" (días desde 1970-01-01) para compararla fácil.
 */
@Entity(tableName = "products", indices = [Index("ownerId")])
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,              // usuario dueño: cada cuenta ve solo lo suyo
    val name: String,
    val category: String,           // nombre de FoodCategory
    val quantity: Int,
    val expirationEpochDay: Int,
    val isOpened: Boolean = false,
    val barcode: String? = null,    // solo si vino de Open Food Facts
    val imageUrl: String? = null,   // foto del producto (Open Food Facts)
)

enum class ExpiryStatus { EXPIRED, SOON, FRESH }

val Product.foodCategory: FoodCategory get() = FoodCategory.fromName(category)

fun Product.daysLeft(today: Int): Int = expirationEpochDay - today

/** Vencido, por vencer (dentro de los días de aviso) o bien. */
fun Product.status(today: Int, warningDays: Int): ExpiryStatus {
    val days = daysLeft(today)
    return when {
        days < 0 -> ExpiryStatus.EXPIRED
        days <= warningDays -> ExpiryStatus.SOON
        else -> ExpiryStatus.FRESH
    }
}
