package com.dselivetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_stocks")
data class WatchlistStock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val targetPrice: Double? = null,
    val lastLtp: Double? = null,
    val prevLtp: Double? = null,
    val direction: String? = null,
    val lastUpdated: Long? = null
)
