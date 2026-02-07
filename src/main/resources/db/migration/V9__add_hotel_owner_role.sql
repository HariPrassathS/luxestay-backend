-- V9: Add hotel owner role and hotel_id column
-- This migration adds HOTEL_OWNER to the role enum and hotel_id to users table

-- Add hotel_id column to users if it doesn't exist
DROP PROCEDURE IF EXISTS add_hotel_owner_columns;
DELIMITER //
CREATE PROCEDURE add_hotel_owner_columns()
BEGIN
    -- Add hotel_id to users if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'hotel_id') THEN
        ALTER TABLE users ADD COLUMN hotel_id BIGINT NULL;
    END IF;
END //
DELIMITER ;

CALL add_hotel_owner_columns();
DROP PROCEDURE IF EXISTS add_hotel_owner_columns;

-- Note: ENUM modification handled by JPA/Hibernate
