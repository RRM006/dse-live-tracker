# DSE Live Tracker

A comprehensive suite for Bangladeshi retail investors to track Dhaka Stock Exchange (DSE) stocks in real-time.

## Components

### 1. Chrome Extension (`/src`)
Monitor live prices, build a portfolio with P&L tracking, set watchlist targets, and search any DSE symbol — all from a convenient popup.

**Key Features:**
- **Portfolio & Holdings:** Track total invested, current value, and live P&L. Sort, manage, and view detailed cards for each stock.
- **Watchlist:** Monitor symbols with optional target prices (alerts when targets are met).
- **Live Search:** Look up symbols instantly.
- **Extras:** Dark mode, auto-refresh (30s), market status indicator, right-click context menus, and extension badge for portfolio health.

**Installation (Unpacked):**
1. Clone this repository.
2. Go to `chrome://extensions/` in Chrome and enable **Developer mode**.
3. Click **Load unpacked** and select the `src/` directory.

> For detailed extension documentation, see [`docs/extension-readme.md`](docs/extension-readme.md).

### 2. Android App (`/android`)
Native Android app built with Kotlin + Jetpack Compose + Material 3. Same features as the extension, optimized for mobile.

**Key Features:**
- **Portfolio Summary** — Total invested, current value, live P&L with percentage, realized P&L from sold stocks, commission breakdown (Broker 0.40% + DSE 0.025% + AIT 0.05%), and Capital Gains Tax (15% on profits > 50 lakh).
- **Holdings** — Scrollable sorted list with live P&L per card. Shows LTP, HIGH, LOW, CLOSEP, YCP, raw CHANGE, and % change. Tap to edit. Sell with date picker and commission breakdown. Remove with 3-second Undo.
- **Watchlist** — Monitor symbols with optional target prices (green BUY SIGNAL indicator when met). Top "Best to Buy" section. Notification alerts on target hit.
- **Search** — Autocomplete symbol lookup with live LTP, P&L, and percentage. Edit or add directly to portfolio.
- **Trade History** — View all sold stocks with buy/sell prices, realized P&L, and commissions.
- **Extras:** Auto-refresh every 30s, manual refresh, market status indicator, dark mode (follows system), offline cached prices with timestamp.

> For detailed mobile documentation, see [`docs/mobile-readme.md`](docs/mobile-readme.md).

## Data Sources

Stock data is fetched directly from the Dhaka Stock Exchange website. Multiple fallback strategies are used for reliability:

1. **Primary:** `https://www.dsebd.org/latest_share_price_scroll_l.php` (full HTML with LTP, high, low, closep, ycp, change, pctChange)
2. **Fallback:** `http://www.dsebd.org/latest_share_price_scroll_l.php` (plain HTTP when HTTPS SSL fails)
3. **Proxy:** `https://corsproxy.io/?https://www.dsebd.org/latest_share_price_scroll_l.php`

Additional sources: `quotes.txt`, `cbul.php` (circuit breakers), `top_20_share.php`, category pages (A/B/Z groups).

No backend server or API key is required. All portfolio/watchlist data is stored locally on your device.

## Project Structure
- `src/` - Chrome extension source code
- `android/` - Android app source code
- `docs/` - Detailed documentation, use cases, and development context/prompts
- `assets/` - UI previews and mockups
