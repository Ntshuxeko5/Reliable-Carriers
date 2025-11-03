-- Fix database schema for payment notes
USE reliable_carriers;

-- First, let's see the current structure
DESCRIBE payments;

-- Alter the notes column to be TEXT (can store up to 65,535 characters)
ALTER TABLE payments MODIFY COLUMN notes TEXT;

-- Also, let's create a separate table for storing detailed payment metadata
-- This will help avoid the notes column size issue
CREATE TABLE IF NOT EXISTS payment_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    metadata_key VARCHAR(100) NOT NULL,
    metadata_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    INDEX idx_payment_id (payment_id),
    INDEX idx_metadata_key (metadata_key)
);

-- Verify the changes
DESCRIBE payments;
DESCRIBE payment_metadata;
