# Business User Manual

## Welcome, Business Partner! 💼

This guide will help you integrate and use the Reliable Carriers API for seamless package delivery services.

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Business Registration](#business-registration)
3. [API Access](#api-access)
4. [API Integration](#api-integration)
5. [Creating Shipments](#creating-shipments)
6. [Tracking Packages](#tracking-packages)
7. [Payment & Billing](#payment--billing)
8. [Business Dashboard](#business-dashboard)
9. [Webhooks](#webhooks)
10. [Best Practices](#best-practices)

---

## Getting Started

### What You Get

- 🔌 **RESTful API**: Full integration capabilities
- 📦 **Bulk Operations**: Create multiple shipments
- 📊 **Analytics Dashboard**: Track your shipments
- 💳 **Credit Terms**: Flexible payment options
- 🔔 **Webhooks**: Real-time notifications
- 📱 **Customer Tracking**: Share tracking with customers

---

## Business Registration

### Step 1: Register Business Account

1. Go to **"Business Solutions"** page
2. Click **"Register Business"**
3. Fill in business details:
   - **Company Name**: Official company name
   - **Registration Number**: Company registration
   - **Tax Number**: VAT/Tax number
   - **Business Type**: Retail, E-commerce, Logistics, etc.
   - **Address**: Business address
   - **Contact Person**: Primary contact
   - **Email**: Business email
   - **Phone**: Contact number

### Step 2: Upload Documents

Required documents:
- ✅ **Business Registration Certificate**
- ✅ **Tax Certificate**
- ✅ **Bank Statement** (for verification)
- ✅ **ID Document** (contact person)

### Step 3: Verification

1. We review your documents
2. Verification usually takes 1-2 business days
3. You'll receive email updates

### Step 4: Account Approval

Once approved:
- ✅ Access to business dashboard
- ✅ API credentials generated
- ✅ Integration guide sent
- ✅ Onboarding support available

---

## API Access

### Getting API Credentials

1. Log into business dashboard
2. Go to **"API Settings"**
3. View your credentials:
   - **API Key**: Your unique key
   - **API Secret**: Keep secure!
   - **Webhook Secret**: For webhook verification

### API Key Management

#### Generate New Key
1. Go to **"API Keys"**
2. Click **"Generate New Key"**
3. Give it a name (e.g., "Production", "Testing")
4. Copy and save securely
5. Old key remains active until revoked

#### Revoke Key
1. Select key to revoke
2. Click **"Revoke"**
3. Confirm revocation
4. Key immediately disabled

#### Key Limits
- **Rate Limit**: Requests per minute
- **Daily Limit**: Requests per day
- **Monthly Limit**: Requests per month
- View usage in dashboard

---

## API Integration

### Base URL

```
Production: https://api.reliablecarriers.co.za/api/business
Sandbox: https://sandbox.reliablecarriers.co.za/api/business
```

### Authentication

All API requests require authentication:

```http
Authorization: Bearer YOUR_API_KEY
X-API-Secret: YOUR_API_SECRET
Content-Type: application/json
```

### Response Format

All responses are JSON:

```json
{
  "success": true,
  "data": { ... },
  "message": "Success message",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Error Handling

Error responses:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error description",
    "details": { ... }
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Status Codes

- **200 OK**: Request successful
- **201 Created**: Resource created
- **400 Bad Request**: Invalid request
- **401 Unauthorized**: Invalid credentials
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **429 Too Many Requests**: Rate limit exceeded
- **500 Server Error**: Server error

---

## Creating Shipments

### API Endpoint

```http
POST /api/business/shipments
```

### Request Body

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
  },
  "instructions": "Handle with care"
}
```

### Response

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

### Bulk Creation

Create multiple shipments at once:

```http
POST /api/business/shipments/bulk
```

Request:

```json
{
  "shipments": [
    { /* shipment 1 */ },
    { /* shipment 2 */ },
    { /* shipment 3 */ }
  ]
}
```

Response includes results for each shipment.

---

## Tracking Packages

### Get Shipment Status

```http
GET /api/business/shipments/{trackingNumber}
```

### Response

```json
{
  "success": true,
  "data": {
    "trackingNumber": "RC123456789ZA",
    "status": "in_transit",
    "currentLocation": {
      "latitude": -26.2041,
      "longitude": 28.0473,
      "address": "Pretoria, Gauteng"
    },
    "history": [
      {
        "status": "booked",
        "timestamp": "2024-01-01T10:00:00Z",
        "location": "Johannesburg"
      },
      {
        "status": "collected",
        "timestamp": "2024-01-01T14:00:00Z",
        "location": "Johannesburg"
      }
    ],
    "estimatedDelivery": "2024-01-03T16:00:00Z"
  }
}
```

### Track Multiple Packages

```http
POST /api/business/shipments/track
```

Request:

```json
{
  "trackingNumbers": [
    "RC123456789ZA",
    "RC987654321ZA",
    "RC456789123ZA"
  ]
}
```

### Get All Your Shipments

```http
GET /api/business/shipments?status=all&page=1&limit=50
```

Query Parameters:
- `status`: Filter by status (all, booked, in_transit, delivered)
- `page`: Page number
- `limit`: Items per page
- `from`: Start date
- `to`: End date

---

## Payment & Billing

### Payment Methods

#### 1. Per-Transaction Payment
- Pay immediately for each shipment
- No credit required
- Instant processing

#### 2. Credit Terms
- Approved businesses only
- Monthly billing
- Net 30 payment terms
- Credit limit based on account

### Checkout Process

#### Option 1: Include Payment in Request

```json
{
  "payment": {
    "method": "card",
    "cardToken": "card_token_from_paystack"
  },
  // ... shipment details
}
```

#### Option 2: Pay Later

Create shipment without payment, then:

```http
POST /api/business/shipments/{trackingNumber}/pay
```

### Invoice Management

#### View Invoices

```http
GET /api/business/invoices
```

#### Download Invoice

```http
GET /api/business/invoices/{invoiceId}/download
```

Returns PDF invoice.

### Payment History

```http
GET /api/business/payments
```

View all payment transactions.

---

## Business Dashboard

### Accessing Dashboard

1. Log into business account
2. View comprehensive dashboard

### Dashboard Features

#### Overview Statistics
- **Total Shipments**: All time count
- **This Month**: Current month shipments
- **Success Rate**: % successful deliveries
- **Average Delivery Time**: Average days
- **Total Spent**: Monthly spending

#### Recent Activity
- Latest shipments
- Status updates
- Payment transactions
- Important notifications

#### Shipment Management
- View all shipments
- Filter by status
- Search by tracking number
- Export data

#### Analytics
- Shipment volume trends
- Cost analysis
- Delivery performance
- Customer insights

---

## Webhooks

### What Are Webhooks?

Webhooks notify your system when events occur:
- Shipment status changes
- Delivery confirmations
- Payment updates
- Error notifications

### Setting Up Webhooks

1. Go to **"Webhooks"** in dashboard
2. Click **"Add Webhook"**
3. Enter:
   - **URL**: Your webhook endpoint
   - **Events**: Select events to receive
   - **Secret**: Webhook secret (for verification)
4. Save webhook

### Webhook Events

Available events:
- `shipment.booked`: Shipment created
- `shipment.collected`: Package collected
- `shipment.in_transit`: Package in transit
- `shipment.out_for_delivery`: Out for delivery
- `shipment.delivered`: Delivered successfully
- `shipment.failed`: Delivery failed
- `payment.completed`: Payment received
- `payment.failed`: Payment failed

### Webhook Payload

```json
{
  "event": "shipment.delivered",
  "timestamp": "2024-01-01T16:00:00Z",
  "data": {
    "trackingNumber": "RC123456789ZA",
    "shipmentId": "shp_abc123",
    "status": "delivered",
    "deliveredAt": "2024-01-01T16:00:00Z",
    "recipient": {
      "name": "Jane Smith",
      "signature": "signature_url"
    }
  },
  "signature": "webhook_signature_for_verification"
}
```

### Verifying Webhooks

Always verify webhook signatures:

```javascript
const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const hash = crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(payload))
    .digest('hex');
  
  return hash === signature;
}
```

---

## Best Practices

### Security

- ✅ **Keep API keys secure**: Never expose in client-side code
- ✅ **Use HTTPS**: Always use encrypted connections
- ✅ **Verify webhooks**: Always verify signatures
- ✅ **Rotate keys**: Regularly rotate API keys
- ✅ **Monitor usage**: Watch for suspicious activity

### Performance

- ⚡ **Use bulk operations**: Create multiple shipments at once
- ⚡ **Cache tracking data**: Cache shipment status
- ⚡ **Handle rate limits**: Implement retry logic
- ⚡ **Use webhooks**: Instead of polling for updates

### Error Handling

- 🛡️ **Retry logic**: Implement exponential backoff
- 🛡️ **Log errors**: Keep error logs
- 🛡️ **Handle failures**: Graceful error handling
- 🛡️ **Monitor status**: Watch API status

### Integration Tips

- 📝 **Test in sandbox**: Test before production
- 📝 **Validate data**: Validate before sending
- 📝 **Handle async**: Webhooks are async
- 📝 **Document integration**: Keep documentation

---

## Code Examples

### JavaScript/Node.js

```javascript
const axios = require('axios');

const apiClient = axios.create({
  baseURL: 'https://api.reliablecarriers.co.za/api/business',
  headers: {
    'Authorization': `Bearer ${process.env.API_KEY}`,
    'X-API-Secret': process.env.API_SECRET,
    'Content-Type': 'application/json'
  }
});

// Create shipment
async function createShipment(shipmentData) {
  try {
    const response = await apiClient.post('/shipments', shipmentData);
    return response.data;
  } catch (error) {
    console.error('Error creating shipment:', error.response.data);
    throw error;
  }
}

// Track shipment
async function trackShipment(trackingNumber) {
  try {
    const response = await apiClient.get(`/shipments/${trackingNumber}`);
    return response.data;
  } catch (error) {
    console.error('Error tracking shipment:', error.response.data);
    throw error;
  }
}
```

### Python

```python
import requests

API_BASE = "https://api.reliablecarriers.co.za/api/business"
API_KEY = "your_api_key"
API_SECRET = "your_api_secret"

headers = {
    "Authorization": f"Bearer {API_KEY}",
    "X-API-Secret": API_SECRET,
    "Content-Type": "application/json"
}

def create_shipment(shipment_data):
    response = requests.post(
        f"{API_BASE}/shipments",
        json=shipment_data,
        headers=headers
    )
    response.raise_for_status()
    return response.json()

def track_shipment(tracking_number):
    response = requests.get(
        f"{API_BASE}/shipments/{tracking_number}",
        headers=headers
    )
    response.raise_for_status()
    return response.json()
```

---

## Support

### API Support

- 📧 **Email**: api@reliablecarriers.co.za
- 📚 **Documentation**: https://docs.reliablecarriers.co.za
- 💬 **Support Chat**: Available in dashboard
- 📞 **Phone**: [API Support Line]

### Integration Support

- **Onboarding**: Dedicated support for new integrations
- **Technical Support**: Help with API integration
- **Best Practices**: Guidance on implementation

---

**Thank you for partnering with Reliable Carriers!** 🚀

