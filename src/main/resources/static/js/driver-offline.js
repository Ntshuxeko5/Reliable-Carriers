/**
 * Driver Offline Mode Support
 * Enables drivers to work offline with data synchronization when online
 */

class DriverOfflineMode {
    constructor() {
        this.isOnline = navigator.onLine;
        this.pendingActions = [];
        this.serviceWorkerRegistration = null;
        this.init();
    }

    async init() {
        this.setupOnlineOfflineListeners();
        await this.registerServiceWorker();
        this.loadPendingActions();
        this.setupPeriodicSync();
    }

    setupOnlineOfflineListeners() {
        window.addEventListener('online', () => {
            this.isOnline = true;
            this.onConnectionRestored();
            this.showNotification('Connection restored. Syncing data...', 'success');
        });

        window.addEventListener('offline', () => {
            this.isOnline = false;
            this.onConnectionLost();
            this.showNotification('You are offline. Changes will sync when connection is restored.', 'warning');
        });

        // Update status indicator
        this.updateConnectionIndicator();
    }

    async registerServiceWorker() {
        if ('serviceWorker' in navigator) {
            try {
                this.serviceWorkerRegistration = await navigator.serviceWorker.register('/sw.js');
                console.log('Service Worker registered for offline mode');
                
                // Request background sync
                if ('sync' in this.serviceWorkerRegistration) {
                    await this.serviceWorkerRegistration.sync.register('sync-driver-data');
                }
            } catch (error) {
                console.error('Service Worker registration failed:', error);
            }
        }
    }

    setupPeriodicSync() {
        if ('periodicSync' in this.serviceWorkerRegistration) {
            this.serviceWorkerRegistration.periodicSync.register('sync-driver-data', {
                minInterval: 5 * 60 * 1000 // 5 minutes
            }).catch(error => {
                console.error('Periodic sync registration failed:', error);
            });
        }
    }

    async savePendingAction(action) {
        const actions = this.getPendingActions();
        actions.push({
            ...action,
            timestamp: new Date().toISOString(),
            id: this.generateId()
        });
        localStorage.setItem('driverPendingActions', JSON.stringify(actions));
        this.pendingActions = actions;
    }

    getPendingActions() {
        if (this.pendingActions.length > 0) {
            return this.pendingActions;
        }
        
        try {
            const stored = localStorage.getItem('driverPendingActions');
            return stored ? JSON.parse(stored) : [];
        } catch (error) {
            console.error('Error loading pending actions:', error);
            return [];
        }
    }

    loadPendingActions() {
        this.pendingActions = this.getPendingActions();
        if (this.pendingActions.length > 0) {
            this.updatePendingActionsIndicator();
        }
    }

    async syncPendingActions() {
        if (!this.isOnline) {
            return;
        }

        const actions = this.getPendingActions();
        if (actions.length === 0) {
            return;
        }

        console.log(`Syncing ${actions.length} pending actions...`);

        const successfulActions = [];
        
        for (const action of actions) {
            try {
                const success = await this.executeAction(action);
                if (success) {
                    successfulActions.push(action.id);
                }
            } catch (error) {
                console.error('Error syncing action:', error);
            }
        }

        // Remove successful actions
        const remainingActions = actions.filter(a => !successfulActions.includes(a.id));
        localStorage.setItem('driverPendingActions', JSON.stringify(remainingActions));
        this.pendingActions = remainingActions;
        this.updatePendingActionsIndicator();

        if (successfulActions.length > 0) {
            this.showNotification(`${successfulActions.length} action(s) synced successfully`, 'success');
        }
    }

    async executeAction(action) {
        const { type, data, endpoint, method } = action;

        try {
            const response = await fetch(endpoint, {
                method: method || 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
                },
                body: JSON.stringify(data)
            });

            return response.ok;
        } catch (error) {
            console.error(`Error executing action ${type}:`, error);
            return false;
        }
    }

    // Offline-capable action methods
    async markPackagePickedUp(packageId, trackingNumber) {
        const action = {
            type: 'PICKUP',
            endpoint: `/api/driver/packages/${packageId}/pickup`,
            method: 'POST',
            data: { trackingNumber }
        };

        if (this.isOnline) {
            const success = await this.executeAction(action);
            if (!success) {
                await this.savePendingAction(action);
            }
        } else {
            await this.savePendingAction(action);
        }
    }

    async markPackageDelivered(packageId, trackingNumber, signature = null) {
        const action = {
            type: 'DELIVERY',
            endpoint: `/api/driver/packages/${packageId}/deliver`,
            method: 'POST',
            data: { trackingNumber, signature }
        };

        if (this.isOnline) {
            const success = await this.executeAction(action);
            if (!success) {
                await this.savePendingAction(action);
            }
        } else {
            await this.savePendingAction(action);
        }
    }

    async updateLocation(latitude, longitude) {
        const action = {
            type: 'LOCATION_UPDATE',
            endpoint: '/api/driver/location/update',
            method: 'POST',
            data: { latitude, longitude }
        };

        if (this.isOnline) {
            const success = await this.executeAction(action);
            if (!success) {
                await this.savePendingAction(action);
            }
        } else {
            // Don't queue location updates offline - too many
            console.log('Location update skipped - offline');
        }
    }

    async updateStatus(status) {
        const action = {
            type: 'STATUS_UPDATE',
            endpoint: '/api/driver/status',
            method: 'POST',
            data: { status }
        };

        if (this.isOnline) {
            const success = await this.executeAction(action);
            if (!success) {
                await this.savePendingAction(action);
            }
        } else {
            await this.savePendingAction(action);
        }
    }

    onConnectionRestored() {
        this.updateConnectionIndicator();
        this.syncPendingActions();
        
        // Request background sync
        if (this.serviceWorkerRegistration && 'sync' in this.serviceWorkerRegistration) {
            this.serviceWorkerRegistration.sync.register('sync-driver-data');
        }
    }

    onConnectionLost() {
        this.updateConnectionIndicator();
    }

    updateConnectionIndicator() {
        const indicator = document.getElementById('connectionStatusIndicator');
        if (indicator) {
            indicator.className = this.isOnline 
                ? 'w-3 h-3 bg-green-500 rounded-full' 
                : 'w-3 h-3 bg-red-500 rounded-full';
            indicator.title = this.isOnline ? 'Online' : 'Offline';
        }
    }

    updatePendingActionsIndicator() {
        const count = this.pendingActions.length;
        const indicator = document.getElementById('pendingActionsBadge');
        if (indicator) {
            if (count > 0) {
                indicator.textContent = count;
                indicator.classList.remove('hidden');
            } else {
                indicator.classList.add('hidden');
            }
        }
    }

    generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `fixed top-4 right-4 bg-${type === 'success' ? 'green' : type === 'warning' ? 'yellow' : 'blue'}-500 text-white px-6 py-3 rounded-lg shadow-lg z-50`;
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    }
}

// Global instance
let driverOfflineMode = null;

// Initialize on driver pages
if (window.location.pathname.includes('/driver')) {
    document.addEventListener('DOMContentLoaded', () => {
        driverOfflineMode = new DriverOfflineMode();
        
        // Expose to window for manual sync
        window.driverOfflineMode = driverOfflineMode;
    });
}
