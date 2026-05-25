package com.dselivetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_cache")
data class StockCacheEntity(
    @PrimaryKey val symbol: String,
    val ltp: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val closep: Double = 0.0,
    val ycp: Double = 0.0,
    val change: Double = 0.0,
    val pctChange: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
