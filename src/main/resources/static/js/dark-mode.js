/**
 * Universal Dark Mode Toggle
 * This script provides dark mode functionality across all pages
 */

(function() {
    'use strict';

    // Initialize dark mode on page load
    function initDarkMode() {
        const body = document.body;
        const html = document.documentElement;
        const currentTheme = localStorage.getItem('theme') || 'light';
        
        if (currentTheme === 'dark') {
            body.classList.add('dark-mode');
            html.classList.add('dark-mode');
            // Ensure background color is set on html
            html.style.backgroundColor = '#0f172a';
            body.style.backgroundColor = '#0f172a';
        } else {
            body.classList.remove('dark-mode');
            html.classList.remove('dark-mode');
            // Reset background colors
            html.style.backgroundColor = '';
            body.style.backgroundColor = '';
        }
        
        // Update toggle button state if it exists
        updateToggleButton(currentTheme === 'dark');
        
        // Dispatch event for other scripts that might need to know theme changed
        window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme: currentTheme } }));
    }

    // Toggle dark mode
    function toggleDarkMode() {
        const body = document.body;
        const html = document.documentElement;
        const isDark = body.classList.contains('dark-mode');
        
        if (isDark) {
            body.classList.remove('dark-mode');
            html.classList.remove('dark-mode');
            // Reset background colors
            html.style.backgroundColor = '';
            body.style.backgroundColor = '';
            localStorage.setItem('theme', 'light');
            updateToggleButton(false);
            window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme: 'light' } }));
        } else {
            body.classList.add('dark-mode');
            html.classList.add('dark-mode');
            // Ensure background color is set on html
            html.style.backgroundColor = '#0f172a';
            body.style.backgroundColor = '#0f172a';
            localStorage.setItem('theme', 'dark');
            updateToggleButton(true);
            window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme: 'dark' } }));
        }
    }

    // Update toggle button appearance
    function updateToggleButton(isDark) {
        const darkModeToggle = document.getElementById('darkModeToggle');
        const toggleSlider = document.querySelector('#darkModeToggle + div > div:last-child, .dark-mode-toggle-slider');
        
        if (darkModeToggle) {
            darkModeToggle.checked = isDark;
        }
        
        if (toggleSlider) {
            if (isDark) {
                toggleSlider.style.transform = 'translateX(1.5rem)';
                toggleSlider.style.backgroundColor = '#3b82f6';
            } else {
                toggleSlider.style.transform = 'translateX(0)';
                toggleSlider.style.backgroundColor = '#ffffff';
            }
        }
    }

    // Set dark mode programmatically
    function setDarkMode(enabled) {
        const body = document.body;
        const html = document.documentElement;
        
        if (enabled) {
            body.classList.add('dark-mode');
            html.classList.add('dark-mode');
            // Ensure background color is set on html
            html.style.backgroundColor = '#0f172a';
            body.style.backgroundColor = '#0f172a';
            localStorage.setItem('theme', 'dark');
            updateToggleButton(true);
        } else {
            body.classList.remove('dark-mode');
            html.classList.remove('dark-mode');
            // Reset background colors
            html.style.backgroundColor = '';
            body.style.backgroundColor = '';
            localStorage.setItem('theme', 'light');
            updateToggleButton(false);
        }
        
        window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme: enabled ? 'dark' : 'light' } }));
    }

    // Get current theme
    function getCurrentTheme() {
        return document.body.classList.contains('dark-mode') ? 'dark' : 'light';
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initDarkMode);
    } else {
        initDarkMode();
    }

    // Attach toggle function to window for global access
    window.toggleDarkMode = toggleDarkMode;
    window.setDarkMode = setDarkMode;
    window.getCurrentTheme = getCurrentTheme;
    window.initDarkMode = initDarkMode;

    // Listen for toggle button clicks
    document.addEventListener('click', function(e) {
        const toggle = e.target.closest('#darkModeToggle, .dark-mode-toggle, [data-toggle="dark-mode"]');
        if (toggle) {
            e.preventDefault();
            toggleDarkMode();
        }
    });

    // Listen for changes from other scripts
    window.addEventListener('themeChanged', function(e) {
        // This can be used by other scripts to react to theme changes
        console.log('Theme changed to:', e.detail.theme);
    });
})();

