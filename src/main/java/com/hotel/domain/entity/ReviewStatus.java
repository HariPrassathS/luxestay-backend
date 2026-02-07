package com.hotel.domain.entity;

/**
 * Status of a review in the moderation workflow
 */
public enum ReviewStatus {
    PENDING,    // Awaiting admin approval
    APPROVED,   // Approved and visible to public
    REJECTED,   // Rejected by admin
    FLAGGED     // Flagged by hotel owner for admin review
}
