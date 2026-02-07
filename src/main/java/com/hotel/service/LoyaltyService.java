package com.hotel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.domain.dto.LoyaltyDto.*;
import com.hotel.domain.entity.*;
import com.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core Loyalty Program Service.
 * Handles XP calculation, badge unlocking, rewards, and referrals.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoyaltyService {

    private final UserLoyaltyProfileRepository profileRepository;
    private final LoyaltyLevelRepository levelRepository;
    private final XpTransactionRepository xpTransactionRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final LoyaltyRewardRepository rewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ==================== XP Configuration ====================
    private static final int XP_PER_BOOKING = 100;
    private static final int XP_PER_NIGHT = 25;
    private static final int XP_PER_1000_SPENT = 10;  // 10 XP per ₹1000
    private static final int XP_FOR_REVIEW = 50;
    private static final int XP_REFERRAL_SIGNUP = 100;
    private static final int XP_REFERRAL_FIRST_BOOKING = 200;
    private static final int XP_STREAK_BONUS_PER_MONTH = 50;
    private static final int XP_LUXURY_BONUS = 50;  // Bonus for 5-star hotels

    // ==================== Profile Management ====================

    /**
     * Get or create loyalty profile for a user.
     */
    public UserLoyaltyProfile getOrCreateProfile(User user) {
        return profileRepository.findByUser(user)
                .orElseGet(() -> createNewProfile(user));
    }

    /**
     * Create a new loyalty profile for a user.
     */
    private UserLoyaltyProfile createNewProfile(User user) {
        LoyaltyLevel starterLevel = levelRepository.findStarterLevel()
                .orElseThrow(() -> new IllegalStateException("Starter level not configured"));

        UserLoyaltyProfile profile = UserLoyaltyProfile.builder()
                .user(user)
                .currentXp(0)
                .lifetimeXp(0)
                .currentLevel(starterLevel)
                .totalBookings(0)
                .totalNights(0)
                .totalSpend(BigDecimal.ZERO)
                .totalReviews(0)
                .totalReferrals(0)
                .currentStreak(0)
                .longestStreak(0)
                .build();

        profile = profileRepository.save(profile);

        // Award welcome bonus
        if (starterLevel.getWelcomeBonusXp() > 0) {
            awardXp(user, starterLevel.getWelcomeBonusXp(),
                    XpTransactionType.WELCOME_BONUS,
                    XpSourceType.SYSTEM,
                    null,
                    "Welcome to LuxeStay Rewards!");
        }

        log.info("Created loyalty profile for user: {}", user.getEmail());
        return profile;
    }

    /**
     * Get full loyalty dashboard for a user.
     * Note: Not readOnly because getOrCreateProfile may create a new profile
     */
    @Transactional
    public LoyaltyDashboard getLoyaltyDashboard(User user) {
        UserLoyaltyProfile profile = getOrCreateProfile(user);
        
        // Build profile response
        LoyaltyProfileResponse profileResponse = buildProfileResponse(profile);
        
        // Get recent activity
        Page<XpTransaction> recentTx = xpTransactionRepository.findRecentActivity(
                user.getId(), PageRequest.of(0, 10));
        List<XpActivityItem> recentActivity = recentTx.getContent().stream()
                .map(XpActivityItem::from)
                .toList();
        
        // Get all badges with unlock status
        List<Badge> allBadges = badgeRepository.findByIsActiveTrueOrderBySortOrderAsc();
        Set<Long> earnedBadgeIds = userBadgeRepository.findByUserIdOrderByUnlockedAtDesc(user.getId())
                .stream()
                .map(ub -> ub.getBadge().getId())
                .collect(Collectors.toSet());
        
        List<BadgeInfo> badgeInfos = allBadges.stream()
                .map(b -> {
                    boolean unlocked = earnedBadgeIds.contains(b.getId());
                    LocalDateTime unlockedAt = null;
                    if (unlocked) {
                        unlockedAt = userBadgeRepository.findByUserIdAndBadgeId(user.getId(), b.getId())
                                .map(UserBadge::getUnlockedAt)
                                .orElse(null);
                    }
                    BadgeInfo info = BadgeInfo.from(b, unlocked, unlockedAt);
                    if (!unlocked) {
                        // Add progress info for locked badges
                        addBadgeProgress(info, b, profile);
                    }
                    return info;
                })
                .toList();
        
        List<BadgeInfo> lockedBadges = badgeInfos.stream()
                .filter(b -> !b.getIsUnlocked())
                .toList();
        
        // Get available rewards
        List<LoyaltyReward> rewards = rewardRepository.findRewardsForLevel(
                profile.getCurrentLevel().getLevelNumber());
        List<RewardInfo> rewardInfos = rewards.stream()
                .map(r -> RewardInfo.from(r, profile.getCurrentXp(), 
                         profile.getCurrentLevel().getLevelNumber()))
                .toList();
        
        // Get active redemptions
        List<RewardRedemption> activeRedemptions = redemptionRepository.findActiveRedemptions(user.getId());
        List<RedemptionInfo> redemptionInfos = activeRedemptions.stream()
                .map(r -> RedemptionInfo.from(r, profile.getCurrentXp(), 
                         profile.getCurrentLevel().getLevelNumber()))
                .toList();
        
        // Get XP breakdown
        List<Object[]> breakdownData = xpTransactionRepository.getXpBreakdownByType(user.getId());
        Map<String, Integer> xpBreakdown = breakdownData.stream()
                .collect(Collectors.toMap(
                        arr -> ((XpTransactionType) arr[0]).name(),
                        arr -> ((Number) arr[1]).intValue()
                ));
        
        // Get leaderboard position
        LeaderboardPosition leaderboard = getLeaderboardPosition(user.getId());
        
        return LoyaltyDashboard.builder()
                .profile(profileResponse)
                .recentActivity(recentActivity)
                .allBadges(badgeInfos)
                .lockedBadges(lockedBadges)
                .availableRewards(rewardInfos)
                .activeRedemptions(redemptionInfos)
                .xpBreakdown(xpBreakdown)
                .leaderboard(leaderboard)
                .build();
    }

    /**
     * Build profile response with level info.
     */
    private LoyaltyProfileResponse buildProfileResponse(UserLoyaltyProfile profile) {
        LevelInfo currentLevel = LevelInfo.from(profile.getCurrentLevel());
        LevelInfo nextLevel = null;
        
        Optional<LoyaltyLevel> nextLevelOpt = levelRepository.findNextLevel(
                profile.getCurrentLevel().getLevelNumber());
        if (nextLevelOpt.isPresent()) {
            nextLevel = LevelInfo.from(nextLevelOpt.get());
        }
        
        // Get featured badges
        List<UserBadge> featuredBadges = userBadgeRepository.findFeaturedBadges(profile.getUser().getId());
        List<BadgeInfo> featuredBadgeInfos = featuredBadges.stream()
                .map(ub -> BadgeInfo.from(ub.getBadge(), true, ub.getUnlockedAt()))
                .toList();
        
        // Get recent badges
        List<UserBadge> recentBadges = userBadgeRepository.findRecentBadges(profile.getUser().getId());
        List<BadgeInfo> recentBadgeInfos = recentBadges.stream()
                .limit(5)
                .map(ub -> BadgeInfo.from(ub.getBadge(), true, ub.getUnlockedAt()))
                .toList();
        
        long totalBadges = userBadgeRepository.countByUserId(profile.getUser().getId());
        
        LoyaltyStats stats = LoyaltyStats.builder()
                .totalBookings(profile.getTotalBookings())
                .totalNights(profile.getTotalNights())
                .totalSpend(profile.getTotalSpend())
                .totalReviews(profile.getTotalReviews())
                .totalReferrals(profile.getTotalReferrals())
                .currentStreak(profile.getCurrentStreak())
                .longestStreak(profile.getLongestStreak())
                .totalBadges((int) totalBadges)
                .lastBookingDate(profile.getLastBookingDate())
                .build();
        
        return LoyaltyProfileResponse.builder()
                .userId(profile.getUser().getId())
                .userName(profile.getUser().getFirstName() + " " + profile.getUser().getLastName())
                .currentXp(profile.getCurrentXp())
                .lifetimeXp(profile.getLifetimeXp())
                .currentLevel(currentLevel)
                .nextLevel(nextLevel)
                .xpToNextLevel(profile.getXpToNextLevel())
                .progressPercentage(profile.getProgressToNextLevel())
                .stats(stats)
                .featuredBadges(featuredBadgeInfos)
                .recentBadges(recentBadgeInfos)
                .memberSince(profile.getMemberSince())
                .build();
    }

    // ==================== XP Award System ====================

    /**
     * Award XP to a user with full audit trail.
     */
    public XpAwardResult awardXp(User user, int xpAmount, XpTransactionType type,
                                  XpSourceType sourceType, Long sourceId, String description) {
        UserLoyaltyProfile profile = getOrCreateProfile(user);
        
        // Prevent duplicate awards for the same source
        if (sourceId != null && xpTransactionRepository.existsByUserIdAndSourceTypeAndSourceIdAndTransactionType(
                user.getId(), sourceType, sourceId, type)) {
            log.warn("XP already awarded for source: {} {} {}", sourceType, sourceId, type);
            return XpAwardResult.builder()
                    .xpAwarded(0)
                    .newTotalXp(profile.getCurrentXp())
                    .leveledUp(false)
                    .message("XP already awarded for this action")
                    .build();
        }
        
        int xpBefore = profile.getCurrentXp();
        LoyaltyLevel levelBefore = profile.getCurrentLevel();
        
        // Add XP
        boolean needsLevelCheck = profile.addXp(xpAmount);
        
        // Check for level up
        LoyaltyLevel newLevel = levelBefore;
        boolean leveledUp = false;
        if (needsLevelCheck) {
            Optional<LoyaltyLevel> correctLevel = levelRepository.findLevelForXp(profile.getCurrentXp());
            if (correctLevel.isPresent() && !correctLevel.get().getId().equals(levelBefore.getId())) {
                newLevel = correctLevel.get();
                profile.setCurrentLevel(newLevel);
                leveledUp = true;
                log.info("User {} leveled up to {}", user.getEmail(), newLevel.getLevelName());
            }
        }
        
        // Save transaction
        XpTransaction transaction = XpTransaction.builder()
                .user(user)
                .xpAmount(xpAmount)
                .transactionType(type)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .description(description)
                .xpBefore(xpBefore)
                .xpAfter(profile.getCurrentXp())
                .levelBefore(levelBefore)
                .levelAfter(newLevel)
                .isLevelUp(leveledUp)
                .build();
        xpTransactionRepository.save(transaction);
        
        // Save profile
        profileRepository.save(profile);
        
        // Check for new badges
        List<BadgeInfo> newBadges = checkAndAwardBadges(user, profile, null);
        
        // If leveled up, award level-up bonus
        if (leveledUp && newLevel.getWelcomeBonusXp() > 0) {
            awardXp(user, newLevel.getWelcomeBonusXp(),
                    XpTransactionType.LEVEL_UP_BONUS,
                    XpSourceType.SYSTEM,
                    null,
                    "Level up bonus for reaching " + newLevel.getLevelName() + "!");
        }
        
        String message = String.format("+%d XP! %s", xpAmount, description);
        if (leveledUp) {
            message += String.format(" 🎉 Level up! You're now %s!", newLevel.getLevelName());
        }
        
        return XpAwardResult.builder()
                .xpAwarded(xpAmount)
                .newTotalXp(profile.getCurrentXp())
                .leveledUp(leveledUp)
                .newLevel(leveledUp ? LevelInfo.from(newLevel) : null)
                .newBadges(newBadges)
                .message(message)
                .build();
    }

    /**
     * Process XP for a completed booking.
     */
    public XpAwardResult processBookingXp(Booking booking) {
        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            log.warn("Booking {} not checked out, cannot award XP", booking.getId());
            return null;
        }
        
        User user = booking.getUser();
        UserLoyaltyProfile profile = getOrCreateProfile(user);
        
        // Calculate total XP
        int totalXp = 0;
        List<String> xpReasons = new ArrayList<>();
        
        // Base XP for booking
        totalXp += XP_PER_BOOKING;
        xpReasons.add(XP_PER_BOOKING + " XP for booking");
        
        // XP for nights stayed
        int nightsXp = booking.getTotalNights() * XP_PER_NIGHT;
        totalXp += nightsXp;
        xpReasons.add(nightsXp + " XP for " + booking.getTotalNights() + " nights");
        
        // XP for spend
        int spendXp = booking.getTotalPrice().divide(BigDecimal.valueOf(1000), 0, 
                java.math.RoundingMode.DOWN).intValue() * XP_PER_1000_SPENT;
        if (spendXp > 0) {
            totalXp += spendXp;
            xpReasons.add(spendXp + " XP for spend");
        }
        
        // Luxury bonus for 5-star hotels
        if (booking.getRoom().getHotel().getStarRating() != null && 
            booking.getRoom().getHotel().getStarRating() >= 5) {
            totalXp += XP_LUXURY_BONUS;
            xpReasons.add(XP_LUXURY_BONUS + " XP luxury bonus");
        }
        
        // Update profile stats
        profile.recordBooking(booking.getTotalNights(), booking.getTotalPrice(), 
                              booking.getCheckOutDate());
        profileRepository.save(profile);
        
        // Award XP
        String description = "Completed stay at " + booking.getRoom().getHotel().getName() + 
                           " (" + String.join(", ", xpReasons) + ")";
        
        XpAwardResult result = awardXp(user, totalXp,
                XpTransactionType.BOOKING_COMPLETED,
                XpSourceType.BOOKING,
                booking.getId(),
                description);
        
        // Check for referral completion
        checkReferralCompletion(user, booking);
        
        // Update streak
        updateStreak(profile);
        
        log.info("Awarded {} XP to user {} for booking {}", totalXp, user.getEmail(), booking.getId());
        return result;
    }

    // ==================== Badge System ====================

    /**
     * Check and award any badges the user has earned.
     */
    public List<BadgeInfo> checkAndAwardBadges(User user, UserLoyaltyProfile profile, Booking booking) {
        List<BadgeInfo> newBadges = new ArrayList<>();
        List<Badge> unlockedBadges = badgeRepository.findUnlockedBadgesForUser(user.getId());
        
        for (Badge badge : unlockedBadges) {
            // Double-check to prevent duplicate inserts on concurrent requests
            if (userBadgeRepository.findByUserIdAndBadgeId(user.getId(), badge.getId()).isPresent()) {
                continue; // Already has this badge
            }
            
            if (checkBadgeCriteria(badge, profile)) {
                try {
                    UserBadge userBadge = UserBadge.builder()
                            .user(user)
                            .badge(badge)
                            .sourceBooking(booking)
                            .isFeatured(false)
                            .build();
                    userBadgeRepository.save(userBadge);
                
                    // Award badge XP
                    if (badge.getXpReward() > 0) {
                        awardXp(user, badge.getXpReward(),
                                XpTransactionType.BADGE_UNLOCKED,
                                XpSourceType.SYSTEM,
                                badge.getId(),
                                "Unlocked badge: " + badge.getBadgeName());
                    }
                    
                    newBadges.add(BadgeInfo.from(badge, true, LocalDateTime.now()));
                    log.info("User {} unlocked badge: {}", user.getEmail(), badge.getBadgeName());
                } catch (Exception e) {
                    // Ignore duplicate key errors from concurrent requests
                    log.debug("Badge {} may already exist for user {}: {}", 
                              badge.getBadgeCode(), user.getEmail(), e.getMessage());
                }
            }
        }
        
        return newBadges;
    }

    /**
     * Check if a user meets the criteria for a badge.
     */
    private boolean checkBadgeCriteria(Badge badge, UserLoyaltyProfile profile) {
        try {
            JsonNode criteria = objectMapper.readTree(badge.getUnlockCriteria());
            
            if (criteria.has("bookings_completed")) {
                int required = criteria.get("bookings_completed").asInt();
                if (profile.getTotalBookings() < required) return false;
            }
            
            if (criteria.has("total_nights")) {
                int required = criteria.get("total_nights").asInt();
                if (profile.getTotalNights() < required) return false;
            }
            
            if (criteria.has("total_spend")) {
                int required = criteria.get("total_spend").asInt();
                if (profile.getTotalSpend().compareTo(BigDecimal.valueOf(required)) < 0) return false;
            }
            
            if (criteria.has("reviews_count")) {
                int required = criteria.get("reviews_count").asInt();
                if (profile.getTotalReviews() < required) return false;
            }
            
            if (criteria.has("referrals_completed")) {
                int required = criteria.get("referrals_completed").asInt();
                if (profile.getTotalReferrals() < required) return false;
            }
            
            if (criteria.has("streak_months")) {
                int required = criteria.get("streak_months").asInt();
                if (profile.getCurrentStreak() < required) return false;
            }
            
            return true;
        } catch (JsonProcessingException e) {
            log.error("Error parsing badge criteria for {}: {}", badge.getBadgeCode(), e.getMessage());
            return false;
        }
    }

    /**
     * Add progress info to a locked badge.
     */
    private void addBadgeProgress(BadgeInfo info, Badge badge, UserLoyaltyProfile profile) {
        try {
            JsonNode criteria = objectMapper.readTree(badge.getUnlockCriteria());
            
            if (criteria.has("bookings_completed")) {
                int required = criteria.get("bookings_completed").asInt();
                int current = profile.getTotalBookings();
                info.setProgressPercent(required > 0 ? Math.min(100, (current * 100) / required) : 0);
                info.setProgressText(current + "/" + required + " bookings");
            } else if (criteria.has("total_nights")) {
                int required = criteria.get("total_nights").asInt();
                int current = profile.getTotalNights();
                info.setProgressPercent(required > 0 ? Math.min(100, (current * 100) / required) : 0);
                info.setProgressText(current + "/" + required + " nights");
            } else if (criteria.has("total_spend")) {
                int required = criteria.get("total_spend").asInt();
                int current = profile.getTotalSpend().intValue();
                info.setProgressPercent(required > 0 ? Math.min(100, (current * 100) / required) : 0);
                info.setProgressText("₹" + current + "/₹" + required);
            } else if (criteria.has("reviews_count")) {
                int required = criteria.get("reviews_count").asInt();
                int current = profile.getTotalReviews();
                info.setProgressPercent(required > 0 ? Math.min(100, (current * 100) / required) : 0);
                info.setProgressText(current + "/" + required + " reviews");
            } else if (criteria.has("referrals_completed")) {
                int required = criteria.get("referrals_completed").asInt();
                int current = profile.getTotalReferrals();
                info.setProgressPercent(required > 0 ? Math.min(100, (current * 100) / required) : 0);
                info.setProgressText(current + "/" + required + " referrals");
            } else if (criteria.has("streak_months")) {
                int required = criteria.get("streak_months").asInt();
                int current = profile.getCurrentStreak();
                info.setProgressPercent(required > 0 ? Math.min(100, (current * 100) / required) : 0);
                info.setProgressText(current + "/" + required + " months");
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing badge criteria: {}", e.getMessage());
        }
    }

    // ==================== Reward Redemption ====================

    /**
     * Redeem a reward.
     */
    public RedemptionInfo redeemReward(User user, Long rewardId) {
        UserLoyaltyProfile profile = getOrCreateProfile(user);
        LoyaltyReward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found"));
        
        // Validate
        if (!reward.isAvailable()) {
            throw new IllegalStateException("This reward is no longer available");
        }
        
        if (!reward.isAvailableForLevel(profile.getCurrentLevel())) {
            throw new IllegalStateException("Your level is too low for this reward");
        }
        
        if (profile.getCurrentXp() < reward.getXpCost()) {
            throw new IllegalStateException("Insufficient XP");
        }
        
        // Check max redemptions per user
        if (reward.getMaxRedemptionsPerUser() != null) {
            long userRedemptions = redemptionRepository.countByUserIdAndRewardId(user.getId(), rewardId);
            if (userRedemptions >= reward.getMaxRedemptionsPerUser()) {
                throw new IllegalStateException("Maximum redemptions reached for this reward");
            }
        }
        
        // Deduct XP
        int xpBefore = profile.getCurrentXp();
        profile.spendXp(reward.getXpCost());
        profileRepository.save(profile);
        
        // Create redemption
        String redemptionCode = generateRedemptionCode();
        RewardRedemption redemption = RewardRedemption.builder()
                .user(user)
                .reward(reward)
                .redemptionCode(redemptionCode)
                .xpSpent(reward.getXpCost())
                .status(RedemptionStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(reward.getValidDays()))
                .build();
        redemption = redemptionRepository.save(redemption);
        
        // Update reward redemption count
        reward.setTotalRedeemed(reward.getTotalRedeemed() + 1);
        rewardRepository.save(reward);
        
        // Log XP transaction
        XpTransaction transaction = XpTransaction.builder()
                .user(user)
                .xpAmount(-reward.getXpCost())
                .transactionType(XpTransactionType.XP_REDEMPTION)
                .sourceType(XpSourceType.SYSTEM)
                .sourceId(redemption.getId())
                .description("Redeemed: " + reward.getRewardName())
                .xpBefore(xpBefore)
                .xpAfter(profile.getCurrentXp())
                .levelBefore(profile.getCurrentLevel())
                .levelAfter(profile.getCurrentLevel())
                .isLevelUp(false)
                .build();
        xpTransactionRepository.save(transaction);
        
        log.info("User {} redeemed reward {} for {} XP", user.getEmail(), 
                reward.getRewardCode(), reward.getXpCost());
        
        return RedemptionInfo.from(redemption, profile.getCurrentXp(), 
                profile.getCurrentLevel().getLevelNumber());
    }

    /**
     * Generate unique redemption code.
     */
    private String generateRedemptionCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder("RWD-");
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    // ==================== Referral System ====================

    /**
     * Generate referral code for a user.
     */
    public String generateReferralCode(User user) {
        if (user.getReferralCode() != null) {
            return user.getReferralCode();
        }
        
        String code = "REF-" + user.getId() + "-" + 
                     Long.toHexString(System.currentTimeMillis()).toUpperCase().substring(0, 4);
        user.setReferralCode(code);
        userRepository.save(user);
        return code;
    }

    /**
     * Get referral info for a user.
     */
    @Transactional(readOnly = true)
    public ReferralInfo getReferralInfo(User user) {
        String referralCode = generateReferralCode(user);
        List<Referral> referrals = referralRepository.findByReferrerIdOrderByCreatedAtDesc(user.getId());
        
        long completed = referrals.stream().filter(r -> r.getStatus() == ReferralStatus.COMPLETED).count();
        long pending = referrals.stream().filter(r -> r.getStatus() != ReferralStatus.COMPLETED).count();
        int totalXp = referralRepository.sumReferralXpEarned(user.getId());
        
        List<ReferralDetail> details = referrals.stream()
                .map(r -> ReferralDetail.builder()
                        .referredUserName(r.getReferred().getFirstName() + " " + 
                                         r.getReferred().getLastName().charAt(0) + ".")
                        .status(r.getStatus().name())
                        .xpEarned(r.getReferrerXpEarned())
                        .registeredAt(r.getRegisteredAt())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();
        
        return ReferralInfo.builder()
                .referralCode(referralCode)
                .shareUrl("https://luxestay.com/signup?ref=" + referralCode)
                .totalReferrals(referrals.size())
                .completedReferrals((int) completed)
                .pendingReferrals((int) pending)
                .totalXpEarned(totalXp)
                .referrals(details)
                .build();
    }

    /**
     * Process referral when a new user signs up with a code.
     */
    public void processReferralSignup(User newUser, String referralCode) {
        if (referralCode == null || referralCode.isEmpty()) return;
        if (referralRepository.existsByReferredId(newUser.getId())) return;
        
        User referrer = userRepository.findByReferralCode(referralCode).orElse(null);
        if (referrer == null || referrer.getId().equals(newUser.getId())) return;
        
        Referral referral = Referral.builder()
                .referrer(referrer)
                .referred(newUser)
                .referralCode(referralCode)
                .status(ReferralStatus.REGISTERED)
                .build();
        referral.markRegistered();
        referralRepository.save(referral);
        
        // Award signup XP to referrer
        awardXp(referrer, XP_REFERRAL_SIGNUP,
                XpTransactionType.REFERRAL_SIGNUP,
                XpSourceType.REFERRAL,
                referral.getId(),
                "Friend " + newUser.getFirstName() + " joined LuxeStay!");
        
        referral.setReferrerXpEarned(XP_REFERRAL_SIGNUP);
        referralRepository.save(referral);
        
        // Update referrer's profile
        UserLoyaltyProfile referrerProfile = getOrCreateProfile(referrer);
        referrerProfile.setTotalReferrals(referrerProfile.getTotalReferrals() + 1);
        profileRepository.save(referrerProfile);
        
        log.info("Processed referral signup: {} referred by {}", 
                newUser.getEmail(), referrer.getEmail());
    }

    /**
     * Check and complete referral when referred user completes first booking.
     */
    private void checkReferralCompletion(User user, Booking booking) {
        Referral referral = referralRepository.findByReferredId(user.getId()).orElse(null);
        if (referral == null || referral.getStatus() == ReferralStatus.COMPLETED) return;
        
        referral.markFirstBooking(booking);
        referral.markCompleted();
        
        // Award first booking XP to referrer
        awardXp(referral.getReferrer(), XP_REFERRAL_FIRST_BOOKING,
                XpTransactionType.REFERRAL_FIRST_BOOKING,
                XpSourceType.REFERRAL,
                referral.getId(),
                "Friend " + user.getFirstName() + " completed their first booking!");
        
        referral.setReferrerXpEarned(referral.getReferrerXpEarned() + XP_REFERRAL_FIRST_BOOKING);
        referralRepository.save(referral);
        
        log.info("Completed referral: {} first booking, referrer {} earned XP", 
                user.getEmail(), referral.getReferrer().getEmail());
    }

    // ==================== Streak Management ====================

    /**
     * Update booking streak for a user.
     */
    private void updateStreak(UserLoyaltyProfile profile) {
        LocalDate lastBooking = profile.getLastBookingDate();
        LocalDate now = LocalDate.now();
        
        if (lastBooking == null) {
            profile.setCurrentStreak(1);
        } else {
            // Check if within same or consecutive month
            boolean sameMonth = lastBooking.getMonth() == now.getMonth() && 
                               lastBooking.getYear() == now.getYear();
            boolean consecutiveMonth = lastBooking.plusMonths(1).getMonth() == now.getMonth() &&
                                      lastBooking.plusMonths(1).getYear() == now.getYear();
            
            if (sameMonth) {
                // Same month, streak continues
            } else if (consecutiveMonth) {
                // Consecutive month, increment streak
                profile.setCurrentStreak(profile.getCurrentStreak() + 1);
                
                // Award streak bonus
                int streakXp = profile.getCurrentStreak() * XP_STREAK_BONUS_PER_MONTH;
                awardXp(profile.getUser(), streakXp,
                        XpTransactionType.STREAK_BONUS,
                        XpSourceType.SYSTEM,
                        null,
                        profile.getCurrentStreak() + " month booking streak!");
            } else {
                // Streak broken
                profile.setCurrentStreak(1);
            }
        }
        
        // Update longest streak
        if (profile.getCurrentStreak() > profile.getLongestStreak()) {
            profile.setLongestStreak(profile.getCurrentStreak());
        }
        
        profileRepository.save(profile);
    }

    // ==================== Leaderboard ====================

    /**
     * Get leaderboard position for a user.
     */
    @Transactional(readOnly = true)
    public LeaderboardPosition getLeaderboardPosition(Long userId) {
        Page<UserLoyaltyProfile> topProfiles = profileRepository.findTopByXp(PageRequest.of(0, 10));
        
        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        int userRank = 0;
        
        for (UserLoyaltyProfile profile : topProfiles.getContent()) {
            long badgeCount = userBadgeRepository.countByUserId(profile.getUser().getId());
            
            entries.add(LeaderboardEntry.builder()
                    .rank(rank)
                    .userName(profile.getUser().getFirstName() + " " + 
                             profile.getUser().getLastName().charAt(0) + ".")
                    .levelName(profile.getCurrentLevel().getLevelName())
                    .levelIcon(profile.getCurrentLevel().getBadgeIcon())
                    .levelColor(profile.getCurrentLevel().getBadgeColor())
                    .xp(profile.getCurrentXp())
                    .badgeCount((int) badgeCount)
                    .build());
            
            if (profile.getUser().getId().equals(userId)) {
                userRank = rank;
            }
            rank++;
        }
        
        long totalUsers = profileRepository.count();
        
        // If user not in top 10, find their rank
        if (userRank == 0 && userId != null) {
            UserLoyaltyProfile userProfile = profileRepository.findByUserId(userId).orElse(null);
            if (userProfile != null) {
                // Approximate rank based on XP
                // In a real app, use a more efficient method
                userRank = (int) profileRepository.count(); // Simplified
            }
        }
        
        String percentile = totalUsers > 0 ? 
                String.format("Top %.0f%%", (double) userRank / totalUsers * 100) : "N/A";
        
        return LeaderboardPosition.builder()
                .rank(userRank)
                .totalUsers((int) totalUsers)
                .percentile(percentile)
                .topUsers(entries)
                .build();
    }

    // ==================== Review XP ====================

    /**
     * Award XP for submitting a review.
     */
    public XpAwardResult processReviewXp(User user, Long bookingId) {
        UserLoyaltyProfile profile = getOrCreateProfile(user);
        profile.setTotalReviews(profile.getTotalReviews() + 1);
        profileRepository.save(profile);
        
        return awardXp(user, XP_FOR_REVIEW,
                XpTransactionType.REVIEW_SUBMITTED,
                XpSourceType.REVIEW,
                bookingId,
                "Thank you for your review!");
    }

    // ==================== Level Benefits ====================

    /**
     * Get applicable discount percentage for a user.
     */
    @Transactional(readOnly = true)
    public BigDecimal getUserDiscount(User user) {
        UserLoyaltyProfile profile = profileRepository.findByUser(user).orElse(null);
        if (profile == null) return BigDecimal.ZERO;
        return profile.getCurrentLevel().getDiscountPercentage();
    }

    /**
     * Get all level benefits for a user.
     */
    @Transactional(readOnly = true)
    public LevelInfo getUserBenefits(User user) {
        UserLoyaltyProfile profile = getOrCreateProfile(user);
        return LevelInfo.from(profile.getCurrentLevel());
    }

    /**
     * Get all loyalty levels.
     */
    @Transactional(readOnly = true)
    public List<LevelInfo> getAllLevels() {
        return levelRepository.findAllByOrderByLevelNumberAsc().stream()
                .map(LevelInfo::from)
                .toList();
    }
}
