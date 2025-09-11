// Tracking Management JavaScript

class TrackingManager {
    constructor() {
        this.map = null;
        this.markers = {};
        this.realTimeInterval = null;
        this.isRealTimeActive = false;
        this.currentFilters = {};
        this.driverData = [];
        this.vehicleData = [];
        
        this.init();
    }
    
    init() {
        this.initMap();
        this.bindEvents();
        this.loadInitialData();
    }
    
    // Initialize the map
    initMap() {
        this.map = L.map('map').setView([-26.2041, 28.0473], 6); // Default to South Africa (Johannesburg)
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.map);
        
        // Add map controls
        this.addMapControls();
    }
    
    // Add map controls
    addMapControls() {
        // Fullscreen control
        L.control.fullscreen({
            position: 'topleft',
            title: {
                'false': 'View Fullscreen',
                'true': 'Exit Fullscreen'
            }
        }).addTo(this.map);
        
        // Scale control
        L.control.scale({
            imperial: true,
            metric: true,
            position: 'bottomleft'
        }).addTo(this.map);
    }
    
    // Bind event listeners
    bindEvents() {
        // Refresh button
        const refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => this.refreshData());
        }
        
        // Real-time toggle
        const realTimeBtn = document.getElementById('realTimeBtn');
        if (realTimeBtn) {
            realTimeBtn.addEventListener('click', () => this.toggleRealTime());
        }
        
        // Search functionality
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
        }
        
        // Filter controls
        this.bindFilterEvents();
    }
    
    // Bind filter events
    bindFilterEvents() {
        const filterForm = document.getElementById('filterForm');
        if (filterForm) {
            filterForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.applyFilters();
            });
        }
        
        // Status filter
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', () => this.applyFilters());
        }
        
        // Location filter
        const cityFilter = document.getElementById('cityFilter');
        const stateFilter = document.getElementById('stateFilter');
        if (cityFilter && stateFilter) {
            cityFilter.addEventListener('change', () => this.applyFilters());
            stateFilter.addEventListener('change', () => this.applyFilters());
        }
    }
    
    // Load initial data
    async loadInitialData() {
        try {
            this.showLoading(true);
            const data = await this.fetchMapViewData();
            this.updateDashboard(data);
            this.showLoading(false);
        } catch (error) {
            console.error('Error loading initial data:', error);
            this.showError('Failed to load tracking data');
            this.showLoading(false);
        }
    }
    
    // Fetch map view data
    async fetchMapViewData() {
        const response = await fetch('/api/tracking/map-view');
        if (!response.ok) {
            throw new Error('Failed to fetch tracking data');
        }
        return await response.json();
    }
    
    // Update dashboard with data
    updateDashboard(data) {
        this.updateStatistics(data.statistics);
        this.updateDriverList(data.allDrivers);
        this.updateVehicleList(data.allVehicles);
        this.updateMapMarkers(data.activeDrivers);
        this.updateCharts(data);
    }
    
    // Update statistics
    updateStatistics(stats) {
        const elements = {
            totalDrivers: document.getElementById('totalDrivers'),
            onlineDrivers: document.getElementById('onlineDrivers'),
            offlineDrivers: document.getElementById('offlineDrivers'),
            onlinePercentage: document.getElementById('onlinePercentage')
        };
        
        if (elements.totalDrivers) elements.totalDrivers.textContent = stats.totalDrivers || 0;
        if (elements.onlineDrivers) elements.onlineDrivers.textContent = stats.onlineDrivers || 0;
        if (elements.offlineDrivers) elements.offlineDrivers.textContent = stats.offlineDrivers || 0;
        if (elements.onlinePercentage) {
            elements.onlinePercentage.textContent = Math.round(stats.onlinePercentage || 0) + '%';
        }
    }
    
    // Update driver list
    updateDriverList(drivers) {
        this.driverData = drivers;
        const driverList = document.getElementById('driverList');
        if (!driverList) return;
        
        driverList.innerHTML = '';
        
        drivers.forEach(driver => {
            const driverItem = this.createDriverItem(driver);
            driverList.appendChild(driverItem);
        });
    }
    
    // Create driver item element
    createDriverItem(driver) {
        const driverItem = document.createElement('div');
        driverItem.className = `bg-white/80 backdrop-blur-md rounded-xl border-l-4 ${driver.isOnline ? 'border-green-500' : 'border-red-500'} p-4 mb-3 transition-all duration-300 hover:bg-white/95 hover:transform hover:translate-x-1`;
        driverItem.innerHTML = `
            <div class="flex justify-between items-center">
                <div class="flex-grow">
                    <div class="flex items-center mb-1">
                        <span class="w-3 h-3 rounded-full ${driver.isOnline ? 'bg-green-500' : 'bg-red-500'} mr-2 animate-pulse"></span>
                        <h6 class="mb-0 font-semibold text-gray-800">${driver.name}</h6>
                    </div>
                    <p class="text-sm text-gray-600 mb-1">${driver.email}</p>
                    ${driver.phone ? `<p class="text-sm text-gray-600">${driver.phone}</p>` : ''}
                </div>
                <div class="text-right">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${driver.isOnline ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'} mb-2">
                        ${driver.status}
                    </span>
                                         <button class="bg-reliable-yellow-500 hover:bg-reliable-yellow-600 text-white text-sm font-medium py-1 px-3 rounded-lg transition-colors duration-200" 
                             onclick="trackingManager.showDriverDetails(${driver.id})">
                         <i class="fas fa-eye"></i>
                     </button>
                </div>
            </div>
        `;
        
        return driverItem;
    }
    
    // Update vehicle list
    updateVehicleList(vehicles) {
        this.vehicleData = vehicles;
        const vehicleList = document.getElementById('vehicleList');
        if (!vehicleList) return;
        
        vehicleList.innerHTML = '';
        
        vehicles.forEach(vehicle => {
            const vehicleItem = this.createVehicleItem(vehicle);
            vehicleList.appendChild(vehicleItem);
        });
    }
    
    // Create vehicle item element
    createVehicleItem(vehicle) {
        const vehicleItem = document.createElement('div');
        vehicleItem.className = `bg-white/80 backdrop-blur-md rounded-xl border-l-4 ${vehicle.isOnline ? 'border-green-500' : 'border-red-500'} p-4 mb-3 transition-all duration-300 hover:bg-white/95 hover:transform hover:translate-x-1`;
        vehicleItem.innerHTML = `
            <div class="flex justify-between items-center">
                <div class="flex-grow">
                    <h6 class="mb-1 font-semibold text-gray-800">${vehicle.model}</h6>
                    <p class="text-sm text-gray-600 mb-1">${vehicle.licensePlate}</p>
                    <p class="text-sm text-gray-600">${vehicle.type}</p>
                </div>
                <div class="text-right">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${vehicle.isOnline ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'} mb-2">
                        ${vehicle.status}
                    </span>
                                         <button class="bg-reliable-yellow-500 hover:bg-reliable-yellow-600 text-white text-sm font-medium py-1 px-3 rounded-lg transition-colors duration-200" 
                             onclick="trackingManager.showVehicleDetails(${vehicle.id})">
                         <i class="fas fa-eye"></i>
                     </button>
                </div>
            </div>
        `;
        
        return vehicleItem;
    }
    
    // Update map markers
    updateMapMarkers(drivers) {
        // Clear existing markers
        Object.values(this.markers).forEach(marker => this.map.removeLayer(marker));
        this.markers = {};
        
        drivers.forEach(driver => {
            if (driver.latitude && driver.longitude) {
                const marker = this.createDriverMarker(driver);
                this.markers[driver.driverId] = marker;
                marker.addTo(this.map);
            }
        });
        
        // Fit map to show all markers
        if (Object.keys(this.markers).length > 0) {
            const group = new L.featureGroup(Object.values(this.markers));
            this.map.fitBounds(group.getBounds().pad(0.1));
        }
    }
    
    // Create driver marker
    createDriverMarker(driver) {
        const markerColor = driver.isOnline ? '#16a34a' : '#dc2626'; // Green for online, red for offline
        const icon = L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: ${markerColor}; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);"></div>`,
            iconSize: [20, 20],
            iconAnchor: [10, 10]
        });
        
        const marker = L.marker([driver.latitude, driver.longitude], { icon })
            .bindPopup(this.createDriverPopup(driver));
        
        return marker;
    }
    
    // Create driver popup content
    createDriverPopup(driver) {
        return `
            <div class="p-4">
                <h6 class="text-reliable-yellow-500 font-semibold mb-3">${driver.driverName}</h6>
                <div class="grid grid-cols-2 gap-3 mb-3">
                    <div>
                        <span class="text-sm font-medium text-gray-600">Vehicle:</span><br>
                        <span class="text-sm">${driver.vehicleModel || 'N/A'}</span>
                    </div>
                    <div>
                        <span class="text-sm font-medium text-gray-600">Status:</span><br>
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${driver.isOnline ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                            ${driver.status}
                        </span>
                    </div>
                </div>
                <hr class="my-3">
                <div class="mb-3">
                    <span class="text-sm font-medium text-gray-600">Last Update:</span><br>
                    <span class="text-sm">${driver.formattedTimestamp}</span>
                </div>
                <button class="w-full bg-reliable-yellow-500 hover:bg-reliable-yellow-600 text-white text-sm font-medium py-2 px-4 rounded-lg transition-colors duration-200" onclick="trackingManager.showDriverDetails(${driver.driverId})">
                    View Details
                </button>
            </div>
        `;
    }
    
    // Update charts (if Chart.js is available)
    updateCharts(data) {
        if (typeof Chart !== 'undefined') {
            this.updateStatusChart(data.driverStatusSummary);
            this.updateLocationChart(data.allDrivers);
        }
    }
    
    // Update status chart
    updateStatusChart(statusData) {
        const ctx = document.getElementById('statusChart');
        if (!ctx) return;
        
        if (this.statusChart) {
            this.statusChart.destroy();
        }
        
        this.statusChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: Object.keys(statusData),
                datasets: [{
                    data: Object.values(statusData),
                    backgroundColor: ['#16a34a', '#dc2626'],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
    
    // Update location chart
    updateLocationChart(drivers) {
        const ctx = document.getElementById('locationChart');
        if (!ctx) return;
        
        // Group drivers by city
        const cityData = {};
        drivers.forEach(driver => {
            if (driver.lastLocation && driver.lastLocation.city) {
                cityData[driver.lastLocation.city] = (cityData[driver.lastLocation.city] || 0) + 1;
            }
        });
        
        if (this.locationChart) {
            this.locationChart.destroy();
        }
        
        this.locationChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Object.keys(cityData),
                datasets: [{
                    label: 'Drivers per City',
                    data: Object.values(cityData),
                    backgroundColor: '#eab308',
                    borderColor: '#ca8a04',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
    
    // Refresh data
    async refreshData() {
        try {
            this.showLoading(true);
            const data = await this.fetchMapViewData();
            this.updateDashboard(data);
            this.showLoading(false);
            this.showSuccess('Data refreshed successfully');
        } catch (error) {
            console.error('Error refreshing data:', error);
            this.showError('Failed to refresh data');
            this.showLoading(false);
        }
    }
    
    // Toggle real-time updates
    toggleRealTime() {
        if (this.isRealTimeActive) {
            this.stopRealTime();
        } else {
            this.startRealTime();
        }
    }
    
    // Start real-time updates
    startRealTime() {
        this.isRealTimeActive = true;
        this.realTimeInterval = setInterval(() => this.refreshData(), 30000); // 30 seconds
        this.updateRealTimeButton(true);
        this.showSuccess('Real-time tracking started');
    }
    
    // Stop real-time updates
    stopRealTime() {
        this.isRealTimeActive = false;
        if (this.realTimeInterval) {
            clearInterval(this.realTimeInterval);
            this.realTimeInterval = null;
        }
        this.updateRealTimeButton(false);
        this.showSuccess('Real-time tracking stopped');
    }
    
    // Update real-time button
    updateRealTimeButton(isActive) {
        const btn = document.getElementById('realTimeBtn');
        const text = document.getElementById('realTimeText');
        if (btn && text) {
            if (isActive) {
                btn.classList.remove('btn-outline-primary');
                btn.classList.add('btn-danger');
                text.textContent = 'Stop Real-time';
            } else {
                btn.classList.remove('btn-danger');
                btn.classList.add('btn-outline-primary');
                text.textContent = 'Start Real-time';
            }
        }
    }
    
    // Handle search
    handleSearch(searchTerm) {
        if (!searchTerm.trim()) {
            this.resetFilters();
            return;
        }
        
        const filteredDrivers = this.driverData.filter(driver => 
            driver.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            driver.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (driver.phone && driver.phone.includes(searchTerm))
        );
        
        this.updateDriverList(filteredDrivers);
    }
    
    // Apply filters
    async applyFilters() {
        const filters = this.getFilterValues();
        this.currentFilters = filters;
        
        try {
            this.showLoading(true);
            const response = await fetch('/api/tracking/filter', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(filters)
            });
            
            if (!response.ok) throw new Error('Filter request failed');
            
            const drivers = await response.json();
            this.updateDriverList(drivers);
            this.updateMapMarkers(drivers);
            this.showLoading(false);
        } catch (error) {
            console.error('Error applying filters:', error);
            this.showError('Failed to apply filters');
            this.showLoading(false);
        }
    }
    
    // Get filter values
    getFilterValues() {
        const filters = {};
        
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) filters.status = statusFilter.value;
        
        const cityFilter = document.getElementById('cityFilter');
        if (cityFilter) filters.city = cityFilter.value;
        
        const stateFilter = document.getElementById('stateFilter');
        if (stateFilter) filters.state = stateFilter.value;
        
        return filters;
    }
    
    // Reset filters
    resetFilters() {
        this.currentFilters = {};
        const filterForm = document.getElementById('filterForm');
        if (filterForm) filterForm.reset();
        this.loadInitialData();
    }
    
    // Show driver details
    showDriverDetails(driverId) {
        // Implement driver details modal or navigation
        console.log('Show driver details:', driverId);
        // You can implement a modal or navigate to a details page
    }
    
    // Show vehicle details
    showVehicleDetails(vehicleId) {
        // Implement vehicle details modal or navigation
        console.log('Show vehicle details:', vehicleId);
        // You can implement a modal or navigate to a details page
    }
    
    // Show loading state
    showLoading(show) {
        const loadingElement = document.getElementById('loadingIndicator');
        if (loadingElement) {
            loadingElement.style.display = show ? 'block' : 'none';
        }
    }
    
    // Show success message
    showSuccess(message) {
        this.showNotification(message, 'success');
    }
    
    // Show error message
    showError(message) {
        this.showNotification(message, 'error');
    }
    
    // Show notification
    showNotification(message, type) {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show position-fixed`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);
    }
}

// Initialize tracking manager when DOM is loaded
let trackingManager;
document.addEventListener('DOMContentLoaded', function() {
    trackingManager = new TrackingManager();
});

// Export for global access
window.trackingManager = trackingManager;
