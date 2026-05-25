package com.dselivetracker.data.repository

import com.dselivetracker.data.local.dao.PortfolioDao
import com.dselivetracker.data.local.entity.PortfolioStock
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(private val dao: PortfolioDao) {

    fun getAllStocks(): Flow<List<PortfolioStock>> = dao.getAllStocks()

    suspend fun getAllStocksOnce(): List<PortfolioStock> = dao.getAllStocksOnce()

    suspend fun addStock(symbol: String, buyPrice: Double, quantity: Int): Long {
        return dao.insert(
            PortfolioStock(
                symbol = symbol.uppercase(),
                buyPrice = buyPrice,
                quantity = quantity
            )
        )
    }

    suspend fun removeStock(id: Long) = dao.deleteById(id)

    suspend fun getBySymbol(symbol: String) = dao.getBySymbol(symbol)

    suspend fun updatePrice(symbol: String, ltp: Double, direction: String?) {
        dao.updatePrice(symbol, ltp, direction, System.currentTimeMillis())
    }

    suspend fun updateDetails(id: Long, buyPrice: Double, quantity: Int) {
        dao.updateDetails(id, buyPrice, quantity)
    }
}
