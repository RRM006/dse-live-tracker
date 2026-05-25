package com.dselivetracker.data.repository

import com.dselivetracker.data.local.dao.WatchlistDao
import com.dselivetracker.data.local.entity.WatchlistStock
import kotlinx.coroutines.flow.Flow

class WatchlistRepository(private val dao: WatchlistDao) {

    fun getAllStocks(): Flow<List<WatchlistStock>> = dao.getAllStocks()

    suspend fun getAllStocksOnce(): List<WatchlistStock> = dao.getAllStocksOnce()

    suspend fun addStock(symbol: String, targetPrice: Double?): Long {
        return dao.insert(
            WatchlistStock(
                symbol = symbol.uppercase(),
                targetPrice = targetPrice
            )
        )
    }

    suspend fun removeStock(id: Long) = dao.deleteById(id)

    suspend fun getBySymbol(symbol: String) = dao.getBySymbol(symbol)

    suspend fun updatePrice(symbol: String, ltp: Double, direction: String?) {
        dao.updatePrice(symbol, ltp, direction, System.currentTimeMillis())
    }

    suspend fun updateTargetPrice(id: Long, targetPrice: Double?) {
        val stock = dao.getAllStocksOnce().find { it.id == id }
        if (stock != null) {
            dao.update(stock.copy(targetPrice = targetPrice))
        }
    }
}
