package com.cookly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookly.data.model.Dates
import com.cookly.data.model.FoodCategory
import com.cookly.data.model.Product
import com.cookly.data.model.ProductDraft
import com.cookly.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddProductUiState(
    val name: String = "",
    val category: FoodCategory = FoodCategory.OTROS,
    val quantity: Int = 1,
    val isOpened: Boolean = false,
    val autoDate: Boolean = true,
    val expirationEpochDay: Int = Dates.today() + FoodCategory.OTROS.minDays,
    val error: String? = null,
    val isEditing: Boolean = false,
)

/**
 * ViewModel del formulario Agregar / Editar producto. Tres formas de abrirlo:
 * - productId: edita un producto que ya existe.
 * - draft: producto nuevo con los datos que trajo Open Food Facts (escáner o detalle).
 * - ninguno: producto nuevo vacío.
 */
class AddProductViewModel(
    private val products: ProductRepository,
    private val ownerId: Long,
    private val productId: Long? = null,
    private val draft: ProductDraft? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(AddProductUiState())
    val state: StateFlow<AddProductUiState> = _state.asStateFlow()

    private var editing: Product? = null
    private val barcode: String? = draft?.barcode
    private val imageUrl: String? = draft?.imageUrl

    init {
        if (draft != null) {
            _state.value = AddProductUiState(
                name = draft.name,
                category = draft.category,
                quantity = draft.quantity,
                expirationEpochDay = suggested(draft.category),
            )
        }
        if (productId != null) {
            viewModelScope.launch {
                val product = products.find(productId, ownerId) ?: return@launch
                editing = product
                _state.value = AddProductUiState(
                    name = product.name,
                    category = FoodCategory.fromName(product.category),
                    quantity = product.quantity,
                    isOpened = product.isOpened,
                    autoDate = false,
                    expirationEpochDay = product.expirationEpochDay,
                    isEditing = true,
                )
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }

    /** Al cambiar la categoría, si la fecha sugerida está activa, se recalcula: hoy + días mínimos. */
    fun onCategoryChange(value: FoodCategory) = _state.update {
        it.copy(category = value, expirationEpochDay = if (it.autoDate) suggested(value) else it.expirationEpochDay)
    }

    fun onQuantityChange(value: Int) = _state.update { it.copy(quantity = value.coerceIn(1, 99)) }

    fun onOpenedChange(value: Boolean) = _state.update { it.copy(isOpened = value) }

    fun onAutoDateChange(value: Boolean) = _state.update {
        it.copy(autoDate = value, expirationEpochDay = if (value) suggested(it.category) else it.expirationEpochDay)
    }

    fun onDateChange(epochDay: Int) = _state.update { it.copy(expirationEpochDay = epochDay, autoDate = false) }

    /**
     * Valida y guarda. Llama onSaved con el producto guardado y si era nuevo,
     * para que la pantalla se cierre y se muestre el aviso.
     */
    fun save(onSaved: (product: Product, isNew: Boolean) -> Unit) {
        val s = _state.value
        val name = s.name.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(error = "Escribe el nombre del producto.") }
            return
        }
        viewModelScope.launch {
            val current = editing
            if (current != null) {
                val updated = current.copy(
                    name = name,
                    category = s.category.name,
                    quantity = s.quantity,
                    isOpened = s.isOpened,
                    expirationEpochDay = s.expirationEpochDay,
                )
                products.update(updated)
                onSaved(updated, false)
            } else {
                val product = Product(
                    ownerId = ownerId,
                    name = name,
                    category = s.category.name,
                    quantity = s.quantity,
                    isOpened = s.isOpened,
                    expirationEpochDay = s.expirationEpochDay,
                    barcode = barcode,
                    imageUrl = imageUrl,
                )
                val id = products.add(product)
                onSaved(product.copy(id = id), true)
            }
        }
    }

    private fun suggested(category: FoodCategory): Int = Dates.today() + category.minDays
}
