/**
 * Modern Animation Controller - Mind Blowing Effects
 * Handles scroll-triggered animations, interactive effects, and micro-interactions
 */

class ModernAnimations {
    constructor() {
        this.observers = new Map();
        this.particles = [];
        this.isInitialized = false;
        this.mousePosition = { x: 0, y: 0 };
        this.scrollPosition = 0;
        this.init();
    }

    init() {
        if (this.isInitialized) return;
        
        this.setupScrollObserver();
        this.setupMouseTracking();
        this.setupParallaxEffects();
        this.setupInteractiveCards();
        this.setupParticleSystem();
        this.setupTypewriterEffects();
        this.setupLoadingAnimations();
        this.setupSmoothScrolling();
        
        this.isInitialized = true;
        console.log('🚀 Modern Animations Initialized');
    }

    // ===== SCROLL TRIGGERED ANIMATIONS =====
    setupScrollObserver() {
        const options = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    this.animateOnScroll(entry.target);
                }
            });
        }, options);

        // Observe all elements with animation classes
        const animatedElements = document.querySelectorAll(
            '.animate-on-scroll, .scroll-trigger, .slide-in-left, .slide-in-right, .slide-in-up, .slide-in-down, .fade-in-scale, .fade-in-rotate'
        );

        animatedElements.forEach(el => {
            observer.observe(el);
        });

        this.observers.set('scroll', observer);
    }

    animateOnScroll(element) {
        const classes = element.className.split(' ');
        
        // Add visible class for CSS transitions
        if (classes.includes('animate-on-scroll') || classes.includes('scroll-trigger')) {
            element.classList.add('visible', 'triggered');
        }

        // Add staggered delays for child elements
        const children = element.querySelectorAll('[class*="delay-"]');
        children.forEach(child => {
            const delayClass = Array.from(child.classList).find(cls => cls.startsWith('delay-'));
            if (delayClass) {
                const delay = delayClass.replace('delay-', '') * 100;
                setTimeout(() => {
                    child.style.opacity = '1';
                    child.style.transform = 'translateY(0)';
                }, delay);
            }
        });
    }

    // ===== MOUSE TRACKING FOR INTERACTIVE EFFECTS =====
    setupMouseTracking() {
        document.addEventListener('mousemove', (e) => {
            this.mousePosition.x = e.clientX;
            this.mousePosition.y = e.clientY;
            
            // Update CSS custom properties for interactive cards
            document.documentElement.style.setProperty('--mouse-x', `${e.clientX}px`);
            document.documentElement.style.setProperty('--mouse-y', `${e.clientY}px`);
        });
    }

    // ===== PARALLAX EFFECTS =====
    setupParallaxEffects() {
        const parallaxElements = document.querySelectorAll('.parallax-element');
        
        window.addEventListener('scroll', () => {
            this.scrollPosition = window.pageYOffset;
            
            parallaxElements.forEach(element => {
                const speed = element.dataset.speed || 0.5;
                const yPos = -(this.scrollPosition * speed);
                element.style.transform = `translateY(${yPos}px)`;
            });
        });
    }

    // ===== INTERACTIVE CARDS =====
    setupInteractiveCards() {
        const interactiveCards = document.querySelectorAll('.interactive-card');
        
        interactiveCards.forEach(card => {
            card.addEventListener('mouseenter', (e) => {
                this.addFloatingEffect(card);
            });
            
            card.addEventListener('mouseleave', (e) => {
                this.removeFloatingEffect(card);
            });
        });
    }

    addFloatingEffect(element) {
        element.style.transform = 'translateY(-10px) scale(1.02)';
        element.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.15)';
    }

    removeFloatingEffect(element) {
        element.style.transform = 'translateY(0) scale(1)';
        element.style.boxShadow = '';
    }

    // ===== PARTICLE SYSTEM =====
    setupParticleSystem() {
        const particleContainers = document.querySelectorAll('.particle-container');
        
        particleContainers.forEach(container => {
            this.createParticles(container);
        });
    }

    createParticles(container) {
        const particleCount = parseInt(container.dataset.particles) || 20;
        
        for (let i = 0; i < particleCount; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';
            particle.style.left = Math.random() * 100 + '%';
            particle.style.animationDelay = Math.random() * 3 + 's';
            particle.style.animationDuration = (Math.random() * 2 + 2) + 's';
            
            container.appendChild(particle);
            this.particles.push(particle);
        }
    }

    // ===== TYPEWRITER EFFECTS =====
    setupTypewriterEffects() {
        const typewriterElements = document.querySelectorAll('.typewriter');
        
        typewriterElements.forEach(element => {
            const text = element.textContent;
            element.textContent = '';
            element.style.width = '0';
            
            let i = 0;
            const typeInterval = setInterval(() => {
                if (i < text.length) {
                    element.textContent += text.charAt(i);
                    element.style.width = ((i + 1) / text.length * 100) + '%';
                    i++;
                } else {
                    clearInterval(typeInterval);
                }
            }, 100);
        });
    }

    // ===== LOADING ANIMATIONS =====
    setupLoadingAnimations() {
        // Show loading animation for async operations
        this.showLoading = (container) => {
            const loader = document.createElement('div');
            loader.className = 'loading-overlay';
            loader.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner-ring"></div>
                    <div class="spinner-ring"></div>
                    <div class="spinner-ring"></div>
                </div>
            `;
            container.appendChild(loader);
        };

        this.hideLoading = (container) => {
            const loader = container.querySelector('.loading-overlay');
            if (loader) {
                loader.remove();
            }
        };
    }

    // ===== SMOOTH SCROLLING =====
    setupSmoothScrolling() {
        const smoothScrollLinks = document.querySelectorAll('a[href^="#"]');
        
        smoothScrollLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const targetId = link.getAttribute('href');
                const targetElement = document.querySelector(targetId);
                
                if (targetElement) {
                    targetElement.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    }

    // ===== UTILITY METHODS =====
    
    // Add floating animation to element
    addFloat(element, duration = 6000) {
        element.classList.add('float');
        element.style.animationDuration = duration + 'ms';
    }

    // Add pulse glow effect
    addPulseGlow(element) {
        element.classList.add('pulse-glow');
    }

    // Add morphing effect
    addMorph(element, fast = false) {
        element.classList.add(fast ? 'morph-fast' : 'morph');
    }

    // Create ripple effect on click
    createRipple(event) {
        const button = event.currentTarget;
        const ripple = document.createElement('span');
        const rect = button.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = event.clientX - rect.left - size / 2;
        const y = event.clientY - rect.top - size / 2;
        
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';
        ripple.classList.add('ripple');
        
        button.appendChild(ripple);
        
        setTimeout(() => {
            ripple.remove();
        }, 600);
    }

    // Animate counter
    animateCounter(element, target, duration = 2000) {
        const start = 0;
        const increment = target / (duration / 16);
        let current = start;
        
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            element.textContent = Math.floor(current);
        }, 16);
    }

    // Add staggered animation to list items
    addStaggeredAnimation(container, className = 'slide-in-up') {
        const items = container.children;
        Array.from(items).forEach((item, index) => {
            item.classList.add(className, `delay-${index * 100}`);
        });
    }

    // Create confetti effect
    createConfetti(container, count = 50) {
        const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe'];
        
        for (let i = 0; i < count; i++) {
            const confetti = document.createElement('div');
            confetti.className = 'confetti';
            confetti.style.left = Math.random() * 100 + '%';
            confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            confetti.style.animationDelay = Math.random() * 3 + 's';
            confetti.style.animationDuration = (Math.random() * 2 + 2) + 's';
            
            container.appendChild(confetti);
            
            setTimeout(() => {
                confetti.remove();
            }, 5000);
        }
    }

    // Add 3D tilt effect
    add3DTilt(element) {
        element.addEventListener('mousemove', (e) => {
            const rect = element.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;
            
            element.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
        });
        
        element.addEventListener('mouseleave', () => {
            element.style.transform = 'perspective(1000px) rotateX(0) rotateY(0)';
        });
    }

    // Add magnetic effect
    addMagneticEffect(element) {
        element.addEventListener('mousemove', (e) => {
            const rect = element.getBoundingClientRect();
            const x = e.clientX - rect.left - rect.width / 2;
            const y = e.clientY - rect.top - rect.height / 2;
            
            element.style.transform = `translate(${x * 0.1}px, ${y * 0.1}px)`;
        });
        
        element.addEventListener('mouseleave', () => {
            element.style.transform = 'translate(0, 0)';
        });
    }

    // Destroy all animations
    destroy() {
        this.observers.forEach(observer => {
            observer.disconnect();
        });
        this.observers.clear();
        this.particles = [];
        this.isInitialized = false;
    }
}

// Initialize animations when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.modernAnimations = new ModernAnimations();
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ModernAnimations;
}
