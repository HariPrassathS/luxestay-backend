package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Loyalty Reward entity for redeemable rewards catalog.
 */
@Entity
@Table(name = "loyalty_rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reward_code", nullable = false, unique = true, length = 50)
    private String rewardCode;

    @Column(name = "reward_name", nullable = false, length = 100)
    private String rewardName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @Column(name = "xp_cost", nullable = false)
    private Integer xpCost;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_max_amount", precision = 10, scale = 2)
    private BigDecimal discountMaxAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "min_level_required")
    private LoyaltyLevel minLevelRequired;

    @Column(name = "valid_days", nullable = false)
    @lombok.Builder.Default
    private Integer validDays = 30;

    @Column(name = "max_redemptions_per_user")
    private Integer maxRedemptionsPerUser;

    @Column(name = "total_available")
    private Integer totalAvailable;

    @Column(name = "total_redeemed", nullable = false)
    @lombok.Builder.Default
    private Integer totalRedeemed = 0;

    @Column(nullable = false, length = 50)
    @lombok.Builder.Default
    private String icon = "fa-gift";

    @Column(name = "is_active", nullable = false)
    @lombok.Builder.Default
    private Boolean isActive = true;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if reward is currently available.
     */
    public boolean isAvailable() {
        if (!isActive) return false;
        LocalDateTime now = LocalDateTime.now();
        if (startsAt != null && now.isBefore(startsAt)) return false;
        if (expiresAt != null && now.isAfter(expiresAt)) return false;
        if (totalAvailable != null && totalRedeemed >= totalAvailable) return false;
        return true;
    }

    /**
     * Check if user level meets minimum requirement.
     */
    public boolean isAvailableForLevel(LoyaltyLevel userLevel) {
        if (minLevelRequired == null) return true;
        return userLevel.getLevelNumber() >= minLevelRequired.getLevelNumber();
    }
}
