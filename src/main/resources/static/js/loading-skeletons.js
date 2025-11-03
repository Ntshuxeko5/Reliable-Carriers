/**
 * Loading Skeleton Utilities
 * Creates and manages loading skeleton screens
 */

class LoadingSkeleton {
    static createCardSkeleton(count = 3) {
        return Array(count).fill(0).map(() => `
            <div class="skeleton-card">
                <div class="skeleton skeleton-title"></div>
                <div class="skeleton skeleton-text"></div>
                <div class="skeleton skeleton-text" style="width: 80%"></div>
            </div>
        `).join('');
    }

    static createTableSkeleton(rows = 5, cols = 4) {
        return `
            <div class="skeleton-table">
                ${Array(rows).fill(0).map(() => `
                    <div class="skeleton-table-row">
                        ${Array(cols).fill(0).map(() => `
                            <div class="skeleton skeleton-table-cell"></div>
                        `).join('')}
                    </div>
                `).join('')}
            </div>
        `;
    }

    static createListSkeleton(count = 5) {
        return Array(count).fill(0).map(() => `
            <div class="skeleton-list-item">
                <div class="skeleton skeleton-avatar"></div>
                <div class="skeleton-list-item-content">
                    <div class="skeleton skeleton-text"></div>
                    <div class="skeleton skeleton-text" style="width: 60%; margin-top: 0.5rem"></div>
                </div>
            </div>
        `).join('');
    }

    static createStatCardsSkeleton(count = 4) {
        return Array(count).fill(0).map(() => `
            <div class="skeleton-stat-card">
                <div class="skeleton skeleton-stat-value"></div>
                <div class="skeleton skeleton-stat-label"></div>
            </div>
        `).join('');
    }

    static show(elementId, type = 'card', options = {}) {
        const element = document.getElementById(elementId);
        if (!element) return;

        let skeletonHtml = '';
        switch (type) {
            case 'card':
                skeletonHtml = this.createCardSkeleton(options.count || 3);
                break;
            case 'table':
                skeletonHtml = this.createTableSkeleton(options.rows || 5, options.cols || 4);
                break;
            case 'list':
                skeletonHtml = this.createListSkeleton(options.count || 5);
                break;
            case 'stats':
                skeletonHtml = this.createStatCardsSkeleton(options.count || 4);
                break;
            default:
                skeletonHtml = this.createCardSkeleton(3);
        }

        element.innerHTML = skeletonHtml;
        element.classList.add('loading-skeleton');
    }

    static hide(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.remove('loading-skeleton');
        }
    }
}

// Auto-show skeletons for elements with data-skeleton attribute
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('[data-skeleton]').forEach(element => {
        const type = element.getAttribute('data-skeleton');
        const count = parseInt(element.getAttribute('data-skeleton-count')) || 3;
        LoadingSkeleton.show(element.id, type, { count });
    });
});
