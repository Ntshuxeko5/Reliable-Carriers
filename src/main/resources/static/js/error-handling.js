/**
 * Frontend Error Handling & Loading States Utility
 * Provides consistent error handling and loading states across all pages
 */

class ErrorHandler {
    constructor() {
        this.loadingOverlays = new Map();
        this.init();
    }

    init() {
        // Create global error container if it doesn't exist
        if (typeof document !== 'undefined') {
            if (!document.getElementById('global-error-container')) {
                const errorContainer = document.createElement('div');
                errorContainer.id = 'global-error-container';
                errorContainer.className = 'fixed top-4 right-4 z-50 space-y-2';
                document.body.appendChild(errorContainer);
            }

            // Create global loading overlay if it doesn't exist
            if (!document.getElementById('global-loading-overlay')) {
                const loadingOverlay = document.createElement('div');
                loadingOverlay.id = 'global-loading-overlay';
                loadingOverlay.className = 'fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center hidden';
                loadingOverlay.innerHTML = `
                    <div class="bg-white rounded-lg p-6 flex flex-col items-center">
                        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
                        <p class="text-gray-700 font-medium" id="loading-message">Loading...</p>
                    </div>
                `;
                document.body.appendChild(loadingOverlay);
            }
        }
    }

    showLoading(message = 'Loading...', elementId = null) {
        if (elementId && typeof document !== 'undefined') {
            const element = document.getElementById(elementId);
            if (element) {
                element.classList.add('loading');
                element.innerHTML = `
                    <div class="flex items-center justify-center p-4">
                        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mr-3"></div>
                        <span class="text-gray-700">${this.escapeHtml(message)}</span>
                    </div>
                `;
                this.loadingOverlays.set(elementId, element);
                return;
            }
        }

        if (typeof document !== 'undefined') {
            const overlay = document.getElementById('global-loading-overlay');
            const messageEl = document.getElementById('loading-message');
            if (overlay && messageEl) {
                messageEl.textContent = message;
                overlay.classList.remove('hidden');
            }
        }
    }

    hideLoading(elementId = null) {
        if (elementId && typeof document !== 'undefined') {
            const element = this.loadingOverlays.get(elementId);
            if (element) {
                element.classList.remove('loading');
                this.loadingOverlays.delete(elementId);
            }
            return;
        }

        if (typeof document !== 'undefined') {
            const overlay = document.getElementById('global-loading-overlay');
            if (overlay) {
                overlay.classList.add('hidden');
            }
        }
    }

    showError(message, duration = 5000) {
        if (typeof document === 'undefined') return;
        const container = document.getElementById('global-error-container');
        if (!container) return;

        const errorDiv = document.createElement('div');
        errorDiv.className = 'bg-red-50 border-l-4 border-red-500 p-4 rounded-lg shadow-lg max-w-md';
        errorDiv.innerHTML = `
            <div class="flex items-start">
                <div class="flex-shrink-0">
                    <i class="fas fa-exclamation-circle text-red-500"></i>
                </div>
                <div class="ml-3 flex-1">
                    <p class="text-sm font-medium text-red-800">${this.escapeHtml(message)}</p>
                </div>
                <button onclick="this.parentElement.parentElement.remove()" class="ml-4 text-red-400 hover:text-red-600">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        container.appendChild(errorDiv);

        if (duration > 0) {
            setTimeout(() => {
                if (errorDiv.parentElement) {
                    errorDiv.remove();
                }
            }, duration);
        }
    }

    showSuccess(message, duration = 3000) {
        if (typeof document === 'undefined') return;
        const container = document.getElementById('global-error-container');
        if (!container) return;

        const successDiv = document.createElement('div');
        successDiv.className = 'bg-green-50 border-l-4 border-green-500 p-4 rounded-lg shadow-lg max-w-md';
        successDiv.innerHTML = `
            <div class="flex items-start">
                <div class="flex-shrink-0">
                    <i class="fas fa-check-circle text-green-500"></i>
                </div>
                <div class="ml-3 flex-1">
                    <p class="text-sm font-medium text-green-800">${this.escapeHtml(message)}</p>
                </div>
                <button onclick="this.parentElement.parentElement.remove()" class="ml-4 text-green-400 hover:text-green-600">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        container.appendChild(successDiv);

        if (duration > 0) {
            setTimeout(() => {
                if (successDiv.parentElement) {
                    successDiv.remove();
                }
            }, duration);
        }
    }

    showWarning(message, duration = 4000) {
        if (typeof document === 'undefined') return;
        const container = document.getElementById('global-error-container');
        if (!container) return;

        const warningDiv = document.createElement('div');
        warningDiv.className = 'bg-yellow-50 border-l-4 border-yellow-500 p-4 rounded-lg shadow-lg max-w-md';
        warningDiv.innerHTML = `
            <div class="flex items-start">
                <div class="flex-shrink-0">
                    <i class="fas fa-exclamation-triangle text-yellow-500"></i>
                </div>
                <div class="ml-3 flex-1">
                    <p class="text-sm font-medium text-yellow-800">${this.escapeHtml(message)}</p>
                </div>
                <button onclick="this.parentElement.parentElement.remove()" class="ml-4 text-yellow-400 hover:text-yellow-600">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        container.appendChild(warningDiv);

        if (duration > 0) {
            setTimeout(() => {
                if (warningDiv.parentElement) {
                    warningDiv.remove();
                }
            }, duration);
        }
    }

    async handleApiError(error, defaultMessage = 'An error occurred', retryCallback = null) {
        let errorMessage = defaultMessage;
        let showRetry = false;

        if (error instanceof Response) {
            try {
                const errorData = await error.json();
                if (errorData.message) {
                    errorMessage = errorData.message;
                } else if (errorData.error) {
                    errorMessage = errorData.error;
                }
            } catch (e) {
                if (error.status === 401) {
                    errorMessage = 'You are not authorized. Please log in again.';
                    setTimeout(() => {
                        if (typeof window !== 'undefined') {
                            window.location.href = '/login';
                        }
                    }, 2000);
                } else if (error.status === 403) {
                    errorMessage = 'You do not have permission to perform this action.';
                } else if (error.status === 404) {
                    errorMessage = 'The requested resource was not found.';
                } else if (error.status >= 500) {
                    errorMessage = 'Server error. Please try again later.';
                    showRetry = retryCallback != null;
                } else {
                    errorMessage = error.statusText || defaultMessage;
                }
            }
        } else if (error instanceof Error) {
            if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
                errorMessage = 'Network error. Please check your internet connection and try again.';
                showRetry = retryCallback != null;
            } else {
                errorMessage = error.message || defaultMessage;
            }
        }

        if (showRetry && retryCallback) {
            this.showErrorWithRetry(errorMessage, retryCallback);
        } else {
            this.showError(errorMessage);
        }
    }

    showErrorWithRetry(message, retryCallback) {
        if (typeof document === 'undefined') return;
        const container = document.getElementById('global-error-container');
        if (!container) return;

        const errorDiv = document.createElement('div');
        errorDiv.className = 'bg-red-50 border-l-4 border-red-500 p-4 rounded-lg shadow-lg max-w-md';
        const callbackId = this.storeRetryCallback(retryCallback);
        errorDiv.innerHTML = `
            <div class="flex items-start">
                <div class="flex-shrink-0">
                    <i class="fas fa-exclamation-circle text-red-500"></i>
                </div>
                <div class="ml-3 flex-1">
                    <p class="text-sm font-medium text-red-800 mb-2">${this.escapeHtml(message)}</p>
                    <button onclick="errorHandler.retryAction(this)" 
                            class="text-sm bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition-colors"
                            data-retry-callback="${callbackId}">
                        <i class="fas fa-redo mr-1"></i> Retry
                    </button>
                </div>
                <button onclick="this.parentElement.parentElement.remove()" class="ml-4 text-red-400 hover:text-red-600">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        container.appendChild(errorDiv);
    }

    storeRetryCallback(callback) {
        const id = 'retry_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        if (typeof window !== 'undefined') {
            if (!window._retryCallbacks) {
                window._retryCallbacks = {};
            }
            window._retryCallbacks[id] = callback;
        }
        return id;
    }

    retryAction(button) {
        if (typeof window === 'undefined') return;
        const callbackId = button.getAttribute('data-retry-callback');
        if (window._retryCallbacks && window._retryCallbacks[callbackId]) {
            const callback = window._retryCallbacks[callbackId];
            delete window._retryCallbacks[callbackId];
            callback();
            const errorDiv = button.closest('.bg-red-50');
            if (errorDiv) {
                errorDiv.remove();
            }
        }
    }

    escapeHtml(text) {
        if (typeof document === 'undefined') return text;
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    async fetchWithErrorHandling(url, options = {}, loadingMessage = 'Loading...') {
        this.showLoading(loadingMessage);
        try {
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            });

            if (!response.ok) {
                await this.handleApiError(response, 'Request failed');
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return response;
        } catch (error) {
            await this.handleApiError(error, 'Network error occurred');
            throw error;
        } finally {
            this.hideLoading();
        }
    }
}

// Global instance
let errorHandler;
if (typeof window !== 'undefined') {
    errorHandler = new ErrorHandler();
}

// Convenience functions
function showLoading(message, elementId) {
    if (errorHandler) errorHandler.showLoading(message, elementId);
}

function hideLoading(elementId) {
    if (errorHandler) errorHandler.hideLoading(elementId);
}

function showError(message, duration) {
    if (errorHandler) errorHandler.showError(message, duration);
}

function showSuccess(message, duration) {
    if (errorHandler) errorHandler.showSuccess(message, duration);
}

function showWarning(message, duration) {
    if (errorHandler) errorHandler.showWarning(message, duration);
}

async function handleApiError(error, defaultMessage, retryCallback) {
    if (errorHandler) {
        return await errorHandler.handleApiError(error, defaultMessage, retryCallback);
    }
}

function fetchWithErrorHandling(url, options, loadingMessage) {
    if (errorHandler) {
        return errorHandler.fetchWithErrorHandling(url, options, loadingMessage);
    }
    return fetch(url, options);
}
