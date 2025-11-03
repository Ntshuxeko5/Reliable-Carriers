/**
 * Customer Dashboard Enhancements
 * Loads real package statistics and recent activity
 */

class DashboardEnhancer {
    constructor() {
        this.customerEmail = this.getCurrentCustomerEmail();
    }

    async init() {
        if (this.customerEmail) {
            await this.loadPackageStatistics();
            await this.loadRecentActivity();
        }
    }

    getCurrentCustomerEmail() {
        // Try to get from sessionStorage, localStorage, or API
        return sessionStorage.getItem('userEmail') || 
               localStorage.getItem('userEmail') || 
               null;
    }

    async loadPackageStatistics() {
        if (!this.customerEmail) return;

        try {
            const response = await fetch(`/api/customer/packages/email/${encodeURIComponent(this.customerEmail)}/statistics`);
            const data = await response.json();

            if (data.success && data.statistics) {
                const stats = data.statistics;
                
                // Update stats display
                this.updateElement('totalPackages', stats.totalPackages || 0);
                this.updateElement('deliveredPackages', stats.deliveredCount || 0);
                this.updateElement('inTransitPackages', stats.inTransitCount || 0);
                this.updateElement('pendingPackages', stats.pendingCount || 0);
            }
        } catch (error) {
            console.error('Error loading package statistics:', error);
        }
    }

    async loadRecentActivity() {
        if (!this.customerEmail) return;

        try {
            const response = await fetch(`/api/customer/packages/email/${encodeURIComponent(this.customerEmail)}/history?limit=5`);
            const data = await response.json();

            if (data.success && data.packages) {
                this.displayRecentActivity(data.packages);
            } else {
                this.displayNoActivity();
            }
        } catch (error) {
            console.error('Error loading recent activity:', error);
            this.displayNoActivity();
        }
    }

    displayRecentActivity(packages) {
        const container = document.getElementById('recentActivity');
        if (!container) return;

        if (packages.length === 0) {
            this.displayNoActivity();
            return;
        }

        container.innerHTML = packages.map(pkg => `
            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                <div class="flex items-center space-x-4">
                    <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                        <i class="fas fa-box text-blue-600"></i>
                    </div>
                    <div>
                        <div class="font-semibold text-gray-900">${pkg.trackingNumber || 'N/A'}</div>
                        <div class="text-sm text-gray-500">${pkg.status || 'Unknown Status'}</div>
                    </div>
                </div>
                <div class="text-right">
                    <div class="text-sm text-gray-500">${this.formatDate(pkg.updatedAt || pkg.createdAt)}</div>
                    <a href="/customer/track?tracking=${pkg.trackingNumber}" 
                       class="text-blue-600 hover:text-blue-700 text-sm font-medium">
                        View Details →
                    </a>
                </div>
            </div>
        `).join('');
    }

    displayNoActivity() {
        const container = document.getElementById('recentActivity');
        if (container) {
            container.innerHTML = `
                <div class="text-center text-gray-500 py-8">
                    <i class="fas fa-inbox text-4xl mb-2"></i>
                    <p>No recent activity</p>
                </div>
            `;
        }
    }

    updateElement(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    }

    formatDate(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-ZA', { 
            day: 'numeric', 
            month: 'short', 
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
}

// Initialize dashboard enhancements
document.addEventListener('DOMContentLoaded', function() {
    const enhancer = new DashboardEnhancer();
    enhancer.init();
});
