/**
 * Input Validation & Sanitization Utility
 * Provides consistent input validation and XSS protection
 */

class InputValidator {
    constructor() {
        this.init();
    }

    init() {
        // Auto-sanitize all form inputs on submit
        if (typeof document !== 'undefined') {
            document.addEventListener('DOMContentLoaded', () => {
                const forms = document.querySelectorAll('form');
                forms.forEach(form => {
                    form.addEventListener('submit', (e) => {
                        if (!this.validateForm(form)) {
                            e.preventDefault();
                            return false;
                        }
                    });
                });
            });
        }
    }

    validateEmail(email) {
        if (!email || typeof email !== 'string') return false;
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email.trim());
    }

    validatePhone(phone) {
        if (!phone || typeof phone !== 'string') return false;
        const cleaned = phone.replace(/[^\d+]/g, '');
        const phoneRegex = /^(\+?\d{10,15})$/;
        return phoneRegex.test(cleaned);
    }

    validateSAPhone(phone) {
        if (!phone || typeof phone !== 'string') return false;
        const cleaned = phone.replace(/[^\d+]/g, '');
        const saRegex = /^(\+27|0)[1-9]\d{8}$/;
        return saRegex.test(cleaned);
    }

    sanitizeHtml(input) {
        if (!input || typeof input !== 'string') return '';
        const div = document.createElement('div');
        div.textContent = input;
        return div.innerHTML;
    }

    sanitizeAttribute(input) {
        if (!input || typeof input !== 'string') return '';
        return input
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#x27;')
            .replace(/\//g, '&#x2F;');
    }

    validateAddress(address) {
        if (!address || typeof address !== 'string') return false;
        const trimmed = address.trim();
        return trimmed.length >= 5 && /[a-zA-Z]/.test(trimmed);
    }

    validatePostalCode(code) {
        if (!code || typeof code !== 'string') return false;
        const postalRegex = /^[A-Za-z0-9]{3,10}$/;
        return postalRegex.test(code.trim());
    }

    validatePassword(password) {
        if (!password || typeof password !== 'string') {
            return { valid: false, strength: 'weak', message: 'Password is required' };
        }

        const minLength = 8;
        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasNumber = /\d/.test(password);
        const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

        if (password.length < minLength) {
            return { valid: false, strength: 'weak', message: `Password must be at least ${minLength} characters` };
        }

        const criteria = [hasUpperCase, hasLowerCase, hasNumber, hasSpecial];
        const metCriteria = criteria.filter(Boolean).length;

        if (metCriteria < 2) {
            return { valid: false, strength: 'weak', message: 'Password must contain at least 2 of: uppercase, lowercase, number, special character' };
        }

        if (metCriteria === 2) {
            return { valid: true, strength: 'medium', message: 'Password strength: Medium' };
        }

        if (metCriteria === 3) {
            return { valid: true, strength: 'strong', message: 'Password strength: Strong' };
        }

        return { valid: true, strength: 'very-strong', message: 'Password strength: Very Strong' };
    }

    validateFile(file, options = {}) {
        const {
            maxSize = 10 * 1024 * 1024,
            allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'],
            allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.pdf']
        } = options;

        if (!file) {
            return { valid: false, message: 'No file selected' };
        }

        if (file.size > maxSize) {
            const maxSizeMB = (maxSize / (1024 * 1024)).toFixed(2);
            return { valid: false, message: `File size exceeds ${maxSizeMB}MB limit` };
        }

        if (!allowedTypes.includes(file.type)) {
            return { valid: false, message: 'Invalid file type. Allowed: ' + allowedExtensions.join(', ') };
        }

        const fileName = file.name.toLowerCase();
        const hasValidExtension = allowedExtensions.some(ext => fileName.endsWith(ext));
        if (!hasValidExtension) {
            return { valid: false, message: 'Invalid file extension' };
        }

        return { valid: true, message: 'File is valid' };
    }

    validateForm(form) {
        const inputs = form.querySelectorAll('input[required], textarea[required], select[required]');
        let isValid = true;
        const errors = [];

        inputs.forEach(input => {
            const value = input.value.trim();
            
            if (input.hasAttribute('required') && !value) {
                isValid = false;
                errors.push(`${input.name || input.id || 'Field'} is required`);
                input.classList.add('border-red-500');
            } else {
                input.classList.remove('border-red-500');
            }

            if (value) {
                if (input.type === 'email' && !this.validateEmail(value)) {
                    isValid = false;
                    errors.push('Invalid email address');
                    input.classList.add('border-red-500');
                } else if (input.type === 'tel' && !this.validatePhone(value)) {
                    isValid = false;
                    errors.push('Invalid phone number');
                    input.classList.add('border-red-500');
                } else if (input.type === 'password' && input.name.includes('password')) {
                    const passwordValidation = this.validatePassword(value);
                    if (!passwordValidation.valid) {
                        isValid = false;
                        errors.push(passwordValidation.message);
                        input.classList.add('border-red-500');
                    }
                }
            }
        });

        if (!isValid && errors.length > 0) {
            if (typeof showError === 'function') {
                showError(errors[0]);
            } else if (typeof alert !== 'undefined') {
                alert(errors[0]);
            }
        }

        return isValid;
    }

    sanitizeForm(form) {
        const inputs = form.querySelectorAll('input[type="text"], input[type="email"], textarea');
        inputs.forEach(input => {
            if (input.value) {
                input.value = this.sanitizeHtml(input.value);
            }
        });
    }
}

// Global instance
const inputValidator = new InputValidator();

// Convenience functions
function validateEmail(email) {
    return inputValidator.validateEmail(email);
}

function validatePhone(phone) {
    return inputValidator.validatePhone(phone);
}

function validateSAPhone(phone) {
    return inputValidator.validateSAPhone(phone);
}

function sanitizeHtml(input) {
    return inputValidator.sanitizeHtml(input);
}

function sanitizeAttribute(input) {
    return inputValidator.sanitizeAttribute(input);
}

function validateAddress(address) {
    return inputValidator.validateAddress(address);
}

function validatePostalCode(code) {
    return inputValidator.validatePostalCode(code);
}

function validatePassword(password) {
    return inputValidator.validatePassword(password);
}

function validateFile(file, options) {
    return inputValidator.validateFile(file, options);
}

function validateForm(form) {
    return inputValidator.validateForm(form);
}

function sanitizeForm(form) {
    return inputValidator.sanitizeForm(form);
}

