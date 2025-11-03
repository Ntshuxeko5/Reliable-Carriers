# API Documentation

## Reliable Carriers Business API

Complete API reference for business integrations.

---

## Base URL

```
Production: https://api.reliablecarriers.co.za/api/business
Sandbox: https://sandbox.reliablecarriers.co.za/api/business
```

---

## Authentication

All API requests require authentication headers:

```http
Authorization: Bearer YOUR_API_KEY
X-API-Secret: YOUR_API_SECRET
Content-Type: application/json
```

---

## Endpoints

### Shipments

#### Create Shipment
```http
POST /api/business/shipments
```

**Request:**
```json
{
  "sender": {
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+27123456789",
    "address": {
      "street": "123 Main Street",
      "city": "Johannesburg",
      "province": "Gauteng",
      "postalCode": "2000",
      "country": "South Africa"
    }
  },
  "recipient": {
    "name": "Jane Smith",
    "email": "jane@example.com",
    "phone": "+27987654321",
    "address": {
      "street": "456 Oak Avenue",
      "city": "Cape Town",
      "province": "Western Cape",
      "postalCode": "8001",
      "country": "South Africa"
    }
  },
  "package": {
    "weight": 2.5,
    "length": 30,
    "width": 20,
    "height": 15,
    "type": "parcel",
    "description": "Electronics",
    "value": 5000.00
  },
  "service": {
    "type": "standard",
    "insurance": true,
    "signatureRequired": false
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "trackingNumber": "RC123456789ZA",
    "shipmentId": "shp_abc123",
    "status": "booked",
    "estimatedDelivery": "2024-01-03",
    "price": {
      "base": 150.00,
      "insurance": 50.00,
      "total": 200.00
    }
  }
}
```

#### Get Shipment
```http
GET /api/business/shipments/{trackingNumber}
```

#### List Shipments
```http
GET /api/business/shipments?status=all&page=1&limit=50
```

#### Track Shipment
```http
GET /api/business/shipments/{trackingNumber}/track
```

#### Create Bulk Shipments
```http
POST /api/business/shipments/bulk
```

### Account

#### Get Account Details
```http
GET /api/business/account
```

#### Update Account
```http
PUT /api/business/account
```

### Analytics

#### Get Statistics
```http
GET /api/business/analytics?from=2024-01-01&to=2024-01-31
```

---

For complete API documentation, visit the Swagger UI at `/swagger-ui.html` (requires admin authentication in production).

