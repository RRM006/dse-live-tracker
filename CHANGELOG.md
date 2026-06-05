# Changelog — DSE Live Tracker

## Feature: Commission Fix + Pie Chart + Watchlist Best-to-Buy + Price Alert Banner + Trade History

### Commission Fix
- `ui/screens/portfolio/PortfolioViewModel.kt`
  - **Invested** now includes buy commission: `sumOf(buyPrice × quantity + commission)`
  - **Total commission** includes both buy commissions from held stocks AND buy+sell commissions from sold stocks (via `getAllTrades()` flow combined into summary)
  - `unrealizedPnl` no longer double-subtracts commission (commission is already baked into invested)

### Pie Chart (Portfolio)
- **New:** `ui/components/PieChart.kt` — Canvas-based donut chart with arc segments, percentage labels inside arcs, center hole, color legend below
- `ui/components/SummaryCard.kt` — Added `pieSlices: List<PieSlice>` to `PortfolioSummary`
- `ui/screens/portfolio/PortfolioViewModel.kt` — Computes `pieSlices` from current‑value breakdown per stock
- `ui/screens/portfolio/PortfolioScreen.kt` — Shows `PieChart` below `SummaryCard` when ≥2 holdings have data

### Watchlist: Best to Buy + Target Price Display
- `ui/screens/watchlist/WatchlistViewModel.kt`
  - Added `bestToBuy: StateFlow<List<WatchlistStock>>` — filters where LTP ≤ target price, sorted by discount % descending `(target-LTP)/target`
- `ui/screens/watchlist/WatchlistScreen.kt` — "⭐ Best to Buy" section at top of LazyColumn before the rest of the list; separator "— All Watchlist Items —"
- `ui/components/StockCard.kt` — Added `targetPrice: Double?` parameter; shows "Target: ৳X" row

### Price Alert Banner (Persistent)
- `ui/screens/watchlist/WatchlistViewModel.kt` — Added `alertBanner: StateFlow<String?>`; set by `checkBuySignal()`, cleared by `clearAlertBanner()`
- `ui/screens/watchlist/WatchlistScreen.kt` — Green `AnimatedVisibility` banner at top, clickable to dismiss

### Trade History Screen
- **New:** `ui/screens/tradehistory/TradeHistoryViewModel.kt` — Exposes `trades: Flow<List<SoldStock>>`
- **New:** `ui/screens/tradehistory/TradeHistoryScreen.kt` — Full screen with:
  - Back navigation, LazyColumn of `TradeCard` items (symbol, date, buy/sell prices, qty, commissions, P&L)
  - Total realized P&L footer card
- `ui/navigation/AppNavigation.kt` — Added `Screen.TradeHistory` route and composable block
- `ui/screens/holdings/HoldingsScreen.kt` — Added `onNavigateToTradeHistory` parameter, "History" button in the sort bar

---

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
