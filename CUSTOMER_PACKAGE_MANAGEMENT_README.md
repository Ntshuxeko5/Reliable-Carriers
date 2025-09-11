# Customer Package Management System

## Overview

The Customer Package Management System provides comprehensive package tracking, quote creation, and shipment management capabilities for customers without requiring accounts. This system allows customers to:

- Create quotes for package shipping
- Track packages using tracking numbers
- View all their packages (past, current, delivered)
- Manage packages by email or phone number
- Create multiple packages for different recipients (store functionality)
- Receive SMS and email notifications without accounts

## Key Features

### 1. Quote Creation System
- **No Account Required**: Customers can create quotes without registering
- **Multiple Service Options**: Economy, Standard, and Express shipping options
- **Real-time Pricing**: Dynamic pricing based on weight, distance, and service type
- **Quote Expiration**: Quotes are valid for 7 days
- **Service Recommendations**: System suggests optimal service based on package details

### 2. Package Tracking
- **Universal Tracking**: Track any package using tracking number
- **Real-time Updates**: Live tracking information with location updates
- **Status Tracking**: Complete shipment lifecycle tracking
- **Driver Information**: Access to assigned driver details
- **Delivery Estimates**: Accurate delivery date predictions

### 3. Customer Package Management
- **Email-based Access**: View all packages associated with an email address
- **Phone-based Access**: Alternative access using phone numbers
- **Status Filtering**: Filter packages by status (pending, in-transit, delivered, etc.)
- **Package History**: View complete shipping history
- **Search Functionality**: Search packages by various criteria

### 4. Store/Business Features
- **Multiple Package Creation**: Send packages to multiple recipients
- **Business Integration**: Support for business customers
- **Bulk Operations**: Efficient handling of multiple shipments
- **Business Analytics**: Package statistics and reporting

### 5. Notification System
- **SMS Notifications**: Real-time SMS updates
- **Email Notifications**: Detailed email tracking updates
- **No Account Required**: Notifications work without customer accounts
- **Customizable Preferences**: Control notification frequency and type

## API Endpoints

### Quote Management

#### Create Quote
```http
POST /api/customer/quote
Content-Type: application/json

{
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "senderPhone": "555-123-4567",
  "pickupAddress": "123 Main St",
  "pickupCity": "New York",
  "pickupState": "NY",
  "pickupZipCode": "10001",
  "pickupCountry": "USA",
  "recipientName": "Jane Smith",
  "recipientEmail": "jane@example.com",
  "recipientPhone": "555-987-6543",
  "deliveryAddress": "456 Oak Ave",
  "deliveryCity": "Los Angeles",
  "deliveryState": "CA",
  "deliveryZipCode": "90210",
  "deliveryCountry": "USA",
  "weight": 5.5,
  "dimensions": "12x8x6",
  "description": "Electronics package",
  "serviceType": "STANDARD"
}
```

#### Create Shipment from Quote
```http
POST /api/customer/quote/{quoteId}/create-shipment
Content-Type: application/json

{
  // Same structure as quote request
}
```

### Package Tracking

#### Track Package
```http
GET /api/customer/track/{trackingNumber}
```

#### Get Estimated Delivery Date
```http
GET /api/customer/track/{trackingNumber}/estimated-delivery
```

### Package Management

#### Get Packages by Email
```http
GET /api/customer/packages/email/{email}
```

#### Get Packages by Phone
```http
GET /api/customer/packages/phone/{phone}
```

#### Get Packages by Status
```http
GET /api/customer/packages/email/{email}/status/{status}
```

#### Get Delivered Packages
```http
GET /api/customer/packages/email/{email}/delivered
```

#### Get Current Packages
```http
GET /api/customer/packages/email/{email}/current
```

#### Get Pending Packages
```http
GET /api/customer/packages/email/{email}/pending
```

### Package History and Search

#### Get Package History
```http
GET /api/customer/packages/email/{email}/history?limit=10
```

#### Search Packages
```http
GET /api/customer/packages/email/{email}/search?searchTerm=electronics
```

#### Get Package Statistics
```http
GET /api/customer/packages/email/{email}/statistics
```

### Package Actions

#### Cancel Package
```http
PUT /api/customer/packages/{trackingNumber}/cancel?email={email}
```

#### Request Pickup
```http
POST /api/customer/packages/{trackingNumber}/pickup-request?email={email}&preferredDate={date}&notes={notes}
```

#### Update Tracking Preferences
```http
PUT /api/customer/packages/{trackingNumber}/tracking-preferences?emailNotifications=true&smsNotifications=true
```

### Store Operations

#### Create Multiple Packages
```http
POST /api/customer/store/multiple-packages?businessName=MyStore
Content-Type: application/json

[
  {
    "senderName": "Store Manager",
    "senderEmail": "store@example.com",
    "recipientName": "Customer 1",
    "recipientEmail": "customer1@example.com",
    // ... other package details
  },
  {
    "senderName": "Store Manager",
    "senderEmail": "store@example.com",
    "recipientName": "Customer 2",
    "recipientEmail": "customer2@example.com",
    // ... other package details
  }
]
```

### Insurance and Options

#### Get Insurance Options
```http
GET /api/customer/packages/{trackingNumber}/insurance-options
```

#### Add Insurance
```http
POST /api/customer/packages/{trackingNumber}/add-insurance?insuranceType=BASIC&amount=100.00
```

## Web Interface

The system provides a user-friendly web interface accessible at `/customer` with the following pages:

### Main Pages
- **Dashboard**: `/customer` - Overview and quick actions
- **Track Package**: `/customer/track` - Package tracking interface
- **Create Quote**: `/customer/quote` - Quote creation form
- **Manage Packages**: `/customer/packages` - View all packages by email
- **Package History**: `/customer/history` - Shipping history
- **Statistics**: `/customer/statistics` - Package analytics

### Action Pages
- **Cancel Package**: `/customer/cancel` - Package cancellation
- **Request Pickup**: `/customer/pickup` - Pickup scheduling
- **Insurance Options**: `/customer/insurance` - Insurance management
- **Store Operations**: `/customer/store` - Business package creation

### Support Pages
- **Help**: `/customer/help` - Help and FAQ
- **Contact**: `/customer/contact` - Contact information

## Data Models

### CustomerPackageRequest
```java
public class CustomerPackageRequest {
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String pickupAddress;
    private String pickupCity;
    private String pickupState;
    private String pickupZipCode;
    private String pickupCountry;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryZipCode;
    private String deliveryCountry;
    private Double weight;
    private String dimensions;
    private String description;
    private String serviceType;
    private Long customerId;
    private String businessName;
    private String businessId;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
}
```

### QuoteResponse
```java
public class QuoteResponse {
    private String quoteId;
    private String trackingNumber;
    private BigDecimal totalCost;
    private BigDecimal baseCost;
    private BigDecimal serviceFee;
    private BigDecimal insuranceFee;
    private BigDecimal fuelSurcharge;
    private ServiceType serviceType;
    private String estimatedDeliveryTime;
    private Date estimatedDeliveryDate;
    private List<ServiceOption> availableServices;
    private String pickupAddress;
    private String deliveryAddress;
    private Double weight;
    private String dimensions;
    private String description;
    private Date quoteExpiryDate;
    private boolean isActive;
}
```

### CustomerPackageResponse
```java
public class CustomerPackageResponse {
    private Long id;
    private String trackingNumber;
    private String senderName;
    private String senderEmail;
    private String recipientName;
    private String recipientEmail;
    private String pickupAddress;
    private String deliveryAddress;
    private Double weight;
    private String dimensions;
    private String description;
    private BigDecimal shippingCost;
    private String serviceType;
    private ShipmentStatus status;
    private String formattedStatus;
    private Date estimatedDeliveryDate;
    private Date actualDeliveryDate;
    private Date createdAt;
    private String driverName;
    private String driverPhone;
    private List<TrackingEvent> trackingEvents;
    private boolean isDelivered;
    private boolean isInTransit;
    private boolean isPending;
    private String currentLocation;
    private String lastUpdate;
}
```

## Service Types

### Available Service Types
1. **ECONOMY**: 5-7 business days, most economical
2. **STANDARD**: 3-5 business days, balanced option
3. **EXPRESS**: 1-2 business days, fastest delivery

### Service Features
- **Base Cost**: Calculated by weight and distance
- **Service Fee**: Additional fee based on service type
- **Fuel Surcharge**: Current fuel cost adjustment
- **Insurance Options**: Optional coverage levels

## Insurance Options

### Available Insurance Types
1. **BASIC**: $5.00 for up to $100 coverage
2. **STANDARD**: $15.00 for up to $500 coverage
3. **PREMIUM**: $25.00 for up to $1000 coverage

## Notification System

### SMS Notifications
- Package pickup confirmation
- In-transit updates
- Out for delivery alerts
- Delivery confirmation
- Failed delivery notifications

### Email Notifications
- Detailed tracking updates
- Delivery confirmations
- Status change notifications
- Pickup requests
- Cancellation confirmations

## Security Features

### Data Protection
- **Email Verification**: All email addresses are validated
- **Phone Validation**: Phone number format validation
- **Tracking Number Security**: Unique, non-guessable tracking numbers
- **Access Control**: Package access limited to sender/recipient

### Privacy Features
- **No Account Required**: Customers can use the system anonymously
- **Data Minimization**: Only necessary data is collected
- **Secure Storage**: All data is encrypted and securely stored
- **GDPR Compliance**: Full compliance with data protection regulations

## Business Integration

### Store Features
- **Bulk Package Creation**: Create multiple packages efficiently
- **Business Analytics**: Package statistics and reporting
- **Customer Management**: Track packages by business customer
- **Automated Notifications**: Bulk notification capabilities

### API Integration
- **RESTful API**: Standard REST endpoints
- **JSON Format**: All data in JSON format
- **CORS Support**: Cross-origin resource sharing enabled
- **Rate Limiting**: API rate limiting for security

## Error Handling

### Common Error Responses
```json
{
  "error": "Package not found",
  "code": "PACKAGE_NOT_FOUND",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Validation Errors
```json
{
  "error": "Invalid tracking number format",
  "field": "trackingNumber",
  "code": "INVALID_FORMAT"
}
```

## Performance Features

### Optimization
- **Caching**: Quote and package data caching
- **Database Indexing**: Optimized database queries
- **Pagination**: Large result set pagination
- **Async Processing**: Background notification processing

### Scalability
- **Horizontal Scaling**: Support for multiple instances
- **Load Balancing**: Distributed load handling
- **Database Sharding**: Large dataset management
- **CDN Integration**: Static content delivery

## Monitoring and Analytics

### System Monitoring
- **Health Checks**: `/api/customer/health`
- **Performance Metrics**: Response time tracking
- **Error Tracking**: Comprehensive error logging
- **Usage Analytics**: API usage statistics

### Business Analytics
- **Package Statistics**: Delivery success rates
- **Customer Analytics**: Usage patterns
- **Revenue Tracking**: Shipping cost analytics
- **Performance Metrics**: Service level monitoring

## Deployment

### Requirements
- **Java 17+**: Runtime environment
- **Spring Boot 3.x**: Application framework
- **PostgreSQL**: Database
- **Redis**: Caching (optional)
- **SMTP Server**: Email notifications
- **SMS Gateway**: SMS notifications

### Configuration
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/reliable_carriers
spring.datasource.username=postgres
spring.datasource.password=password

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=notifications@reliablecarriers.com
spring.mail.password=app_password

# SMS Configuration
twilio.account.sid=your_account_sid
twilio.auth.token=your_auth_token
twilio.phone.number=+1234567890

# Application Configuration
app.quote.expiry.days=7
app.tracking.number.prefix=RC
app.notification.enabled=true
```

## Testing

### API Testing
```bash
# Test quote creation
curl -X POST http://localhost:8080/api/customer/quote \
  -H "Content-Type: application/json" \
  -d @quote-request.json

# Test package tracking
curl -X GET http://localhost:8080/api/customer/track/RC12345678

# Test package management
curl -X GET http://localhost:8080/api/customer/packages/email/customer@example.com
```

### Integration Testing
- **Quote Creation**: End-to-end quote to shipment flow
- **Package Tracking**: Complete tracking lifecycle
- **Notification System**: SMS and email delivery testing
- **Store Operations**: Bulk package creation testing

## Future Enhancements

### Planned Features
1. **Mobile App**: Native iOS and Android applications
2. **Real-time Tracking**: GPS-based live tracking
3. **Advanced Analytics**: Machine learning insights
4. **International Shipping**: Global package delivery
5. **API Webhooks**: Real-time event notifications
6. **Multi-language Support**: Internationalization
7. **Advanced Insurance**: Custom coverage options
8. **Integration APIs**: Third-party system integration

### Technology Upgrades
1. **GraphQL API**: Alternative to REST API
2. **Microservices**: Service decomposition
3. **Event Sourcing**: Event-driven architecture
4. **Kubernetes**: Container orchestration
5. **Service Mesh**: Inter-service communication

## Support and Documentation

### Documentation
- **API Documentation**: Swagger/OpenAPI specification
- **User Guides**: Step-by-step usage instructions
- **Developer Guides**: Integration and development guides
- **Troubleshooting**: Common issues and solutions

### Support Channels
- **Email Support**: support@reliablecarriers.com
- **Phone Support**: 1-800-RELIABLE
- **Live Chat**: Available on website
- **Knowledge Base**: Self-service documentation

## Conclusion

The Customer Package Management System provides a comprehensive solution for package tracking and management without requiring customer accounts. The system is designed to be user-friendly, secure, and scalable, supporting both individual customers and business users. With its robust API, web interface, and notification system, it offers a complete package management experience.
