package com.hotel.repository;

import com.hotel.domain.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserIdOrderByUnlockedAtDesc(Long userId);

    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, Long badgeId);

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    /**
     * Find featured badges for a user (max 3).
     */
    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId AND ub.isFeatured = true ORDER BY ub.unlockedAt DESC")
    List<UserBadge> findFeaturedBadges(@Param("userId") Long userId);

    /**
     * Count badges for a user.
     */
    long countByUserId(Long userId);

    /**
     * Find recent badge unlocks for a user.
     */
    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId ORDER BY ub.unlockedAt DESC")
    List<UserBadge> findRecentBadges(@Param("userId") Long userId);

    /**
     * Count badges by rarity for a user.
     */
    @Query("SELECT ub.badge.rarity, COUNT(ub) FROM UserBadge ub WHERE ub.user.id = :userId GROUP BY ub.badge.rarity")
    List<Object[]> countBadgesByRarity(@Param("userId") Long userId);
}
