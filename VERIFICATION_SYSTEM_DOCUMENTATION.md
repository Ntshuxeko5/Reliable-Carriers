# Verification System Documentation

## Overview

The Reliable Carriers verification system ensures that all drivers and businesses are properly verified before they can operate on the platform. **All documents must be certified copies**.

## Who Verifies?

**Administrators (ADMIN role)** verify both drivers and businesses through the admin dashboard.

## Driver Verification

### Required Documents (All Certified Copies)

1. **Driver's License** (Required)
   - Valid driver's license
   - Must be valid for at least 6 months
   - Certified copy required

2. **ID Document** (Required)
   - Certified copy of National ID or passport (front and back)
   - Must be current and valid

3. **Vehicle Registration** (Required)
   - Certified copy of vehicle registration certificate (eNatis document)
   - Must match the vehicle being used for deliveries

4. **Vehicle Insurance** (Required)
   - Certified copy of valid vehicle insurance certificate
   - Must be comprehensive insurance
   - Must be current

### Optional Documents

5. **Proof of Address** (Optional)
   - Certified copy of utility bill or bank statement
   - Not older than 3 months

6. **Background Check** (Optional)
   - Certified criminal background check from SAPS (South African Police Service)

7. **Medical Certificate** (Optional)
   - Certified medical fitness certificate from registered medical practitioner

### Driver Verification Process

1. **Registration**: Driver registers and status is set to `PENDING`
2. **Document Upload**: Driver uploads certified copies via `/api/driver/documents/upload`
   - Must provide: `isCertified=true`, `certifiedBy` (name of certifying officer)
3. **Status Change**: When documents are uploaded, status changes to `DOCUMENTS_SUBMITTED`
4. **Admin Review**: Admin reviews documents via admin dashboard
   - Admin can approve/reject individual documents
   - Admin can view document details and certification information
5. **Auto-Approval**: When all required documents are verified, driver status automatically changes to `APPROVED`
6. **Notification**: Driver receives SMS notification at each stage

### Driver Verification Status Flow

```
PENDING → DOCUMENTS_SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED
```

## Business Verification

### Required Documents (All Certified Copies)

1. **Business Registration Certificate** (Required)
   - CIPC (Companies and Intellectual Property Commission) registration certificate
   - Certified copy required
   - Must match the business registration number provided

2. **VAT/Tax Registration Certificate** (Required)
   - SARS VAT registration certificate
   - Certified copy required

3. **Tax Clearance Certificate** (Required)
   - SARS tax clearance certificate
   - Certified copy required
   - Must be current

4. **Director/Owner ID** (Required)
   - Certified copy of ID document or passport of business owner/director
   - Must be current and valid

5. **Business Address Proof** (Required)
   - Certified copy of utility bill, lease agreement, or municipal rates account
   - Must be in business name
   - Not older than 3 months

6. **Bank Statement** (Required)
   - Certified bank statement (not older than 3 months)
   - Must show business account

### Optional Documents

7. **Authorization Letter** (Optional)
   - Certified letter authorizing the registrant to represent the business

8. **Business Plan** (Optional)
   - Business plan or operational overview

9. **Trade License** (Optional)
   - Municipal trade license (if applicable)
   - Certified copy

### Business Verification Process

1. **Registration**: Business registers and status is set to `PENDING`
2. **Document Upload**: Business uploads certified copies via `/api/business/documents/upload`
   - Must provide: `isCertified=true`, `certifiedBy` (name of certifying officer)
3. **Status Change**: When documents are uploaded, status changes to `UNDER_REVIEW`
4. **Admin Review**: Admin reviews documents via admin dashboard
   - Admin reviews each document individually
   - Admin can approve/reject documents
5. **Business Approval**: Once all required documents are verified, admin manually approves the business
   - Admin can set credit limit and payment terms
   - Status changes to `APPROVED`
6. **Notification**: Business receives SMS notification at each stage

### Business Verification Status Flow

```
PENDING → UNDER_REVIEW → APPROVED/REJECTED
```

## Document Certification Requirements

**All documents must be certified copies.** Documents can be certified by:

- Commissioner of Oaths
- Notary Public
- Attorney
- Bank Manager
- Police Officer
- Other authorized certifying officers

When uploading documents, users must provide:
- `isCertified`: true (required)
- `certifiedBy`: Name and title of the person who certified the document
- `certificationDate`: Date the document was certified

## Admin Verification Endpoints

### Driver Verification

- `GET /api/admin/verification/drivers/pending-documents` - Get all pending driver documents
- `POST /api/admin/verification/drivers/documents/{documentId}/verify` - Verify/reject driver document
- `GET /api/admin/verification/drivers/{driverId}/details` - Get driver verification details
- `GET /api/admin/verification/drivers/summary` - Get driver verification summary

### Business Verification

- `GET /api/admin/verification/businesses/pending-documents` - Get all pending business documents
- `POST /api/admin/verification/businesses/documents/{documentId}/verify` - Verify/reject business document
- `POST /api/admin/verification/businesses/{businessId}/verify` - Approve/reject business account
- `GET /api/admin/verification/businesses/{businessId}/details` - Get business verification details
- `GET /api/admin/verification/businesses/summary` - Get business verification summary

### Dashboard

- `GET /api/admin/verification/pending` - Get all pending verifications (combined dashboard)

## Document Upload Endpoints

### Driver Documents

- `POST /api/driver/documents/upload` - Upload driver document (requires certification details)
- `GET /api/driver/documents` - Get driver's documents
- `GET /api/driver/documents/required` - Get list of required documents
- `DELETE /api/driver/documents/{documentId}` - Delete document

### Business Documents

- `POST /api/business/documents/upload` - Upload business document (requires certification details)
- `GET /api/business/documents` - Get business documents
- `GET /api/business/documents/required` - Get list of required documents
- `DELETE /api/business/documents/{documentId}` - Delete document

## Summary

### Driver Required Documents (4)
1. Driver's License (Certified)
2. ID Document (Certified)
3. Vehicle Registration (Certified)
4. Vehicle Insurance (Certified)

### Business Required Documents (6)
1. Business Registration Certificate (Certified)
2. VAT/Tax Registration Certificate (Certified)
3. Tax Clearance Certificate (Certified)
4. Director/Owner ID (Certified)
5. Business Address Proof (Certified)
6. Bank Statement (Certified)

**All documents must be certified copies** by an authorized certifying officer (Commissioner of Oaths, Notary Public, etc.).


