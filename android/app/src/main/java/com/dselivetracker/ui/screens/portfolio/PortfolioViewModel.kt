package com.dselivetracker.ui.screens.portfolio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.remote.DseApiClient
import com.dselivetracker.data.remote.QuotesParser
import com.dselivetracker.data.repository.PortfolioRepository
import com.dselivetracker.ui.components.PortfolioSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PortfolioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as DseApp).database
    private val portfolioRepo = PortfolioRepository(db.portfolioDao())

    private val stocks = portfolioRepo.getAllStocks()

    val summary: StateFlow<PortfolioSummary?> = stocks.combine(MutableStateFlow(Unit)) { list, _ ->
        if (list.isEmpty()) null
        else {
            val invested = list.sumOf { it.buyPrice * it.quantity }
            val current = list.filter { it.lastLtp != null }.sumOf { it.lastLtp!! * it.quantity }
            val pnl = current - invested
            val pct = if (invested > 0) (pnl / invested) * 100 else 0.0
            PortfolioSummary(
                invested = invested,
                currentValue = current,
                pnl = pnl,
                pnlPercent = pct,
                stockCount = list.size,
                countWithData = list.count { it.lastLtp != null }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _symbol = MutableStateFlow("")
    val symbol: StateFlow<String> = _symbol

    private val _buyPrice = MutableStateFlow("")
    val buyPrice: StateFlow<String> = _buyPrice

    private val _quantity = MutableStateFlow("")
    val quantity: StateFlow<String> = _quantity

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun updateSymbol(value: String) { _symbol.value = value.uppercase() }
    fun updateBuyPrice(value: String) { _buyPrice.value = value }
    fun updateQuantity(value: String) { _quantity.value = value }
    fun clearError() { _error.value = null }

    fun addStock() {
        val sym = _symbol.value.trim()
        val bp = _buyPrice.value.toDoubleOrNull()
        val qty = _quantity.value.toIntOrNull() ?: 1
        if (sym.isEmpty() || bp == null || bp <= 0) return
        viewModelScope.launch {
            val existing = portfolioRepo.getBySymbol(sym)
            if (existing != null) {
                _error.value = "$sym already in portfolio"
                return@launch
            }
            portfolioRepo.addStock(sym, bp, qty)
            _symbol.value = ""
            _buyPrice.value = ""
            _quantity.value = ""
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val text = DseApiClient.fetchQuotes()
                val parsed = QuotesParser.parse(text)
                val currentStocks = portfolioRepo.getAllStocksOnce()
                for (stock in currentStocks) {
                    val quote = parsed.quotes.find { it.symbol == stock.symbol }
                    if (quote != null) {
                        val direction = when {
                            stock.lastLtp == null -> null
                            quote.ltp > stock.lastLtp -> "up"
                            quote.ltp < stock.lastLtp -> "down"
                            else -> "flat"
                        }
                        portfolioRepo.updatePrice(stock.symbol, quote.ltp, direction)
                    }
                }
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _lastUpdated.value = "Portfolio updated at $time"
            } catch (e: Exception) {
                _lastUpdated.value = "Update failed"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
