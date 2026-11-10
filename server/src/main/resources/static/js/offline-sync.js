/**
 * EasyNutri — Offline Synchronization Engine
 * Automatically dispatches queued IndexedDB actions to /api/v1/sync/batch when connectivity returns.
 */

(function (window) {
  'use strict';

  let isSyncing = false;

  function getCsrfToken() {
    const meta = document.querySelector('meta[name="_csrf"]');
    return meta ? meta.getAttribute('content') : '';
  }

  function getCsrfHeader() {
    const meta = document.querySelector('meta[name="_csrf_header"]');
    return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
  }

  function updateSyncBadge(status, message) {
    const badge = document.getElementById('networkSyncBadge');
    if (!badge) return;

    switch (status) {
      case 'SYNCING':
        badge.className = 'badge';
        badge.style.background = '#e0f2fe';
        badge.style.color = '#0369a1';
        badge.innerHTML = '<span style="display:inline-block;animation:spin 1s linear infinite;">🔄</span> Syncing...';
        break;
      case 'PENDING':
        badge.className = 'badge';
        badge.style.background = '#fef3c7';
        badge.style.color = '#92400e';
        badge.innerHTML = '🟠 ' + (message || 'Pending Sync');
        break;
      case 'SUCCESS':
        badge.className = 'badge badge-mint';
        badge.innerHTML = '✓ All Synced';
        setTimeout(() => updateSyncBadge('ONLINE'), 3000);
        break;
      case 'ERROR':
        badge.className = 'badge';
        badge.style.background = '#fee2e2';
        badge.style.color = '#991b1b';
        badge.innerHTML = '⚠️ Sync Attention Needed';
        break;
      case 'OFFLINE':
        badge.className = 'badge badge-warning';
        badge.innerHTML = 'Offline Mode';
        break;
      case 'ONLINE':
      default:
        badge.className = 'badge badge-mint';
        badge.innerHTML = '<span style="width:8px;height:8px;border-radius:50%;background:#237A57;display:inline-block;margin-right:4px;"></span> Online';
        break;
    }
  }

  async function triggerSynchronization() {
    if (!navigator.onLine || isSyncing) return;
    if (!window.EasyNutriOfflineDB) return;

    try {
      const actions = await window.EasyNutriOfflineDB.getPendingActions();
      if (!actions || actions.length === 0) {
        updateSyncBadge('ONLINE');
        return;
      }

      isSyncing = true;
      updateSyncBadge('SYNCING');

      const payload = {
        operations: actions.map((a) => ({
          operationId: a.operationId,
          idempotencyKey: a.idempotencyKey,
          actionType: a.actionType,
          targetProfileId: a.targetProfileId,
          clientVersion: a.clientVersion || 1,
          timestamp: a.timestamp,
          payloadJson: a.payloadJson
        }))
      };

      const headers = {
        'Content-Type': 'application/json'
      };
      const csrfToken = getCsrfToken();
      if (csrfToken) {
        headers[getCsrfHeader()] = csrfToken;
      }

      const response = await fetch('/api/v1/sync/batch', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        throw new Error('Sync endpoint returned HTTP ' + response.status);
      }

      const result = await response.json();

      // Process results and remove processed items
      if (result.results) {
        for (const res of result.results) {
          if (res.status === 'SUCCESS' || res.status === 'DUPLICATE_IGNORED') {
            await window.EasyNutriOfflineDB.removeAction(res.operationId);
          }
        }
      }

      const remaining = await window.EasyNutriOfflineDB.getPendingCount();
      if (remaining === 0) {
        updateSyncBadge('SUCCESS');
        if (window.showToast) {
          window.showToast('Offline changes synchronized with server.', 'success');
        }
      } else {
        updateSyncBadge('PENDING', remaining + ' Queued');
      }
    } catch (err) {
      console.warn('[EasyNutri Sync] Batch sync error:', err);
      updateSyncBadge('ERROR');
    } finally {
      isSyncing = false;
    }
  }

  // Event Listeners
  window.addEventListener('online', () => {
    updateSyncBadge('ONLINE');
    triggerSynchronization();
  });

  window.addEventListener('offline', () => {
    window.EasyNutriOfflineDB.getPendingCount().then((count) => {
      if (count > 0) {
        updateSyncBadge('PENDING', count + ' Offline');
      } else {
        updateSyncBadge('OFFLINE');
      }
    });
  });

  document.addEventListener('DOMContentLoaded', () => {
    if (window.EasyNutriOfflineDB) {
      window.EasyNutriOfflineDB.getPendingCount().then((count) => {
        if (count > 0 && navigator.onLine) {
          triggerSynchronization();
        } else if (count > 0) {
          updateSyncBadge('PENDING', count + ' Queued');
        }
      });
    }
  });

  window.EasyNutriSync = {
    triggerSync: triggerSynchronization
  };
})(window);
