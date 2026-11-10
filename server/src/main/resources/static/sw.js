/**
 * EasyNutri — Service Worker (PWA Offline Support v2)
 * Strictly excludes private authenticated health endpoints and APIs from static caching.
 */

const CACHE_NAME = 'easynutri-cache-v2';
const STATIC_ASSETS = [
  '/',
  '/offline.html',
  '/css/main.css',
  '/js/app.js',
  '/js/pwa.js',
  '/js/offline-db.js',
  '/js/offline-sync.js',
  '/manifest.webmanifest'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS);
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const url = event.request.url;

  // STRICT PRIVACY: Never cache authenticated APIs, PDF reports, or dynamic user sync endpoints
  if (
    url.includes('/api/v1/') ||
    url.includes('/reports/export-pdf') ||
    url.includes('/h2-console') ||
    url.includes('/logout')
  ) {
    return; // Pass through directly to network
  }

  // Navigation requests: Network-first with offline.html fallback
  if (event.request.mode === 'navigate') {
    event.respondWith(
      fetch(event.request).catch(() => {
        return caches.match('/offline.html');
      })
    );
    return;
  }

  // Static Assets: Cache-first with network fallback
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      return (
        cachedResponse ||
        fetch(event.request).then((networkResponse) => {
          if (
            networkResponse &&
            networkResponse.status === 200 &&
            (url.includes('/css/') || url.includes('/js/') || url.includes('/icons/'))
          ) {
            const responseClone = networkResponse.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, responseClone));
          }
          return networkResponse;
        })
      );
    })
  );
});

self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
