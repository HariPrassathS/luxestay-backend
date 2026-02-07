package com.hotel.repository;

import com.hotel.domain.entity.RedemptionStatus;
import com.hotel.domain.entity.RewardRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {

    Optional<RewardRedemption> findByRedemptionCode(String redemptionCode);

    List<RewardRedemption> findByUserIdOrderByRedeemedAtDesc(Long userId);

    List<RewardRedemption> findByUserIdAndStatus(Long userId, RedemptionStatus status);

    /**
     * Find active (unused, not expired) redemptions for a user.
     */
    @Query("SELECT r FROM RewardRedemption r WHERE r.user.id = :userId AND r.status = 'ACTIVE' AND r.expiresAt > CURRENT_TIMESTAMP")
    List<RewardRedemption> findActiveRedemptions(@Param("userId") Long userId);

    /**
     * Count redemptions of a specific reward by a user.
     */
    long countByUserIdAndRewardId(Long userId, Long rewardId);

    /**
     * Find expired but still active redemptions (for cleanup).
     */
    @Query("SELECT r FROM RewardRedemption r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<RewardRedemption> findExpiredActiveRedemptions(@Param("now") LocalDateTime now);
}
