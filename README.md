# DSE Live Tracker

A comprehensive suite for Bangladeshi retail investors to track Dhaka Stock Exchange (DSE) stocks in real-time. Currently available as a Chrome Extension, with a **Mobile App** coming soon!

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

### 2. Mobile App (Coming Soon)
We are currently planning to build a companion mobile application! This will allow users to track their DSE portfolios and watchlists seamlessly on the go. Stay tuned for updates in this repository as development begins.

## Project Structure
- `src/` - Chrome extension source code
- `docs/` - Detailed documentation and use cases
- `assets/` - UI previews and mockups
