// Main Application JavaScript
class ReliableCarriersApp {
    constructor() {
        this.apiBaseUrl = '/api';
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkAuthStatus();
    }

    setupEventListeners() {
        // Login form
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Register form
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }

        // Tracking form
        const trackingForm = document.getElementById('trackingForm');
        if (trackingForm) {
            trackingForm.addEventListener('submit', (e) => this.handleTracking(e));
        }

        // Logout button
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.handleLogout());
        }

        // Booking form
        const bookingForm = document.getElementById('bookingForm');
        if (bookingForm) {
            bookingForm.addEventListener('submit', (e) => this.handleBooking(e));
        }
    }

    async handleLogin(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const email = formData.get('email');
        const password = formData.get('password');

        try {
            const response = await fetch(`${this.apiBaseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify(data));
                this.showAlert('Login successful!', 'success');
                setTimeout(() => {
                    // Role-based routing
                    let redirectUrl = '/dashboard'; // Default
                    
                    switch(data.role) {
                        case 'ADMIN':
                            redirectUrl = '/admin/dashboard';
                            break;
                        case 'DRIVER':
                            redirectUrl = '/driver/dashboard';
                            break;
                        case 'TRACKING_MANAGER':
                            redirectUrl = '/tracking/dashboard';
                            break;
                        case 'STAFF':
                            redirectUrl = '/admin/dashboard';
                            break;
                        case 'CUSTOMER':
                        default:
                            redirectUrl = '/customer';
                            break;
                    }
                    
                    window.location.href = redirectUrl;
                }, 1000);
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Login failed', 'danger');
            }
        } catch (error) {
            this.showAlert('Network error. Please try again.', 'danger');
        }
    }

    async handleRegister(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const userData = {
            firstName: formData.get('firstName'),
            lastName: formData.get('lastName'),
            email: formData.get('email'),
            password: formData.get('password'),
            phone: formData.get('phone'),
            address: formData.get('address'),
            city: formData.get('city'),
            state: formData.get('state'),
            zipCode: formData.get('zipCode'),
            country: formData.get('country')
        };

        try {
            const response = await fetch(`${this.apiBaseUrl}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData)
            });

            if (response.ok) {
                this.showAlert('Registration successful! Please login.', 'success');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Registration failed', 'danger');
            }
        } catch (error) {
            this.showAlert('Network error. Please try again.', 'danger');
        }
    }

    async handleTracking(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const trackingNumber = formData.get('trackingNumber');

        try {
            const response = await fetch(`${this.apiBaseUrl}/customer/track/${encodeURIComponent(trackingNumber)}`);
            if (response.ok) {
                const trackingData = await response.json();
                this.displayTrackingInfo(trackingData);
            } else {
                this.showAlert('Tracking number not found', 'warning');
            }
        } catch (error) {
            this.showAlert('Error fetching tracking information', 'danger');
        }
    }

    displayTrackingInfo(trackingData) {
        const trackingSection = document.getElementById('trackingSection');
        if (!trackingSection) return;
        const timeline = document.getElementById('trackingTimeline');
        timeline.innerHTML = '';

        // Populate header info if available
        const headerContainer = document.getElementById('trackingNumberDisplay');
        if (headerContainer) headerContainer.textContent = trackingData.trackingNumber || '';

        const statusEl = document.getElementById('packageStatus');
        if (statusEl) statusEl.textContent = trackingData.formattedStatus || (trackingData.status || '');

        const lastUpdateEl = document.getElementById('packageLocation');
        if (lastUpdateEl) lastUpdateEl.textContent = trackingData.lastUpdate ? `Last updated: ${trackingData.lastUpdate}` : '';

        const estEl = document.getElementById('estimatedDelivery');
        if (estEl) estEl.textContent = trackingData.formattedEstimatedDelivery || '';

        const weightEl = document.getElementById('packageWeight');
        if (weightEl) weightEl.textContent = trackingData.weight ? trackingData.weight + ' kg' : (trackingData.weightString || '');

        const dimEl = document.getElementById('packageDimensions');
        if (dimEl) dimEl.textContent = trackingData.dimensions || '';

        const svcEl = document.getElementById('serviceType');
        if (svcEl) svcEl.textContent = trackingData.serviceType || '';

        const senderEl = document.getElementById('senderAddress');
        if (senderEl) senderEl.textContent = trackingData.pickupCity ? `${trackingData.pickupCity}, ${trackingData.pickupState || ''}` : (trackingData.pickupAddress || '');

        const recipientEl = document.getElementById('recipientAddress');
        if (recipientEl) recipientEl.textContent = trackingData.deliveryCity ? `${trackingData.deliveryCity}, ${trackingData.deliveryState || ''}` : (trackingData.deliveryAddress || '');

        const sigEl = document.getElementById('signatureRequired');
        if (sigEl) sigEl.textContent = trackingData.signatureRequired ? 'Yes' : 'No';

        const events = trackingData.trackingEvents || [];
        events.forEach((item, index) => {
            // Build a single timeline item per event
            const timelineItem = document.createElement('div');
            timelineItem.className = 'flex items-start space-x-4 py-2';

            const dot = document.createElement('div');
            dot.className = 'flex-shrink-0';
            const circle = document.createElement('div');
            circle.className = 'w-3 h-3 bg-reliable-500 rounded-full ' + (index === 0 ? 'ring-2 ring-reliable-200' : '');
            dot.appendChild(circle);
            if (index < events.length - 1) {
                const line = document.createElement('div');
                line.className = 'w-0.5 h-8 bg-gray-300 ml-1.5';
                dot.appendChild(line);
            }

            const content = document.createElement('div');
            content.className = 'flex-1';
            const statusP = document.createElement('p');
            statusP.className = 'text-sm font-medium text-gray-800';
            statusP.textContent = item.status || item.formattedStatus || '';
            const locP = document.createElement('p');
            locP.className = 'text-sm text-gray-600';
            locP.textContent = item.location || item.notes || '';
            const tsP = document.createElement('p');
            tsP.className = 'text-xs text-gray-500';
            tsP.textContent = item.formattedTimestamp || item.timestamp || '';

            content.appendChild(statusP);
            if (locP.textContent) content.appendChild(locP);
            if (tsP.textContent) content.appendChild(tsP);

            timelineItem.appendChild(dot);
            timelineItem.appendChild(content);
            timeline.appendChild(timelineItem);
        });

        trackingSection.style.display = 'block';
    }

    async handleBooking(e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const bookingData = {
            recipientName: formData.get('recipientName'),
            recipientEmail: formData.get('recipientEmail'),
            recipientPhone: formData.get('recipientPhone'),
            pickupAddress: formData.get('pickupAddress'),
            pickupCity: formData.get('pickupCity'),
            pickupState: formData.get('pickupState'),
            pickupZipCode: formData.get('pickupZipCode'),
            pickupCountry: formData.get('pickupCountry'),
            deliveryAddress: formData.get('deliveryAddress'),
            deliveryCity: formData.get('deliveryCity'),
            deliveryState: formData.get('deliveryState'),
            deliveryZipCode: formData.get('deliveryZipCode'),
            deliveryCountry: formData.get('deliveryCountry'),
            weight: parseFloat(formData.get('weight')),
            dimensions: formData.get('dimensions'),
            description: formData.get('description')
        };

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${this.apiBaseUrl}/shipments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(bookingData)
            });

            if (response.ok) {
                const data = await response.json();
                this.showAlert(`Booking successful! Tracking number: ${data.trackingNumber}`, 'success');
                setTimeout(() => {
                    window.location.href = `/tracking/${data.trackingNumber}`;
                }, 2000);
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Booking failed', 'danger');
            }
        } catch (error) {
            this.showAlert('Network error. Please try again.', 'danger');
        }
    }

    async handleLogout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/';
    }

    checkAuthStatus() {
        const token = localStorage.getItem('token');
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        
        const authLinks = document.querySelectorAll('.auth-required');
        const guestLinks = document.querySelectorAll('.guest-only');
        const userInfo = document.getElementById('userInfo');

        if (token && user) {
            authLinks.forEach(link => link.style.display = 'inline');
            guestLinks.forEach(link => link.style.display = 'none');
            
            if (userInfo) {
                userInfo.textContent = `Welcome, ${user.firstName} ${user.lastName}`;
            }
        } else {
            authLinks.forEach(link => link.style.display = 'none');
            guestLinks.forEach(link => link.style.display = 'inline');
            
            if (userInfo) {
                userInfo.textContent = '';
            }
        }
    }

    showAlert(message, type = 'info') {
        const alertContainer = document.getElementById('alertContainer');
        if (!alertContainer) return;

        const alert = document.createElement('div');
        alert.className = `alert alert-${type}`;
        alert.innerHTML = `
            ${message}
            <button type="button" class="close" onclick="this.parentElement.remove()">&times;</button>
        `;

        alertContainer.appendChild(alert);

        setTimeout(() => {
            alert.remove();
        }, 5000);
    }

    // API helper methods
    async makeAuthenticatedRequest(url, options = {}) {
        const token = localStorage.getItem('token');
        if (!token) {
            throw new Error('No authentication token');
        }

        return fetch(url, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
    }

    // Dashboard data loading
    async loadDashboardData() {
        try {
            const response = await this.makeAuthenticatedRequest(`${this.apiBaseUrl}/dashboard/stats`);
            if (response.ok) {
                const data = await response.json();
                this.updateDashboardStats(data);
            }
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        }
    }

    updateDashboardStats(data) {
        const totalShipments = document.getElementById('totalShipments');
        const activeShipments = document.getElementById('activeShipments');
        const deliveredShipments = document.getElementById('deliveredShipments');
        const totalRevenue = document.getElementById('totalRevenue');

        if (totalShipments) totalShipments.textContent = data.totalShipments || 0;
        if (activeShipments) activeShipments.textContent = data.activeShipments || 0;
        if (deliveredShipments) deliveredShipments.textContent = data.deliveredShipments || 0;
        if (totalRevenue) totalRevenue.textContent = `$${(data.totalRevenue || 0).toFixed(2)}`;
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new ReliableCarriersApp();
});

// Google Maps integration for tracking
function initMap() {
    const mapContainer = document.getElementById('map');
    if (!mapContainer) return;

    // Initialize Google Maps
    const map = new google.maps.Map(mapContainer, {
        zoom: 12,
        center: { lat: 40.7128, lng: -74.0060 }, // Default to NYC
        styles: [
            {
                featureType: 'poi',
                elementType: 'labels',
                stylers: [{ visibility: 'off' }]
            }
        ]
    });

    // Add markers for tracking points
    const trackingPoints = [
        { lat: 40.7128, lng: -74.0060, title: 'Pickup Location' },
        { lat: 40.7589, lng: -73.9851, title: 'Current Location' },
        { lat: 40.7505, lng: -73.9934, title: 'Delivery Location' }
    ];

    trackingPoints.forEach(point => {
        new google.maps.Marker({
            position: { lat: point.lat, lng: point.lng },
            map: map,
            title: point.title
        });
    });
}
