/**
 * Web Push Notifications Service
 * Handles browser push notification subscription and management
 */

class WebPushNotificationService {
    constructor() {
        this.registration = null;
        this.subscription = null;
        this.VAPID_PUBLIC_KEY = null; // Should be loaded from server
    }

    async init() {
        if ('serviceWorker' in navigator && 'PushManager' in window) {
            try {
                // Register service worker
                this.registration = await navigator.serviceWorker.register('/sw.js');
                console.log('Service Worker registered');

                // Get VAPID public key from server
                await this.loadVapidKey();

                // Check if already subscribed
                this.subscription = await this.registration.pushManager.getSubscription();
                
                if (this.subscription) {
                    console.log('Already subscribed to push notifications');
                    return true;
                }

                return false;
            } catch (error) {
                console.error('Service Worker registration failed:', error);
                return false;
            }
        } else {
            console.log('Push notifications are not supported');
            return false;
        }
    }

    async loadVapidKey() {
        try {
            const response = await fetch('/api/push/vapid-public-key');
            const data = await response.json();
            this.VAPID_PUBLIC_KEY = data.publicKey;
        } catch (error) {
            console.error('Failed to load VAPID key:', error);
        }
    }

    async subscribe() {
        if (!this.registration || !this.VAPID_PUBLIC_KEY) {
            console.error('Service worker or VAPID key not available');
            return false;
        }

        try {
            this.subscription = await this.registration.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: this.urlBase64ToUint8Array(this.VAPID_PUBLIC_KEY)
            });

            // Send subscription to server
            await this.sendSubscriptionToServer(this.subscription);
            
            console.log('Subscribed to push notifications');
            return true;
        } catch (error) {
            console.error('Failed to subscribe to push notifications:', error);
            return false;
        }
    }

    async unsubscribe() {
        if (!this.subscription) {
            return false;
        }

        try {
            const success = await this.subscription.unsubscribe();
            if (success) {
                // Notify server
                await fetch('/api/push/unsubscribe', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ subscription: this.subscription })
                });
                
                this.subscription = null;
                console.log('Unsubscribed from push notifications');
                return true;
            }
            return false;
        } catch (error) {
            console.error('Failed to unsubscribe:', error);
            return false;
        }
    }

    async sendSubscriptionToServer(subscription) {
        try {
            await fetch('/api/push/subscribe', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
                },
                body: JSON.stringify({ subscription })
            });
        } catch (error) {
            console.error('Failed to send subscription to server:', error);
        }
    }

    urlBase64ToUint8Array(base64String) {
        const padding = '='.repeat((4 - base64String.length % 4) % 4);
        const base64 = (base64String + padding)
            .replace(/\-/g, '+')
            .replace(/_/g, '/');

        const rawData = window.atob(base64);
        const outputArray = new Uint8Array(rawData.length);

        for (let i = 0; i < rawData.length; ++i) {
            outputArray[i] = rawData.charCodeAt(i);
        }
        return outputArray;
    }

    async requestPermission() {
        if ('Notification' in window) {
            const permission = await Notification.requestPermission();
            return permission === 'granted';
        }
        return false;
    }
}

// Global instance
const webPushService = new WebPushNotificationService();

// Initialize on page load
document.addEventListener('DOMContentLoaded', async function() {
    const isSupported = await webPushService.init();
    
    // Show notification prompt if supported and not subscribed
    if (isSupported && !webPushService.subscription) {
        // Optional: Show a banner to prompt user to enable notifications
        showNotificationPrompt();
    }
});

function showNotificationPrompt() {
    // Create notification prompt banner
    const banner = document.createElement('div');
    banner.id = 'notificationPrompt';
    banner.className = 'fixed bottom-4 right-4 bg-blue-600 text-white p-4 rounded-lg shadow-lg max-w-sm z-50';
    banner.innerHTML = `
        <div class="flex items-start space-x-3">
            <i class="fas fa-bell text-xl mt-1"></i>
            <div class="flex-1">
                <h4 class="font-semibold mb-1">Get Real-Time Updates</h4>
                <p class="text-sm text-blue-100 mb-3">Enable notifications to receive instant package status updates.</p>
                <div class="flex space-x-2">
                    <button onclick="enableNotifications()" class="bg-white text-blue-600 px-4 py-2 rounded font-medium text-sm hover:bg-blue-50">
                        Enable
                    </button>
                    <button onclick="dismissNotificationPrompt()" class="bg-blue-700 text-white px-4 py-2 rounded font-medium text-sm hover:bg-blue-800">
                        Dismiss
                    </button>
                </div>
            </div>
            <button onclick="dismissNotificationPrompt()" class="text-blue-200 hover:text-white">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
    document.body.appendChild(banner);
}

async function enableNotifications() {
    const permissionGranted = await webPushService.requestPermission();
    if (permissionGranted) {
        const subscribed = await webPushService.subscribe();
        if (subscribed) {
            showToast('Notifications enabled! You will receive real-time updates.', 'success');
            dismissNotificationPrompt();
        } else {
            showToast('Failed to enable notifications. Please try again.', 'error');
        }
    } else {
        showToast('Notification permission denied.', 'error');
    }
}

function dismissNotificationPrompt() {
    const banner = document.getElementById('notificationPrompt');
    if (banner) {
        banner.remove();
    }
}

function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `fixed top-4 right-4 bg-${type === 'success' ? 'green' : 'red'}-500 text-white px-6 py-3 rounded-lg shadow-lg z-50`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}
