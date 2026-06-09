package com.dselivetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_stocks")
data class PortfolioStock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val buyPrice: Double,
    val quantity: Int,
    val commission: Double = 0.0,
    val lastLtp: Double? = null,
    val prevLtp: Double? = null,
    val direction: String? = null,
    val buyDate: Long? = null,
    val sellDate: Long? = null,
    val lastUpdated: Long? = null
)
