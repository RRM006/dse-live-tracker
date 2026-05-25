# DSE Live Tracker — Android App

Native Android app for Bangladeshi retail investors to track Dhaka Stock Exchange (DSE) stocks in real-time. Built with **Kotlin + Jetpack Compose + Material 3**.

## Features

| Tab | Description |
|-----|-------------|
| **Portfolio** | Summary card showing total invested, current value, and overall P&L with percentage. Add stocks with symbol, buy price, and quantity. |
| **Holdings** | Full scrollable list of your portfolio stocks with live P&L per card. Sort by P&L, % change, or name. Tap a card to jump to Search with pre-filled data. Remove with 3-second Undo. |
| **Watchlist** | Monitor any DSE symbol with an optional target price. Cards show live LTP, direction arrow, and % change. Green accent when target is reached. |
| **Search** | Look up any DSE symbol with autocomplete suggestions. Enter buy price and quantity to get live LTP, total P&L, and % change. Add results directly to your portfolio. |

### Additional Features

- Auto-refresh every 30 seconds
- Dark mode (follows system theme)
- Market status indicator (Open/Closed based on DSE hours: 10:00-14:30, Sun-Thu)
- Offline support — last fetched prices are cached and displayed when offline
- Data sourced from `dsebd.org/datafile/quotes.txt`

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (bottom nav with 4 tabs) |
| Database | SQLite via Room |
| Networking | OkHttp |
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
│   │   │   │   │   ├── AppDatabase.kt    # Room database
│   │   │   │   │   ├── dao/              # Data access objects
│   │   │   │   │   └── entity/           # Room entities
│   │   │   │   ├── remote/               # API client + quotes parser
│   │   │   │   └── repository/           # Data repositories
│   │   │   └── ui/
│   │   │       ├── navigation/           # Bottom nav + nav host
│   │   │       ├── theme/                # Colors, typography, theme
│   │   │       ├── components/           # Reusable composables
│   │   │       └── screens/              # 4 tab screens + ViewModels
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

Stock data is fetched directly from:
- Primary: `https://www.dsebd.org/datafile/quotes.txt`
- Fallback: `https://corsproxy.io/?https://www.dsebd.org/datafile/quotes.txt`

No backend server or API key is required. All portfolio/watchlist data is stored locally on your device.

## Storage

- APK size: ~5 MB
- Database: few KB (text-only: symbol names, prices, quantities)
- Runtime memory: ~50-100 MB
- **Total storage: under 10 MB**

## Usage

| Tab | What to do |
|-----|-----------|
| Portfolio | Fill in Symbol, Buy Price, Quantity → tap "+ Add to Portfolio" |
| Holdings | Browse your stocks, use Sort dropdown, tap a card to update in Search, remove with Undo |
| Watchlist | Enter a Symbol, optionally set a Target Price → tap "+ Add to Watchlist" |
| Search | Type a symbol → autocomplete appears → fill Buy Price + Quantity → tap "Check Price" |

## Architecture Overview

```
User taps refresh → ViewModel → DseApiClient.fetchQuotes() → dsebd.org
                                    ↓
                              QuotesParser.parse()
                                    ↓
                    Repository updates Room DB (SQLite)
                                    ↓
                    UI recomposes via StateFlow ← Flow from Room
```

When offline, Room returns the last cached prices, so you can still view your portfolio with a "stale data" indicator.
