package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Badge entity representing achievable badges.
 */
@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_code", nullable = false, unique = true, length = 50)
    private String badgeCode;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 50)
    @lombok.Builder.Default
    private String icon = "fa-award";

    @Column(nullable = false, length = 20)
    @lombok.Builder.Default
    private String color = "#FFD700";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private BadgeRarity rarity = BadgeRarity.COMMON;

    @Column(name = "xp_reward", nullable = false)
    @lombok.Builder.Default
    private Integer xpReward = 0;

    @Column(name = "unlock_criteria", nullable = false, columnDefinition = "JSON")
    private String unlockCriteria;  // JSON string

    @Column(name = "is_active", nullable = false)
    @lombok.Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    @lombok.Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
