package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Loyalty Profile entity storing loyalty status and lifetime stats.
 */
@Entity
@Table(name = "user_loyalty_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoyaltyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_xp", nullable = false)
    @lombok.Builder.Default
    private Integer currentXp = 0;

    @Column(name = "lifetime_xp", nullable = false)
    @lombok.Builder.Default
    private Integer lifetimeXp = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_level_id", nullable = false)
    private LoyaltyLevel currentLevel;

    @Column(name = "total_bookings", nullable = false)
    @lombok.Builder.Default
    private Integer totalBookings = 0;

    @Column(name = "total_nights", nullable = false)
    @lombok.Builder.Default
    private Integer totalNights = 0;

    @Column(name = "total_spend", nullable = false, precision = 12, scale = 2)
    @lombok.Builder.Default
    private BigDecimal totalSpend = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @lombok.Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_referrals", nullable = false)
    @lombok.Builder.Default
    private Integer totalReferrals = 0;

    @Column(name = "current_streak", nullable = false)
    @lombok.Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @lombok.Builder.Default
    private Integer longestStreak = 0;

    @Column(name = "last_booking_date")
    private LocalDate lastBookingDate;

    @Column(name = "member_since", nullable = false, updatable = false)
    private LocalDateTime memberSince;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        memberSince = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add XP and return whether a level up occurred.
     */
    public boolean addXp(int xpAmount) {
        int oldXp = this.currentXp;
        this.currentXp += xpAmount;
        this.lifetimeXp += Math.max(0, xpAmount); // Only add positive to lifetime
        return this.currentXp > oldXp && !currentLevel.containsXp(this.currentXp);
    }

    /**
     * Spend XP on rewards.
     */
    public boolean spendXp(int xpAmount) {
        if (xpAmount > currentXp) return false;
        this.currentXp -= xpAmount;
        return true;
    }

    /**
     * Get progress percentage to next level.
     */
    public int getProgressToNextLevel() {
        return currentLevel.getProgressPercentage(currentXp);
    }

    /**
     * Get XP needed for next level.
     */
    public int getXpToNextLevel() {
        return currentLevel.getXpToNextLevel(currentXp);
    }

    /**
     * Increment booking stats.
     */
    public void recordBooking(int nights, BigDecimal amount, LocalDate bookingDate) {
        this.totalBookings++;
        this.totalNights += nights;
        this.totalSpend = this.totalSpend.add(amount);
        this.lastBookingDate = bookingDate;
    }
}
