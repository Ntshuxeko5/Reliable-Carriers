/**
 * Password Strength Meter
 * Real-time password strength checking and visual feedback
 */

class PasswordStrengthMeter {
    constructor(passwordInputId, strengthIndicatorId, strengthTextId) {
        this.passwordInput = document.getElementById(passwordInputId);
        this.strengthIndicator = document.getElementById(strengthIndicatorId);
        this.strengthText = document.getElementById(strengthTextId);
        
        if (this.passwordInput) {
            this.passwordInput.addEventListener('input', () => this.checkStrength());
        }
    }
    
    checkStrength() {
        const password = this.passwordInput.value;
        const strength = this.calculateStrength(password);
        this.updateUI(strength);
        return strength;
    }
    
    calculateStrength(password) {
        if (!password || password.length < 8) {
            return { level: 'weak', score: 0, message: 'Too short (minimum 8 characters)' };
        }
        
        let score = 0;
        const checks = {
            length: password.length >= 8,
            length12: password.length >= 12,
            lowercase: /[a-z]/.test(password),
            uppercase: /[A-Z]/.test(password),
            digit: /[0-9]/.test(password),
            special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
        };
        
        // Score calculation
        if (checks.length) score++;
        if (checks.length12) score++;
        if (checks.lowercase) score++;
        if (checks.uppercase) score++;
        if (checks.digit) score++;
        if (checks.special) score++;
        
        // Determine strength level
        let level, message;
        if (score <= 2) {
            level = 'weak';
            message = 'Weak password. Add uppercase, numbers, and special characters.';
        } else if (score <= 4) {
            level = 'fair';
            message = 'Fair password. Consider adding more characters or special characters.';
        } else if (score <= 6) {
            level = 'good';
            message = 'Good password!';
        } else {
            level = 'strong';
            message = 'Strong password! ✓';
        }
        
        return { level, score, message, checks };
    }
    
    updateUI(strength) {
        if (!this.strengthIndicator || !this.strengthText) return;
        
        // Update strength bar
        this.strengthIndicator.className = 'password-strength-bar password-strength-' + strength.level;
        this.strengthIndicator.style.width = (strength.score / 6 * 100) + '%';
        
        // Update text
        this.strengthText.textContent = strength.message;
        this.strengthText.className = 'password-strength-text text-' + this.getTextColor(strength.level);
        
        // Update password input border color
        if (this.passwordInput) {
            this.passwordInput.classList.remove('border-red-500', 'border-yellow-500', 'border-blue-500', 'border-green-500');
            this.passwordInput.classList.add('border-' + this.getBorderColor(strength.level));
        }
    }
    
    getTextColor(level) {
        const colors = {
            weak: 'red-600',
            fair: 'yellow-600',
            good: 'blue-600',
            strong: 'green-600'
        };
        return colors[level] || 'gray-600';
    }
    
    getBorderColor(level) {
        const colors = {
            weak: 'red-500',
            fair: 'yellow-500',
            good: 'blue-500',
            strong: 'green-500'
        };
        return colors[level] || 'gray-300';
    }
}

// Initialize password strength meter when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        // Create strength indicator elements if they don't exist
        let strengthContainer = document.getElementById('passwordStrengthContainer');
        if (!strengthContainer) {
            strengthContainer = document.createElement('div');
            strengthContainer.id = 'passwordStrengthContainer';
            strengthContainer.className = 'mt-2';
            
            const strengthBarWrapper = document.createElement('div');
            strengthBarWrapper.className = 'w-full bg-gray-200 rounded-full h-2 mb-1';
            
            const strengthBar = document.createElement('div');
            strengthBar.id = 'passwordStrengthIndicator';
            strengthBar.className = 'password-strength-bar h-2 rounded-full transition-all duration-300';
            strengthBar.style.width = '0%';
            
            const strengthText = document.createElement('p');
            strengthText.id = 'passwordStrengthText';
            strengthText.className = 'text-xs';
            
            strengthBarWrapper.appendChild(strengthBar);
            strengthContainer.appendChild(strengthBarWrapper);
            strengthContainer.appendChild(strengthText);
            
            passwordInput.parentElement.appendChild(strengthContainer);
        }
        
        // Initialize the meter
        new PasswordStrengthMeter('password', 'passwordStrengthIndicator', 'passwordStrengthText');
    }
});
