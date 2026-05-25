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
Native Android app built with Kotlin + Jetpack Compose. Same features as the extension, optimized for mobile.

**Key Features:**
- **Portfolio Summary** — Total invested, current value, live P&L with percentage.
- **Holdings** — Scrollable sorted list with live P&L per stock. Tap to edit. Remove with 3-second Undo.
- **Watchlist** — Monitor symbols with optional target prices (green indicator when hit).
- **Search** — Autocomplete symbol lookup with live LTP, P&L, and percentage.
- **Extras:** Auto-refresh every 30s, market status indicator, dark mode (follows system), offline cached prices.

> For detailed mobile documentation, see [`docs/mobile-readme.md`](docs/mobile-readme.md).

## Project Structure
- `src/` - Chrome extension source code
- `android/` - Android app source code
- `docs/` - Detailed documentation and use cases
- `assets/` - UI previews and mockups
