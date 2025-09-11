-- Reliable Carriers Database Setup Script
-- MySQL Database Creation and Initial Setup

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS reliable_carriers
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE reliable_carriers;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER', 'DRIVER', 'STAFF', 'TRACKING_MANAGER') NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (is_active)
);

-- Vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type VARCHAR(50) NOT NULL,
    make VARCHAR(50),
    model VARCHAR(50),
    year INT,
    license_plate VARCHAR(20) UNIQUE,
    capacity_kg DECIMAL(10,2),
    color VARCHAR(100),
    fuel_type VARCHAR(50),
    mileage DOUBLE,
    assigned_driver_id BIGINT,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_license_plate (license_plate),
    INDEX idx_available (is_available),
    INDEX idx_assigned_driver (assigned_driver_id)
);

-- Shipments table
CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_number VARCHAR(50) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(100),
    recipient_phone VARCHAR(20),
    pickup_address VARCHAR(255) NOT NULL,
    pickup_city VARCHAR(100) NOT NULL,
    pickup_state VARCHAR(50) NOT NULL,
    pickup_zip_code VARCHAR(20) NOT NULL,
    pickup_country VARCHAR(50) DEFAULT 'South Africa',
    delivery_address VARCHAR(255) NOT NULL,
    delivery_city VARCHAR(100) NOT NULL,
    delivery_state VARCHAR(50) NOT NULL,
    delivery_zip_code VARCHAR(20) NOT NULL,
    delivery_country VARCHAR(50) DEFAULT 'South Africa',
    weight DECIMAL(10,2) NOT NULL,
    dimensions VARCHAR(100),
    description TEXT,
    shipping_cost DECIMAL(10,2) NOT NULL,
    service_type ENUM('ECONOMY', 'OVERNIGHT', 'SAME_DAY') NOT NULL DEFAULT 'OVERNIGHT',
    status ENUM('PENDING', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED_DELIVERY', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    assigned_driver_id BIGINT,
    assigned_vehicle_id BIGINT,
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_driver_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    INDEX idx_tracking_number (tracking_number),
    INDEX idx_sender_id (sender_id),
    INDEX idx_status (status),
    INDEX idx_assigned_driver (assigned_driver_id),
    INDEX idx_created_at (created_at)
);

-- Shipment tracking table
CREATE TABLE IF NOT EXISTS shipment_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    status ENUM('PENDING', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED_DELIVERY', 'CANCELLED') NOT NULL,
    location VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Driver locations table
CREATE TABLE IF NOT EXISTS driver_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    address VARCHAR(200),
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'South Africa',
    vehicle_id BIGINT,
    notes VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    INDEX idx_driver_id (driver_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_coordinates (latitude, longitude)
);

-- Moving services table
CREATE TABLE IF NOT EXISTS moving_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    service_type ENUM('MOVING', 'DELIVERY', 'PICKUP') NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    distance_km DECIMAL(10,2) NOT NULL,
    description TEXT,
    weight_kg DECIMAL(10,2),
    number_of_items INT,
    total_cost DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    assigned_driver_id BIGINT,
    scheduled_date DATE,
    completed_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_driver_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_assigned_driver (assigned_driver_id),
    INDEX idx_scheduled_date (scheduled_date)
);

-- Quotes table (for storing generated quotes)
CREATE TABLE IF NOT EXISTS quotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quote_id VARCHAR(50) NOT NULL UNIQUE,
    customer_email VARCHAR(100) NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    weight DECIMAL(10,2) NOT NULL,
    dimensions VARCHAR(100),
    description TEXT,
    total_cost DECIMAL(10,2) NOT NULL,
    service_type ENUM('ECONOMY', 'OVERNIGHT', 'SAME_DAY') NOT NULL,
    estimated_delivery_time VARCHAR(100),
    estimated_delivery_date DATE,
    expiry_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_quote_id (quote_id),
    INDEX idx_customer_email (customer_email),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_active (is_active)
);

-- Insurance options table
CREATE TABLE IF NOT EXISTS insurance_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    coverage_amount DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default insurance options
INSERT INTO insurance_options (type, description, cost, coverage_amount) VALUES
('BASIC', 'Basic coverage up to $100', 5.00, 100.00),
('STANDARD', 'Standard coverage up to $500', 15.00, 500.00),
('PREMIUM', 'Premium coverage up to $1000', 25.00, 1000.00)
ON DUPLICATE KEY UPDATE
description = VALUES(description),
cost = VALUES(cost),
coverage_amount = VALUES(coverage_amount);

-- Create default admin user (password: admin123)
INSERT INTO users (first_name, last_name, email, phone, password, role) VALUES
('Admin', 'User', 'admin@reliablecarriers.com', '+27123456789', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ADMIN')
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name),
last_name = VALUES(last_name),
phone = VALUES(phone);

-- Create sample vehicles with detailed information
INSERT INTO vehicles (vehicle_type, make, model, year, license_plate, capacity_kg, color, fuel_type, mileage) VALUES
('Van', 'Toyota', 'Hiace', 2020, 'GP123456', 1000.00, 'White', 'Diesel', 45000.00),
('Truck', 'Isuzu', 'N-Series', 2019, 'GP789012', 3000.00, 'Blue', 'Diesel', 65000.00),
('Van', 'Ford', 'Transit', 2021, 'GP345678', 1200.00, 'Silver', 'Petrol', 32000.00),
('Van', 'Mercedes-Benz', 'Sprinter', 2022, 'GP901234', 1500.00, 'Black', 'Diesel', 28000.00),
('Truck', 'Volvo', 'FH16', 2018, 'GP567890', 5000.00, 'Red', 'Diesel', 85000.00),
('Van', 'Nissan', 'NV350', 2020, 'GP234567', 1100.00, 'Grey', 'Petrol', 38000.00)
ON DUPLICATE KEY UPDATE
vehicle_type = VALUES(vehicle_type),
make = VALUES(make),
model = VALUES(model),
year = VALUES(year),
capacity_kg = VALUES(capacity_kg),
color = VALUES(color),
fuel_type = VALUES(fuel_type),
mileage = VALUES(mileage);

-- Create sample drivers
INSERT INTO users (first_name, last_name, email, phone, password, role) VALUES
('John', 'Driver', 'driver@reliablecarriers.com', '+27123456788', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'DRIVER'),
('Sarah', 'Wilson', 'sarah.wilson@reliablecarriers.com', '+27123456787', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'DRIVER'),
('Michael', 'Brown', 'michael.brown@reliablecarriers.com', '+27123456786', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'DRIVER'),
('Lisa', 'Garcia', 'lisa.garcia@reliablecarriers.com', '+27123456785', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'DRIVER'),
('David', 'Martinez', 'david.martinez@reliablecarriers.com', '+27123456784', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'DRIVER'),
('Emma', 'Johnson', 'emma.johnson@reliablecarriers.com', '+27123456783', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'DRIVER')
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name),
last_name = VALUES(last_name),
phone = VALUES(phone);

-- Assign vehicles to drivers
UPDATE vehicles SET assigned_driver_id = (SELECT id FROM users WHERE email = 'driver@reliablecarriers.com' LIMIT 1) WHERE license_plate = 'GP123456';
UPDATE vehicles SET assigned_driver_id = (SELECT id FROM users WHERE email = 'sarah.wilson@reliablecarriers.com' LIMIT 1) WHERE license_plate = 'GP789012';
UPDATE vehicles SET assigned_driver_id = (SELECT id FROM users WHERE email = 'michael.brown@reliablecarriers.com' LIMIT 1) WHERE license_plate = 'GP345678';
UPDATE vehicles SET assigned_driver_id = (SELECT id FROM users WHERE email = 'lisa.garcia@reliablecarriers.com' LIMIT 1) WHERE license_plate = 'GP901234';
UPDATE vehicles SET assigned_driver_id = (SELECT id FROM users WHERE email = 'david.martinez@reliablecarriers.com' LIMIT 1) WHERE license_plate = 'GP567890';
UPDATE vehicles SET assigned_driver_id = (SELECT id FROM users WHERE email = 'emma.johnson@reliablecarriers.com' LIMIT 1) WHERE license_plate = 'GP234567';

-- Create sample customer
INSERT INTO users (first_name, last_name, email, phone, password, role) VALUES
('Jane', 'Customer', 'customer@example.com', '+27123456787', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'CUSTOMER')
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name),
last_name = VALUES(last_name),
phone = VALUES(phone);

-- Create sample tracking manager
INSERT INTO users (first_name, last_name, email, phone, password, role) VALUES
('Alex', 'Tracker', 'tracking@reliablecarriers.com', '+27123456782', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'TRACKING_MANAGER')
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name),
last_name = VALUES(last_name),
phone = VALUES(phone);

-- Create sample staff user
INSERT INTO users (first_name, last_name, email, phone, password, role) VALUES
('Staff', 'User', 'staff@reliablecarriers.com', '+27123456781', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'STAFF')
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name),
last_name = VALUES(last_name),
phone = VALUES(phone);

-- Create sample shipments for testing driver workboard
INSERT INTO shipments (tracking_number, sender_id, recipient_name, recipient_email, recipient_phone, 
                      pickup_address, pickup_city, pickup_state, pickup_zip_code, pickup_country,
                      delivery_address, delivery_city, delivery_state, delivery_zip_code, delivery_country,
                      weight, dimensions, description, shipping_cost, service_type, status, assigned_driver_id, estimated_delivery_date) VALUES
('RC001234567', 1, 'John Smith', 'john.smith@email.com', '+27123456701', 
 '123 Main St', 'Johannesburg', 'Gauteng', '2000', 'South Africa',
 '456 Oak Ave', 'Pretoria', 'Gauteng', '0001', 'South Africa',
 5.5, '30x20x15cm', 'Electronics package', 150.00, 'OVERNIGHT', 'PENDING', NULL, DATE_ADD(CURDATE(), INTERVAL 1 DAY)),

('RC001234568', 1, 'Sarah Johnson', 'sarah.j@email.com', '+27123456702',
 '789 Pine Rd', 'Cape Town', 'Western Cape', '8001', 'South Africa',
 '321 Elm St', 'Durban', 'KwaZulu-Natal', '4000', 'South Africa',
 3.2, '25x18x12cm', 'Clothing items', 120.00, 'OVERNIGHT', 'PENDING', NULL, DATE_ADD(CURDATE(), INTERVAL 1 DAY)),

('RC001234569', 1, 'Mike Wilson', 'mike.w@email.com', '+27123456703',
 '654 Maple Dr', 'Port Elizabeth', 'Eastern Cape', '6001', 'South Africa',
 '987 Cedar Ln', 'Bloemfontein', 'Free State', '9300', 'South Africa',
 8.1, '40x30x25cm', 'Heavy machinery parts', 200.00, 'OVERNIGHT', 'ASSIGNED', 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY)),

('RC001234570', 1, 'Lisa Brown', 'lisa.b@email.com', '+27123456704',
 '147 Birch Ave', 'East London', 'Eastern Cape', '5200', 'South Africa',
 '258 Spruce St', 'Kimberley', 'Northern Cape', '8300', 'South Africa',
 2.8, '20x15x10cm', 'Documents and books', 80.00, 'OVERNIGHT', 'IN_TRANSIT', 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY)),

('RC001234571', 1, 'David Miller', 'david.m@email.com', '+27123456705',
 '369 Willow Rd', 'Nelspruit', 'Mpumalanga', '1200', 'South Africa',
 '741 Poplar Dr', 'Polokwane', 'Limpopo', '0700', 'South Africa',
 6.5, '35x25x20cm', 'Home appliances', 180.00, 'OVERNIGHT', 'PENDING', NULL, DATE_ADD(CURDATE(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE
recipient_name = VALUES(recipient_name),
recipient_email = VALUES(recipient_email),
recipient_phone = VALUES(recipient_phone),
delivery_address = VALUES(delivery_address),
delivery_city = VALUES(delivery_city),
delivery_state = VALUES(delivery_state),
status = VALUES(status),
assigned_driver_id = VALUES(assigned_driver_id);

-- Create sample driver locations for testing
INSERT INTO driver_locations (driver_id, latitude, longitude, address, city, state, zip_code, country, vehicle_id, notes, timestamp) VALUES
(2, -26.2041, 28.0473, '123 Main Street, Johannesburg', 'Johannesburg', 'Gauteng', '2000', 'South Africa', 1, 'Current location', NOW()),
(3, -33.9249, 18.4241, '456 Oak Avenue, Cape Town', 'Cape Town', 'Western Cape', '8001', 'South Africa', 2, 'On delivery route', NOW()),
(4, -29.8587, 31.0218, '789 Pine Road, Durban', 'Durban', 'KwaZulu-Natal', '4000', 'South Africa', 3, 'At pickup location', NOW()),
(5, -25.7479, 28.2293, '321 Elm Street, Pretoria', 'Pretoria', 'Gauteng', '0001', 'South Africa', 4, 'Returning to depot', NOW()),
(6, -26.6731, 27.9261, '654 Maple Drive, Krugersdorp', 'Krugersdorp', 'Gauteng', '1739', 'South Africa', 5, 'En route to delivery', NOW()),
(7, -26.1076, 28.0567, '987 Cedar Lane, Sandton', 'Sandton', 'Gauteng', '2196', 'South Africa', 6, 'At customer location', NOW())
ON DUPLICATE KEY UPDATE
latitude = VALUES(latitude),
longitude = VALUES(longitude),
address = VALUES(address),
city = VALUES(city),
state = VALUES(state),
timestamp = VALUES(timestamp);

-- Create sample proof of delivery records
INSERT INTO proof_of_delivery (shipment_id, driver_id, delivery_date, delivery_location, recipient_name, recipient_phone, delivery_method, signature_required, photo_required, id_verification_required, delivery_status, created_at, updated_at) VALUES
(1, 2, CURDATE(), '456 Oak Ave, Pretoria, Gauteng', 'John Smith', '+27123456701', 'HAND_TO_RECIPIENT', TRUE, TRUE, FALSE, 'COMPLETED', NOW(), NOW()),
(2, 3, CURDATE(), '321 Elm St, Durban, KwaZulu-Natal', 'Sarah Johnson', '+27123456702', 'HAND_TO_RECIPIENT', TRUE, TRUE, FALSE, 'COMPLETED', NOW(), NOW()),
(3, 4, CURDATE(), '987 Cedar Ln, Bloemfontein, Free State', 'Mike Wilson', '+27123456703', 'HAND_TO_RECIPIENT', TRUE, TRUE, FALSE, 'COMPLETED', NOW(), NOW())
ON DUPLICATE KEY UPDATE
delivery_date = VALUES(delivery_date),
delivery_location = VALUES(delivery_location),
delivery_status = VALUES(delivery_status),
updated_at = VALUES(updated_at);

-- Add sample data for new tables

-- Sample customer feedback
INSERT INTO customer_feedback (shipment_id, customer_id, driver_id, rating, feedback_text, feedback_type) VALUES
(1, 1, 2, 5, 'Excellent service! Package was delivered on time and in perfect condition.', 'DELIVERY'),
(2, 1, 3, 4, 'Good service, driver was professional and courteous.', 'DELIVERY'),
(3, 1, 4, 5, 'Outstanding delivery experience. Highly recommended!', 'DELIVERY')
ON DUPLICATE KEY UPDATE
rating = VALUES(rating),
feedback_text = VALUES(feedback_text);

-- Sample payments
INSERT INTO payments (shipment_id, customer_id, amount, currency, payment_method, payment_status, transaction_id, payment_date) VALUES
(1, 1, 150.00, 'ZAR', 'PAYSTACK', 'COMPLETED', 'TXN001234567', NOW()),
(2, 1, 120.00, 'ZAR', 'PAYSTACK', 'COMPLETED', 'TXN001234568', NOW()),
(3, 1, 200.00, 'ZAR', 'PAYSTACK', 'COMPLETED', 'TXN001234569', NOW()),
(4, 1, 80.00, 'ZAR', 'PAYSTACK', 'COMPLETED', 'TXN001234570', NOW()),
(5, 1, 180.00, 'ZAR', 'PAYSTACK', 'PENDING', 'TXN001234571', NULL)
ON DUPLICATE KEY UPDATE
amount = VALUES(amount),
payment_status = VALUES(payment_status);

-- Sample automated dispatch records
INSERT INTO automated_dispatch (shipment_id, driver_id, vehicle_id, dispatch_type, dispatch_status, priority_level, estimated_pickup_time, estimated_delivery_time) VALUES
(3, 2, 1, 'AUTOMATIC', 'ASSIGNED', 'MEDIUM', DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR)),
(4, 2, 1, 'AUTOMATIC', 'ACCEPTED', 'HIGH', DATE_ADD(NOW(), INTERVAL 30 MINUTE), DATE_ADD(NOW(), INTERVAL 2 HOUR))
ON DUPLICATE KEY UPDATE
dispatch_status = VALUES(dispatch_status),
priority_level = VALUES(priority_level);

-- Sample integration webhooks
INSERT INTO integration_webhooks (webhook_name, webhook_url, webhook_type, is_active, secret_key) VALUES
('Shipday Integration', 'https://api.shipday.com/webhooks/reliable-carriers', 'SHIPMENT_UPDATE', TRUE, 'webhook_secret_key_123'),
('Customer Notification', 'https://reliable-carriers.com/api/webhooks/customer-notifications', 'DELIVERY_CONFIRMATION', TRUE, 'customer_webhook_secret_456'),
('Payment Gateway', 'https://api.paystack.co/webhooks/reliable-carriers', 'PAYMENT_UPDATE', TRUE, 'paystack_webhook_secret_789')
ON DUPLICATE KEY UPDATE
webhook_url = VALUES(webhook_url),
is_active = VALUES(is_active);

-- Show created tables
SHOW TABLES;

-- Show table structures
DESCRIBE users;
DESCRIBE vehicles;
DESCRIBE shipments;
DESCRIBE shipment_tracking;
DESCRIBE driver_locations;
DESCRIBE moving_services;
DESCRIBE quotes;
DESCRIBE insurance_options;

-- Add missing tables for existing entities

-- Proof of Delivery table
CREATE TABLE IF NOT EXISTS proof_of_delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    delivery_date DATE NOT NULL,
    delivery_location VARCHAR(255) NOT NULL,
    recipient_signature TEXT,
    delivery_photo_url VARCHAR(1000),
    package_photo_url VARCHAR(1000),
    recipient_name VARCHAR(100),
    recipient_phone VARCHAR(20),
    recipient_id_number VARCHAR(100),
    delivery_notes TEXT,
    delivery_method VARCHAR(50),
    signature_required BOOLEAN DEFAULT TRUE,
    photo_required BOOLEAN DEFAULT TRUE,
    id_verification_required BOOLEAN DEFAULT FALSE,
    delivery_status VARCHAR(50) DEFAULT 'COMPLETED',
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_driver_id (driver_id),
    INDEX idx_delivery_date (delivery_date),
    INDEX idx_delivery_status (delivery_status)
);

-- Customer Feedback table
CREATE TABLE IF NOT EXISTS customer_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT,
    customer_id BIGINT NOT NULL,
    driver_id BIGINT,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback_text TEXT,
    feedback_type ENUM('DELIVERY', 'SERVICE', 'WEBSITE', 'GENERAL') DEFAULT 'GENERAL',
    is_resolved BOOLEAN DEFAULT FALSE,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE SET NULL,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_driver_id (driver_id),
    INDEX idx_rating (rating),
    INDEX idx_feedback_type (feedback_type)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'ZAR',
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'CASH', 'PAYSTACK', 'PAYPAL') NOT NULL,
    payment_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED') DEFAULT 'PENDING',
    transaction_id VARCHAR(100) UNIQUE,
    payment_date TIMESTAMP,
    refund_amount DECIMAL(10,2) DEFAULT 0.00,
    refund_reason TEXT,
    payment_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_transaction_id (transaction_id)
);

-- Automated Dispatch table
CREATE TABLE IF NOT EXISTS automated_dispatch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    driver_id BIGINT,
    vehicle_id BIGINT,
    dispatch_type ENUM('AUTOMATIC', 'MANUAL', 'OPTIMIZED') DEFAULT 'AUTOMATIC',
    dispatch_status ENUM('PENDING', 'ASSIGNED', 'ACCEPTED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING',
    priority_level ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    estimated_pickup_time DATETIME,
    estimated_delivery_time DATETIME,
    actual_pickup_time DATETIME,
    actual_delivery_time DATETIME,
    dispatch_notes TEXT,
    optimization_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_driver_id (driver_id),
    INDEX idx_dispatch_status (dispatch_status),
    INDEX idx_priority_level (priority_level)
);

-- Integration Webhooks table
CREATE TABLE IF NOT EXISTS integration_webhooks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    webhook_name VARCHAR(100) NOT NULL,
    webhook_url VARCHAR(500) NOT NULL,
    webhook_type ENUM('SHIPMENT_UPDATE', 'DELIVERY_CONFIRMATION', 'PAYMENT_UPDATE', 'DRIVER_LOCATION', 'CUSTOM') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    secret_key VARCHAR(255),
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    last_triggered TIMESTAMP,
    last_response_code INT,
    last_response_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_webhook_type (webhook_type),
    INDEX idx_is_active (is_active)
);

-- Webhook Events table
CREATE TABLE IF NOT EXISTS webhook_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    webhook_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSON,
    status ENUM('PENDING', 'SENT', 'FAILED', 'RETRY') DEFAULT 'PENDING',
    response_code INT,
    response_message TEXT,
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (webhook_id) REFERENCES integration_webhooks(id) ON DELETE CASCADE,
    INDEX idx_webhook_id (webhook_id),
    INDEX idx_status (status),
    INDEX idx_event_type (event_type)
);

-- Audit Logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    user_email VARCHAR(255),
    user_role VARCHAR(100),
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    execution_time_ms BIGINT,
    additional_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_user_email (user_email),
    INDEX idx_user_role (user_role),
    INDEX idx_action (action),
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_id (entity_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_ip_address (ip_address),
    INDEX idx_session_id (session_id)
);

-- Sample audit logs
INSERT INTO audit_logs (user_id, user_email, user_role, action, entity_type, entity_id, status, ip_address, user_agent, created_at) VALUES
(1, 'admin@reliablecarriers.com', 'ADMIN', 'LOGIN', 'USER', 1, 'SUCCESS', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 1 HOUR),
(2, 'driver1@reliablecarriers.com', 'DRIVER', 'LOGIN', 'USER', 2, 'SUCCESS', '192.168.1.101', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)', NOW() - INTERVAL 2 HOUR),
(3, 'customer1@example.com', 'CUSTOMER', 'SHIPMENT_CREATION', 'SHIPMENT', 1, 'SUCCESS', '192.168.1.102', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 3 HOUR),
(1, 'admin@reliablecarriers.com', 'ADMIN', 'DRIVER_ASSIGNMENT', 'SHIPMENT', 1, 'SUCCESS', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 4 HOUR),
(2, 'driver1@reliablecarriers.com', 'DRIVER', 'DELIVERY_CONFIRMATION', 'SHIPMENT', 1, 'SUCCESS', '192.168.1.101', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)', NOW() - INTERVAL 5 HOUR),
(4, 'tracking@reliablecarriers.com', 'TRACKING_MANAGER', 'LOGIN', 'USER', 4, 'SUCCESS', '192.168.1.103', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NOW() - INTERVAL 6 HOUR),
(5, 'staff@reliablecarriers.com', 'STAFF', 'USER_CREATION', 'USER', 6, 'SUCCESS', '192.168.1.104', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 7 HOUR),
(1, 'admin@reliablecarriers.com', 'ADMIN', 'PAYMENT', 'PAYMENT', 1, 'SUCCESS', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 8 HOUR),
(3, 'customer1@example.com', 'CUSTOMER', 'LOGIN', 'USER', 3, 'FAILED', '192.168.1.102', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 9 HOUR),
(2, 'driver1@reliablecarriers.com', 'DRIVER', 'FILE_UPLOAD', 'FILE', NULL, 'SUCCESS', '192.168.1.101', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)', NOW() - INTERVAL 10 HOUR);
