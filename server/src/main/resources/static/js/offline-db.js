/**
 * EasyNutri — IndexedDB Offline Storage Layer
 * Manages offline reference data and offline action queues with UUID idempotency keys.
 */

(function (window) {
  'use strict';

  const DB_NAME = 'EasyNutriOfflineDB';
  const DB_VERSION = 1;

  let dbPromise = null;

  function openDatabase() {
    if (dbPromise) return dbPromise;

    dbPromise = new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION);

      request.onupgradeneeded = (event) => {
        const db = event.target.result;

        // Action queue for pending offline writes
        if (!db.objectStoreNames.contains('action_queue')) {
          const actionStore = db.createObjectStore('action_queue', { keyPath: 'operationId' });
          actionStore.createIndex('idempotencyKey', 'idempotencyKey', { unique: true });
          actionStore.createIndex('status', 'status', { unique: false });
        }

        // Public food items for offline search
        if (!db.objectStoreNames.contains('foods')) {
          db.createObjectStore('foods', { keyPath: 'id' });
        }

        // Public recipes for offline viewing
        if (!db.objectStoreNames.contains('recipes')) {
          db.createObjectStore('recipes', { keyPath: 'id' });
        }
      };

      request.onsuccess = (event) => resolve(event.target.result);
      request.onerror = (event) => reject(event.target.error);
    });

    return dbPromise;
  }

  function generateUUID() {
    if (crypto && crypto.randomUUID) {
      return crypto.randomUUID();
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }

  const OfflineDB = {
    /**
     * Enqueues an offline action into IndexedDB.
     */
    async enqueueAction(actionType, targetProfileId, payload) {
      const db = await openDatabase();
      const operationId = generateUUID();
      const idempotencyKey = 'idemp_' + operationId;

      const record = {
        operationId: operationId,
        idempotencyKey: idempotencyKey,
        actionType: actionType,
        targetProfileId: targetProfileId,
        clientVersion: 1,
        timestamp: new Date().toISOString(),
        payloadJson: JSON.stringify(payload),
        status: 'PENDING_SYNC',
        retryCount: 0
      };

      return new Promise((resolve, reject) => {
        const tx = db.transaction('action_queue', 'readwrite');
        const store = tx.objectStore('action_queue');
        const req = store.add(record);
        req.onsuccess = () => resolve(record);
        req.onerror = () => reject(req.error);
      });
    },

    /**
     * Retrieves all pending synchronization actions.
     */
    async getPendingActions() {
      const db = await openDatabase();
      return new Promise((resolve, reject) => {
        const tx = db.transaction('action_queue', 'readonly');
        const store = tx.objectStore('action_queue');
        const req = store.getAll();
        req.onsuccess = () => resolve(req.result || []);
        req.onerror = () => reject(req.error);
      });
    },

    /**
     * Removes an action from the queue after successful sync.
     */
    async removeAction(operationId) {
      const db = await openDatabase();
      return new Promise((resolve, reject) => {
        const tx = db.transaction('action_queue', 'readwrite');
        const store = tx.objectStore('action_queue');
        const req = store.delete(operationId);
        req.onsuccess = () => resolve();
        req.onerror = () => reject(req.error);
      });
    },

    /**
     * Counts pending operations.
     */
    async getPendingCount() {
      const actions = await this.getPendingActions();
      return actions.length;
    }
  };

  window.EasyNutriOfflineDB = OfflineDB;
})(window);
