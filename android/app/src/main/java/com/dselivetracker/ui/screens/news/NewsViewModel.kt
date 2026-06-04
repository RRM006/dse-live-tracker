package com.dselivetracker.ui.screens.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.local.dao.PortfolioDao
import com.dselivetracker.data.local.dao.WatchlistDao
import com.dselivetracker.data.remote.NewsParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DseApp
    private val db = app.database
    private val stockRepo = app.stockRepository
    private val portfolioDao: PortfolioDao = db.portfolioDao()
    private val watchlistDao: WatchlistDao = db.watchlistDao()

    private val _filterSymbols = MutableStateFlow<Set<String>>(emptySet())

    val filteredNews: StateFlow<List<NewsParser.NewsItem>> =
        stockRepo.allNews.combine(_filterSymbols) { news, symbols ->
            if (symbols.isEmpty()) news
            else news.filter { it.tradingCode in symbols }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val portfolio = portfolioDao.getAllStocksOnce()
            val watchlist = watchlistDao.getAllStocksOnce()
            val symbols = (portfolio.map { it.symbol } + watchlist.map { it.symbol }).toSet()
            _filterSymbols.value = symbols
        }
    }
}
