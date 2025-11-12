# Email SMTP Connection Troubleshooting Guide

## Problem: SMTP Connection Timeout

If you're experiencing connection timeouts when sending emails (especially 2FA codes), follow these troubleshooting steps:

## Common Error Messages

```
Mail server connection failed. Failed messages: 
org.eclipse.angus.mail.util.MailConnectException: 
Couldn't connect to host, port: smtp.gmail.com, 587; timeout 15000
```

## Solutions

### 1. Check Gmail App Password

**Important**: Gmail requires an App Password, not your regular Gmail password.

#### Steps to Generate Gmail App Password:

1. Go to your Google Account: https://myaccount.google.com/
2. Click **Security** in the left sidebar
3. Under "Signing in to Google", enable **2-Step Verification** (if not already enabled)
4. After enabling 2-Step Verification, go back to Security
5. Under "Signing in to Google", click **App passwords**
6. Select **Mail** as the app and **Other (Custom name)** as the device
7. Enter "Reliable Carriers" as the custom name
8. Click **Generate**
9. Copy the 16-character password (it will look like: `abcd efgh ijkl mnop`)
10. Use this password (without spaces) as `GMAIL_APP_PASSWORD` in your environment variables

### 2. Check Network/Firewall Settings

Some hosting providers (like Render, Railway) may block outbound SMTP connections on port 587.

#### Try Alternative Ports:

**Option A: Port 465 with SSL**
```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.starttls.enable=false
```

**Option B: Port 587 with STARTTLS** (current default)
```properties
spring.mail.port=587
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### 3. Environment Variables (Render/Railway)

Make sure these are set in your hosting platform:

```bash
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-16-character-app-password
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
```

### 4. Alternative Email Providers

If Gmail SMTP continues to fail, consider using:

#### A. SendGrid (Recommended for Production)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your-sendgrid-api-key
```

#### B. Mailgun
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=your-mailgun-username
spring.mail.password=your-mailgun-password
```

#### C. Amazon SES
```properties
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=your-ses-access-key
spring.mail.password=your-ses-secret-key
```

### 5. Test SMTP Connection

You can test your SMTP connection using telnet:

```bash
telnet smtp.gmail.com 587
```

If this fails, your hosting provider may be blocking outbound SMTP connections.

### 6. Check Application Logs

The application now includes:
- Retry logic (3 attempts with exponential backoff)
- Increased timeouts (30 seconds instead of 15)
- Better error messages
- Token logging when email fails (for development)

### 7. Temporary Workaround

If email continues to fail:
- The 2FA token is still saved in the database
- Check application logs for the token
- The registration response includes the token in the `demoCode` field
- Users can manually enter the code for verification

## Current Configuration

The application is configured with:
- **Connection timeout**: 30 seconds
- **Read timeout**: 30 seconds
- **Write timeout**: 30 seconds
- **Retry attempts**: 3
- **Retry delay**: Exponential backoff (2s, 4s, 6s)

## Next Steps

1. Verify Gmail App Password is correct
2. Check if your hosting provider allows outbound SMTP connections
3. Try alternative port 465 with SSL
4. Consider switching to a production email service (SendGrid, Mailgun, AWS SES)
5. Check application logs for detailed error messages

