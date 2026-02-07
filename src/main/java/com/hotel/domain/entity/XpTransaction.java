package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * XP Transaction entity for auditable XP history.
 */
@Entity
@Table(name = "xp_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XpTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "xp_amount", nullable = false)
    private Integer xpAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private XpTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private XpSourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "xp_before", nullable = false)
    private Integer xpBefore;

    @Column(name = "xp_after", nullable = false)
    private Integer xpAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_before_id")
    private LoyaltyLevel levelBefore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_after_id")
    private LoyaltyLevel levelAfter;

    @Column(name = "is_level_up", nullable = false)
    @lombok.Builder.Default
    private Boolean isLevelUp = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
