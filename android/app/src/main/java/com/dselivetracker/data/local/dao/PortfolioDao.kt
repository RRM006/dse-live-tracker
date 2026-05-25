package com.dselivetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dselivetracker.data.local.entity.PortfolioStock
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_stocks ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<PortfolioStock>>

    @Query("SELECT * FROM portfolio_stocks")
    suspend fun getAllStocksOnce(): List<PortfolioStock>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: PortfolioStock): Long

    @Update
    suspend fun update(stock: PortfolioStock)

    @Delete
    suspend fun delete(stock: PortfolioStock)

    @Query("DELETE FROM portfolio_stocks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM portfolio_stocks WHERE symbol = :symbol LIMIT 1")
    suspend fun getBySymbol(symbol: String): PortfolioStock?

    @Query("UPDATE portfolio_stocks SET lastLtp = :ltp, prevLtp = lastLtp, direction = :direction, lastUpdated = :timestamp WHERE symbol = :symbol")
    suspend fun updatePrice(symbol: String, ltp: Double, direction: String?, timestamp: Long)

    @Query("UPDATE portfolio_stocks SET buyPrice = :buyPrice, quantity = :quantity WHERE id = :id")
    suspend fun updateDetails(id: Long, buyPrice: Double, quantity: Int)
}
