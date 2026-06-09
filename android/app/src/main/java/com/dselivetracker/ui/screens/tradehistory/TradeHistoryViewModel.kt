package com.dselivetracker.ui.screens.tradehistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.local.entity.SoldStock
import com.dselivetracker.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TradeHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DseApp
    private val db = app.database
    private val portfolioRepo = PortfolioRepository(db.portfolioDao(), db.tradeHistoryDao())

    val trades: StateFlow<List<SoldStock>> = portfolioRepo.getAllTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteTrade(id: Long) {
        viewModelScope.launch {
            portfolioRepo.removeTrade(id)
        }
    }
}
