package com.cookly.data.model

/**
 * Categorías de alimentos con sus días de duración en refrigeración
 * (guías FDA FoodSafety.gov / USDA FoodKeeper).
 */
enum class FoodCategory(val label: String, val minDays: Int, val maxDays: Int) {
    CARNE_MOLIDA("Carne molida", 1, 2),
    CORTES_CARNE("Cortes enteros de carne", 3, 5),
    POLLO("Pollo o pavo crudo", 1, 2),
    PESCADO("Pescado y mariscos crudos", 1, 2),
    COCIDA("Comida cocida / sobras", 3, 4),
    EMBUTIDOS("Embutidos o fiambres abiertos", 3, 5),
    LACTEOS("Lácteos abiertos", 5, 7),
    VERDURAS_HOJA("Verduras de hoja", 3, 7),
    FRUTAS_BLANDAS("Frutas blandas", 1, 2),
    FRUTAS_FIRMES("Frutas firmes", 5, 7),
    OTROS("Otros / enlatados abiertos", 3, 5);

    companion object {
        /** En la base se guarda el nombre del enum; si no se reconoce, queda en OTROS. */
        fun fromName(name: String?): FoodCategory = entries.firstOrNull { it.name == name } ?: OTROS

        /** Traduce las etiquetas de Open Food Facts (en:dairies, en:meats...) a una categoría propia. */
        fun fromOffTags(tags: List<String>): FoodCategory {
            val text = tags.joinToString(" ").lowercase()
            return when {
                "ground" in text || "minced" in text -> CARNE_MOLIDA
                "poultr" in text || "chicken" in text || "turkey" in text -> POLLO
                "fish" in text || "seafood" in text -> PESCADO
                "sausage" in text || "cold-cut" in text || "deli-meat" in text -> EMBUTIDOS
                "meat" in text -> CORTES_CARNE
                "dair" in text || "milk" in text || "yogurt" in text || "cheese" in text -> LACTEOS
                "leaf" in text || "salad" in text || "lettuce" in text || "spinach" in text -> VERDURAS_HOJA
                "berr" in text || "banana" in text || "peach" in text -> FRUTAS_BLANDAS
                "fruit" in text -> FRUTAS_FIRMES
                "meal" in text || "prepared" in text -> COCIDA
                else -> OTROS
            }
        }
    }
}
