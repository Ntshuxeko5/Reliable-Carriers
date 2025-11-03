# Admin User Manual

## Welcome, Administrator! 👨‍💼

This comprehensive guide covers all administrative functions of the Reliable Carriers platform.

---

## Table of Contents

1. [Admin Dashboard](#admin-dashboard)
2. [User Management](#user-management)
3. [Package Management](#package-management)
4. [Driver Management](#driver-management)
5. [Business Management](#business-management)
6. [Analytics & Reports](#analytics--reports)
7. [System Configuration](#system-configuration)
8. [Notifications Management](#notifications-management)
9. [Security & Monitoring](#security--monitoring)
10. [Troubleshooting](#troubleshooting)

---

## Admin Dashboard

### Accessing Admin Panel

1. Log in with admin credentials
2. Navigate to **"Admin Dashboard"**
3. View system overview

### Dashboard Overview

#### Key Metrics
- **Total Users**: Customers, drivers, businesses
- **Active Packages**: In transit right now
- **Today's Deliveries**: Scheduled for today
- **Revenue**: Today/Week/Month totals
- **System Health**: Server status, errors

#### Quick Actions
- ➕ **Create Package**: Manual package entry
- 👤 **Add User**: Create new user account
- 🚚 **Assign Driver**: Assign packages to drivers
- 📊 **Generate Report**: Quick reports

#### Activity Feed
- Recent user registrations
- Package status changes
- Payment transactions
- System alerts

#### Real-Time Monitoring
- **Live Driver Tracking**: See all drivers on map
- **Package Status**: Real-time package locations
- **System Alerts**: Critical notifications

---

## User Management

### Viewing Users

1. Go to **"Users"** in admin menu
2. View all users with filters:
   - **Role**: Customer, Driver, Business, Admin
   - **Status**: Active, Inactive, Suspended
   - **Tier**: Individual, Business, Enterprise, Premium
   - **Registration Date**: Filter by date range

### User Details

Click on any user to view:
- Personal information
- Package history
- Payment history
- Account status
- Login history
- Support tickets

### Creating Users

#### Manual User Creation

1. Click **"Add User"**
2. Fill in details:
   - **Name**: First and last name
   - **Email**: Valid email address
   - **Phone**: Contact number
   - **Role**: Select appropriate role
   - **Password**: Temporary password (user must change)
3. Click **"Create User"**
4. User receives welcome email

#### Bulk User Import

1. Go to **"Import Users"**
2. Download template CSV
3. Fill in user data
4. Upload CSV file
5. Review and confirm
6. Users created in bulk

### Editing Users

1. Find user in list
2. Click **"Edit"**
3. Update information:
   - Personal details
   - Contact information
   - Account status
   - Role permissions
4. Save changes

### User Status Management

#### Activate User
- Account becomes active
- Can log in and use system

#### Deactivate User
- Account temporarily disabled
- Cannot log in
- Packages unaffected

#### Suspend User
- Account suspended
- Usually for policy violations
- Requires admin approval to reactivate

#### Delete User
- ⚠️ **Warning**: Permanent action
- All data removed
- Cannot be undone
- Use with caution

### Role Management

#### Customer
- Basic package booking
- Tracking
- Dashboard access

#### Driver
- Delivery assignments
- Status updates
- Earnings access

#### Business
- API access
- Bulk operations
- Business dashboard

#### Admin
- Full system access
- User management
- System configuration

---

## Package Management

### Viewing Packages

1. Go to **"Packages"** in admin menu
2. View all packages with filters:
   - **Status**: All statuses
   - **Date Range**: Filter by date
   - **Driver**: Filter by assigned driver
   - **Service Type**: Standard, Express, Same-Day

### Package Details

Click any package to view:
- Sender and recipient information
- Package details (weight, dimensions)
- Tracking history
- Status updates
- Driver assignment
- Payment information
- Delivery proof

### Creating Packages Manually

1. Click **"Create Package"**
2. Enter sender details
3. Enter recipient details
4. Add package information:
   - Weight
   - Dimensions
   - Type
   - Value
5. Select service type
6. Set pickup/delivery dates
7. Save package

### Assigning Drivers

#### Single Assignment

1. Open package details
2. Click **"Assign Driver"**
3. Select driver from list
4. View driver:
   - Current location
   - Active packages
   - Rating
   - Availability
5. Confirm assignment
6. Driver receives notification

#### Bulk Assignment

1. Select multiple packages
2. Click **"Bulk Assign"**
3. Select driver
4. System optimizes route
5. All packages assigned
6. Driver receives batch notification

#### Auto-Assignment

Enable auto-assignment:
1. Go to **"Settings"** → **"Auto-Assignment"**
2. Configure rules:
   - Proximity-based
   - Load balancing
   - Driver rating priority
3. Save settings
4. System automatically assigns

### Updating Package Status

Manually update status if needed:
1. Open package details
2. Click **"Update Status"**
3. Select new status
4. Add notes
5. Save update
6. Customer notified

### Package Search

Search packages by:
- **Tracking Number**: Exact match
- **Sender Email/Phone**: Find all sender packages
- **Recipient Email/Phone**: Find all recipient packages
- **Driver**: All driver's packages
- **Date Range**: Packages in date range

---

## Driver Management

### Viewing Drivers

1. Go to **"Drivers"** in admin menu
2. View all drivers with:
   - **Status**: Online, Offline, On Break
   - **Rating**: Performance rating
   - **Active Packages**: Current assignments
   - **Location**: Real-time GPS position

### Driver Details

View comprehensive driver information:
- Personal information
- Vehicle details
- Documents (license, insurance)
- Performance metrics
- Earnings summary
- Rating history
- Package history

### Driver Verification

#### Document Verification

1. Go to **"Pending Verifications"**
2. Review uploaded documents:
   - Driver's license
   - ID document
   - Vehicle registration
   - Insurance
3. Verify authenticity
4. Approve or reject

#### Approval Process

**Approve Driver**:
1. Verify all documents
2. Confirm background check
3. Click **"Approve"**
4. Driver receives approval email
5. Driver can start accepting deliveries

**Reject Driver**:
1. Select reason for rejection
2. Add notes
3. Click **"Reject"**
4. Driver receives rejection email with reason

### Driver Performance

#### Performance Metrics

- **Completion Rate**: % successful deliveries
- **On-Time Rate**: % on-time deliveries
- **Average Rating**: Customer ratings
- **Earnings**: Total and average
- **Active Hours**: Time online

#### Performance Reports

1. Select driver
2. Click **"Performance Report"**
3. View:
   - Weekly/monthly stats
   - Trend analysis
   - Comparison with others
   - Recommendations

### Driver Actions

#### Suspend Driver
- Temporarily disable driver account
- Cannot accept new packages
- Complete existing deliveries

#### Remove Driver
- Permanently remove driver
- Reassign packages
- Finalize earnings

#### Send Message
- Direct communication
- Important updates
- Performance feedback

---

## Business Management

### Viewing Businesses

1. Go to **"Businesses"** in admin menu
2. View all business accounts:
   - **Status**: Active, Pending Verification, Suspended
   - **Tier**: Business, Enterprise, Premium
   - **API Usage**: API call statistics

### Business Verification

#### Verification Process

1. Review business registration:
   - Company name
   - Registration number
   - Business address
   - Contact details
2. Verify business documents:
   - Registration certificate
   - Tax certificate
   - Bank details
3. Approve or reject

#### Business Tiers

**Business Tier**:
- Basic API access
- Standard rates
- Email support

**Enterprise Tier**:
- Advanced API features
- Discounted rates
- Priority support
- Custom integration

**Premium Tier**:
- Full API access
- Best rates
- Dedicated support
- Custom features

### API Key Management

View and manage business API keys:
- **Active Keys**: Currently in use
- **Usage Statistics**: API call counts
- **Rate Limits**: Current limits
- **Revoke Keys**: Disable access

### Business Analytics

View business-specific analytics:
- Package volume
- API usage
- Payment history
- Growth trends

---

## Analytics & Reports

### Dashboard Analytics

#### Revenue Reports
- Daily, weekly, monthly revenue
- Payment method breakdown
- Service type revenue
- Trend analysis

#### Package Reports
- Total packages by period
- Status breakdown
- Service type distribution
- Success rate

#### User Reports
- User growth
- Active users
- User retention
- Registration sources

#### Driver Reports
- Driver performance
- Earnings distribution
- Rating statistics
- Activity levels

### Generating Reports

1. Go to **"Reports"** in admin menu
2. Select report type
3. Choose date range
4. Apply filters
5. Generate report
6. Export to:
   - PDF
   - Excel
   - CSV

### Custom Reports

Create custom reports:
1. Click **"Custom Report"**
2. Select metrics
3. Choose filters
4. Set date range
5. Save report template
6. Generate and export

---

## System Configuration

### General Settings

#### Application Settings
- **App Name**: System name
- **Base URL**: Application URL
- **Contact Email**: Support email
- **Phone Number**: Support phone

#### Service Settings
- **Service Types**: Standard, Express, Same-Day
- **Pricing Rules**: Base rates, multipliers
- **Delivery Zones**: Serviceable areas
- **Operating Hours**: Business hours

### Email Configuration

#### SMTP Settings
- **Host**: SMTP server
- **Port**: SMTP port
- **Username**: Email account
- **Password**: App password
- **Test**: Send test email

#### Email Templates
- Welcome emails
- Order confirmations
- Status updates
- Password resets

### SMS Configuration

#### SMS Provider Settings
- **Provider**: SMS provider (SMSPortal)
- **API Key**: Provider API key
- **API Secret**: Provider secret
- **Test**: Send test SMS

### Payment Configuration

#### Paystack Settings
- **Secret Key**: Paystack secret
- **Public Key**: Paystack public key
- **Webhook Secret**: Webhook verification
- **Test Mode**: Enable/disable test mode

### Notification Settings

Configure notification preferences:
- **Email Notifications**: Enable/disable
- **SMS Notifications**: Enable/disable
- **Push Notifications**: Enable/disable
- **Notification Triggers**: What triggers notifications

---

## Notifications Management

### System Notifications

View all system notifications:
- User registrations
- Package updates
- Payment transactions
- System alerts

### Sending Notifications

#### Manual Notification
1. Go to **"Notifications"**
2. Click **"Send Notification"**
3. Select recipients:
   - All users
   - Specific users
   - User groups
4. Choose method:
   - Email
   - SMS
   - Push
5. Write message
6. Send notification

### Notification Templates

Manage notification templates:
- Create templates
- Edit existing
- Use variables
- Preview before sending

---

## Security & Monitoring

### Security Monitoring

#### Login Monitoring
- Failed login attempts
- Suspicious activity
- IP addresses
- Account lockouts

#### Audit Logs
- User actions
- System changes
- Configuration updates
- Security events

### System Health

#### Server Status
- CPU usage
- Memory usage
- Disk space
- Network status

#### Database Health
- Connection pool
- Query performance
- Database size
- Backup status

#### Application Health
- Error rates
- Response times
- API performance
- Service status

### Backup & Recovery

#### Manual Backup
1. Go to **"Backups"**
2. Click **"Create Backup"**
3. Select backup type:
   - Full backup
   - Database only
   - Files only
4. Download backup

#### Automated Backups
- Configure schedule
- Backup retention
- Storage location
- Notification settings

---

## Troubleshooting

### Common Issues

#### User Cannot Login
1. Check account status
2. Verify credentials
3. Check account lockout
4. Reset password if needed

#### Package Not Updating
1. Check driver assignment
2. Verify GPS location
3. Check system status
4. Manually update if needed

#### Payment Issues
1. Verify payment gateway
2. Check transaction logs
3. Review payment settings
4. Contact payment provider

#### System Performance
1. Check server resources
2. Review error logs
3. Check database performance
4. Optimize if needed

### Support Contacts

- **Technical Support**: tech@reliablecarriers.co.za
- **System Admin**: admin@reliablecarriers.co.za
- **Emergency**: [Emergency Contact]

---

**Thank you for managing the Reliable Carriers platform!** 🎯

