-- SIMPLE VERSION: Add coordinate columns to shipments table
-- Use this version if the complex version doesn't work
-- Run each statement one at a time, skip if column already exists

USE reliable_carriers;

-- Add pickup coordinates (run these one at a time)
ALTER TABLE shipments ADD COLUMN pickup_latitude DECIMAL(10, 8) NULL;
ALTER TABLE shipments ADD COLUMN pickup_longitude DECIMAL(11, 8) NULL;

-- Add delivery coordinates (run these one at a time)
ALTER TABLE shipments ADD COLUMN delivery_latitude DECIMAL(10, 8) NULL;
ALTER TABLE shipments ADD COLUMN delivery_longitude DECIMAL(11, 8) NULL;

-- Add indexes (optional, skip if they already exist)
CREATE INDEX idx_shipments_pickup_coords ON shipments(pickup_latitude, pickup_longitude);
CREATE INDEX idx_shipments_delivery_coords ON shipments(delivery_latitude, delivery_longitude);

-- Verify
DESCRIBE shipments;

