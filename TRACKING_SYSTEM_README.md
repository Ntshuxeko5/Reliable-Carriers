# Driver Tracking System - Reliable Carriers

## Overview

The Driver Tracking System is a comprehensive real-time tracking solution for Reliable Carriers that allows tracking managers to monitor driver locations, vehicle status, and fleet operations through an integrated map interface.

## Features

### 🗺️ Real-Time Map Tracking
- Interactive map with driver location markers
- Real-time location updates (30-second intervals)
- Driver status indicators (online/offline)
- Movement trails and history tracking
- Geographic filtering and search

### 📊 Dashboard & Analytics
- Live statistics dashboard
- Driver status summary
- Vehicle tracking information
- Historical data analysis
- Export capabilities

### 🔍 Advanced Filtering
- Filter by driver status (online/offline)
- Geographic bounding box filtering
- City and state-based filtering
- Search by driver name, email, or phone
- Time-based filtering

### 👥 User Management
- Role-based access control
- Tracking Manager role for specialized access
- Admin oversight capabilities
- Secure API endpoints

## System Architecture

### Backend Components

#### 1. Models
- `DriverLocation`: Stores driver location data with timestamps
- `User`: Enhanced with role-based access
- `Vehicle`: Vehicle information and status

#### 2. Services
- `TrackingService`: Core tracking business logic
- `TrackingServiceImpl`: Implementation with real-time capabilities
- `DriverLocationService`: Location data management

#### 3. Controllers
- `TrackingController`: REST API endpoints for tracking data
- `TrackingWebController`: Web interface controllers
- `DriverLocationController`: Enhanced with tracking features

#### 4. DTOs
- `DriverLocationResponse`: Enhanced response with tracking data
- `TrackingRequest`: Advanced filtering and search parameters

### Frontend Components

#### 1. Templates
- `tracking/dashboard.html`: Main tracking dashboard
- `tracking/map.html`: Full-screen map view
- Enhanced `dashboard.html`: Integration with main dashboard

#### 2. JavaScript
- `tracking.js`: Core tracking functionality
- Real-time updates and map interactions
- Data filtering and visualization

#### 3. CSS
- `tracking.css`: Modern, responsive styling
- Map integration styles
- Status indicators and animations

## API Endpoints

### Tracking Management
```
GET  /api/tracking/active-drivers          # Get all active drivers
POST /api/tracking/filter                  # Filter drivers with criteria
POST /api/tracking/realtime                # Get real-time updates
GET  /api/tracking/map-view                # Combined map data
GET  /api/tracking/statistics              # Tracking statistics
GET  /api/tracking/driver-status-summary   # Driver status summary
```

### Driver Location History
```
GET  /api/tracking/driver/{id}/history     # Driver location history
GET  /api/tracking/vehicle/{id}/history    # Vehicle location history
GET  /api/tracking/driver/{id}/summary     # Driver tracking summary
GET  /api/tracking/driver/{id}/last-location # Last known location
```

### Geographic Filtering
```
GET  /api/tracking/bounding-box            # Filter by geographic bounds
GET  /api/tracking/location                # Filter by city/state
GET  /api/tracking/search                  # Search drivers
```

### Web Interface
```
GET  /tracking/dashboard                   # Main tracking dashboard
GET  /tracking/map                         # Full-screen map view
GET  /tracking/driver-history              # Driver history view
GET  /tracking/vehicle-tracking            # Vehicle tracking view
GET  /tracking/analytics                   # Analytics dashboard
GET  /tracking/settings                    # Tracking settings
```

## Setup and Installation

### Prerequisites
- Java 17+
- Spring Boot 3.x
- Maven
- Database (MySQL/PostgreSQL)

### Database Setup
The system uses existing tables with enhanced tracking capabilities:

```sql
-- Driver locations table (already exists)
CREATE TABLE driver_locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    driver_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    address VARCHAR(200),
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50),
    vehicle_id BIGINT,
    notes VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES users(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

-- Add indexes for performance
CREATE INDEX idx_driver_location_driver ON driver_locations(driver_id);
CREATE INDEX idx_driver_location_timestamp ON driver_locations(timestamp);
CREATE INDEX idx_driver_location_coordinates ON driver_locations(latitude, longitude);
```

### Configuration
Add to `application.properties`:

```properties
# Tracking system configuration
tracking.real-time-interval=30000
tracking.online-threshold=300000
tracking.max-history-days=30
```

## Usage

### For Tracking Managers

1. **Access the Dashboard**
   - Login with TRACKING_MANAGER or ADMIN role
   - Navigate to `/tracking/dashboard`

2. **Real-Time Monitoring**
   - View live driver locations on the map
   - Monitor driver status (online/offline)
   - Enable real-time updates

3. **Filtering and Search**
   - Use advanced filters to find specific drivers
   - Search by name, location, or status
   - Apply geographic filters

4. **Analytics**
   - View tracking statistics
   - Analyze driver patterns
   - Export data for reporting

### For Drivers

1. **Location Updates**
   - Drivers can update their location via API
   - Automatic timestamp tracking
   - Optional notes and vehicle association

2. **Privacy Controls**
   - Drivers can only see their own location history
   - Role-based access restrictions

## Security Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control
- Secure API endpoints
- Session management

### Data Protection
- Encrypted location data
- Privacy controls for drivers
- Audit logging
- Data retention policies

## Performance Considerations

### Optimization
- Database indexing for location queries
- Caching for frequently accessed data
- Efficient real-time updates
- Pagination for large datasets

### Scalability
- Horizontal scaling support
- Load balancing ready
- Database connection pooling
- Async processing for heavy operations

## Troubleshooting

### Common Issues

1. **Map not loading**
   - Check internet connection for map tiles
   - Verify Leaflet.js is loaded
   - Check browser console for errors

2. **Real-time updates not working**
   - Verify WebSocket connection
   - Check server logs for errors
   - Ensure proper authentication

3. **Location data not updating**
   - Check database connectivity
   - Verify API endpoints
   - Check driver app connectivity

### Debug Mode
Enable debug logging in `application.properties`:

```properties
logging.level.com.reliablecarriers.tracking=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Future Enhancements

### Planned Features
- Heatmap visualization
- Route optimization
- Predictive analytics
- Mobile app integration
- Advanced reporting
- Geofencing alerts

### Technical Improvements
- WebSocket for real-time updates
- Redis caching layer
- Elasticsearch for advanced search
- Machine learning for pattern recognition

## Support

For technical support or questions about the tracking system:

1. Check the application logs
2. Review the API documentation
3. Contact the development team
4. Submit issues through the project repository

## License

This tracking system is part of the Reliable Carriers application and follows the same licensing terms.
