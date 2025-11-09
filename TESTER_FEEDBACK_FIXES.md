# Tester Feedback - Issues and Fixes

## Issues Identified

1. **Verification of package arrival** - Need to check UI/UX
2. **"Choose your service" functionality** - Not working properly
3. **Separate page for moving/furniture services** - Missing or not linked
4. **Error messages when logging in** - Not displaying properly
5. **Validation of email address** - May not be working correctly
6. **Registration notifications must be separated** - Need role-specific notifications
7. **Registration data not being saved** - Critical issue
8. **Verification code only works after multiple resend attempts** - 2FA resend issue

## Analysis

### Issue 7: Registration Data Not Being Saved
**Status**: The code shows users ARE being saved (AuthServiceImpl line 73-74), but there might be a transaction issue or the user is being created before 2FA verification. The user should only be fully activated after 2FA verification.

### Issue 8: Verification Code Resend
**Status**: The resend endpoint exists and looks correct. The issue might be that the first code isn't being sent properly, or there's a timing issue.

### Issue 5: Email Validation
**Status**: Email validation exists in UserValidationService but might not be strict enough or not being called properly.

### Issue 4: Login Error Messages
**Status**: Error handling exists but might not be displaying user-friendly messages.

### Issue 6: Registration Notifications
**Status**: Need to check if different email templates are sent for different roles.

### Issue 2: "Choose Your Service"
**Status**: The booking.html page has this functionality, but it might not be working correctly.

### Issue 3: Moving/Furniture Services
**Status**: moving-services.html exists but might not be properly linked or accessible.

### Issue 1: Package Arrival Verification
**Status**: Code exists for delivery verification, but UI might not be clear.

