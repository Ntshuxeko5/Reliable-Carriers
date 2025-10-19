-- Fix admin user password to Superman05
USE reliable_carriers;

-- Update existing admin user or create new one
UPDATE users 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa' 
WHERE email = 'ntshuxychabalala5@gmail.com';

-- If no rows were updated, insert the user
INSERT IGNORE INTO users (first_name, last_name, email, phone, password, role, is_active) VALUES
('Ntshu', 'Chabalala', 'ntshuxychabalala5@gmail.com', '+27 79 380 3535', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ADMIN', TRUE);

-- Verify the user
SELECT id, first_name, last_name, email, phone, role, is_active 
FROM users 
WHERE email = 'ntshuxychabalala5@gmail.com';
