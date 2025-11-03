/**
 * Currency Utilities for South African Rand (ZAR)
 * All prices displayed in South African Rand (R)
 */

/**
 * Format amount as South African Rand
 * @param {number|string} amount - The amount to format
 * @param {boolean} includeSymbol - Whether to include R symbol (default: true)
 * @returns {string} Formatted currency string (e.g., "R 1,234.56")
 */
function formatZAR(amount, includeSymbol = true) {
    if (amount === null || amount === undefined || amount === '') {
        return includeSymbol ? 'R 0.00' : '0.00';
    }
    
    const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
    
    if (isNaN(numAmount)) {
        return includeSymbol ? 'R 0.00' : '0.00';
    }
    
    // Format with thousands separator and 2 decimal places
    const formatted = numAmount.toLocaleString('en-ZA', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
    
    return includeSymbol ? `R ${formatted}` : formatted;
}

/**
 * Format amount with R symbol prefix (compact format)
 * @param {number|string} amount - The amount to format
 * @returns {string} Formatted currency string (e.g., "R1,234.56")
 */
function formatZARCompact(amount) {
    if (amount === null || amount === undefined || amount === '') {
        return 'R0.00';
    }
    
    const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
    
    if (isNaN(numAmount)) {
        return 'R0.00';
    }
    
    const formatted = numAmount.toLocaleString('en-ZA', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
    
    return `R${formatted}`;
}

/**
 * Format amount for input fields (without symbol)
 * @param {number|string} amount - The amount to format
 * @returns {string} Formatted number string (e.g., "1234.56")
 */
function formatAmountForInput(amount) {
    if (amount === null || amount === undefined || amount === '') {
        return '0.00';
    }
    
    const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
    
    if (isNaN(numAmount)) {
        return '0.00';
    }
    
    return numAmount.toFixed(2);
}

/**
 * Parse currency string to number
 * @param {string} currencyString - Currency string to parse (e.g., "R 1,234.56")
 * @returns {number} Parsed amount
 */
function parseZAR(currencyString) {
    if (!currencyString) {
        return 0;
    }
    
    // Remove R symbol, spaces, and commas
    const cleaned = currencyString.toString().replace(/[R\s,]/g, '');
    const parsed = parseFloat(cleaned);
    
    return isNaN(parsed) ? 0 : parsed;
}

/**
 * Get currency symbol
 * @returns {string} "R"
 */
function getCurrencySymbol() {
    return 'R';
}

/**
 * Get currency code
 * @returns {string} "ZAR"
 */
function getCurrencyCode() {
    return 'ZAR';
}

// Make functions available globally
window.formatZAR = formatZAR;
window.formatZARCompact = formatZARCompact;
window.formatAmountForInput = formatAmountForInput;
window.parseZAR = parseZAR;
window.getCurrencySymbol = getCurrencySymbol;
window.getCurrencyCode = getCurrencyCode;





