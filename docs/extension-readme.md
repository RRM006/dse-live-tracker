# DSE Live Tracker

A Chrome extension for Bangladeshi retail investors to track Dhaka Stock Exchange (DSE) stocks in real-time. Monitor live prices, build a portfolio with P&L tracking, set watchlist targets, and search any DSE symbol — all from a 320px popup.

## Features

- **Portfolio** — Summary card showing total invested, current value, and overall P&L with percentage. Add stocks with buy price and quantity.
- **Holdings** — Full scrollable list of your portfolio stocks with live P&L per card. Sort by P&L, % change, or name (ascending/descending). Click a card to jump to Search. Remove with 3-second undo.
- **Watchlist** — Monitor any DSE symbol with an optional target price. Cards show live LTP, direction arrow, and change %. When target is reached, the card gets a green border accent.
- **Search** — Look up any DSE symbol with buy price and quantity. Get live LTP, total P&L, and % change. Add results directly to your portfolio.

### Additional features

- Auto-refresh every 30 seconds
- Dark mode toggle
- Market status indicator (Open/Closed based on DSE hours)
- Keyboard shortcut: `Ctrl+Shift+S` to open popup
- Right-click context menu: select any stock symbol on any page → "Check DSE price"
- Extension badge shows total portfolio P&L (green = profit, red = loss)
- DSE data sourced from `dsebd.org/datafile/quotes.txt` with CORS proxy fallback

## UI Preview

Open `preview.html` in any browser to see interactive mockups of the empty state, profit state, and loss state.

## Installation

1. Clone this repository
2. Open `chrome://extensions/` in Chrome
3. Enable **Developer mode** (top right)
4. Click **Load unpacked**
5. Select the `stock-checker-extension` folder

## Usage

| Tab | What to do |
|-----|-----------|
| Portfolio | Fill in Symbol, Buy Price, Quantity → click "+ Add to Portfolio" |
| Holdings | Browse your stocks, use Sort dropdown, click a card to update in Search, ✕ to remove (with Undo) |
| Watchlist | Enter a Symbol, optionally set a Target Price → click "+ Add to Watchlist" |
| Search | Type a symbol → autocomplete suggestions appear → fill Buy Price + Quantity → click "Check Price" |

## Project Structure

```
stock-checker-extension/
├── manifest.json     # Extension manifest (MV3)
├── popup.html        # Popup UI structure (4 tabs)
├── popup.css         # All styles (light/dark themes)
├── popup.js          # All logic (~1050 lines)
├── background.js     # Service worker (context menu, badge, notifications)
└── preview.html      # UI mockup preview (open in browser)
```

## Tech Stack

- Chrome Extension Manifest V3
- Vanilla HTML / CSS / JavaScript
- Data: `dsebd.org/datafile/quotes.txt`
- Font: Inter (Google Fonts)
