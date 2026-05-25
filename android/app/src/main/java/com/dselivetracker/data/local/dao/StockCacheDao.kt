package com.dselivetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dselivetracker.data.local.entity.StockCacheEntity

@Dao
interface StockCacheDao {
    @Query("SELECT * FROM stock_cache")
    suspend fun getAll(): List<StockCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<StockCacheEntity>)

    @Query("DELETE FROM stock_cache")
    suspend fun clearAll()

    @Query("SELECT * FROM stock_cache WHERE symbol = :symbol LIMIT 1")
    suspend fun getBySymbol(symbol: String): StockCacheEntity?
}
