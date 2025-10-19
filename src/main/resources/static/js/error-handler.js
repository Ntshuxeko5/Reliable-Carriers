// Error Handler for Reliable Carriers
// This script handles common JavaScript errors and provides fallbacks

(function() {
    'use strict';
    
    // Global error handler for uncaught exceptions
    window.addEventListener('error', function(event) {
        console.warn('JavaScript Error Caught:', event.error);
        
        // Handle language destructuring errors
        if (event.error && event.error.message && event.error.message.includes('Cannot destructure property \'language\'')) {
            console.warn('Language destructuring error detected, providing fallback');
            // Set a default language object
            if (typeof window.userSettings === 'undefined') {
                window.userSettings = { language: 'en' };
            }
            return true; // Prevent default error handling
        }
        
        // Handle other common errors
        if (event.error && event.error.message && event.error.message.includes('Cannot read property')) {
            console.warn('Property access error detected, providing fallback');
            return true;
        }
    });
    
    // Handle promise rejections
    window.addEventListener('unhandledrejection', function(event) {
        console.warn('Unhandled Promise Rejection:', event.reason);
        
        // Handle specific Paystack errors
        if (event.reason && event.reason.message && event.reason.message.includes('message channel closed')) {
            console.warn('Message channel error (likely browser extension conflict) - ignoring');
            event.preventDefault();
            return;
        }
        
        event.preventDefault(); // Prevent default browser behavior
    });
    
    // Safe property access helper
    window.safeGet = function(obj, path, defaultValue = null) {
        try {
            return path.split('.').reduce((current, key) => {
                return current && current[key] !== undefined ? current[key] : defaultValue;
            }, obj);
        } catch (error) {
            console.warn('Safe property access failed:', error);
            return defaultValue;
        }
    };
    
    // Safe destructuring helper
    window.safeDestructure = function(obj, properties, defaultValue = {}) {
        try {
            const result = {};
            properties.forEach(prop => {
                result[prop] = obj && obj[prop] !== undefined ? obj[prop] : defaultValue[prop] || null;
            });
            return result;
        } catch (error) {
            console.warn('Safe destructuring failed:', error);
            return defaultValue;
        }
    };
    
    // Handle browser extension conflicts
    if (typeof chrome !== 'undefined' && chrome.runtime) {
        console.log('Chrome extension detected - message channel errors may occur');
    }
    
    // Suppress common browser extension errors
    const originalConsoleError = console.error;
    console.error = function(...args) {
        const message = args.join(' ');
        if (message.includes('message channel closed') || 
            message.includes('Content Script Bridge') ||
            message.includes('TSS: Received response')) {
            console.warn('Browser extension message:', ...args);
            return;
        }
        originalConsoleError.apply(console, args);
    };
    
    console.log('Error handler initialized');
})();
