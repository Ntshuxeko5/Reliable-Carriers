# Driver Vehicle Integration - Car Make and Model Details

## Overview

The Reliable Carriers application now includes comprehensive vehicle information for drivers, including car make and model details. This enhancement provides complete visibility into the fleet and driver-vehicle assignments.

## Features Added

### 1. Enhanced Driver Information
- **Complete Vehicle Details**: Make, model, year, registration number
- **Vehicle Specifications**: Color, fuel type, capacity, mileage
- **Maintenance Information**: Last and next maintenance dates
- **Status Tracking**: Online/offline status, current location

### 2. Vehicle Information in All Driver Responses
- **DriverResponse DTO**: Comprehensive driver and vehicle information
- **DriverLocationResponse**: Updated to include vehicle make
- **CustomerPackageResponse**: Vehicle details for assigned drivers
- **Tracking Responses**: Vehicle information in tracking data

### 3. Advanced Search and Filtering
- Search drivers by vehicle make and model
- Filter drivers by vehicle type, capacity, year range
- Find drivers by vehicle registration number
- Get drivers with vehicles due for maintenance

## Database Schema Updates

### Vehicle Table Enhancements
```sql
-- Enhanced vehicle information
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type VARCHAR(50) NOT NULL,
    make VARCHAR(50) NOT NULL,           -- Vehicle make (e.g., Toyota, Ford)
    model VARCHAR(50) NOT NULL,          -- Vehicle model (e.g., Hiace, Transit)
    year INT NOT NULL,                   -- Manufacturing year
    license_plate VARCHAR(20) UNIQUE,    -- Registration number
    capacity_kg DECIMAL(10,2),           -- Cargo capacity
    color VARCHAR(100),                  -- Vehicle color
    fuel_type VARCHAR(50),               -- Fuel type (Petrol, Diesel, Electric)
    mileage DOUBLE,                      -- Current mileage
    assigned_driver_id BIGINT,           -- Assigned driver
    -- ... other fields
);
```

### Sample Data
```sql
-- Sample vehicles with detailed information
INSERT INTO vehicles (vehicle_type, make, model, year, license_plate, capacity_kg, color, fuel_type, mileage) VALUES
('Van', 'Toyota', 'Hiace', 2020, 'GP123456', 1000.00, 'White', 'Diesel', 45000.00),
('Truck', 'Isuzu', 'N-Series', 2019, 'GP789012', 3000.00, 'Blue', 'Diesel', 65000.00),
('Van', 'Ford', 'Transit', 2021, 'GP345678', 1200.00, 'Silver', 'Petrol', 32000.00),
('Van', 'Mercedes-Benz', 'Sprinter', 2022, 'GP901234', 1500.00, 'Black', 'Diesel', 28000.00),
('Truck', 'Volvo', 'FH16', 2018, 'GP567890', 5000.00, 'Red', 'Diesel', 85000.00),
('Van', 'Nissan', 'NV350', 2020, 'GP234567', 1100.00, 'Grey', 'Petrol', 38000.00);
```

## API Endpoints

### Driver Management

#### Get All Drivers with Vehicle Information
```http
GET /api/drivers
Authorization: Bearer <admin-token>

Response:
{
  "id": 1,
  "firstName": "John",
  "lastName": "Driver",
  "email": "driver@reliablecarriers.com",
  "phone": "+27123456788",
  "vehicleId": 1,
  "vehicleMake": "Toyota",
  "vehicleModel": "Hiace",
  "vehicleRegistrationNumber": "GP123456",
  "vehicleType": "VAN",
  "vehicleYear": 2020,
  "vehicleColor": "White",
  "vehicleFuelType": "Diesel",
  "vehicleCapacity": 1000.0,
  "vehicleMileage": 45000.0,
  "isOnline": true,
  "currentStatus": "ACTIVE",
  "activePackages": 3,
  "totalWeightCarrying": 250.5,
  "currentLocation": "123 Main St, Johannesburg, Gauteng",
  "lastLocationUpdate": "2024-01-15T10:30:00Z"
}
```

#### Get Driver by ID
```http
GET /api/drivers/{driverId}
Authorization: Bearer <admin-token>
```

#### Search Drivers by Vehicle Make and Model
```http
GET /api/drivers/vehicle/make-model?make=Toyota&model=Hiace
Authorization: Bearer <admin-token>
```

#### Get Drivers by Vehicle Type
```http
GET /api/drivers/vehicle/type/VAN
Authorization: Bearer <admin-token>
```

#### Get Driver by Vehicle Registration
```http
GET /api/drivers/vehicle/registration/GP123456
Authorization: Bearer <admin-token>
```

#### Get Drivers with Vehicles Due for Maintenance
```http
GET /api/drivers/maintenance-due
Authorization: Bearer <admin-token>
```

### Statistics and Analytics

#### Driver Statistics
```http
GET /api/drivers/statistics
Authorization: Bearer <admin-token>

Response:
{
  "totalDrivers": 6,
  "onlineDrivers": 4,
  "offlineDrivers": 2,
  "driversWithVehicles": 6,
  "driversWithoutVehicles": 0,
  "driversWithActivePackages": 3
}
```

#### Driver Count by Vehicle Make
```http
GET /api/drivers/statistics/vehicle-make
Authorization: Bearer <admin-token>

Response:
{
  "Toyota": 1,
  "Ford": 1,
  "Mercedes-Benz": 1,
  "Isuzu": 1,
  "Volvo": 1,
  "Nissan": 1
}
```

#### Driver Count by Vehicle Type
```http
GET /api/drivers/statistics/vehicle-type
Authorization: Bearer <admin-token>

Response:
{
  "VAN": 4,
  "TRUCK": 2
}
```

## DTO Classes

### DriverResponse
```java
public class DriverResponse {
    // Driver Information
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    
    // Vehicle Information
    private Long vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleRegistrationNumber;
    private String vehicleType;
    private Integer vehicleYear;
    private String vehicleColor;
    private String vehicleFuelType;
    private Double vehicleCapacity;
    private Double vehicleMileage;
    private String vehicleStatus;
    private Date vehicleLastMaintenanceDate;
    private Date vehicleNextMaintenanceDate;
    
    // Status Information
    private Boolean isOnline;
    private String currentStatus;
    private Integer activePackages;
    private Double totalWeightCarrying;
    private String currentLocation;
    private Date lastLocationUpdate;
    
    // Helper Methods
    public String getFullName() { return firstName + " " + lastName; }
    public String getVehicleFullName() { return vehicleMake + " " + vehicleModel; }
    public String getVehicleDisplayInfo() { /* formatted vehicle info */ }
}
```

### Updated DriverLocationResponse
```java
public class DriverLocationResponse {
    // ... existing fields ...
    private String vehicleMake;      // Added vehicle make
    private String vehicleModel;
    private String vehiclePlate;
    private String vehicleType;
}
```

### Updated CustomerPackageResponse
```java
public class CustomerPackageResponse {
    // ... existing fields ...
    private String driverVehicleMake;    // Added vehicle make
    private String driverVehicleModel;
    private String driverVehiclePlate;
}
```

## Service Layer

### DriverService Interface
```java
public interface DriverService {
    DriverResponse getDriverById(Long driverId);
    List<DriverResponse> getAllDrivers();
    List<DriverResponse> getDriversByVehicleMakeAndModel(String make, String model);
    List<DriverResponse> getDriversByVehicleType(String vehicleType);
    List<DriverResponse> getOnlineDrivers();
    List<DriverResponse> getOfflineDrivers();
    List<DriverResponse> getDriversWithActivePackages();
    List<DriverResponse> getDriversWithoutVehicles();
    List<DriverResponse> searchDrivers(String searchTerm);
    Map<String, Object> getDriverStatistics();
    Map<String, Object> getDriverPerformanceMetrics(Long driverId);
    DriverResponse getDriverByVehicleRegistration(String registrationNumber);
    List<DriverResponse> getDriversWithVehiclesDueForMaintenance();
    // ... more methods
}
```

### DriverServiceImpl
The implementation includes:
- **Vehicle Assignment**: Automatic vehicle lookup for drivers
- **Status Population**: Online/offline status based on recent location updates
- **Package Counting**: Active packages and total weight calculation
- **Performance Metrics**: Delivery statistics and performance analysis

## Sample Data

### Drivers with Assigned Vehicles
| Driver Name | Email | Vehicle Make | Vehicle Model | Registration | Type | Year |
|-------------|-------|--------------|---------------|--------------|------|------|
| John Driver | driver@reliablecarriers.com | Toyota | Hiace | GP123456 | Van | 2020 |
| Sarah Wilson | sarah.wilson@reliablecarriers.com | Isuzu | N-Series | GP789012 | Truck | 2019 |
| Michael Brown | michael.brown@reliablecarriers.com | Ford | Transit | GP345678 | Van | 2021 |
| Lisa Garcia | lisa.garcia@reliablecarriers.com | Mercedes-Benz | Sprinter | GP901234 | Van | 2022 |
| David Martinez | david.martinez@reliablecarriers.com | Volvo | FH16 | GP567890 | Truck | 2018 |
| Emma Johnson | emma.johnson@reliablecarriers.com | Nissan | NV350 | GP234567 | Van | 2020 |

## Usage Examples

### 1. Get All Drivers with Vehicle Information
```bash
curl -X GET "http://localhost:8080/api/drivers" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Search for Toyota Drivers
```bash
curl -X GET "http://localhost:8080/api/drivers/vehicle/make-model?make=Toyota&model=Hiace" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Get Online Drivers
```bash
curl -X GET "http://localhost:8080/api/drivers/online" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Get Driver Statistics
```bash
curl -X GET "http://localhost:8080/api/drivers/statistics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Find Driver by Vehicle Registration
```bash
curl -X GET "http://localhost:8080/api/drivers/vehicle/registration/GP123456" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Security and Authorization

### Role-Based Access Control
- **ADMIN**: Full access to all driver and vehicle information
- **STAFF**: Access to driver management and statistics
- **TRACKING_MANAGER**: Access to driver location and vehicle information
- **DRIVER**: Access to own information only

### Endpoint Security
```java
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
public ResponseEntity<List<DriverResponse>> getAllDrivers()

@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER') or #driverId == authentication.principal.id")
public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long driverId)
```

## Benefits

### 1. Complete Fleet Visibility
- Real-time view of all drivers and their assigned vehicles
- Vehicle specifications and maintenance schedules
- Driver status and current location tracking

### 2. Enhanced Customer Experience
- Customers can see vehicle details for their package delivery
- Professional presentation with complete driver information
- Better tracking and communication capabilities

### 3. Operational Efficiency
- Easy vehicle assignment and management
- Maintenance scheduling and alerts
- Performance tracking and analytics
- Route optimization based on vehicle capabilities

### 4. Compliance and Safety
- Complete vehicle documentation
- Maintenance tracking and alerts
- Driver-vehicle assignment records
- Safety and compliance reporting

## Future Enhancements

### Planned Features
1. **Vehicle Maintenance Alerts**: Automated notifications for maintenance due
2. **Fuel Efficiency Tracking**: Monitor fuel consumption and efficiency
3. **Vehicle Performance Analytics**: Track vehicle performance metrics
4. **Integration with Vehicle Tracking Systems**: Real-time vehicle data
5. **Mobile App Integration**: Driver app with vehicle information
6. **Advanced Reporting**: Comprehensive fleet and driver reports

### Technical Improvements
1. **Caching**: Vehicle information caching for better performance
2. **Real-time Updates**: WebSocket integration for live updates
3. **Image Support**: Vehicle photos and documentation
4. **API Rate Limiting**: Enhanced API security and performance
5. **Audit Logging**: Complete audit trail for vehicle assignments

## Conclusion

The driver vehicle integration feature provides comprehensive vehicle information including car make and model details. This enhancement significantly improves fleet management, customer experience, and operational efficiency while maintaining security and compliance standards.

The system now offers complete visibility into the driver-vehicle relationship, enabling better decision-making, improved customer service, and enhanced operational control.
