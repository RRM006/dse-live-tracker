(function () {
  'use strict';

  const QUOTES_URL = 'https://www.dsebd.org/datafile/quotes.txt';
  const PROXY_URL = 'https://corsproxy.io/?' + encodeURIComponent(QUOTES_URL);
  const AUTO_REFRESH_MS = 30000;
  const MAX_RETRIES = 2;
  const RETRY_DELAY = 1500;
  const WL_KEY = 'dse_watchlist';
  const WQ_KEY = 'dse_quickwatch';
  const DARK_KEY = 'dse_dark_mode';
  const SORT_KEY = 'dse_sort';
  const UNDO_MS = 3000;

  const $ = (id) => document.getElementById(id);

  const dom = {};
  function cacheDOM() {
    dom.symbol = $('symbol');
    dom.buyPrice = $('buyPrice');
    dom.quantity = $('quantity');
    dom.checkBtn = $('checkBtn');
    dom.refreshBtn = $('refreshBtn');
    dom.darkToggle = $('darkToggle');
    dom.viewPortfolio = $('viewPortfolio');
    dom.viewHoldings = $('viewHoldings');
    dom.viewWatchlist = $('viewWatchlist');
    dom.viewSearch = $('viewSearch');
    dom.tabs = document.querySelectorAll('.tab');
    dom.wlSummary = $('wlSummary');
    dom.addSymbol = $('addSymbol');
    dom.addBuyPrice = $('addBuyPrice');
    dom.addQty = $('addQty');
    dom.addBtn = $('addBtn');
    dom.addToWatchlistBtn = $('addToWatchlistBtn');
    dom.resultCard = $('resultCard');
    dom.resultSymbol = $('resultSymbol');
    dom.resultQty = $('resultQty');
    dom.resultLtp = $('resultLtp');
    dom.resultBuyPrice = $('resultBuyPrice');
    dom.resultProfit = $('resultProfit');
    dom.resultPercent = $('resultPercent');
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
  }

  let watchlist = [];
  let quickWatch = [];
  let autoCompleteCache = [];
  let refreshInterval = null;
  let lastSymbol = '';
  let lastBuyPrice = 0;
  let lastQty = 1;
  let hasResult = false;
  let isDark = false;
  let sortMode = 'pnl-asc';
  let isEditingWatchlist = null;
  let undoTimeout = null;
  let pendingRemove = null;

  document.addEventListener('DOMContentLoaded', () => {
    cacheDOM();
    loadTheme();
    loadWatchlist();
    loadQuickWatch();
    loadSort();
    bindEvents();
    renderSummary();
    renderHoldings();
    renderQuickWatch();
    updateBadges();
    updateMarketStatus();
    dom.symbol.focus();
    checkContextSymbol();

    const activeView = document.querySelector('.tab.active').dataset.view;
    if (activeView === 'portfolio' || activeView === 'holdings') {
      if (watchlist.length) refreshWatchlistPrices();
    } else if (activeView === 'watchlist' && quickWatch.length) {
      refreshQuickWatchPrices();
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

    dom.refreshBtn.addEventListener('click', () => {
      const activeView = document.querySelector('.tab.active').dataset.view;
      if (activeView === 'portfolio' || activeView === 'holdings') {
        refreshWatchlistPrices();
      } else if (activeView === 'watchlist') {
        refreshQuickWatchPrices();
      } else if (hasResult) {
        fetchAndRender(lastSymbol, lastBuyPrice, lastQty, true);
      }
    });

    dom.symbol.addEventListener('input', () => {
      dom.symbol.value = dom.symbol.value.toUpperCase();
      handleAutocomplete(dom.symbol.value);
    });
    dom.symbol.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.buyPrice.focus();
      if (e.key === 'ArrowDown') moveAutocomplete(1);
      if (e.key === 'ArrowUp') moveAutocomplete(-1);
    });
    dom.symbol.addEventListener('blur', () => setTimeout(hideAutocomplete, 200));

    dom.buyPrice.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.quantity.focus();
    });
    dom.quantity.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.checkBtn.click();
    });

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
      if (e.key === 'Enter') dom.addBtn.click();
    });

    dom.addToWatchlistBtn.addEventListener('click', onAddSearchResultToWatchlist);

    dom.qwSymbol.addEventListener('input', () => {
      dom.qwSymbol.value = dom.qwSymbol.value.toUpperCase();
    });
    dom.qwSymbol.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') dom.qwTarget.focus();
    });
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
  }

  function switchView(view) {
    dom.tabs.forEach(t => t.classList.toggle('active', t.dataset.view === view));
    dom.viewPortfolio.classList.toggle('active', view === 'portfolio');
    dom.viewHoldings.classList.toggle('active', view === 'holdings');
    dom.viewWatchlist.classList.toggle('active', view === 'watchlist');
    dom.viewSearch.classList.toggle('active', view === 'search');

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

  function onAddToWatchlist() {
    const symbol = dom.addSymbol.value.trim().toUpperCase();
    const buyPrice = parseFloat(dom.addBuyPrice.value);
    const qty = parseInt(dom.addQty.value) || 1;

    if (!symbol) return;
    if (isNaN(buyPrice) || buyPrice <= 0) return;

    if (watchlist.find(w => w.symbol === symbol)) return;

    watchlist.push({ symbol, buyPrice, qty });
    saveWatchlist();
    dom.addSymbol.value = '';
    dom.addBuyPrice.value = '';
    dom.addQty.value = '';
    renderSummary();
    renderHoldings();
    updateBadges();
    if (!refreshInterval) refreshWatchlistPrices();
    dom.addSymbol.focus();
  }

  function removeFromWatchlist(symbol) {
    watchlist = watchlist.filter(w => w.symbol !== symbol);
    saveWatchlist();
    renderSummary();
    renderHoldings();
    updateBadges();
  }

  function clickWatchlistItem(symbol) {
    const item = watchlist.find(w => w.symbol === symbol);
    if (!item) return;
    dom.symbol.value = item.symbol;
    dom.buyPrice.value = item.buyPrice;
    dom.quantity.value = item.qty || 1;
    isEditingWatchlist = symbol;
    switchView('search');
    onCheckPrice();
  }

  function onAddSearchResultToWatchlist() {
    if (!hasResult) return;

    if (isEditingWatchlist) {
      const item = watchlist.find(w => w.symbol === isEditingWatchlist);
      if (item) {
        item.buyPrice = lastBuyPrice;
        item.qty = lastQty;
        saveWatchlist();
        renderSummary();
        renderHoldings();
        updateBadges();
        dom.addToWatchlistBtn.textContent = 'Updated!';
        dom.addToWatchlistBtn.disabled = true;
        dom.addToWatchlistBtn.classList.remove('edit-mode');
        isEditingWatchlist = null;
      }
      return;
    }

    const symbol = lastSymbol;
    const buyPrice = lastBuyPrice;
    const qty = lastQty;

    if (watchlist.find(w => w.symbol === symbol)) return;

    watchlist.push({ symbol, buyPrice, qty });
    saveWatchlist();
    renderSummary();
    renderHoldings();
    updateBadges();
    dom.addToWatchlistBtn.textContent = 'Added to Portfolio';
    dom.addToWatchlistBtn.disabled = true;
  }

  function promptRemove(symbol) {
    if (undoTimeout) return;
    const item = watchlist.find(w => w.symbol === symbol);
    if (!item) return;

    pendingRemove = { ...item, _ltp: item._ltp, _prevLtp: item._prevLtp, _direction: item._direction, _timestamp: item._timestamp };

    watchlist = watchlist.filter(w => w.symbol !== symbol);
    saveWatchlist();
    renderSummary();
    renderHoldings();
    updateBadges();

    dom.snackbarText.textContent = symbol + ' removed';
    dom.snackbarUndo.textContent = 'Undo';
    dom.snackbar.classList.remove('hidden');

    undoTimeout = setTimeout(() => {
      pendingRemove = null;
      undoTimeout = null;
      dom.snackbar.classList.add('hidden');
    }, UNDO_MS);
  }

  function cancelRemove() {
    if (!pendingRemove || !undoTimeout) return;
    clearTimeout(undoTimeout);
    undoTimeout = null;

    watchlist.push(pendingRemove);
    pendingRemove = null;
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
    if (!refreshInterval) refreshQuickWatchPrices();
    dom.qwSymbol.focus();
  }

  function removeFromQuickWatch(symbol) {
    quickWatch = quickWatch.filter(w => w.symbol !== symbol);
    saveQuickWatch();
    renderQuickWatch();
    updateBadges();
  }

  function renderQuickWatch() {
    dom.qwContainer.innerHTML = '';

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

    quickWatch.forEach(item => {
      const card = document.createElement('div');
      card.className = 'wl-card';
      card.dataset.symbol = item.symbol;

      const ltpValue = item._ltp;
      const ltpDisplay = ltpValue !== undefined ? formatBDT(ltpValue) + ' BDT' : 'Awaiting data...';

      let arrow = '';
      if (item._direction === 'up') arrow = '<span class="wl-direction up">\u2191</span>';
      else if (item._direction === 'down') arrow = '<span class="wl-direction down">\u2193</span>';
      else if (ltpValue !== undefined) arrow = '<span class="wl-direction flat">\u2192</span>';

      let pctDisplay = '';
      let pctColor = '';
      if (ltpValue !== undefined && item._prevLtp !== undefined && item._prevLtp > 0) {
        const pct = ((ltpValue - item._prevLtp) / item._prevLtp) * 100;
        const isProfit = pct >= 0;
        pctColor = isProfit ? 'profit-text' : 'loss-text';
        pctDisplay = (isProfit ? '+' : '') + pct.toFixed(2) + '%';
      } else if (ltpValue !== undefined) {
        pctDisplay = '--';
      }

      let targetBadge = '';
      if (item.targetPrice && ltpValue !== undefined) {
        const reached = ltpValue >= item.targetPrice;
        targetBadge = '<span class="qw-target' + (reached ? ' reached' : '') + '">'
          + (reached ? '&#10003;' : '&#127919;') + ' ' + formatBDT(item.targetPrice) + '</span>';
        if (reached) card.classList.add('qw-card-target-hit');
      }

      card.innerHTML = `
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
        </div>`;

      const removeBtn = card.querySelector('.wl-remove');
      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        removeFromQuickWatch(item.symbol);
      });

      dom.qwContainer.appendChild(card);
    });
  }

  async function refreshQuickWatchPrices() {
    if (!quickWatch.length) return;
    dom.refreshBtn.classList.add('spinning');
    updateStatus('Updating watchlist...');

    try {
      const text = await fetchWithRetry();
      const parsed = parseAllQuotes(text);

      quickWatch.forEach(item => {
        const found = parsed.find(p => p.symbol === item.symbol);
        if (found) {
          if (item._ltp !== undefined) {
            item._prevLtp = item._ltp;
          }
          item._ltp = found.ltp;

          if (item._prevLtp !== undefined) {
            if (item._ltp > item._prevLtp) item._direction = 'up';
            else if (item._ltp < item._prevLtp) item._direction = 'down';
            else item._direction = 'flat';
          }
        }
      });

      saveQuickWatch();
      updateStatus('Watchlist updated at ' + new Date().toLocaleTimeString());
      renderQuickWatch();
    } catch (err) {
      updateStatus('Update failed');
    } finally {
      dom.refreshBtn.classList.remove('spinning');
    }
  }

  function getSortedWatchlist() {
    const sorted = [...watchlist];
    switch (sortMode) {
      case 'pnl-asc':
        sorted.sort((a, b) => {
          const pnlA = a._ltp !== undefined ? (a._ltp - a.buyPrice) * (a.qty || 1) : -Infinity;
          const pnlB = b._ltp !== undefined ? (b._ltp - b.buyPrice) * (b.qty || 1) : -Infinity;
          return (pnlA - pnlB) || 0;
        });
        break;
      case 'pnl-desc':
        sorted.sort((a, b) => {
          const pnlA = a._ltp !== undefined ? (a._ltp - a.buyPrice) * (a.qty || 1) : -Infinity;
          const pnlB = b._ltp !== undefined ? (b._ltp - b.buyPrice) * (b.qty || 1) : -Infinity;
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

  function renderSummary() {
    if (!watchlist.length) {
      dom.wlSummary.innerHTML = '';
      return;
    }

    let invested = 0, current = 0, countWithData = 0;
    watchlist.forEach(item => {
      const q = item.qty || 1;
      invested += item.buyPrice * q;
      if (item._ltp !== undefined) {
        current += item._ltp * q;
        countWithData++;
      }
    });

    const pnl = current - invested;
    const pct = invested > 0 ? (pnl / invested) * 100 : 0;
    const isProfit = pnl >= 0;
    const sign = isProfit ? '+' : '-';
    const colorClass = isProfit ? 'profit-text' : 'loss-text';

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
        </div>
        <div class="wl-summary-total ${colorClass}">
          <span>Total P/L</span>
          <span>${sign}${formatBDT(Math.abs(pnl))} BDT (${sign}${Math.abs(pct).toFixed(2)}%)</span>
        </div>
      </div>`;
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

    sorted.forEach(item => {
      const card = document.createElement('div');
      card.className = 'wl-card';
      card.dataset.symbol = item.symbol;

      const pnl = item._ltp !== undefined ? (item._ltp - item.buyPrice) * (item.qty || 1) : null;
      const pct = item._ltp !== undefined && item.buyPrice > 0
        ? ((item._ltp - item.buyPrice) / item.buyPrice) * 100
        : null;
      const isProfit = pnl !== null ? pnl >= 0 : null;
      const colorClass = pnl !== null ? (isProfit ? 'profit-text' : 'loss-text') : '';

      const pnlDisplay = pnl !== null
        ? (isProfit ? '+' : '') + formatBDT(Math.abs(pnl))
        : '--';
      const pctDisplay = pct !== null
        ? (isProfit ? '+' : '') + Math.abs(pct).toFixed(2) + '%'
        : '--';
      const ltpDisplay = item._ltp !== undefined ? formatBDT(item._ltp) + ' BDT' : 'Awaiting data...';

      let arrow = '';
      if (item._direction === 'up') arrow = '<span class="wl-direction up">\u2191</span>';
      else if (item._direction === 'down') arrow = '<span class="wl-direction down">\u2193</span>';
      else if (item._ltp !== undefined) arrow = '<span class="wl-direction flat">\u2192</span>';

      card.innerHTML = `
        <div class="wl-card-top">
          <span class="wl-symbol">${item.symbol}</span>
          <div class="wl-card-top-right">
            <span class="wl-pnl ${colorClass}">${pnlDisplay}</span>
            ${arrow}
            <button class="wl-remove" data-symbol="${item.symbol}">&#x2715;</button>
          </div>
        </div>
        <div class="wl-card-mid">
          <span class="wl-ltp">LTP: ${ltpDisplay}</span>
          <span class="wl-percent ${colorClass}">${pctDisplay}</span>
        </div>
        <div class="wl-card-bot">
          <span>Buy: ${formatBDT(item.buyPrice)}</span>
          <span>&times; ${item.qty || 1}</span>
          <span>= ${formatBDT(item.buyPrice * (item.qty || 1))}</span>
        </div>`;

      card.addEventListener('click', (e) => {
        if (e.target.closest('.wl-remove')) return;
        clickWatchlistItem(item.symbol);
      });

      const removeBtn = card.querySelector('.wl-remove');
      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        promptRemove(item.symbol);
      });

      dom.holdingsContainer.appendChild(card);
    });
  }

  async function refreshWatchlistPrices() {
    if (!watchlist.length) return;
    dom.refreshBtn.classList.add('spinning');
    updateStatus('Updating portfolio...');

    try {
      const text = await fetchWithRetry();
      const parsed = parseAllQuotes(text);

      let anyUpdated = false;
      watchlist.forEach(item => {
        const found = parsed.find(p => p.symbol === item.symbol);
        if (found) {
          if (item._ltp !== undefined) {
            item._prevLtp = item._ltp;
          }
          item._ltp = found.ltp;
          item._timestamp = found.timestamp;

          if (item._prevLtp !== undefined) {
            if (item._ltp > item._prevLtp) item._direction = 'up';
            else if (item._ltp < item._prevLtp) item._direction = 'down';
            else item._direction = 'flat';
          }

          anyUpdated = true;
        } else {
          item._ltp = undefined;
          item._direction = undefined;
        }
      });

      saveWatchlist();

      if (anyUpdated) updateStatus('Portfolio updated at ' + new Date().toLocaleTimeString());
      updateBadge();

      renderSummary();
      renderHoldings();
    } catch (err) {
      updateStatus('Update failed');
    } finally {
      dom.refreshBtn.classList.remove('spinning');
    }
  }

  function onCheckPrice() {
    const symbol = dom.symbol.value.trim().toUpperCase();
    const buyPrice = parseFloat(dom.buyPrice.value);
    const qty = parseInt(dom.quantity.value) || 1;

    if (!symbol) { showError('Please enter a stock symbol'); dom.symbol.focus(); return; }
    if (isNaN(buyPrice) || buyPrice <= 0) { showError('Please enter a valid buy price'); dom.buyPrice.focus(); return; }

    stopAutoRefresh();
    fetchAndRender(symbol, buyPrice, qty, false);
  }

  async function fetchAndRender(symbol, buyPrice, qty, silent) {
    if (!silent) {
      setLoading(true);
    }

    try {
      const text = await fetchWithRetry();
      const result = parseQuotes(text, symbol);

      if (!result) {
        if (!silent) { setLoading(false); showError('Stock "' + symbol + '" not found in DSE data'); }
        return;
      }

      const ltp = result.ltp;
      const profitPerShare = ltp - buyPrice;
      const totalProfit = profitPerShare * qty;
      const percent = buyPrice > 0 ? (profitPerShare / buyPrice) * 100 : 0;

      lastSymbol = symbol;
      lastBuyPrice = buyPrice;
      lastQty = qty;
      hasResult = true;

      if (!autoCompleteCache.length) {
        buildAutocompleteCache(text);
      }

      renderResult({ symbol, ltp, buyPrice, qty, profit: totalProfit, percent });
      updateStatus(result.timestamp || 'just now');
      updateBadge();

      if (!silent) {
        setLoading(false);

        const already = watchlist.find(w => w.symbol === symbol);

        if (isEditingWatchlist || already) {
          dom.addToWatchlistBtn.classList.remove('hidden');
          if (isEditingWatchlist) {
            dom.addToWatchlistBtn.classList.add('edit-mode');
            dom.addToWatchlistBtn.textContent = 'Update in Portfolio';
            dom.addToWatchlistBtn.disabled = false;
          } else {
            dom.addToWatchlistBtn.classList.remove('edit-mode');
            dom.addToWatchlistBtn.textContent = 'Already in Portfolio';
            dom.addToWatchlistBtn.disabled = true;
          }
        } else {
          dom.addToWatchlistBtn.classList.add('hidden');
          isEditingWatchlist = null;
        }

        startAutoRefresh();
      }
    } catch (err) {
      if (!silent) { setLoading(false); showError('Network error. Check your connection.'); }
      else { updateStatus('Update failed'); }
    }
  }

  async function fetchWithRetry() {
    const sources = [QUOTES_URL, PROXY_URL];
    for (const url of sources) {
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

    const matches = autoCompleteCache
      .filter(s => s.includes(query))
      .slice(0, 12);

    if (!matches.length) { hideAutocomplete(); return; }

    dom.autocomplete.innerHTML = '';
    matches.forEach((match, idx) => {
      const div = document.createElement('div');
      div.className = 'ac-item' + (idx === acIndex ? ' selected' : '');
      div.textContent = match;
      div.addEventListener('mousedown', () => {
        dom.symbol.value = match;
        hideAutocomplete();
        dom.buyPrice.focus();
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
      dom.symbol.value = items[acIndex].textContent;
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
    dom.resultQty.textContent = '\u00d7 ' + data.qty;

    dom.resultLtp.innerHTML = formatBDT(data.ltp) + ' <span class="currency">BDT</span>';
    dom.resultBuyPrice.innerHTML = formatBDT(data.buyPrice) + ' <span class="currency">BDT</span>';

    const isProfit = data.profit >= 0;
    const sign = isProfit ? '+' : '-';
    const absProfit = Math.abs(data.profit);
    const colorClass = isProfit ? 'profit-text' : 'loss-text';

    dom.resultProfit.textContent = sign + formatBDT(absProfit);
    dom.resultProfit.innerHTML += ' <span class="currency">BDT</span>';
    dom.resultProfit.className = 'result-value profit-loss ' + colorClass;

    dom.resultPercent.textContent = sign + Math.abs(data.percent).toFixed(2) + '%';
    dom.resultPercent.className = 'result-value profit-loss ' + colorClass;

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
      dom.checkBtn.textContent = 'Check Price';
      dom.refreshBtn.classList.remove('spinning');
    }
  }

  function showError(msg) {
    dom.errorMsg.textContent = msg;
    dom.errorMsg.classList.remove('hidden');
    dom.resultCard.classList.add('hidden');
    dom.loadingContainer.classList.add('hidden');
    dom.searchEmpty.classList.add('hidden');
    dom.addToWatchlistBtn.classList.add('hidden');
    dom.checkBtn.disabled = false;
    dom.checkBtn.textContent = 'Check Price';
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
    const now = new Date();
    const day = now.getDay();
    const hours = now.getHours();
    const mins = now.getMinutes();
    const totalMins = hours * 60 + mins;

    let isOpen = false;
    if (day >= 0 && day <= 4) {
      const open = 10 * 60;
      const close = 14 * 60 + 30;
      isOpen = totalMins >= open && totalMins < close;
    }

    dom.marketStatus.innerHTML = `
      <span class="status-dot ${isOpen ? 'open' : 'closed'}"></span>
      ${isOpen ? 'Open' : 'Closed'}`;
  }

  function toggleDark() {
    isDark = !isDark;
    document.body.classList.toggle('dark', isDark);
    try { localStorage.setItem(DARK_KEY, isDark ? '1' : ''); } catch (e) {}
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
          dom.buyPrice.focus();
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
        if (watchlist.length) refreshWatchlistPrices();
      } else if (activeView === 'watchlist') {
        if (quickWatch.length) refreshQuickWatchPrices();
      } else if (hasResult) {
        fetchAndRender(lastSymbol, lastBuyPrice, lastQty, true);
      }
    }, AUTO_REFRESH_MS);
  }

  function stopAutoRefresh() {
    if (refreshInterval) { clearInterval(refreshInterval); refreshInterval = null; }
  }

})();
