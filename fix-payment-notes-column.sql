-- Fix payment notes column length to accommodate JSON data
USE reliable_carriers;

-- Check current column definition
DESCRIBE payments;

-- Alter the notes column to be larger (TEXT can store up to 65,535 characters)
ALTER TABLE payments MODIFY COLUMN notes TEXT;

-- Verify the change
DESCRIBE payments;
