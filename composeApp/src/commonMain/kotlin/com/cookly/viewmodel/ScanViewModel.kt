package com.cookly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookly.data.model.Dates
import com.cookly.data.model.FoodCategory
import com.cookly.data.model.Product
import com.cookly.data.model.ProductDraft
import com.cookly.data.network.ApiError
import com.cookly.data.network.ApiResult
import com.cookly.data.network.FoodApi
import com.cookly.data.network.OffProduct
import com.cookly.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * En qué va el escaneo. Es una máquina de estados (igual a ScanState de iOS):
 * la pantalla dibuja una tarjeta distinta para cada estado.
 */
sealed interface ScanState {
    data object Idle : ScanState
    data object Loading : ScanState
    /** Encontrado: se muestra la tarjeta para confirmar antes de guardar. */
    data class Preview(val off: OffProduct, val category: FoodCategory, val alreadyInFridge: Boolean) : ScanState
    /** Ya se guardó: se puede deshacer o escanear otro. */
    data class Added(val product: Product) : ScanState
    /** La API lo encontró pero sin nombre: el usuario lo completa en el formulario. */
    data class NeedsDetails(val draft: ProductDraft) : ScanState
    data class NotFound(val code: String) : ScanState
    data class Failed(val message: String) : ScanState
}

data class ScanUiState(
    val code: String = "",
    val quantity: Int = 1,
    val state: ScanState = ScanState.Idle,
)

/** ViewModel del escáner: consulta Open Food Facts y guarda el producto en la nevera. */
class ScanViewModel(
    private val api: FoodApi,
    private val products: ProductRepository,
    private val ownerId: Long,
) : ViewModel() {

    private val _ui = MutableStateFlow(ScanUiState())
    val ui: StateFlow<ScanUiState> = _ui.asStateFlow()

    private var lastCode: String = ""

    fun onCodeChange(value: String) = _ui.update { it.copy(code = value.filter(Char::isDigit).take(14)) }

    fun onQuantityChange(value: Int) = _ui.update { it.copy(quantity = value.coerceIn(1, 99)) }

    /** Lo llama la cámara (Android) o el botón Buscar de la entrada manual. */
    fun handle(code: String) {
        val clean = code.filter(Char::isDigit)
        val current = _ui.value.state
        if (current !is ScanState.Idle && current !is ScanState.Failed && current !is ScanState.NotFound) return
        if (clean.length !in 8..14) {
            _ui.update { it.copy(state = ScanState.Failed("Escribe un código de barras de 8 a 14 dígitos.")) }
            return
        }
        lastCode = clean
        _ui.update { it.copy(code = clean, quantity = 1, state = ScanState.Loading) }
        viewModelScope.launch { lookup(clean) }
    }

    fun searchTyped() = handle(_ui.value.code)

    private suspend fun lookup(code: String) {
        val next = when (val response = api.fetchProduct(code)) {
            is ApiResult.Success -> {
                val off = response.value
                val category = FoodCategory.fromOffTags(off.categoriesTags)
                if (off.usableName == null) {
                    ScanState.NeedsDetails(ProductDraft(category = category, barcode = code, imageUrl = off.imageUrl))
                } else {
                    val already = products.findByBarcode(code, ownerId) != null
                    ScanState.Preview(off.copy(code = off.code.ifEmpty { code }), category, already)
                }
            }
            is ApiResult.Failure ->
                if (response.error == ApiError.NOT_FOUND) ScanState.NotFound(code)
                else ScanState.Failed(response.error.message)
        }
        _ui.update { it.copy(state = next) }
    }

    /** "Agregar a la nevera": guarda con la fecha sugerida por la categoría. */
    fun addToFridge() {
        val preview = _ui.value.state as? ScanState.Preview ?: return
        viewModelScope.launch {
            val product = Product(
                ownerId = ownerId,
                name = preview.off.displayName,
                category = preview.category.name,
                quantity = _ui.value.quantity,
                expirationEpochDay = Dates.today() + preview.category.minDays,
                barcode = preview.off.code,
                imageUrl = preview.off.imageUrl,
            )
            val id = products.add(product)
            _ui.update { it.copy(state = ScanState.Added(product.copy(id = id))) }
        }
    }

    /** "Editar": borrador para abrir el formulario lleno con lo que trajo la API. */
    fun draftForPreview(): ProductDraft? {
        val preview = _ui.value.state as? ScanState.Preview ?: return null
        return ProductDraft(
            name = preview.off.displayName,
            category = preview.category,
            quantity = _ui.value.quantity,
            barcode = preview.off.code,
            imageUrl = preview.off.imageUrl,
        )
    }

    /** El formulario guardó un producto que venía del escáner: queda como "Agregado". */
    fun noteSaved(product: Product) = _ui.update { it.copy(state = ScanState.Added(product)) }

    /** "Deshacer": borra el producto recién agregado. */
    fun undo() {
        val added = _ui.value.state as? ScanState.Added ?: return
        viewModelScope.launch {
            products.delete(added.product)
            rescan()
        }
    }

    fun rescan() = _ui.update { ScanUiState() }

    fun retry() {
        if (lastCode.isEmpty()) return rescan()
        _ui.update { it.copy(state = ScanState.Loading) }
        viewModelScope.launch { lookup(lastCode) }
    }
}
