-- V13: Hotel Owner System Schema Updates
-- This migration adds support for the HOTEL_OWNER role and hotel registration workflow

-- Note: HOTEL_OWNER role and hotel_id column were added in V9 directly to the database
-- This migration adds the remaining schema changes for full hotel owner functionality

-- Add password management columns to users table (using procedure for IF NOT EXISTS behavior)
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists()
BEGIN
    -- Add must_change_password to users if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'must_change_password') THEN
        ALTER TABLE users ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    
    -- Add password_changed_at to users if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'password_changed_at') THEN
        ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP NULL;
    END IF;
    
    -- Add approval_status to hotels if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hotels' AND COLUMN_NAME = 'approval_status') THEN
        ALTER TABLE hotels ADD COLUMN approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'APPROVED';
    END IF;
    
    -- Add rejection_reason to hotels if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hotels' AND COLUMN_NAME = 'rejection_reason') THEN
        ALTER TABLE hotels ADD COLUMN rejection_reason TEXT NULL;
    END IF;
    
    -- Add approved_at to hotels if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hotels' AND COLUMN_NAME = 'approved_at') THEN
        ALTER TABLE hotels ADD COLUMN approved_at TIMESTAMP NULL;
    END IF;
    
    -- Add approved_by to hotels if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hotels' AND COLUMN_NAME = 'approved_by') THEN
        ALTER TABLE hotels ADD COLUMN approved_by BIGINT NULL;
    END IF;
END //
DELIMITER ;

CALL add_column_if_not_exists();
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- Create hotel owner audit log table for tracking owner actions
CREATE TABLE IF NOT EXISTS hotel_owner_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    description VARCHAR(500) NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_audit_hotel_id (hotel_id),
    INDEX idx_audit_owner_id (owner_id),
    INDEX idx_audit_action_type (action_type),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes (ignore errors if already exist)
DROP PROCEDURE IF EXISTS create_index_if_not_exists;
DELIMITER //
CREATE PROCEDURE create_index_if_not_exists()
BEGIN
    DECLARE CONTINUE HANDLER FOR 1061 BEGIN END; -- Duplicate key name - ignore
    
    CREATE INDEX idx_users_hotel_id ON users(hotel_id);
    CREATE INDEX idx_users_role ON users(role);
    CREATE INDEX idx_hotels_approval_status ON hotels(approval_status);
END //
DELIMITER ;

CALL create_index_if_not_exists();
DROP PROCEDURE IF EXISTS create_index_if_not_exists;

-- Update existing hotels to have APPROVED status (they were created before this system)
UPDATE hotels SET approval_status = 'APPROVED' WHERE approval_status = 'APPROVED' OR approval_status IS NULL;
