package com.hotel.repository;

import com.hotel.domain.entity.Badge;
import com.hotel.domain.entity.BadgeCategory;
import com.hotel.domain.entity.BadgeRarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByBadgeCode(String badgeCode);

    List<Badge> findByIsActiveTrueOrderBySortOrderAsc();

    List<Badge> findByCategoryAndIsActiveTrue(BadgeCategory category);

    List<Badge> findByRarityAndIsActiveTrue(BadgeRarity rarity);

    /**
     * Find badges not yet earned by a user.
     */
    @Query("SELECT b FROM Badge b WHERE b.isActive = true AND b.id NOT IN " +
           "(SELECT ub.badge.id FROM UserBadge ub WHERE ub.user.id = :userId) " +
           "ORDER BY b.sortOrder")
    List<Badge> findUnlockedBadgesForUser(@Param("userId") Long userId);

    /**
     * Find badges earned by a user.
     */
    @Query("SELECT b FROM Badge b WHERE b.id IN " +
           "(SELECT ub.badge.id FROM UserBadge ub WHERE ub.user.id = :userId) " +
           "ORDER BY b.sortOrder")
    List<Badge> findEarnedBadgesForUser(@Param("userId") Long userId);
}
