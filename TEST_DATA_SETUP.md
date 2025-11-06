# Test Data Setup Guide

This guide helps you set up test data for comprehensive testing of the Reliable Carriers application.

## Quick Setup

```bash
# Run the test data setup script
./scripts/setup-test-data.sh
```

Or manually create test data using the SQL scripts or API endpoints below.

---

## Test Users

### Admin User
```sql
INSERT INTO users (email, password, first_name, last_name, role, enabled, created_at, updated_at)
VALUES (
    'admin@reliablecarriers.co.za',
    '$2a$10$YourHashedPasswordHere', -- Use BCrypt hash for 'admin123'
    'Admin',
    'User',
    'ADMIN',
    true,
    NOW(),
    NOW()
);
```

Or use the API endpoint:
```bash
POST /create-test-users
```

### Test Users by Role

#### Customer Users
- Email: `customer1@test.com` / Password: `password123`
- Email: `customer2@test.com` / Password: `password123`

#### Driver Users
- Email: `driver1@test.com` / Password: `password123`
- Email: `driver2@test.com` / Password: `password123`

#### Business Users
- Email: `business1@test.com` / Password: `password123`
- Email: `business2@test.com` / Password: `password123`

---

## Test Documents

### Driver Documents

#### ID Document
- Type: ID_DOCUMENT
- Certified: Yes
- Certified By: Commissioner of Oaths
- Expiry Date: 2 years from now

#### Driver's License
- Type: DRIVERS_LICENSE
- Certified: Yes
- Certified By: Commissioner of Oaths
- Expiry Date: 1 year from now

#### Proof of Address
- Type: PROOF_OF_ADDRESS
- Certified: Yes
- Certified By: Commissioner of Oaths
- Expiry Date: 6 months from now

### Business Documents

#### Business Registration Certificate
- Type: BUSINESS_REGISTRATION
- Certified: Yes
- Certified By: Commissioner of Oaths
- Expiry Date: 1 year from now

#### Tax Clearance Certificate
- Type: TAX_CLEARANCE
- Certified: Yes
- Certified By: Commissioner of Oaths
- Expiry Date: 1 year from now

---

## Test Shipments

### Active Shipments
```json
{
  "recipientName": "John Doe",
  "recipientEmail": "john@example.com",
  "recipientPhone": "0821234567",
  "pickupAddress": "123 Main St, Johannesburg",
  "deliveryAddress": "456 Oak Ave, Cape Town",
  "weight": 5.5,
  "status": "IN_TRANSIT"
}
```

### Completed Shipments (for feedback testing)
- Status: DELIVERED
- Delivery date: Various dates for testing date ranges

---

## Test Feedback Data

### Positive Feedback
```json
{
  "overallRating": 5,
  "deliverySpeedRating": 5,
  "driverCourtesyRating": 5,
  "packageConditionRating": 5,
  "communicationRating": 5,
  "comments": "Excellent service!",
  "feedbackType": "POSITIVE"
}
```

### Negative Feedback
```json
{
  "overallRating": 2,
  "deliverySpeedRating": 2,
  "driverCourtesyRating": 3,
  "packageConditionRating": 2,
  "communicationRating": 2,
  "comments": "Package was delayed",
  "feedbackType": "COMPLAINT"
}
```

---

## SQL Scripts for Test Data

### Complete Test Data Setup

```sql
-- Test Users
-- (Use the /create-test-users endpoint or insert manually)

-- Test Documents with Expiry Dates
INSERT INTO driver_documents (
    driver_id, document_type, file_path, file_name, 
    is_certified, certified_by, certification_date,
    verification_status, expires_at, created_at, updated_at
) VALUES
-- Document expiring in 30 days
(1, 'DRIVERS_LICENSE', '/test/license1.pdf', 'license1.pdf',
 true, 'Commissioner of Oaths', NOW(),
 'VERIFIED', DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW()),
-- Document expiring in 14 days
(1, 'ID_DOCUMENT', '/test/id1.pdf', 'id1.pdf',
 true, 'Commissioner of Oaths', NOW(),
 'VERIFIED', DATE_ADD(NOW(), INTERVAL 14 DAY), NOW(), NOW()),
-- Document expiring in 7 days
(2, 'DRIVERS_LICENSE', '/test/license2.pdf', 'license2.pdf',
 true, 'Commissioner of Oaths', NOW(),
 'VERIFIED', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW()),
-- Expired document
(2, 'PROOF_OF_ADDRESS', '/test/address1.pdf', 'address1.pdf',
 true, 'Commissioner of Oaths', DATE_SUB(NOW(), INTERVAL 1 YEAR),
 'VERIFIED', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), NOW());

-- Test Shipments
INSERT INTO shipments (
    tracking_number, sender_id, recipient_name, recipient_email,
    pickup_address, delivery_address, weight, status, created_at, updated_at
) VALUES
('RC12345678', 1, 'Test Customer', 'customer@test.com',
 '123 Pickup St', '456 Delivery St', 10.5, 'DELIVERED',
 DATE_SUB(NOW(), INTERVAL 7 DAY), NOW());

-- Test Feedback
INSERT INTO customer_feedback (
    shipment_id, customer_email, overall_rating,
    delivery_speed_rating, driver_courtesy_rating,
    package_condition_rating, communication_rating,
    comments, feedback_type, created_at
) VALUES
(1, 'customer@test.com', 5, 5, 5, 5, 5,
 'Great service!', 'POSITIVE', DATE_SUB(NOW(), INTERVAL 5 DAY));
```

---

## API Endpoints for Test Data

### Create Test Users
```bash
POST /create-test-users
```

### Create Test Shipment
```bash
POST /api/customer/shipments
Authorization: Bearer <token>
Content-Type: application/json

{
  "recipientName": "Test Recipient",
  "recipientEmail": "recipient@test.com",
  "recipientPhone": "0821234567",
  "pickupAddress": "123 Test St, Johannesburg",
  "deliveryAddress": "456 Test Ave, Cape Town",
  "weight": 5.0
}
```

### Upload Test Document
```bash
POST /api/driver/documents
Authorization: Bearer <driver_token>
Content-Type: multipart/form-data

file: <test_document.pdf>
documentType: DRIVERS_LICENSE
isCertified: true
certifiedBy: Commissioner of Oaths
certificationDate: 2024-01-01
expiresAt: 2025-01-01
```

---

## Cleanup Test Data

### Reset Test Data
```bash
# Use the cleanup script
./scripts/cleanup-test-data.sh
```

### Manual Cleanup SQL
```sql
-- WARNING: Only use in test/development environments!

-- Delete test feedback
DELETE FROM customer_feedback WHERE customer_email LIKE '%@test.com';

-- Delete test shipments
DELETE FROM shipments WHERE recipient_email LIKE '%@test.com';

-- Delete test documents
DELETE FROM driver_documents WHERE file_path LIKE '/test/%';
DELETE FROM business_documents WHERE file_path LIKE '/test/%';

-- Delete test users (except admin)
DELETE FROM users WHERE email LIKE '%@test.com' AND role != 'ADMIN';
```

---

## Test Data Scenarios

### Scenario 1: Document Verification Flow
1. Create driver user
2. Upload documents (pending verification)
3. Login as admin
4. Verify documents
5. Check email notifications

### Scenario 2: Document Expiry Alerts
1. Create documents with various expiry dates:
   - 30 days from now
   - 14 days from now
   - 7 days from now
   - Expired
2. Run scheduled tasks
3. Verify email alerts sent
4. Verify expired documents marked

### Scenario 3: Feedback System
1. Create completed shipments
2. Send feedback request emails
3. Submit feedback
4. Generate PDF report
5. Export as CSV/Excel

### Scenario 4: End-to-End Shipment
1. Customer creates quote
2. Customer books shipment
3. Customer makes payment
4. Admin assigns driver
5. Driver accepts and picks up
6. Driver delivers
7. Customer receives feedback request
8. Customer submits feedback

---

## Test Data Files

### Sample Documents
Place test documents in `src/test/resources/documents/`:
- `test_id_document.pdf`
- `test_drivers_license.pdf`
- `test_business_certificate.pdf`

### Sample Images
- `test_photo.jpg` (for profile pictures)

---

## Environment Variables for Testing

```properties
# Test Email Configuration
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=test_username
spring.mail.password=test_password

# Test SMS Configuration (use test credentials)
sms.api.key=test_key
sms.api.secret=test_secret

# Test Paystack (use test keys)
paystack.secret.key=sk_test_...
paystack.public.key=pk_test_...

# Enable Test Mode
testing.enabled=true
```

---

## Troubleshooting

### Test Data Not Loading
- Check database connection
- Verify SQL scripts syntax
- Check foreign key constraints
- Verify user roles exist

### Documents Not Uploading
- Check file size limits
- Verify file types allowed
- Check storage directory permissions
- Verify multipart configuration

### Email Not Sending
- Check SMTP configuration
- Verify email credentials
- Check email service status
- Review email logs

---

**Last Updated**: 2024
**Version**: 1.0

