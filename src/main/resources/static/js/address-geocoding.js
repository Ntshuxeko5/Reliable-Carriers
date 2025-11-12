/**
 * Address Geocoding Utility
 * Handles geocoding addresses to coordinates using Google Maps Geocoding API
 * Provides fallback for packages without stored coordinates
 */

class AddressGeocoding {
    constructor() {
        this.geocoder = null;
        this.geocodeCache = new Map(); // Cache geocoded addresses
    }

    /**
     * Initialize the geocoder (call after Google Maps API is loaded)
     */
    init() {
        if (typeof google !== 'undefined' && google.maps && google.maps.Geocoder) {
            this.geocoder = new google.maps.Geocoder();
        } else {
            console.warn('Google Maps Geocoder not available');
        }
    }

    /**
     * Geocode an address string to coordinates
     * @param {string} address - Full address string
     * @param {Function} callback - Callback function (lat, lng, formattedAddress)
     */
    geocodeAddress(address, callback) {
        if (!address || address.trim() === '') {
            callback(null, null, null);
            return;
        }

        // Check cache first
        const cached = this.geocodeCache.get(address);
        if (cached) {
            callback(cached.lat, cached.lng, cached.formattedAddress);
            return;
        }

        if (!this.geocoder) {
            this.init();
        }

        if (!this.geocoder) {
            console.warn('Geocoder not initialized');
            callback(null, null, null);
            return;
        }

        this.geocoder.geocode(
            {
                address: address,
                region: 'za' // Restrict to South Africa
            },
            (results, status) => {
                if (status === 'OK' && results && results.length > 0) {
                    const location = results[0].geometry.location;
                    const lat = location.lat();
                    const lng = location.lng();
                    const formattedAddress = results[0].formatted_address;

                    // Cache the result
                    this.geocodeCache.set(address, {
                        lat: lat,
                        lng: lng,
                        formattedAddress: formattedAddress
                    });

                    callback(lat, lng, formattedAddress);
                } else {
                    console.warn(`Geocoding failed for address: ${address}. Status: ${status}`);
                    callback(null, null, null);
                }
            }
        );
    }

    /**
     * Build full address string from address components
     * @param {Object} addressComponents - Object with address, city, state, zipCode, country
     * @returns {string} Full address string
     */
    buildFullAddress(addressComponents) {
        if (!addressComponents) return '';
        
        const parts = [];
        if (addressComponents.address) parts.push(addressComponents.address);
        if (addressComponents.city) parts.push(addressComponents.city);
        if (addressComponents.state) parts.push(addressComponents.state);
        if (addressComponents.zipCode || addressComponents.postalCode) {
            parts.push(addressComponents.zipCode || addressComponents.postalCode);
        }
        if (addressComponents.country) parts.push(addressComponents.country);

        return parts.join(', ');
    }

    /**
     * Get coordinates for a package (pickup or delivery)
     * Uses stored coordinates if available, otherwise geocodes the address
     * @param {Object} packageData - Package object with address and coordinate fields
     * @param {string} type - 'pickup' or 'delivery'
     * @param {Function} callback - Callback function (lat, lng)
     */
    getPackageCoordinates(packageData, type, callback) {
        if (!packageData) {
            callback(null, null);
            return;
        }

        const isPickup = type === 'pickup';
        const latField = isPickup ? 'pickupLatitude' : 'deliveryLatitude';
        const lngField = isPickup ? 'pickupLongitude' : 'deliveryLongitude';
        const addressField = isPickup ? 'pickupAddress' : 'deliveryAddress';
        const cityField = isPickup ? 'pickupCity' : 'deliveryCity';
        const stateField = isPickup ? 'pickupState' : 'deliveryState';
        const zipField = isPickup ? 'pickupZipCode' : 'deliveryZipCode';
        const countryField = isPickup ? 'pickupCountry' : 'deliveryCountry';

        // Use stored coordinates if available
        if (packageData[latField] != null && packageData[lngField] != null) {
            const lat = typeof packageData[latField] === 'number' 
                ? packageData[latField] 
                : parseFloat(packageData[latField]);
            const lng = typeof packageData[lngField] === 'number' 
                ? packageData[lngField] 
                : parseFloat(packageData[lngField]);
            
            if (!isNaN(lat) && !isNaN(lng)) {
                callback(lat, lng);
                return;
            }
        }

        // Build address string and geocode
        const addressComponents = {
            address: packageData[addressField],
            city: packageData[cityField],
            state: packageData[stateField],
            zipCode: packageData[zipField],
            country: packageData[countryField]
        };

        const fullAddress = this.buildFullAddress(addressComponents);
        
        if (!fullAddress) {
            console.warn(`No address available for ${type} location`);
            callback(null, null);
            return;
        }

        this.geocodeAddress(fullAddress, (lat, lng) => {
            callback(lat, lng);
        });
    }

    /**
     * Get coordinates for multiple packages (batch geocoding with delay)
     * @param {Array} packages - Array of package objects
     * @param {string} type - 'pickup' or 'delivery'
     * @param {Function} callback - Callback function (results: Array of {package, lat, lng})
     * @param {number} delay - Delay between geocoding requests (ms) - default 200ms to respect rate limits
     */
    getBatchCoordinates(packages, type, callback, delay = 200) {
        const results = [];
        let index = 0;

        const processNext = () => {
            if (index >= packages.length) {
                callback(results);
                return;
            }

            const pkg = packages[index];
            this.getPackageCoordinates(pkg, type, (lat, lng) => {
                results.push({
                    package: pkg,
                    lat: lat,
                    lng: lng
                });
                index++;
                setTimeout(processNext, delay);
            });
        };

        processNext();
    }
}

// Create global instance
const addressGeocoding = new AddressGeocoding();

// Auto-initialize when Google Maps is loaded
if (typeof google !== 'undefined' && google.maps) {
    addressGeocoding.init();
} else {
    // Wait for Google Maps to load
    window.addEventListener('load', () => {
        if (typeof google !== 'undefined' && google.maps) {
            addressGeocoding.init();
        }
    });
}

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AddressGeocoding;
}

