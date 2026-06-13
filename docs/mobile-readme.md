# DSE Live Tracker — Android App

Native Android app for Bangladeshi retail investors to track Dhaka Stock Exchange (DSE) stocks in real-time. Built with **Kotlin + Jetpack Compose + Material 3**.

## Features

| Tab | Description |
|-----|-------------|
| **Portfolio** | Summary card showing total invested, current value, overall P&L with percentage, realized P&L, commission breakdown (Broker 0.40% + DSE 0.025% + AIT 0.05%), and Capital Gains Tax (15% on profits exceeding 50 lakh). Pie chart for portfolio allocation. Add stocks with symbol, buy price, quantity, and optional buy date. |
| **Holdings** | Full scrollable list of your portfolio stocks with live P&L per card. Each card shows LTP, HIGH, LOW, CLOSEP, YCP, raw CHANGE value, % change, circuit limits, category (A/B/Z), direction arrow, buy price with quantity breakdown, settlement date, and data timestamp. Sort by P&L, % change, or name. Tap a card to jump to Search with pre-filled data. Sell with date picker and per-fee commission breakdown. Remove with 3-second Undo. |
| **Watchlist** | Monitor any DSE symbol with an optional target price. "Best to Buy" section sorts symbols closest to target. Cards show live LTP, direction arrow, HIGH, LOW, YCP, and target progress. Green BUY SIGNAL accent when target is reached. Notification alerts on target hit. |
| **Search** | Look up any DSE symbol with autocomplete suggestions. Enter buy price and quantity to get live LTP, total P&L, and % change. Add results directly to your portfolio or edit existing holdings. |
| **Trade History** | View all sold stocks with buy price, sell price, quantity, buy commission, sell commission, and realized P&L for each trade. Remove trades from history. |

### Additional Features

- Auto-refresh every 30 seconds (Portfolio tab)
- Manual refresh button on Holdings tab
- Dark mode (follows system theme)
- Market status indicator (Open/Closed based on DSE hours: 10:00-14:30, Sun-Thu)
- Offline support — last fetched prices are cached in Room DB and displayed with a "Data: HH:mm:ss" timestamp when offline
- Smart network retry: HTTPS → HTTP → CORS proxy fallback chain
- Capital Gains Tax auto-calculated at 15% on total realized profit exceeding 500万 BDT

## Commission Structure

| Fee | Rate | Applied On |
|-----|------|------------|
| Broker | 0.40% | Buy + Sell |
| DSE | 0.025% | Buy + Sell |
| AIT | 0.05% | Sell only |
| **Total Buy** | **0.425%** | Buy value |
| **Total Sell** | **0.475%** | Sell value |

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (bottom nav with 5 tabs) |
| Database | SQLite via Room |
| Networking | OkHttp |
| HTML Parsing | Jsoup |
| Architecture | MVVM (ViewModel + StateFlow + Repository) |
| Min SDK | 26 (Android 8.0) |

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/dselivetracker/
│   │   │   ├── DseApp.kt                 # Application class
│   │   │   ├── MainActivity.kt           # Single activity host
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── AppDatabase.kt    # Room database + migrations
│   │   │   │   │   ├── dao/              # Data access objects
│   │   │   │   │   └── entity/           # Room entities
│   │   │   │   ├── remote/               # OkHttp client + HTML/text parser
│   │   │   │   └── repository/           # Data repositories
│   │   │   └── ui/
│   │   │       ├── navigation/           # Bottom nav + nav host
│   │   │       ├── theme/                # Colors, typography, theme
│   │   │       ├── components/           # Reusable composables
│   │   │       └── screens/              # 5 tab screens + ViewModels
│   │   └── res/                          # Resources
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Installation

### Prerequisites
- Android Studio (latest version)
- Android phone or emulator

### Build & Install using Android Studio
1. Open the `android/` directory in Android Studio
2. Wait for Gradle sync to complete
3. Connect your phone via USB (enable Developer options + USB Debugging)
4. Click **Run** (green triangle) to build and install

### Build APK (no USB needed)
1. In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Find the APK at: `android/app/build/outputs/apk/debug/app-debug.apk`
3. Upload the APK to Google Drive (or email/Dropbox)
4. On your phone: download the APK, tap to install
5. If prompted, enable **"Install from unknown apps"** for your file manager/browser

### Install via Wireless Debugging (Android 11+)
1. Phone: **Developer options → Wireless debugging → Enable**
2. Android Studio: **File → Settings → Experimental → Pair using Wi-Fi**
3. Scan the QR code shown on your phone

## Data Source

Stock data is fetched directly from the Dhaka Stock Exchange website with multiple fallback strategies:

1. **HTTPS:** `https://www.dsebd.org/latest_share_price_scroll_l.php`
2. **HTTP:** `http://www.dsebd.org/latest_share_price_scroll_l.php` (fallback when SSL fails)
3. **CORS Proxy:** `https://corsproxy.io/?https://www.dsebd.org/latest_share_price_scroll_l.php`

Additional endpoints: `quotes.txt` (text LTP), `cbul.php` (circuit breaker limits), `top_20_share.php`, category group pages.

No backend server or API key is required. All portfolio/watchlist data is stored locally on your device.

## Storage

- APK size: ~5 MB
- Database: few KB (text-only: symbol names, prices, quantities)
- Runtime memory: ~50-100 MB
- **Total storage: under 10 MB**

## Usage

| Tab | What to do |
|-----|-----------|
| Portfolio | Fill in Symbol, Buy Price, Quantity, optional Buy Date → tap "+ Add to Portfolio". View portfolio summary and pie chart. |
| Holdings | Browse your stocks, use Sort dropdown, tap a card to update in Search, Sell with price/date, or Remove with Undo. |
| Watchlist | Enter a Symbol, optionally set a Target Price → tap "+ Add to Watchlist". View "Best to Buy" and all watched stocks. |
| Search | Type a symbol → autocomplete appears → fill Buy Price + Quantity → tap "Check Price" → Add/Edit in portfolio. |
| Trade History | View all completed sales with P&L details. |

## Architecture Overview

```
User taps refresh → ViewModel calls fetchAndUpdateAll()
                            ↓
                DseApiClient (8 parallel requests)
                    ↓              ↓           ↓
             quotes.txt    full HTML    cbul/top20/category
                    ↓              ↓           ↓
              QuotesParser parses all responses
                            ↓
              StockRepository._allStocks (in-memory cache)
                            ↓
                    Room DB (stock_cache table)
                            ↓
              Per-stock updatePrice() on portfolio/watchlist
                            ↓
              UI recomposes via StateFlow ← Room Flow
```

When offline, Room returns the last cached prices from the previous successful fetch. The in-memory cache is preserved on failed refreshes. Each StockCard displays a "Data: HH:mm:ss" timestamp showing when the price was last updated.
