# Notification System Setup Guide

## Overview
This guide will help you set up the email and SMS notification system for Reliable Carriers.

## Prerequisites
- TurboSMTP account for email notifications
- SMSPortal account for SMS notifications
- Spring Boot application running

## Environment Variables Setup

### 1. Email Configuration (TurboSMTP)

#### Get TurboSMTP Credentials:
1. Sign up at [TurboSMTP](https://serversmtp.com/)
2. Create a user and password for SMTP
3. Note the SMTP host (smtp.turbosmtp.com) and port (587)

#### Set Environment Variables (recommended):
```bash
# Windows (PowerShell)
$Env:TURBOSMTP_USERNAME="your-turbosmtp-username"
$Env:TURBOSMTP_PASSWORD="your-turbosmtp-password"

# Windows (cmd)
set TURBOSMTP_USERNAME=your-turbosmtp-username
set TURBOSMTP_PASSWORD=your-turbosmtp-password

# Linux/Mac
export TURBOSMTP_USERNAME=your-turbosmtp-username
export TURBOSMTP_PASSWORD=your-turbosmtp-password
```

### 2. SMS Configuration (SMSPortal)

#### Get SMSPortal Credentials:
1. Sign up at [SMSPortal](https://www.smsportal.com/)
2. Go to API Settings
3. Get your API Key and Secret
4. Copy both credentials

#### Set Environment Variables:
```bash
# Windows
set SMSPORTAL_API_KEY=your-smsportal-api-key-here
set SMSPORTAL_API_SECRET=your-smsportal-api-secret-here

# Linux/Mac
export SMSPORTAL_API_KEY=your-smsportal-api-key-here
export SMSPORTAL_API_SECRET=your-smsportal-api-secret-here
```

## Application Properties

The following properties are configured in `application.properties`:

```properties
# Email Configuration (TurboSMTP)
spring.mail.host=smtp.turbosmtp.com
spring.mail.port=587
spring.mail.username=${TURBOSMTP_USERNAME:your-turbosmtp-username}
spring.mail.password=${TURBOSMTP_PASSWORD:your-turbosmtp-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# SMS Configuration (SMSPortal)
sms.api.url=https://api.smsportal.com/v1
sms.api.key=${SMSPORTAL_API_KEY:your-smsportal-api-key}
sms.api.secret=${SMSPORTAL_API_SECRET:your-smsportal-api-secret}

# Notification Settings
app.notifications.email.enabled=true
app.notifications.sms.enabled=true
app.tracking.url=http://localhost:8080
```

## Testing the System

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. Access the Test Dashboard
- Navigate to: `http://localhost:8080/notifications`
- Login as an admin user
- Use the test forms to send email and SMS notifications

### 3. Test API Endpoints

#### Send Test Email:
```bash
curl -X POST http://localhost:8080/api/notifications/email \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "to": "test@example.com",
    "subject": "Test Email",
    "message": "This is a test email"
  }'
```

#### Send Test SMS:
```bash
curl -X POST http://localhost:8080/api/notifications/sms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "phoneNumber": "+1234567890",
    "message": "This is a test SMS"
  }'
```

## Troubleshooting

### Email Issues:
1. **Authentication Failed**: Check your SendGrid API key
2. **Connection Refused**: Verify SendGrid SMTP settings
3. **Email Not Received**: Check spam folder and recipient email

### SMS Issues:
1. **Authentication Failed**: Check SMSPortal API credentials
2. **Invalid Phone Number**: Ensure phone number is in international format (+1234567890)
3. **Message Too Long**: SMS messages should be under 160 characters

### Common Error Messages:
- `Failed to get SMS access token`: Check SMSPortal API credentials
- `Email sending failed`: Check SendGrid API key and SMTP settings
- `Network error`: Check internet connection and API endpoints

## Production Deployment

### 1. Environment Variables in Production:
```bash
# Set these in your production environment
SENDGRID_API_KEY=your-production-sendgrid-key
SMSPORTAL_API_KEY=your-production-smsportal-key
SMSPORTAL_API_SECRET=your-production-smsportal-secret
```

### 2. Update Application Properties:
```properties
# Production settings
app.tracking.url=https://your-domain.com
app.notifications.email.enabled=true
app.notifications.sms.enabled=true
```

### 3. Monitoring:
- Check application logs for notification errors
- Monitor SendGrid and SMSPortal dashboards for delivery status
- Set up alerts for failed notifications

## Security Considerations

1. **API Keys**: Never commit API keys to version control
2. **Environment Variables**: Use environment variables for sensitive data
3. **Rate Limiting**: Be aware of SendGrid and SMSPortal rate limits
4. **Access Control**: Only admins can send custom notifications

## Support

For issues with:
- **SendGrid**: Contact SendGrid support
- **SMSPortal**: Contact SMSPortal support
- **Application**: Check application logs and error messages
