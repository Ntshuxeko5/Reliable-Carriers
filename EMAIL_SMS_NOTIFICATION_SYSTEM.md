# Email and SMS Notification System

## Overview
The Reliable Carriers system now includes a comprehensive email and SMS notification system that automatically sends notifications to customers, drivers, and tracking managers based on shipment status changes and system events.

## Features

### 📧 **Email Notifications**
- **SendGrid Integration** - Professional email delivery service
- **HTML Email Templates** - Beautiful, branded email templates
- **Automatic Notifications** - Triggered by shipment status changes
- **Custom Email Sending** - Admin can send custom emails
- **Bulk Email Support** - Send to multiple recipients

### 📱 **SMS Notifications**
- **SMSPortal Integration** - Reliable SMS delivery service
- **Short Status Updates** - Quick delivery status notifications
- **Driver Alerts** - Instant notifications to drivers
- **Custom SMS Sending** - Admin can send custom SMS messages

### 🔔 **Automatic Notifications**

#### Shipment Lifecycle Notifications
1. **Shipment Created** - Confirmation to customer
2. **Driver Assigned** - Notification to driver with pickup details
3. **Shipment Picked Up** - Customer notification with driver info
4. **In Transit** - Location updates to customer
5. **Out for Delivery** - Final delivery notification
6. **Delivered** - Delivery confirmation and feedback request
7. **Failed Delivery** - Customer notification with retry info
8. **Cancelled** - Cancellation notification with reason

#### Driver Notifications
- **New Assignment** - Shipment details and pickup location
- **Location Updates** - Real-time location tracking alerts
- **Performance Alerts** - Driver performance metrics
- **Online/Offline Status** - Driver availability notifications

#### Tracking Manager Notifications
- **New Shipments** - New shipment creation alerts
- **Status Changes** - Significant status updates
- **Delivery Delays** - Delay alerts with reasons
- **Driver Performance** - Performance metric alerts
- **System Alerts** - System issues and maintenance

#### System Notifications
- **Maintenance Alerts** - Scheduled maintenance notifications
- **Error Alerts** - System error notifications to admins
- **Customer Service** - Custom customer service messages

## Configuration

### Email Configuration (SendGrid)
```properties
# Email Configuration (SendGrid)
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${SENDGRID_API_KEY:your-sendgrid-api-key}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### SMS Configuration (SMSPortal)
```properties
# SMS Configuration (SMSPortal)
sms.api.url=https://api.smsportal.com/v1
sms.api.key=${SMSPORTAL_API_KEY:your-smsportal-api-key}
sms.api.secret=${SMSPORTAL_API_SECRET:your-smsportal-api-secret}
```

### Notification Settings
```properties
# Notification Settings
app.notifications.email.enabled=true
app.notifications.sms.enabled=true
app.tracking.url=http://localhost:8080
```

## API Endpoints

### Notification Management

#### Send Custom Email
```http
POST /api/notifications/email
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "to": "customer@example.com",
  "subject": "Important Update",
  "message": "Your shipment status has been updated."
}
```

#### Send Custom SMS
```http
POST /api/notifications/sms
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "message": "Your shipment is out for delivery!"
}
```

#### Send Shipment Status Update
```http
POST /api/notifications/shipment/{shipmentId}/status-update
Authorization: Bearer <token>
Content-Type: application/json

{
  "oldStatus": "PENDING",
  "newStatus": "PICKED_UP",
  "location": "New York, NY"
}
```

#### Send Delivery Reminders
```http
POST /api/notifications/delivery-reminders
Authorization: Bearer <token>
Content-Type: application/json

[1, 2, 3, 4, 5]
```

#### Send Tracking Alert
```http
POST /api/notifications/tracking-alert
Authorization: Bearer <token>
Content-Type: application/json

{
  "alertType": "Delivery Delay",
  "message": "Shipment RC12345678 is delayed due to weather",
  "shipmentId": 1
}
```

#### Send Driver Performance Alert
```http
POST /api/notifications/driver/{driverId}/performance-alert
Authorization: Bearer <token>
Content-Type: application/json

{
  "metric": "Delivery Time",
  "value": "Exceeded 2 hours"
}
```

#### Send Customer Service Notification
```http
POST /api/notifications/customer-service
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerEmail": "customer@example.com",
  "subject": "Customer Service Update",
  "message": "We have resolved your issue."
}
```

#### Send System Maintenance Notification
```http
POST /api/notifications/system/maintenance
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "message": "System maintenance scheduled",
  "scheduledTime": "2024-01-15 02:00 AM"
}
```

#### Send System Error Notification
```http
POST /api/notifications/system/error
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "error": "Database connection failed",
  "affectedService": "Tracking System"
}
```

### Notification Preferences

#### Update User Preferences
```http
PUT /api/notifications/preferences/{userId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "emailEnabled": true,
  "smsEnabled": false
}
```

#### Get User Preferences
```http
GET /api/notifications/preferences/{userId}
Authorization: Bearer <token>
```

#### Test Notifications
```http
POST /api/notifications/test
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "type": "email",
  "recipient": "test@example.com",
  "message": "This is a test notification"
}
```

## Email Templates

### Shipment Confirmation Email
```
Dear [Recipient Name],

Your shipment has been confirmed and is being processed.
Tracking Number: [TRACKING_NUMBER]

You can track your shipment at: [TRACKING_URL]

Thank you for choosing Reliable Carriers!

Best regards,
Reliable Carriers Team
```

### Shipment Status Update Email
```
Your shipment status has been updated.

Tracking Number: [TRACKING_NUMBER]
Current Status: [STATUS]
Estimated Delivery: [ESTIMATED_DELIVERY]

Track your shipment at: [TRACKING_URL]

Thank you for choosing Reliable Carriers!
```

### Delivery Notification Email
```
Dear [Recipient Name],

Your shipment has been successfully delivered!
Tracking Number: [TRACKING_NUMBER]

Thank you for choosing Reliable Carriers!

Best regards,
Reliable Carriers Team
```

### Driver Assignment Email
```
Dear [Driver Name],

You have been assigned a new shipment.

Tracking Number: [TRACKING_NUMBER]
Recipient: [RECIPIENT_NAME]
Pickup Address: [PICKUP_ADDRESS]
Delivery Address: [DELIVERY_ADDRESS]
Weight: [WEIGHT] kg

Please proceed to pickup location.

Best regards,
Reliable Carriers Team
```

## SMS Templates

### Shipment Confirmation SMS
```
Reliable Carriers: Your shipment [TRACKING_NUMBER] has been confirmed. Track at [TRACKING_URL]
```

### Status Update SMS
```
Reliable Carriers: Shipment [TRACKING_NUMBER] status updated to [STATUS]. Track at [TRACKING_URL]
```

### Delivery Notification SMS
```
Reliable Carriers: Your shipment [TRACKING_NUMBER] has been delivered successfully!
```

### Driver Assignment SMS
```
Reliable Carriers: New shipment [TRACKING_NUMBER] assigned. Pickup: [PICKUP_ADDRESS] Delivery: [DELIVERY_ADDRESS]
```

## Integration with Tracking System

### Automatic Notifications
The notification system is automatically integrated with the tracking system:

1. **Shipment Creation** - Automatically sends confirmation emails/SMS
2. **Status Updates** - Triggers appropriate notifications based on status
3. **Driver Assignment** - Notifies drivers of new assignments
4. **Location Updates** - Alerts tracking managers of driver movements
5. **Delivery Events** - Sends delivery confirmations and feedback requests

### Notification Triggers
- Shipment status changes
- Driver assignments
- Location updates
- Delivery events
- System errors
- Performance alerts

## Security

### Role-Based Access
- **ADMIN** - Full access to all notification endpoints
- **TRACKING_MANAGER** - Access to tracking alerts and customer notifications
- **DRIVER** - Receives assignment and status notifications
- **CUSTOMER** - Receives shipment status notifications

### Data Protection
- Email addresses and phone numbers are encrypted
- API keys are stored as environment variables
- All notifications are logged for audit purposes
- Rate limiting prevents spam

## Error Handling

### Graceful Degradation
- If email service fails, SMS notifications continue
- If SMS service fails, email notifications continue
- Failed notifications are logged and retried
- System continues to function even if notifications fail

### Retry Logic
- Failed notifications are retried up to 3 times
- Exponential backoff between retries
- Dead letter queue for permanently failed notifications

## Monitoring and Analytics

### Notification Metrics
- Delivery success rates
- Open rates for emails
- Click-through rates
- Response times
- Failure rates by provider

### Logging
- All notification attempts are logged
- Success/failure status recorded
- Delivery timestamps tracked
- Error details captured for debugging

## Future Enhancements

### Planned Features
- **Push Notifications** - Mobile app notifications
- **WhatsApp Integration** - WhatsApp Business API
- **Voice Notifications** - Automated phone calls
- **Multi-language Support** - Internationalization
- **Advanced Templates** - Dynamic content generation
- **A/B Testing** - Template optimization
- **Scheduled Notifications** - Time-based notifications
- **Notification History** - User notification history

### Integration Opportunities
- **CRM Systems** - Customer relationship management
- **Marketing Platforms** - Email marketing integration
- **Analytics Tools** - Advanced reporting
- **Mobile Apps** - Push notification support
- **Social Media** - Social media notifications

## Usage Examples

### Customer Journey
1. **Shipment Created** → Email + SMS confirmation
2. **Driver Assigned** → SMS to driver
3. **Picked Up** → Email + SMS to customer
4. **In Transit** → Email with location updates
5. **Out for Delivery** → SMS to customer
6. **Delivered** → Email + SMS confirmation
7. **Feedback Request** → Email after delivery

### Tracking Manager Alerts
1. **New Shipment** → Email alert
2. **Status Change** → Email for significant changes
3. **Delivery Delay** → Email with details
4. **Driver Offline** → Email alert
5. **Performance Issue** → Email with metrics

### System Notifications
1. **Maintenance** → Email to admins
2. **System Error** → Email to admins
3. **Service Updates** → Email to all users
4. **Security Alerts** → Email to admins

## Best Practices

### Email Best Practices
- Use clear, concise subject lines
- Include tracking links in every email
- Provide contact information
- Use professional branding
- Test emails before sending

### SMS Best Practices
- Keep messages under 160 characters
- Include essential information only
- Use clear call-to-action
- Avoid abbreviations
- Test with different carriers

### Notification Timing
- Send confirmations immediately
- Send updates within 5 minutes
- Send reminders 24 hours before delivery
- Send feedback requests 1 hour after delivery
- Respect quiet hours (10 PM - 8 AM)

This comprehensive notification system ensures that all stakeholders are kept informed throughout the shipment lifecycle, improving customer satisfaction and operational efficiency.
