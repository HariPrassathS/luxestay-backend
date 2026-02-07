-- =====================================================
-- LUXESTAY GAMIFIED LOYALTY PROGRAM SCHEMA
-- Version: 1.0
-- Description: Complete loyalty system with XP, levels, badges, and rewards
-- =====================================================

-- ==================== LOYALTY LEVELS TABLE ====================
-- Defines the tier structure with requirements and benefits
CREATE TABLE loyalty_levels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_number INT NOT NULL UNIQUE,
    level_name VARCHAR(50) NOT NULL UNIQUE,
    min_xp INT NOT NULL DEFAULT 0,
    max_xp INT,  -- NULL for highest tier (unlimited)
    discount_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    free_breakfast BOOLEAN NOT NULL DEFAULT FALSE,
    room_upgrade_priority INT NOT NULL DEFAULT 0,  -- 0=none, 1=low, 2=medium, 3=high
    late_checkout_hours INT NOT NULL DEFAULT 0,
    early_checkin_hours INT NOT NULL DEFAULT 0,
    welcome_bonus_xp INT NOT NULL DEFAULT 0,
    badge_icon VARCHAR(50) NOT NULL DEFAULT 'fa-medal',
    badge_color VARCHAR(20) NOT NULL DEFAULT '#CD7F32',  -- Bronze default
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_loyalty_levels_xp (min_xp, max_xp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== USER LOYALTY PROFILES ====================
-- Stores each user's loyalty status and lifetime stats
CREATE TABLE user_loyalty_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    current_xp INT NOT NULL DEFAULT 0,
    lifetime_xp INT NOT NULL DEFAULT 0,
    current_level_id BIGINT NOT NULL,
    total_bookings INT NOT NULL DEFAULT 0,
    total_nights INT NOT NULL DEFAULT 0,
    total_spend DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_reviews INT NOT NULL DEFAULT 0,
    total_referrals INT NOT NULL DEFAULT 0,
    current_streak INT NOT NULL DEFAULT 0,  -- Consecutive months with bookings
    longest_streak INT NOT NULL DEFAULT 0,
    last_booking_date DATE,
    member_since TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_loyalty_profile_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_loyalty_profile_level 
        FOREIGN KEY (current_level_id) REFERENCES loyalty_levels(id),
    INDEX idx_loyalty_profile_user (user_id),
    INDEX idx_loyalty_profile_level (current_level_id),
    INDEX idx_loyalty_profile_xp (current_xp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== XP TRANSACTIONS ====================
-- Auditable log of all XP earned/spent with source verification
CREATE TABLE xp_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    xp_amount INT NOT NULL,  -- Positive for earned, negative for spent
    transaction_type ENUM(
        'BOOKING_COMPLETED',      -- XP from completed stays
        'BOOKING_VALUE_BONUS',    -- Bonus XP for high-value bookings
        'STAY_DURATION_BONUS',    -- Bonus for longer stays
        'REVIEW_SUBMITTED',       -- XP for leaving reviews
        'REFERRAL_SIGNUP',        -- Referral joined
        'REFERRAL_FIRST_BOOKING', -- Referral completed first booking
        'STREAK_BONUS',           -- Monthly booking streak bonus
        'LEVEL_UP_BONUS',         -- Bonus XP on level advancement
        'WELCOME_BONUS',          -- New member welcome XP
        'BIRTHDAY_BONUS',         -- Birthday reward
        'SEASONAL_PROMOTION',     -- Special promotional XP
        'XP_REDEMPTION',          -- XP spent on rewards (negative)
        'ADMIN_ADJUSTMENT'        -- Manual admin correction
    ) NOT NULL,
    source_type ENUM('BOOKING', 'REVIEW', 'REFERRAL', 'SYSTEM', 'ADMIN') NOT NULL,
    source_id BIGINT,  -- ID of booking/review/referral that triggered this
    description VARCHAR(255) NOT NULL,
    xp_before INT NOT NULL,
    xp_after INT NOT NULL,
    level_before_id BIGINT,
    level_after_id BIGINT,
    is_level_up BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_xp_trans_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_xp_trans_level_before 
        FOREIGN KEY (level_before_id) REFERENCES loyalty_levels(id),
    CONSTRAINT fk_xp_trans_level_after 
        FOREIGN KEY (level_after_id) REFERENCES loyalty_levels(id),
    INDEX idx_xp_trans_user (user_id),
    INDEX idx_xp_trans_type (transaction_type),
    INDEX idx_xp_trans_source (source_type, source_id),
    INDEX idx_xp_trans_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== BADGE DEFINITIONS ====================
-- All achievable badges with unlock criteria
CREATE TABLE badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge_code VARCHAR(50) NOT NULL UNIQUE,
    badge_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    icon VARCHAR(50) NOT NULL DEFAULT 'fa-award',
    color VARCHAR(20) NOT NULL DEFAULT '#FFD700',
    category ENUM('BOOKING', 'LOYALTY', 'REVIEW', 'REFERRAL', 'SPECIAL', 'MILESTONE') NOT NULL,
    rarity ENUM('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY') NOT NULL DEFAULT 'COMMON',
    xp_reward INT NOT NULL DEFAULT 0,  -- XP awarded when badge is unlocked
    unlock_criteria JSON NOT NULL,  -- Structured criteria like {"bookings_count": 10}
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_badges_category (category),
    INDEX idx_badges_rarity (rarity),
    INDEX idx_badges_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== USER BADGES ====================
-- Badges earned by users with unlock timestamp
CREATE TABLE user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source_booking_id BIGINT,  -- The booking that triggered this badge (if applicable)
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,  -- User can feature up to 3 badges
    
    CONSTRAINT fk_user_badges_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_badges_badge 
        FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_badges_booking 
        FOREIGN KEY (source_booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    CONSTRAINT uk_user_badge UNIQUE (user_id, badge_id),
    INDEX idx_user_badges_user (user_id),
    INDEX idx_user_badges_badge (badge_id),
    INDEX idx_user_badges_unlocked (unlocked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== REFERRAL TRACKING ====================
-- Tracks referral relationships and rewards
CREATE TABLE referrals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referrer_user_id BIGINT NOT NULL,
    referred_user_id BIGINT NOT NULL UNIQUE,  -- A user can only be referred once
    referral_code VARCHAR(20) NOT NULL,
    status ENUM('PENDING', 'REGISTERED', 'FIRST_BOOKING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    referrer_xp_earned INT NOT NULL DEFAULT 0,
    referred_xp_earned INT NOT NULL DEFAULT 0,
    first_booking_id BIGINT,
    registered_at TIMESTAMP,
    first_booking_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_referrals_referrer 
        FOREIGN KEY (referrer_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_referrals_referred 
        FOREIGN KEY (referred_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_referrals_booking 
        FOREIGN KEY (first_booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_referrals_referrer (referrer_user_id),
    INDEX idx_referrals_referred (referred_user_id),
    INDEX idx_referrals_code (referral_code),
    INDEX idx_referrals_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== LOYALTY REWARDS CATALOG ====================
-- Redeemable rewards that users can spend XP on
CREATE TABLE loyalty_rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reward_code VARCHAR(50) NOT NULL UNIQUE,
    reward_name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    reward_type ENUM('DISCOUNT', 'FREE_NIGHT', 'UPGRADE', 'AMENITY', 'EXPERIENCE') NOT NULL,
    xp_cost INT NOT NULL,
    discount_percentage DECIMAL(5, 2),  -- For DISCOUNT type
    discount_max_amount DECIMAL(10, 2),  -- Max discount cap
    min_level_required BIGINT,  -- Minimum loyalty level to redeem
    valid_days INT NOT NULL DEFAULT 30,  -- Days until reward expires after redemption
    max_redemptions_per_user INT,  -- NULL = unlimited
    total_available INT,  -- NULL = unlimited inventory
    total_redeemed INT NOT NULL DEFAULT 0,
    icon VARCHAR(50) NOT NULL DEFAULT 'fa-gift',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rewards_min_level 
        FOREIGN KEY (min_level_required) REFERENCES loyalty_levels(id),
    INDEX idx_rewards_type (reward_type),
    INDEX idx_rewards_active (is_active),
    INDEX idx_rewards_cost (xp_cost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== USER REWARD REDEMPTIONS ====================
-- Tracks rewards redeemed by users
CREATE TABLE reward_redemptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    redemption_code VARCHAR(30) NOT NULL UNIQUE,
    xp_spent INT NOT NULL,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    used_on_booking_id BIGINT,
    redeemed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    
    CONSTRAINT fk_redemptions_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_redemptions_reward 
        FOREIGN KEY (reward_id) REFERENCES loyalty_rewards(id) ON DELETE RESTRICT,
    CONSTRAINT fk_redemptions_booking 
        FOREIGN KEY (used_on_booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_redemptions_user (user_id),
    INDEX idx_redemptions_status (status),
    INDEX idx_redemptions_code (redemption_code),
    INDEX idx_redemptions_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== ADD REFERRAL CODE TO USERS ====================
ALTER TABLE users ADD COLUMN referral_code VARCHAR(20) UNIQUE AFTER phone;
ALTER TABLE users ADD COLUMN referred_by_code VARCHAR(20) AFTER referral_code;

-- =====================================================
-- SEED DATA: LOYALTY LEVELS
-- =====================================================
INSERT INTO loyalty_levels (level_number, level_name, min_xp, max_xp, discount_percentage, free_breakfast, room_upgrade_priority, late_checkout_hours, early_checkin_hours, welcome_bonus_xp, badge_icon, badge_color, description) VALUES
(1, 'Explorer', 0, 999, 0.00, FALSE, 0, 0, 0, 100, 'fa-compass', '#CD7F32', 'Welcome to LuxeStay! Start your journey and earn XP with every booking.'),
(2, 'Adventurer', 1000, 4999, 3.00, FALSE, 1, 1, 0, 150, 'fa-hiking', '#C0C0C0', 'You''re on your way! Enjoy 3% off all bookings and priority room upgrades.'),
(3, 'Voyager', 5000, 14999, 5.00, FALSE, 2, 2, 1, 200, 'fa-globe-americas', '#FFD700', 'A seasoned traveler! Get 5% off, 2-hour late checkout, and 1-hour early check-in.'),
(4, 'Elite', 15000, 39999, 8.00, TRUE, 2, 3, 2, 300, 'fa-crown', '#E5E4E2', 'Elite status unlocked! Enjoy 8% off, complimentary breakfast, and flexible timings.'),
(5, 'Ambassador', 40000, NULL, 12.00, TRUE, 3, 4, 3, 500, 'fa-gem', '#B9F2FF', 'Our most valued guest! Maximum benefits including 12% off and highest upgrade priority.');

-- =====================================================
-- SEED DATA: BADGES
-- =====================================================
INSERT INTO badges (badge_code, badge_name, description, icon, color, category, rarity, xp_reward, unlock_criteria, sort_order) VALUES
-- Booking Milestones
('FIRST_BOOKING', 'First Step', 'Complete your first booking', 'fa-door-open', '#4CAF50', 'BOOKING', 'COMMON', 50, '{"bookings_completed": 1}', 1),
('BOOKINGS_5', 'Regular Guest', 'Complete 5 bookings', 'fa-suitcase', '#2196F3', 'BOOKING', 'COMMON', 100, '{"bookings_completed": 5}', 2),
('BOOKINGS_10', 'Frequent Traveler', 'Complete 10 bookings', 'fa-plane-departure', '#9C27B0', 'BOOKING', 'UNCOMMON', 200, '{"bookings_completed": 10}', 3),
('BOOKINGS_25', 'Road Warrior', 'Complete 25 bookings', 'fa-road', '#FF9800', 'BOOKING', 'RARE', 500, '{"bookings_completed": 25}', 4),
('BOOKINGS_50', 'Globe Trotter', 'Complete 50 bookings', 'fa-earth-americas', '#F44336', 'BOOKING', 'EPIC', 1000, '{"bookings_completed": 50}', 5),
('BOOKINGS_100', 'Legendary Traveler', 'Complete 100 bookings', 'fa-trophy', '#FFD700', 'BOOKING', 'LEGENDARY', 2500, '{"bookings_completed": 100}', 6),

-- Stay Duration
('NIGHTS_10', 'Night Owl', 'Stay 10 nights total', 'fa-moon', '#3F51B5', 'MILESTONE', 'COMMON', 75, '{"total_nights": 10}', 10),
('NIGHTS_30', 'Extended Stay', 'Stay 30 nights total', 'fa-bed', '#00BCD4', 'MILESTONE', 'UNCOMMON', 200, '{"total_nights": 30}', 11),
('NIGHTS_100', 'Hotel Resident', 'Stay 100 nights total', 'fa-house-user', '#E91E63', 'MILESTONE', 'RARE', 500, '{"total_nights": 100}', 12),
('NIGHTS_365', 'Year-Round Guest', 'Stay 365 nights total', 'fa-calendar-check', '#FFD700', 'MILESTONE', 'LEGENDARY', 2000, '{"total_nights": 365}', 13),

-- Spending Milestones (in INR)
('SPEND_50K', 'Silver Spender', 'Spend ₹50,000 lifetime', 'fa-wallet', '#C0C0C0', 'MILESTONE', 'COMMON', 100, '{"total_spend": 50000}', 20),
('SPEND_200K', 'Gold Spender', 'Spend ₹2,00,000 lifetime', 'fa-coins', '#FFD700', 'MILESTONE', 'UNCOMMON', 300, '{"total_spend": 200000}', 21),
('SPEND_500K', 'Platinum Spender', 'Spend ₹5,00,000 lifetime', 'fa-gem', '#E5E4E2', 'MILESTONE', 'RARE', 750, '{"total_spend": 500000}', 22),
('SPEND_1M', 'Diamond Spender', 'Spend ₹10,00,000 lifetime', 'fa-diamond', '#B9F2FF', 'MILESTONE', 'LEGENDARY', 2000, '{"total_spend": 1000000}', 23),

-- Review Badges
('FIRST_REVIEW', 'Critic', 'Write your first review', 'fa-comment', '#8BC34A', 'REVIEW', 'COMMON', 50, '{"reviews_count": 1}', 30),
('REVIEWS_5', 'Reviewer', 'Write 5 reviews', 'fa-comments', '#009688', 'REVIEW', 'UNCOMMON', 150, '{"reviews_count": 5}', 31),
('REVIEWS_20', 'Top Reviewer', 'Write 20 reviews', 'fa-star-half-alt', '#FF5722', 'REVIEW', 'RARE', 400, '{"reviews_count": 20}', 32),

-- Referral Badges
('FIRST_REFERRAL', 'Connector', 'Refer your first friend', 'fa-user-plus', '#00BCD4', 'REFERRAL', 'COMMON', 100, '{"referrals_completed": 1}', 40),
('REFERRALS_5', 'Influencer', 'Refer 5 friends', 'fa-users', '#673AB7', 'REFERRAL', 'UNCOMMON', 300, '{"referrals_completed": 5}', 41),
('REFERRALS_10', 'Ambassador', 'Refer 10 friends', 'fa-bullhorn', '#E91E63', 'REFERRAL', 'RARE', 750, '{"referrals_completed": 10}', 42),

-- Streak Badges
('STREAK_3', 'Consistent Traveler', 'Book 3 months in a row', 'fa-fire', '#FF5722', 'LOYALTY', 'UNCOMMON', 200, '{"streak_months": 3}', 50),
('STREAK_6', 'Dedicated Guest', 'Book 6 months in a row', 'fa-fire-alt', '#F44336', 'LOYALTY', 'RARE', 500, '{"streak_months": 6}', 51),
('STREAK_12', 'Yearly Devotee', 'Book 12 months in a row', 'fa-award', '#FFD700', 'LOYALTY', 'LEGENDARY', 1500, '{"streak_months": 12}', 52),

-- Special Badges
('LUXURY_LOVER', 'Luxury Lover', 'Stay at 5 different 5-star hotels', 'fa-star', '#FFD700', 'SPECIAL', 'RARE', 400, '{"five_star_hotels": 5}', 60),
('CITY_EXPLORER', 'City Explorer', 'Stay in 5 different cities', 'fa-city', '#607D8B', 'SPECIAL', 'UNCOMMON', 250, '{"unique_cities": 5}', 61),
('SUITE_LIFE', 'Suite Life', 'Book 3 suite rooms', 'fa-concierge-bell', '#9C27B0', 'SPECIAL', 'RARE', 350, '{"suite_bookings": 3}', 62),
('WEEKEND_WARRIOR', 'Weekend Warrior', 'Complete 10 weekend stays', 'fa-umbrella-beach', '#FF9800', 'SPECIAL', 'UNCOMMON', 200, '{"weekend_stays": 10}', 63);

-- =====================================================
-- SEED DATA: LOYALTY REWARDS
-- =====================================================
INSERT INTO loyalty_rewards (reward_code, reward_name, description, reward_type, xp_cost, discount_percentage, discount_max_amount, min_level_required, valid_days, icon) VALUES
('DISCOUNT_5', '5% Off Next Booking', 'Get 5% discount on your next booking (max ₹2,000)', 'DISCOUNT', 500, 5.00, 2000.00, 1, 30, 'fa-percent'),
('DISCOUNT_10', '10% Off Next Booking', 'Get 10% discount on your next booking (max ₹5,000)', 'DISCOUNT', 1000, 10.00, 5000.00, 2, 30, 'fa-percent'),
('DISCOUNT_15', '15% Off Next Booking', 'Get 15% discount on your next booking (max ₹10,000)', 'DISCOUNT', 2000, 15.00, 10000.00, 3, 30, 'fa-percent'),
('FREE_BREAKFAST', 'Complimentary Breakfast', 'Enjoy free breakfast during your stay', 'AMENITY', 300, NULL, NULL, 1, 60, 'fa-coffee'),
('ROOM_UPGRADE', 'Room Upgrade', 'Get upgraded to the next room category (subject to availability)', 'UPGRADE', 750, NULL, NULL, 2, 45, 'fa-arrow-up'),
('LATE_CHECKOUT', 'Late Checkout (4 hours)', 'Extend your checkout by 4 hours', 'AMENITY', 400, NULL, NULL, 1, 60, 'fa-clock'),
('SPA_DISCOUNT', '20% Spa Discount', 'Get 20% off on spa services', 'AMENITY', 600, 20.00, 3000.00, 2, 30, 'fa-spa'),
('FREE_NIGHT_BASIC', 'Free Night (Basic Room)', 'One free night in a basic room (up to ₹5,000 value)', 'FREE_NIGHT', 5000, NULL, 5000.00, 3, 90, 'fa-bed'),
('FREE_NIGHT_DELUXE', 'Free Night (Deluxe Room)', 'One free night in a deluxe room (up to ₹10,000 value)', 'FREE_NIGHT', 10000, NULL, 10000.00, 4, 90, 'fa-star'),
('VIP_EXPERIENCE', 'VIP Airport Transfer', 'Complimentary luxury airport pickup', 'EXPERIENCE', 3000, NULL, NULL, 4, 60, 'fa-car');
