package com.dselivetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dselivetracker.data.local.dao.PortfolioDao
import com.dselivetracker.data.local.dao.StockCacheDao
import com.dselivetracker.data.local.dao.TradeHistoryDao
import com.dselivetracker.data.local.dao.WatchlistDao
import com.dselivetracker.data.local.entity.PortfolioStock
import com.dselivetracker.data.local.entity.SoldStock
import com.dselivetracker.data.local.entity.StockCacheEntity
import com.dselivetracker.data.local.entity.WatchlistStock

@Database(
    entities = [PortfolioStock::class, WatchlistStock::class, StockCacheEntity::class, SoldStock::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun stockCacheDao(): StockCacheDao
    abstract fun tradeHistoryDao(): TradeHistoryDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE portfolio_stocks ADD COLUMN commission REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE watchlist_stocks ADD COLUMN high REAL")
                database.execSQL("ALTER TABLE watchlist_stocks ADD COLUMN low REAL")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS trade_history (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "symbol TEXT NOT NULL, " +
                            "buyPrice REAL NOT NULL, " +
                            "sellPrice REAL NOT NULL, " +
                            "quantity INTEGER NOT NULL, " +
                            "buyCommission REAL NOT NULL, " +
                            "sellCommission REAL NOT NULL, " +
                            "realizedPnl REAL NOT NULL, " +
                            "soldAt INTEGER NOT NULL" +
                            ")"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE stock_cache ADD COLUMN upperLimit REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE stock_cache ADD COLUMN lowerLimit REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE stock_cache ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE portfolio_stocks ADD COLUMN buyDate INTEGER")
                database.execSQL("ALTER TABLE portfolio_stocks ADD COLUMN sellDate INTEGER")
                database.execSQL("ALTER TABLE trade_history ADD COLUMN sellDate INTEGER")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE portfolio_stocks SET commission = ROUND(buyPrice * quantity * 0.00425, 2)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dse_tracker_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
