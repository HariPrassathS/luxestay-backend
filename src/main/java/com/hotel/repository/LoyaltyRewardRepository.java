package com.hotel.repository;

import com.hotel.domain.entity.LoyaltyReward;
import com.hotel.domain.entity.RewardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyRewardRepository extends JpaRepository<LoyaltyReward, Long> {

    Optional<LoyaltyReward> findByRewardCode(String rewardCode);

    List<LoyaltyReward> findByIsActiveTrueOrderByXpCostAsc();

    List<LoyaltyReward> findByRewardTypeAndIsActiveTrue(RewardType type);

    /**
     * Find rewards available for a user's level and XP.
     */
    @Query("SELECT r FROM LoyaltyReward r WHERE r.isActive = true " +
           "AND (r.minLevelRequired IS NULL OR r.minLevelRequired.levelNumber <= :levelNumber) " +
           "AND r.xpCost <= :userXp " +
           "AND (r.startsAt IS NULL OR r.startsAt <= CURRENT_TIMESTAMP) " +
           "AND (r.expiresAt IS NULL OR r.expiresAt > CURRENT_TIMESTAMP) " +
           "AND (r.totalAvailable IS NULL OR r.totalRedeemed < r.totalAvailable) " +
           "ORDER BY r.xpCost ASC")
    List<LoyaltyReward> findAvailableRewards(@Param("levelNumber") Integer levelNumber, @Param("userXp") Integer userXp);

    /**
     * Find all rewards a user can see (may not afford).
     */
    @Query("SELECT r FROM LoyaltyReward r WHERE r.isActive = true " +
           "AND (r.minLevelRequired IS NULL OR r.minLevelRequired.levelNumber <= :levelNumber) " +
           "AND (r.startsAt IS NULL OR r.startsAt <= CURRENT_TIMESTAMP) " +
           "AND (r.expiresAt IS NULL OR r.expiresAt > CURRENT_TIMESTAMP) " +
           "ORDER BY r.xpCost ASC")
    List<LoyaltyReward> findRewardsForLevel(@Param("levelNumber") Integer levelNumber);
}
