// Driver Dashboard JavaScript
// Additional utility functions for the driver dashboard

// Global variables
let refreshInterval;

// Initialize driver dashboard
function initializeDriverDashboard() {
    console.log('Driver dashboard initialized');
    
    // Set up auto-refresh
    setupAutoRefresh();
    
    // Set up event listeners
    setupEventListeners();
}

// Set up auto-refresh functionality
function setupAutoRefresh() {
    // Refresh data every 30 seconds
    refreshInterval = setInterval(() => {
        if (typeof loadPackageData === 'function') {
            loadPackageData();
        }
    }, 30000);
}

// Set up event listeners
function setupEventListeners() {
    // Listen for visibility change to pause/resume refresh when tab is not active
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            clearInterval(refreshInterval);
        } else {
            setupAutoRefresh();
        }
    });
    
    // Listen for online/offline status
    window.addEventListener('online', function() {
        console.log('Connection restored');
        if (typeof loadPackageData === 'function') {
            loadPackageData();
        }
    });
    
    window.addEventListener('offline', function() {
        console.log('Connection lost');
    });
}

// Utility function to format distance
function formatDistance(distance) {
    if (distance === null || distance === undefined) {
        return 'N/A';
    }
    
    if (distance < 1) {
        return Math.round(distance * 1000) + ' m';
    } else {
        return distance.toFixed(1) + ' km';
    }
}

// Utility function to format time
function formatTime(minutes) {
    if (minutes === null || minutes === undefined) {
        return 'N/A';
    }
    
    if (minutes < 60) {
        return minutes + ' min';
    } else {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return hours + 'h ' + mins + 'm';
    }
}

// Utility function to show notifications
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg transition-all duration-300 transform translate-x-full ${
        type === 'success' ? 'bg-green-500 text-white' :
        type === 'error' ? 'bg-red-500 text-white' :
        type === 'warning' ? 'bg-yellow-500 text-white' :
        'bg-blue-500 text-white'
    }`;
    
    notification.innerHTML = `
        <div class="flex items-center">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : type === 'warning' ? 'exclamation-triangle' : 'info-circle'} mr-2"></i>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.classList.remove('translate-x-full');
    }, 100);
    
    // Remove after 5 seconds
    setTimeout(() => {
        notification.classList.add('translate-x-full');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 5000);
}

// Utility function to confirm actions
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

// Utility function to handle API errors
function handleApiError(error, context = '') {
    console.error(`API Error ${context}:`, error);
    
    let message = 'An error occurred. Please try again.';
    
    if (error.status === 401) {
        message = 'Session expired. Please log in again.';
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);
    } else if (error.status === 403) {
        message = 'You do not have permission to perform this action.';
    } else if (error.status === 404) {
        message = 'The requested resource was not found.';
    } else if (error.status >= 500) {
        message = 'Server error. Please try again later.';
    }
    
    showNotification(message, 'error');
}

// Utility function to validate coordinates
function isValidCoordinates(lat, lng) {
    return lat !== null && lng !== null && 
           !isNaN(lat) && !isNaN(lng) &&
           lat >= -90 && lat <= 90 &&
           lng >= -180 && lng <= 180;
}

// Utility function to calculate bearing between two points
function calculateBearing(lat1, lng1, lat2, lng2) {
    const toRad = Math.PI / 180;
    const toDeg = 180 / Math.PI;
    
    const dLng = (lng2 - lng1) * toRad;
    const lat1Rad = lat1 * toRad;
    const lat2Rad = lat2 * toRad;
    
    const y = Math.sin(dLng) * Math.cos(lat2Rad);
    const x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - 
              Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng);
    
    let bearing = Math.atan2(y, x) * toDeg;
    bearing = (bearing + 360) % 360;
    
    return bearing;
}

// Utility function to get compass direction
function getCompassDirection(bearing) {
    const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
    const index = Math.round(bearing / 45) % 8;
    return directions[index];
}

// Export functions for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        initializeDriverDashboard,
        formatDistance,
        formatTime,
        showNotification,
        confirmAction,
        handleApiError,
        isValidCoordinates,
        calculateBearing,
        getCompassDirection
    };
}
