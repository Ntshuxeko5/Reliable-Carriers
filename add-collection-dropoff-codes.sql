-- Add collection and drop-off code columns to shipments table
-- This script adds the new fields for collection and drop-off codes

-- Add collection code column
ALTER TABLE shipments ADD COLUMN collection_code VARCHAR(10) UNIQUE;

-- Add drop-off code column  
ALTER TABLE shipments ADD COLUMN drop_off_code VARCHAR(10) UNIQUE;

-- Add indexes for better performance
CREATE INDEX idx_shipments_collection_code ON shipments(collection_code);
CREATE INDEX idx_shipments_drop_off_code ON shipments(drop_off_code);

-- Update existing shipments with generated codes (optional - for existing data)
-- UPDATE shipments SET collection_code = CONCAT('COL', LPAD(FLOOR(RAND() * 100000), 5, '0')) WHERE collection_code IS NULL;
-- UPDATE shipments SET drop_off_code = CONCAT('DRO', LPAD(FLOOR(RAND() * 100000), 5, '0')) WHERE drop_off_code IS NULL;

COMMIT;
