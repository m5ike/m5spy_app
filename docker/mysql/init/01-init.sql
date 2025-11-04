-- My Spy - MySQL Initialization Script
-- @author Michael KOJDL
-- @version 2.0.0

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS myspy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to user
GRANT ALL PRIVILEGES ON myspy_db.* TO 'myspy_user'@'%';
FLUSH PRIVILEGES;

-- Log initialization
SELECT 'Database initialized successfully' AS Status;
