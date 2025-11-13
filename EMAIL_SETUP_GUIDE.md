# Email Setup Guide - TurboSMTP for Render (Staging)

This guide explains how to set up TurboSMTP for email sending on Render (staging environment). TurboSMTP is a free email service that works reliably on Render, which blocks Gmail SMTP connections.

## Why TurboSMTP for Render?

- **Render blocks Gmail SMTP**: Render's firewall blocks outbound SMTP connections to Gmail
- **TurboSMTP works on Render**: TurboSMTP SMTP servers are accessible from Render
- **Free tier available**: 6,000 emails per month (perfect for testing/staging)
- **Easy setup**: Simple SMTP configuration, no code changes needed

## TurboSMTP Free Account Setup

### Step 1: Sign Up for TurboSMTP

1. Go to [TurboSMTP](https://serversmtp.com/) or [TurboSMTP Free](https://serversmtp.com/free-smtp-server/)
2. Click "Sign Up" or "Get Started Free"
3. Fill in the registration form:
   - Email address
   - Password
   - Company name (optional)
4. Verify your email address by clicking the link in the confirmation email

### Step 2: Get Your SMTP Credentials

1. Log in to your TurboSMTP account
2. Navigate to **SMTP Settings** or **SMTP Configuration**
3. You'll see your SMTP credentials:
   - **SMTP Server**: `smtp.turbosmtp.com`
   - **Port**: `587` (STARTTLS) or `2525` (alternative)
   - **Username**: Your TurboSMTP username (usually your email)
   - **Password**: Your TurboSMTP password (or SMTP password if different)

### Step 3: Verify Your Sender Email

1. In TurboSMTP dashboard, go to **Sender Verification** or **Domains**
2. Add and verify the email address you want to send from
3. Check your email inbox for verification link
4. Click the verification link to confirm

**Note**: You can only send emails from verified email addresses.

## Configuration for Render (Staging)

### Environment Variables

Set these environment variables in your Render dashboard:

```bash
# TurboSMTP Configuration
MAIL_HOST=smtp.turbosmtp.com
MAIL_PORT=587
MAIL_USE_SSL=false
MAIL_USE_STARTTLS=true
MAIL_PROTOCOL=smtp

# TurboSMTP Credentials
TURBOSMTP_USERNAME=your-turbosmtp-username
TURBOSMTP_PASSWORD=your-turbosmtp-password
```

### How to Set Environment Variables on Render

1. Go to your Render dashboard
2. Select your web service
3. Click on **Environment** tab
4. Click **Add Environment Variable**
5. Add each variable one by one:
   - Key: `MAIL_HOST`, Value: `smtp.turbosmtp.com`
   - Key: `MAIL_PORT`, Value: `587`
   - Key: `MAIL_USE_SSL`, Value: `false`
   - Key: `MAIL_USE_STARTTLS`, Value: `true`
   - Key: `MAIL_PROTOCOL`, Value: `smtp`
   - Key: `TURBOSMTP_USERNAME`, Value: `your-username`
   - Key: `TURBOSMTP_PASSWORD`, Value: `your-password`
6. Click **Save Changes**
7. Redeploy your service (Render will auto-redeploy if enabled)

## Configuration for Railway (Production)

For Railway, continue using Gmail SMTP (it works fine):

```bash
# Gmail SMTP Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USE_SSL=false
MAIL_USE_STARTTLS=true
MAIL_PROTOCOL=smtp

# Gmail Credentials
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-gmail-app-password
```

## Testing Email Configuration

### Test Email Sending

1. Deploy your application with the new environment variables
2. Trigger an email (e.g., user registration, password reset)
3. Check the application logs for email sending status
4. Verify the email was received

### Common Issues

**Issue: "Authentication failed"**
- **Solution**: Verify your TurboSMTP username and password are correct
- Check that you're using the SMTP password, not your account login password

**Issue: "Connection timeout"**
- **Solution**: Ensure `MAIL_PORT=587` and `MAIL_USE_STARTTLS=true`
- Check that your sender email is verified in TurboSMTP

**Issue: "Email not received"**
- **Solution**: Check spam folder
- Verify sender email is verified in TurboSMTP
- Check TurboSMTP dashboard for delivery status

## TurboSMTP Free Tier Limits

- **Emails per month**: 6,000
- **Emails per day**: ~200 (distributed)
- **Features**: Basic SMTP sending
- **Support**: Community support

**Note**: If you exceed the free tier, you'll need to upgrade to a paid plan or switch to another provider.

## Alternative Free Email Services

If TurboSMTP doesn't work for you, consider:

1. **Brevo (formerly Sendinblue)**: 300 emails/day free
2. **Mailgun**: 5,000 emails/month free (first 3 months)
3. **Resend**: 3,000 emails/month free (requires API integration)

## Upgrading to Production Email Service

When ready for production, consider:

- **SendGrid**: Industry standard, reliable, good free tier
- **Mailgun**: Excellent deliverability, good free tier
- **AWS SES**: Very cost-effective at scale
- **Postmark**: Excellent deliverability, pay-per-use

## Support

- **TurboSMTP Support**: https://serversmtp.com/support/
- **TurboSMTP Documentation**: https://serversmtp.com/knowledge-base/
- **Application Email Troubleshooting**: See `EMAIL_SMTP_TROUBLESHOOTING.md`

## Summary

✅ **Render (Staging)**: Use TurboSMTP (free, works on Render)
✅ **Railway (Production)**: Use Gmail SMTP (free, works on Railway)
✅ **No code changes**: Pure configuration via environment variables
✅ **Easy switching**: Change environment variables to switch providers

