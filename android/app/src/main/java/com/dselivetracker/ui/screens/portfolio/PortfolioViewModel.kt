package com.dselivetracker.ui.screens.portfolio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.local.entity.PortfolioStock
import com.dselivetracker.data.repository.PortfolioRepository
import com.dselivetracker.ui.components.PieSlice
import com.dselivetracker.ui.components.PortfolioSummary
import com.dselivetracker.ui.components.pieColors
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
    private val app = application as DseApp
    private val db = app.database
    private val portfolioRepo = PortfolioRepository(db.portfolioDao(), db.tradeHistoryDao())
    private val stockRepo = app.stockRepository

    private val stocks = portfolioRepo.getAllStocks()
    private val realizedPnl = portfolioRepo.getRealizedPnl()
    private val trades = portfolioRepo.getAllTrades()

    val summary: StateFlow<PortfolioSummary?> = combine(stocks, realizedPnl, trades) { list, realized, tradeList ->
        if (list.isEmpty()) null
        else {
            val buyCommission = list.sumOf { it.commission }
            val tradeCommission = tradeList.sumOf { it.buyCommission + it.sellCommission }
            val totalCommission = buyCommission + tradeCommission
            val invested = list.sumOf { it.buyPrice * it.quantity + it.commission }
            val current = list.filter { it.lastLtp != null }.sumOf { it.lastLtp!! * it.quantity }
            val unrealizedPnl = current - invested
            val totalPnl = unrealizedPnl + realized
            val totalInvested = if (invested > 0) invested else 1.0
            val totalPct = (totalPnl / totalInvested) * 100
            val slices = list.filter { it.lastLtp != null }.mapIndexed { index, stock ->
                PieSlice(
                    label = stock.symbol,
                    value = stock.lastLtp!! * stock.quantity,
                    color = pieColors[index % pieColors.size]
                )
            }
            PortfolioSummary(
                invested = invested,
                currentValue = current,
                pnl = totalPnl,
                pnlPercent = totalPct,
                stockCount = list.size,
                countWithData = list.count { it.lastLtp != null },
                realizedPnl = realized,
                unrealizedPnl = unrealizedPnl,
                totalCommission = totalCommission,
                pieSlices = slices
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _symbol = MutableStateFlow("")
    val symbol: StateFlow<String> = _symbol

    private val _buyPrice = MutableStateFlow("")
    val buyPrice: StateFlow<String> = _buyPrice

    private val _quantity = MutableStateFlow("")
    val quantity: StateFlow<String> = _quantity

    val marketStatus: StateFlow<String?> = stockRepo.marketStatus

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
                stockRepo.fetchAndUpdateAll()
                val currentStocks = portfolioRepo.getAllStocksOnce()
                for (stock in currentStocks) {
                    val info = stockRepo.getBySymbol(stock.symbol)
                    if (info != null) {
                        val direction = when {
                            stock.lastLtp == null -> null
                            info.ltp > stock.lastLtp -> "up"
                            info.ltp < stock.lastLtp -> "down"
                            else -> "flat"
                        }
                        portfolioRepo.updatePrice(stock.symbol, info.ltp, direction)
                    }
                }
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _lastUpdated.value = time
            } catch (e: Exception) {
                _lastUpdated.value = "Update failed"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
