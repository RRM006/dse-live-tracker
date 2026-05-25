package com.dselivetracker.ui.screens.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.dselivetracker.data.local.entity.WatchlistStock
import com.dselivetracker.data.remote.DseApiClient
import com.dselivetracker.data.remote.QuotesParser

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as DseApp).database
    private val watchlistRepo = WatchlistRepository(db.watchlistDao())

    val watchlistStocks: StateFlow<List<WatchlistStock>> = watchlistRepo.getAllStocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _symbol = MutableStateFlow("")
    val symbol: StateFlow<String> = _symbol

    private val _targetPrice = MutableStateFlow("")
    val targetPrice: StateFlow<String> = _targetPrice

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun updateSymbol(value: String) { _symbol.value = value.uppercase() }
    fun updateTargetPrice(value: String) { _targetPrice.value = value }
    fun clearError() { _error.value = null }

    fun addStock() {
        val sym = _symbol.value.trim()
        if (sym.isEmpty()) return
        val tp = _targetPrice.value.toDoubleOrNull()
        viewModelScope.launch {
            val existing = watchlistRepo.getBySymbol(sym)
            if (existing != null) {
                _error.value = "$sym already in watchlist"
                return@launch
            }
            watchlistRepo.addStock(sym, tp)
            _symbol.value = ""
            _targetPrice.value = ""
        }
    }

    fun removeStock(id: Long) {
        viewModelScope.launch { watchlistRepo.removeStock(id) }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val text = DseApiClient.fetchQuotes()
                val parsed = QuotesParser.parse(text)
                val current = watchlistRepo.getAllStocksOnce()
                for (stock in current) {
                    val quote = parsed.quotes.find { it.symbol == stock.symbol }
                    if (quote != null) {
                        val direction = when {
                            stock.lastLtp == null -> null
                            quote.ltp > stock.lastLtp -> "up"
                            quote.ltp < stock.lastLtp -> "down"
                            else -> "flat"
                        }
                        watchlistRepo.updatePrice(stock.symbol, quote.ltp, direction)
                    }
                }
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
