package com.dselivetracker.ui.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchResult(
    val symbol: String,
    val ltp: Double,
    val buyPrice: Double,
    val quantity: Int,
    val totalPnl: Double,
    val percent: Double,
    val ycp: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val closep: Double = 0.0,
    val timestamp: String? = null
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DseApp
    private val db = app.database
    private val portfolioRepo = PortfolioRepository(db.portfolioDao())
    private val stockRepo = app.stockRepository

    private val _symbol = MutableStateFlow("")
    val symbol: StateFlow<String> = _symbol

    private val _buyPrice = MutableStateFlow("")
    val buyPrice: StateFlow<String> = _buyPrice

    private val _quantity = MutableStateFlow("1")
    val quantity: StateFlow<String> = _quantity

    private val _result = MutableStateFlow<SearchResult?>(null)
    val result: StateFlow<SearchResult?> = _result

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<String>> = _autocompleteSuggestions

    private val _inPortfolio = MutableStateFlow(false)
    val inPortfolio: StateFlow<Boolean> = _inPortfolio

    private val _isEditingSymbol = MutableStateFlow<String?>(null)
    val isEditingSymbol: StateFlow<String?> = _isEditingSymbol

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated

    fun updateSymbol(value: String) {
        _symbol.value = value.uppercase()
        val query = value.uppercase()
        if (query.length >= 1) {
            val allSymbols = stockRepo.allStocks.value.keys.toList()
            val startsWith = allSymbols.filter { it.startsWith(query) }
            val contains = allSymbols.filter { it.contains(query) && !it.startsWith(query) }
            _autocompleteSuggestions.value = (startsWith + contains).take(8)
        } else {
            _autocompleteSuggestions.value = emptyList()
        }
    }

    fun updateBuyPrice(value: String) { _buyPrice.value = value }
    fun updateQuantity(value: String) { _quantity.value = value }

    fun hideAutocomplete() {
        _autocompleteSuggestions.value = emptyList()
    }

    fun selectSymbol(symbol: String) {
        _symbol.value = symbol
        hideAutocomplete()
    }

    fun setInitialValues(symbol: String, buyPrice: String, quantity: String) {
        if (symbol.isNotBlank() && _symbol.value.isEmpty()) {
            _symbol.value = symbol.uppercase()
            _buyPrice.value = buyPrice
            _quantity.value = quantity.ifBlank { "1" }
            _isEditingSymbol.value = symbol.uppercase()
        }
    }

    fun checkPrice() {
        val sym = _symbol.value.trim()
        val bp = _buyPrice.value.toDoubleOrNull()
        val qty = _quantity.value.toIntOrNull() ?: 1
        if (sym.isEmpty() || bp == null || bp <= 0) {
            _error.value = "Please enter a symbol and valid buy price"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _result.value = null
            try {
                stockRepo.fetchAndUpdateAll()
                _autocompleteSuggestions.value = emptyList()

                val info = stockRepo.getBySymbol(sym)
                if (info == null || info.ltp == 0.0) {
                    _error.value = "Stock \"$sym\" not found in DSE data"
                    _isLoading.value = false
                    return@launch
                }

                val profitPerShare = info.ltp - bp
                val totalProfit = profitPerShare * qty
                val percent = if (bp > 0) (profitPerShare / bp) * 100 else 0.0

                _result.value = SearchResult(
                    symbol = sym,
                    ltp = info.ltp,
                    buyPrice = bp,
                    quantity = qty,
                    totalPnl = totalProfit,
                    percent = percent,
                    ycp = info.ycp,
                    high = info.high,
                    low = info.low,
                    closep = info.closep
                )

                val existing = portfolioRepo.getBySymbol(sym)
                _inPortfolio.value = existing != null
                if (existing != null && _isEditingSymbol.value == sym) {
                    _isEditingSymbol.value = sym
                }
            } catch (e: Exception) {
                _error.value = "Network error. Check your connection."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToPortfolio() {
        val result = _result.value ?: return
        viewModelScope.launch {
            if (_isEditingSymbol.value != null) {
                val existing = portfolioRepo.getBySymbol(result.symbol)
                if (existing != null) {
                    portfolioRepo.updateDetails(existing.id, result.buyPrice, result.quantity)
                    _isEditingSymbol.value = null
                }
            } else {
                portfolioRepo.addStock(result.symbol, result.buyPrice, result.quantity)
                _inPortfolio.value = true
            }
        }
    }

    fun clearResult() {
        _result.value = null
        _error.value = null
        _isEditingSymbol.value = null
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                stockRepo.fetchAndUpdateAll()
                val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                _lastUpdated.value = "Data refreshed at $time"
            } catch (e: Exception) {
                _lastUpdated.value = "Refresh failed"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
