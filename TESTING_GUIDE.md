# Comprehensive Testing Guide for Reliable Carriers

This guide provides a complete testing strategy to ensure the application is production-ready before deployment.

## Table of Contents
1. [Testing Overview](#testing-overview)
2. [Pre-Deployment Testing Checklist](#pre-deployment-testing-checklist)
3. [Unit Testing](#unit-testing)
4. [Integration Testing](#integration-testing)
5. [Manual Testing Procedures](#manual-testing-procedures)
6. [Performance Testing](#performance-testing)
7. [Security Testing](#security-testing)
8. [User Acceptance Testing (UAT)](#user-acceptance-testing-uat)
9. [Test Data Setup](#test-data-setup)
10. [Automated Testing Scripts](#automated-testing-scripts)

---

## Testing Overview

### Testing Levels
- **Unit Tests**: Individual component testing
- **Integration Tests**: Component interaction testing
- **System Tests**: End-to-end functionality testing
- **Security Tests**: Vulnerability and security assessment
- **Performance Tests**: Load and stress testing
- **UAT**: User acceptance and business validation

### Testing Tools
- **JUnit 5**: Unit and integration testing
- **Spring Boot Test**: Spring-specific testing
- **MockMvc**: Web layer testing
- **TestContainers**: Database integration testing
- **Postman/Insomnia**: API testing
- **Selenium/Playwright**: Frontend testing (optional)

---

## Pre-Deployment Testing Checklist

### ✅ Critical Functionality Tests

#### Authentication & Authorization
- [ ] User registration (Customer, Driver, Business)
- [ ] User login with email/password
- [ ] OAuth2 login (Google, Facebook)
- [ ] JWT token generation and validation
- [ ] Password reset functionality
- [ ] Role-based access control (Admin, Staff, Driver, Customer, Business)
- [ ] Session management
- [ ] Logout functionality

#### Core Features
- [ ] Shipment creation and tracking
- [ ] Quote generation
- [ ] Payment processing (Paystack integration)
- [ ] Driver assignment
- [ ] Live tracking
- [ ] Email notifications
- [ ] SMS notifications

#### Document Verification System
- [ ] Driver document upload
- [ ] Business document upload
- [ ] Certified copy validation
- [ ] Admin verification dashboard
- [ ] Document approval/rejection
- [ ] Document expiry alerts (30, 14, 7 days)
- [ ] Automatic expiry marking
- [ ] Email notifications for verification status

#### Feedback System
- [ ] Feedback submission
- [ ] Feedback request email
- [ ] PDF report generation
- [ ] CSV export
- [ ] Excel export
- [ ] Feedback statistics

#### API Endpoints
- [ ] Public API endpoints (tracking, quotes)
- [ ] Authenticated API endpoints
- [ ] Business API with API keys
- [ ] Rate limiting
- [ ] CORS configuration

---

## Unit Testing

### Running Unit Tests

```bash
# Run all unit tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=CustomerFeedbackServiceImplTest

# Run with coverage report
./mvnw test jacoco:report
```

### Test Coverage Goals
- Minimum 70% code coverage
- 100% coverage for critical business logic
- All service methods tested
- All controller endpoints tested

---

## Integration Testing

### Database Integration Tests

```bash
# Run integration tests with TestContainers
./mvnw test -Dtest=*IntegrationTest
```

### API Integration Tests

Use Postman collection or automated scripts (see below).

---

## Manual Testing Procedures

### 1. User Registration Flow

#### Customer Registration
1. Navigate to `/register`
2. Fill in registration form:
   - First name (no numbers)
   - Last name (no numbers)
   - Email
   - Password
   - Phone number
3. Submit form
4. Verify email confirmation
5. Login with new credentials

#### Driver Registration
1. Navigate to `/register/driver`
2. Complete registration form
3. Upload required documents:
   - ID document (certified copy)
   - Driver's license (certified copy)
   - Proof of address (certified copy)
4. Verify documents are pending verification
5. Check email for confirmation

#### Business Registration
1. Navigate to `/register/business`
2. Complete business registration form:
   - Business name (no special characters except allowed ones)
   - Registration number
   - Tax ID
3. Upload business documents:
   - Business registration certificate (certified copy)
   - Tax clearance certificate (certified copy)
4. Verify documents are pending verification

### 2. Document Verification Testing

#### Admin Verification Dashboard
1. Login as admin
2. Navigate to `/admin/verification`
3. View pending driver documents
4. View pending business documents
5. Approve a document:
   - Click "Approve"
   - Add notes (optional)
   - Verify email notification sent
6. Reject a document:
   - Click "Reject"
   - Add rejection reason
   - Verify email notification sent

#### Document Expiry Testing
1. Create test documents with expiry dates:
   - 30 days from now
   - 14 days from now
   - 7 days from now
   - Past expiry date
2. Run scheduled task: `checkExpiringDocuments()`
3. Verify email alerts sent for 30, 14, 7 days
4. Run scheduled task: `markExpiredDocuments()`
5. Verify expired documents marked as EXPIRED

### 3. Feedback System Testing

#### Submit Feedback
1. Login as customer
2. Navigate to feedback form
3. Submit feedback with:
   - Overall rating
   - Category ratings
   - Comments
4. Verify feedback saved
5. Verify feedback visible in admin dashboard

#### Generate Feedback Report
1. Login as admin
2. Navigate to feedback reports
3. Select date range
4. Generate PDF report
5. Verify PDF contains:
   - Summary statistics
   - Detailed feedback table
   - Correct date range

#### Export Feedback Data
1. Login as admin
2. Navigate to feedback export
3. Select date range
4. Export as CSV
5. Verify CSV contains all feedback fields
6. Export as Excel
7. Verify Excel file opens correctly

#### Send Feedback Request
1. Create a shipment
2. Mark as delivered
3. Call `sendFeedbackRequest(shipmentId)`
4. Verify email sent to recipient
5. Verify email contains tracking number

### 4. Payment Processing Testing

1. Create a shipment
2. Proceed to payment
3. Test Paystack integration:
   - Use test card numbers
   - Verify payment success
   - Verify payment failure handling
   - Verify webhook processing

### 5. Email Notification Testing

Test all email templates:
- [ ] Registration confirmation
- [ ] Password reset
- [ ] Shipment confirmation
- [ ] Delivery updates
- [ ] Payment confirmation
- [ ] Document verification status
- [ ] Document expiry warnings
- [ ] Feedback request

Use email testing service (Mailtrap, MailHog) for development.

---

## Performance Testing

### Load Testing

```bash
# Using Apache JMeter or similar
# Test scenarios:
# - 100 concurrent users
# - 500 concurrent users
# - 1000 concurrent users
```

### Key Metrics to Monitor
- Response time (target: < 200ms for API, < 2s for pages)
- Throughput (requests per second)
- Error rate (target: < 0.1%)
- Database connection pool usage
- Memory usage
- CPU usage

### Stress Testing
- Test system under maximum load
- Test database connection limits
- Test file upload limits
- Test email sending limits

---

## Security Testing

### Security Checklist

#### Authentication & Authorization
- [ ] Test SQL injection prevention
- [ ] Test XSS (Cross-Site Scripting) prevention
- [ ] Test CSRF protection
- [ ] Test JWT token expiration
- [ ] Test role-based access control
- [ ] Test unauthorized access attempts
- [ ] Test password strength requirements

#### Input Validation
- [ ] Test name validation (no numbers)
- [ ] Test email validation
- [ ] Test phone number validation
- [ ] Test file upload validation
- [ ] Test file size limits
- [ ] Test file type restrictions

#### API Security
- [ ] Test API key authentication
- [ ] Test rate limiting
- [ ] Test CORS headers
- [ ] Test sensitive data exposure
- [ ] Test API endpoint authorization

#### Data Protection
- [ ] Verify sensitive data encrypted
- [ ] Verify passwords hashed (BCrypt)
- [ ] Verify JWT secrets secure
- [ ] Verify environment variables not exposed
- [ ] Verify SQL queries use prepared statements

---

## User Acceptance Testing (UAT)

### Test Scenarios by User Role

#### Customer
1. Register account
2. Create shipment quote
3. Book shipment
4. Make payment
5. Track shipment
6. Receive delivery
7. Submit feedback

#### Driver
1. Register as driver
2. Upload documents
3. Wait for verification
4. Accept delivery assignments
5. Update delivery status
6. View earnings

#### Business
1. Register business account
2. Upload business documents
3. Wait for verification
4. Create bulk shipments
5. Use API integration
6. View analytics

#### Admin
1. Verify driver documents
2. Verify business documents
3. View dashboard
4. Generate reports
5. Manage users
6. Monitor system

---

## Test Data Setup

### Development Test Data

Create test users for each role:
- Admin user
- Staff user
- Customer users (multiple)
- Driver users (verified and unverified)
- Business users (verified and unverified)

### Test Documents
- Sample ID documents (certified copies)
- Sample driver licenses
- Sample business certificates
- Sample invoices

See `TEST_DATA_SETUP.md` for detailed scripts.

---

## Automated Testing Scripts

### API Testing Script

```bash
# Run API tests
./scripts/test-api.sh
```

### End-to-End Test Script

```bash
# Run full test suite
./scripts/test-e2e.sh
```

### Database Migration Test

```bash
# Test database migrations
./mvnw flyway:migrate
./mvnw flyway:info
```

---

## Deployment Readiness Checklist

### Pre-Deployment
- [ ] All tests passing
- [ ] Code coverage > 70%
- [ ] Security scan completed
- [ ] Performance benchmarks met
- [ ] Documentation updated
- [ ] Environment variables configured
- [ ] Database migrations tested
- [ ] Backup strategy in place

### Post-Deployment
- [ ] Smoke tests passed
- [ ] Monitoring configured
- [ ] Logging configured
- [ ] Error tracking active
- [ ] Email notifications working
- [ ] SSL certificate valid
- [ ] Domain DNS configured
- [ ] CDN configured (if applicable)

---

## Testing Resources

### Test Environments
- **Development**: Local development
- **Staging**: Pre-production testing
- **Production**: Live environment

### Test Accounts
See `.env.example` for test account credentials.

### Test Cards (Paystack)
- Success: `4084084084084081`
- Failure: `5060666666666666669`
- Insufficient funds: `4084084084084085`

---

## Reporting Issues

When reporting test failures:
1. Include test scenario
2. Include steps to reproduce
3. Include expected vs actual behavior
4. Include screenshots/logs
5. Include environment details

---

## Next Steps

1. Run unit tests: `./mvnw test`
2. Set up test data: See `TEST_DATA_SETUP.md`
3. Run integration tests
4. Perform manual testing
5. Run security scan
6. Perform load testing
7. Complete UAT
8. Review deployment checklist

---

**Last Updated**: 2024
**Version**: 1.0

