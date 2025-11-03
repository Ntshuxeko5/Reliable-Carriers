# Business API Documentation

## Overview

Reliable Carriers provides a comprehensive REST API for business customers to integrate shipping and tracking functionality into their own systems. The API allows businesses to:

- **Track Delivery Status**: Real-time tracking of shipments
- **Create Shipments**: Programmatically create bookings/shipments
- **Bulk Operations**: Create multiple shipments at once
- **Get Statistics**: Access shipping statistics and analytics
- **Easy Shipping**: Quick shipping features with saved addresses

---

## Authentication

Business APIs use **API Key Authentication**. You can generate API keys from your business dashboard after your account is verified.

### API Key Format
```
rc_<base64-encoded-random-string>
```

### Authentication Methods

#### Method 1: X-API-Key Header (Recommended)
```http
X-API-Key: rc_your_api_key_here
```

#### Method 2: Authorization Bearer Token
```http
Authorization: Bearer rc_your_api_key_here
```

#### Method 3: API-Key Header
```http
API-Key: rc_your_api_key_here
```

### Rate Limits

- **Default**: 1,000 requests per hour per API key
- Rate limits can be customized when generating API keys
- Rate limit exceeded returns HTTP 429 (Too Many Requests)

---

## Base URL

```
Production: https://api.reliablecarriers.co.za/api/business
Development: http://localhost:8080/api/business
```

---

## API Endpoints

### 1. Track Delivery Status

Get real-time tracking information for a shipment.

**Endpoint**: `GET /api/business/tracking/{trackingNumber}`

**Headers**:
```
X-API-Key: rc_your_api_key_here
```

**Response**:
```json
{
  "success": true,
  "data": {
    "trackingNumber": "RC12345678",
    "status": "IN_TRANSIT",
    "pickupAddress": "123 Main St, Johannesburg, 2000",
    "deliveryAddress": "456 Oak Ave, Cape Town, 8000",
    "estimatedDeliveryDate": "2024-11-01T10:00:00Z",
    "trackingHistory": [
      {
        "status": "PICKED_UP",
        "location": "Johannesburg Warehouse",
        "timestamp": "2024-10-29T08:00:00Z",
        "notes": "Package collected from sender"
      }
    ],
    "driver": {
      "name": "John Doe",
      "phone": "+27123456789"
    }
  }
}
```

**Use Case**: Integrate into your customer portal to show delivery status.

---

### 2. Create Shipment

Create a new shipment/booking via API.

**Endpoint**: `POST /api/business/shipments`

**Headers**:
```
X-API-Key: rc_your_api_key_here
Content-Type: application/json
```

**Request Body**:
```json
{
  "serviceType": "SAME_DAY",
  "pickupAddress": "123 Business St, Sandton, 2196",
  "pickupCity": "Sandton",
  "pickupState": "Gauteng",
  "pickupPostalCode": "2196",
  "pickupContactName": "John Manager",
  "pickupContactPhone": "+27123456789",
  "deliveryAddress": "456 Customer Ave, Cape Town, 8000",
  "deliveryCity": "Cape Town",
  "deliveryState": "Western Cape",
  "deliveryPostalCode": "8000",
  "deliveryContactName": "Jane Customer",
  "deliveryContactPhone": "+27987654321",
  "weight": 5.5,
  "dimensions": "30x20x15",
  "description": "Electronics package",
  "insurance": false,
  "packing": false,
  "saturdayDelivery": false,
  "signatureRequired": true
}
```

**Response**:
```json
{
  "success": true,
  "message": "Shipment created successfully",
  "data": {
    "bookingNumber": "BK1730123456789",
    "trackingNumber": "RC12345678",
    "status": "PENDING",
    "totalAmount": 140.00
  }
}
```

---

### 3. Get All Shipments

Retrieve all shipments for your business with optional filtering.

**Endpoint**: `GET /api/business/shipments?status=DELIVERED&page=0&size=50`

**Query Parameters**:
- `status` (optional): Filter by status (PENDING, CONFIRMED, IN_TRANSIT, DELIVERED, etc.)
- `page` (optional, default: 0): Page number for pagination
- `size` (optional, default: 50): Number of results per page

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "bookingNumber": "BK1730123456789",
      "trackingNumber": "RC12345678",
      "status": "DELIVERED",
      "totalAmount": 140.00,
      "createdAt": "2024-10-28T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 50,
    "total": 150,
    "totalPages": 3
  }
}
```

---

### 4. Get Shipment Details

Get detailed information about a specific shipment.

**Endpoint**: `GET /api/business/shipments/{bookingNumber}` or `GET /api/business/shipments/{trackingNumber}`

**Response**: Full booking details with all information

---

### 5. Bulk Shipment Creation

Create multiple shipments at once.

**Endpoint**: `POST /api/business/easy-shipping/bulk`

**Request Body**:
```json
[
  {
    "serviceType": "ECONOMY",
    "pickupAddress": "...",
    "deliveryAddress": "...",
    "weight": 2.0,
    ...
  },
  {
    "serviceType": "OVERNIGHT",
    "pickupAddress": "...",
    "deliveryAddress": "...",
    "weight": 5.0,
    ...
  }
]
```

**Response**:
```json
{
  "success": true,
  "message": "Processed 2 shipments. 2 successful, 0 failed",
  "results": [
    {
      "index": 0,
      "success": true,
      "bookingNumber": "BK123",
      "trackingNumber": "RC123",
      "totalAmount": 100.00
    }
  ],
  "errors": []
}
```

---

### 6. Get Saved Addresses

Retrieve frequently used addresses from your shipping history.

**Endpoint**: `GET /api/business/easy-shipping/saved-addresses`

**Response**:
```json
{
  "success": true,
  "data": {
    "pickupAddresses": [
      {
        "address": "123 Business St",
        "city": "Sandton",
        "state": "Gauteng",
        "postalCode": "2196",
        "label": "123 Business St, Sandton"
      }
    ],
    "deliveryAddresses": [...]
  }
}
```

---

### 7. Quick Shipment Creation

Create a shipment using simplified template format.

**Endpoint**: `POST /api/business/easy-shipping/quick`

**Request Body**:
```json
{
  "serviceType": "ECONOMY",
  "pickup": {
    "address": "123 Business St, Sandton, 2196",
    "city": "Sandton",
    "state": "Gauteng",
    "postalCode": "2196",
    "contactName": "John Manager",
    "contactPhone": "+27123456789"
  },
  "delivery": {
    "address": "456 Customer Ave, Cape Town, 8000",
    "city": "Cape Town",
    "state": "Western Cape",
    "postalCode": "8000",
    "contactName": "Jane Customer",
    "contactPhone": "+27987654321"
  },
  "package": {
    "weight": 3.5,
    "dimensions": "25x20x15",
    "description": "Product shipment"
  }
}
```

---

### 8. Get Shipping Statistics

Get business shipping statistics.

**Endpoint**: `GET /api/business/easy-shipping/statistics`

**Response**:
```json
{
  "success": true,
  "data": {
    "totalShipments": 150,
    "pending": 5,
    "inTransit": 10,
    "delivered": 130,
    "totalSpent": 15000.00,
    "monthlyShipments": 45
  }
}
```

---

### 9. Get Business Account Information

**Endpoint**: `GET /api/business/account`

**Response**:
```json
{
  "success": true,
  "data": {
    "businessName": "ABC Company",
    "email": "business@example.com",
    "phone": "+27123456789",
    "verificationStatus": "APPROVED",
    "creditLimit": 50000.00,
    "currentBalance": 1500.00,
    "paymentTerms": 30
  }
}
```

---

## API Key Management

### Generate API Key

**Via Web Dashboard**: `/customer/api-keys` (requires login)

**Via API** (if authenticated via session): `POST /api/business/keys`
```json
{
  "keyName": "Production API Key",
  "description": "Used for our e-commerce platform",
  "rateLimit": 2000
}
```

### List API Keys

**Endpoint**: `GET /api/business/keys`

### Revoke API Key

**Endpoint**: `DELETE /api/business/keys/{keyId}`

---

## Integration Examples

### JavaScript/Node.js

```javascript
const axios = require('axios');

const API_KEY = 'rc_your_api_key_here';
const BASE_URL = 'https://api.reliablecarriers.co.za/api/business';

async function trackShipment(trackingNumber) {
  try {
    const response = await axios.get(`${BASE_URL}/tracking/${trackingNumber}`, {
      headers: {
        'X-API-Key': API_KEY
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error:', error.response.data);
    throw error;
  }
}

async function createShipment(shipmentData) {
  try {
    const response = await axios.post(`${BASE_URL}/shipments`, shipmentData, {
      headers: {
        'X-API-Key': API_KEY,
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error:', error.response.data);
    throw error;
  }
}
```

### Python

```python
import requests

API_KEY = 'rc_your_api_key_here'
BASE_URL = 'https://api.reliablecarriers.co.za/api/business'

def track_shipment(tracking_number):
    headers = {'X-API-Key': API_KEY}
    response = requests.get(f'{BASE_URL}/tracking/{tracking_number}', headers=headers)
    return response.json()

def create_shipment(shipment_data):
    headers = {
        'X-API-Key': API_KEY,
        'Content-Type': 'application/json'
    }
    response = requests.post(f'{BASE_URL}/shipments', json=shipment_data, headers=headers)
    return response.json()
```

### PHP

```php
<?php
$apiKey = 'rc_your_api_key_here';
$baseUrl = 'https://api.reliablecarriers.co.za/api/business';

function trackShipment($trackingNumber, $apiKey, $baseUrl) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "$baseUrl/tracking/$trackingNumber");
    curl_setopt($ch, CURLOPT_HTTPHEADER, ["X-API-Key: $apiKey"]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}

function createShipment($shipmentData, $apiKey, $baseUrl) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "$baseUrl/shipments");
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($shipmentData));
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        "X-API-Key: $apiKey",
        "Content-Type: application/json"
    ]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}
?>
```

---

## Use Cases

### 1. E-commerce Integration

**Scenario**: When a customer places an order on your website, automatically create a shipment.

```javascript
// After order is placed
const shipment = await createShipment({
  serviceType: 'ECONOMY',
  pickupAddress: 'Your Warehouse Address',
  deliveryAddress: order.customerAddress,
  weight: order.totalWeight,
  // ... other details
});

// Store tracking number in your order
order.trackingNumber = shipment.data.trackingNumber;
```

### 2. Customer Portal Integration

**Scenario**: Display delivery status on your customer portal.

```javascript
// Get tracking info for customer's order
const tracking = await trackShipment(order.trackingNumber);

// Display on customer portal
updateOrderStatus(tracking.data.status);
showTrackingHistory(tracking.data.trackingHistory);
```

### 3. Bulk Shipping Operations

**Scenario**: Process multiple orders at the end of the day.

```javascript
// Collect all orders ready to ship
const ordersToShip = getOrdersReadyToShip();

// Create shipments in bulk
const shipments = await createBulkShipments(
  ordersToShip.map(order => convertOrderToShipment(order))
);

// Update all orders with tracking numbers
shipments.results.forEach((result, index) => {
  if (result.success) {
    ordersToShip[result.index].trackingNumber = result.trackingNumber;
  }
});
```

---

## Error Handling

All API endpoints return consistent error responses:

```json
{
  "success": false,
  "error": "Error message description"
}
```

### HTTP Status Codes

- **200 OK**: Request successful
- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Invalid or missing API key
- **403 Forbidden**: Business not verified or insufficient permissions
- **404 Not Found**: Resource not found
- **429 Too Many Requests**: Rate limit exceeded
- **500 Internal Server Error**: Server error

---

## Best Practices

1. **Store API Keys Securely**: Never expose API keys in client-side code
2. **Implement Retry Logic**: Handle rate limits and temporary failures
3. **Cache Tracking Data**: Reduce API calls by caching tracking information
4. **Use Webhooks**: Subscribe to shipment status updates (coming soon)
5. **Monitor Usage**: Track your API usage to stay within rate limits
6. **Error Handling**: Always handle errors gracefully in your integration

---

## Support

For API support:
- Email: api-support@reliablecarriers.co.za
- Documentation: https://docs.reliablecarriers.co.za
- Status Page: https://status.reliablecarriers.co.za

