package com.cookly.data.model

/**
 * Borrador de producto para abrir el formulario ya lleno
 * (desde el escáner, desde el detalle o desde "Agregar manualmente").
 */
data class ProductDraft(
    val name: String = "",
    val category: FoodCategory = FoodCategory.OTROS,
    val quantity: Int = 1,
    val barcode: String? = null,
    val imageUrl: String? = null,
)
