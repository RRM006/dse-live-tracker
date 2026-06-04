package com.dselivetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dselivetracker.data.local.entity.SoldStock
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeHistoryDao {
    @Query("SELECT * FROM trade_history ORDER BY soldAt DESC")
    fun getAllTrades(): Flow<List<SoldStock>>

    @Query("SELECT * FROM trade_history ORDER BY soldAt DESC")
    suspend fun getAllTradesOnce(): List<SoldStock>

    @Insert
    suspend fun insert(trade: SoldStock)

    @Query("SELECT COALESCE(SUM(realizedPnl), 0) FROM trade_history")
    fun getRealizedPnl(): Flow<Double>

    @Query("SELECT COALESCE(SUM(realizedPnl), 0) FROM trade_history")
    suspend fun getRealizedPnlOnce(): Double
}
