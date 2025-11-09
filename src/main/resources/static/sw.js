/**
 * Service Worker for Offline Support and Push Notifications
 */

const CACHE_NAME = 'reliable-carriers-v1';
const urlsToCache = [
    '/',
    '/css/dark-mode.css',
    '/js/password-strength.js',
    '/js/currency-utils.js',
    '/js/live-tracking.js',
    '/images/Reliable 1.png'
];

// Install event - cache resources
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => {
                console.log('Opened cache');
                return cache.addAll(urlsToCache);
            })
    );
});

// Fetch event - serve from cache when offline
self.addEventListener('fetch', (event) => {
    // Skip service worker for API calls - always fetch from network
    if (event.request.url.includes('/api/') || event.request.url.includes('/auth/')) {
        event.respondWith(fetch(event.request));
        return;
    }
    
    // For other requests, try cache first, then network
    event.respondWith(
        caches.match(event.request)
            .then((response) => {
                // Return cached version or fetch from network
                return response || fetch(event.request);
            })
            .catch(() => {
                // If fetch fails, return cached version if available
                return caches.match(event.request);
            })
    );
});

// Push event - handle push notifications
self.addEventListener('push', (event) => {
    const data = event.data ? event.data.json() : {};
    const title = data.title || 'Reliable Carriers';
    const options = {
        body: data.body || 'You have a new update',
        icon: '/images/Reliable 1.png',
        badge: '/images/Reliable 1.png',
        data: data.url || '/',
        tag: data.tag || 'default',
        requireInteraction: false,
        actions: data.actions || []
    };

    event.waitUntil(
        self.registration.showNotification(title, options)
    );
});

// Notification click event
self.addEventListener('notificationclick', (event) => {
    event.notification.close();

    event.waitUntil(
        clients.openWindow(event.notification.data || '/')
    );
});

// Background sync for driver offline mode
self.addEventListener('sync', (event) => {
    if (event.tag === 'sync-driver-data') {
        event.waitUntil(syncDriverData());
    }
});

// Periodic sync handler
self.addEventListener('periodicsync', (event) => {
    if (event.tag === 'sync-driver-data') {
        event.waitUntil(syncDriverData());
    }
});

async function syncDriverData() {
    try {
        // Get pending actions from IndexedDB or localStorage via client message
        const clients = await self.clients.matchAll();
        
        for (const client of clients) {
            client.postMessage({
                type: 'SYNC_REQUEST'
            });
        }
    } catch (error) {
        console.error('Background sync error:', error);
    }
}

// Message handler for communication with client
self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});