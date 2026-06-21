(function () {
  'use strict';

  const QUOTES_URL = 'https://www.dsebd.org/datafile/quotes.txt';
  const QUOTES_URL_HTTP = 'http://www.dsebd.org/datafile/quotes.txt';
  const PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(QUOTES_URL);
  const FULL_QUOTES_URL = 'https://www.dsebd.org/latest_share_price_scroll_l.php';
  const FULL_QUOTES_URL_HTTP = 'http://www.dsebd.org/latest_share_price_scroll_l.php';
  const FULL_PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(FULL_QUOTES_URL);
  const BY_LTP_URL = 'https://www.dsebd.org/latest_share_price_scroll_by_ltp.php';
  const BY_LTP_URL_HTTP = 'http://www.dsebd.org/latest_share_price_scroll_by_ltp.php';
  const BY_LTP_PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(BY_LTP_URL);
  const CBUL_URL = 'https://www.dsebd.org/cbul.php';
  const CBUL_URL_HTTP = 'http://www.dsebd.org/cbul.php';
  const CBUL_PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(CBUL_URL);
  const CAT_URL = 'https://www.dsebd.org/latest_share_price_scroll_group.php';
  const CAT_URL_HTTP = 'http://www.dsebd.org/latest_share_price_scroll_group.php';
  const CAT_PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(CAT_URL);
  const TOP20_URL = 'https://www.dsebd.org/top_20_share.php';
  const TOP20_URL_HTTP = 'http://www.dsebd.org/top_20_share.php';
  const TOP20_PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(TOP20_URL);
  const MARKET_STATUS_URL = 'https://www.dsebd.org/';
  const MARKET_STATUS_URL_HTTP = 'http://www.dsebd.org/';
  const MARKET_PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(MARKET_STATUS_URL);
  const BROKER_RATE = 0.004;
  const DSE_RATE = 0.00025;
  const AIT_RATE = 0.0005;
  const BUY_COMMISSION_RATE = BROKER_RATE + DSE_RATE;
  const SELL_COMMISSION_RATE = BROKER_RATE + DSE_RATE + AIT_RATE;
  const CAP_GAINS_TAX_RATE = 0.15;
  const CAP_GAINS_TAX_THRESHOLD = 5000000;
  const AUTO_REFRESH_MS = 30000;
  const MAX_RETRIES = 1;
  const RETRY_DELAY = 1500;
  const WL_KEY = 'dse_watchlist';
  const WQ_KEY = 'dse_quickwatch';
  const TH_KEY = 'dse_trade_history';
  const DARK_KEY = 'dse_dark_mode';
  const SORT_KEY = 'dse_sort';
  const UNDO_MS = 3000;

  const STOCK_NAMES = {
    "1JANATAMF": "First Janata Bank Mutual Fund",
    "AAMRANET": "aamra networks limited",
    "AAMRATECH": "aamra technologies limited",
    "ABBANK": "AB Bank Limited",
    "ACI": "ACI Limited",
    "ACIFORMULA": "ACI Formulations Limited",
    "ACMELAB": "The ACME Laboratories Limited",
    "ADNTEL": "ADN Telecom Limited",
    "ADVENT": "Advent Pharma Limited",
    "AFCAGRO": "AFC Agro Biotech Ltd.",
    "AFTABAUTO": "Aftab Automobiles Limited",
    "AGNISYSL": "Agni Systems Ltd.",
    "ALARABANK": "Al-Arafah Islami Bank Ltd",
    "ALIF": "Alif Manufacturing Company Ltd.",
    "AMANFEED": "Aman Feed Limited",
    "AMBEEPHA": "Ambee Pharmaceuticals PLC",
    "AMCL(PRAN)": "Agricultural Marketing Company Ltd. (Pran)",
    "ANWARGALV": "Anwar Galvanizing Ltd.",
    "APEXFOOT": "Apex Footwear Limited",
    "APEXSPINN": "Apex Spinning & Knitting Mills Limited",
    "BATASHOE": "Bata Shoe Company (Bangladesh) Limited",
    "BATBC": "British American Tobacco Bangladesh Company Limited",
    "BBS": "Bangladesh Building Systems Ltd.",
    "BBSCABLES": "BBS Cables Limited",
    "BERGERPBL": "Berger Paints Bangladesh Limited",
    "BEXIMCO": "Bangladesh Export Import Company Limited",
    "BRACBANK": "BRAC Bank PLC",
    "BSCCL": "Bangladesh Submarine Cables PLC",
    "BSRMLTD": "Bangladesh Steel Re-Rolling Mills Limited",
    "BXPHARMA": "Beximco Pharmaceuticals PLC",
    "CITYBANK": "City Bank PLC",
    "CONFIDCEM": "Confidence Cement Limited",
    "DUTCHBANGL": "Dutch-Bangla Bank PLC",
    "EBL": "Eastern Bank PLC",
    "FORTUNE": "Fortune Shoes Limited",
    "GP": "Grameenphone Ltd.",
    "IDLC": "IDLC Finance PLC",
    "IFADAUTOS": "IFAD Autos Limited",
    "ISLAMIBANK": "Islami Bank Bangladesh PLC",
    "JAMUNABANK": "Jamuna Bank PLC",
    "LHB": "LafargeHolcim Bangladesh PLC",
    "LANKABANG": "LankaBangla Finance PLC",
    "MARICO": "Marico Bangladesh Limited",
    "MJLBD": "MJL Bangladesh PLC",
    "MTB": "Mutual Trust Bank PLC",
    "NATLIFEINS": "National Life Insurance Co. Ltd.",
    "OLYMPIC": "Olympic Industries PLC",
    "ORIONPHARM": "Orion Pharma Ltd.",
    "POWERGRID": "Power Grid Bangladesh PLC",
    "RECKITTBEN": "Reckitt Benckiser (Bangladesh) PLC",
    "RENATA": "Renata PLC",
    "ROBI": "Robi Axiata PLC",
    "SQURPHARMA": "Square Pharmaceuticals PLC",
    "SUMITPOWER": "Summit Power Limited",
    "TITASGAS": "Titas Gas Transmission & Distribution Co. Ltd.",
    "UPGDCL": "United Power Generation & Distribution Company Ltd.",
    "UTTARABANK": "Uttara Bank PLC",
    "WALTONHIL": "Walton Hi-Tech Industries PLC"
  };

  function getStockName(s) { return STOCK_NAMES[s] || ''; }
  function getBreakerPctForPrice(price) {
    if (price <= 200) return 10.0;
    if (price <= 500) return 8.75;
    if (price <= 1000) return 7.5;
    if (price <= 2000) return 6.25;
    if (price <= 5000) return 5.0;
    return 3.75;
  }

  const $ = (id) => document.getElementById(id);

  const dom = {};
  function cacheDOM() {
    dom.symbol = $('symbol');
    dom.checkBtn = $('checkBtn');
    dom.refreshBtn = $('refreshBtn');
    dom.darkToggle = $('darkToggle');
    dom.viewPortfolio = $('viewPortfolio');
    dom.viewHoldings = $('viewHoldings');
    dom.viewWatchlist = $('viewWatchlist');
    dom.viewSearch = $('viewSearch');
    dom.viewTop20 = $('viewTop20');
    dom.viewTradeHistory = $('viewTradeHistory');
    dom.tabs = document.querySelectorAll('.tab');
    dom.wlSummary = $('wlSummary');
    dom.pieChart = $('pieChart');
    dom.pieChartContainer = $('pieChartContainer');
    dom.addSymbol = $('addSymbol');
    dom.addBuyPrice = $('addBuyPrice');
    dom.addQty = $('addQty');
    dom.addBuyDate = $('addBuyDate');
    dom.addBtn = $('addBtn');
    dom.resultCard = $('resultCard');
    dom.resultSymbol = $('resultSymbol');
    dom.resultCompany = $('resultCompany');
    dom.resultLtp = $('resultLtp');
    dom.resultClosep = $('resultClosep');
    dom.resultPercent = $('resultPercent');
    dom.resultCategory = $('resultCategory');
    dom.resultBreaker = $('resultBreaker');
    dom.resultTickSize = $('resultTickSize');
    dom.resultOpenAdj = $('resultOpenAdj');
    dom.resultLowerLimit = $('resultLowerLimit');
    dom.resultUpperLimit = $('resultUpperLimit');
    dom.resultCbulSection = $('resultCbulSection');
    dom.resultNextSection = $('resultNextSection');
    dom.resultNextBreaker = $('resultNextBreaker');
    dom.resultNextUpper = $('resultNextUpper');
    dom.resultNextLower = $('resultNextLower');
    dom.loadingContainer = $('loadingContainer');
    dom.errorMsg = $('errorMsg');
    dom.statusText = $('statusText');
    dom.marketStatus = $('marketStatus');
    dom.autocomplete = $('autocompleteDropdown');
    dom.searchEmpty = $('searchEmpty');
    dom.qwSymbol = $('qwSymbol');
    dom.qwTarget = $('qwTarget');
    dom.qwAddBtn = $('qwAddBtn');
    dom.qwContainer = $('qwContainer');
    dom.holdingsContainer = $('holdingsContainer');
    dom.holdingsSort = $('holdingsSort');
    dom.holdingsCount = $('holdingsCount');
    dom.holdingsAddBtn = $('holdingsAddBtn');
    dom.holdingsBadge = $('holdingsBadge');
    dom.watchlistBadge = $('watchlistBadge');
    dom.snackbar = $('snackbar');
    dom.snackbarText = $('snackbarText');
    dom.snackbarUndo = $('snackbarUndo');
    dom.resultYcp = $('resultYcp');
    dom.resultHigh = $('resultHigh');
    dom.resultLow = $('resultLow');
    dom.sellDialogOverlay = $('sellDialogOverlay');
    dom.sellSymbol = $('sellSymbol');
    dom.sellPrice = $('sellPrice');
    dom.sellDate = $('sellDate');
    dom.sellMaturityWarning = $('sellMaturityWarning');
    dom.sellBrokerFee = $('sellBrokerFee');
    dom.sellDseFee = $('sellDseFee');
    dom.sellAitFee = $('sellAitFee');
    dom.sellBuyComm = $('sellBuyComm');
    dom.sellSellComm = $('sellSellComm');
    dom.sellEstValue = $('sellEstValue');
    dom.sellEstPnl = $('sellEstPnl');
    dom.sellCancelBtn = $('sellCancelBtn');
    dom.sellConfirmBtn = $('sellConfirmBtn');
    dom.sellDialogClose = $('sellDialogClose');
    dom.alertBanner = $('alertBanner');
    dom.bestToBuySection = $('bestToBuySection');
    dom.bestToBuyContainer = $('bestToBuyContainer');
    dom.top20Container = $('top20Container');
    dom.tradeHistoryContainer = $('tradeHistoryContainer');
    dom.viewTradeHistoryBtn = $('viewTradeHistoryBtn');
    dom.backToHoldingsBtn = $('backToHoldingsBtn');
  }

  let watchlist = [];
  let quickWatch = [];
  let tradeHistory = [];
  let autoCompleteCache = [];
  let refreshInterval = null;
  let lastSearchSymbol = '';
  let hasResult = false;
  let isDark = false;
  let sortMode = 'pnl-asc';
  let undoTimeout = null;
  let pendingRemove = null;
  let pendingRemoveIndex = -1;
  let stockData = {};
  let cbulData = {};
  let categoryData = {};
  let top20Data = [];
  let acActiveInput = null;
  let notifiedAt = {};
  let dseDataTimestamp = null;
  let dseDataDate = null;
  let marketStatusFromDSE = null;
  let sellingSymbol = null;
  let sellingIndex = -1;

  document.addEventListener('DOMContentLoaded', () => {
    cacheDOM();
    loadTheme();
    loadWatchlist();
    loadQuickWatch();
    loadTradeHistory();
    loadSort();
    bindEvents();
    renderSummary();
    renderHoldings();
    renderQuickWatch();
    renderTradeHistory();
    updateBadges();
    updateMarketStatus();
    dom.symbol.focus();
    checkContextSymbol();

    const activeView = document.querySelector('.tab.active').dataset.view;
    if (activeView === 'portfolio' || activeView === 'holdings') {
      if (watchlist.length) refreshAllData();
    } else if (activeView === 'watchlist' && quickWatch.length) {
      refreshAllData();
    }

    if (watchlist.length || quickWatch.length) {
      startAutoRefresh();
    }
  });

  function loadSort() {
    try {
      const val = localStorage.getItem(SORT_KEY);
      if (val) sortMode = val;
    } catch (e) {}
    dom.holdingsSort.value = sortMode;
  }

  function saveSort() {
    try { localStorage.setItem(SORT_KEY, sortMode); } catch (e) {}
  }

  function bindEvents() {
    dom.tabs.forEach(tab => {
      tab.addEventListener('click', () => switchView(tab.dataset.view));
    });

    dom.darkToggle.addEventListener('click', toggleDark);

    dom.holdingsSort.addEventListener('change', () => {
      sortMode = dom.holdingsSort.value;
      saveSort();
      renderHoldings();
    });

    dom.refreshBtn.addEventListener('click', refreshAllData);

    dom.symbol.addEventListener('input', () => {
      dom.symbol.value = dom.symbol.value.toUpperCase();
      acActiveInput = 'search';
      handleAutocomplete(dom.symbol.value);
    });
    dom.symbol.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.checkBtn.click();
      if (e.key === 'ArrowDown') moveAutocomplete(1);
      if (e.key === 'ArrowUp') moveAutocomplete(-1);
    });
    dom.symbol.addEventListener('blur', () => setTimeout(hideAutocomplete, 200));

    dom.checkBtn.addEventListener('click', onCheckPrice);

    dom.addBtn.addEventListener('click', onAddToWatchlist);

    dom.addSymbol.addEventListener('input', () => {
      dom.addSymbol.value = dom.addSymbol.value.toUpperCase();
    });
    dom.addSymbol.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.addBuyPrice.focus();
    });
    dom.addBuyPrice.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.addQty.focus();
    });
    dom.addQty.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.addBuyDate.focus();
    });
    dom.addBuyDate.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.addBtn.click();
    });

    dom.qwSymbol.addEventListener('input', () => {
      dom.qwSymbol.value = dom.qwSymbol.value.toUpperCase();
      acActiveInput = 'watchlist';
      handleAutocomplete(dom.qwSymbol.value);
    });
    dom.qwSymbol.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowDown') moveAutocomplete(1);
      if (e.key === 'ArrowUp') moveAutocomplete(-1);
      if (e.key === 'Enter') { hideAutocomplete(); dom.qwTarget.focus(); }
    });
    dom.qwSymbol.addEventListener('blur', () => setTimeout(hideAutocomplete, 200));

    dom.qwTarget.addEventListener('input', () => {
      dom.qwTarget.classList.toggle('input-target-set', dom.qwTarget.value.trim() !== '');
    });
    dom.qwTarget.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.qwAddBtn.click();
    });
    dom.qwAddBtn.addEventListener('click', onAddToQuickWatch);

    dom.holdingsAddBtn.addEventListener('click', () => {
      switchView('portfolio');
      dom.addSymbol.focus();
    });

    dom.snackbarUndo.addEventListener('click', cancelRemove);

    dom.sellPrice.addEventListener('input', updateSellPreview);
    dom.sellDate.addEventListener('input', updateSellMaturity);
    dom.sellCancelBtn.addEventListener('click', closeSellDialog);
    dom.sellDialogClose.addEventListener('click', closeSellDialog);
    dom.sellConfirmBtn.addEventListener('click', confirmSell);
    dom.sellDialogOverlay.addEventListener('click', (e) => {
      if (e.target === dom.sellDialogOverlay) closeSellDialog();
    });

    dom.viewTradeHistoryBtn.addEventListener('click', () => {
      switchView('tradehistory');
    });
    dom.backToHoldingsBtn.addEventListener('click', () => {
      switchView('holdings');
    });
  }

  function switchView(view) {
    dom.tabs.forEach(t => t.classList.toggle('active', t.dataset.view === view));
    dom.viewPortfolio.classList.toggle('active', view === 'portfolio');
    dom.viewHoldings.classList.toggle('active', view === 'holdings');
    dom.viewWatchlist.classList.toggle('active', view === 'watchlist');
    dom.viewSearch.classList.toggle('active', view === 'search');
    dom.viewTop20.classList.toggle('active', view === 'top20');
    dom.viewTradeHistory.classList.toggle('active', view === 'tradehistory');

    hideAutocomplete();

    if (view === 'portfolio') {
      renderSummary();
    } else if (view === 'holdings') {
      renderHoldings();
    } else if (view === 'watchlist') {
      renderQuickWatch();
    } else if (view === 'search') {
      if (!hasResult) {
        dom.searchEmpty.classList.remove('hidden');
      }
      dom.symbol.focus();
    } else if (view === 'top20') {
      renderTop20();
    } else if (view === 'tradehistory') {
      renderTradeHistory();
    }
  }

  function loadWatchlist() {
    try {
      const data = localStorage.getItem(WL_KEY);
      watchlist = data ? JSON.parse(data) : [];
    } catch (e) { watchlist = []; }
  }

  function saveWatchlist() {
    try { localStorage.setItem(WL_KEY, JSON.stringify(watchlist)); } catch (e) {}
  }

  function loadTradeHistory() {
    try {
      const data = localStorage.getItem(TH_KEY);
      tradeHistory = data ? JSON.parse(data) : [];
    } catch (e) { tradeHistory = []; }
  }

  function saveTradeHistory() {
    try { localStorage.setItem(TH_KEY, JSON.stringify(tradeHistory)); } catch (e) {}
  }

  function onAddToWatchlist() {
    const symbol = dom.addSymbol.value.trim().toUpperCase();
    const buyPrice = parseFloat(dom.addBuyPrice.value);
    const qty = parseInt(dom.addQty.value) || 1;
    const buyDate = dom.addBuyDate.value || null;

    if (!symbol) return;
    if (isNaN(buyPrice) || buyPrice <= 0) return;

    watchlist.push({ symbol, buyPrice, qty, buyDate });
    saveWatchlist();
    dom.addSymbol.value = '';
    dom.addBuyPrice.value = '';
    dom.addQty.value = '';
    dom.addBuyDate.value = '';
    renderSummary();
    renderHoldings();
    updateBadges();
    if (!refreshInterval) refreshAllData();
    dom.addSymbol.focus();
  }

  function removeFromWatchlist(idx) {
    watchlist.splice(idx, 1);
    saveWatchlist();
    renderSummary();
    renderHoldings();
    updateBadges();
  }

  function clickWatchlistItem(symbol) {
    dom.symbol.value = symbol;
    switchView('search');
    onCheckPrice();
  }

  function promptRemove(item) {
    if (undoTimeout) return;
    const idx = watchlist.indexOf(item);
    if (idx === -1) return;

    pendingRemove = { ...item, _ltp: item._ltp, _prevLtp: item._prevLtp, _direction: item._direction, _timestamp: item._timestamp };
    pendingRemoveIndex = idx;

    watchlist.splice(idx, 1);
    saveWatchlist();
    renderSummary();
    renderHoldings();
    updateBadges();

    dom.snackbarText.textContent = item.symbol + ' removed';
    dom.snackbarUndo.textContent = 'Undo';
    dom.snackbar.classList.remove('hidden');

    undoTimeout = setTimeout(() => {
      pendingRemove = null;
      pendingRemoveIndex = -1;
      undoTimeout = null;
      dom.snackbar.classList.add('hidden');
    }, UNDO_MS);
  }

  function cancelRemove() {
    if (!pendingRemove || !undoTimeout) return;
    clearTimeout(undoTimeout);
    undoTimeout = null;

    const insertAt = Math.min(pendingRemoveIndex, watchlist.length);
    watchlist.splice(insertAt, 0, pendingRemove);
    pendingRemove = null;
    pendingRemoveIndex = -1;
    saveWatchlist();
    renderSummary();
    renderHoldings();
    updateBadges();
    dom.snackbar.classList.add('hidden');
  }

  function loadQuickWatch() {
    try {
      const data = localStorage.getItem(WQ_KEY);
      quickWatch = data ? JSON.parse(data) : [];
    } catch (e) { quickWatch = []; }
  }

  function saveQuickWatch() {
    try { localStorage.setItem(WQ_KEY, JSON.stringify(quickWatch)); } catch (e) {}
  }

  function onAddToQuickWatch() {
    const symbol = dom.qwSymbol.value.trim().toUpperCase();
    if (!symbol) return;

    if (quickWatch.find(w => w.symbol === symbol)) return;

    const targetPrice = parseFloat(dom.qwTarget.value);
    const entry = { symbol };
    if (!isNaN(targetPrice) && targetPrice > 0) {
      entry.targetPrice = targetPrice;
    }

    quickWatch.push(entry);
    saveQuickWatch();
    dom.qwSymbol.value = '';
    dom.qwTarget.value = '';
    renderQuickWatch();
    updateBadges();
    if (!refreshInterval) refreshAllData();
    dom.qwSymbol.focus();
  }

  function removeFromQuickWatch(symbol) {
    quickWatch = quickWatch.filter(w => w.symbol !== symbol);
    saveQuickWatch();
    renderQuickWatch();
    updateBadges();
  }

  function getStockInfo(symbol) {
    return stockData[symbol] || null;
  }

  function getCategory(symbol) {
    return categoryData[symbol] || null;
  }

  function getCbul(symbol) {
    return cbulData[symbol] || null;
  }

  function calcSettlementDate(buyDateStr, category) {
    if (!buyDateStr) return null;
    const d = new Date(buyDateStr + 'T00:00:00');
    const cat = category || getDefaultCategory();
    const addDays = (cat === 'Z') ? 3 : 2;
    let added = 0;
    while (added < addDays) {
      d.setDate(d.getDate() + 1);
      const dow = d.getDay();
      if (dow !== 5 && dow !== 6) added++;
    }
    return d;
  }

  function isMature(buyDateStr, category) {
    const sd = calcSettlementDate(buyDateStr, category);
    if (!sd) return null;
    return new Date() >= sd;
  }

  function getDefaultCategory() {
    return 'A';
  }

  function openSellDialog(item) {
    const idx = watchlist.indexOf(item);
    if (idx === -1) return;
    sellingSymbol = item.symbol;
    sellingIndex = idx;
    dom.sellSymbol.textContent = item.symbol;
    dom.sellPrice.value = item._ltp || '';
    dom.sellDate.value = '';
    dom.sellMaturityWarning.classList.add('hidden');
    dom.sellConfirmBtn.disabled = false;
    dom.sellDialogOverlay.classList.remove('hidden');
    updateSellPreview();
    updateSellMaturity();
  }

  function closeSellDialog() {
    dom.sellDialogOverlay.classList.add('hidden');
    sellingSymbol = null;
    sellingIndex = -1;
  }

  function updateSellPreview() {
    const item = sellingIndex >= 0 ? watchlist[sellingIndex] : null;
    if (!item) return;
    const sellPrice = parseFloat(dom.sellPrice.value);
    if (isNaN(sellPrice) || sellPrice <= 0) {
      dom.sellBrokerFee.textContent = '0.00';
      dom.sellDseFee.textContent = '0.00';
      dom.sellAitFee.textContent = '0.00';
      dom.sellBuyComm.textContent = '0.00';
      dom.sellSellComm.textContent = '0.00';
      dom.sellEstValue.textContent = '0.00';
      dom.sellEstPnl.textContent = '0.00';
      return;
    }
    const qty = item.qty || 1;
    const buyPrice = item.buyPrice;
    const buyComm = buyPrice * qty * BUY_COMMISSION_RATE;
    const sellComm = sellPrice * qty * SELL_COMMISSION_RATE;
    const brokerFee = sellPrice * qty * BROKER_RATE;
    const dseFee = sellPrice * qty * DSE_RATE;
    const aitFee = sellPrice * qty * AIT_RATE;
    const estValue = sellPrice * qty;
    const estPnl = estValue - (buyPrice * qty) - buyComm - sellComm;

    dom.sellBrokerFee.textContent = '\u09F3' + formatBDT(brokerFee);
    dom.sellDseFee.textContent = '\u09F3' + formatBDT(dseFee);
    dom.sellAitFee.textContent = '\u09F3' + formatBDT(aitFee);
    dom.sellBuyComm.textContent = '\u09F3' + formatBDT(buyComm);
    dom.sellSellComm.textContent = '\u09F3' + formatBDT(sellComm);
    dom.sellEstValue.textContent = '\u09F3' + formatBDT(estValue);

    const isProfit = estPnl >= 0;
    dom.sellEstPnl.textContent = (isProfit ? '+' : '-') + '\u09F3' + formatBDT(Math.abs(estPnl));
    dom.sellEstPnl.className = 'sell-comm-row total final ' + (isProfit ? 'profit-text' : 'loss-text');
  }

  function updateSellMaturity() {
    const item = sellingIndex >= 0 ? watchlist[sellingIndex] : null;
    if (!item) return;
    const buyDate = item.buyDate;
    const cat = getCategory(sellingSymbol);
    if (!buyDate) {
      dom.sellMaturityWarning.classList.add('hidden');
      dom.sellConfirmBtn.disabled = false;
      return;
    }
    const sd = calcSettlementDate(buyDate, cat);
    if (!sd) return;
    const sellDateVal = dom.sellDate.value;
    if (sellDateVal) {
      const sellD = new Date(sellDateVal + 'T00:00:00');
      if (sellD < sd) {
        dom.sellMaturityWarning.textContent = 'Warning: Earliest allowed sell date is ' + sd.toLocaleDateString('en-BD');
        dom.sellMaturityWarning.classList.remove('hidden');
        dom.sellConfirmBtn.disabled = true;
        return;
      }
    }
    dom.sellMaturityWarning.classList.add('hidden');
    dom.sellConfirmBtn.disabled = false;
  }

  function confirmSell() {
    const item = sellingIndex >= 0 ? watchlist[sellingIndex] : null;
    if (!item) return;
    const sellPrice = parseFloat(dom.sellPrice.value);
    if (isNaN(sellPrice) || sellPrice <= 0) return;
    const sellDateVal = dom.sellDate.value || new Date().toISOString().split('T')[0];
    const qty = item.qty || 1;
    const buyPrice = item.buyPrice;
    const buyComm = buyPrice * qty * BUY_COMMISSION_RATE;
    const sellComm = sellPrice * qty * SELL_COMMISSION_RATE;
    const totalComm = buyComm + sellComm;
    const estValue = sellPrice * qty;
    const estPnl = estValue - (buyPrice * qty) - buyComm - sellComm;

    tradeHistory.push({
      symbol: sellingSymbol,
      buyPrice,
      sellPrice,
      qty,
      buyDate: item.buyDate || '',
      sellDate: sellDateVal,
      buyCommission: buyComm,
      sellCommission: sellComm,
      totalCommission: totalComm,
      realizedPnl: estPnl,
      timestamp: new Date().toISOString()
    });
    saveTradeHistory();

    watchlist.splice(sellingIndex, 1);
    saveWatchlist();
    closeSellDialog();
    renderSummary();
    renderHoldings();
    renderTradeHistory();
    updateBadges();

    dom.snackbarText.textContent = sellingSymbol + ' sold';
    dom.snackbarUndo.textContent = 'OK';
    dom.snackbar.classList.remove('hidden');
    setTimeout(() => { dom.snackbar.classList.add('hidden'); }, 2500);
  }

  function renderQuickWatch() {
    dom.qwContainer.innerHTML = '';
    dom.bestToBuySection.classList.add('hidden');
    dom.alertBanner.classList.add('hidden');

    if (!quickWatch.length) {
      dom.qwContainer.innerHTML = `
        <div class="wl-empty">
          <svg class="wl-empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2v20M2 12h20"/>
          </svg>
          <div class="wl-empty-title">Your watchlist is empty</div>
          <div class="wl-empty-sub">Add stocks to monitor their live prices</div>
        </div>`;
      return;
    }

    const buySignals = [];
    const others = [];

    quickWatch.forEach(item => {
      const info = getStockInfo(item.symbol);
      const ltpValue = item._ltp !== undefined ? item._ltp : (info ? info.ltp : undefined);
      const ycpValue = info ? info.ycp : undefined;

      if (ltpValue !== undefined && info && item._ltp === undefined) {
        item._ltp = ltpValue;
        item._ycp = ycpValue;
      }
      if (ycpValue !== undefined) item._ycp = ycpValue;

      let isBuySignal = false;
      let discount = 0;
      if (item.targetPrice && ltpValue !== undefined) {
        isBuySignal = ltpValue <= item.targetPrice;
        if (isBuySignal) {
          discount = ((item.targetPrice - ltpValue) / item.targetPrice);
        }
      }

      if (isBuySignal) {
        buySignals.push({ item, discount, ltpValue, ycpValue });
      } else {
        others.push({ item, ltpValue, ycpValue });
      }
    });

    buySignals.sort((a, b) => b.discount - a.discount);

    if (buySignals.length) {
      dom.bestToBuySection.classList.remove('hidden');
      dom.bestToBuyContainer.innerHTML = buySignals.map(({ item, discount, ltpValue, ycpValue }) =>
        createWatchlistCard(item, ltpValue, ycpValue, true)
      ).join('');

      const allSignals = buySignals.map(({ item, ltpValue }) =>
        item.symbol + ' LTP \u09F3' + formatBDT(ltpValue) + ' reached target'
      );
      dom.alertBanner.textContent = '\u2705 Buy Signal: ' + allSignals.join(' | ');
      dom.alertBanner.classList.remove('hidden');
    }

    others.forEach(({ item, ltpValue, ycpValue }) => {
      const card = document.createElement('div');
      card.innerHTML = createWatchlistCard(item, ltpValue, ycpValue, false);
      while (card.firstChild) dom.qwContainer.appendChild(card.firstChild);
    });

    quickWatch.forEach(item => {
      const info = getStockInfo(item.symbol);
      const ltpValue = item._ltp !== undefined ? item._ltp : (info ? info.ltp : undefined);
      if (item.targetPrice && ltpValue !== undefined && ltpValue <= item.targetPrice) {
        const lastNotified = notifiedAt[item.symbol];
        const wasAbove = lastNotified !== undefined && lastNotified === 'above';
        if (lastNotified === undefined || wasAbove) {
          notifiedAt[item.symbol] = ltpValue;
          try {
            chrome.runtime.sendMessage({
              type: 'SHOW_NOTIFICATION',
              title: '\uD83D\uDFE6 Buy Signal: ' + item.symbol,
              message: 'LTP \u09F3' + formatBDT(ltpValue) + ' has reached your target \u09F3' + formatBDT(item.targetPrice) + '. Possible entry point!',
              id: 'watchlist-' + item.symbol
            });
          } catch (e) {}
        }
      }
      if (item.targetPrice && ltpValue !== undefined && ltpValue > item.targetPrice) {
        notifiedAt[item.symbol] = 'above';
      }
    });

    dom.qwContainer.querySelectorAll('.wl-remove').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        removeFromQuickWatch(btn.dataset.symbol);
      });
    });
    dom.bestToBuyContainer.querySelectorAll('.wl-remove').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        removeFromQuickWatch(btn.dataset.symbol);
      });
    });
  }

  function createWatchlistCard(item, ltpValue, ycpValue, isBestToBuy) {
    const displayLtp = ltpValue;
    const displayYcp = ycpValue;

    const ltpDisplay = displayLtp !== undefined ? '\u09F3' + formatBDT(displayLtp) : 'Awaiting data...';

    let arrow = '';
    if (displayLtp !== undefined && displayYcp !== undefined) {
      if (displayLtp > displayYcp) arrow = '<span class="wl-direction up">\u2191</span>';
      else if (displayLtp < displayYcp) arrow = '<span class="wl-direction down">\u2193</span>';
      else arrow = '<span class="wl-direction flat">\u2192</span>';
    } else if (displayLtp !== undefined) {
      arrow = '<span class="wl-direction flat">\u2192</span>';
    }

    let pctDisplay = '';
    let pctColor = '';
    if (displayLtp !== undefined && displayYcp !== undefined && displayYcp > 0) {
      const pct = ((displayLtp - displayYcp) / displayYcp) * 100;
      const isProfit = pct >= 0;
      pctColor = isProfit ? 'profit-text' : 'loss-text';
      pctDisplay = (isProfit ? '+' : '') + pct.toFixed(2) + '%';
    } else if (displayLtp !== undefined) {
      pctDisplay = '--';
    }

    let targetBadge = '';
    let isBuySignal = false;
    if (item.targetPrice && displayLtp !== undefined) {
      isBuySignal = displayLtp <= item.targetPrice;
      if (isBuySignal) {
        targetBadge = '<span class="qw-target buy-signal">\u2705 BUY SIGNAL</span>';
      } else {
        targetBadge = '<span class="qw-target">\uD83C\uDFAF ' + formatBDT(item.targetPrice) + '</span>';
      }
    }

    let ycpLine = '';
    if (displayYcp !== undefined) {
      ycpLine = '<div class="wl-ycp">YCP: \u09F3' + formatBDT(displayYcp) + '</div>';
    }

    const cardClass = isBestToBuy ? 'wl-card qw-card-buy-signal' : (isBuySignal ? 'wl-card qw-card-buy-signal' : 'wl-card');

    return `<div class="${cardClass}" data-symbol="${item.symbol}">
      <div class="wl-card-top">
        <span class="wl-symbol">${item.symbol}</span>
        <div class="wl-card-top-right">
          ${targetBadge}
          ${arrow}
          <button class="wl-remove" data-symbol="${item.symbol}">&#x2715;</button>
        </div>
      </div>
      <div class="wl-card-mid">
        <span class="wl-ltp">LTP: ${ltpDisplay}</span>
        <span class="wl-percent ${pctColor}">${pctDisplay}</span>
      </div>
      ${ycpLine ? '<div class="wl-card-bot-single">' + ycpLine + '</div>' : ''}</div>`;
  }

  function getSortedWatchlist() {
    const sorted = [...watchlist];
    switch (sortMode) {
      case 'pnl-asc':
        sorted.sort((a, b) => {
          const pnlA = a._ltp !== undefined ? (a._ltp - a.buyPrice) * (a.qty || 1) - calcBuyCommission(a) : -Infinity;
          const pnlB = b._ltp !== undefined ? (b._ltp - b.buyPrice) * (b.qty || 1) - calcBuyCommission(b) : -Infinity;
          return (pnlA - pnlB) || 0;
        });
        break;
      case 'pnl-desc':
        sorted.sort((a, b) => {
          const pnlA = a._ltp !== undefined ? (a._ltp - a.buyPrice) * (a.qty || 1) - calcBuyCommission(a) : -Infinity;
          const pnlB = b._ltp !== undefined ? (b._ltp - b.buyPrice) * (b.qty || 1) - calcBuyCommission(b) : -Infinity;
          return (pnlB - pnlA) || 0;
        });
        break;
      case 'pct-asc':
        sorted.sort((a, b) => {
          const pctA = a._ltp !== undefined && a.buyPrice > 0 ? ((a._ltp - a.buyPrice) / a.buyPrice) * 100 : -Infinity;
          const pctB = b._ltp !== undefined && b.buyPrice > 0 ? ((b._ltp - b.buyPrice) / b.buyPrice) * 100 : -Infinity;
          return (pctA - pctB) || 0;
        });
        break;
      case 'pct-desc':
        sorted.sort((a, b) => {
          const pctA = a._ltp !== undefined && a.buyPrice > 0 ? ((a._ltp - a.buyPrice) / a.buyPrice) * 100 : -Infinity;
          const pctB = b._ltp !== undefined && b.buyPrice > 0 ? ((b._ltp - b.buyPrice) / b.buyPrice) * 100 : -Infinity;
          return (pctB - pctA) || 0;
        });
        break;
      case 'name-asc':
        sorted.sort((a, b) => (a.symbol || '').localeCompare(b.symbol || ''));
        break;
      case 'name-desc':
        sorted.sort((a, b) => (b.symbol || '').localeCompare(a.symbol || ''));
        break;
      default:
        break;
    }
    return sorted;
  }

  function calcBuyCommission(item) {
    return item.buyPrice * (item.qty || 1) * BUY_COMMISSION_RATE;
  }

  function calcNetPnl(item) {
    if (item._ltp === undefined) return null;
    const q = item.qty || 1;
    const grossPnl = (item._ltp - item.buyPrice) * q;
    return grossPnl - calcBuyCommission(item);
  }

  function renderSummary() {
    if (!watchlist.length) {
      dom.wlSummary.innerHTML = '';
      dom.pieChartContainer.classList.add('hidden');
      return;
    }

    let invested = 0, current = 0, countWithData = 0;
    let totalBuyComm = 0;
    watchlist.forEach(item => {
      const q = item.qty || 1;
      invested += item.buyPrice * q;
      totalBuyComm += calcBuyCommission(item);
      if (item._ltp !== undefined) {
        current += item._ltp * q;
        countWithData++;
      }
    });

    const totalFees = totalBuyComm + tradeHistory.reduce((s, t) => s + (t.totalCommission || 0), 0);
    const unrealizedPnl = current - invested - totalBuyComm;
    const realizedPnl = tradeHistory.reduce((s, t) => s + (t.realizedPnl || 0), 0);
    const capGains = realizedPnl > CAP_GAINS_TAX_THRESHOLD ? (realizedPnl - CAP_GAINS_TAX_THRESHOLD) * CAP_GAINS_TAX_RATE : 0;
    const netPnl = unrealizedPnl + realizedPnl - capGains;
    const isNetProfit = netPnl >= 0;
    const totalInvestedWithComm = invested + totalBuyComm;
    const pct = totalInvestedWithComm > 0 ? (netPnl / totalInvestedWithComm) * 100 : 0;

    dom.wlSummary.innerHTML = `
      <div class="wl-summary-card">
        <div class="wl-summary-top">
          <span class="wl-summary-label">Portfolio</span>
          <span class="wl-summary-count">${watchlist.length} stock${watchlist.length > 1 ? 's' : ''}</span>
        </div>
        <div class="wl-summary-rows">
          <div class="wl-summary-row">
            <span class="label">Invested</span>
            <span class="value">${formatBDT(invested)} BDT</span>
          </div>
          <div class="wl-summary-row">
            <span class="label">Current${countWithData < watchlist.length ? ' (' + countWithData + ' updated)' : ''}</span>
            <span class="value">${formatBDT(current)} BDT</span>
          </div>
          <div class="wl-summary-row">
            <span class="label">Unrealized P/L</span>
            <span class="value ${unrealizedPnl >= 0 ? 'profit-text' : 'loss-text'}">${unrealizedPnl >= 0 ? '+' : '-'}${formatBDT(Math.abs(unrealizedPnl))} BDT</span>
          </div>
          <div class="wl-summary-row">
            <span class="label">Realized P/L</span>
            <span class="value ${realizedPnl >= 0 ? 'profit-text' : 'loss-text'}">${realizedPnl >= 0 ? '+' : '-'}${formatBDT(Math.abs(realizedPnl))} BDT</span>
          </div>
          <div class="wl-summary-row">
            <span class="label">Total Fees</span>
            <span class="value">${formatBDT(totalFees)} BDT</span>
          </div>` +
      (capGains > 0 ? `
          <div class="wl-summary-row">
            <span class="label">Cap. Gains Tax</span>
            <span class="value loss-text">-${formatBDT(capGains)} BDT</span>
          </div>` : '') +
      `</div>
        <div class="wl-summary-total ${isNetProfit ? 'profit-text' : 'loss-text'}">
          <span>Net P/L</span>
          <span>${isNetProfit ? '+' : '-'}${formatBDT(Math.abs(netPnl))} BDT (${isNetProfit ? '+' : '-'}${Math.abs(pct).toFixed(2)}%)</span>
        </div>
      </div>`;

    renderPieChart();
  }

  function renderPieChart() {
    const stocksWithData = watchlist.filter(item => item._ltp !== undefined);
    if (stocksWithData.length < 2) {
      dom.pieChartContainer.classList.add('hidden');
      return;
    }
    dom.pieChartContainer.classList.remove('hidden');

    const canvas = dom.pieChart;
    const ctx = canvas.getContext('2d');
    const w = canvas.width, h = canvas.height;
    ctx.clearRect(0, 0, w, h);

    const cx = w / 2 - 20, cy = h / 2, outerR = Math.min(w, h) / 2 - 10, innerR = outerR * 0.55;

    const colors = ['#2563EB', '#16A34A', '#DC2626', '#F59E0B', '#8B5CF6', '#EC4899', '#06B6D4', '#F97316', '#14B8A6', '#6366F1'];
    const total = stocksWithData.reduce((s, i) => s + i._ltp * (i.qty || 1), 0);
    if (total <= 0) return;

    let startAngle = -Math.PI / 2;
    stocksWithData.forEach((item, idx) => {
      const val = item._ltp * (item.qty || 1);
      const slice = (val / total) * Math.PI * 2;
      const color = colors[idx % colors.length];
      ctx.beginPath();
      ctx.moveTo(cx + innerR * Math.cos(startAngle), cy + innerR * Math.sin(startAngle));
      ctx.arc(cx, cy, outerR, startAngle, startAngle + slice);
      ctx.arc(cx, cy, innerR, startAngle + slice, startAngle, true);
      ctx.closePath();
      ctx.fillStyle = color;
      ctx.fill();

      const midAngle = startAngle + slice / 2;
      const labelR = outerR + 14;
      const lx = cx + labelR * Math.cos(midAngle);
      const ly = cy + labelR * Math.sin(midAngle);
      ctx.fillStyle = getComputedStyle(document.body).getPropertyValue('--text-primary').trim() || '#0F172A';
      ctx.font = '10px Inter, sans-serif';
      ctx.textAlign = midAngle > Math.PI / 2 && midAngle < 3 * Math.PI / 2 ? 'right' : 'left';
      const pct = ((val / total) * 100).toFixed(1);
      ctx.fillText(item.symbol + ' ' + pct + '%', lx, ly + 3);

      startAngle += slice;
    });

    ctx.beginPath();
    ctx.arc(cx, cy, innerR, 0, Math.PI * 2);
    ctx.fillStyle = getComputedStyle(document.body).getPropertyValue('--bg-card').trim() || '#FFFFFF';
    ctx.fill();
  }

  function renderHoldings() {
    dom.holdingsContainer.innerHTML = '';

    if (!watchlist.length) {
      dom.holdingsContainer.innerHTML = `
        <div class="wl-empty">
          <div class="wl-empty-title">No holdings yet</div>
          <div class="wl-empty-sub">Go to Portfolio tab to add your first stock</div>
          <button class="btn-add wl-empty-action" id="emptyAddBtn">+ Add Stock</button>
        </div>`;
      dom.holdingsCount.textContent = '';
      const emptyAddBtn = dom.holdingsContainer.querySelector('#emptyAddBtn');
      if (emptyAddBtn) {
        emptyAddBtn.addEventListener('click', () => {
          switchView('portfolio');
          dom.addSymbol.focus();
        });
      }
      return;
    }

    const sorted = getSortedWatchlist();
    dom.holdingsCount.textContent = sorted.length + ' stock' + (sorted.length !== 1 ? 's' : '');

    const totalsBySymbol = {};
    sorted.forEach(item => {
      if (!totalsBySymbol[item.symbol]) totalsBySymbol[item.symbol] = 0;
      totalsBySymbol[item.symbol] += item.qty || 1;
    });

    sorted.forEach((item) => {
      const card = document.createElement('div');
      card.className = 'wl-card';
      card.dataset.symbol = item.symbol;

      const info = getStockInfo(item.symbol);
      if (info && item._ltp === undefined) {
        item._ltp = info.ltp;
        item._ycp = info.ycp;
      } else if (info) {
        item._ycp = info.ycp;
      }

      const cat = getCategory(item.symbol);
      const cbul = getCbul(item.symbol);
      const companyName = getStockName(item.symbol);
      const maturityInfo = item.buyDate ? isMature(item.buyDate, cat) : null;

      const netPnl = calcNetPnl(item);
      const pct = item._ltp !== undefined && item.buyPrice > 0
        ? ((item._ltp - item.buyPrice) / item.buyPrice) * 100
        : null;
      const isProfit = netPnl !== null ? netPnl >= 0 : null;
      const colorClass = netPnl !== null ? (isProfit ? 'profit-text' : 'loss-text') : '';

      const pnlDisplay = netPnl !== null
        ? (isProfit ? '+' : '') + formatBDT(Math.abs(netPnl))
        : '--';
      const pctDisplay = pct !== null
        ? (isProfit ? '+' : '') + Math.abs(pct).toFixed(2) + '%'
        : '--';
      const ltpDisplay = item._ltp !== undefined ? '\u09F3' + formatBDT(item._ltp) : 'Awaiting data...';

      let arrow = '';
      if (item._ltp !== undefined && item._ycp !== undefined) {
        if (item._ltp > item._ycp) arrow = '<span class="wl-direction up">\u2191</span>';
        else if (item._ltp < item._ycp) arrow = '<span class="wl-direction down">\u2193</span>';
        else arrow = '<span class="wl-direction flat">\u2192</span>';
      } else if (item._ltp !== undefined) {
        arrow = '<span class="wl-direction flat">\u2192</span>';
      }

      let ycpLine = '';
      if (item._ycp !== undefined) {
        const dayPct = item._ltp !== undefined && item._ycp > 0 ? ((item._ltp - item._ycp) / item._ycp) * 100 : null;
        const dayColor = dayPct !== null ? (dayPct >= 0 ? 'profit-text' : 'loss-text') : '';
        const dayStr = dayPct !== null ? (dayPct >= 0 ? '+' : '') + dayPct.toFixed(2) + '%' : '';
        ycpLine = 'YCP: \u09F3' + formatBDT(item._ycp) + (dayStr ? ' <span class="' + dayColor + '">(' + dayStr + ')</span>' : '');
      }

      let categoryBadge = '';
      if (cat) {
        const catColors = { 'A': '#16A34A', 'B': '#2563EB', 'Z': '#DC2626', 'N': '#8B5CF6' };
        const catColor = catColors[cat] || '#64748B';
        categoryBadge = `<span class="cat-badge" style="background:${catColor}20;color:${catColor};border-color:${catColor}">${cat}</span>`;
      }

      let cbulHtml = '';
      if (cbul && cbul.lower && cbul.upper) {
        cbulHtml = `<span class="cb-range">CB: \u09F3${formatBDT(cbul.lower)} - \u09F3${formatBDT(cbul.upper)}</span>`;
      }

      let maturityBadge = '';
      if (maturityInfo === true) {
        maturityBadge = '<span class="maturity-badge matured">\u2705 Matured</span>';
      } else if (maturityInfo === false) {
        const sd = calcSettlementDate(item.buyDate, cat);
        if (sd) {
          const ds = sd.toLocaleDateString('en-BD', { month: 'short', day: 'numeric' });
          maturityBadge = '<span class="maturity-badge pending">Settlement: ' + ds + '</span>';
        }
      }

      const totalQty = totalsBySymbol[item.symbol] || 0;
      const totalSharesLine = totalQty > (item.qty || 1)
        ? '<div class="wl-total-shares">Total: ' + totalQty + ' shares</div>'
        : '';

      card.innerHTML = `
        <div class="wl-card-top">
          <div class="wl-symbol-row">
            <span class="wl-symbol">${item.symbol}</span>
            ${categoryBadge}
            ${maturityBadge}
          </div>
          <div class="wl-card-top-right">
            <span class="wl-pnl ${colorClass}">${pnlDisplay}</span>
            ${arrow}
            <button class="wl-remove">&#x2715;</button>
          </div>
        </div>
        ${companyName ? '<div class="wl-company">' + companyName + '</div>' : ''}
        <div class="wl-card-mid">
          <span class="wl-ltp">LTP: ${ltpDisplay}</span>
          <span class="wl-percent ${colorClass}">${pctDisplay}</span>
        </div>
        <div class="wl-card-bot">
          <span>Buy: ${formatBDT(item.buyPrice)}</span>
          <span>&times; ${item.qty || 1}</span>
          <span>= ${formatBDT(item.buyPrice * (item.qty || 1))}</span>
        </div>
        ${totalSharesLine}
        <div class="wl-card-bot-single">
          ${ycpLine}
          ${cbulHtml ? '<br>' + cbulHtml : ''}
        </div>
        <div class="wl-card-actions">
          <button class="card-action-btn sell-btn">Sell</button>
        </div>`;

      card.addEventListener('click', (e) => {
        if (e.target.closest('.wl-remove') || e.target.closest('.card-action-btn')) return;
        clickWatchlistItem(item.symbol);
      });

      const removeBtn = card.querySelector('.wl-remove');
      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        promptRemove(item);
      });

      const sellBtn = card.querySelector('.sell-btn');
      sellBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        openSellDialog(item);
      });

      dom.holdingsContainer.appendChild(card);
    });
  }

  function renderTradeHistory() {
    dom.tradeHistoryContainer.innerHTML = '';

    if (!tradeHistory.length) {
      dom.tradeHistoryContainer.innerHTML = `
        <div class="wl-empty">
          <div class="wl-empty-title">No trade history yet</div>
          <div class="wl-empty-sub">Sold stocks will appear here</div>
        </div>`;
      return;
    }

    let totalRealized = 0;
    const sortedTrades = [...tradeHistory].reverse();

    sortedTrades.forEach((trade, idx) => {
      totalRealized += trade.realizedPnl || 0;
      const isProfit = (trade.realizedPnl || 0) >= 0;
      const card = document.createElement('div');
      card.className = 'trade-card';
      card.innerHTML = `
        <div class="trade-card-top">
          <span class="trade-symbol">${trade.symbol}</span>
          <span class="trade-pnl ${isProfit ? 'profit-text' : 'loss-text'}">${isProfit ? '+' : '-'}\u09F3${formatBDT(Math.abs(trade.realizedPnl || 0))}</span>
        </div>
        <div class="trade-card-mid">
          <span>Sold: ${trade.sellDate || new Date(trade.timestamp).toLocaleDateString()}</span>
        </div>
        <div class="trade-card-bot">
          <span>Buy: \u09F3${formatBDT(trade.buyPrice)}</span>
          <span>Sell: \u09F3${formatBDT(trade.sellPrice)}</span>
          <span>Qty: ${trade.qty}</span>
        </div>
        <div class="trade-card-comm">
          <span>Buy Comm: \u09F3${formatBDT(trade.buyCommission || 0)}</span>
          <span>Sell Comm: \u09F3${formatBDT(trade.sellCommission || 0)}</span>
        </div>
        <button class="trade-delete" data-idx="${idx}">&#x2715; Delete</button>`;

      const delBtn = card.querySelector('.trade-delete');
      delBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        if (confirm('Delete this trade record?')) {
          tradeHistory.splice(tradeHistory.length - 1 - idx, 1);
          saveTradeHistory();
          renderTradeHistory();
          renderSummary();
          updateBadge();
        }
      });

      dom.tradeHistoryContainer.appendChild(card);
    });

    const totalIsProfit = totalRealized >= 0;
    const footer = document.createElement('div');
    footer.className = 'trade-total ' + (totalIsProfit ? 'profit-text' : 'loss-text');
    footer.textContent = 'Total Realized P/L: ' + (totalIsProfit ? '+' : '-') + '\u09F3' + formatBDT(Math.abs(totalRealized));
    dom.tradeHistoryContainer.appendChild(footer);
  }

  function renderTop20() {
    if (!top20Data.length) {
      dom.top20Container.innerHTML = `
        <div class="wl-empty">
          <div class="wl-empty-title">Top 20</div>
          <div class="wl-empty-sub">Refresh to load data</div>
        </div>`;
      return;
    }

    dom.top20Container.innerHTML = '<div class="top20-table">' +
      top20Data.map((item, idx) => {
        const changeClass = item.pctChange >= 0 ? 'profit-text' : 'loss-text';
        const vol = item.volume || 0;
        const volStr = vol >= 1000000 ? (vol / 1000000).toFixed(2) + 'M' : vol >= 1000 ? (vol / 1000).toFixed(1) + 'K' : vol.toString();
        return `<div class="top20-row">
          <span class="top20-rank">${idx + 1}</span>
          <span class="top20-symbol">${item.symbol}</span>
          <span class="top20-ltp">\u09F3${formatBDT(item.ltp)}</span>
          <span class="top20-change ${changeClass}">${item.pctChange >= 0 ? '+' : ''}${item.pctChange.toFixed(2)}%</span>
          <span class="top20-vol">${volStr}</span>
        </div>`;
      }).join('') + '</div>';
  }

  async function refreshAllData() {
    dom.refreshBtn.classList.add('spinning');
    updateStatus('Refreshing data...');

    try {
      const [byLtpHtml, htmlScroll, homepage, textQuotes, cbulHtml, catHtml, top20Html] = await Promise.all([
        fetchWithRetry(BY_LTP_URL, BY_LTP_URL_HTTP, BY_LTP_PROXY_URL).catch(() => null),
        fetchWithRetry(FULL_QUOTES_URL, FULL_QUOTES_URL_HTTP, FULL_PROXY_URL).catch(() => null),
        fetchWithRetry(MARKET_STATUS_URL, MARKET_STATUS_URL_HTTP, MARKET_PROXY_URL).catch(() => null),
        fetchWithRetry(QUOTES_URL, QUOTES_URL_HTTP, PROXY_URL).catch(() => null),
        fetchWithRetry(CBUL_URL, CBUL_URL_HTTP, CBUL_PROXY_URL).catch(() => null),
        fetchWithRetry(CAT_URL, CAT_URL_HTTP, CAT_PROXY_URL).catch(() => null),
        fetchWithRetry(TOP20_URL, TOP20_URL_HTTP, TOP20_PROXY_URL).catch(() => null)
      ]);

      stockData = {};

      const fullHtml = byLtpHtml || htmlScroll;
      if (fullHtml) {
        const fullData = parseFullQuotes(fullHtml);
        Object.assign(stockData, fullData);
      }

      if (textQuotes) extractTimestamp(textQuotes);

      if (textQuotes) {
        const basicData = parseAllQuotesMap(textQuotes);
        for (const [sym, ltp] of Object.entries(basicData)) {
          if (stockData[sym]) {
            if (!stockData[sym].ltp || stockData[sym].ltp === 0) stockData[sym].ltp = ltp;
          } else {
            stockData[sym] = { symbol: sym, ltp, high: 0, low: 0, closep: 0, ycp: 0, change: 0, pctChange: 0 };
          }
        }
      }

      if (cbulHtml) cbulData = parseCbulHtml(cbulHtml);
      if (catHtml) categoryData = parseCategoryHtml(catHtml);
      if (top20Html) top20Data = parseTop20Html(top20Html);

      autoCompleteCache = Object.keys(stockData).sort();

      if (homepage) {
        const parsed = parseMarketStatus(homepage);
        if (parsed) marketStatusFromDSE = parsed;
      }
      updateMarketStatus();

      const activeView = document.querySelector('.tab.active').dataset.view;

      if (watchlist.length) {
        let anyUpdated = false;
        watchlist.forEach(item => {
          const info = stockData[item.symbol];
          if (info && info.ltp > 0) {
            if (item._ltp !== undefined) item._prevLtp = item._ltp;
            item._ltp = info.ltp;
            item._ycp = info.ycp;
            item._timestamp = '';
            item._high = info.high;
            item._low = info.low;
            item._closep = info.closep;
            if (item._prevLtp !== undefined) {
              if (item._ltp > item._prevLtp) item._direction = 'up';
              else if (item._ltp < item._prevLtp) item._direction = 'down';
              else item._direction = 'flat';
            }
            anyUpdated = true;
          }
        });
        if (anyUpdated) saveWatchlist();
        updateBadge();
        if (activeView === 'portfolio' || activeView === 'holdings') {
          renderSummary();
          renderHoldings();
        }
      }

      if (quickWatch.length) {
        quickWatch.forEach(item => {
          const info = stockData[item.symbol];
          if (info && info.ltp > 0) {
            if (item._ltp !== undefined) item._prevLtp = item._ltp;
            item._ltp = info.ltp;
            item._ycp = info.ycp;
            item._high = info.high;
            item._low = info.low;
            item._closep = info.closep;
            if (item._prevLtp !== undefined) {
              if (item._ltp > item._prevLtp) item._direction = 'up';
              else if (item._ltp < item._prevLtp) item._direction = 'down';
              else item._direction = 'flat';
            }
          }
        });
        saveQuickWatch();
        if (activeView === 'watchlist') renderQuickWatch();
      }

      if (activeView === 'top20') renderTop20();

      if (hasResult && stockData[lastSearchSymbol]) {
        const info = stockData[lastSearchSymbol];
        if (info && info.ltp > 0) {
          fetchAndRender(lastSearchSymbol, true);
        }
      }

      const timeStr = new Date().toLocaleTimeString();
      const dseTime = dseDataTimestamp ? dseDataTimestamp.split(' ')[1] : '';
      updateStatus(timeStr + (dseTime ? ' \u00B7 DSE: ' + dseTime : ''));
    } catch (err) {
      updateStatus('Update failed');
    } finally {
      dom.refreshBtn.classList.remove('spinning');
    }
  }

  function parseCbulHtml(html) {
    const data = {};
    try {
      const doc = new DOMParser().parseFromString(html, 'text/html');
      const rows = doc.querySelectorAll('table tbody tr');
      rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length >= 7) {
          const symbol = cells[1].textContent.trim().toUpperCase();
          if (!symbol) return;
          data[symbol] = {
            symbol,
            breakerPct: parseFloat(cells[2].textContent.replace(/,/g, '')) || 0,
            tickSize: parseFloat(cells[3].textContent.replace(/,/g, '')) || 0,
            openAdjPrice: parseFloat(cells[4].textContent.replace(/,/g, '')) || 0,
            lower: parseFloat(cells[5].textContent.replace(/,/g, '')) || 0,
            upper: parseFloat(cells[6].textContent.replace(/,/g, '')) || 0
          };
        }
      });
    } catch (e) {}
    return data;
  }

  function parseCategoryHtml(html) {
    const data = {};
    try {
      const doc = new DOMParser().parseFromString(html, 'text/html');
      const rows = doc.querySelectorAll('table tbody tr');
      rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length >= 3) {
          const symbol = cells[1].textContent.trim().toUpperCase();
          if (!symbol) return;
          data[symbol] = cells[2] ? cells[2].textContent.trim().toUpperCase() : '';
        }
      });
    } catch (e) {}
    return data;
  }

  function parseTop20Html(html) {
    const data = [];
    try {
      const doc = new DOMParser().parseFromString(html, 'text/html');
      const rows = doc.querySelectorAll('table tbody tr');
      rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length >= 6) {
          const symbol = cells[1].textContent.trim().toUpperCase();
          if (!symbol) return;
          data.push({
            symbol,
            ltp: parseFloat(cells[2].textContent.replace(/,/g, '')) || 0,
            pctChange: parseFloat(cells[4].textContent.replace(/,/g, '').replace('%', '')) || 0,
            volume: parseInt(cells[5].textContent.replace(/,/g, '')) || 0
          });
        }
      });
    } catch (e) {}
    return data;
  }

  function onCheckPrice() {
    const symbol = dom.symbol.value.trim().toUpperCase();
    if (!symbol) { showError('Please enter a stock symbol'); dom.symbol.focus(); return; }

    stopAutoRefresh();
    fetchAndRender(symbol, false);
  }

  async function fetchAndRender(symbol, silent) {
    if (!silent) {
      setLoading(true);
    }

    try {
      if (!stockData[symbol]) {
        const text = await fetchWithRetry(QUOTES_URL, QUOTES_URL_HTTP, PROXY_URL);
        const parsed = parseAllQuotesMap(text);
        const ltp = parsed[symbol];
        if (ltp === undefined) {
          if (!silent) { setLoading(false); showError('Stock "' + symbol + '" not found in DSE data'); }
          return;
        }
        if (!stockData[symbol]) stockData[symbol] = { symbol, ltp, high: 0, low: 0, closep: 0, ycp: 0, change: 0, pctChange: 0 };
        if (!autoCompleteCache.length) autoCompleteCache = Object.keys(parsed).sort();
      }

      const info = stockData[symbol];
      if (!info || !info.ltp) {
        if (!silent) { setLoading(false); showError('Stock "' + symbol + '" not found'); }
        return;
      }

      lastSearchSymbol = symbol;
      hasResult = true;

      const cat = getCategory(symbol);
      const cbul = getCbul(symbol);
      const calcBreaker = getBreakerPctForPrice(info.ltp);
      const calcUpper = info.ltp * (1 + calcBreaker / 100);
      const calcLower = info.ltp * (1 - calcBreaker / 100);
      renderResult({ symbol, ltp: info.ltp, ycp: info.ycp, high: info.high, low: info.low, closep: info.closep, pctChange: info.pctChange, category: cat, cbul, calcBreaker, calcUpper, calcLower });
      updateStatus(info.timestamp || 'just now');
      updateBadge();

      if (!silent) {
        setLoading(false);
        startAutoRefresh();
      }
    } catch (err) {
      if (!silent) { setLoading(false); showError('Network error. Check your connection.'); }
      else { updateStatus('Update failed'); }
    }
  }

  async function fetchWithRetry(httpsUrl, httpUrl, proxyUrl) {
    const sources = [httpsUrl, httpUrl, proxyUrl];
    for (const url of sources) {
      if (!url) continue;
      for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
        try {
          const res = await fetch(url, { cache: 'no-cache' });
          if (!res.ok) throw new Error('HTTP ' + res.status);
          return await res.text();
        } catch (e) {
          if (attempt < MAX_RETRIES) await sleep(RETRY_DELAY);
          else break;
        }
      }
    }
    throw new Error('All sources failed');
  }

  function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

  function parseFullQuotes(html) {
    const data = {};
    try {
      const doc = new DOMParser().parseFromString(html, 'text/html');
      const rows = doc.querySelectorAll('table tbody tr');
      rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length >= 11) {
          const symbol = cells[1].textContent.trim().toUpperCase();
          if (!symbol) return;
          const ltp = parseFloat(cells[2].textContent.replace(/,/g, '')) || 0;
          const ycp = parseFloat(cells[6].textContent.replace(/,/g, '')) || 0;
          const pctChange = ycp > 0 ? ((ltp - ycp) / ycp) * 100 : 0;
          data[symbol] = {
            symbol,
            ltp,
            high: parseFloat(cells[3].textContent.replace(/,/g, '')) || 0,
            low: parseFloat(cells[4].textContent.replace(/,/g, '')) || 0,
            closep: parseFloat(cells[5].textContent.replace(/,/g, '')) || 0,
            ycp,
            change: parseFloat(cells[7].textContent.replace(/,/g, '')) || 0,
            pctChange
          };
        }
      });
    } catch (e) {}
    return data;
  }

  function parseAllQuotesMap(text) {
    const lines = text.split('\n');
    const map = {};
    if (lines.length < 4) return map;
    for (let i = 4; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      const parts = line.split(/\s+/);
      if (parts.length >= 2) {
        const symbol = parts[0].toUpperCase();
        const ltp = parseFloat(parts[1]);
        if (!isNaN(ltp)) map[symbol] = ltp;
      }
    }
    return map;
  }

  function extractTimestamp(text) {
    const m = text.match(/Date:\s*(\d{2}-\d{2}-\d{4})\s+Time:\s*(\d{2}:\d{2}:\d{2})/);
    if (m) {
      dseDataTimestamp = m[1] + ' ' + m[2];
      const [dd, mm, yyyy] = m[1].split('-');
      const [hh, min, ss] = m[2].split(':');
      dseDataDate = new Date(+yyyy, +mm - 1, +dd, +hh, +min, +ss);
    }
  }

  function parseMarketStatus(html) {
    const m = html.match(/Market Status:\s*<[^>]*>\s*<b>\s*(Open|Closed)\s*<\/b>/i);
    return m ? m[1] : null;
  }

  function parseQuotes(text, searchSymbol) {
    const lines = text.split('\n');
    if (lines.length < 4) return null;
    let timestamp = '';
    const m = lines[0].match(/Date:\s*(\d{2}-\d{2}-\d{4})\s+Time:\s*(\d{2}:\d{2}:\d{2})/);
    if (m) timestamp = m[1] + ' ' + m[2];
    const target = searchSymbol.toUpperCase();
    for (let i = 4; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      const parts = line.split(/\s+/);
      if (parts.length >= 2) {
        const symbol = parts[0].toUpperCase();
        const ltp = parseFloat(parts[1]);
        if (symbol === target && !isNaN(ltp)) return { ltp, timestamp };
      }
    }
    return null;
  }

  function parseAllQuotes(text) {
    const lines = text.split('\n');
    const results = [];
    if (lines.length < 4) return results;
    let timestamp = '';
    const m = lines[0].match(/Date:\s*(\d{2}-\d{2}-\d{4})\s+Time:\s*(\d{2}:\d{2}:\d{2})/);
    if (m) timestamp = m[1] + ' ' + m[2];
    for (let i = 4; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      const parts = line.split(/\s+/);
      if (parts.length >= 2) {
        const symbol = parts[0].toUpperCase();
        const ltp = parseFloat(parts[1]);
        if (!isNaN(ltp)) results.push({ symbol, ltp, timestamp });
      }
    }
    return results;
  }

  function buildAutocompleteCache(text) {
    const lines = text.split('\n');
    autoCompleteCache = [];
    for (let i = 4; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      const parts = line.split(/\s+/);
      if (parts.length >= 2) {
        autoCompleteCache.push(parts[0].toUpperCase());
      }
    }
  }

  let acIndex = -1;

  function handleAutocomplete(query) {
    if (!query || query.length < 1) { hideAutocomplete(); return; }

    const sourceList = autoCompleteCache.length ? autoCompleteCache : Object.keys(stockData);
    const startsWith = [];
    const includes = [];

    const ucQuery = query.toUpperCase();
    sourceList.forEach(s => {
      const name = getStockName(s).toUpperCase();
      if (s.startsWith(ucQuery)) startsWith.push({ symbol: s, name: getStockName(s), priority: 0 });
      else if (name && name.startsWith(ucQuery)) startsWith.push({ symbol: s, name: getStockName(s), priority: 1 });
      else if (s.includes(ucQuery)) includes.push({ symbol: s, name: getStockName(s), priority: 2 });
      else if (name && name.includes(ucQuery)) includes.push({ symbol: s, name: getStockName(s), priority: 2 });
    });

    startsWith.sort((a, b) => a.priority - b.priority || a.symbol.localeCompare(b.symbol));
    includes.sort((a, b) => a.symbol.localeCompare(b.symbol));

    const matches = [...startsWith, ...includes].slice(0, 8);

    if (!matches.length) { hideAutocomplete(); return; }

    acIndex = -1;
    dom.autocomplete.innerHTML = '';

    const inputRect = acActiveInput === 'watchlist' ? dom.qwSymbol.getBoundingClientRect() : dom.symbol.getBoundingClientRect();
    dom.autocomplete.style.top = (inputRect.bottom - document.querySelector('#app').getBoundingClientRect().top) + 'px';
    dom.autocomplete.style.left = (inputRect.left - document.querySelector('#app').getBoundingClientRect().left) + 'px';
    dom.autocomplete.style.width = inputRect.width + 'px';

    matches.forEach((match, idx) => {
      const div = document.createElement('div');
      div.className = 'ac-item' + (idx === acIndex ? ' selected' : '');
      div.innerHTML = '<strong>' + match.symbol + '</strong>' + (match.name ? ' <span class="ac-sub">' + match.name + '</span>' : '');
      div.addEventListener('mousedown', () => {
        if (acActiveInput === 'watchlist') {
          dom.qwSymbol.value = match.symbol;
        } else {
          dom.symbol.value = match.symbol;
        }
        hideAutocomplete();
        if (acActiveInput !== 'watchlist') dom.checkBtn.focus();
      });
      div.addEventListener('mouseenter', () => {
        acIndex = idx;
        highlightAcItem();
      });
      dom.autocomplete.appendChild(div);
    });
    dom.autocomplete.classList.remove('hidden');
  }

  function moveAutocomplete(dir) {
    const items = dom.autocomplete.querySelectorAll('.ac-item');
    if (!items.length) return;
    acIndex = Math.max(-1, Math.min(items.length - 1, acIndex + dir));
    highlightAcItem();
    if (acIndex >= 0 && items[acIndex]) {
      const symbolSpan = items[acIndex].querySelector('strong');
      const val = symbolSpan ? symbolSpan.textContent : items[acIndex].textContent.trim().split(' ')[0];
      if (acActiveInput === 'watchlist') {
        dom.qwSymbol.value = val;
      } else {
        dom.symbol.value = val;
      }
    }
  }

  function highlightAcItem() {
    dom.autocomplete.querySelectorAll('.ac-item').forEach((el, i) => {
      el.classList.toggle('selected', i === acIndex);
    });
  }

  function hideAutocomplete() {
    dom.autocomplete.classList.add('hidden');
    acIndex = -1;
  }

  function renderResult(data) {
    dom.resultSymbol.textContent = data.symbol;
    const name = getStockName(data.symbol);
    dom.resultCompany.textContent = name || '';
    dom.resultCompany.classList.toggle('hidden', !name);

    dom.resultLtp.innerHTML = '\u09F3' + formatBDT(data.ltp) + ' <span class="currency">BDT</span>';

    const pctChange = data.pctChange || 0;
    const isProfitPct = pctChange >= 0;
    dom.resultPercent.textContent = (isProfitPct ? '+' : '') + Math.abs(pctChange).toFixed(2) + '%';
    dom.resultPercent.className = 'result-value profit-loss ' + (isProfitPct ? 'profit-text' : 'loss-text');

    if (dom.resultYcp) {
      dom.resultYcp.innerHTML = '\u09F3' + formatBDT(data.ycp || 0) + ' <span class="currency">BDT</span>';
    }
    if (dom.resultClosep) {
      dom.resultClosep.innerHTML = '\u09F3' + formatBDT(data.closep || 0) + ' <span class="currency">BDT</span>';
    }
    if (dom.resultHigh) {
      dom.resultHigh.innerHTML = '\u09F3' + formatBDT(data.high || 0) + ' <span class="currency">BDT</span>';
      dom.resultHigh.className = 'result-value profit-text';
    }
    if (dom.resultLow) {
      dom.resultLow.innerHTML = '\u09F3' + formatBDT(data.low || 0) + ' <span class="currency">BDT</span>';
      dom.resultLow.className = 'result-value loss-text';
    }

    if (dom.resultCategory) {
      const cat = data.category || getCategory(data.symbol);
      dom.resultCategory.textContent = cat || 'N/A';
    }

    if (data.cbul && dom.resultCbulSection) {
      dom.resultBreaker.textContent = data.cbul.breakerPct !== undefined ? data.cbul.breakerPct + '%' : '--';
      dom.resultTickSize.textContent = data.cbul.tickSize !== undefined ? '\u09F3' + formatBDT(data.cbul.tickSize) : '--';
      dom.resultOpenAdj.textContent = data.cbul.openAdjPrice !== undefined ? '\u09F3' + formatBDT(data.cbul.openAdjPrice) : '--';
      dom.resultLowerLimit.textContent = data.cbul.lower !== undefined ? '\u09F3' + formatBDT(data.cbul.lower) : '--';
      dom.resultUpperLimit.textContent = data.cbul.upper !== undefined ? '\u09F3' + formatBDT(data.cbul.upper) : '--';
      dom.resultCbulSection.classList.remove('hidden');
    } else if (dom.resultCbulSection) {
      dom.resultCbulSection.classList.add('hidden');
    }

    if (dom.resultNextSection && data.calcUpper) {
      dom.resultNextBreaker.textContent = data.calcBreaker !== undefined ? data.calcBreaker + '%' : '--';
      dom.resultNextUpper.textContent = data.calcUpper !== undefined ? '\u09F3' + formatBDT(data.calcUpper) : '--';
      dom.resultNextLower.textContent = data.calcLower !== undefined ? '\u09F3' + formatBDT(data.calcLower) : '--';
      dom.resultNextSection.classList.remove('hidden');
    } else if (dom.resultNextSection) {
      dom.resultNextSection.classList.add('hidden');
    }

    dom.resultCard.classList.remove('hidden');
    dom.errorMsg.classList.add('hidden');
    dom.searchEmpty.classList.add('hidden');

    dom.resultCard.style.animation = 'none';
    requestAnimationFrame(() => { dom.resultCard.style.animation = 'fadeInUp 0.3s ease'; });
  }

  function formatBDT(val) {
    const fixed = Math.abs(val).toFixed(2);
    const parts = fixed.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
  }

  function setLoading(isLoading) {
    if (isLoading) {
      dom.loadingContainer.classList.remove('hidden');
      dom.resultCard.classList.add('hidden');
      dom.errorMsg.classList.add('hidden');
      dom.searchEmpty.classList.add('hidden');
      dom.checkBtn.disabled = true;
      dom.checkBtn.textContent = 'Fetching...';
      dom.refreshBtn.classList.add('spinning');
    } else {
      dom.loadingContainer.classList.add('hidden');
      dom.checkBtn.disabled = false;
      dom.checkBtn.textContent = 'Look Up';
      dom.refreshBtn.classList.remove('spinning');
    }
  }

  function showError(msg) {
    dom.errorMsg.textContent = msg;
    dom.errorMsg.classList.remove('hidden');
    dom.resultCard.classList.add('hidden');
    dom.loadingContainer.classList.add('hidden');
    dom.searchEmpty.classList.add('hidden');
    dom.checkBtn.disabled = false;
    dom.checkBtn.textContent = 'Look Up';
  }

  function updateStatus(msg) {
    dom.statusText.textContent = 'Last updated: ' + msg;
  }

  function updateBadge() {
    try {
      let totalPnl = 0;
      let hasData = false;
      watchlist.forEach(item => {
        if (item._ltp !== undefined) {
          totalPnl += (item._ltp - item.buyPrice) * (item.qty || 1);
          hasData = true;
        }
      });

      if (!hasData) {
        chrome.runtime.sendMessage({ type: 'UPDATE_BADGE', text: '', profit: true });
        return;
      }

      const isProfit = totalPnl >= 0;
      const absPnl = Math.abs(totalPnl);
      let badgeText;
      if (absPnl >= 10000) {
        badgeText = (isProfit ? '+' : '-') + Math.round(absPnl / 1000) + 'k';
      } else if (absPnl >= 1000) {
        badgeText = (isProfit ? '+' : '-') + Math.round(absPnl / 1000) + 'k';
      } else {
        badgeText = (isProfit ? '+' : '-') + absPnl.toFixed(0);
      }
      badgeText = badgeText.slice(0, 4);

      chrome.runtime.sendMessage({
        type: 'UPDATE_BADGE',
        text: badgeText,
        profit: isProfit
      });
    } catch (e) {}
  }

  function updateBadges() {
    const hCount = watchlist.length;
    const wCount = quickWatch.length;
    dom.holdingsBadge.textContent = hCount ? '(' + hCount + ')' : '';
    dom.holdingsBadge.classList.toggle('hidden', !hCount);
    dom.watchlistBadge.textContent = wCount ? '(' + wCount + ')' : '';
    dom.watchlistBadge.classList.toggle('hidden', !wCount);
  }

  function updateMarketStatus() {
    if (marketStatusFromDSE) {
      const isOpen = marketStatusFromDSE === 'Open';
      dom.marketStatus.innerHTML = `
        <span class="status-dot ${isOpen ? 'open' : 'closed'}"></span>
        ${isOpen ? 'Live' : 'Closed'}`;
      return;
    }

    if (dseDataDate) {
      const now = new Date();
      const diffMin = (now - dseDataDate) / 60000;
      const isLive = diffMin >= 0 && diffMin < 30;
      if (isLive) {
        dom.marketStatus.innerHTML = `
          <span class="status-dot open"></span>
          Live`;
        return;
      }
    }

    const now = new Date();
    const day = now.getDay();
    const totalMins = now.getHours() * 60 + now.getMinutes();
    let isOpen = false;
    if (day >= 0 && day <= 4) {
      isOpen = totalMins >= 600 && totalMins < 870;
    }
    dom.marketStatus.innerHTML = `
      <span class="status-dot ${isOpen ? 'open' : 'closed'}"></span>
      ${isOpen ? 'Live' : 'Closed'}`;
  }

  function toggleDark() {
    isDark = !isDark;
    document.body.classList.toggle('dark', isDark);
    try { localStorage.setItem(DARK_KEY, isDark ? '1' : ''); } catch (e) {}
    renderPieChart();
  }

  function loadTheme() {
    try { isDark = localStorage.getItem(DARK_KEY) === '1'; } catch (e) { isDark = false; }
    document.body.classList.toggle('dark', isDark);
  }

  function checkContextSymbol() {
    try {
      chrome.storage.local.get('dse_context_symbol', (data) => {
        const sym = data.dse_context_symbol;
        if (sym) {
          dom.symbol.value = sym;
          dom.checkBtn.focus();
          chrome.storage.local.remove('dse_context_symbol');
        }
      });
    } catch (e) {}
  }

  function startAutoRefresh() {
    stopAutoRefresh();
    refreshInterval = setInterval(() => {
      const activeView = document.querySelector('.tab.active').dataset.view;
      if (activeView === 'portfolio' || activeView === 'holdings') {
        if (watchlist.length) refreshAllData();
      } else if (activeView === 'watchlist') {
        if (quickWatch.length) refreshAllData();
      } else if (activeView === 'top20') {
        refreshAllData();
      } else if (hasResult) {
        fetchAndRender(lastSearchSymbol, true);
      }
    }, AUTO_REFRESH_MS);
  }

  function stopAutoRefresh() {
    if (refreshInterval) { clearInterval(refreshInterval); refreshInterval = null; }
  }

})();
