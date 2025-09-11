# Driver Workboard System

## Overview

The Driver Workboard System is a comprehensive package management solution that allows drivers to efficiently handle package pickups and deliveries with advanced features including signature capture, photo documentation, and distance-based package recommendations.

## Key Features

### 1. Package Management
- **Available Packages**: View packages available for pickup with distance-based recommendations
- **Assigned Packages**: Manage packages currently assigned to the driver
- **Package Pickup**: Request to pick up additional packages
- **Package Delivery**: Complete deliveries with proof of delivery

### 2. Signature Capture
- **Digital Signatures**: Capture recipient signatures using a signature pad
- **Signature Storage**: Store signatures as Base64 encoded data
- **Signature Verification**: Validate signatures before completing deliveries

### 3. Photo Documentation
- **Package Photos**: Take photos of packages during pickup
- **Delivery Photos**: Capture delivery location photos
- **Photo Storage**: Automatically save photos to the server
- **Photo Management**: Organize photos by package and delivery type

### 4. Distance-Based Recommendations
- **Location Tracking**: Real-time driver location updates
- **Nearby Packages**: Find packages within a specified radius
- **Route Optimization**: Get optimized delivery routes
- **Distance Calculations**: Calculate distances between locations

### 5. Workboard Statistics
- **Real-time Stats**: Live updates of package counts and earnings
- **Performance Metrics**: Track delivery performance and customer satisfaction
- **Earnings Summary**: Monitor daily, weekly, and monthly earnings
- **Route Analytics**: Analyze delivery routes and efficiency

## System Architecture

### Backend Components

#### 1. DriverWorkboardController
```java
@RestController
@RequestMapping("/api/driver/workboard")
@PreAuthorize("hasRole('DRIVER')")
public class DriverWorkboardController
```

**Key Endpoints:**
- `GET /stats` - Get workboard statistics
- `GET /available-packages` - Get available packages for pickup
- `GET /assigned-packages` - Get packages assigned to driver
- `GET /optimized-route` - Get optimized delivery route
- `POST /packages/{id}/pickup` - Pick up a package
- `POST /packages/{id}/deliver` - Deliver a package
- `GET /nearby-packages` - Find nearby packages
- `POST /location` - Update driver location

#### 2. DriverWorkboardService
```java
public interface DriverWorkboardService
```

**Key Methods:**
- `getWorkboardStats()` - Calculate comprehensive statistics
- `getAvailablePackagesForPickup()` - Find packages available for pickup
- `pickupPackage()` - Process package pickup with signature/photo
- `deliverPackage()` - Process package delivery with proof
- `calculateDistance()` - Calculate distance between coordinates
- `optimizeRoute()` - Optimize delivery route

#### 3. DTOs (Data Transfer Objects)

**PackagePickupRequest:**
```java
public class PackagePickupRequest {
    private Long driverId;
    private Long packageId;
    private String signature; // Base64 encoded
    private MultipartFile signaturePhoto;
    private MultipartFile packagePhoto;
    private String pickupNotes;
    private Double pickupLat;
    private Double pickupLng;
}
```

**PackageDeliveryRequest:**
```java
public class PackageDeliveryRequest {
    private Long driverId;
    private Long packageId;
    private String signature; // Base64 encoded
    private MultipartFile signaturePhoto;
    private MultipartFile deliveryPhoto;
    private String recipientName;
    private String recipientPhone;
    private String recipientIdNumber;
    private String deliveryNotes;
    private Double deliveryLat;
    private Double deliveryLng;
}
```

**WorkboardStats:**
```java
public class WorkboardStats {
    private Long totalPackages;
    private Long packagesToPickup;
    private Long packagesInVehicle;
    private Long packagesDeliveredToday;
    private Double totalWeight;
    private Double totalDistance;
    private BigDecimal todayEarnings;
    private String currentLocation;
    private Double currentLat;
    private Double currentLng;
}
```

### Frontend Components

#### 1. Workboard Dashboard (`/driver/workboard`)
- **Interactive Map**: Real-time package locations and driver position
- **Package Lists**: Available and assigned packages with distance information
- **Quick Actions**: Find nearby packages, optimized routes, today's summary
- **Statistics Cards**: Live updates of package counts and earnings

#### 2. Signature Capture Modal
- **Signature Pad**: HTML5 canvas-based signature capture
- **Clear/Save Options**: Easy signature management
- **Validation**: Ensure signatures are provided before proceeding

#### 3. Photo Capture Modal
- **Camera Access**: Direct camera integration for photo capture
- **Photo Preview**: Review captured photos before saving
- **Retake Option**: Easy photo retake functionality

## Database Schema

### Enhanced Tables

#### 1. ProofOfDelivery Table
```sql
CREATE TABLE proof_of_delivery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    delivery_date DATETIME NOT NULL,
    delivery_location VARCHAR(200) NOT NULL,
    recipient_signature TEXT, -- Base64 encoded signature
    delivery_photo_url VARCHAR(1000),
    package_photo_url VARCHAR(1000),
    recipient_name VARCHAR(500),
    recipient_phone VARCHAR(15),
    recipient_id_number VARCHAR(100),
    delivery_notes TEXT,
    delivery_method VARCHAR(20),
    signature_required BOOLEAN NOT NULL,
    photo_required BOOLEAN NOT NULL,
    id_verification_required BOOLEAN NOT NULL,
    delivery_status VARCHAR(20),
    failure_reason VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id),
    FOREIGN KEY (driver_id) REFERENCES users(id)
);
```

#### 2. DriverLocation Table
```sql
CREATE TABLE driver_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    driver_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    address VARCHAR(500),
    timestamp DATETIME NOT NULL,
    FOREIGN KEY (driver_id) REFERENCES users(id)
);
```

## API Endpoints

### Workboard Statistics
```http
GET /api/driver/workboard/stats?currentLat={lat}&currentLng={lng}
```

**Response:**
```json
{
    "totalPackages": 15,
    "packagesToPickup": 5,
    "packagesInVehicle": 8,
    "packagesDeliveredToday": 12,
    "totalWeight": 45.5,
    "totalDistance": 25.3,
    "estimatedTimeRemaining": 120,
    "todayEarnings": 600.00,
    "currentLocation": "123 Main St, Johannesburg",
    "currentLat": -26.2041,
    "currentLng": 28.0473,
    "driverStatus": "ONLINE"
}
```

### Available Packages
```http
GET /api/driver/workboard/available-packages?currentLat={lat}&currentLng={lng}&maxDistance=10.0&page=0&size=20
```

**Response:**
```json
[
    {
        "id": 1,
        "trackingNumber": "RC123456789",
        "recipientName": "John Doe",
        "recipientPhone": "+27123456789",
        "pickupAddress": "123 Pickup St",
        "pickupCity": "Johannesburg",
        "pickupState": "Gauteng",
        "deliveryAddress": "456 Delivery Ave",
        "deliveryCity": "Pretoria",
        "deliveryState": "Gauteng",
        "weight": 2.5,
        "description": "Electronics package",
        "status": "PENDING",
        "distanceFromCurrentLocation": 3.2
    }
]
```

### Package Pickup
```http
POST /api/driver/workboard/packages/{packageId}/pickup
Content-Type: multipart/form-data

signature: [Base64 encoded signature]
signaturePhoto: [file]
packagePhoto: [file]
pickupNotes: "Package picked up from front desk"
pickupLat: -26.2041
pickupLng: 28.0473
```

### Package Delivery
```http
POST /api/driver/workboard/packages/{packageId}/deliver
Content-Type: multipart/form-data

signature: [Base64 encoded signature]
signaturePhoto: [file]
deliveryPhoto: [file]
recipientName: "John Doe"
recipientPhone: "+27123456789"
recipientIdNumber: "8001015009087"
deliveryNotes: "Delivered to recipient"
deliveryLat: -25.7461
deliveryLng: 28.1881
```

## Usage Instructions

### For Drivers

#### 1. Accessing the Workboard
1. Log in to the driver dashboard
2. Click the "Workboard" button in the header
3. The workboard will load with current statistics and package information

#### 2. Finding Available Packages
1. The "Available Packages" section shows packages you can request to pick up
2. Packages are sorted by distance from your current location
3. Click on a package to request pickup

#### 3. Managing Assigned Packages
1. The "My Packages" section shows packages assigned to you
2. Click on a package to see pickup/delivery options
3. Follow the prompts to complete pickup or delivery

#### 4. Capturing Signatures
1. When prompted, click "Capture Signature"
2. Use your finger or stylus to sign on the signature pad
3. Click "Save Signature" to confirm

#### 5. Taking Photos
1. When prompted, click "Take Photo"
2. Allow camera access when requested
3. Position the camera and click "Capture"
4. Review the photo and click "Save" or "Retake"

#### 6. Using Quick Actions
- **Find Nearby Packages**: Discover packages within 5km of your location
- **Optimized Route**: Get the most efficient delivery route
- **Today's Summary**: View your daily performance metrics

### For Administrators

#### 1. Monitoring Driver Activity
- View real-time driver locations
- Track package pickup and delivery progress
- Monitor driver performance metrics

#### 2. Managing Package Assignments
- Assign packages to drivers manually
- Review pickup requests from drivers
- Monitor delivery completion rates

#### 3. Reviewing Proof of Delivery
- Access signature and photo documentation
- Verify delivery completion
- Handle failed delivery reports

## Security Features

### Authentication & Authorization
- All endpoints require driver authentication
- Role-based access control (DRIVER role required)
- Session management and timeout handling

### Data Protection
- Encrypted signature storage
- Secure photo upload and storage
- Location data privacy controls
- Audit trail for all actions

### Input Validation
- File type validation for photos
- Signature data validation
- Location coordinate validation
- Package ownership verification

## Performance Optimizations

### Database Optimization
- Indexed queries for location-based searches
- Efficient distance calculations
- Optimized route algorithms
- Cached statistics calculations

### Frontend Optimization
- Lazy loading of package data
- Efficient map rendering
- Optimized photo compression
- Responsive design for mobile devices

### API Optimization
- Pagination for large datasets
- Caching of frequently accessed data
- Asynchronous processing for heavy operations
- Rate limiting for API endpoints

## Error Handling

### Common Error Scenarios
1. **Location Services Disabled**: Prompt user to enable location
2. **Camera Access Denied**: Provide alternative photo upload
3. **Network Connectivity Issues**: Queue operations for retry
4. **Package Already Picked Up**: Show appropriate error message
5. **Invalid Signature**: Require signature before proceeding

### Error Response Format
```json
{
    "error": "PACKAGE_NOT_FOUND",
    "message": "Package with ID 123 not found",
    "timestamp": "2024-01-15T10:30:00Z",
    "details": "The requested package does not exist or has been removed"
}
```

## Future Enhancements

### Planned Features
1. **Offline Mode**: Work without internet connection
2. **Voice Commands**: Hands-free operation
3. **Barcode Scanning**: Quick package identification
4. **Real-time Chat**: Communication with dispatchers
5. **Advanced Analytics**: Detailed performance insights
6. **Integration APIs**: Third-party logistics platform integration

### Technical Improvements
1. **Machine Learning**: Predictive route optimization
2. **IoT Integration**: Smart package tracking
3. **Blockchain**: Immutable delivery records
4. **AR Navigation**: Augmented reality directions
5. **AI Photo Analysis**: Automatic damage detection

## Troubleshooting

### Common Issues

#### 1. Location Not Updating
- Check GPS permissions
- Ensure location services are enabled
- Try refreshing the page

#### 2. Camera Not Working
- Check camera permissions
- Ensure HTTPS connection (required for camera access)
- Try using a different browser

#### 3. Signature Not Saving
- Ensure signature pad is not empty
- Check internet connection
- Try clearing browser cache

#### 4. Package Not Loading
- Check internet connection
- Verify driver authentication
- Contact support if issue persists

### Support Contact
For technical support or feature requests, contact the development team at:
- Email: support@reliablecarriers.com
- Phone: +27 11 123 4567
- Support Hours: Monday-Friday 8:00 AM - 6:00 PM SAST

## Conclusion

The Driver Workboard System provides a comprehensive solution for efficient package management with advanced features for signature capture, photo documentation, and intelligent routing. The system is designed to improve driver productivity, enhance customer satisfaction, and provide detailed tracking and analytics for business optimization.
