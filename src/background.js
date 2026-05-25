chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: 'checkDsePrice',
    title: 'Check DSE price for "%s"',
    contexts: ['selection']
  });
});

chrome.contextMenus.onClicked.addListener((info) => {
  if (info.menuItemId === 'checkDsePrice') {
    const symbol = info.selectionText.trim().toUpperCase();
    if (symbol) {
      chrome.storage.local.set({ dse_context_symbol: symbol });
    }
  }
});

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg.type === 'UPDATE_BADGE') {
    const text = msg.text ? msg.text.toString().slice(0, 4) : '';
    chrome.action.setBadgeText({ text });
    chrome.action.setBadgeBackgroundColor({ color: msg.profit !== false ? '#16A34A' : '#DC2626' });
  }

  if (msg.type === 'SHOW_NOTIFICATION') {
    chrome.notifications.create({
      type: 'basic',
      title: msg.title || 'Price Alert',
      message: msg.message || '',
      priority: 2
    });
  }
});
