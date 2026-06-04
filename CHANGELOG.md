# Changelog — DSE Live Tracker

## Feature: Sell Transactions + Realized P&L

### New Files
- `data/local/entity/SoldStock.kt` — Room entity (`trade_history` table) with fields: id, symbol, buyPrice, sellPrice, quantity, buyCommission, sellCommission, realizedPnl, soldAt
- `data/local/dao/TradeHistoryDao.kt` — DAO with getAllTrades() (Flow), getAllTradesOnce() (suspend), getRealizedPnl() (Flow), getRealizedPnlOnce() (suspend), insert(trade)

### Modified Files
- `data/local/AppDatabase.kt`
  - Added `SoldStock::class` to entities array
  - Added `tradeHistoryDao(): TradeHistoryDao` abstract method
  - Added `MIGRATION_2_3` — creates `trade_history` table
- `data/repository/PortfolioRepository.kt`
  - Constructor now takes `TradeHistoryDao` as second parameter
  - `addStock()` now calculates commission: `buyPrice * quantity * 0.0004` and stores it in `PortfolioStock.commission`
  - Added `sellStock(id, sellPrice)` — computes buyCommission + sellCommission + realizedPnl, inserts SoldStock into trade_history, deletes portfolio entry
  - Added `getRealizedPnl()` (Flow), `getRealizedPnlOnce()`, `getAllTrades()`
- `ui/screens/portfolio/PortfolioViewModel.kt`
  - Now creates `PortfolioRepository(db.portfolioDao(), db.tradeHistoryDao())`
  - Summary now combines stocks Flow with realizedPnl Flow
  - Total P&L = unrealizedPnl + realizedPnl (both after commission)
  - Extra fields in PortfolioSummary: realizedPnl, unrealizedPnl, totalCommission
- `ui/screens/portfolio/PortfolioScreen.kt` — No code changes needed (SummaryCard handles new fields)
- `ui/screens/holdings/HoldingsViewModel.kt`
  - Now creates `PortfolioRepository(db.portfolioDao(), db.tradeHistoryDao())`
  - Added `sellStockId` (StateFlow<Long?>) — which stock dialog is open for
  - Added `sellPrice` (StateFlow<String>) — sell price input text
  - Added `showSellDialog(id)`, `hideSellDialog()`, `updateSellPrice(value)`, `confirmSell()`
- `ui/screens/holdings/HoldingsScreen.kt`
  - Added AlertDialog for sell confirmation with price input, estimated value/commission/P&L
  - Sell button per StockCard (red "Sell" button via `showSell`/`onSell` props)

---

## Feature: Commission Calculation

### Modified Files
- `data/local/entity/PortfolioStock.kt` — Added field `commission: Double = 0.0`
- `data/repository/PortfolioRepository.kt` — See above (addStock + sellStock calculate commission)
- `data/local/AppDatabase.kt` — `MIGRATION_2_3` adds `commission REAL NOT NULL DEFAULT 0` to portfolio_stocks
- `ui/components/SummaryCard.kt` — Shows "Commission" row with total
- `ui/screens/portfolio/PortfolioViewModel.kt` — Unrealized P&L calculation subtracts total commission

### Formula
- Buy commission = `buyPrice × quantity × 0.0004`
- Sell commission = `sellPrice × quantity × 0.0004`
- Realized P&L = `(sellPrice - buyPrice) × quantity - buyCommission - sellCommission`
- Unrealized P&L = `sumOf((LTP - buyPrice) × quantity) - totalBuyCommission`

---

## Feature: Watchlist Improvements

### Modified Files
- `data/local/entity/WatchlistStock.kt` — Added fields `high: Double? = null`, `low: Double? = null`
- `data/local/dao/WatchlistDao.kt`
  - `updatePrice()` now takes `high: Double?` and `low: Double?` params
  - SQL: `high = COALESCE(:high, high), low = COALESCE(:low, low)`
- `data/repository/WatchlistRepository.kt`
  - `updatePrice()` now passes `high` and `low` to DAO
- `ui/screens/watchlist/WatchlistViewModel.kt`
  - `refresh()` now passes `info.high` and `info.low` to `watchlistRepo.updatePrice()`
- `ui/screens/watchlist/WatchlistScreen.kt`
  - StockCard call now passes `high = stock.high` and `low = stock.low`
- `ui/screens/holdings/HoldingsViewModel.kt`
  - Added `stockQuotes` (StateFlow<Map<String, StockQuoteFull>>) populated from stockRepo.allStocks
- `ui/screens/holdings/HoldingsScreen.kt`
  - StockCard call now passes `high = quote?.high` and `low = quote?.low` from stockQuotes
- `ui/components/StockCard.kt`
  - Added parameters: `high: Double? = null`, `low: Double? = null`
  - New H/L row showing "H: ৳X  L: ৳Y"
  - **Full green background** (BuySignalGreen at 15% alpha) when `targetHit = true` (replaces old left blue border)
  - LTP text turns ProfitGreen instead of SkyBlue on targetHit
- `data/local/AppDatabase.kt` — `MIGRATION_2_3` adds `high REAL` and `low REAL` columns to watchlist_stocks

---

## Feature: News Tab (5th Bottom Nav)

### New Files
- `ui/screens/news/NewsViewModel.kt`
  - Loads holdings + watchlist symbols from DAO on init
  - `filteredNews` — combines `stockRepo.allNews` with filterSymbols set
  - Only shows news for symbols in user's holdings/watchlist (or all news if empty)
- `ui/screens/news/NewsScreen.kt`
  - TopAppBar with "News" title
  - LazyColumn of news cards showing tradingCode (bold primary), title (semi-bold), body (small, muted)
  - Empty state: "No news for your stocks"

### Modified Files
- `data/repository/StockRepository.kt`
  - Added `allNews` StateFlow (List<NewsParser.NewsItem>)
  - In `fetchAndUpdateAll()`: after parsing market status from homepage, also calls `NewsParser.parseNews(homepage)` and publishes via `_allNews`
- `ui/navigation/AppNavigation.kt`
  - Added `Screen.News` sealed class with route "news", label "News", Article icons
  - Added `NewsScreen` to `bottomNavScreens` list (5th tab)
  - Added `composable(Screen.News.route) { NewsScreen() }` to NavHost

---

## Database Migration: v2 → v3

### Migration SQL (MIGRATION_2_3)
```sql
ALTER TABLE portfolio_stocks ADD COLUMN commission REAL NOT NULL DEFAULT 0;
ALTER TABLE watchlist_stocks ADD COLUMN high REAL;
ALTER TABLE watchlist_stocks ADD COLUMN low REAL;
CREATE TABLE IF NOT EXISTS trade_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    symbol TEXT NOT NULL,
    buyPrice REAL NOT NULL,
    sellPrice REAL NOT NULL,
    quantity INTEGER NOT NULL,
    buyCommission REAL NOT NULL,
    sellCommission REAL NOT NULL,
    realizedPnl REAL NOT NULL,
    soldAt INTEGER NOT NULL
);
```
