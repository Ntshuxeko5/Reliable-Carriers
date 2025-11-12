/**
 * Unified Google Maps Utility
 * Provides consistent Google Maps functionality across the application
 */
class UnifiedGoogleMaps {
    constructor(mapElementId, options = {}) {
        this.mapElementId = mapElementId;
        this.map = null;
        this.markers = {};
        this.infoWindows = {};
        this.bounds = null;
        this.options = {
            center: options.center || { lat: -26.2041, lng: 28.0473 }, // Default to Johannesburg
            zoom: options.zoom || 10,
            mapTypeId: options.mapTypeId || google.maps.MapTypeId.ROADMAP,
            ...options
        };
    }

    /**
     * Initialize the map
     */
    init() {
        const mapElement = document.getElementById(this.mapElementId);
        if (!mapElement) {
            console.error(`Map element with ID '${this.mapElementId}' not found`);
            return;
        }

        this.map = new google.maps.Map(mapElement, {
            zoom: this.options.zoom,
            center: this.options.center,
            mapTypeId: this.options.mapTypeId,
            styles: this.options.styles || [],
            disableDefaultUI: this.options.disableDefaultUI || false,
            zoomControl: this.options.zoomControl !== false,
            mapTypeControl: this.options.mapTypeControl !== false,
            scaleControl: this.options.scaleControl !== false,
            streetViewControl: this.options.streetViewControl !== false,
            fullscreenControl: this.options.fullscreenControl !== false
        });

        this.bounds = new google.maps.LatLngBounds();
        return this.map;
    }

    /**
     * Add a marker to the map
     */
    addMarker(id, position, options = {}) {
        if (!this.map) {
            console.error('Map not initialized. Call init() first.');
            return null;
        }

        const markerOptions = {
            position: position,
            map: this.map,
            title: options.title || '',
            icon: options.icon || null,
            animation: options.animation || null,
            draggable: options.draggable || false,
            ...options
        };

        const marker = new google.maps.Marker(markerOptions);
        
        // Add info window if content provided
        if (options.content) {
            const infoWindow = new google.maps.InfoWindow({
                content: options.content
            });

            marker.addListener('click', () => {
                // Close all other info windows
                Object.values(this.infoWindows).forEach(iw => iw.close());
                infoWindow.open(this.map, marker);
            });

            this.infoWindows[id] = infoWindow;
        }

        this.markers[id] = marker;
        
        // Extend bounds
        if (this.bounds) {
            this.bounds.extend(position);
        }

        return marker;
    }

    /**
     * Update marker position
     */
    updateMarker(id, position) {
        if (this.markers[id]) {
            this.markers[id].setPosition(position);
            if (this.bounds) {
                this.bounds.extend(position);
            }
        }
    }

    /**
     * Remove a marker
     */
    removeMarker(id) {
        if (this.markers[id]) {
            this.markers[id].setMap(null);
            delete this.markers[id];
        }
        if (this.infoWindows[id]) {
            delete this.infoWindows[id];
        }
    }

    /**
     * Clear all markers
     */
    clearMarkers() {
        Object.keys(this.markers).forEach(id => {
            this.removeMarker(id);
        });
        this.bounds = new google.maps.LatLngBounds();
    }

    /**
     * Fit map to show all markers
     */
    fitBounds() {
        if (this.map && this.bounds && !this.bounds.isEmpty()) {
            this.map.fitBounds(this.bounds);
        }
    }

    /**
     * Set map center
     */
    setCenter(position) {
        if (this.map) {
            this.map.setCenter(position);
        }
    }

    /**
     * Set map zoom
     */
    setZoom(zoom) {
        if (this.map) {
            this.map.setZoom(zoom);
        }
    }

    /**
     * Add a polyline (route)
     */
    addPolyline(path, options = {}) {
        if (!this.map) return null;

        const polylineOptions = {
            path: path,
            geodesic: options.geodesic !== false,
            strokeColor: options.strokeColor || '#FF0000',
            strokeOpacity: options.strokeOpacity || 1.0,
            strokeWeight: options.strokeWeight || 2,
            ...options
        };

        const polyline = new google.maps.Polyline(polylineOptions);
        polyline.setMap(this.map);
        return polyline;
    }

    /**
     * Get directions between two points
     */
    getDirections(origin, destination, callback) {
        if (!this.map) return;

        const directionsService = new google.maps.DirectionsService();
        const directionsRenderer = new google.maps.DirectionsRenderer({
            map: this.map,
            suppressMarkers: false
        });

        directionsService.route({
            origin: origin,
            destination: destination,
            travelMode: google.maps.TravelMode.DRIVING
        }, (result, status) => {
            if (status === 'OK') {
                directionsRenderer.setDirections(result);
                if (callback) callback(result);
            } else {
                console.error('Directions request failed:', status);
                if (callback) callback(null, status);
            }
        });

        return directionsRenderer;
    }

    /**
     * Get current map instance
     */
    getMap() {
        return this.map;
    }

    /**
     * Get marker by ID
     */
    getMarker(id) {
        return this.markers[id] || null;
    }

    /**
     * Get all markers
     */
    getAllMarkers() {
        return this.markers;
    }
}

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = UnifiedGoogleMaps;
}

