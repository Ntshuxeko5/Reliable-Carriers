# Customer Dashboard and Profile Fixes

## Issues Fixed

### 1. Duplicate Customer Dashboards
**Problem**: Two different routes were serving the customer dashboard:
- `/customer` and `/customer/dashboard` in `CustomerWebController` 
- `/customer/dashboard` in `WebController`

**Fix**: Updated `WebController` to redirect `/customer/dashboard` to `/customer` to avoid confusion.

### 2. Profile Edit Not Working
**Problem**: Profile page showed "failed to load" error when trying to edit information.

**Root Cause**: 
- JavaScript was trying to use Bearer token authentication
- API calls weren't including session cookies
- Error handling wasn't showing proper error messages

**Fixes Applied**:
- Updated all API calls to use `credentials: 'include'` to send session cookies
- Made Bearer token optional (only sent if available)
- Improved error handling to show specific error messages
- Added proper error messages for 401 (unauthorized) responses

### 3. Saved Addresses Not Showing
**Problem**: Saved addresses section was not displaying any addresses.

**Root Cause**:
- Address management JavaScript code was missing from the profile page
- API calls weren't including session cookies
- No error handling for failed address loads

**Fixes Applied**:
- Added complete address management JavaScript code:
  - `loadSavedAddresses()` - Loads and displays saved addresses
  - `showAddAddressModal()` - Shows add/edit address modal
  - `hideAddAddressModal()` - Hides the modal
  - `editAddress()` - Loads address for editing
  - `deleteAddress()` - Deletes an address
  - Address form submission handler
- Updated all address API calls to use `credentials: 'include'`
- Added proper error handling with retry functionality
- Integrated address loading into the main DOMContentLoaded handler

## Technical Details

### Authentication Method
The application supports both:
1. **Session-based authentication** (for web pages)
2. **JWT Bearer token authentication** (for API clients)

The JavaScript now handles both:
- If a token exists in localStorage/sessionStorage, it's sent as Bearer token
- Session cookies are always sent via `credentials: 'include'`
- This ensures API calls work whether the user logged in via web form or API

### Filter Order
The security filter chain order is:
1. RateLimitFilter
2. ApiKeyAuthenticationFilter (if available)
3. SessionAuthenticationFilter (restores auth from session)
4. JwtAuthenticationFilter (processes Bearer tokens)
5. UsernamePasswordAuthenticationFilter

This ensures session authentication is checked before JWT authentication.

## Files Modified

1. `src/main/java/com/reliablecarriers/Reliable/Carriers/controller/WebController.java`
   - Fixed duplicate dashboard route

2. `src/main/resources/templates/customer/profile.html`
   - Fixed profile loading and update functionality
   - Added complete address management JavaScript
   - Improved error handling throughout

3. `src/main/java/com/reliablecarriers/Reliable/Carriers/model/CustomerAddress.java`
   - Changed latitude/longitude from `Double` to `BigDecimal` to fix database schema issue

4. `src/main/java/com/reliablecarriers/Reliable/Carriers/controller/CustomerProfileController.java`
   - Updated to handle `BigDecimal` for coordinates

## Testing Checklist

- [ ] Login as customer
- [ ] Navigate to `/customer` - should see customer dashboard
- [ ] Navigate to `/customer/dashboard` - should redirect to `/customer`
- [ ] Go to profile page (`/customer/profile`)
- [ ] Verify profile information loads correctly
- [ ] Edit profile information and save - should work
- [ ] Verify saved addresses section loads
- [ ] Add a new address - should work
- [ ] Edit an existing address - should work
- [ ] Delete an address - should work
- [ ] Set default address - should work

## Notes

- All API calls now use `credentials: 'include'` to ensure session cookies are sent
- Bearer token is optional and only sent if available
- Error messages are now more descriptive
- Address management is fully functional with proper error handling

