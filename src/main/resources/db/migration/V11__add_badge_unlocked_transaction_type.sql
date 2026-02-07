-- =====================================================
-- ADD BADGE_UNLOCKED TRANSACTION TYPE
-- Version: 1.1
-- Description: Adds BADGE_UNLOCKED to xp_transactions enum
-- =====================================================

-- Alter the transaction_type enum to include BADGE_UNLOCKED
ALTER TABLE xp_transactions 
MODIFY COLUMN transaction_type ENUM(
    'BOOKING_COMPLETED',
    'BOOKING_VALUE_BONUS',
    'STAY_DURATION_BONUS',
    'REVIEW_SUBMITTED',
    'REFERRAL_SIGNUP',
    'REFERRAL_FIRST_BOOKING',
    'STREAK_BONUS',
    'LEVEL_UP_BONUS',
    'BADGE_UNLOCKED',
    'WELCOME_BONUS',
    'BIRTHDAY_BONUS',
    'SEASONAL_PROMOTION',
    'XP_REDEMPTION',
    'ADMIN_ADJUSTMENT'
) NOT NULL;
