package com.dselivetracker.data.repository

import com.dselivetracker.data.local.dao.PortfolioDao
import com.dselivetracker.data.local.dao.TradeHistoryDao
import com.dselivetracker.data.local.entity.PortfolioStock
import com.dselivetracker.data.local.entity.SoldStock
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(private val dao: PortfolioDao, private val tradeDao: TradeHistoryDao) {

    fun getAllStocks(): Flow<List<PortfolioStock>> = dao.getAllStocks()

    suspend fun getAllStocksOnce(): List<PortfolioStock> = dao.getAllStocksOnce()

    suspend fun addStock(symbol: String, buyPrice: Double, quantity: Int): Long {
        val commission = buyPrice * quantity * 0.0004
        return dao.insert(
            PortfolioStock(
                symbol = symbol.uppercase(),
                buyPrice = buyPrice,
                quantity = quantity,
                commission = commission
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

    suspend fun sellStock(id: Long, sellPrice: Double) {
        val stock = dao.getAllStocksOnce().find { it.id == id } ?: return
        val buyCommission = stock.commission
        val sellCommission = sellPrice * stock.quantity * 0.0004
        val realizedPnl = (sellPrice - stock.buyPrice) * stock.quantity - buyCommission - sellCommission
        tradeDao.insert(
            SoldStock(
                symbol = stock.symbol,
                buyPrice = stock.buyPrice,
                sellPrice = sellPrice,
                quantity = stock.quantity,
                buyCommission = buyCommission,
                sellCommission = sellCommission,
                realizedPnl = realizedPnl
            )
        )
        dao.deleteById(id)
    }

    fun getRealizedPnl(): Flow<Double> = tradeDao.getRealizedPnl()

    suspend fun getRealizedPnlOnce(): Double = tradeDao.getRealizedPnlOnce()

    fun getAllTrades(): Flow<List<SoldStock>> = tradeDao.getAllTrades()

    suspend fun getTotalTradeCommissionOnce(): Double = tradeDao.getTotalTradeCommissionOnce()
}
