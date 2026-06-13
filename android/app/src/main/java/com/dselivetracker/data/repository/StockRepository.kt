package com.dselivetracker.data.repository

import com.dselivetracker.data.local.dao.StockCacheDao
import com.dselivetracker.data.local.entity.StockCacheEntity
import com.dselivetracker.data.remote.DseApiClient
import com.dselivetracker.data.remote.QuotesParser
import com.dselivetracker.data.remote.QuotesParser.StockQuoteFull
import com.dselivetracker.data.remote.QuotesParser.Top20Entry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class StockRepository(private val cacheDao: StockCacheDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _allStocks = MutableStateFlow<Map<String, StockQuoteFull>>(emptyMap())
    val allStocks: StateFlow<Map<String, StockQuoteFull>> = _allStocks
    private val _marketStatus = MutableStateFlow<String?>(null)
    val marketStatus: StateFlow<String?> = _marketStatus
    private val _top20 = MutableStateFlow<List<Top20Entry>>(emptyList())
    val top20: StateFlow<List<Top20Entry>> = _top20
    private var _hasNotified = mutableMapOf<String, Boolean>()
    val hasNotified: Map<String, Boolean> get() = _hasNotified

    init {
        scope.launch {
            val cached = cacheDao.getAll()
            val map = cached.associate { entity ->
                entity.symbol to StockQuoteFull(
                    symbol = entity.symbol, ltp = entity.ltp,
                    high = entity.high, low = entity.low,
                    closep = entity.closep, ycp = entity.ycp,
                    change = entity.change, pctChange = entity.pctChange,
                    upperLimit = entity.upperLimit, lowerLimit = entity.lowerLimit,
                    category = entity.category
                )
            }
            _allStocks.value = map
        }
    }

    suspend fun fetchAndUpdateAll() = coroutineScope {
        val byLtpHtml = async { try { DseApiClient.fetchByLtpHtml() } catch (_: Exception) { null } }
        val defScroll = async { try { DseApiClient.fetchFullQuotesHtml() } catch (_: Exception) { null } }
        val defHomepage = async { try { DseApiClient.fetchHomepage() } catch (_: Exception) { null } }
        val defCbul = async { try { DseApiClient.fetchCbul() } catch (_: Exception) { null } }
        val defTop20 = async { try { DseApiClient.fetchTop20() } catch (_: Exception) { null } }
        val defA = async { try { DseApiClient.fetchCategoryPage("A") } catch (_: Exception) { null } }
        val defB = async { try { DseApiClient.fetchCategoryPage("B") } catch (_: Exception) { null } }
        val defZ = async { try { DseApiClient.fetchCategoryPage("Z") } catch (_: Exception) { null } }
        val defQuotes = async { try { DseApiClient.fetchQuotes() } catch (_: Exception) { null } }

        try {
            withTimeout(30_000L) {
                val results = listOf(byLtpHtml, defScroll, defHomepage, defCbul, defTop20, defA, defB, defZ, defQuotes).awaitAll()
                val htmlByLtp = results[0]
                val htmlScroll = results[1]
                val homepage = results[2]
                val cbulHtml = results[3]
                val top20Html = results[4]
                val catA = results[5]
                val catB = results[6]
                val catZ = results[7]
                val textQuotes = results[8]

                val merged = mutableMapOf<String, StockQuoteFull>()

                if (homepage != null) {
                    val parsed = QuotesParser.parseMarketStatus(homepage)
                    if (parsed != null) _marketStatus.value = parsed
                }

                val fullHtml = htmlByLtp ?: htmlScroll
                if (fullHtml != null) {
                    val fullData = QuotesParser.parseFullHtml(fullHtml)
                    merged.putAll(fullData)
                }

                if (textQuotes != null) {
                    val basic = QuotesParser.parse(textQuotes)
                    for (quote in basic.quotes) {
                        val existing = merged[quote.symbol]
                        if (existing == null || existing.ltp == 0.0) {
                            merged[quote.symbol] = StockQuoteFull(
                                symbol = quote.symbol, ltp = quote.ltp,
                                high = existing?.high ?: 0.0, low = existing?.low ?: 0.0,
                                closep = existing?.closep ?: 0.0, ycp = existing?.ycp ?: 0.0,
                                change = existing?.change ?: 0.0, pctChange = existing?.pctChange ?: 0.0
                            )
                        }
                    }
                }

                val cbMap = if (cbulHtml != null) QuotesParser.parseCbulHtml(cbulHtml) else emptyMap()
                for ((symbol, limits) in cbMap) {
                    val existing = merged[symbol]
                    if (existing != null) {
                        merged[symbol] = existing.copy(upperLimit = limits.first, lowerLimit = limits.second)
                    } else {
                        merged[symbol] = StockQuoteFull(symbol = symbol, ltp = 0.0, high = 0.0, low = 0.0, closep = 0.0, ycp = 0.0, change = 0.0, pctChange = 0.0, upperLimit = limits.first, lowerLimit = limits.second)
                    }
                }

                val categoryMap = mutableMapOf<String, String>()
                val categories = listOf(catA to "A", catB to "B", catZ to "Z")
                for (pair in categories) {
                    val html = pair.first
                    val cat = pair.second
                    if (html != null) {
                        val symbols = QuotesParser.parseFullHtml(html).keys
                        for (sym in symbols) categoryMap[sym] = cat
                    }
                }
                for ((symbol, cat) in categoryMap) {
                    merged[symbol] = merged[symbol]?.copy(category = cat) ?: StockQuoteFull(symbol = symbol, ltp = 0.0, high = 0.0, low = 0.0, closep = 0.0, ycp = 0.0, change = 0.0, pctChange = 0.0, category = cat)
                }

                if (top20Html != null) {
                    _top20.value = QuotesParser.parseTop20Html(top20Html)
                }

                if (merged.isNotEmpty()) {
                    _allStocks.value = merged
                    val entities = merged.map { (symbol, q) ->
                        StockCacheEntity(
                            symbol = symbol, ltp = q.ltp, high = q.high, low = q.low,
                            closep = q.closep, ycp = q.ycp, change = q.change, pctChange = q.pctChange,
                            upperLimit = q.upperLimit, lowerLimit = q.lowerLimit, category = q.category,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    cacheDao.clearAll()
                    cacheDao.insertAll(entities)
                }
            }
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
        }
    }

    fun getBySymbol(symbol: String): StockQuoteFull? = _allStocks.value[symbol]

    fun markNotified(symbol: String) { _hasNotified[symbol] = true }
    fun resetNotifiedFlag(symbol: String) { _hasNotified.remove(symbol) }
}
