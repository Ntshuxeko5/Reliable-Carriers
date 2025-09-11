# Paystack Integration Guide - Reliable Carriers

## Overview
This guide explains how to set up and use the Paystack payment gateway integration in the Reliable Carriers application.

## Features Implemented

### 1. Backend Integration
- **PaystackService**: Core service for handling Paystack API interactions
- **PaystackController**: REST endpoints for payment operations
- **DTOs**: Data transfer objects for Paystack requests and responses
- **Payment Verification**: Automatic payment status verification
- **Error Handling**: Comprehensive error handling and logging

### 2. Frontend Integration
- **Payment Page**: Complete payment interface with Paystack
- **Real-time Status**: Live payment status updates
- **Responsive Design**: Mobile-friendly payment interface
- **Security**: Secure payment processing with encryption

### 3. Payment Flow
1. User initiates payment
2. System creates payment record
3. Paystack payment modal opens
4. User completes payment
5. System verifies payment
6. Payment status updated
7. User receives confirmation

## Setup Instructions

### 1. Paystack Account Setup
1. Create a Paystack account at [paystack.com](https://paystack.com)
2. Get your API keys from the dashboard
3. Configure webhook endpoints (optional)

### 2. Environment Configuration
Add the following to your `application.properties`:

```properties
# Paystack Configuration
paystack.secret.key=sk_test_your_paystack_secret_key_here
paystack.public.key=pk_test_your_paystack_public_key_here
paystack.base.url=https://api.paystack.co

# Application Configuration
app.base.url=http://localhost:8080
```

### 3. API Keys
- **Test Keys**: Use for development and testing
- **Live Keys**: Use for production (replace `sk_test_` with `sk_live_`)

## API Endpoints

### 1. Initialize Payment
```http
POST /api/paystack/initialize
Content-Type: application/json

{
    "shipmentId": 123,
    "amount": 5500.00,
    "email": "customer@example.com"
}
```

**Response:**
```json
{
    "status": "success",
    "authorizationUrl": "https://checkout.paystack.com/...",
    "reference": "PAY_1234567890",
    "accessCode": "abc123def456"
}
```

### 2. Verify Payment
```http
GET /api/paystack/verify?reference=PAY_1234567890
```

**Response:**
```json
{
    "status": "success",
    "paymentStatus": "COMPLETED",
    "message": "Payment verification completed"
}
```

### 3. Get Public Key
```http
GET /api/paystack/public-key
```

**Response:**
```json
{
    "publicKey": "pk_test_your_public_key_here"
}
```

### 4. Create Simple Payment
```http
POST /api/paystack/create-payment
Content-Type: application/json

{
    "amount": 5500.00,
    "email": "customer@example.com"
}
```

## Frontend Implementation

### 1. Payment Page
Access the payment page at `/payment`

### 2. JavaScript Integration
```javascript
// Initialize Paystack
const handler = PaystackPop.setup({
    key: paystackPublicKey,
    email: customerEmail,
    amount: amountInKobo,
    currency: 'NGN',
    ref: reference,
    callback: function(response) {
        // Handle successful payment
        verifyPayment(response.reference);
    },
    onClose: function() {
        // Handle modal close
    }
});
handler.openIframe();
```

### 3. Payment Verification
```javascript
function verifyPayment(reference) {
    fetch(`/api/paystack/verify?reference=${reference}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                showPaymentSuccess();
            } else {
                showPaymentFailed();
            }
        });
}
```

## Payment Methods Supported

### 1. Card Payments
- Visa
- Mastercard
- Verve
- American Express

### 2. Bank Transfers
- Direct bank transfers
- USSD payments
- Mobile money

### 3. Digital Wallets
- Apple Pay
- Google Pay
- Samsung Pay

## Security Features

### 1. Data Encryption
- All payment data encrypted in transit
- Secure API communication
- PCI DSS compliance

### 2. Fraud Prevention
- 3D Secure authentication
- Address verification
- CVV validation

### 3. Webhook Verification
- Signature verification
- IP whitelisting
- Request validation

## Testing

### 1. Test Cards
Use these test card numbers for testing:

**Successful Payment:**
- Card: 4084 0840 8408 4081
- Expiry: Any future date
- CVV: Any 3 digits

**Failed Payment:**
- Card: 4084 0840 8408 4082
- Expiry: Any future date
- CVV: Any 3 digits

### 2. Test Scenarios
1. **Successful Payment**: Complete payment flow
2. **Failed Payment**: Test error handling
3. **Cancelled Payment**: Test user cancellation
4. **Network Issues**: Test timeout handling

## Error Handling

### 1. Common Errors
- **Invalid API Key**: Check your secret key
- **Invalid Amount**: Amount must be in kobo
- **Invalid Email**: Email format validation
- **Network Error**: Check internet connection

### 2. Error Responses
```json
{
    "status": "error",
    "message": "Payment initialization failed",
    "code": "PAYMENT_ERROR"
}
```

## Monitoring and Logging

### 1. Payment Logs
- All payment attempts logged
- Success/failure tracking
- Transaction history

### 2. Analytics
- Payment success rates
- Average transaction value
- Popular payment methods

## Production Deployment

### 1. Environment Variables
Set these environment variables in production:

```bash
PAYSTACK_SECRET_KEY=sk_live_your_live_secret_key
PAYSTACK_PUBLIC_KEY=pk_live_your_live_public_key
APP_BASE_URL=https://yourdomain.com
```

### 2. SSL Certificate
- Ensure HTTPS is enabled
- Valid SSL certificate required
- Secure cookie settings

### 3. Webhook Configuration
Configure webhooks in Paystack dashboard:
- URL: `https://yourdomain.com/api/paystack/webhook`
- Events: `charge.success`, `charge.failed`

## Troubleshooting

### 1. Payment Not Initializing
- Check API keys
- Verify network connectivity
- Check browser console for errors

### 2. Payment Verification Fails
- Verify reference format
- Check server logs
- Ensure callback URL is correct

### 3. Amount Mismatch
- Ensure amount is in kobo (multiply by 100)
- Check currency setting
- Verify decimal precision

## Best Practices

### 1. Security
- Never expose secret keys in frontend
- Use HTTPS in production
- Implement proper error handling

### 2. User Experience
- Show loading states
- Provide clear error messages
- Implement retry mechanisms

### 3. Testing
- Test with real cards in test mode
- Verify all payment methods
- Test error scenarios

## Support

### 1. Paystack Support
- Documentation: [docs.paystack.com](https://docs.paystack.com)
- Support: support@paystack.com
- Status: [status.paystack.com](https://status.paystack.com)

### 2. Application Support
- Check application logs
- Verify configuration
- Test with sample data

## Conclusion

The Paystack integration provides a secure, reliable, and user-friendly payment solution for Reliable Carriers. The implementation includes comprehensive error handling, security features, and testing capabilities to ensure a smooth payment experience for customers.
