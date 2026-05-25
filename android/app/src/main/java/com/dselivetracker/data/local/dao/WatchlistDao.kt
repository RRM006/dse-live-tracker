package com.dselivetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dselivetracker.data.local.entity.WatchlistStock
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_stocks ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<WatchlistStock>>

    @Query("SELECT * FROM watchlist_stocks")
    suspend fun getAllStocksOnce(): List<WatchlistStock>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: WatchlistStock): Long

    @Update
    suspend fun update(stock: WatchlistStock)

    @Delete
    suspend fun delete(stock: WatchlistStock)

    @Query("DELETE FROM watchlist_stocks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM watchlist_stocks WHERE symbol = :symbol LIMIT 1")
    suspend fun getBySymbol(symbol: String): WatchlistStock?

    @Query("UPDATE watchlist_stocks SET lastLtp = :ltp, prevLtp = lastLtp, direction = :direction, lastUpdated = :timestamp WHERE symbol = :symbol")
    suspend fun updatePrice(symbol: String, ltp: Double, direction: String?, timestamp: Long)
}
