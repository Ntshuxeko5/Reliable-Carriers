# Render Environment Variables Setup Guide

## Problem
2FA codes are not being sent because email/SMS environment variables are not configured on Render.

## Required Environment Variables for Render

### 1. Email Configuration (Gmail SMTP)

Set these in your Render dashboard under **Environment**:

```
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password-here
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USE_STARTTLS=true
MAIL_USE_SSL=false
```

**How to get Gmail App Password:**
1. Go to your Google Account settings
2. Enable 2-Step Verification
3. Go to App Passwords: https://myaccount.google.com/apppasswords
4. Generate a new app password for "Mail"
5. Copy the 16-character password (no spaces)
6. Set it as `GMAIL_APP_PASSWORD` in Render

**Note:** On Render, port 587 might be blocked. If email fails, try:
```
MAIL_PORT=465
MAIL_USE_SSL=true
MAIL_USE_STARTTLS=false
```

### 2. SMS Configuration (SMSPortal)

Set these in your Render dashboard:

```
SMSPORTAL_API_KEY=your-smsportal-api-key
SMSPORTAL_API_SECRET=your-smsportal-api-secret
SMS_ENABLED=true
```

**How to get SMSPortal credentials:**
1. Sign up at https://www.smsportal.com/
2. Go to API Settings
3. Copy your API Key and Secret
4. Set them in Render environment variables

### 3. Database Configuration

```
DB_URL=jdbc:postgresql://your-render-db-host:5432/your-db-name
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password
DB_DDL_AUTO=validate
```

### 4. JWT Configuration

```
JWT_SECRET=your-secure-random-secret-key-min-256-bits
```

Render can auto-generate this, or generate your own:
```bash
openssl rand -hex 32
```

### 5. Application URLs

```
APP_BASE_URL=https://your-app-name.onrender.com
APP_TRACKING_URL=https://your-app-name.onrender.com
```

## How to Set Environment Variables on Render

### Method 1: Via Render Dashboard (Recommended)

1. Go to your Render dashboard
2. Select your web service
3. Click on **Environment** tab
4. Click **Add Environment Variable**
5. Add each variable one by one:
   - Key: `GMAIL_USERNAME`
   - Value: `your-email@gmail.com`
   - Click **Save Changes**
6. Repeat for all variables

### Method 2: Via render.yaml

Update your `render.yaml` file:

```yaml
services:
  - type: web
    name: reliable-carriers
    envVars:
      - key: GMAIL_USERNAME
        sync: false  # Set manually in dashboard
      - key: GMAIL_APP_PASSWORD
        sync: false  # Set manually in dashboard (use secret)
      - key: SMSPORTAL_API_KEY
        sync: false
      - key: SMSPORTAL_API_SECRET
        sync: false
      - key: DB_URL
        fromDatabase:
          name: reliable-carriers-db
          property: connectionString
      - key: JWT_SECRET
        generateValue: true  # Render will generate
```

Then set the values manually in the dashboard.

## Testing Email Configuration

After setting environment variables, test email:

1. **Check logs** in Render dashboard for email errors
2. **Try registration** - codes should be sent via email
3. **Check email inbox** (and spam folder)

## Troubleshooting

### Email Not Sending

1. **Check environment variables are set:**
   ```bash
   # In Render logs, you should see:
   # Email sent successfully to...
   # NOT: Email sender is not configured
   ```

2. **Check Gmail App Password:**
   - Make sure it's a 16-character app password (not your regular password)
   - No spaces in the password
   - App password is for "Mail" application

3. **Check SMTP port:**
   - Render may block port 587
   - Try port 465 with SSL instead

4. **Check logs for errors:**
   ```
   Failed to send email to... (Attempt 1/3): ...
   ```

### SMS Not Sending

1. **Check SMSPortal credentials:**
   - Verify API Key and Secret are correct
   - Check account has credits

2. **Check SMS is enabled:**
   ```
   SMS_ENABLED=true
   ```

3. **Check logs:**
   ```
   SMS sent successfully via SMSPortal to...
   ```

### Codes Generated But Not Sent

**Good news:** Even if email/SMS fails, codes are still generated and saved in the database!

1. **Check database** for the token:
   ```sql
   SELECT * FROM two_factor_tokens WHERE user_id = ? ORDER BY expires_at DESC;
   ```

2. **Use resend functionality** - the frontend has a "Resend Code" button

3. **Check logs** for the actual token (if debug mode is enabled)

## Current Behavior After Fix

With the latest fixes:

1. ✅ **Codes are always generated** - even if email/SMS fails
2. ✅ **Codes are saved to database** - users can verify them
3. ✅ **Email failures don't break registration** - registration succeeds even if email fails
4. ✅ **Better error messages** - users know to use "Resend Code" if email fails
5. ✅ **Logging** - all failures are logged for debugging

## Quick Setup Checklist

- [ ] Set `GMAIL_USERNAME` in Render
- [ ] Set `GMAIL_APP_PASSWORD` in Render (use secret)
- [ ] Set `MAIL_HOST=smtp.gmail.com`
- [ ] Set `MAIL_PORT=587` (or 465 if 587 blocked)
- [ ] Set `SMSPORTAL_API_KEY` (optional, for SMS)
- [ ] Set `SMSPORTAL_API_SECRET` (optional, for SMS)
- [ ] Set `DB_URL` (from Render database)
- [ ] Set `DB_USERNAME` and `DB_PASSWORD`
- [ ] Set `JWT_SECRET` (or let Render generate)
- [ ] Set `APP_BASE_URL` to your Render URL
- [ ] Redeploy application
- [ ] Test registration and check logs

## Security Notes

1. **Never commit** environment variables to git
2. **Use Render's secret management** for sensitive values
3. **Rotate passwords** regularly
4. **Use app passwords** for Gmail (not your main password)
5. **Restrict API keys** to specific IPs if possible

