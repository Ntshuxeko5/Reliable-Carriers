-- Add coordinate columns to shipments table for Google Maps geocoding
-- This migration adds pickup and delivery latitude/longitude columns
-- Run this script manually if the Java migration component doesn't work

-- For MySQL: Check and add columns one by one
-- Note: This script will fail if columns already exist - that's OK, just means they're already there

USE reliable_carriers;

-- Check if pickup_latitude exists, if not add it
SET @dbname = DATABASE();
SET @tablename = 'shipments';
SET @columnname = 'pickup_latitude';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(10, 8) NULL COMMENT ''Pickup location latitude from Google Maps geocoding''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add pickup_longitude
SET @columnname = 'pickup_longitude';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(11, 8) NULL COMMENT ''Pickup location longitude from Google Maps geocoding''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add delivery_latitude
SET @columnname = 'delivery_latitude';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(10, 8) NULL COMMENT ''Delivery location latitude from Google Maps geocoding''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add delivery_longitude
SET @columnname = 'delivery_longitude';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(11, 8) NULL COMMENT ''Delivery location longitude from Google Maps geocoding''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add indexes (will fail if they exist - that's OK)
CREATE INDEX idx_shipments_pickup_coords ON shipments(pickup_latitude, pickup_longitude);
CREATE INDEX idx_shipments_delivery_coords ON shipments(delivery_latitude, delivery_longitude);

-- Verify the columns were added
DESCRIBE shipments;
