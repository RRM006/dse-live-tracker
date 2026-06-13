# Fix Live Data Fetch Plan — DSE Live Tracker

## Problem

LTP data takes too long to load or fails entirely. Multiple issues in the data fetching pipeline.

---

## Changes

### File 1: `data/remote/QuotesParser.kt`

**What:** Fix `parseFullHtml` to calculate `pctChange` instead of reading wrong column.

The DSE HTML table has 11 columns:
```
# | TRADING CODE | LTP* | HIGH | LOW | CLOSEP* | YCP* | CHANGE | TRADE | VALUE (mn) | VOLUME
```
Current code reads `cells[8]` as `pctChange` but column 8 is `TRADE` count (not percentage). Percentage is NOT in the HTML — must be calculated.

**Change:**
- Increase check from `cells.size >= 9` to `cells.size >= 11`
- Calculate `pctChange = if (ycp > 0) ((ltp - ycp) / ycp) * 100 else 0.0`
- Remove `cells[8]` read entirely

### File 2: `data/remote/DseApiClient.kt`

**Changes:**
1. **Fix category A URL** — line 167: change `val groupParam = if (group == "A") "" else "?group=$group"` to `val groupParam = "?group=$group"`
2. **Add `fetchByLtpHtml()` method** — new function fetching `https://www.dsebd.org/latest_share_price_scroll_by_ltp.php` with HTTP + proxy fallbacks
3. **Reduce retries/timout** — `MAX_RETRIES` from 2 to 1, `TIMEOUT_MS` from 10000 to 8000

### File 3: `data/repository/StockRepository.kt`

**Changes to `fetchAndUpdateAll()`:**
1. Wrap with `withTimeout(30_000)` — caps max refresh time
2. Add `fetchByLtpHtml()` as primary source (parallel with existing)
3. Make `fetchQuotes()` non-blocking — run separately with shorter timeout, do NOT include in `awaitAll` for the main merge
4. Priority order: `scroll_by_ltp.php` → `scroll_l.php` → category pages → CBUL → quotes.txt → top20 → homepage

---

## Execution Order

1. Edit `QuotesParser.kt` — fix pctChange calculation
2. Edit `DseApiClient.kt` — fix category URL, add byLtp method, reduce timeout
3. Edit `StockRepository.kt` — refactor fetchAndUpdateAll with timeout + priority

No new files. 3 files modified. No dependency issues between edits.
