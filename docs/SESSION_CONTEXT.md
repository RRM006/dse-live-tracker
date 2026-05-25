# Session Context — DSE Live Price Checker

## Project
**DSE Live Price Checker** — Chrome Extension (Manifest V3) for Bangladeshi retail investors.

## Files
```
F:\stock market\stock-checker-extension\
├── manifest.json        # MV3, permissions, commands (Ctrl+Shift+S)
├── background.js        # Service worker: context menu, badge updater, notifications
├── popup.html           # 4-tab popup: Portfolio, Holdings, Watchlist, Search + snackbar
├── popup.css            # 523 lines — light/dark themes, layout, cards, snackbar
├── popup.js             # 1077 lines — all logic: CRUD, sort, undo, auto-refresh, badge, search
├── README.md            # Project docs
├── USECASE.md           # Use case descriptions
├── preview.html         # UI mockup (3 phone states: empty, profit, loss)
└── SESSION_CONTEXT.md   # This file
```

## Architecture

### Layout
- 320px popup, 4 tabs (12px font-size for tab buttons)
- Header: title + dark mode toggle + refresh button
- Footer: status text, dsebd.org link, market status (open/closed dot)
- Undo snackbar: absolute positioned at bottom of `#app`

### Data Source
- Primary: `https://www.dsebd.org/datafile/quotes.txt`
- Fallback: `https://corsproxy.io/?` + encodeURIComponent(primary)
- Pipe/whitespace-delimited. Skip first 4 lines, then `line.split(/\s+/)`.
- Fields: symbol (col 0), LTP (col 1), rest ignored.

### Storage Keys (localStorage)
| Key | Purpose |
|-----|---------|
| `dse_watchlist` | Portfolio — `[{symbol, buyPrice, qty, _ltp, _prevLtp, _direction, _timestamp}]` |
| `dse_quickwatch` | Watchlist — `[{symbol, targetPrice, _ltp, _prevLtp, _direction}]` |
| `dse_dark_mode` | `"1"` if dark, `""` if light |
| `dse_sort` | Sort mode: `pnl-asc`, `pnl-desc`, `pct-asc`, `pct-desc`, `name-asc`, `name-desc` |

### Tab Structure
| Tab | ID | Contents |
|-----|----|----------|
| Portfolio | `#viewPortfolio` | Summary card (invested/current/total P&L) + spacer + Add Stock form |
| Holdings | `#viewHoldings` | Sort dropdown + scrollable card list + "Add New Stock" button |
| Watchlist | `#viewWatchlist` | Scrollable card list + Track Stock form |
| Search | `#viewSearch` | Symbol input (autocomplete) + buy price + qty + Check Price + result card |

## Key Implementation Details

### P&L Formula
```
profit = (LTP - buyPrice) * qty
percent = (profit / buyPrice) * 100
```
Green → profit, Red → loss.

### Badge (`updateBadge()` in popup.js:971)
- Shows total portfolio P&L (NOT single stock LTP)
- Max 4 chars: `+615`, `-2k`, `+15k`
- Called after every portfolio mutation + silent refresh
- Edge case: ≥ 100,000 BDT P&L may truncate incorrectly (`.slice(0,4)` can lose the `k`)

### Sort Comparators
- `(a - b) || 0` pattern for NaN-safe numeric sort
- `(a.symbol || '').localeCompare(b.symbol || '')` for name sort

### Undo Snackbar
- `promptRemove()`: immediately removes stock, shows "GP removed [Undo]" with 3s timeout
- `cancelRemove()`: re-adds stock from `pendingRemove`
- Closing popup finalizes removal (timeout fires)
- Debounced: ignores clicks while `undoTimeout` is active

### Auto-Refresh (30s)
- `AUTO_REFRESH_MS = 30000`
- Refreshes whichever tab is active
- Portfolio/Holdings → `refreshWatchlistPrices()`
- Watchlist → `refreshQuickWatchPrices()`
- Search → `fetchAndRender(lastSymbol, lastBuyPrice, lastQty, true)` (silent)

### Quantity Fields
- No `value="1"` in HTML
- Start empty, retain user input, clear only after successful Portfolio add
- Search fields never auto-cleared

## States / Gotchas Fixed

### ✅ Overflow scroll
- `.view { overflow-y: auto }` added (was missing for Portfolio/Search)
- `.portfolio-spacer { flex: 1; min-height: 0 }` pushes form to bottom of Portfolio

### ✅ Label accessibility (8 fixes)
- `for="addSymbol"`, `for="addBuyPrice"`, `for="addQty"` on Portfolio labels
- `for="qwSymbol"`, `for="qwTarget"` on Watchlist labels
- `aria-label="Stock symbol"`, `aria-label="Buy price"`, `aria-label="Quantity"` on Search inputs

### ✅ Badge shows total portfolio P&L
- Rewrote `updateBadge()` to iterate `watchlist` array and sum `(LTP - buyPrice) * qty`
- Removed parameters from `updateBadge()` and both call sites

### ✅ Target badge hidden until LTP data arrives
- Changed condition from `if (item.targetPrice)` to `if (item.targetPrice && ltpValue !== undefined)`

### ✅ Safe-area padding
- All `-add-section` containers use `env(safe-area-inset-bottom, 0px)`

### ✅ Tab badges (Holdings (N), Watchlist (N))
- `updateBadges()` renders parenthesized count on tab buttons, hidden when empty

## Testing Checklist (manual — Chrome required)

1. **Load extension**: chrome://extensions → Load unpacked → select folder
2. **Portfolio**: Add stocks, verify summary card P&L math
3. **Holdings**: Sort by all 6 modes, click card → Search pre-fills, ✕ remove → Undo
4. **Watchlist**: Add symbol with target, wait for refresh → verify badge accent on target hit
5. **Search**: Type partial symbol → autocomplete works. Big numbers format with commas
6. **Dark mode**: Toggle persists across opens
7. **Badge**: Close popup, see icon badge update with total P&L
8. **Overflow**: Resize popup to min height (500px), verify Portfolio/Search scroll
9. **Right-click**: Select stock symbol on any page → right-click → "Check DSE price"

## What's Left

1. Minor badge edge case: P&L ≥ 100,000 BDT may truncate `k` suffix (fix the two identical `>=10000` / `>=1000` branches in `updateBadge()`)
2. Chrome Web Store publishing (optional) — package extension, create listing
