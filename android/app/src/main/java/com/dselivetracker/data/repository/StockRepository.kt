package com.dselivetracker.data.repository

import com.dselivetracker.data.local.dao.StockCacheDao
import com.dselivetracker.data.local.entity.StockCacheEntity
import com.dselivetracker.data.remote.DseApiClient
import com.dselivetracker.data.remote.QuotesParser
import com.dselivetracker.data.remote.QuotesParser.StockQuoteFull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockRepository(private val cacheDao: StockCacheDao) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _allStocks = MutableStateFlow<Map<String, StockQuoteFull>>(emptyMap())
    val allStocks: StateFlow<Map<String, StockQuoteFull>> = _allStocks

    private var _hasNotified = mutableMapOf<String, Boolean>()
    val hasNotified: Map<String, Boolean> get() = _hasNotified

    init {
        scope.launch {
            val cached = cacheDao.getAll()
            val map = cached.associate { entity ->
                entity.symbol to StockQuoteFull(
                    symbol = entity.symbol,
                    ltp = entity.ltp,
                    high = entity.high,
                    low = entity.low,
                    closep = entity.closep,
                    ycp = entity.ycp,
                    change = entity.change,
                    pctChange = entity.pctChange
                )
            }
            _allStocks.value = map
        }
    }

    suspend fun fetchAndUpdateAll() {
        val (text1, html2) = DseApiClient.fetchBothSources()
        val merged = mutableMapOf<String, StockQuoteFull>()

        if (html2 != null) {
            val fullData = QuotesParser.parseFullHtml(html2)
            merged.putAll(fullData)
        }

        if (text1 != null) {
            val basic = QuotesParser.parse(text1)
            for (quote in basic.quotes) {
                val existing = merged[quote.symbol]
                if (existing == null || existing.ltp == 0.0) {
                    merged[quote.symbol] = StockQuoteFull(
                        symbol = quote.symbol,
                        ltp = quote.ltp,
                        high = existing?.high ?: 0.0,
                        low = existing?.low ?: 0.0,
                        closep = existing?.closep ?: 0.0,
                        ycp = existing?.ycp ?: 0.0,
                        change = existing?.change ?: 0.0,
                        pctChange = existing?.pctChange ?: 0.0
                    )
                }
            }
        }

        _allStocks.value = merged

        if (merged.isNotEmpty()) {
            val entities = merged.map { (symbol, q) ->
                StockCacheEntity(
                    symbol = symbol,
                    ltp = q.ltp,
                    high = q.high,
                    low = q.low,
                    closep = q.closep,
                    ycp = q.ycp,
                    change = q.change,
                    pctChange = q.pctChange,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            cacheDao.clearAll()
            cacheDao.insertAll(entities)
        }
    }

    fun getBySymbol(symbol: String): StockQuoteFull? = _allStocks.value[symbol]

    fun markNotified(symbol: String) {
        _hasNotified[symbol] = true
    }

    fun resetNotifiedFlag(symbol: String) {
        _hasNotified.remove(symbol)
    }
}
