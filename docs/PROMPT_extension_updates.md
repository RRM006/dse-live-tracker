# DSE Live Tracker — Chrome Extension Update Prompt

## Context
You are updating an existing Chrome Extension (Manifest V3) called **DSE Live Tracker**.
The extension already exists and works. You are NOT building from scratch — you are modifying
the existing files: `popup.html`, `popup.css`, `popup.js`, `background.js`.

The current file structure is:
```
stock-checker-extension/
├── manifest.json
├── popup.html
├── popup.css
├── popup.js          (~1050 lines, all tab logic)
├── background.js     (service worker: context menu, badge, notifications)
└── preview.html
```

The current app has 4 tabs: Portfolio, Holdings, Watchlist, Search.
It already fetches live LTP from: `https://www.dsebd.org/datafile/quotes.txt`
With CORS fallback: `https://corsproxy.io/?https://www.dsebd.org/datafile/quotes.txt`

---

## New Data Source (CRITICAL — read carefully)

You must now fetch TWO data sources on every refresh:

### Source 1 (existing — keep it)
**URL:** `https://www.dsebd.org/datafile/quotes.txt`
**Format:** Plain text, tab-separated, one stock per line:
```
SYMBOL    LTP
GP        241
BATBC     211.5
```
**Use for:** Fast symbol list for autocomplete + LTP fallback.

### Source 2 (NEW — add this)
**URL:** `https://www.dsebd.org/latest_share_price_scroll_l.php`
**CORS proxy URL:** `https://corsproxy.io/?https://www.dsebd.org/latest_share_price_scroll_l.php`
**Format:** HTML page containing a `<table>` with columns in this order:
`#  |  TRADING CODE  |  LTP  |  HIGH  |  LOW  |  CLOSEP  |  YCP  |  CHANGE  |  %CHANGE  |  TRADE  |  VALUE  |  VOLUME`

Parse this HTML table using `DOMParser` in JavaScript:
```javascript
async function fetchFullQuotes() {
  const url = 'https://corsproxy.io/?https://www.dsebd.org/latest_share_price_scroll_l.php';
  const res = await fetch(url);
  const html = await res.text();
  const doc = new DOMParser().parseFromString(html, 'text/html');
  const rows = doc.querySelectorAll('table tbody tr');
  const data = {};
  rows.forEach(row => {
    const cells = row.querySelectorAll('td');
    if (cells.length >= 7) {
      const symbol = cells[1].textContent.trim();
      data[symbol] = {
        ltp:    parseFloat(cells[2].textContent.replace(/,/g, '')) || 0,
        high:   parseFloat(cells[3].textContent.replace(/,/g, '')) || 0,
        low:    parseFloat(cells[4].textContent.replace(/,/g, '')) || 0,
        closep: parseFloat(cells[5].textContent.replace(/,/g, '')) || 0,
        ycp:    parseFloat(cells[6].textContent.replace(/,/g, '')) || 0,
        change: parseFloat(cells[7].textContent.replace(/,/g, '')) || 0,
        pctChange: parseFloat(cells[8].textContent.replace(/,/g, '').replace('%','')) || 0,
      };
    }
  });
  return data;
}
```

**Merge strategy:** Fetch both sources in parallel (`Promise.all`). Use Source 2 as primary.
Fall back to Source 1 (LTP only) if Source 2 fails. Store merged result in a global
`window.stockData = {}` keyed by symbol.

---

## Change 1: Autocomplete (Search tab + Watchlist symbol input)

**Where:** `popup.js` — the Search tab symbol input and Watchlist symbol input.

**Current behaviour:** Unknown / basic.

**New behaviour:**
- When user types any letter(s) in the symbol input field, show a dropdown of matching symbols
  filtered from `window.stockData` keys (populated from quotes.txt + full table).
- Match against the START of the symbol first, then also mid-string matches (start matches
  appear first).
- Show max 8 suggestions at a time in a scrollable dropdown `<ul>` positioned absolutely
  below the input.
- Clicking a suggestion fills the input and closes the dropdown.
- Pressing Escape or clicking outside closes the dropdown.
- Dropdown must work in both light and dark mode (use CSS variables).

**Implementation notes:**
- Reuse a single `<div id="autocomplete-dropdown">` in the HTML.
- Attach `input` event listener to both symbol fields.
- The symbol list is always available because quotes.txt is fetched on popup open.

---

## Change 2: Manual Refresh Button

**Where:** `popup.html` header area (already has a refresh icon placeholder).

**New behaviour:**
- A visible refresh button (🔄 icon or "Refresh" label) is always present in the header.
- Clicking it triggers a full data fetch (both Source 1 + Source 2).
- While fetching, the button shows a spinning animation (CSS `@keyframes spin`).
- After fetch completes (success or error), spinner stops.
- The existing auto-refresh (every 30 seconds) must continue to work independently.
- Manual refresh must work on ALL tabs (Portfolio, Holdings, Watchlist, Search) — it always
  refreshes whichever tab is currently visible.
- After refresh, update the "Last updated" status bar with current time.

---

## Change 3: Watchlist — Buy Signal Logic (sky blue)

**Current behaviour:** When LTP ≥ target → green border.

**New behaviour — completely replace the target logic:**

A watchlist item has one target price. The meaning of "target" is: the price at which you
WANT to buy. So the signal fires when LTP is AT or BELOW the target (price has dropped to
your desired entry level).

### Visual states:
| Condition | Visual |
|-----------|--------|
| No target set | Normal card, no accent |
| Target set, LTP > target | Normal card — waiting (show target as grey badge) |
| Target set, LTP ≤ target | **Sky blue** left border (`#00BFFF`) + sky blue LTP text + "✅ BUY SIGNAL" green badge on card |

### Notification (Chrome):
- When LTP first drops to ≤ target (detected on any refresh), trigger a Chrome notification:
  ```javascript
  chrome.notifications.create(`watchlist-${symbol}`, {
    type: 'basic',
    iconUrl: 'icons/icon48.png',
    title: `🟦 Buy Signal: ${symbol}`,
    message: `LTP ৳${ltp} has reached your target ৳${target}. Possible entry point!`,
    priority: 2
  });
  ```
- The notification fires ONCE per trigger event and does NOT repeat on subsequent refreshes
  UNLESS the price first goes above target again and then drops back to ≤ target (re-trigger).
- Track this with a per-symbol flag `notifiedAt[symbol] = ltp` — only notify again if ltp
  previously exceeded target after the last notification.
- The notification permission must be declared in `manifest.json` under `"permissions"`.

### Card layout changes:
- Always show YCP on watchlist cards: `YCP: ৳{ycp}` in small grey text.
- Show direction arrow: ↑ if LTP > YCP, ↓ if LTP < YCP, → if equal.

---

## Change 4: YCP display

**Where:** Search tab result card + Watchlist cards.

### Search tab result card — add these fields:
```
LTP:      ৳{ltp}       (already exists)
YCP:      ৳{ycp}       (NEW — yesterday's closing price)
HIGH:     ৳{high}      (NEW — today's intraday high)
LOW:      ৳{low}       (NEW — today's intraday low)
Total P&L: ৳{pnl}     (already exists)
% Change: {pct}%       (already exists)
```

Layout: display them in a clean 2-column grid inside the result card.

### Watchlist cards — add:
- `YCP: ৳{ycp}` shown in small text below the LTP line.

---

## Change 5: HIGH / LOW in Search tab

Already covered in Change 4 above. Make sure HIGH and LOW are prominently shown,
perhaps with colour coding: HIGH in green text, LOW in red text.

---

## Summary of all files to modify

| File | Changes |
|------|---------|
| `popup.js` | fetchFullQuotes(), autocomplete logic, manual refresh handler, watchlist buy-signal logic, watchlist re-trigger tracking, updated card renderers for Search + Watchlist to show YCP/HIGH/LOW |
| `popup.html` | Autocomplete dropdown `<div>`, refresh button in header |
| `popup.css` | Sky blue watchlist card state, autocomplete dropdown styles (light + dark), refresh spinner animation, new data fields layout in Search result card |
| `background.js` | Watchlist notification trigger (already has notification code — update the condition to LTP ≤ target, add re-trigger logic) |
| `manifest.json` | Ensure `"notifications"` is in permissions array |

---

## Constraints / Rules
- Do NOT change the tab structure (Portfolio, Holdings, Watchlist, Search).
- Do NOT change the existing Portfolio/Holdings logic.
- Keep dark mode working for all new UI elements.
- Keep the existing CORS proxy fallback pattern.
- The autocomplete dropdown must NOT block other UI elements unexpectedly (use `z-index: 1000`).
- All currency amounts must display with `৳` (Bangladeshi Taka symbol).
- Do not use any external JS libraries — vanilla JS only.
- Keep the existing keyboard shortcut (Ctrl+Shift+S) and right-click context menu working.
