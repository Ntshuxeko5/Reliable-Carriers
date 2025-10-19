-- Update payment table to make shipment_id and user_id optional
-- This allows for quote payments before shipment creation

-- Make shipment_id nullable
ALTER TABLE payments MODIFY COLUMN shipment_id BIGINT NULL;

-- Make user_id nullable  
ALTER TABLE payments MODIFY COLUMN user_id BIGINT NULL;

-- Update existing foreign key constraints if they exist
-- (This might need to be done manually depending on your database setup)
