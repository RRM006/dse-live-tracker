package com.dselivetracker.data.repository

import com.dselivetracker.data.local.dao.PortfolioDao
import com.dselivetracker.data.local.dao.TradeHistoryDao
import com.dselivetracker.data.local.entity.PortfolioStock
import com.dselivetracker.data.local.entity.SoldStock
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(private val dao: PortfolioDao, private val tradeDao: TradeHistoryDao) {

    companion object {
        const val BROKER_RATE = 0.004
        const val DSE_RATE = 0.00025
        const val AIT_RATE = 0.0005
        const val BUY_COMMISSION_RATE = BROKER_RATE + DSE_RATE // 0.00425
        const val SELL_COMMISSION_RATE = BROKER_RATE + DSE_RATE + AIT_RATE // 0.00475
    }

    fun getAllStocks(): Flow<List<PortfolioStock>> = dao.getAllStocks()

    suspend fun getAllStocksOnce(): List<PortfolioStock> = dao.getAllStocksOnce()

    suspend fun addStock(symbol: String, buyPrice: Double, quantity: Int, buyDate: Long? = null): Long {
        val buyValue = buyPrice * quantity
        val commission = buyValue * BUY_COMMISSION_RATE
        return dao.insert(PortfolioStock(symbol = symbol.uppercase(), buyPrice = buyPrice, quantity = quantity, commission = commission, buyDate = buyDate))
    }

    suspend fun removeStock(id: Long) = dao.deleteById(id)

    suspend fun getBySymbol(symbol: String) = dao.getBySymbol(symbol)

    suspend fun updatePrice(symbol: String, ltp: Double, direction: String?) {
        dao.updatePrice(symbol, ltp, direction, System.currentTimeMillis())
    }

    suspend fun updateDetails(id: Long, buyPrice: Double, quantity: Int) {
        dao.updateDetails(id, buyPrice, quantity)
    }

    suspend fun sellStock(id: Long, sellPrice: Double, sellDate: Long? = null) {
        val stock = dao.getAllStocksOnce().find { it.id == id } ?: return
        val sellValue = sellPrice * stock.quantity
        val buyCommission = stock.commission
        val sellCommission = sellValue * SELL_COMMISSION_RATE
        val realizedPnl = (sellPrice - stock.buyPrice) * stock.quantity - buyCommission - sellCommission
        tradeDao.insert(SoldStock(symbol = stock.symbol, buyPrice = stock.buyPrice, sellPrice = sellPrice, quantity = stock.quantity, buyCommission = buyCommission, sellCommission = sellCommission, realizedPnl = realizedPnl, sellDate = sellDate))
        dao.deleteById(id)
    }

    fun getRealizedPnl(): Flow<Double> = tradeDao.getRealizedPnl()

    suspend fun getRealizedPnlOnce(): Double = tradeDao.getRealizedPnlOnce()

    fun getAllTrades(): Flow<List<SoldStock>> = tradeDao.getAllTrades()

    suspend fun getTotalTradeCommissionOnce(): Double = tradeDao.getTotalTradeCommissionOnce()

    suspend fun removeTrade(id: Long) = tradeDao.deleteById(id)
}
