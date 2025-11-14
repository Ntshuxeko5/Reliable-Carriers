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

Some hosting providers (like Render) may block outbound SMTP connections to Gmail. Railway typically allows Gmail SMTP connections.

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

If Gmail SMTP continues to fail (especially on Render), use an alternative provider:

#### A. TurboSMTP (Recommended for Render - Free Tier Available)

**Best for Render (Staging)**: TurboSMTP works reliably on Render and offers a free tier (6,000 emails/month).

```bash
# Environment Variables for Render
MAIL_HOST=smtp.turbosmtp.com
MAIL_PORT=587
MAIL_USE_SSL=false
MAIL_USE_STARTTLS=true
MAIL_PROTOCOL=smtp
TURBOSMTP_USERNAME=your-turbosmtp-username
TURBOSMTP_PASSWORD=your-turbosmtp-password
```

**Setup Steps:**
1. Sign up at [TurboSMTP](https://serversmtp.com/) (free account)
2. Verify your email address
3. Get SMTP credentials from dashboard
4. Set environment variables in Render
5. Redeploy your service

**For detailed setup instructions, see:** [`EMAIL_SETUP_GUIDE.md`](EMAIL_SETUP_GUIDE.md)

#### B. SendGrid (Recommended for Production)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your-sendgrid-api-key
```

#### C. Mailgun
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=your-mailgun-username
spring.mail.password=your-mailgun-password
```

#### D. Amazon SES
```properties
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=your-ses-access-key
spring.mail.password=your-ses-secret-key
```

### 4.1. Platform-Specific Recommendations

**For Railway (Production):**
- ✅ Use **Gmail SMTP** (works fine, free)
- Gmail SMTP connections are not blocked on Railway

**For Render (Staging):**
- ❌ **Gmail SMTP is blocked** - will not work
- ✅ Use **TurboSMTP** (free tier, works on Render)
- Alternative: Use SendGrid, Mailgun, or AWS SES

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

1. **For Railway**: Verify Gmail App Password is correct
2. **For Render**: Switch to TurboSMTP (Gmail is blocked) - see [`EMAIL_SETUP_GUIDE.md`](EMAIL_SETUP_GUIDE.md)
3. Check if your hosting provider allows outbound SMTP connections
4. Try alternative port 465 with SSL (if using Gmail)
5. Consider switching to a production email service (SendGrid, Mailgun, AWS SES) for production
6. Check application logs for detailed error messages

## Quick Solution for Render

If you're deploying to Render and experiencing email connection timeouts:

1. **Sign up for TurboSMTP** (free): https://serversmtp.com/
2. **Get your SMTP credentials** from TurboSMTP dashboard
3. **Set environment variables in Render**:
   ```bash
   MAIL_HOST=smtp.turbosmtp.com
   MAIL_PORT=587
   TURBOSMTP_USERNAME=your-username
   TURBOSMTP_PASSWORD=your-password
   ```
4. **Redeploy** your service

See [`EMAIL_SETUP_GUIDE.md`](EMAIL_SETUP_GUIDE.md) for complete setup instructions.

