package com.hotel.domain.dto;

import com.hotel.domain.entity.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for the Loyalty Program system.
 */
public class LoyaltyDto {

    // ==================== Level DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelInfo {
        private Long id;
        private Integer levelNumber;
        private String levelName;
        private Integer minXp;
        private Integer maxXp;
        private BigDecimal discountPercentage;
        private Boolean freeBreakfast;
        private Integer roomUpgradePriority;
        private Integer lateCheckoutHours;
        private Integer earlyCheckinHours;
        private String badgeIcon;
        private String badgeColor;
        private String description;

        public static LevelInfo from(LoyaltyLevel level) {
            return LevelInfo.builder()
                    .id(level.getId())
                    .levelNumber(level.getLevelNumber())
                    .levelName(level.getLevelName())
                    .minXp(level.getMinXp())
                    .maxXp(level.getMaxXp())
                    .discountPercentage(level.getDiscountPercentage())
                    .freeBreakfast(level.getFreeBreakfast())
                    .roomUpgradePriority(level.getRoomUpgradePriority())
                    .lateCheckoutHours(level.getLateCheckoutHours())
                    .earlyCheckinHours(level.getEarlyCheckinHours())
                    .badgeIcon(level.getBadgeIcon())
                    .badgeColor(level.getBadgeColor())
                    .description(level.getDescription())
                    .build();
        }
    }

    // ==================== Profile DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyProfileResponse {
        private Long userId;
        private String userName;
        private Integer currentXp;
        private Integer lifetimeXp;
        private LevelInfo currentLevel;
        private LevelInfo nextLevel;
        private Integer xpToNextLevel;
        private Integer progressPercentage;
        private LoyaltyStats stats;
        private List<BadgeInfo> featuredBadges;
        private List<BadgeInfo> recentBadges;
        private LocalDateTime memberSince;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyStats {
        private Integer totalBookings;
        private Integer totalNights;
        private BigDecimal totalSpend;
        private Integer totalReviews;
        private Integer totalReferrals;
        private Integer currentStreak;
        private Integer longestStreak;
        private Integer totalBadges;
        private LocalDate lastBookingDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyDashboard {
        private LoyaltyProfileResponse profile;
        private List<XpActivityItem> recentActivity;
        private List<BadgeInfo> allBadges;
        private List<BadgeInfo> lockedBadges;
        private List<RewardInfo> availableRewards;
        private List<RedemptionInfo> activeRedemptions;
        private Map<String, Integer> xpBreakdown;
        private LeaderboardPosition leaderboard;
    }

    // ==================== XP DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XpActivityItem {
        private Long id;
        private Integer xpAmount;
        private String transactionType;
        private String sourceType;
        private Long sourceId;
        private String description;
        private Boolean isLevelUp;
        private String levelName;
        private LocalDateTime timestamp;

        public static XpActivityItem from(XpTransaction tx) {
            return XpActivityItem.builder()
                    .id(tx.getId())
                    .xpAmount(tx.getXpAmount())
                    .transactionType(tx.getTransactionType().name())
                    .sourceType(tx.getSourceType().name())
                    .sourceId(tx.getSourceId())
                    .description(tx.getDescription())
                    .isLevelUp(tx.getIsLevelUp())
                    .levelName(tx.getLevelAfter() != null ? tx.getLevelAfter().getLevelName() : null)
                    .timestamp(tx.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XpAwardResult {
        private Integer xpAwarded;
        private Integer newTotalXp;
        private Boolean leveledUp;
        private LevelInfo newLevel;
        private List<BadgeInfo> newBadges;
        private String message;
    }

    // ==================== Badge DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BadgeInfo {
        private Long id;
        private String badgeCode;
        private String badgeName;
        private String description;
        private String icon;
        private String color;
        private String category;
        private String rarity;
        private Integer xpReward;
        private Boolean isUnlocked;
        private LocalDateTime unlockedAt;
        private Integer progressPercent;  // For locked badges
        private String progressText;      // e.g., "3/10 bookings"

        public static BadgeInfo from(Badge badge, boolean unlocked, LocalDateTime unlockedAt) {
            return BadgeInfo.builder()
                    .id(badge.getId())
                    .badgeCode(badge.getBadgeCode())
                    .badgeName(badge.getBadgeName())
                    .description(badge.getDescription())
                    .icon(badge.getIcon())
                    .color(badge.getColor())
                    .category(badge.getCategory().name())
                    .rarity(badge.getRarity().name())
                    .xpReward(badge.getXpReward())
                    .isUnlocked(unlocked)
                    .unlockedAt(unlockedAt)
                    .build();
        }
    }

    // ==================== Reward DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardInfo {
        private Long id;
        private String rewardCode;
        private String rewardName;
        private String description;
        private String rewardType;
        private Integer xpCost;
        private BigDecimal discountPercentage;
        private BigDecimal discountMaxAmount;
        private Integer minLevelRequired;
        private String minLevelName;
        private Integer validDays;
        private String icon;
        private Boolean canAfford;
        private Boolean meetsLevelRequirement;

        public static RewardInfo from(LoyaltyReward reward, Integer userXp, Integer userLevel) {
            boolean canAfford = userXp >= reward.getXpCost();
            boolean meetsLevel = reward.getMinLevelRequired() == null || 
                                 userLevel >= reward.getMinLevelRequired().getLevelNumber();
            
            return RewardInfo.builder()
                    .id(reward.getId())
                    .rewardCode(reward.getRewardCode())
                    .rewardName(reward.getRewardName())
                    .description(reward.getDescription())
                    .rewardType(reward.getRewardType().name())
                    .xpCost(reward.getXpCost())
                    .discountPercentage(reward.getDiscountPercentage())
                    .discountMaxAmount(reward.getDiscountMaxAmount())
                    .minLevelRequired(reward.getMinLevelRequired() != null ? 
                                     reward.getMinLevelRequired().getLevelNumber() : null)
                    .minLevelName(reward.getMinLevelRequired() != null ? 
                                 reward.getMinLevelRequired().getLevelName() : null)
                    .validDays(reward.getValidDays())
                    .icon(reward.getIcon())
                    .canAfford(canAfford)
                    .meetsLevelRequirement(meetsLevel)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedemptionInfo {
        private Long id;
        private String redemptionCode;
        private RewardInfo reward;
        private Integer xpSpent;
        private String status;
        private LocalDateTime redeemedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime usedAt;
        private Long usedOnBookingId;

        public static RedemptionInfo from(RewardRedemption redemption, Integer userXp, Integer userLevel) {
            return RedemptionInfo.builder()
                    .id(redemption.getId())
                    .redemptionCode(redemption.getRedemptionCode())
                    .reward(RewardInfo.from(redemption.getReward(), userXp, userLevel))
                    .xpSpent(redemption.getXpSpent())
                    .status(redemption.getStatus().name())
                    .redeemedAt(redemption.getRedeemedAt())
                    .expiresAt(redemption.getExpiresAt())
                    .usedAt(redemption.getUsedAt())
                    .usedOnBookingId(redemption.getUsedOnBooking() != null ? 
                                    redemption.getUsedOnBooking().getId() : null)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedeemRewardRequest {
        private Long rewardId;
    }

    // ==================== Referral DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralInfo {
        private String referralCode;
        private String shareUrl;
        private Integer totalReferrals;
        private Integer completedReferrals;
        private Integer pendingReferrals;
        private Integer totalXpEarned;
        private List<ReferralDetail> referrals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralDetail {
        private String referredUserName;
        private String status;
        private Integer xpEarned;
        private LocalDateTime registeredAt;
        private LocalDateTime completedAt;
    }

    // ==================== Leaderboard DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardPosition {
        private Integer rank;
        private Integer totalUsers;
        private String percentile;
        private List<LeaderboardEntry> topUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private Integer rank;
        private String userName;
        private String levelName;
        private String levelIcon;
        private String levelColor;
        private Integer xp;
        private Integer badgeCount;
    }
}
