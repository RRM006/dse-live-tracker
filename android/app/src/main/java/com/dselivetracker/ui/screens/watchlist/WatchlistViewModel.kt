package com.dselivetracker.ui.screens.watchlist

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.local.entity.WatchlistStock
import com.dselivetracker.data.repository.WatchlistRepository
import com.dselivetracker.utils.StockUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.dselivetracker.data.remote.QuotesParser.StockQuoteFull

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DseApp
    private val db = app.database
    private val watchlistRepo = WatchlistRepository(db.watchlistDao())
    private val stockRepo = app.stockRepository

    val watchlistStocks: StateFlow<List<WatchlistStock>> = watchlistRepo.getAllStocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestToBuy: StateFlow<List<WatchlistStock>> = watchlistStocks.map { list ->
        list.filter { it.lastLtp != null && it.targetPrice != null && it.lastLtp <= it.targetPrice }
            .sortedByDescending { stock ->
                val tp = stock.targetPrice ?: return@sortedByDescending 0.0
                val ltp = stock.lastLtp ?: return@sortedByDescending 0.0
                if (tp > 0) (tp - ltp) / tp else 0.0
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _symbol = MutableStateFlow("")
    val symbol: StateFlow<String> = _symbol

    private val _targetPrice = MutableStateFlow("")
    val targetPrice: StateFlow<String> = _targetPrice

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<String>> = _autocompleteSuggestions

    private val _ycpMap = MutableStateFlow<Map<String, Double>>(emptyMap())
    val ycpMap: StateFlow<Map<String, Double>> = _ycpMap

    val marketStatus: StateFlow<String?> = stockRepo.marketStatus

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _alertBanner = MutableStateFlow<String?>(null)
    val alertBanner: StateFlow<String?> = _alertBanner

    init {
        viewModelScope.launch {
            stockRepo.allStocks.collect { stocks ->
                _ycpMap.value = stocks.mapValues { it.value.ycp }
            }
        }
    }

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

    fun selectSymbol(suggestion: String) {
        val sym = suggestion.split(" - ")[0]
        _symbol.value = sym
        _autocompleteSuggestions.value = emptyList()
    }

    fun hideAutocomplete() {
        _autocompleteSuggestions.value = emptyList()
    }

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
                stockRepo.fetchAndUpdateAll()
                val current = watchlistRepo.getAllStocksOnce()
                for (stock in current) {
                    val info = stockRepo.getBySymbol(stock.symbol)
                    if (info != null) {
                        val direction = when {
                            stock.lastLtp == null -> null
                            info.ltp > stock.lastLtp -> "up"
                            info.ltp < stock.lastLtp -> "down"
                            else -> "flat"
                        }
                        watchlistRepo.updatePrice(stock.symbol, info.ltp, info.high, info.low, direction)
                        checkBuySignal(stock.symbol, info)
                    }
                }
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun checkBuySignal(symbol: String, info: StockQuoteFull) {
        val target = watchlistRepo.getAllStocksOnce().find { it.symbol == symbol }?.targetPrice ?: return
        if (info.ltp <= target) {
            val wasNotified = stockRepo.hasNotified[symbol] == true
            _alertBanner.value = "\uD83D\uDFE6 Buy Signal: $symbol \u2014 LTP \u09F3${info.ltp} reached your target \u09F3$target"
            if (!wasNotified) {
                stockRepo.markNotified(symbol)
                _snackbarMessage.value = _alertBanner.value
                sendNotification(symbol, info.ltp, target)
            }
        } else {
            stockRepo.resetNotifiedFlag(symbol)
            _alertBanner.value = null
        }
    }

    fun clearAlertBanner() {
        _alertBanner.value = null
    }

    private fun sendNotification(symbol: String, ltp: Double, target: Double) {
        try {
            val context = getApplication<DseApp>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }

            val notification = NotificationCompat.Builder(context, "watchlist_alerts")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("\uD83D\uDFE6 Buy Signal: $symbol")
                .setContentText("LTP \u09F3$ltp has reached your target \u09F3$target")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(symbol.hashCode(), notification)
        } catch (_: Exception) {}
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
