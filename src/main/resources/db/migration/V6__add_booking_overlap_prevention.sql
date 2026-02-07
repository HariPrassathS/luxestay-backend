-- =====================================================
-- BOOKING OVERLAP PREVENTION
-- Version: 6
-- Description: Add constraint and trigger to prevent double bookings
-- This is a database-level safety net in addition to application locking
-- =====================================================

-- Add index for faster overlap queries
CREATE INDEX idx_bookings_room_dates_status 
    ON bookings(room_id, check_in_date, check_out_date, status);

-- Add index for user booking lookups
CREATE INDEX idx_bookings_user_status 
    ON bookings(user_id, status, created_at);

-- Create trigger to prevent overlapping bookings at database level
-- This serves as a last line of defense if application-level locking fails

DELIMITER //

CREATE TRIGGER prevent_double_booking_insert
BEFORE INSERT ON bookings
FOR EACH ROW
BEGIN
    DECLARE overlap_count INT;
    
    -- Only check if the new booking is not cancelled
    IF NEW.status NOT IN ('CANCELLED', 'CHECKED_OUT') THEN
        -- Check for overlapping confirmed/pending bookings
        SELECT COUNT(*) INTO overlap_count
        FROM bookings
        WHERE room_id = NEW.room_id
          AND status NOT IN ('CANCELLED', 'CHECKED_OUT')
          AND check_in_date < NEW.check_out_date
          AND check_out_date > NEW.check_in_date;
        
        IF overlap_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'DOUBLE_BOOKING_PREVENTED: Room is already booked for the selected dates';
        END IF;
    END IF;
END//

-- Also prevent updates that would create overlaps
CREATE TRIGGER prevent_double_booking_update
BEFORE UPDATE ON bookings
FOR EACH ROW
BEGIN
    DECLARE overlap_count INT;
    
    -- Only check if status or dates are changing and new status is active
    IF (NEW.status NOT IN ('CANCELLED', 'CHECKED_OUT')) AND 
       (OLD.check_in_date != NEW.check_in_date OR 
        OLD.check_out_date != NEW.check_out_date OR
        OLD.room_id != NEW.room_id OR
        OLD.status IN ('CANCELLED', 'CHECKED_OUT')) THEN
        
        SELECT COUNT(*) INTO overlap_count
        FROM bookings
        WHERE room_id = NEW.room_id
          AND id != NEW.id
          AND status NOT IN ('CANCELLED', 'CHECKED_OUT')
          AND check_in_date < NEW.check_out_date
          AND check_out_date > NEW.check_in_date;
        
        IF overlap_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'DOUBLE_BOOKING_PREVENTED: Room is already booked for the selected dates';
        END IF;
    END IF;
END//

DELIMITER ;
