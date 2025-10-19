-- Add admin user with specified credentials
-- Email: ntshuxychabalala5@gmail.com
-- Phone: +27 79 380 3535
-- Password: admin123 (hashed with BCrypt)

USE reliable_carriers;

-- Insert admin user with specified credentials
INSERT INTO users (first_name, last_name, email, phone, password, role, is_active) VALUES
('Ntshu', 'Chabalala', 'ntshuxychabalala5@gmail.com', '+27 79 380 3535', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name),
last_name = VALUES(last_name),
phone = VALUES(phone),
password = VALUES(password),
role = VALUES(role),
is_active = VALUES(is_active);

-- Verify the user was created
SELECT id, first_name, last_name, email, phone, role, is_active, created_at 
FROM users 
WHERE email = 'ntshuxychabalala5@gmail.com';
