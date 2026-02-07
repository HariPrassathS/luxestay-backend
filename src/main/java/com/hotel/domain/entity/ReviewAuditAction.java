package com.hotel.domain.entity;

/**
 * Audit actions for review moderation
 */
public enum ReviewAuditAction {
    APPROVE,    // Admin approved the review
    REJECT,     // Admin rejected the review
    FLAG,       // Owner flagged for admin review
    EDIT,       // Admin edited review content
    DELETE,     // Admin deleted the review
    UNFLAG      // Admin removed flag
}
