# Quote and Address Features - Implementation Summary

## Overview
This document summarizes the implementation of three key features:
1. Saved Address Management on Profile Page
2. Quote Saving for Later
3. Booking Flow for Non-Logged-In Users

## 1. Saved Address Management

### Backend Components

#### CustomerAddress Model (`src/main/java/com/reliablecarriers/Reliable/Carriers/model/CustomerAddress.java`)
- New entity for storing multiple addresses per customer
- Fields include: label, address lines, city, state, zip, country, coordinates, contact info
- Supports default address designation
- Soft delete (isActive flag)

#### CustomerAddressRepository (`src/main/java/com/reliablecarriers/Reliable/Carriers/repository/CustomerAddressRepository.java`)
- JPA repository for address operations
- Methods to find active addresses, default address, etc.

#### API Endpoints (`src/main/java/com/reliablecarriers/Reliable/Carriers/controller/CustomerProfileController.java`)
- `GET /api/customer/profile/addresses` - Get all saved addresses
- `POST /api/customer/profile/addresses` - Save new address
- `PUT /api/customer/profile/addresses/{id}` - Update address
- `DELETE /api/customer/profile/addresses/{id}` - Delete address (soft delete)

### Frontend Components

#### Profile Page (`src/main/resources/templates/customer/profile.html`)
- Address list display with edit/delete functionality
- Add/Edit address modal form
- Default address indicator
- Real-time address loading from API

#### Quote Pages
- Saved addresses loaded into dropdown selectors
- Address selection auto-fills form fields including coordinates

## 2. Quote Saving for Later

### Backend Components

#### Quote Model
- Quotes are already saved to database when created via `/api/customer/quote`
- Includes expiry date, active status, all quote details

#### API Endpoints
- `GET /api/customer/quotes` - Get saved quotes for logged-in customer
- Quotes are automatically saved when created through the API

### Frontend Components

#### Quote Pages (`customer/quote.html` and `customer/quote-logged-in.html`)
- "Save Quote for Later" button added to quote results
- Quote data stored in `currentQuote` variable for later use
- Functionality to save quotes via API (requires login)

## 3. Booking Flow for Non-Logged-In Users

### Flow Description

1. **Non-Logged-In User Creates Quote**
   - User fills out quote form
   - Quote is calculated (client-side or via API)
   - User clicks "Book This Service"

2. **Authentication Check**
   - System checks for authentication token
   - If not logged in, quote data is saved to `sessionStorage` as `pendingQuoteData`
   - User is prompted to login or register

3. **Login/Registration**
   - User is redirected to login page with `?redirect=/customer/quote`
   - After successful login/registration, system checks for `pendingQuoteData`
   - If found, user is redirected to booking page with quote data

4. **Booking**
   - Quote data is transferred from `pendingQuoteData` to `quoteData`
   - User proceeds to booking page with pre-filled quote information

### Implementation Details

#### Quote Pages
- `proceedToBooking()` function checks for authentication
- Stores quote data in `sessionStorage` if not logged in
- Redirects to login with redirect parameter

#### Login Page (`login.html`)
- Handles redirect parameter
- Checks for pending quote data after successful login
- Automatically redirects to booking page if pending quote exists

#### Storage Keys
- `pendingQuoteData` - Quote data waiting for authentication
- `pendingBookingAction` - Action type ('book' or 'save')
- `quoteData` - Quote data ready for booking

## Database Schema

### customer_addresses table
```sql
CREATE TABLE customer_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    country VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(15),
    contact_name VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    place_id VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id)
);
```

## Testing Checklist

- [ ] Save new address on profile page
- [ ] Edit existing address
- [ ] Delete address
- [ ] Set default address
- [ ] Load saved addresses in quote form dropdowns
- [ ] Select saved address and verify form auto-fill
- [ ] Create quote while logged in - verify it's saved
- [ ] Create quote while not logged in - click "Book This Service"
- [ ] Verify login prompt appears
- [ ] Login and verify redirect to booking with quote data
- [ ] Register new account and verify booking flow
- [ ] Save quote for later (logged in user)
- [ ] View saved quotes

## Notes

- All amounts are displayed in South African Rands (ZAR)
- Address coordinates are optional but recommended for accurate distance calculations
- Quotes expire after a set period (configurable)
- Address deletion is soft delete - addresses are marked as inactive
- Only one default address per customer at a time

