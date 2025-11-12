-- Database Performance Optimization - Indexes
-- This script adds indexes for frequently queried fields to improve query performance

USE reliable_carriers;

-- Shipments table indexes
-- Index for status queries (most common filter)
CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments(status);

-- Index for tracking number lookups (unique, but index helps)
CREATE INDEX IF NOT EXISTS idx_shipments_tracking_number ON shipments(tracking_number);

-- Index for driver assignment queries
CREATE INDEX IF NOT EXISTS idx_shipments_assigned_driver ON shipments(assigned_driver_id);

-- Index for sender queries
CREATE INDEX IF NOT EXISTS idx_shipments_sender ON shipments(sender_id);

-- Composite index for status + date queries (common in admin dashboards)
CREATE INDEX IF NOT EXISTS idx_shipments_status_created ON shipments(status, created_at);

-- Index for coordinate-based queries (for geocoding and map features)
CREATE INDEX IF NOT EXISTS idx_shipments_pickup_coords ON shipments(pickup_latitude, pickup_longitude);
CREATE INDEX IF NOT EXISTS idx_shipments_delivery_coords ON shipments(delivery_latitude, delivery_longitude);

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_shipments_created_at ON shipments(created_at);
CREATE INDEX IF NOT EXISTS idx_shipments_estimated_delivery ON shipments(estimated_delivery_date);

-- Bookings table indexes
-- Index for status queries
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);

-- Index for booking number lookups
CREATE INDEX IF NOT EXISTS idx_bookings_booking_number ON bookings(booking_number);

-- Index for customer email queries
CREATE INDEX IF NOT EXISTS idx_bookings_customer_email ON bookings(customer_email);

-- Index for driver assignment
CREATE INDEX IF NOT EXISTS idx_bookings_assigned_driver ON bookings(assigned_driver_id);

-- Composite index for status + date queries
CREATE INDEX IF NOT EXISTS idx_bookings_status_created ON bookings(status, created_at);

-- Index for payment status queries
CREATE INDEX IF NOT EXISTS idx_bookings_payment_status ON bookings(payment_status);

-- Index for coordinate-based queries
CREATE INDEX IF NOT EXISTS idx_bookings_pickup_coords ON bookings(pickup_latitude, pickup_longitude);
CREATE INDEX IF NOT EXISTS idx_bookings_delivery_coords ON bookings(delivery_latitude, delivery_longitude);

-- Users table indexes (additional to existing)
-- Index for role-based queries
CREATE INDEX IF NOT EXISTS idx_users_role_active ON users(role, is_active);

-- Index for phone number lookups
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);

-- Index for business queries
CREATE INDEX IF NOT EXISTS idx_users_is_business ON users(is_business);

-- Index for customer tier queries
CREATE INDEX IF NOT EXISTS idx_users_customer_tier ON users(customer_tier);

-- Shipment tracking table indexes
-- Index for shipment tracking queries
CREATE INDEX IF NOT EXISTS idx_shipment_tracking_shipment ON shipment_tracking(shipment_id);

-- Composite index for tracking history queries
CREATE INDEX IF NOT EXISTS idx_shipment_tracking_shipment_date ON shipment_tracking(shipment_id, created_at);

-- Index for status queries in tracking
CREATE INDEX IF NOT EXISTS idx_shipment_tracking_status ON shipment_tracking(status);

-- Driver locations table indexes
-- Index for driver location queries
CREATE INDEX IF NOT EXISTS idx_driver_locations_driver ON driver_locations(driver_id);

-- Index for timestamp queries (for location history)
CREATE INDEX IF NOT EXISTS idx_driver_locations_timestamp ON driver_locations(timestamp);

-- Composite index for driver + timestamp queries
CREATE INDEX IF NOT EXISTS idx_driver_locations_driver_timestamp ON driver_locations(driver_id, timestamp);

-- Index for coordinate-based queries (for nearby driver searches)
CREATE INDEX IF NOT EXISTS idx_driver_locations_coords ON driver_locations(latitude, longitude);

-- Quotes table indexes
-- Index for quote ID lookups
CREATE INDEX IF NOT EXISTS idx_quotes_quote_id ON quotes(quote_id);

-- Index for customer email queries
CREATE INDEX IF NOT EXISTS idx_quotes_customer_email ON quotes(customer_email);

-- Index for expiry date queries (for cleanup jobs)
CREATE INDEX IF NOT EXISTS idx_quotes_expiry_date ON quotes(expiry_date);

-- Index for active quotes
CREATE INDEX IF NOT EXISTS idx_quotes_active ON quotes(is_active);

-- Payments table indexes
-- Index for booking payment queries
CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(booking_id);

-- Index for transaction ID lookups
CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payments(transaction_id);

-- Index for payment status queries
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);

-- Two-factor tokens table indexes
-- Index for user token queries
CREATE INDEX IF NOT EXISTS idx_two_factor_tokens_user ON two_factor_tokens(user_id);

-- Index for token lookups
CREATE INDEX IF NOT EXISTS idx_two_factor_tokens_token ON two_factor_tokens(token);

-- Index for expiry cleanup queries
CREATE INDEX IF NOT EXISTS idx_two_factor_tokens_expires ON two_factor_tokens(expires_at);

-- Index for used tokens cleanup
CREATE INDEX IF NOT EXISTS idx_two_factor_tokens_used ON two_factor_tokens(used);

-- Contact messages table indexes
-- Index for date queries
CREATE INDEX IF NOT EXISTS idx_contact_messages_created_at ON contact_messages(created_at);

-- Index for read status queries
CREATE INDEX IF NOT EXISTS idx_contact_messages_read ON contact_messages(is_read);

-- Moving services table indexes
-- Index for customer queries
CREATE INDEX IF NOT EXISTS idx_moving_services_customer ON moving_services(customer_id);

-- Index for status queries
CREATE INDEX IF NOT EXISTS idx_moving_services_status ON moving_services(status);

-- Index for scheduled date queries
CREATE INDEX IF NOT EXISTS idx_moving_services_scheduled_date ON moving_services(scheduled_date);

-- Vehicles table indexes (additional to existing)
-- Index for available vehicles queries
CREATE INDEX IF NOT EXISTS idx_vehicles_available ON vehicles(is_available, vehicle_type);

-- Index for assigned driver queries
CREATE INDEX IF NOT EXISTS idx_vehicles_assigned_driver ON vehicles(assigned_driver_id);

-- Print summary
SELECT 'Database indexes created successfully!' AS status;

