/**
 * Admin Utilities - Common functions for admin pages
 * Status formatting, currency formatting, and helper functions
 */

/**
 * Format status with color and icon
 * @param {string} status - Status value
 * @param {string} type - Type of status (shipment, booking, driver, etc.)
 * @returns {object} Object with display text, color classes, and icon
 */
function formatStatus(status, type = 'shipment') {
    if (!status) {
        return {
            text: 'Unknown',
            color: 'bg-gray-100 text-gray-800',
            icon: 'fa-question-circle'
        };
    }
    
    const statusUpper = status.toUpperCase();
    
    // Shipment Status
    if (type === 'shipment') {
        const statusMap = {
            'PENDING': {
                text: 'Pending',
                color: 'bg-yellow-100 text-yellow-800',
                icon: 'fa-clock'
            },
            'PICKED_UP': {
                text: 'Picked Up',
                color: 'bg-blue-100 text-blue-800',
                icon: 'fa-box'
            },
            'IN_TRANSIT': {
                text: 'In Transit',
                color: 'bg-indigo-100 text-indigo-800',
                icon: 'fa-truck'
            },
            'OUT_FOR_DELIVERY': {
                text: 'Out for Delivery',
                color: 'bg-purple-100 text-purple-800',
                icon: 'fa-shipping-fast'
            },
            'DELIVERED': {
                text: 'Delivered',
                color: 'bg-green-100 text-green-800',
                icon: 'fa-check-circle'
            },
            'FAILED': {
                text: 'Failed',
                color: 'bg-red-100 text-red-800',
                icon: 'fa-exclamation-circle'
            },
            'CANCELLED': {
                text: 'Cancelled',
                color: 'bg-gray-100 text-gray-800',
                icon: 'fa-times-circle'
            },
            'RETURNED': {
                text: 'Returned',
                color: 'bg-reliable-yellow-100 text-reliable-yellow-800',
                icon: 'fa-undo'
            }
        };
        return statusMap[statusUpper] || {
            text: status,
            color: 'bg-gray-100 text-gray-800',
            icon: 'fa-circle'
        };
    }
    
    // Booking Status
    if (type === 'booking') {
        const statusMap = {
            'PENDING': {
                text: 'Pending',
                color: 'bg-yellow-100 text-yellow-800',
                icon: 'fa-clock'
            },
            'CONFIRMED': {
                text: 'Confirmed',
                color: 'bg-green-100 text-green-800',
                icon: 'fa-check'
            },
            'PAYMENT_PENDING': {
                text: 'Payment Pending',
                color: 'bg-reliable-yellow-100 text-reliable-yellow-800',
                icon: 'fa-credit-card'
            },
            'CANCELLED': {
                text: 'Cancelled',
                color: 'bg-red-100 text-red-800',
                icon: 'fa-times'
            },
            'COMPLETED': {
                text: 'Completed',
                color: 'bg-blue-100 text-blue-800',
                icon: 'fa-check-circle'
            }
        };
        return statusMap[statusUpper] || {
            text: status,
            color: 'bg-gray-100 text-gray-800',
            icon: 'fa-circle'
        };
    }
    
    // Driver Status
    if (type === 'driver') {
        const statusMap = {
            'ACTIVE': {
                text: 'Active',
                color: 'bg-green-100 text-green-800',
                icon: 'fa-check-circle'
            },
            'INACTIVE': {
                text: 'Inactive',
                color: 'bg-gray-100 text-gray-800',
                icon: 'fa-pause-circle'
            },
            'ON_DELIVERY': {
                text: 'On Delivery',
                color: 'bg-blue-100 text-blue-800',
                icon: 'fa-truck'
            },
            'OFF_DUTY': {
                text: 'Off Duty',
                color: 'bg-yellow-100 text-yellow-800',
                icon: 'fa-bed'
            },
            'SUSPENDED': {
                text: 'Suspended',
                color: 'bg-red-100 text-red-800',
                icon: 'fa-ban'
            }
        };
        return statusMap[statusUpper] || {
            text: status,
            color: 'bg-gray-100 text-gray-800',
            icon: 'fa-circle'
        };
    }
    
    // Verification Status
    if (type === 'verification') {
        const statusMap = {
            'PENDING': {
                text: 'Pending',
                color: 'bg-yellow-100 text-yellow-800',
                icon: 'fa-clock'
            },
            'UNDER_REVIEW': {
                text: 'Under Review',
                color: 'bg-blue-100 text-blue-800',
                icon: 'fa-eye'
            },
            'APPROVED': {
                text: 'Approved',
                color: 'bg-green-100 text-green-800',
                icon: 'fa-check-circle'
            },
            'VERIFIED': {
                text: 'Verified',
                color: 'bg-green-100 text-green-800',
                icon: 'fa-check-double'
            },
            'REJECTED': {
                text: 'Rejected',
                color: 'bg-red-100 text-red-800',
                icon: 'fa-times-circle'
            }
        };
        return statusMap[statusUpper] || {
            text: status,
            color: 'bg-gray-100 text-gray-800',
            icon: 'fa-circle'
        };
    }
    
    // Payment Status
    if (type === 'payment') {
        const statusMap = {
            'PENDING': {
                text: 'Pending',
                color: 'bg-yellow-100 text-yellow-800',
                icon: 'fa-clock'
            },
            'COMPLETED': {
                text: 'Completed',
                color: 'bg-green-100 text-green-800',
                icon: 'fa-check-circle'
            },
            'FAILED': {
                text: 'Failed',
                color: 'bg-red-100 text-red-800',
                icon: 'fa-exclamation-circle'
            },
            'REFUNDED': {
                text: 'Refunded',
                color: 'bg-reliable-yellow-100 text-reliable-yellow-800',
                icon: 'fa-undo'
            }
        };
        return statusMap[statusUpper] || {
            text: status,
            color: 'bg-gray-100 text-gray-800',
            icon: 'fa-circle'
        };
    }
    
    // Generic status
    return {
        text: status,
        color: 'bg-gray-100 text-gray-800',
        icon: 'fa-circle'
    };
}

/**
 * Get status badge HTML
 * @param {string} status - Status value
 * @param {string} type - Type of status
 * @returns {string} HTML string for status badge
 */
function getStatusBadge(status, type = 'shipment') {
    const statusInfo = formatStatus(status, type);
    return `
        <span class="px-2 py-1 inline-flex items-center text-xs font-semibold rounded-full ${statusInfo.color}">
            <i class="fas ${statusInfo.icon} mr-1"></i>
            ${statusInfo.text}
        </span>
    `;
}

/**
 * Format date for display
 * @param {string|Date} date - Date to format
 * @returns {string} Formatted date string
 */
function formatDate(date) {
    if (!date) return '-';
    const d = new Date(date);
    return d.toLocaleDateString('en-ZA', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Format date time for display (South African format)
 * @param {string|Date} date - Date to format
 * @returns {string} Formatted date time string
 */
function formatDateTime(date) {
    if (!date) return '-';
    const d = new Date(date);
    return d.toLocaleString('en-ZA', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Format relative time (e.g., "2 hours ago")
 * @param {string|Date} date - Date to format
 * @returns {string} Relative time string
 */
function formatRelativeTime(date) {
    if (!date) return '-';
    const d = new Date(date);
    const now = new Date();
    const diffMs = now - d;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins !== 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours !== 1 ? 's' : ''} ago`;
    if (diffDays < 7) return `${diffDays} day${diffDays !== 1 ? 's' : ''} ago`;
    return formatDate(date);
}

/**
 * Get role color badge
 * @param {string} role - User role
 * @returns {string} Color class string
 */
function getRoleColor(role) {
    const colors = {
        'ADMIN': 'bg-purple-100 text-purple-800',
        'DRIVER': 'bg-blue-100 text-blue-800',
        'CUSTOMER': 'bg-green-100 text-green-800',
        'BUSINESS': 'bg-indigo-100 text-indigo-800',
        'STAFF': 'bg-yellow-100 text-yellow-800',
        'TRACKING_MANAGER': 'bg-indigo-100 text-indigo-800',
        'SYSTEM': 'bg-gray-100 text-gray-800'
    };
    return colors[role] || 'bg-gray-100 text-gray-800';
}

/**
 * Format driver location for display
 * @param {object} location - Location object with latitude, longitude, address
 * @returns {string} Formatted location string
 */
function formatDriverLocation(location) {
    if (!location) return 'No location data';
    if (location.address) return location.address;
    if (location.latitude && location.longitude) {
        return `${location.latitude.toFixed(6)}, ${location.longitude.toFixed(6)}`;
    }
    return 'Location unavailable';
}

/**
 * Get map marker color based on status
 * @param {string} status - Driver or shipment status
 * @returns {string} Color code for map marker
 */
function getMapMarkerColor(status) {
    const statusUpper = (status || '').toUpperCase();
    const colorMap = {
        'ACTIVE': '#10B981', // green
        'ON_DELIVERY': '#3B82F6', // blue
        'IN_TRANSIT': '#6366F1', // indigo
        'OUT_FOR_DELIVERY': '#8B5CF6', // purple
        'PENDING': '#F59E0B', // yellow
        'INACTIVE': '#6B7280', // gray
        'OFF_DUTY': '#F97316', // orange
        'DELIVERED': '#10B981', // green
        'FAILED': '#EF4444' // red
    };
    return colorMap[statusUpper] || '#6B7280';
}

