package com.dselivetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dselivetracker.data.local.dao.PortfolioDao
import com.dselivetracker.data.local.dao.WatchlistDao
import com.dselivetracker.data.local.entity.PortfolioStock
import com.dselivetracker.data.local.entity.WatchlistStock

@Database(
    entities = [PortfolioStock::class, WatchlistStock::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dse_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
