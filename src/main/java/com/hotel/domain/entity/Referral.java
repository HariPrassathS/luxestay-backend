package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Referral entity tracking referral relationships.
 */
@Entity
@Table(name = "referrals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_user_id", nullable = false)
    private User referrer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id", nullable = false, unique = true)
    private User referred;

    @Column(name = "referral_code", nullable = false, length = 20)
    private String referralCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private ReferralStatus status = ReferralStatus.PENDING;

    @Column(name = "referrer_xp_earned", nullable = false)
    @lombok.Builder.Default
    private Integer referrerXpEarned = 0;

    @Column(name = "referred_xp_earned", nullable = false)
    @lombok.Builder.Default
    private Integer referredXpEarned = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_booking_id")
    private Booking firstBooking;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "first_booking_at")
    private LocalDateTime firstBookingAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void markRegistered() {
        this.status = ReferralStatus.REGISTERED;
        this.registeredAt = LocalDateTime.now();
    }

    public void markFirstBooking(Booking booking) {
        this.status = ReferralStatus.FIRST_BOOKING;
        this.firstBooking = booking;
        this.firstBookingAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.status = ReferralStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
