# Database Communication for Geocoding Implementation

## ✅ Yes, Everything Communicates with the Database

All components of the geocoding implementation interact with the database through Spring Data JPA repositories.

## Database Interactions

### 1. **Shipment Entity → Database Table**
```java
@Entity
@Table(name = "shipments")
public class Shipment {
    @Column(precision = 10, scale = 8)
    private BigDecimal pickupLatitude;  // Maps to pickup_latitude column
    
    @Column(precision = 11, scale = 8)
    private BigDecimal pickupLongitude; // Maps to pickup_longitude column
    
    @Column(precision = 10, scale = 8)
    private BigDecimal deliveryLatitude; // Maps to delivery_latitude column
    
    @Column(precision = 11, scale = 8)
    private BigDecimal deliveryLongitude; // Maps to delivery_longitude column
}
```

### 2. **ShipmentServiceImpl - Saves Coordinates**
```java
// When creating shipment
Shipment savedShipment = shipmentRepository.save(shipment);
// ↑ Saves to database with coordinates

// When updating shipment
return shipmentRepository.save(existingShipment);
// ↑ Updates database with new coordinates
```

### 3. **ShipmentGeocodingService - Reads and Writes**
```java
// Reads shipments missing coordinates
List<Shipment> missingCoords = shipmentRepository
    .findByPickupLatitudeIsNullOrDeliveryLatitudeIsNull();
// ↑ Queries database

// Saves geocoded coordinates
shipmentRepository.save(shipment);
// ↑ Updates database with coordinates
```

### 4. **UnifiedPackageService - Reads Coordinates**
```java
// Reads shipments from database
dto.setPickupLatitude(shipment.getPickupLatitude());
dto.setDeliveryLatitude(shipment.getDeliveryLatitude());
// ↑ Reads coordinates from database
```

### 5. **GeocodingController - Indirect Database Access**
```java
// Calls services that interact with database
shipmentGeocodingService.geocodeShipment(shipmentId);
// ↑ Service reads from and writes to database
```

## Database Schema

### New Columns Added to `shipments` Table:
- `pickup_latitude` DECIMAL(10, 8) NULL
- `pickup_longitude` DECIMAL(11, 8) NULL
- `delivery_latitude` DECIMAL(10, 8) NULL
- `delivery_longitude` DECIMAL(11, 8) NULL

### Indexes Created:
- `idx_shipments_pickup_coords` on (pickup_latitude, pickup_longitude)
- `idx_shipments_delivery_coords` on (delivery_latitude, delivery_longitude)

## Automatic Schema Updates

### Development Mode (`ddl-auto=update`)
- Hibernate automatically creates/updates columns when application starts
- No manual migration needed

### Production Mode (`ddl-auto=validate` or `none`)
- Use migration script: `add-shipment-coordinates.sql`
- Or use `ShipmentCoordinatesMigration` component (runs on startup)
- Ensures columns exist before application uses them

## Data Flow

```
1. User creates shipment
   ↓
2. ShipmentServiceImpl.createShipment()
   ↓
3. Geocodes addresses → Gets coordinates
   ↓
4. Sets coordinates on Shipment entity
   ↓
5. shipmentRepository.save(shipment)
   ↓
6. Database INSERT/UPDATE with coordinates ✅
```

```
1. Background job runs (hourly)
   ↓
2. ShipmentGeocodingService.geocodeMissingCoordinates()
   ↓
3. Queries database: findByPickupLatitudeIsNullOrDeliveryLatitudeIsNull()
   ↓
4. Gets shipments without coordinates
   ↓
5. Geocodes addresses → Gets coordinates
   ↓
6. Sets coordinates on Shipment entity
   ↓
7. shipmentRepository.save(shipment)
   ↓
8. Database UPDATE with coordinates ✅
```

```
1. Frontend requests package data
   ↓
2. UnifiedPackageService.getAllPackages()
   ↓
3. Queries database: shipmentRepository.findAll()
   ↓
4. Reads shipments with coordinates
   ↓
5. Converts to UnifiedPackageDTO
   ↓
6. Returns coordinates to frontend ✅
```

## Verification

All database operations use:
- ✅ Spring Data JPA repositories (`ShipmentRepository`)
- ✅ JPA entity mappings (`@Entity`, `@Column`)
- ✅ Transaction management (`@Transactional`)
- ✅ Database connection pooling (HikariCP)

The coordinates are **persisted** in the database and **retrieved** when needed. The background job ensures existing shipments get geocoded and saved to the database automatically.

