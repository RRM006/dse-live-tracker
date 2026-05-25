# Use Cases — DSE Live Tracker

## UC1: Add stock to portfolio
1. Open extension → **Portfolio** tab
2. Enter symbol (e.g. `GP`), buy price, quantity
3. Click "+ Add to Portfolio"
4. Summary updates with new stock count and P&L

## UC2: View live portfolio summary
1. Open **Portfolio** tab
2. Auto-refresh runs every 30s, or click refresh icon
3. Card shows: Invested total, Current value (with count), Total P/L (BDT + %)

## UC3: Browse and sort holdings
1. Open **Holdings** tab
2. Sort by: P&L worst/best, % worst/best, Name A-Z/Z-A
3. Each card shows: symbol, P&L, direction arrow (↑↓→), LTP, %, buy price × qty

## UC4: Edit holding via search
1. In **Holdings**, click any card → jumps to **Search**
2. Buy price and quantity pre-filled
3. Click "Check Price" → shows live LTP and P&L
4. Click "Update in Portfolio" to save new buy/qty

## UC5: Remove holding with undo
1. In **Holdings**, click ✕ on a card
2. Stock is removed immediately, snackbar shows "GP removed [Undo]"
3. Click **Undo** to restore; wait 3s or close popup to finalize

## UC6: Monitor symbols with target prices
1. Open **Watchlist** tab
2. Enter symbol + optional target price → "+ Add to Watchlist"
3. Cards show LTP, arrow, % change, target badge
4. When LTP ≥ target: green left border accent + green LTP text

## UC7: Search any DSE stock
1. Open **Search** tab
2. Type symbol → autocomplete dropdown
3. Enter buy price and quantity → "Check Price"
4. Card shows: LTP, buy price, Total P/L, % change

## UC8: Search → Add to Portfolio
1. After checking a price in **Search**
2. Click "+ Add to Portfolio" → stock is saved
3. Button changes to "Added to Portfolio" (disabled)

## UC9: Dark mode
1. Click the moon/sun icon in header
2. Dark theme persists across popup opens

## UC10: Extension badge
1. When popup is closed, extension icon badge shows total portfolio P&L
2. Green = profit, red = loss
3. Max 4 chars: `+615`, `-2k`, `+15k`

## UC11: Right-click lookup
1. Select any text on any page → right-click → "Check DSE price"
2. Opens popup to **Search** tab with symbol pre-filled

## UC12: Keyboard shortcut
1. Press `Ctrl+Shift+S` (Windows) or `Cmd+Shift+S` (Mac)
2. Opens the extension popup

## UC13: Auto-refresh
1. While popup is open, prices refresh every 30s
2. Refreshes whichever tab is currently active
3. Status bar shows "Last updated: Portfolio updated at HH:MM:SS"

## UC14: Market status
1. Footer shows green dot + "Open" (10:00-14:30, Sun-Thu)
2. Otherwise shows red dot + "Closed"
