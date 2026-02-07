package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Reward Redemption entity tracking redeemed rewards.
 */
@Entity
@Table(name = "reward_redemptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reward_id", nullable = false)
    private LoyaltyReward reward;

    @Column(name = "redemption_code", nullable = false, unique = true, length = 30)
    private String redemptionCode;

    @Column(name = "xp_spent", nullable = false)
    private Integer xpSpent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private RedemptionStatus status = RedemptionStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_on_booking_id")
    private Booking usedOnBooking;

    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        redeemedAt = LocalDateTime.now();
    }

    /**
     * Check if redemption is still valid.
     */
    public boolean isValid() {
        if (status != RedemptionStatus.ACTIVE) return false;
        return LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Mark as used on a booking.
     */
    public void markUsed(Booking booking) {
        this.status = RedemptionStatus.USED;
        this.usedOnBooking = booking;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Mark as expired.
     */
    public void markExpired() {
        this.status = RedemptionStatus.EXPIRED;
    }

    /**
     * Cancel the redemption.
     */
    public void cancel() {
        this.status = RedemptionStatus.CANCELLED;
    }
}
