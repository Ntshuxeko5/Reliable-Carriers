# 2FA Fixes for Business and Driver Registration

## Overview
Fixed 2FA (Two-Factor Authentication) implementation for business and driver registration to work exactly like customer registration.

## Issues Fixed

### 1. Business Registration (`register-business.html`)
**Problems:**
- ❌ Was not storing email in sessionStorage
- ❌ Was getting email from form input instead of sessionStorage
- ❌ Was using wrong endpoint format (POST with body instead of query params)
- ❌ Was not handling all response cases properly

**Fixes Applied:**
- ✅ Now stores email and role in sessionStorage when 2FA is required
- ✅ Uses sessionStorage to get email for verification (like customer registration)
- ✅ Uses query params format for verification endpoint: `/api/auth/register/verify?email=...&code=...`
- ✅ Uses query params format for resend endpoint: `/api/auth/register/resend?email=...&method=EMAIL`
- ✅ Handles all response cases (success, requires2fa, errors, validation errors)
- ✅ Shows demo code if available (for testing)
- ✅ Proper error handling and user feedback

### 2. Driver Registration (`register-driver.html`)
**Problems:**
- ❌ Was using `/api/driver/register` endpoint which doesn't implement 2FA
- ❌ Had no OTP verification stage
- ❌ Was redirecting directly to login without 2FA

**Fixes Applied:**
- ✅ Changed to use `/api/auth/register` endpoint (which has 2FA built-in)
- ✅ Added OTP verification stage HTML
- ✅ Added OTP verification handler (same as customer registration)
- ✅ Added resend code handler
- ✅ Stores email and role in sessionStorage
- ✅ Redirects to driver dashboard after successful verification
- ✅ Handles all response cases properly

## Key Changes

### Business Registration
1. **Registration Response Handling:**
   ```javascript
   if (result && result.success) {
       if (result.requires2fa) {
           sessionStorage.setItem('pending2faEmail', result.email);
           sessionStorage.setItem('pending2faRole', result.role || 'CUSTOMER');
           // Show OTP stage
       }
   }
   ```

2. **Verification:**
   ```javascript
   const email = sessionStorage.getItem('pending2faEmail');
   fetch('/api/auth/register/verify?email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code), {
       method: 'POST'
   })
   ```

3. **Resend Code:**
   ```javascript
   const email = sessionStorage.getItem('pending2faEmail');
   fetch('/api/auth/register/resend?email=' + encodeURIComponent(email) + '&method=EMAIL', {
       method: 'POST'
   })
   ```

### Driver Registration
1. **Changed Endpoint:**
   - From: `/api/driver/register` (no 2FA)
   - To: `/api/auth/register` (has 2FA)

2. **Added OTP Stage:**
   - Same HTML structure as customer registration
   - Same verification flow
   - Same resend code functionality

3. **Registration Flow:**
   ```javascript
   fetch('/api/auth/register', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify(data)
   })
   .then(result => {
       if (result.requires2fa) {
           // Store email, show OTP stage
       }
   })
   ```

## How It Works Now

### Customer Registration Flow (Reference)
1. User fills registration form
2. Submits to `/api/auth/register`
3. Backend returns `{ success: true, requires2fa: true, email: "...", demoCode: "..." }`
4. Frontend stores email in sessionStorage
5. Frontend shows OTP input stage
6. User enters code
7. Frontend calls `/api/auth/register/verify?email=...&code=...`
8. Backend verifies and returns JWT token
9. Frontend redirects to appropriate dashboard

### Business Registration Flow (Now Fixed)
✅ **Same flow as customer registration**

1. User fills business registration form
2. Submits to `/api/auth/register` (with `isBusiness: true`)
3. Backend returns `{ success: true, requires2fa: true, email: "...", demoCode: "..." }`
4. Frontend stores email in sessionStorage
5. Frontend shows OTP input stage
6. User enters code
7. Frontend calls `/api/auth/register/verify?email=...&code=...`
8. Backend verifies and returns JWT token
9. Frontend redirects to customer dashboard

### Driver Registration Flow (Now Fixed)
✅ **Same flow as customer registration**

1. User fills driver registration form
2. Submits to `/api/auth/register` (with `role: 'DRIVER'`)
3. Backend returns `{ success: true, requires2fa: true, email: "...", demoCode: "..." }`
4. Frontend stores email in sessionStorage
5. Frontend shows OTP input stage
6. User enters code
7. Frontend calls `/api/auth/register/verify?email=...&code=...`
8. Backend verifies and returns JWT token
9. Frontend redirects to driver dashboard

## Testing

### Test Business Registration:
1. Go to `/register/business`
2. Fill in business registration form
3. Submit
4. Should see OTP verification stage
5. Enter verification code (check console for demo code)
6. Should redirect to customer dashboard

### Test Driver Registration:
1. Go to `/register/driver`
2. Fill in driver registration form (all 3 steps)
3. Submit
4. Should see OTP verification stage
5. Enter verification code (check console for demo code)
6. Should redirect to driver dashboard

### Test Resend Code:
1. After receiving OTP stage
2. Click "Resend Code"
3. Should receive new code
4. Verify with new code

## Files Modified

1. **`register-business.html`**
   - Fixed registration response handling
   - Fixed OTP verification handler
   - Fixed resend code handler
   - Now uses sessionStorage for email

2. **`register-driver.html`**
   - Changed endpoint from `/api/driver/register` to `/api/auth/register`
   - Added OTP verification stage HTML
   - Added OTP verification handler
   - Added resend code handler
   - Now uses sessionStorage for email

## Backend Endpoints Used

All registration types now use the same endpoints:
- **Registration**: `POST /api/auth/register`
- **Verification**: `POST /api/auth/register/verify?email=...&code=...`
- **Resend Code**: `POST /api/auth/register/resend?email=...&method=EMAIL`

## Notes

- The `/api/auth/register` endpoint already handles:
  - Customer registration
  - Business registration (when `isBusiness: true`)
  - Driver registration (when `role: 'DRIVER'`)
  - 2FA token generation and sending
  - Demo code generation for testing

- All three registration types now have identical 2FA flows
- Demo codes are shown in console/alert for testing purposes
- In production, remove demo code display

---

**Status**: ✅ Fixed and ready for testing
**Date**: 2025-01-05

