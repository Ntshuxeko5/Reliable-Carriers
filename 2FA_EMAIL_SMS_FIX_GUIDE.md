# 2FA and Email/SMS Fix Guide

## Issues Fixed

### 1. TwoFactorServiceImpl Constructor Issue ✅
- **Problem**: The `smsService` parameter was not being assigned in the constructor
- **Fix**: Added the missing assignment in the constructor

### 2. SMS Configuration Mismatch ✅
- **Problem**: SMS service was looking for `sms.enabled` but configuration used `app.notifications.sms.enabled`
- **Fix**: Updated SMS service to use the correct configuration property

### 3. Missing SMSPortal Integration ✅
- **Problem**: SMS service had placeholder methods but no actual SMSPortal integration
- **Fix**: Implemented full SMSPortal API integration with proper authentication and error handling

### 4. Missing Error Handling ✅
- **Problem**: No proper error handling in email and SMS services
- **Fix**: Added comprehensive error handling with fallback mechanisms

### 5. Missing Configuration Properties ✅
- **Problem**: Missing SMS and 2FA configuration properties
- **Fix**: Added all necessary configuration properties to `application.properties`

## Configuration

### Email Configuration (TurboSMTP)
```properties
# Email Configuration (TurboSMTP)
spring.mail.host=smtp.turbosmtp.com
spring.mail.port=587
spring.mail.username=${TURBOSMTP_USERNAME:ntshuxekochabalal80@gmail.com}
spring.mail.password=${TURBOSMTP_PASSWORD:jGvEK9SC}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### SMS Configuration (SMSPortal)
```properties
# SMS Configuration (SMSPortal)
sms.api.url=https://api.smsportal.com/v1
sms.api.key=${SMSPORTAL_API_KEY:c7887093-21b4-456d-af98-e03889d90210}
sms.api.secret=${SMSPORTAL_API_SECRET:Uxh1ZUDuI1YG5RBEBv+YycazgtBptJVr}
sms.provider=smsportal
sms.enabled=true
```

### 2FA Configuration
```properties
# 2FA Configuration
app.2fa.token.ttl.minutes=10
app.2fa.enabled=true
app.notifications.email.enabled=true
app.notifications.sms.enabled=true
```

## Testing the System

### 1. Test Email Service
```bash
curl -X POST "http://localhost:8080/api/test/email" \
  -d "to=your-email@example.com" \
  -d "subject=Test Email" \
  -d "message=This is a test email from Reliable Carriers"
```

### 2. Test SMS Service
```bash
curl -X POST "http://localhost:8080/api/test/sms" \
  -d "to=+1234567890" \
  -d "message=This is a test SMS from Reliable Carriers"
```

### 3. Test 2FA Email
```bash
curl -X POST "http://localhost:8080/api/test/2fa/email" \
  -d "email=user@example.com"
```

### 4. Test 2FA SMS
```bash
curl -X POST "http://localhost:8080/api/test/2fa/sms" \
  -d "phone=+1234567890"
```

### 5. Check Service Status
```bash
curl -X GET "http://localhost:8080/api/test/status"
```

## Production Setup

### Environment Variables
Set these environment variables in production:

```bash
# Email (TurboSMTP)
export TURBOSMTP_USERNAME="your-turbosmtp-username"
export TURBOSMTP_PASSWORD="your-turbosmtp-password"

# SMS (SMSPortal)
export SMSPORTAL_API_KEY="your-smsportal-api-key"
export SMSPORTAL_API_SECRET="your-smsportal-api-secret"
```

### Email Service Setup
1. Sign up at [TurboSMTP](https://serversmtp.com/)
2. Create SMTP credentials
3. Update environment variables
4. Test email sending

### SMS Service Setup
1. Sign up at [SMSPortal](https://www.smsportal.com/)
2. Get API Key and Secret from dashboard
3. Update environment variables
4. Test SMS sending

## Troubleshooting

### Email Issues
- Check SMTP credentials
- Verify network connectivity to smtp.turbosmtp.com:587
- Check spam folder
- Review application logs for detailed error messages

### SMS Issues
- Verify SMSPortal API credentials
- Check phone number format (include country code)
- Ensure SMS service is enabled in configuration
- Review application logs for API response errors

### 2FA Issues
- Ensure user has valid email/phone number
- Check token expiration (default 10 minutes)
- Verify 2FA is enabled in configuration
- Check database for token records

## API Endpoints

### Authentication 2FA Endpoints
- `POST /api/auth/2fa/setup` - Setup TOTP 2FA
- `POST /api/auth/2fa/verify` - Verify TOTP code
- `POST /api/auth/2fa/request` - Request method-based 2FA token
- `POST /api/auth/2fa/verify-method` - Verify method-based 2FA token

### Test Endpoints (Development Only)
- `POST /api/test/email` - Test email service
- `POST /api/test/sms` - Test SMS service
- `POST /api/test/2fa/email` - Test 2FA email
- `POST /api/test/2fa/sms` - Test 2FA SMS
- `GET /api/test/status` - Check service status

## Security Notes

1. **Remove Test Controller**: The TestController should be removed or secured in production
2. **Environment Variables**: Never commit API keys to version control
3. **Rate Limiting**: Consider implementing rate limiting for 2FA requests
4. **Token Cleanup**: Implement automatic cleanup of expired tokens
5. **Logging**: Monitor failed authentication attempts

## Next Steps

1. Test all endpoints with valid user data
2. Configure proper environment variables
3. Set up monitoring and alerting
4. Implement rate limiting
5. Add comprehensive logging
6. Remove or secure test endpoints for production
