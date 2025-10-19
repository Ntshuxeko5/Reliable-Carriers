-- Update admin user password to Superman05
-- Using BCrypt hash for password: Superman05

USE reliable_carriers;

-- Update the admin user password with a known working BCrypt hash
UPDATE users 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa' 
WHERE email = 'ntshuxychabalala5@gmail.com';

-- If the user doesn't exist, create them
INSERT INTO users (first_name, last_name, email, phone, password, role, is_active) VALUES
('Ntshu', 'Chabalala', 'ntshuxychabalala5@gmail.com', '+27 79 380 3535', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE
password = VALUES(password),
first_name = VALUES(first_name),
last_name = VALUES(last_name),
phone = VALUES(phone),
role = VALUES(role),
is_active = VALUES(is_active);

-- Verify the user was updated/created
SELECT id, first_name, last_name, email, phone, role, is_active, created_at 
FROM users 
WHERE email = 'ntshuxychabalala5@gmail.com';
