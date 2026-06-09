package com.dselivetracker.ui.screens.holdings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.local.entity.PortfolioStock
import com.dselivetracker.data.remote.QuotesParser.StockQuoteFull
import com.dselivetracker.data.repository.PortfolioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val label: String) {
    PNL_ASC("P&L (worst first)"),
    PNL_DESC("P&L (best first)"),
    PCT_ASC("% Change (worst first)"),
    PCT_DESC("% Change (best first)"),
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)")
}

class HoldingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DseApp
    private val db = app.database
    private val portfolioRepo = PortfolioRepository(db.portfolioDao(), db.tradeHistoryDao())
    private val stockRepo = app.stockRepository

    private val _sortMode = MutableStateFlow(SortMode.PNL_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode

    val sortedStocks: StateFlow<List<PortfolioStock>> = portfolioRepo.getAllStocks().map { list ->
        when (_sortMode.value) {
            SortMode.PNL_ASC -> list.sortedBy {
                if (it.lastLtp != null) (it.lastLtp - it.buyPrice) * it.quantity else Double.MAX_VALUE
            }
            SortMode.PNL_DESC -> list.sortedByDescending {
                if (it.lastLtp != null) (it.lastLtp - it.buyPrice) * it.quantity else Double.MIN_VALUE
            }
            SortMode.PCT_ASC -> list.sortedBy {
                if (it.lastLtp != null && it.buyPrice > 0) ((it.lastLtp - it.buyPrice) / it.buyPrice) * 100 else Double.MAX_VALUE
            }
            SortMode.PCT_DESC -> list.sortedByDescending {
                if (it.lastLtp != null && it.buyPrice > 0) ((it.lastLtp - it.buyPrice) / it.buyPrice) * 100 else Double.MIN_VALUE
            }
            SortMode.NAME_ASC -> list.sortedBy { it.symbol }
            SortMode.NAME_DESC -> list.sortedByDescending { it.symbol }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pendingRemove = MutableStateFlow<PortfolioStock?>(null)
    val pendingRemove: StateFlow<PortfolioStock?> = _pendingRemove

    private var undoJob: Job? = null

    private val _ycpMap = MutableStateFlow<Map<String, Double>>(emptyMap())
    val ycpMap: StateFlow<Map<String, Double>> = _ycpMap

    private val _stockQuotes = MutableStateFlow<Map<String, StockQuoteFull>>(emptyMap())
    val stockQuotes: StateFlow<Map<String, StockQuoteFull>> = _stockQuotes

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated

    private val _sellStockId = MutableStateFlow<Long?>(null)
    val sellStockId: StateFlow<Long?> = _sellStockId

    private val _sellPrice = MutableStateFlow("")
    val sellPrice: StateFlow<String> = _sellPrice

    private val _sellDate = MutableStateFlow<Long?>(null)
    val sellDate: StateFlow<Long?> = _sellDate

    fun showSellDialog(id: Long) {
        _sellStockId.value = id
        _sellPrice.value = ""
        _sellDate.value = System.currentTimeMillis()
    }

    fun hideSellDialog() {
        _sellStockId.value = null
        _sellPrice.value = ""
        _sellDate.value = null
    }

    fun updateSellPrice(value: String) {
        _sellPrice.value = value
    }

    fun updateSellDate(timestamp: Long) {
        _sellDate.value = timestamp
    }

    fun confirmSell() {
        val id = _sellStockId.value ?: return
        val price = _sellPrice.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            portfolioRepo.sellStock(id, price, _sellDate.value)
            _sellStockId.value = null
            _sellPrice.value = ""
            _sellDate.value = null
        }
    }

    init {
        viewModelScope.launch {
            stockRepo.allStocks.collect { stocks ->
                _ycpMap.value = stocks.mapValues { it.value.ycp }
                _stockQuotes.value = stocks
            }
        }
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun removeStock(id: Long) {
        viewModelScope.launch {
            val stock = portfolioRepo.getAllStocksOnce().find { it.id == id } ?: return@launch
            portfolioRepo.removeStock(id)
            _pendingRemove.value = stock
            undoJob?.cancel()
            undoJob = viewModelScope.launch {
                delay(3000)
                _pendingRemove.value = null
            }
        }
    }

    fun undoRemove() {
        val stock = _pendingRemove.value ?: return
        undoJob?.cancel()
        viewModelScope.launch {
            portfolioRepo.addStock(stock.symbol, stock.buyPrice, stock.quantity, stock.buyDate)
            _pendingRemove.value = null
        }
    }

    fun clearPendingRemove() {
        _pendingRemove.value = null
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
                val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                _lastUpdated.value = time
            } catch (e: Exception) {
                _lastUpdated.value = "Update failed"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
