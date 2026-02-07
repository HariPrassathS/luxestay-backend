-- V9: Add hotel owner role
-- This migration adds HOTEL_OWNER to the role enum in users table
-- Note: This migration was already applied to the database

-- The ALTER TABLE to modify the role enum was executed previously
-- ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'ADMIN', 'HOTEL_OWNER') NOT NULL DEFAULT 'USER';

-- Also added hotel_id foreign key for hotel owners
-- ALTER TABLE users ADD COLUMN hotel_id BIGINT NULL;
-- ALTER TABLE users ADD CONSTRAINT fk_users_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE SET NULL;

-- This file exists as a placeholder to match the flyway_schema_history record
SELECT 1;
