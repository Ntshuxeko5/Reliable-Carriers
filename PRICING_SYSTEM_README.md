# Reliable Carriers - Integrated Pricing System

## Overview

The Reliable Carriers system now features a comprehensive, integrated pricing system that handles both courier services and moving services with different pricing models and availability zones.

## Pricing Structure

### 🚚 Courier Services (Gauteng Province Only)

Currently available only in Gauteng Province with fixed pricing:

| Service Type | Price | Delivery Time | Description |
|--------------|-------|---------------|-------------|
| **Same Day Delivery** | R140 | Same Day | Priority same-day delivery within Gauteng |
| **Overnight Delivery** | R120 | Next Day | Next-day delivery within Gauteng |
| **Economy Delivery** | R100 | 2-3 Business Days | Cost-effective 2-3 business days delivery |
| **Urgent Delivery** | R425 | Priority | Premium urgent delivery service |

**Note:** Courier services are currently limited to Gauteng Province. Nationwide expansion is planned.

### 🏠 Moving Services (Nationwide)

Available nationwide with distance-based pricing:

| Service Type | Base Price | Pricing Model | Description |
|--------------|------------|---------------|-------------|
| **Furniture Moving** | R550 (20km) | Distance-based | Professional furniture moving service |
| **Load Transport** | R550 (20km) | Distance-based | Heavy load and equipment transport |
| **Complete Moving** | R550 (20km) | Distance-based | Complete home/office relocation |

**Pricing Formula:**
- **Base Price:** R550 for distances up to 20km
- **Additional Cost:** R25 per kilometer beyond 20km
- **Example:** 25km journey = R550 + (5km × R25) = R675

## System Architecture

### Core Components

#### 1. ServiceType Enum
```java
public enum ServiceType {
    // Courier Services (Gauteng Province Only)
    SAME_DAY("Same Day Delivery", new BigDecimal("140.00"), "Same day delivery within Gauteng"),
    OVERNIGHT("Overnight Delivery", new BigDecimal("120.00"), "Next day delivery within Gauteng"),
    ECONOMY("Economy 2-3 Business Days", new BigDecimal("100.00"), "2-3 business days delivery within Gauteng"),
    URGENT("Urgent Delivery", new BigDecimal("425.00"), "Priority urgent delivery within Gauteng"),
    
    // Moving Services
    FURNITURE("Furniture Moving", new BigDecimal("550.00"), "Furniture moving service - 1 Load (20km): R550"),
    MOVING("Moving Service", new BigDecimal("550.00"), "Complete moving service"),
    LOAD_TRANSPORT("Load Transport", new BigDecimal("550.00"), "Heavy load transport service");
}
```

#### 2. PricingService Interface
```java
public interface PricingService {
    BigDecimal calculateCourierPrice(ServiceType serviceType);
    BigDecimal calculateMovingServicePrice(ServiceType serviceType, Double distanceKm);
    BigDecimal calculatePrice(ServiceType serviceType, Double distanceKm);
    Map<String, Object> getMovingServicePricingBreakdown(Double distanceKm);
    Map<ServiceType, BigDecimal> getCourierServicePrices();
    Map<ServiceType, BigDecimal> getMovingServiceBasePrices();
    boolean isServiceAvailableInProvince(ServiceType serviceType, String province);
    Map<String, Object> getServiceInfo(ServiceType serviceType);
}
```

#### 3. PricingController
REST API endpoints for pricing operations:
- `GET /api/pricing/courier-services` - Get all courier service prices
- `GET /api/pricing/moving-services` - Get all moving service base prices
- `POST /api/pricing/calculate` - Calculate price for any service type
- `POST /api/pricing/moving-service-breakdown` - Get moving service pricing breakdown
- `GET /api/pricing/availability` - Check service availability in province
- `GET /api/pricing/service-info/{serviceType}` - Get service information
- `GET /api/pricing/price-list` - Get complete price list

## API Endpoints

### Get Courier Service Prices
```http
GET /api/pricing/courier-services
```

**Response:**
```json
{
  "prices": {
    "SAME_DAY": 140.00,
    "OVERNIGHT": 120.00,
    "ECONOMY": 100.00,
    "URGENT": 425.00
  },
  "availability": "Gauteng Province Only",
  "note": "These prices apply only to Gauteng province. Nationwide expansion planned."
}
```

### Get Moving Service Prices
```http
GET /api/pricing/moving-services
```

**Response:**
```json
{
  "basePrices": {
    "FURNITURE": 550.00,
    "MOVING": 550.00,
    "LOAD_TRANSPORT": 550.00
  },
  "availability": "Nationwide",
  "pricingModel": "R550 for 20km, R25/km thereafter"
}
```

### Calculate Price
```http
POST /api/pricing/calculate?serviceType=SAME_DAY
POST /api/pricing/calculate?serviceType=FURNITURE&distanceKm=25.5
```

**Courier Service Response:**
```json
{
  "serviceType": "SAME_DAY",
  "price": 140.00,
  "pricingModel": "Fixed Price",
  "availability": "Gauteng Province Only"
}
```

**Moving Service Response:**
```json
{
  "serviceType": "FURNITURE",
  "price": 687.50,
  "breakdown": {
    "basePrice": 550.00,
    "extraDistanceCharge": 137.50,
    "totalPrice": 687.50,
    "distanceKm": 25.5,
    "maxFreeDistanceKm": 20,
    "pricePerKm": 25.00,
    "breakdown": "Base price for 20 km: R550.00 + Extra 5.5 km × R25.00/km = R137.50"
  },
  "pricingModel": "Distance-based",
  "availability": "Nationwide"
}
```

### Get Moving Service Pricing Breakdown
```http
POST /api/pricing/moving-service-breakdown?distanceKm=25.5
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

### Check Service Availability
```http
GET /api/pricing/availability?serviceType=SAME_DAY&province=Gauteng
```

**Response:**
```json
{
  "serviceType": "SAME_DAY",
  "province": "Gauteng",
  "isAvailable": true,
  "note": "Courier services are currently only available in Gauteng Province"
}
```

### Get Complete Price List
```http
GET /api/pricing/price-list
```

**Response:**
```json
{
  "courierServices": {
    "Same Day Delivery": {
      "price": 140.00,
      "description": "Same day delivery within Gauteng",
      "availability": "Gauteng Province Only"
    },
    "Overnight Delivery": {
      "price": 120.00,
      "description": "Next day delivery within Gauteng",
      "availability": "Gauteng Province Only"
    }
  },
  "movingServices": {
    "Furniture Moving": {
      "basePrice": 550.00,
      "description": "Furniture moving service - 1 Load (20km): R550",
      "pricingModel": "R550 for 20km, R25/km thereafter",
      "availability": "Nationwide"
    }
  },
  "note": "Courier services are currently only available in Gauteng province. Nationwide expansion is planned."
}
```

## Integration with Existing Systems

### Shipment Model Updates
The `Shipment` model has been enhanced to include service type:
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private ServiceType serviceType;
```

### ShipmentService Integration
The `ShipmentService` now automatically calculates shipping costs based on service type:
- **Courier Services:** Fixed pricing from `ServiceType.getBasePrice()`
- **Moving Services:** Distance-based calculation using `PricingService`

### MovingServiceService Integration
The `MovingServiceService` now uses the centralized `PricingService` for all pricing calculations, eliminating code duplication.

## Frontend Integration

### Price List Page
A comprehensive price list page (`/price-list`) displays:
- All courier service prices with availability information
- All moving service base prices with pricing model explanation
- Interactive price calculator for moving services
- Service comparison table
- Contact information

### Features
- **Responsive Design:** Works on all device sizes
- **Interactive Calculator:** Real-time price calculation for moving services
- **Service Comparison:** Side-by-side comparison of all services
- **Availability Information:** Clear indication of service availability zones

## Configuration

### Environment Variables
No additional environment variables are required for the pricing system.

### Database Changes
The `shipments` table now includes a `service_type` column:
```sql
ALTER TABLE shipments ADD COLUMN service_type VARCHAR(50) NOT NULL DEFAULT 'ECONOMY';
```

## Testing

### API Testing
Test all pricing endpoints:
```bash
# Test courier service prices
curl -X GET "http://localhost:8080/api/pricing/courier-services"

# Test moving service calculation
curl -X POST "http://localhost:8080/api/pricing/calculate?serviceType=FURNITURE&distanceKm=25.5"

# Test price breakdown
curl -X POST "http://localhost:8080/api/pricing/moving-service-breakdown?distanceKm=25.5"
```

### Frontend Testing
1. Navigate to `/price-list`
2. Test the price calculator with different distances
3. Verify all service information is displayed correctly
4. Test responsive design on different screen sizes

## Future Enhancements

### Planned Features
1. **Dynamic Pricing:** Real-time pricing based on demand and availability
2. **Bulk Pricing:** Discounts for multiple shipments or services
3. **Seasonal Pricing:** Price adjustments for peak seasons
4. **Regional Pricing:** Different pricing for different provinces
5. **Fuel Surcharge:** Dynamic fuel surcharge calculation
6. **Insurance Options:** Additional insurance pricing

### Nationwide Expansion
When expanding beyond Gauteng:
1. Update `PricingService.isServiceAvailableInProvince()` method
2. Add province-specific pricing logic
3. Update frontend to show availability by province
4. Implement regional pricing models

## Support and Maintenance

### Adding New Service Types
1. Add new enum value to `ServiceType`
2. Update pricing logic in `PricingServiceImpl`
3. Add new service to frontend price list
4. Update documentation

### Modifying Pricing
1. Update constants in `PricingServiceImpl`
2. Update `ServiceType` enum base prices
3. Test all pricing calculations
4. Update frontend price display
5. Notify stakeholders of price changes

## Conclusion

The integrated pricing system provides a robust, scalable foundation for managing both courier and moving service pricing. The system is designed to be easily extensible for future service types and pricing models while maintaining clear separation between different service categories and their respective pricing logic.
