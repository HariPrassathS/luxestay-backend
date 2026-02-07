package com.hotel.controller;

import com.hotel.domain.dto.LoyaltyDto.*;
import com.hotel.domain.entity.User;
import com.hotel.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Loyalty Program endpoints.
 */
@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Slf4j
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    // ==================== Profile Endpoints ====================

    /**
     * Get current user's loyalty dashboard.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<LoyaltyDashboard> getDashboard(@AuthenticationPrincipal User user) {
        log.info("Getting loyalty dashboard for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get current user's loyalty profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<LoyaltyProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        log.info("Getting loyalty profile for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard.getProfile());
    }

    /**
     * Get user's current benefits based on level.
     */
    @GetMapping("/benefits")
    public ResponseEntity<LevelInfo> getBenefits(@AuthenticationPrincipal User user) {
        log.info("Getting benefits for user: {}", user.getEmail());
        LevelInfo benefits = loyaltyService.getUserBenefits(user);
        return ResponseEntity.ok(benefits);
    }

    /**
     * Get user's applicable discount percentage.
     */
    @GetMapping("/discount")
    public ResponseEntity<Map<String, Object>> getDiscount(@AuthenticationPrincipal User user) {
        log.info("Getting discount for user: {}", user.getEmail());
        var discount = loyaltyService.getUserDiscount(user);
        return ResponseEntity.ok(Map.of(
            "discountPercentage", discount,
            "message", discount.compareTo(java.math.BigDecimal.ZERO) > 0 
                ? "You get " + discount + "% off on all bookings!" 
                : "Complete more stays to unlock discounts"
        ));
    }

    // ==================== Level Endpoints ====================

    /**
     * Get all loyalty levels.
     */
    @GetMapping("/levels")
    public ResponseEntity<List<LevelInfo>> getAllLevels() {
        log.info("Getting all loyalty levels");
        List<LevelInfo> levels = loyaltyService.getAllLevels();
        return ResponseEntity.ok(levels);
    }

    // ==================== Badge Endpoints ====================

    /**
     * Get all badges with unlock status for current user.
     */
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeInfo>> getBadges(@AuthenticationPrincipal User user) {
        log.info("Getting badges for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard.getAllBadges());
    }

    /**
     * Get locked badges with progress.
     */
    @GetMapping("/badges/locked")
    public ResponseEntity<List<BadgeInfo>> getLockedBadges(@AuthenticationPrincipal User user) {
        log.info("Getting locked badges for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard.getLockedBadges());
    }

    // ==================== Reward Endpoints ====================

    /**
     * Get available rewards for current user.
     */
    @GetMapping("/rewards")
    public ResponseEntity<List<RewardInfo>> getRewards(@AuthenticationPrincipal User user) {
        log.info("Getting rewards for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard.getAvailableRewards());
    }

    /**
     * Redeem a reward.
     */
    @PostMapping("/rewards/redeem")
    public ResponseEntity<RedemptionInfo> redeemReward(
            @AuthenticationPrincipal User user,
            @RequestBody RedeemRewardRequest request) {
        log.info("User {} redeeming reward {}", user.getEmail(), request.getRewardId());
        try {
            RedemptionInfo redemption = loyaltyService.redeemReward(user, request.getRewardId());
            return ResponseEntity.ok(redemption);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Redemption failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get active redemptions for current user.
     */
    @GetMapping("/redemptions")
    public ResponseEntity<List<RedemptionInfo>> getRedemptions(@AuthenticationPrincipal User user) {
        log.info("Getting redemptions for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard.getActiveRedemptions());
    }

    // ==================== Referral Endpoints ====================

    /**
     * Get referral information for current user.
     */
    @GetMapping("/referrals")
    public ResponseEntity<ReferralInfo> getReferralInfo(@AuthenticationPrincipal User user) {
        log.info("Getting referral info for user: {}", user.getEmail());
        ReferralInfo info = loyaltyService.getReferralInfo(user);
        return ResponseEntity.ok(info);
    }

    /**
     * Generate referral code for current user.
     */
    @PostMapping("/referrals/code")
    public ResponseEntity<Map<String, String>> generateReferralCode(@AuthenticationPrincipal User user) {
        log.info("Generating referral code for user: {}", user.getEmail());
        String code = loyaltyService.generateReferralCode(user);
        return ResponseEntity.ok(Map.of(
            "referralCode", code,
            "shareUrl", "https://luxestay.com/signup?ref=" + code
        ));
    }

    // ==================== XP Activity Endpoints ====================

    /**
     * Get XP activity history for current user.
     */
    @GetMapping("/activity")
    public ResponseEntity<List<XpActivityItem>> getActivity(@AuthenticationPrincipal User user) {
        log.info("Getting XP activity for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard.getRecentActivity());
    }

    /**
     * Get XP breakdown by source.
     */
    @GetMapping("/xp-breakdown")
    public ResponseEntity<Map<String, Integer>> getXpBreakdown(@AuthenticationPrincipal User user) {
        log.info("Getting XP breakdown for user: {}", user.getEmail());
        LoyaltyDashboard dashboard = loyaltyService.getLoyaltyDashboard(user);
        return ResponseEntity.ok(dashboard.getXpBreakdown());
    }

    // ==================== Leaderboard Endpoints ====================

    /**
     * Get leaderboard with current user's position.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardPosition> getLeaderboard(@AuthenticationPrincipal User user) {
        log.info("Getting leaderboard for user: {}", user.getEmail());
        LeaderboardPosition leaderboard = loyaltyService.getLeaderboardPosition(user.getId());
        return ResponseEntity.ok(leaderboard);
    }
}
