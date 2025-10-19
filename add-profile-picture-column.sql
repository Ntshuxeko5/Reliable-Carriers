-- Add profilePicture column to users table if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture VARCHAR(255);

-- Update the column to allow NULL values
ALTER TABLE users MODIFY COLUMN profile_picture VARCHAR(255) NULL;

-- Add a comment to the column
ALTER TABLE users MODIFY COLUMN profile_picture VARCHAR(255) NULL COMMENT 'URL to profile picture';
