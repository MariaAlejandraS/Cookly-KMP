package com.cookly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookly.data.network.ApiResult
import com.cookly.data.network.FoodApi
import com.cookly.data.network.OffProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val product: OffProduct,
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel del detalle: muestra de una vez lo que ya se tiene
 * y pide el detalle completo por código de barras (segunda llamada a la API).
 */
class ProductDetailViewModel(private val api: FoodApi, initial: OffProduct) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState(initial))
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        val code = _state.value.product.code
        if (code.isBlank()) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = api.fetchProduct(code)) {
                is ApiResult.Success -> _state.update { it.copy(product = result.value, isLoading = false) }
                is ApiResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error.message) }
            }
        }
    }
}
