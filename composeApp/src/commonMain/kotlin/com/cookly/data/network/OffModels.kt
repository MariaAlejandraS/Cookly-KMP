package com.cookly.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Forma del JSON que responde Open Food Facts.
 * @Serializable = kotlinx.serialization genera el código para convertir JSON <-> objeto.
 * @SerialName traduce el nombre del JSON (product_name) al de Kotlin (productName).
 */
@Serializable
data class OffResponse(
    val status: Int = 0,              // 1 = encontrado, 0 = no existe
    val product: OffProduct? = null,
)

@Serializable
data class OffProduct(
    val code: String = "",
    @SerialName("product_name") val productName: String? = null,
    val brands: String? = null,
    val quantity: String? = null,
    @SerialName("image_front_small_url") val imageUrl: String? = null,
    @SerialName("nova_group") val novaGroup: Int? = null,
    @SerialName("nutriscore_grade") val nutriscoreGrade: String? = null,
    @SerialName("categories_tags") val categoriesTags: List<String> = emptyList(),
    @SerialName("ingredients_text") val ingredientsText: String? = null,
    val allergens: String? = null,
) {
    val displayName: String
        get() = usableName ?: "Producto sin nombre"

    /** El nombre solo si sirve para guardar; si viene vacío, el usuario debe escribirlo. */
    val usableName: String?
        get() = productName?.trim()?.takeIf { it.isNotEmpty() }
}
