package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Loyalty Level entity representing tier structure with benefits.
 */
@Entity
@Table(name = "loyalty_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_number", nullable = false, unique = true)
    private Integer levelNumber;

    @Column(name = "level_name", nullable = false, unique = true, length = 50)
    private String levelName;

    @Column(name = "min_xp", nullable = false)
    @lombok.Builder.Default
    private Integer minXp = 0;

    @Column(name = "max_xp")
    private Integer maxXp;  // NULL for highest tier

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    @lombok.Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "free_breakfast", nullable = false)
    @lombok.Builder.Default
    private Boolean freeBreakfast = false;

    @Column(name = "room_upgrade_priority", nullable = false)
    @lombok.Builder.Default
    private Integer roomUpgradePriority = 0;

    @Column(name = "late_checkout_hours", nullable = false)
    @lombok.Builder.Default
    private Integer lateCheckoutHours = 0;

    @Column(name = "early_checkin_hours", nullable = false)
    @lombok.Builder.Default
    private Integer earlyCheckinHours = 0;

    @Column(name = "welcome_bonus_xp", nullable = false)
    @lombok.Builder.Default
    private Integer welcomeBonusXp = 0;

    @Column(name = "badge_icon", nullable = false, length = 50)
    @lombok.Builder.Default
    private String badgeIcon = "fa-medal";

    @Column(name = "badge_color", nullable = false, length = 20)
    @lombok.Builder.Default
    private String badgeColor = "#CD7F32";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if XP falls within this level's range.
     */
    public boolean containsXp(int xp) {
        if (xp < minXp) return false;
        return maxXp == null || xp <= maxXp;
    }

    /**
     * Calculate progress percentage to next level.
     */
    public int getProgressPercentage(int currentXp) {
        if (maxXp == null) return 100; // Max level
        int range = maxXp - minXp + 1;
        int progress = currentXp - minXp;
        return Math.min(100, (progress * 100) / range);
    }

    /**
     * Get XP needed for next level.
     */
    public int getXpToNextLevel(int currentXp) {
        if (maxXp == null) return 0; // Already max
        return Math.max(0, maxXp + 1 - currentXp);
    }
}
