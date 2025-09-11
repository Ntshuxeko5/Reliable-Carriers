# Moving Services and Load Transport System

## Overview
The Reliable Carriers system now includes a comprehensive moving services and load transport system with automatic pricing calculation based on distance. The system offers transparent pricing with a fixed base price for the first 20km and additional charges for longer distances.

## Pricing Structure

### Base Pricing Model
- **Base Price:** R550 for distances up to 20km
- **Additional Cost:** R25 per kilometer beyond 20km
- **Free Distance:** First 20km included in base price

### Pricing Examples
- **15km journey:** R550 (base price only)
- **25km journey:** R550 + (5km × R25) = R675
- **50km journey:** R550 + (30km × R25) = R1,300

## Service Types

### 1. Moving Service
- Complete home/office relocation services
- Professional moving team
- Packing and unpacking assistance
- Furniture protection

### 2. Load Transport
- Heavy load and equipment transportation
- Industrial equipment moving
- Construction material transport
- Machinery relocation

### 3. Express Delivery
- Fast delivery within 24 hours
- Priority handling
- Real-time tracking
- Guaranteed delivery time

### 4. Same Day Delivery
- Delivery on the same day
- Premium service
- Dedicated vehicle
- Priority scheduling

## System Features

### 🔧 **Core Functionality**
- **Automatic Price Calculation** - Real-time pricing based on distance
- **Distance Calculation** - Haversine formula for accurate distance measurement
- **Service Management** - Complete lifecycle management
- **Driver Assignment** - Automatic driver assignment and notifications
- **Status Tracking** - Real-time status updates
- **Notification System** - Email and SMS notifications for all events

### 📊 **Business Intelligence**
- **Service Statistics** - Comprehensive analytics and reporting
- **Revenue Tracking** - Financial performance monitoring
- **Performance Metrics** - Driver and service performance analysis
- **Customer Analytics** - Customer behavior and preferences

### 🔐 **Security & Access Control**
- **Role-based Access** - Different permissions for different user types
- **Customer Privacy** - Secure customer data handling
- **Driver Management** - Controlled driver access to service information

## API Endpoints

### Service Management

#### Create Moving Service
```http
POST /api/moving-services?customerId={customerId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "serviceType": "MOVING",
  "pickupAddress": "123 Main St, Johannesburg",
  "deliveryAddress": "456 Oak Ave, Pretoria",
  "distanceKm": 25.5,
  "description": "Moving furniture from apartment",
  "weightKg": 500.0,
  "numberOfItems": 15,
  "requestedDate": "2024-01-15",
  "scheduledDate": "2024-01-20T09:00:00",
  "specialInstructions": "Handle with care, fragile items"
}
```

#### Get Service by ID
```http
GET /api/moving-services/{id}
Authorization: Bearer <token>
```

#### Get All Services (Admin/Manager)
```http
GET /api/moving-services
Authorization: Bearer <token>
```

#### Get Customer Services
```http
GET /api/moving-services/customer/{customerId}
Authorization: Bearer <token>
```

#### Get Driver Services
```http
GET /api/moving-services/driver/{driverId}
Authorization: Bearer <token>
```

### Service Operations

#### Assign Driver
```http
POST /api/moving-services/{serviceId}/assign-driver?driverId={driverId}
Authorization: Bearer <token>
```

#### Update Status
```http
PUT /api/moving-services/{serviceId}/status?status=PICKED_UP&notes=Driver picked up items
Authorization: Bearer <token>
```

#### Schedule Service
```http
POST /api/moving-services/{serviceId}/schedule?scheduledDate=2024-01-20T09:00:00
Authorization: Bearer <token>
```

#### Complete Service
```http
POST /api/moving-services/{serviceId}/complete?completionNotes=All items delivered safely
Authorization: Bearer <token>
```

#### Cancel Service
```http
POST /api/moving-services/{serviceId}/cancel?cancellationReason=Customer requested cancellation
Authorization: Bearer <token>
```

### Pricing & Calculation

#### Calculate Price
```http
POST /api/moving-services/calculate-price?distanceKm=25.5
Authorization: Bearer <token>
```

**Response:**
```json
{
  "basePrice": 550.00,
  "extraDistanceCharge": 137.50,
  "totalPrice": 687.50,
  "distanceKm": 25.5,
  "maxFreeDistanceKm": 20,
  "pricePerKm": 25.00,
  "breakdown": "Base price for 20 km: R550.00 + Extra 5.5 km × R25.00/km = R137.50"
}
```

### Analytics & Reporting

#### Service Statistics
```http
GET /api/moving-services/statistics
Authorization: Bearer <token>
```

#### Revenue Statistics
```http
GET /api/moving-services/revenue?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer <token>
```

#### Top Services by Price
```http
GET /api/moving-services/top-price?limit=10
Authorization: Bearer <token>
```

#### Top Services by Distance
```http
GET /api/moving-services/top-distance?limit=10
Authorization: Bearer <token>
```

### Search & Filtering

#### Search Services
```http
GET /api/moving-services/search?query=Johannesburg
Authorization: Bearer <token>
```

#### Filter by Status
```http
GET /api/moving-services/status/PENDING
Authorization: Bearer <token>
```

#### Filter by Service Type
```http
GET /api/moving-services/type/MOVING
Authorization: Bearer <token>
```

#### Filter by Date Range
```http
GET /api/moving-services/date-range?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer <token>
```

#### Filter by Distance Range
```http
GET /api/moving-services/distance-range?minDistance=10&maxDistance=50
Authorization: Bearer <token>
```

#### Filter by Price Range
```http
GET /api/moving-services/price-range?minPrice=500&maxPrice=1000
Authorization: Bearer <token>
```

## Database Schema

### MovingService Entity
```java
@Entity
@Table(name = "moving_services")
public class MovingService {
    private Long id;
    private User customer;
    private User driver;
    private ServiceType serviceType;
    private String pickupAddress;
    private String deliveryAddress;
    private Double distanceKm;
    private BigDecimal basePrice;
    private BigDecimal totalPrice;
    private BigDecimal pricePerKm;
    private Integer maxFreeDistanceKm;
    private BigDecimal maxFreePrice;
    private String description;
    private Double weightKg;
    private Integer numberOfItems;
    private ShipmentStatus status;
    private Date requestedDate;
    private Date scheduledDate;
    private Date completedDate;
    private String specialInstructions;
    private Date createdAt;
    private Date updatedAt;
}
```

### ServiceType Enum
```java
public enum ServiceType {
    COURIER("Courier Service"),
    MOVING("Moving Service"),
    LOAD_TRANSPORT("Load Transport"),
    EXPRESS_DELIVERY("Express Delivery"),
    SAME_DAY_DELIVERY("Same Day Delivery");
}
```

## User Interface

### Web Interface
- **URL:** `http://localhost:8080/moving-services`
- **Access:** Customers and Admins
- **Features:**
  - Service booking form
  - Real-time price calculator
  - Service history
  - Status tracking

### Key Features
1. **Interactive Price Calculator** - Calculate prices before booking
2. **Service Type Selection** - Choose from different service types
3. **Address Input** - Pickup and delivery address forms
4. **Service Details** - Weight, items, description, special instructions
5. **Scheduling** - Requested and scheduled dates
6. **Service History** - View all past and current services

## Notification System

### Automatic Notifications
1. **Service Created** - Confirmation to customer
2. **Driver Assigned** - Notification to customer and driver
3. **Status Updates** - Real-time status change notifications
4. **Service Scheduled** - Schedule confirmation
5. **Service Completed** - Completion notification
6. **Service Cancelled** - Cancellation notification

### Notification Channels
- **Email** - Detailed notifications with service information
- **SMS** - Quick status updates and confirmations

## Integration with Existing Systems

### Tracking System
- Moving services integrate with the existing driver tracking system
- Real-time location updates for moving services
- Driver assignment and management

### Payment System
- Pricing calculation for payment processing
- Invoice generation based on calculated prices
- Payment status tracking

### User Management
- Customer and driver management
- Role-based access control
- User authentication and authorization

## Configuration

### Application Properties
```properties
# Moving Services Configuration
app.moving-services.base-price=550.00
app.moving-services.price-per-km=25.00
app.moving-services.max-free-distance=20
```

### Environment Variables
```bash
# Set these in your environment
MOVING_SERVICES_BASE_PRICE=550.00
MOVING_SERVICES_PRICE_PER_KM=25.00
MOVING_SERVICES_MAX_FREE_DISTANCE=20
```

## Testing

### API Testing
```bash
# Test price calculation
curl -X POST "http://localhost:8080/api/moving-services/calculate-price?distanceKm=25.5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test service creation
curl -X POST "http://localhost:8080/api/moving-services?customerId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "serviceType": "MOVING",
    "pickupAddress": "123 Main St, Johannesburg",
    "deliveryAddress": "456 Oak Ave, Pretoria",
    "distanceKm": 25.5,
    "description": "Moving furniture",
    "weightKg": 500.0,
    "numberOfItems": 15
  }'
```

### Web Interface Testing
1. Navigate to `http://localhost:8080/moving-services`
2. Login as a customer
3. Use the price calculator
4. Book a moving service
5. View service history

## Security Considerations

### Access Control
- **Customers** - Can create, view, and cancel their own services
- **Drivers** - Can view assigned services and update status
- **Admins** - Full access to all services and operations
- **Tracking Managers** - Can view and manage services

### Data Protection
- Customer information is protected
- Driver details are secured
- Service history is maintained securely
- Payment information is handled securely

## Monitoring & Analytics

### Key Metrics
- **Total Services** - Number of services created
- **Revenue** - Total revenue from moving services
- **Average Price** - Average service price
- **Service Types** - Distribution by service type
- **Status Distribution** - Services by status
- **Distance Analysis** - Average distance and pricing

### Performance Monitoring
- Service creation performance
- Price calculation speed
- Database query optimization
- API response times

## Future Enhancements

### Planned Features
1. **Real-time GPS Tracking** - Live tracking of moving vehicles
2. **Digital Signatures** - Electronic delivery confirmation
3. **Photo Documentation** - Before/after photos
4. **Insurance Integration** - Automatic insurance quotes
5. **Multi-stop Routes** - Multiple pickup/delivery points
6. **Equipment Tracking** - Track moving equipment and tools

### Advanced Analytics
1. **Predictive Pricing** - AI-based price optimization
2. **Route Optimization** - Optimal route planning
3. **Demand Forecasting** - Predict service demand
4. **Customer Segmentation** - Advanced customer analytics

## Support & Maintenance

### Troubleshooting
1. **Price Calculation Issues** - Check distance calculation
2. **Service Creation Errors** - Validate input data
3. **Notification Failures** - Check email/SMS configuration
4. **Performance Issues** - Monitor database queries

### Maintenance Tasks
1. **Database Optimization** - Regular index maintenance
2. **Log Analysis** - Monitor system logs
3. **Backup Management** - Regular data backups
4. **Security Updates** - Keep system updated

## Conclusion

The Moving Services and Load Transport system provides a comprehensive solution for managing moving and transport services with transparent pricing, automated calculations, and full integration with the existing Reliable Carriers platform. The system is designed to be scalable, secure, and user-friendly while providing powerful analytics and reporting capabilities.
