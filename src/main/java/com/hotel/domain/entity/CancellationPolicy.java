package com.hotel.domain.entity;

/**
 * CancellationPolicy enum - Defines clear, understandable cancellation tiers.
 * 
 * Design Philosophy:
 * - Simple, memorable tier names
 * - No hidden rules
 * - Human-readable explanations
 * - Fair to both guests and hotels
 */
public enum CancellationPolicy {
    
    /**
     * FLEXIBLE: Best for guests who need flexibility
     * - Free cancellation up to 24 hours before check-in
     * - Full refund within 3-5 business days
     */
    FLEXIBLE("Flexible", 
             "Free cancellation up to 24 hours before check-in",
             24, 100, 3, 5),
    
    /**
     * MODERATE: Balanced policy for most travelers
     * - Free cancellation up to 5 days before check-in
     * - 50% refund if cancelled 1-5 days before
     * - No refund within 24 hours
     */
    MODERATE("Moderate", 
              "Free cancellation up to 5 days before check-in",
              120, 100, 5, 7),
    
    /**
     * STRICT: For best rates with commitment
     * - Free cancellation up to 7 days before check-in
     * - 50% refund if cancelled 3-7 days before
     * - No refund within 3 days
     */
    STRICT("Strict", 
           "Free cancellation up to 7 days before check-in",
           168, 100, 7, 10);
    
    private final String displayName;
    private final String shortDescription;
    private final int freeHoursBefore;        // Hours before check-in for 100% refund
    private final int fullRefundPercent;       // Refund percentage for free period
    private final int refundDaysMin;           // Minimum days for refund processing
    private final int refundDaysMax;           // Maximum days for refund processing
    
    CancellationPolicy(String displayName, String shortDescription, 
                       int freeHoursBefore, int fullRefundPercent,
                       int refundDaysMin, int refundDaysMax) {
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.freeHoursBefore = freeHoursBefore;
        this.fullRefundPercent = fullRefundPercent;
        this.refundDaysMin = refundDaysMin;
        this.refundDaysMax = refundDaysMax;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public int getFreeHoursBefore() {
        return freeHoursBefore;
    }
    
    public int getFullRefundPercent() {
        return fullRefundPercent;
    }
    
    public int getRefundDaysMin() {
        return refundDaysMin;
    }
    
    public int getRefundDaysMax() {
        return refundDaysMax;
    }
    
    /**
     * Get free cancellation cutoff in days (rounded)
     */
    public int getFreeDaysBefore() {
        return freeHoursBefore / 24;
    }
}
