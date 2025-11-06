# Quote to Booking Flow - Complete Process

This document explains what happens after a client receives a quote and how they proceed to book a shipment.

## 📋 Overview

The complete flow from quote to shipment delivery:

1. **Get Quote** → 2. **Select Service** → 3. **Book Service** → 4. **Payment** → 5. **Confirmation** → 6. **Shipment Creation** → 7. **Tracking**

---

## 1️⃣ Getting a Quote

### Customer Journey:
- Customer visits `/quote` page
- Fills in package details:
  - Pickup and delivery addresses
  - Package dimensions (length, width, height)
  - Weight (kg)
  - Package type (document, parcel, fragile, valuable)
  - Service type preference (economy, standard, express, same-day)
  - Optional: Insurance, special instructions

### What Happens:
- System calculates distance using Google Maps API (or fallback estimation)
- Calculates volume weight: `(L × W × H) / 5000`
- Uses chargeable weight: `max(actual weight, volume weight)`
- Applies service-specific base price and distance/weight charges
- Adds fees: service fee (3%), fuel surcharge (5%), insurance (2% if selected)
- Generates quote with 7-day expiry

### Quote Details:
- **Quote ID**: Unique identifier
- **Price Breakdown**: Base price, distance charge, weight charge, fees
- **Service Options**: Multiple service tiers with different pricing
- **Estimated Delivery**: Date and time window
- **Quote Expiry**: 7 days from generation

---

## 2️⃣ Selecting a Service

### Customer Journey:
- Customer reviews all available service options on the quote page
- Each service shows:
  - **Price**: Total cost
  - **Delivery Time**: Estimated delivery window
  - **Description**: Service features
  - **Book Button**: Proceeds to booking

### Service Options:
1. **Economy** (4-7 Business Days)
   - Most affordable
   - Best for non-urgent deliveries
   - Standard handling

2. **Standard** (2-3 Business Days)
   - Balanced price and speed
   - Most popular choice
   - Priority handling

3. **Express** (Same-day & Next-day)
   - Fast delivery
   - Premium handling
   - Guaranteed time slots

4. **Same Day** (Same Day Delivery)
   - Fastest option
   - Urgent deliveries only
   - Premium pricing

---

## 3️⃣ Proceeding to Booking

### Customer Journey:
- Customer clicks "Book This Service" button
- System stores quote data in `sessionStorage`
- Redirects to `/booking` page

### What Gets Stored:
```javascript
{
  quoteId: "QUOTE-123456",
  selectedService: {
    type: "standard",
    price: 750.00,
    deliveryTime: "2-3 Business Days"
  },
  packageDetails: { ... },
  addresses: { ... },
  // ... all quote data
}
```

---

## 4️⃣ Booking Page

### Customer Journey:
- **Pre-filled Data**: Quote information is automatically loaded
- **Additional Details**:
  - Sender information (if not logged in)
  - Recipient contact details
  - Pickup date/time preference
  - Special instructions
  - Additional services (packing, signature required, etc.)

### Address Validation:
- Google Maps integration for address autocomplete
- Address validation and geocoding
- Distance recalculation for accuracy

### Booking Confirmation:
- Customer reviews all details
- Confirms pricing
- Clicks "Confirm Booking"

---

## 5️⃣ Payment Processing

### Payment Options:
1. **Paystack Payment Gateway**
   - Credit/Debit cards
   - Bank transfers
   - Mobile money (if available)

### Payment Flow:
1. Customer clicks "Pay Now"
2. Redirected to Paystack payment page
3. Completes payment
4. Paystack webhook confirms payment
5. System processes payment confirmation

### Payment Details Stored:
- Payment reference
- Transaction ID
- Payment amount
- Payment date/time
- Payment status

---

## 6️⃣ Booking Confirmation

### What Happens After Payment:
1. **Booking Status Updated**: `PENDING` → `CONFIRMED`
2. **Payment Status**: `COMPLETED`
3. **Customer Codes Generated**:
   - Pickup verification code
   - Delivery verification code
4. **Shipment Created**: Automatic shipment creation from booking
5. **Tracking Number Generated**: Unique tracking number assigned
6. **Notifications Sent**:
   - Email confirmation with codes
   - SMS confirmation (if phone provided)
   - Booking confirmation notification

### Email Confirmation Includes:
- Booking reference number
- Tracking number
- Pickup verification code
- Delivery verification code
- Delivery instructions
- Contact information
- Expected delivery date

---

## 7️⃣ Shipment Creation

### Automatic Shipment Creation:
When booking is confirmed, a shipment is automatically created:

```java
Shipment shipment = new Shipment();
shipment.setTrackingNumber(generateTrackingNumber());
shipment.setSender(booking.getCustomer());
shipment.setRecipientName(booking.getDeliveryContactName());
shipment.setPickupAddress(booking.getPickupAddress());
shipment.setDeliveryAddress(booking.getDeliveryAddress());
shipment.setShippingCost(booking.getTotalAmount());
shipment.setServiceType(booking.getServiceType());
shipment.setStatus(ShipmentStatus.PENDING);
```

### Initial Tracking Entry:
- Status: `PENDING`
- Message: "Shipment created"
- Description: "Shipment registered in the system"
- Timestamp: Creation date/time

---

## 8️⃣ Quote Status Updates

### After Booking:
- Quote marked as `isActive = false`
- Quote cannot be reused
- New quote required for changes

### Quote Expiry:
- Quotes expire after 7 days
- Expired quotes cannot be used for booking
- Customer must generate a new quote

---

## 🔄 Flow Diagram

```
┌─────────────┐
│ Get Quote   │
│  (/quote)   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Review      │
│ Services    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Select      │
│ Service     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Booking     │
│  (/booking) │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Payment     │
│  (Paystack) │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Confirmation│
│  (Email/SMS)│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Shipment    │
│  Created    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Tracking    │
│  Available  │
└─────────────┘
```

---

## 📱 User Experience Flow

### For Logged-In Users:
1. Quote page shows saved addresses
2. Quick booking with saved payment methods
3. Booking history available
4. Automatic email notifications

### For Guests:
1. Must provide all details manually
2. Can create account after booking
3. Booking confirmation email with account creation link
4. Access to tracking without account

---

## 🔐 Security Features

### Quote Protection:
- Quote ID validation
- Quote expiry checking
- Quote status verification
- One-time use per quote

### Payment Security:
- Secure payment gateway (Paystack)
- Payment verification
- Transaction logging
- Fraud detection

### Verification Codes:
- Unique pickup code
- Unique delivery code
- Time-limited validity
- Secure generation

---

## 📧 Notifications

### Email Notifications:
1. **Quote Generated**: Quote details and expiry
2. **Booking Confirmed**: Confirmation with codes
3. **Payment Successful**: Receipt and tracking info
4. **Shipment Status Updates**: Tracking updates

### SMS Notifications:
1. Booking confirmation
2. Payment confirmation
3. Shipment status updates
4. Delivery notifications

---

## ⚠️ Important Notes

### Quote Expiry:
- Quotes are valid for **7 days**
- Expired quotes cannot be used
- Generate new quote for expired ones

### Payment Window:
- Payment must be completed within **24 hours** of booking
- Unpaid bookings expire after 24 hours
- Booking can be cancelled before payment

### Changes After Booking:
- **Before Payment**: Can update booking details
- **After Payment**: Contact customer service for changes
- **After Shipment Created**: Changes may incur fees

### Cancellations:
- **Before Payment**: Free cancellation
- **After Payment**: Refund policy applies
- **After Shipment**: Contact customer service

---

## 🎯 Key Features

✅ **Instant Quotes**: Real-time pricing calculation
✅ **Multiple Service Options**: Economy to Same-Day
✅ **Secure Payment**: Paystack integration
✅ **Automatic Shipment Creation**: Seamless flow
✅ **Tracking Integration**: Immediate tracking availability
✅ **Email/SMS Notifications**: Keep customers informed
✅ **Verification Codes**: Secure pickup/delivery
✅ **Quote Expiry Management**: Prevents stale quotes

---

## 📞 Support

If customers have questions about quotes or bookings:
- **Email**: support@reliablecarriers.co.za
- **Phone**: 0800-RELIABLE
- **Live Chat**: Available on website
- **Help Center**: `/help-center` for documentation

---

**Last Updated**: 2025-01-05
**Version**: 1.0

