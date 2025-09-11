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
            const response = await fetch(`${this.apiBaseUrl}/tracking/shipment/${trackingNumber}`);
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

        const statuses = [
            { status: 'PENDING', label: 'Order Placed', icon: '📦' },
            { status: 'PICKED_UP', label: 'Picked Up', icon: '🚚' },
            { status: 'IN_TRANSIT', label: 'In Transit', icon: '🚛' },
            { status: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: '📮' },
            { status: 'DELIVERED', label: 'Delivered', icon: '✅' }
        ];

        const currentStatus = trackingData.status;
        let currentIndex = -1;

        statuses.forEach((status, index) => {
            if (status.status === currentStatus) {
                currentIndex = index;
            }
        });

        statuses.forEach((status, index) => {
            const isCompleted = index <= currentIndex;
            const isCurrent = index === currentIndex;
            
            const timelineItem = document.createElement('div');
            timelineItem.className = `timeline-item ${isCompleted ? 'completed' : ''}`;
            timelineItem.innerHTML = `
                <div class="timeline-content">
                    <h4>${status.icon} ${status.label}</h4>
                    <p>${isCurrent ? trackingData.estimatedDeliveryDate || 'Processing...' : ''}</p>
                </div>
            `;
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
