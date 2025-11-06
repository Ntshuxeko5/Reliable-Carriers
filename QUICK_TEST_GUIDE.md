# Quick Testing Guide

A quick reference for testing the Reliable Carriers application before deployment.

## Quick Start

### 1. Run All Tests
```bash
# Run unit and integration tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report
```

### 2. Test API Endpoints
```bash
# Windows
scripts\test-api.bat

# Linux/Mac
./scripts/test-api.sh
```

### 3. Manual Testing Checklist

#### ✅ Authentication (5 minutes)
- [ ] Register new customer
- [ ] Login with credentials
- [ ] Login with Google OAuth
- [ ] Reset password
- [ ] Logout

#### ✅ Core Features (15 minutes)
- [ ] Create shipment quote
- [ ] Book shipment
- [ ] Make payment (test mode)
- [ ] Track shipment
- [ ] View tracking updates

#### ✅ Document Verification (10 minutes)
- [ ] Register as driver
- [ ] Upload documents
- [ ] Login as admin
- [ ] Verify documents
- [ ] Check email notifications

#### ✅ Feedback System (10 minutes)
- [ ] Submit feedback
- [ ] Generate PDF report
- [ ] Export CSV
- [ ] Export Excel
- [ ] Send feedback request email

## Critical Path Testing

### Test Flow 1: Complete Shipment Lifecycle
1. Customer registers → Login
2. Create quote → Book shipment
3. Make payment → Payment confirmed
4. Admin assigns driver → Driver accepts
5. Driver picks up → Updates status
6. Driver delivers → Delivery confirmed
7. Customer receives feedback request → Submits feedback

**Expected Result**: All steps complete without errors

### Test Flow 2: Document Verification
1. Driver registers → Uploads documents
2. Admin logs in → Views pending documents
3. Admin approves document → Email sent
4. Driver receives email → Document status updated
5. Document expires → Expiry alert sent

**Expected Result**: All notifications sent, statuses updated correctly

### Test Flow 3: Feedback & Reports
1. Multiple shipments completed
2. Feedback submitted for each
3. Generate PDF report for date range
4. Export CSV for same range
5. Export Excel for same range

**Expected Result**: All exports generate successfully with correct data

## Smoke Tests (Post-Deployment)

Quick checks after deployment:

```bash
# 1. Health check
curl https://yourdomain.com/actuator/health

# 2. Home page
curl -I https://yourdomain.com

# 3. API endpoint
curl https://yourdomain.com/api/public/track/RC12345678

# 4. Login page
curl -I https://yourdomain.com/login
```

## Common Issues & Solutions

### Issue: Tests failing
**Solution**: Check database connection, verify test data setup

### Issue: Email not sending
**Solution**: Check SMTP configuration, verify email credentials

### Issue: PDF generation fails
**Solution**: Check iText7 dependencies, verify file permissions

### Issue: Document expiry alerts not sending
**Solution**: Verify scheduled tasks enabled, check cron expression

## Performance Quick Checks

```bash
# Test response times
time curl https://yourdomain.com/api/public/track/RC12345678

# Test concurrent requests
ab -n 100 -c 10 https://yourdomain.com/
```

## Security Quick Checks

- [ ] Try SQL injection: `' OR '1'='1`
- [ ] Try XSS: `<script>alert('XSS')</script>`
- [ ] Verify HTTPS enforced
- [ ] Check security headers present
- [ ] Verify rate limiting works

## Environment Variables Check

Before deployment, verify all required variables are set:

```bash
# Check environment variables
echo $DB_URL
echo $JWT_SECRET
echo $GMAIL_USERNAME
echo $GMAIL_APP_PASSWORD
echo $PAYSTACK_SECRET_KEY
```

## Quick Reference

- **Test User**: `test@example.com` / `password123`
- **Admin User**: `admin@reliablecarriers.co.za` / `admin123`
- **Test Tracking**: `RC12345678`
- **Test Paystack Card**: `4084084084084081`

## Next Steps

1. Complete quick tests above
2. Run full test suite
3. Review deployment checklist
4. Deploy to staging
5. Perform UAT
6. Deploy to production

---

**Need more details?** See `TESTING_GUIDE.md` for comprehensive testing procedures.

