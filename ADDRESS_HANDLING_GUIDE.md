# Package Address Handling with Google Maps

## Overview

Since migrating from Leaflet to Google Maps, package addresses need to be properly geocoded (converted from text addresses to latitude/longitude coordinates) for map display and routing.

## Current State

### Database Schema

1. **Booking Entity** ✅
   - Has `pickupLatitude`, `pickupLongitude`, `deliveryLatitude`, `deliveryLongitude` fields
   - Coordinates are geocoded server-side when bookings are created via `BookingServiceImpl`
   - Uses `GoogleMapsGeocodingService` for geocoding

2. **Shipment Entity** ⚠️
   - Does NOT have latitude/longitude fields
   - Only stores address strings (pickupAddress, deliveryAddress, etc.)
   - Coordinates need to be geocoded on-demand

### Backend Services

1. **GoogleMapsGeocodingService**
   - Server-side geocoding service
   - Geocodes addresses when bookings are created
   - Used by `BookingServiceImpl` to populate coordinates

2. **UnifiedPackageService**
   - Merges data from Booking and Shipment entities
   - For Bookings: Uses stored coordinates
   - For Shipments: Coordinates are null (need client-side geocoding)

### Frontend Handling

1. **Address Geocoding Utility** (`address-geocoding.js`)
   - Client-side geocoding using Google Maps Geocoding API
   - Caches geocoded addresses to reduce API calls
   - Provides fallback when coordinates are missing

2. **Package Coordinate Resolution**
   - First: Use stored coordinates from DTO (`pickupLatitude`, `pickupLongitude`, etc.)
   - Fallback: Geocode address using client-side utility
   - Last resort: Use default Johannesburg coordinates (for demo/development)

## How It Works

### For Packages with Coordinates (Bookings)

```javascript
// Coordinates are already available in the DTO
const position = {
    lat: package.pickupLatitude,
    lng: package.pickupLongitude
};
```

### For Packages without Coordinates (Shipments)

```javascript
// Use client-side geocoding utility
addressGeocoding.getPackageCoordinates(package, 'pickup', (lat, lng) => {
    if (lat && lng) {
        const position = { lat: lat, lng: lng };
        // Add marker to map
    } else {
        console.warn('Failed to geocode address');
    }
});
```

## Implementation in Frontend

### Example: Driver Workboard

```javascript
// OLD (demo code - generates random coordinates)
function getPackageCoordinates(pkg) {
    return {
        lat: -26.2041 + (Math.random() - 0.5) * 0.1,
        lng: 28.0473 + (Math.random() - 0.5) * 0.1
    };
}

// NEW (uses actual coordinates or geocodes)
function getPackageCoordinates(pkg, callback) {
    // Check if coordinates are available
    if (pkg.pickupLatitude != null && pkg.pickupLongitude != null) {
        callback(pkg.pickupLatitude, pkg.pickupLongitude);
        return;
    }
    
    // Geocode address if coordinates missing
    addressGeocoding.getPackageCoordinates(pkg, 'pickup', callback);
}
```

## Best Practices

1. **Always check for stored coordinates first** - Reduces API calls and improves performance
2. **Use geocoding cache** - The utility caches results to avoid duplicate API calls
3. **Handle geocoding failures gracefully** - Show address text if geocoding fails
4. **Batch geocoding with delays** - When geocoding multiple addresses, add delays to respect rate limits

## Future Improvements

1. **Add coordinates to Shipment entity** - Store geocoded coordinates in database
2. **Background geocoding job** - Geocode Shipment addresses when they're created
3. **Coordinate validation** - Verify coordinates are still valid when addresses are updated
4. **Geocoding service endpoint** - Provide server-side geocoding API for frontend use

## Rate Limits

Google Maps Geocoding API has rate limits:
- Free tier: 40,000 requests per month
- Per-second limit: ~10 requests/second

The client-side utility includes:
- Caching to reduce duplicate requests
- Batch processing with delays
- Error handling for rate limit errors

## Migration Notes

When updating frontend code:
1. Replace demo coordinate generation with actual coordinate usage
2. Include `address-geocoding.js` script in pages that display packages
3. Initialize geocoding utility after Google Maps API loads
4. Use `getPackageCoordinates()` method for consistent coordinate resolution

