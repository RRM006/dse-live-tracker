package com.dselivetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.dselivetracker.data.local.AppDatabase
import com.dselivetracker.data.repository.StockRepository

class DseApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val stockRepository: StockRepository by lazy { StockRepository(database.stockCacheDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "watchlist_alerts",
            "Watchlist Buy Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifies when a stock hits your buy target"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
