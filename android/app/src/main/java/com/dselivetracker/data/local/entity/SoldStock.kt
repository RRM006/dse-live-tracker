package com.dselivetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_history")
data class SoldStock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val quantity: Int,
    val buyCommission: Double,
    val sellCommission: Double,
    val realizedPnl: Double,
    val soldAt: Long = System.currentTimeMillis()
)
