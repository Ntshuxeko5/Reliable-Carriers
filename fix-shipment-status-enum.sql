-- Fix shipment status enum to include ASSIGNED status
-- This script updates the database schema to match the Java enum

USE reliable_carriers;

-- Update the shipments table status column to include ASSIGNED
ALTER TABLE shipments 
MODIFY COLUMN status ENUM('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED_DELIVERY', 'CANCELLED') 
NOT NULL DEFAULT 'PENDING';

-- Update the shipment_tracking table status column to include ASSIGNED
ALTER TABLE shipment_tracking 
MODIFY COLUMN status ENUM('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED_DELIVERY', 'CANCELLED') 
NOT NULL;

-- Verify the changes
DESCRIBE shipments;
DESCRIBE shipment_tracking;
