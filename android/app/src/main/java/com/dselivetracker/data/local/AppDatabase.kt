package com.dselivetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dselivetracker.data.local.dao.PortfolioDao
import com.dselivetracker.data.local.dao.StockCacheDao
import com.dselivetracker.data.local.dao.WatchlistDao
import com.dselivetracker.data.local.entity.PortfolioStock
import com.dselivetracker.data.local.entity.StockCacheEntity
import com.dselivetracker.data.local.entity.WatchlistStock

@Database(
    entities = [PortfolioStock::class, WatchlistStock::class, StockCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun stockCacheDao(): StockCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS stock_cache (" +
                            "symbol TEXT PRIMARY KEY NOT NULL, " +
                            "ltp REAL NOT NULL DEFAULT 0, " +
                            "high REAL NOT NULL DEFAULT 0, " +
                            "low REAL NOT NULL DEFAULT 0, " +
                            "closep REAL NOT NULL DEFAULT 0, " +
                            "ycp REAL NOT NULL DEFAULT 0, " +
                            "change REAL NOT NULL DEFAULT 0, " +
                            "pctChange REAL NOT NULL DEFAULT 0, " +
                            "lastUpdated INTEGER NOT NULL DEFAULT 0" +
                            ")"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dse_tracker_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
