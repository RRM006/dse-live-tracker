# Google Play Publish Plan — DSE Live Tracker

## Overview

Publish the DSE Live Tracker Android app to Google Play Store. The app is a stock tracking app (package: `com.dselivetracker`, minSdk: 26, targetSdk: 34).

---

## Phase 1: Privacy Policy (GitHub Pages)

### Steps
1. Create `docs/privacy-policy.html` with proper privacy policy content
2. The user enables GitHub Pages in repo Settings → Pages → branch `main`, folder `/docs`
3. Privacy policy URL: `https://RRM006.github.io/dse-live-tracker/privacy-policy`

### File to create: `docs/privacy-policy.html`

Content:
- App name: DSE Live Tracker
- States NO personal data is collected
- All data stored locally on device
- Internet used only to fetch DSE stock data
- Uses CORS proxy (corsproxy.io) only for public data
- Notifications are local-only
- Contact: rrmanik006@gmail.com
- Last updated: May 26, 2026

---

## Phase 2: App Graphics

### 512x512 Play Store Icon

Current app icon is an adaptive icon:
- Background: white (`#FFFFFF`)
- Foreground: blue line chart (stock chart pattern) using `#2563EB` and `#3B82F6`
- The icon already looks good and appropriate for Finance category

**Action needed**: The user needs to generate a 512x512 PNG from the adaptive icon. Options:
- Use Android Studio: `File → New → Image Asset` → set "Icon Type" to "App Icon" → resize to 512x512 → export
- Or use an online tool like `https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html`
- Or I can create an SVG version if they share the exact icon

### 1024x500 Feature Graphic

I will create `docs/feature-graphic.svg` with:
- Dark background (`#0F172A`)
- App name "DSE Live Tracker" in white bold text
- Tagline: "Track DSE stocks in real-time"
- Small stock chart line decoration (matching app icon style)
- Blue accent (`#2563EB`)

The user can open the SVG in a browser, screenshot it, or convert to PNG.

### Screenshots (user does on phone/emulator)

Required: at least 2 phone screenshots (recommend 4-6, 1080x1920)

Suggestions to capture:
1. Portfolio tab with stocks added → shows summary card with invested/current/P&L
2. Holdings tab → shows stock cards with P&L, YCP, direction arrows
3. Watchlist with a buy signal triggered → shows sky blue border + "✅ BUY SIGNAL"
4. Search result card → shows LTP, YCP, HIGH (green), LOW (red), % change in 2-column layout
5. Dark mode version of any tab (optional but good)

---

## Phase 3: App Version & Build Prep

### Update `android/app/build.gradle.kts`

Current version: `versionCode = 1`, `versionName = "1.0.0"` — these are correct for first release.

### Remove unused import

File: `android/app/src/main/java/com/dselivetracker/ui/screens/portfolio/PortfolioViewModel.kt`
- Remove line 8: `import com.dselivetracker.data.remote.QuotesParser` (already done)

### Keystore Generation (user does in Android Studio)

1. Open project in Android Studio
2. **Build → Generate Signed Bundle / APK**
3. Select **Android App Bundle (AAB)**
4. Click **Create new...**
5. Fill in:
   - Key store path: `C:\Users\User\dse-keystore.jks` (safe location)
   - Password: (create a strong password, write it down)
   - Confirm password
   - Alias: `dse-tracker-key`
   - Validity (years): `25`
   - First and Last Name: (user's name)
6. Click OK
7. ⚠️ **BACKUP** the `.jks` file to USB drive + Google Drive + another safe place

### Build Signed AAB (user does in Android Studio)

1. **Build → Generate Signed Bundle / APK** → **Android App Bundle**
2. Select the keystore created above
3. Select `release` build variant
4. Click **Finish**
5. AAB file: `android/app/release/app-release.aab`

---

## Phase 4: Store Listing Descriptions

### App Name: DSE Live Tracker

### Short Description (80 chars):
```
Track DSE stocks, build a portfolio, get live price updates & buy alerts
```

### Full Description (for store listing):
```
DSE Live Tracker helps you monitor Dhaka Stock Exchange (DSE) stocks in real-time. Build your portfolio, track holdings, set watchlist alerts, and get notified when stocks hit your target price.

★ PORTFOLIO MANAGEMENT
Add stocks with buy price and quantity. View your total investment, current value, and profit/loss at a glance.

★ HOLDINGS TRACKING
See individual stock performance with live P&L calculations. Sort by profit/loss or percentage change. Each card shows YCP (Yesterday's Closing Price) with direction arrows indicating daily movement.

★ WATCHLIST WITH BUY SIGNALS
Add stocks to your watchlist with optional target prices. When the Last Traded Price (LTP) reaches or falls below your target, the card turns sky blue and displays a "BUY SIGNAL" badge. You'll also receive a push notification on your phone.

★ SEARCH & RESEARCH
Look up any DSE stock symbol to see LTP, YCP, Day's HIGH, Day's LOW, and percentage change in a clean 2-column layout.

★ AUTO-REFRESH
Prices refresh automatically every 30 seconds so you never miss a move.

★ DARK MODE
Comfortable viewing day or night with a built-in dark mode.

★ PRIVACY FIRST
All your data (portfolio, holdings, watchlist) is stored locally on your device. No personal information is collected, stored, or shared.

Note: This app requires internet connectivity to fetch live stock data from dsebd.org. Data is delayed by approximately 15-30 minutes as provided by the Dhaka Stock Exchange.

DISCLAIMER: DSE Live Tracker is an independent tracking tool and is not affiliated with or endorsed by the Dhaka Stock Exchange (DSE). All stock data is publicly available at dsebd.org. This app does not provide financial advice. Always do your own research before making investment decisions.
```

### Tags/Categories:
- **Category**: Finance
- **Tags**: stocks, DSE, Bangladesh, finance, stock market, trading, portfolio, tracker, investment

---

## Phase 5: GitHub Pages Setup (user does)

1. Go to https://github.com/RRM006/dse-live-tracker
2. Click **Settings** tab
3. Click **Pages** in left sidebar
4. Under "Branch": select `main`, folder `/docs`
5. Click **Save**
6. Wait 2-3 minutes
7. Privacy policy will be live at: `https://RRM006.github.io/dse-live-tracker/privacy-policy`

---

## Phase 6: Play Console Setup (user does, I guide)

### Step 1: Create Developer Account
1. Go to https://play.google.com/console/signup
2. Sign in with Google account
3. Pay $25 registration fee (one-time, lifetime)
4. Fill developer name, contact info
5. Wait 24-48 hours for verification

### Step 2: Create New App
1. Click **Create app**
2. App name: `DSE Live Tracker`
3. Default language: English
4. App or game: App
5. Free or paid: Free
6. Click **Create app**

### Step 3: Fill Store Listing
Navigate to **Store presence → Main store listing**:

| Field | Value |
|-------|-------|
| App name | DSE Live Tracker |
| Short description | (from Phase 4 above) |
| Full description | (from Phase 4 above) |
| Screenshots | Upload 2-6 phone screenshots |
| Tablet screenshots | Optional |
| Feature graphic | Upload 1024x500 PNG |
| App icon | Upload 512x512 PNG |
| App category | Finance |
| Contact email | rrmanik006@gmail.com |
| Privacy Policy URL | `https://RRM006.github.io/dse-live-tracker/privacy-policy` |

### Step 4: App Signing
Navigate to **Setup → App integrity**:
1. Choose **Let Google manage and protect your app signing key** (recommended)
2. Upload your `app-release.aab` file
3. Google will extract the public key

### Step 5: Content Rating
Navigate to **Setup → Content Rating**:
1. Click **Continue**
2. Select category: Finance
3. Answer questionnaire:
   - Violence: None
   - Sexual content: None
   - Hate speech: None
   - Drugs/alcohol: None
   - User interaction: No
   - Sharing location: No
4. Submit → Expected rating: **Everyone** or **Teen**

### Step 6: Pricing & Distribution
Navigate to **Setup → Pricing & Distribution**:
1. Set to **Free** (cannot change to paid later)
2. Countries: **All countries and territories**
3. Check all consent boxes:
   - Content guidelines
   - US export laws
4. Ads: **No** (app has no ads)

### Step 7: Review & Publish
Navigate to **Production → Overview**:
1. Click **Review app**
2. Fix any warnings (usually just confirm age rating, privacy policy)
3. Click **Send for review**
4. Review takes a few hours to 2 days
5. Once approved, app goes live!

---

## Files to Create/Modify

| File | Action | Description |
|------|--------|-------------|
| `docs/privacy-policy.html` | **Create** | Privacy policy page for GitHub Pages |
| `docs/feature-graphic.svg` | **Create** | 1024x500 feature graphic template |
| `..opencode/plans/google-play-publish-plan.md` | **Create** | This plan file |

---

## Execution Order

1. Create `docs/privacy-policy.html`
2. Create `docs/feature-graphic.svg`
3. User enables GitHub Pages on repo
4. User generates keystore in Android Studio
5. User builds signed AAB
6. User creates Play Console account
7. User fills store listing (I provide all texts)
8. User uploads assets and submits
