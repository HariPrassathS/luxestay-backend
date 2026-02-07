-- V17: Login Tracking and Email Notification System
-- This migration adds support for first-time login detection, login auditing, and email notifications
-- SAFE: All additions, no modifications to existing data

-- ==================== User Table Extensions ====================

-- Add login tracking columns using procedure for IF NOT EXISTS behavior
DROP PROCEDURE IF EXISTS add_login_tracking_columns;
DELIMITER //
CREATE PROCEDURE add_login_tracking_columns()
BEGIN
    -- Add last_login_at to track last successful login
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'last_login_at') THEN
        ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL;
    END IF;
    
    -- Add login_count to track total logins
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'login_count') THEN
        ALTER TABLE users ADD COLUMN login_count INT NOT NULL DEFAULT 0;
    END IF;
    
    -- Add failed_login_attempts for rate limiting
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'failed_login_attempts') THEN
        ALTER TABLE users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
    END IF;
    
    -- Add locked_until for temporary account lockout
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'locked_until') THEN
        ALTER TABLE users ADD COLUMN locked_until TIMESTAMP NULL;
    END IF;
    
    -- Add first_login_completed to distinguish first-time users
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'first_login_completed') THEN
        ALTER TABLE users ADD COLUMN first_login_completed BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END //
DELIMITER ;

CALL add_login_tracking_columns();
DROP PROCEDURE IF EXISTS add_login_tracking_columns;

-- Mark existing users as having completed first login (they already have passwords)
UPDATE users SET first_login_completed = TRUE WHERE password_hash IS NOT NULL AND first_login_completed = FALSE;

-- ==================== Login Audit Log Table ====================

CREATE TABLE IF NOT EXISTS login_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    email VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL COMMENT 'LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, PASSWORD_CHANGED, FIRST_LOGIN',
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    is_first_login BOOLEAN DEFAULT FALSE,
    is_successful BOOLEAN DEFAULT FALSE,
    failure_reason VARCHAR(500) NULL,
    additional_info TEXT NULL COMMENT 'JSON with extra context',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_login_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_login_audit_user_id (user_id),
    INDEX idx_login_audit_email (email),
    INDEX idx_login_audit_action (action),
    INDEX idx_login_audit_created_at (created_at),
    INDEX idx_login_audit_ip (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== Email Log Table ====================

CREATE TABLE IF NOT EXISTS email_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(200) NULL,
    email_type VARCHAR(50) NOT NULL COMMENT 'FIRST_LOGIN, PASSWORD_CHANGED, LOGIN_NOTIFICATION, BOOKING_CONFIRMATION',
    subject VARCHAR(255) NOT NULL,
    template_name VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SENT, FAILED',
    error_message TEXT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    
    INDEX idx_email_log_recipient (recipient_email),
    INDEX idx_email_log_type (email_type),
    INDEX idx_email_log_status (status),
    INDEX idx_email_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== Rate Limiting Table (IP-based) ====================

CREATE TABLE IF NOT EXISTS rate_limit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL COMMENT 'IP address or email',
    identifier_type VARCHAR(20) NOT NULL COMMENT 'IP, EMAIL',
    action_type VARCHAR(50) NOT NULL COMMENT 'LOGIN_ATTEMPT',
    attempt_count INT NOT NULL DEFAULT 1,
    window_start TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked_until TIMESTAMP NULL,
    
    UNIQUE KEY uk_rate_limit_identifier (identifier, identifier_type, action_type),
    INDEX idx_rate_limit_window (window_start),
    INDEX idx_rate_limit_blocked (blocked_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
