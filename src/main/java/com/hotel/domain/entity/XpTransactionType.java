package com.hotel.domain.entity;

/**
 * Types of XP transactions for audit logging.
 */
public enum XpTransactionType {
    BOOKING_COMPLETED,      // XP from completed stays
    BOOKING_VALUE_BONUS,    // Bonus XP for high-value bookings
    STAY_DURATION_BONUS,    // Bonus for longer stays
    REVIEW_SUBMITTED,       // XP for leaving reviews
    REFERRAL_SIGNUP,        // Referral joined
    REFERRAL_FIRST_BOOKING, // Referral completed first booking
    STREAK_BONUS,           // Monthly booking streak bonus
    LEVEL_UP_BONUS,         // Bonus XP on level advancement
    BADGE_UNLOCKED,         // XP reward for unlocking a badge
    WELCOME_BONUS,          // New member welcome XP
    BIRTHDAY_BONUS,         // Birthday reward
    SEASONAL_PROMOTION,     // Special promotional XP
    XP_REDEMPTION,          // XP spent on rewards (negative)
    ADMIN_ADJUSTMENT        // Manual admin correction
}
