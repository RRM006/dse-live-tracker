# DSE Live Tracker — Android App Update Prompt

## Context
You are updating an existing **Android app** called **DSE Live Tracker**.
The app is already built and works. You are NOT building from scratch — you are modifying
existing Kotlin + Jetpack Compose + Material 3 files.

Current tech stack:
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Navigation: Navigation Compose (bottom nav, 4 tabs)
- Database: Room (SQLite)
- Networking: OkHttp
- Architecture: MVVM (ViewModel + StateFlow + Repository)
- Min SDK: 26 (Android 8.0)

Current project structure:
```
android/app/src/main/java/com/dselivetracker/
├── DseApp.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   ├── remote/
│   │   ├── DseApiClient.kt       ← fetch quotes.txt
│   │   └── QuotesParser.kt       ← parse quotes.txt
│   └── repository/
└── ui/
    ├── navigation/
    ├── theme/
    ├── components/
    └── screens/                  ← 4 tab screens + ViewModels
```

---

## New Data Source (CRITICAL — read carefully)

You must now fetch TWO data sources on every refresh.

### Source 1 (existing — keep it)
**URL:** `https://www.dsebd.org/datafile/quotes.txt`
**Format:** Plain text, tab-separated:
```
SYMBOL    LTP
GP        241
BATBC     211.5
```
**Use for:** Symbol list for autocomplete + LTP.

### Source 2 (NEW — add this)
**URL:** `https://www.dsebd.org/latest_share_price_scroll_l.php`
**Format:** HTML page with a `<table>`. Columns in order:
`# | TRADING CODE | LTP | HIGH | LOW | CLOSEP | YCP | CHANGE | %CHANGE | TRADE | VALUE | VOLUME`

**How to parse in Android (using Jsoup):**

Add Jsoup to `app/build.gradle.kts`:
```kotlin
implementation("org.jsoup:jsoup:1.17.2")
```

Parse the table:
```kotlin
import org.jsoup.Jsoup

data class StockQuote(
    val symbol: String,
    val ltp: Double,
    val high: Double,
    val low: Double,
    val closep: Double,
    val ycp: Double,
    val change: Double,
    val pctChange: Double
)

fun parseFullQuotesHtml(html: String): Map<String, StockQuote> {
    val doc = Jsoup.parse(html)
    val rows = doc.select("table tbody tr")
    val result = mutableMapOf<String, StockQuote>()
    for (row in rows) {
        val cells = row.select("td")
        if (cells.size >= 7) {
            val symbol = cells[1].text().trim()
            if (symbol.isNotEmpty()) {
                result[symbol] = StockQuote(
                    symbol   = symbol,
                    ltp      = cells[2].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                    high     = cells[3].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                    low      = cells[4].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                    closep   = cells[5].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                    ycp      = cells[6].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                    change   = cells[7].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                    pctChange= cells[8].text().replace(",", "").replace("%","").toDoubleOrNull() ?: 0.0
                )
            }
        }
    }
    return result
}
```

**In `DseApiClient.kt`:** Fetch both URLs in parallel (use Kotlin coroutines with
`async { }` + `awaitAll()`). Merge results — Source 2 is primary, Source 1 is fallback
for LTP if Source 2 fails.

Store merged result in the repository and expose via StateFlow to all ViewModels.

---

## Change 1: Autocomplete (Search tab + Watchlist symbol input)

**Where:** `SearchScreen.kt` and `WatchlistScreen.kt` composables.

**New behaviour:**
- When user types in the symbol `TextField`, show a `DropdownMenu` or `LazyColumn` popup
  below the field with matching symbols from the loaded stock data map.
- Match: show symbols that START WITH the typed text first, then symbols that CONTAIN the
  typed text (case-insensitive). Max 8 results shown.
- Tapping a suggestion fills the TextField and closes the dropdown.
- Pressing back or tapping outside dismisses it.
- The symbol list is always available because it is fetched on app start and stored in
  the ViewModel's StateFlow.

**Implementation approach:**
```kotlin
// In ViewModel, expose:
val symbolSuggestions: StateFlow<List<String>>  // filtered by current query

// In Composable:
ExposedDropdownMenuBox(
    expanded = suggestions.isNotEmpty() && query.isNotEmpty(),
    onExpandedChange = {}
) {
    OutlinedTextField(
        value = query,
        onValueChange = { viewModel.onSymbolQueryChanged(it) },
        modifier = Modifier.menuAnchor(),
        label = { Text("Symbol") }
    )
    ExposedDropdownMenu(
        expanded = suggestions.isNotEmpty() && query.isNotEmpty(),
        onDismissRequest = { viewModel.clearSuggestions() }
    ) {
        suggestions.forEach { symbol ->
            DropdownMenuItem(
                text = { Text(symbol) },
                onClick = {
                    viewModel.selectSymbol(symbol)
                }
            )
        }
    }
}
```

---

## Change 2: Manual Refresh Button

**Where:** All 4 tab screens — add a refresh button in each screen's top bar or as a
floating action button (FAB). The top bar `@Composable` already exists — add an
`IconButton` with `Icons.Default.Refresh`.

**New behaviour:**
- Tapping the refresh button triggers a data fetch (both Source 1 + Source 2).
- Show a `CircularProgressIndicator` (small, in the top bar area) while fetching.
- The existing auto-refresh (every 30 seconds via a coroutine `delay` loop in ViewModel)
  must continue to work independently.
- After fetch: update `lastUpdated: StateFlow<String>` with formatted time.
- Show a `Snackbar` with "Data refreshed at HH:MM:SS" on success.
- Show a `Snackbar` with "Refresh failed — showing cached data" on error.

**In ViewModel:**
```kotlin
fun manualRefresh() {
    viewModelScope.launch {
        _isRefreshing.value = true
        try {
            repository.fetchAndUpdateAll()
            _lastUpdated.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        } catch (e: Exception) {
            _error.value = "Refresh failed"
        } finally {
            _isRefreshing.value = false
        }
    }
}
```

---

## Change 3: Watchlist — Buy Signal Logic

**Current behaviour:** When LTP ≥ target → green border. REMOVE THIS LOGIC.

**New behaviour:**

A watchlist target price means: the price at which the user WANTS TO BUY.
The signal fires when LTP ≤ target (price dropped to or below desired entry level).

### Visual states for watchlist cards:

| State | Visual |
|-------|--------|
| No target | Normal `Card`, no accent |
| Target set, LTP > target | Normal card — waiting. Show target as grey `Text` badge |
| Target set, LTP ≤ target | Sky blue left border (`Color(0xFF00BFFF)`) + sky blue LTP text + green "✅ BUY SIGNAL" chip |

**Implement sky blue left border using Compose modifier:**
```kotlin
Modifier
  .fillMaxWidth()
  .then(
    if (isBuySignal) Modifier.drawBehind {
        drawRect(
            color = Color(0xFF00BFFF),
            size = androidx.compose.ui.geometry.Size(6f, size.height)
        )
    } else Modifier
  )
```

### Android Notification (both in-app and push):

**In-app Snackbar:** When buy signal detected during any refresh, show:
```
"🟦 Buy Signal: {SYMBOL} — LTP ৳{ltp} reached your target ৳{target}"
```

**Push notification** (even when app is minimized):

1. Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

2. Create a `NotificationChannel` in `DseApp.kt`:
```kotlin
val channel = NotificationChannel(
    "watchlist_alerts",
    "Watchlist Buy Alerts",
    NotificationManager.IMPORTANCE_HIGH
).apply { description = "Notifies when a stock hits your buy target" }
getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
```

3. Trigger notification in ViewModel/Repository when signal fires:
```kotlin
val notification = NotificationCompat.Builder(context, "watchlist_alerts")
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle("🟦 Buy Signal: $symbol")
    .setContentText("LTP ৳$ltp has reached your target ৳$target")
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setAutoCancel(true)
    .build()
NotificationManagerCompat.from(context).notify(symbol.hashCode(), notification)
```

### Re-trigger logic:
- Notification fires ONCE per signal event.
- Track with a `Map<String, Boolean>` called `hasNotified` in the WatchlistRepository.
- Only re-notify if price previously rose ABOVE target after last notification, then drops
  back to ≤ target again.
- Reset `hasNotified[symbol] = false` when `ltp > target` is observed.

### Watchlist card layout updates:
- Add `YCP: ৳{ycp}` in small `MaterialTheme.typography.labelSmall` grey text.
- Show direction arrow: ↑ if ltp > ycp, ↓ if ltp < ycp, → if equal.
  Use `Color(0xFF4CAF50)` for ↑, `Color(0xFFF44336)` for ↓, grey for →.

---

## Change 4: YCP display

**Where:** Search screen result card + Watchlist cards.

### Search screen result card — add these new fields:
```
LTP:       ৳{ltp}           (existing)
YCP:       ৳{ycp}           (NEW)
HIGH:      ৳{high}          (NEW — in green text)
LOW:       ৳{low}           (NEW — in red text)
Total P&L: ৳{pnl}          (existing)
% Change:  {pctChange}%     (existing)
```

Layout: use a 2-column `Row` grid inside a `Card`. Each field is a `Column` with a
`Text` label and `Text` value.

### Watchlist cards:
- Add `YCP: ৳{ycp}` below the LTP line in `MaterialTheme.typography.bodySmall`.

---

## Change 5: HIGH / LOW in Search tab

Covered in Change 4. Additional detail:
- HIGH: use `Color(0xFF4CAF50)` (green)
- LOW: use `Color(0xFFF44336)` (red)
- Both shown prominently in the result card.

---

## Room Database Changes

The existing `StockQuote` entity (if it exists) or cached data model needs new fields:
```kotlin
@Entity(tableName = "stock_cache")
data class StockCacheEntity(
    @PrimaryKey val symbol: String,
    val ltp: Double,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val closep: Double = 0.0,
    val ycp: Double = 0.0,
    val change: Double = 0.0,
    val pctChange: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
```

Add a Room migration if the table already exists:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE stock_cache ADD COLUMN high REAL NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE stock_cache ADD COLUMN low REAL NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE stock_cache ADD COLUMN ycp REAL NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE stock_cache ADD COLUMN closep REAL NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE stock_cache ADD COLUMN pctChange REAL NOT NULL DEFAULT 0")
    }
}
```

---

## Dependencies to add in `app/build.gradle.kts`

```kotlin
implementation("org.jsoup:jsoup:1.17.2")
implementation("androidx.core:core-ktx:1.12.0")  // for NotificationCompat
implementation("androidx.work:work-runtime-ktx:2.9.0")  // optional, for background refresh
```

---

## Summary of files to modify / create

| File | Change |
|------|--------|
| `data/remote/DseApiClient.kt` | Add `fetchFullQuotesHtml()` method, add Jsoup parsing |
| `data/remote/QuotesParser.kt` | Add `parseFullHtml()` function |
| `data/local/entity/StockCacheEntity.kt` | Add high, low, ycp, closep, pctChange fields |
| `data/local/AppDatabase.kt` | Add migration 1→2 |
| `data/repository/StockRepository.kt` | Fetch both sources in parallel, merge, update Room |
| `ui/screens/SearchScreen.kt` | Add autocomplete, show YCP/HIGH/LOW in result card |
| `ui/screens/WatchlistScreen.kt` | Add autocomplete, sky blue buy signal card, YCP display |
| `ui/screens/WatchlistViewModel.kt` | Buy signal detection, re-trigger logic, notification |
| `ui/components/StockCard.kt` | Updated card composable with new fields |
| `MainActivity.kt` | Request POST_NOTIFICATIONS permission at runtime (Android 13+) |
| `DseApp.kt` | Create notification channel |
| `AndroidManifest.xml` | Add POST_NOTIFICATIONS permission |

---

## Constraints / Rules
- Do NOT change the tab structure (Portfolio, Holdings, Watchlist, Search).
- Do NOT change existing Portfolio/Holdings logic.
- Keep dark mode (follows system theme) working for all new elements.
- Keep offline support: if both fetches fail, Room cached data is shown with "stale" indicator.
- All currency must use `৳` (Bangladeshi Taka symbol).
- Keep MVVM architecture — no business logic in Composables.
- The app must still compile for Min SDK 26.
- For Android 13+ (API 33+), request `POST_NOTIFICATIONS` permission at runtime before
  sending notifications.
- Do not add unnecessary dependencies.
