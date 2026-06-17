package com.dselivetracker.ui.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.utils.StockUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchResult(
    val symbol: String,
    val ltp: Double,
    val ycp: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val closep: Double = 0.0,
    val pctChange: Double = 0.0,
    val upperLimit: Double = 0.0,
    val lowerLimit: Double = 0.0,
    val breakerPct: Double = 0.0,
    val tickSize: Double = 0.0,
    val openAdjPrice: Double = 0.0,
    val category: String = "",
    val timestamp: String? = null
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DseApp
    private val stockRepo = app.stockRepository

    private val _symbol = MutableStateFlow("")
    val symbol: StateFlow<String> = _symbol

    private val _result = MutableStateFlow<SearchResult?>(null)
    val result: StateFlow<SearchResult?> = _result

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<String>> = _autocompleteSuggestions

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated

    fun updateSymbol(value: String) {
        _symbol.value = value.uppercase()
        val query = value.uppercase()
        if (query.length >= 1) {
            val allSymbols = stockRepo.allStocks.value.keys.toList()
            val matches = allSymbols.filter { sym ->
                sym.contains(query) || StockUtils.getStockName(sym).uppercase().contains(query)
            }.sortedBy { sym ->
                val name = StockUtils.getStockName(sym).uppercase()
                when {
                    sym.startsWith(query) -> 0
                    name.startsWith(query) -> 1
                    else -> 2
                }
            }
            _autocompleteSuggestions.value = matches.take(8).map { sym ->
                val name = StockUtils.getStockName(sym)
                if (name != sym) "$sym - $name" else sym
            }
        } else {
            _autocompleteSuggestions.value = emptyList()
        }
    }

    fun hideAutocomplete() {
        _autocompleteSuggestions.value = emptyList()
    }

    fun selectSymbol(suggestion: String) {
        val sym = suggestion.split(" - ")[0]
        _symbol.value = sym
        hideAutocomplete()
    }

    fun checkPrice() {
        val sym = _symbol.value.trim()
        if (sym.isEmpty()) {
            _error.value = "Please enter a stock symbol"
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

                _result.value = SearchResult(
                    symbol = sym,
                    ltp = info.ltp,
                    ycp = info.ycp,
                    high = info.high,
                    low = info.low,
                    closep = info.closep,
                    pctChange = info.pctChange,
                    upperLimit = info.upperLimit,
                    lowerLimit = info.lowerLimit,
                    breakerPct = info.breakerPct,
                    tickSize = info.tickSize,
                    openAdjPrice = info.openAdjPrice,
                    category = info.category
                )
            } catch (e: Exception) {
                _error.value = "Network error. Check your connection."
            } finally {
                _isLoading.value = false
            }
        }
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
