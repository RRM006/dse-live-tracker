package com.dselivetracker.ui.screens.holdings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.local.entity.PortfolioStock
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
    private val db = (application as DseApp).database
    private val portfolioRepo = PortfolioRepository(db.portfolioDao())

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
            portfolioRepo.addStock(stock.symbol, stock.buyPrice, stock.quantity)
            _pendingRemove.value = null
        }
    }

    fun clearPendingRemove() {
        _pendingRemove.value = null
    }
}
