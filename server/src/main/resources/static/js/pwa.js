/**
 * EasyNutri — Progressive Web App (PWA) Registration & Sync Monitor
 */

(function () {
  'use strict';

  // Register Service Worker
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker
        .register('/sw.js')
        .then((reg) => {
          console.log('[EasyNutri PWA] Service Worker registered:', reg.scope);
        })
        .catch((err) => {
          console.warn('[EasyNutri PWA] Service Worker registration failed:', err);
        });
    });
  }

  // Network Connectivity Monitoring & Indicator
  function updateOnlineStatus() {
    const isOnline = navigator.onLine;
    const badge = document.getElementById('networkSyncBadge');
    if (badge) {
      if (isOnline) {
        badge.className = 'badge badge-mint';
        badge.innerHTML = '<span style="width:8px;height:8px;border-radius:50%;background:#237A57;display:inline-block;margin-right:4px;"></span> Online';
      } else {
        badge.className = 'badge badge-warning';
        badge.innerHTML = '<span style="width:8px;height:8px;border-radius:50%;background:#E68A00;display:inline-block;margin-right:4px;"></span> Offline Mode';
      }
    }
  }

  window.addEventListener('online', () => {
    updateOnlineStatus();
    if (window.showToast) {
      window.showToast('Reconnected to network. Syncing offline changes...', 'info');
    }
  });

  window.addEventListener('offline', () => {
    updateOnlineStatus();
    if (window.showToast) {
      window.showToast('Network disconnected. Operating in offline PWA mode.', 'warning');
    }
  });

  document.addEventListener('DOMContentLoaded', updateOnlineStatus);
})();

