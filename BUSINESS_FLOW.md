# Business Customer Flow - Complete Process

This document explains the complete flow for business customers from registration to using the courier service with API integration, bulk shipping, and advanced features.

## 📋 Overview

Business customers have a distinct flow from individual customers, with additional features:
- **Business Registration** → **Document Upload** → **Verification** → **API Access** → **Bulk Shipping** → **Credit Terms** → **Analytics**

---

## 1️⃣ Business Registration

### Customer Journey:
- Business visits `/register/business` page
- Fills in business registration form with:
  - **Personal Information**: First name, last name, email, phone
  - **Business Information**:
    - Business name (required)
    - Business registration number (CIPC)
    - Tax ID / VAT number
    - Business address
  - **Account Details**: Password, confirmation
  - **Terms & Conditions**: Accept waiver

### What Happens:
- System creates business account with `CustomerTier.BUSINESS`
- Business verification status set to `PENDING`
- Account created but **not yet verified**
- Email confirmation sent with next steps
- Business redirected to document upload page

### Business vs Individual Registration:
| Feature | Individual | Business |
|---------|-----------|----------|
| Form | Simple personal form | Enhanced business form |
| Required Fields | Name, email, phone | + Business name, registration number, tax ID |
| Verification | Email only | Email + Document verification |
| Account Type | `CUSTOMER` | `BUSINESS` |
| Tier | `STANDARD` | `BUSINESS` |

---

## 2️⃣ Document Upload & Verification

### Required Documents:
1. **Business Registration Certificate**
   - CIPC (Companies and Intellectual Property Commission) certificate
   - Must show registration number matching provided number
   - Certified copy accepted

2. **Tax/VAT Documentation**
   - VAT/Tax registration certificate
   - SARS tax clearance certificate (optional but recommended)
   - Tax ID must match provided tax ID

3. **Proof of Business Address**
   - Business premises lease/ownership documents
   - Utility bills in business name (not older than 3 months)
   - Municipal rates account

4. **Identity Verification**
   - Director/owner ID document (certified copy)
   - Proof of authority to represent business

### Upload Process:
1. Business navigates to document upload page
2. Selects document type from dropdown
3. Uploads file (PDF, PNG, JPG supported)
4. Marks as certified copy if applicable
5. Provides certification details
6. Submits for review

### Verification Status Flow:
```
PENDING → UNDER_REVIEW → APPROVED/VERIFIED
                ↓
            REJECTED (can resubmit)
```

### Status Meanings:
- **PENDING**: Documents uploaded, awaiting admin review
- **UNDER_REVIEW**: Admin is reviewing documents
- **APPROVED**: Manual verification completed, account verified
- **VERIFIED**: Automated verification completed (CIPC/SARS integration)
- **REJECTED**: Documents rejected, reason provided, can resubmit

---

## 3️⃣ Admin Verification Process

### Admin Review:
1. Admin receives notification of new business registration
2. Admin reviews uploaded documents in admin dashboard
3. Admin verifies:
   - Business registration number (CIPC database)
   - Tax ID validity
   - Document authenticity
   - Business information matches

### Verification Actions:
- **Approve**: Business account verified, features enabled
- **Reject**: Specify rejection reason, business can resubmit
- **Request More Info**: Request additional documents

### Automated Verification (Future):
- **CIPC Integration**: Verify business registration automatically
- **SARS Integration**: Verify tax compliance automatically
- **OCR Processing**: Extract information from documents automatically

---

## 4️⃣ Post-Verification: Account Activation

### Once Verified:
✅ **Business Features Enabled**:
- API access and API key generation
- Bulk shipping capabilities
- Credit terms (Net 30 payment)
- 5% discount on all shipments
- Priority customer support
- Business analytics dashboard
- Webhook integration
- Corporate invoicing

### Email Notification:
- Verification confirmation email sent
- Includes:
  - Welcome message
  - Account activation confirmation
  - Access to business dashboard
  - API documentation link
  - Support contact information

---

## 5️⃣ Business Dashboard & Features

### Dashboard Access:
- Business logs in at `/login`
- Redirected to business dashboard (`/customer/dashboard`)
- Shows:
  - Account status and verification status
  - Recent shipments
  - Shipping statistics
  - Account balance (for credit accounts)
  - API usage statistics

### Available Features:

#### A. API Key Management
- **Location**: `/customer/api-keys`
- **Features**:
  - Generate new API keys
  - View all API keys
  - Revoke API keys
  - Set rate limits
  - View usage statistics
  - Key expiration management

#### B. Web Interface Shipping
- **Create Shipments**: Web form for single shipments
- **Bulk Shipping**: Upload CSV/Excel for multiple shipments
- **Saved Addresses**: Save frequently used addresses
- **Quick Ship**: One-click shipping from saved templates

#### C. API Integration
- **REST API**: Full programmatic access
- **Endpoints**: 15+ endpoints for all operations
- **Authentication**: API key in header
- **Rate Limiting**: Configurable per key
- **Webhooks**: Real-time event notifications

---

## 6️⃣ Creating Shipments (Multiple Methods)

### Method 1: Web Interface (Single Shipment)
1. Navigate to "Create Shipment"
2. Fill in shipment details:
   - Pickup address
   - Delivery address
   - Package details (weight, dimensions)
   - Service type
   - Additional services
3. Get quote
4. Confirm and create shipment
5. Payment (if not on credit terms)

### Method 2: Bulk Shipping (CSV/Excel)
1. Navigate to "Bulk Shipping"
2. Download template CSV/Excel
3. Fill in multiple shipments
4. Upload file
5. Review and validate
6. Submit for processing
7. Receive confirmation with tracking numbers

### Method 3: API Integration
```bash
POST /api/business/shipments
Headers: X-API-Key: rc_your_api_key_here
Body: {
  "serviceType": "STANDARD",
  "pickupAddress": "...",
  "deliveryAddress": "...",
  "weight": 5.5,
  ...
}
```

### Method 4: Easy Shipping (Quick)
1. Navigate to "Easy Shipping"
2. Select saved template
3. Modify if needed
4. Submit
5. Shipment created instantly

---

## 7️⃣ Payment & Credit Terms

### Payment Options:

#### For Verified Businesses:
- **Credit Terms**: Net 30 payment terms
- **Credit Limit**: Set based on verification and credit check
- **Invoice Generation**: Monthly invoices
- **Payment Methods**: Bank transfer, EFT, credit card

#### For Unverified Businesses:
- **Immediate Payment**: Required before shipment processing
- **Payment Gateway**: Paystack integration
- **Payment Methods**: Credit card, bank transfer

### Credit Account Management:
- **Account Balance**: Track current balance
- **Credit Limit**: Maximum credit allowed
- **Payment History**: View all payments
- **Invoice History**: Download invoices
- **Payment Reminders**: Automatic reminders before due date

### Payment Flow:
1. Shipment created
2. If credit account: Added to account balance
3. If immediate payment: Payment required
4. Monthly invoice generated (credit accounts)
5. Payment due within 30 days
6. Payment processed
7. Account balance updated

---

## 8️⃣ API Integration Flow

### Step 1: Generate API Key
1. Navigate to API Keys page
2. Click "Generate New API Key"
3. Provide key name and description
4. Set rate limit (default: 1000/hour)
5. Copy API key (shown only once)
6. Store securely

### Step 2: Integrate API
```javascript
// Example: Track shipment
fetch('https://api.reliablecarriers.co.za/api/business/tracking/RC12345678', {
  headers: {
    'X-API-Key': 'rc_your_api_key_here'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Shipment status:', data);
});
```

### Step 3: Create Shipments via API
```javascript
// Example: Create shipment
fetch('https://api.reliablecarriers.co.za/api/business/shipments', {
  method: 'POST',
  headers: {
    'X-API-Key': 'rc_your_api_key_here',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    serviceType: 'STANDARD',
    pickupAddress: '123 Business St, Sandton',
    deliveryAddress: '456 Customer Ave, Cape Town',
    weight: 5.5,
    dimensions: '30x20x15'
  })
})
.then(response => response.json())
.then(data => {
  console.log('Shipment created:', data);
});
```

### Step 4: Set Up Webhooks
1. Navigate to Webhooks page
2. Create new webhook
3. Provide webhook URL
4. Select events to subscribe to:
   - `shipment.created`
   - `shipment.updated`
   - `shipment.delivered`
   - `shipment.failed`
5. Save webhook
6. Test webhook

---

## 9️⃣ Business Analytics & Reporting

### Analytics Dashboard:
- **Shipping Statistics**:
  - Total shipments
  - Shipments by status
  - Shipments by service type
  - Monthly trends
  - Revenue tracking

- **API Usage Statistics**:
  - Total API calls
  - Calls by endpoint
  - Response times
  - Error rates
  - Rate limit usage

- **Financial Reports**:
  - Account balance
  - Monthly spending
  - Payment history
  - Invoice history
  - Cost per shipment

### Export Options:
- **CSV Export**: Download shipment data
- **PDF Reports**: Monthly/quarterly reports
- **Excel Export**: Detailed analytics

---

## 🔟 Business Pricing & Discounts

### Pricing Structure:
- **Base Pricing**: Same as individual customers
- **Business Discount**: **5% automatic discount** on all shipments
- **Volume Discounts**: Available for high-volume businesses (contact sales)

### Pricing Example:
```
Regular Price: R500.00
Business Discount (5%): -R25.00
Final Price: R475.00
```

### Credit Terms:
- **Net 30**: Payment due 30 days after invoice date
- **Credit Limit**: Based on business verification and credit check
- **Interest**: No interest if paid within terms
- **Late Fees**: May apply if payment overdue

---

## 🔄 Complete Flow Diagram

```
┌─────────────────────┐
│ Business Registration│
│  (/register/business)│
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Account Created    │
│  Status: PENDING    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Document Upload     │
│  (Required Docs)    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Admin Review        │
│  Status: UNDER_REVIEW│
└──────────┬──────────┘
           │
           ▼
    ┌──────┴──────┐
    │             │
    ▼             ▼
┌─────────┐  ┌─────────┐
│ APPROVED│  │ REJECTED│
│ VERIFIED│  │(Resubmit)│
└────┬────┘  └────┬────┘
     │            │
     └────────────┘
           │
           ▼
┌─────────────────────┐
│ Features Enabled    │
│ - API Access        │
│ - Credit Terms      │
│ - 5% Discount       │
│ - Bulk Shipping     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Create Shipments    │
│ - Web Interface     │
│ - API Integration   │
│ - Bulk Upload       │
│ - Easy Shipping     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Payment Processing  │
│ - Credit Terms      │
│ - Immediate Payment │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Tracking & Analytics│
│ - Real-time Tracking│
│ - Webhooks          │
│ - Analytics Dashboard│
└─────────────────────┘
```

---

## 📊 Business vs Individual Comparison

| Feature | Individual | Business |
|---------|-----------|----------|
| **Registration** | Simple form | Business form + documents |
| **Verification** | Email only | Email + document verification |
| **API Access** | ❌ | ✅ (after verification) |
| **Bulk Shipping** | ❌ | ✅ |
| **Credit Terms** | Immediate payment | Net 30 (after verification) |
| **Discount** | None | 5% on all shipments |
| **Package Limit** | 10/month | 100/month |
| **Analytics** | Basic | Comprehensive |
| **Webhooks** | ❌ | ✅ |
| **Support** | Standard | Priority |
| **Invoicing** | Receipt only | Corporate invoices |
| **API Keys** | ❌ | ✅ Multiple keys |
| **Rate Limiting** | N/A | Configurable |

---

## 🔐 Security & Compliance

### API Security:
- **SHA-256 Hashing**: API keys securely hashed
- **HMAC Signatures**: Webhook signature verification
- **Rate Limiting**: Per-key rate limiting
- **IP Tracking**: Request origin tracking
- **Usage Logging**: Detailed request logging

### Data Security:
- **Encrypted Storage**: Documents encrypted at rest
- **Secure Transfer**: HTTPS/TLS for all communications
- **Access Control**: Role-based access control
- **Audit Logging**: All actions logged

### Compliance:
- **POPI Act**: Personal information protection
- **GDPR**: European data protection (if applicable)
- **CIPC Compliance**: Business registration verification
- **SARS Compliance**: Tax information handling

---

## 📧 Notifications

### Email Notifications:
1. **Registration Confirmation**: Welcome email with next steps
2. **Document Upload**: Confirmation of upload
3. **Verification Status**: Status change notifications
4. **Account Approved**: Welcome and feature access
5. **Shipment Created**: Confirmation with tracking
6. **Shipment Updates**: Status change notifications
7. **Payment Reminders**: For credit accounts
8. **Invoice Generated**: Monthly invoices
9. **API Key Generated**: Security notification

### SMS Notifications:
- Shipment status updates
- Payment reminders
- Verification status changes

### Webhook Notifications:
- Real-time event notifications
- Configurable event subscriptions
- Automatic retry on failure

---

## 📞 Support & Resources

### Business Support:
- **Priority Support**: Dedicated business support team
- **Email**: business@reliablecarriers.co.za
- **Phone**: 0800-RELIABLE (Business line)
- **Live Chat**: Available on dashboard
- **API Documentation**: `/help-center/api`

### Resources:
- **API Documentation**: Complete API reference
- **Integration Guides**: Step-by-step guides
- **SDK/Libraries**: Available for popular languages
- **Webhook Guide**: Webhook setup and testing
- **FAQ**: Business-specific FAQs

---

## ⚠️ Important Notes

### Verification Requirements:
- **All documents must be certified copies**
- **Documents must be valid and not expired**
- **Business information must match registration**
- **Verification can take 1-3 business days**

### API Access:
- **Only available after verification**
- **API keys must be kept secure**
- **Rate limits apply per key**
- **Usage is monitored and logged**

### Credit Terms:
- **Only available after verification**
- **Credit limit set by admin**
- **Payment due within 30 days**
- **Late payments may affect credit limit**

### Bulk Shipping:
- **CSV/Excel format required**
- **Maximum 100 shipments per upload**
- **Validation performed before processing**
- **Failed shipments reported separately**

---

## 🎯 Key Features Summary

✅ **Separate Business Registration**: Dedicated business form
✅ **Document Verification**: Upload and verify business documents
✅ **Admin Approval**: Manual review and approval process
✅ **API Integration**: Full REST API access
✅ **API Key Management**: Secure key generation and management
✅ **Bulk Shipping**: Upload multiple shipments at once
✅ **Credit Terms**: Net 30 payment terms
✅ **5% Discount**: Automatic discount on all shipments
✅ **Webhooks**: Real-time event notifications
✅ **Analytics Dashboard**: Comprehensive statistics and reports
✅ **Priority Support**: Enhanced customer support
✅ **Corporate Invoicing**: Monthly invoices for credit accounts

---

**Last Updated**: 2025-01-05
**Version**: 1.0

