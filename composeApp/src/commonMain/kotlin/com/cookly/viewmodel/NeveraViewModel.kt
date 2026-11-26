package com.cookly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookly.data.model.Dates
import com.cookly.data.model.ExpiryStatus
import com.cookly.data.model.Product
import com.cookly.data.model.status
import com.cookly.data.prefs.UserPrefs
import com.cookly.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StatusFilter(val label: String) { ALL("Todos"), SOON("Por vencer"), EXPIRED("Vencidos") }

data class NeveraUiState(
    val all: List<Product> = emptyList(),
    val visible: List<Product> = emptyList(),
    val search: String = "",
    val filter: StatusFilter = StatusFilter.ALL,
    val today: Int = Dates.today(),
    val warningDays: Int = 3,
    val soonCount: Int = 0,
    val expiredCount: Int = 0,
    val loaded: Boolean = false,
)

/**
 * ViewModel de la Nevera. Junta cuatro fuentes en un solo estado:
 * los productos de la base (Flow de Room), el texto de búsqueda, el filtro y los días de aviso.
 * Si cualquiera cambia, el estado se recalcula y la pantalla se redibuja sola.
 */
class NeveraViewModel(
    private val products: ProductRepository,
    prefs: UserPrefs,
    ownerId: Long,
) : ViewModel() {

    private val search = MutableStateFlow("")
    private val filter = MutableStateFlow(StatusFilter.ALL)

    val state: StateFlow<NeveraUiState> = combine(
        products.observe(ownerId), search, filter, prefs.warningDays,
    ) { list, query, selected, warning ->
        val today = Dates.today()
        val visible = list.filter { product ->
            val matchesText = query.isBlank() || product.name.contains(query.trim(), ignoreCase = true)
            val status = product.status(today, warning)
            val matchesFilter = when (selected) {
                StatusFilter.ALL -> true
                StatusFilter.SOON -> status == ExpiryStatus.SOON
                StatusFilter.EXPIRED -> status == ExpiryStatus.EXPIRED
            }
            matchesText && matchesFilter
        }
        NeveraUiState(
            all = list,
            visible = visible,
            search = query,
            filter = selected,
            today = today,
            warningDays = warning,
            soonCount = list.count { it.status(today, warning) == ExpiryStatus.SOON },
            expiredCount = list.count { it.status(today, warning) == ExpiryStatus.EXPIRED },
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NeveraUiState())

    fun onSearchChange(value: String) { search.value = value }

    fun onFilterChange(value: StatusFilter) { filter.value = value }

    fun delete(product: Product) {
        viewModelScope.launch { products.delete(product) }
    }
}
