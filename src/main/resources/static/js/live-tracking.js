/**
 * Live Package Tracking with Real-Time Updates
 * Integrates WebSocket for real-time package location updates
 */

class LivePackageTracker {
    constructor(trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.map = null;
        this.driverMarker = null;
        this.routePolyline = null;
        this.stompClient = null;
        this.isConnected = false;
    }

    init() {
        this.initializeMap();
        this.connectWebSocket();
        this.startTracking();
    }

    initializeMap() {
        const mapContainer = document.getElementById('liveTrackingMap');
        if (!mapContainer) return;

        this.map = new google.maps.Map(mapContainer, {
            zoom: 12,
            center: { lat: -26.2041, lng: 28.0473 }, // Default to Johannesburg
            mapTypeControl: true,
            streetViewControl: false,
            fullscreenControl: true
        });
    }

    connectWebSocket() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('Connected to WebSocket:', frame);
            this.isConnected = true;
            this.subscribeToUpdates();
            this.updateConnectionStatus(true);
        }, (error) => {
            console.error('WebSocket connection error:', error);
            this.isConnected = false;
            this.updateConnectionStatus(false);
            // Retry after 5 seconds
            setTimeout(() => this.connectWebSocket(), 5000);
        });
    }

    subscribeToUpdates() {
        if (!this.stompClient || !this.isConnected) return;

        // Subscribe to package status updates
        this.stompClient.subscribe('/topic/package-updates', (message) => {
            const update = JSON.parse(message.body);
            if (update.trackingNumber === this.trackingNumber) {
                this.handleStatusUpdate(update);
            }
        });

        // Subscribe to specific tracking number updates
        this.stompClient.subscribe('/topic/tracking/' + this.trackingNumber, (message) => {
            const update = JSON.parse(message.body);
            this.handleStatusUpdate(update);
        });

        // Subscribe to live tracking updates
        this.stompClient.subscribe('/topic/live-tracking/' + this.trackingNumber, (message) => {
            const data = JSON.parse(message.body);
            this.updateDriverLocation(data);
        });
    }

    async startTracking() {
        try {
            const response = await fetch(`/api/customer/live-tracking/${this.trackingNumber}`);
            const result = await response.json();

            if (result.success && result.data) {
                this.displayTrackingData(result.data);
            } else {
                console.error('Tracking error:', result.message);
            }
        } catch (error) {
            console.error('Error fetching tracking data:', error);
        }
    }

    displayTrackingData(data) {
        // Update status display
        if (data.status) {
            document.getElementById('currentStatus').textContent = this.formatStatus(data.status);
            document.getElementById('currentStatus').className = 'text-lg font-semibold status-' + data.status.toLowerCase().replace('_', '-');
            this.updateProgressBar(data.status);
        }

        // Update addresses
        if (data.pickupAddress) {
            document.getElementById('pickupLocation').textContent = data.pickupAddress;
        }
        if (data.deliveryAddress) {
            document.getElementById('deliveryLocation').textContent = data.deliveryAddress;
        }

        // Display map markers
        if (data.pickupLat && data.pickupLng) {
            this.addMarker(data.pickupLat, data.pickupLng, 'Pickup Location', '#3b82f6');
        }

        if (data.deliveryLat && data.deliveryLng) {
            this.addMarker(data.deliveryLat, data.deliveryLng, 'Delivery Location', '#10b981');
        }

        // Display driver location if available
        if (data.driverLocation) {
            this.updateDriverLocation(data);
        }

        // Draw route if both locations available
        if (data.pickupLat && data.pickupLng && data.deliveryLat && data.deliveryLng) {
            this.drawRoute(
                { lat: parseFloat(data.pickupLat), lng: parseFloat(data.pickupLng) },
                { lat: parseFloat(data.deliveryLat), lng: parseFloat(data.deliveryLng) }
            );
        }
    }

    updateDriverLocation(data) {
        if (!data.driverLocation || !this.map) return;

        const lat = parseFloat(data.driverLocation.latitude);
        const lng = parseFloat(data.driverLocation.longitude);

        if (this.driverMarker) {
            // Animate marker movement
            this.driverMarker.setPosition({ lat, lng });
        } else {
            // Create new marker
            this.driverMarker = new google.maps.Marker({
                position: { lat, lng },
                map: this.map,
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 10,
                    fillColor: '#ef4444',
                    fillOpacity: 1,
                    strokeColor: '#ffffff',
                    strokeWeight: 2
                },
                title: 'Driver Location',
                animation: google.maps.Animation.DROP
            });

            // Center map on driver
            this.map.setCenter({ lat, lng });
            this.map.setZoom(14);
        }

        // Update route if driver is moving
        if (data.pickupLat && data.deliveryLat) {
            this.updateRouteWithDriver(
                { lat: parseFloat(data.pickupLat), lng: parseFloat(data.pickupLng) },
                { lat, lng },
                { lat: parseFloat(data.deliveryLat), lng: parseFloat(data.deliveryLng) }
            );
        }
    }

    addMarker(lat, lng, title, color) {
        if (!this.map) return;

        new google.maps.Marker({
            position: { lat: parseFloat(lat), lng: parseFloat(lng) },
            map: this.map,
            title: title,
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 8,
                fillColor: color,
                fillOpacity: 1,
                strokeColor: '#ffffff',
                strokeWeight: 2
            }
        });
    }

    drawRoute(start, end) {
        if (!this.map) return;

        const directionsService = new google.maps.DirectionsService();
        const directionsRenderer = new google.maps.DirectionsRenderer({
            map: this.map,
            suppressMarkers: true,
            polylineOptions: {
                strokeColor: '#3b82f6',
                strokeWeight: 4,
                strokeOpacity: 0.7
            }
        });

        directionsService.route({
            origin: start,
            destination: end,
            travelMode: google.maps.TravelMode.DRIVING
        }, (result, status) => {
            if (status === 'OK') {
                directionsRenderer.setDirections(result);
                this.routePolyline = directionsRenderer;
            }
        });
    }

    updateRouteWithDriver(pickup, driver, delivery) {
        // Update route to show driver's current position along the route
        if (this.map && this.driverMarker) {
            // Keep existing route, just update driver position
            this.map.setCenter(driver);
        }
    }

    handleStatusUpdate(update) {
        // Update UI with new status
        if (update.status) {
            document.getElementById('currentStatus').textContent = this.formatStatus(update.status);
            this.updateProgressBar(update.status);
        }

        // Show notification
        this.showNotification(`Status updated: ${this.formatStatus(update.status)}`, 'info');
    }

    updateProgressBar(status) {
        const progress = this.getStatusProgress(status);
        const progressBar = document.getElementById('statusProgress');
        const progressText = document.getElementById('progressText');

        if (progressBar) {
            progressBar.style.width = progress + '%';
        }
        if (progressText) {
            progressText.textContent = progress + '% Complete';
        }
    }

    getStatusProgress(status) {
        const statusMap = {
            'PENDING': 10,
            'CONFIRMED': 20,
            'ASSIGNED': 30,
            'PICKED_UP': 50,
            'IN_TRANSIT': 70,
            'OUT_FOR_DELIVERY': 90,
            'DELIVERED': 100
        };
        return statusMap[status] || 0;
    }

    formatStatus(status) {
        return status.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
    }

    updateConnectionStatus(connected) {
        const indicator = document.getElementById('statusIndicator');
        if (indicator) {
            indicator.style.backgroundColor = connected ? '#10b981' : '#ef4444';
            indicator.title = connected ? 'Real-time updates connected' : 'Real-time updates disconnected';
        }
    }

    showNotification(message, type) {
        // Create toast notification
        const notification = document.createElement('div');
        notification.className = `fixed top-4 right-4 bg-${type === 'info' ? 'blue' : 'green'}-500 text-white px-6 py-3 rounded-lg shadow-lg z-50`;
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    destroy() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// Initialize when tracking number is available
let liveTracker = null;

function initLiveTracking(trackingNumber) {
    if (liveTracker) {
        liveTracker.destroy();
    }
    liveTracker = new LivePackageTracker(trackingNumber);
    liveTracker.init();
}
