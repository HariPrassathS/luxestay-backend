package com.hotel.repository;

import com.hotel.domain.entity.User;
import com.hotel.domain.entity.UserLoyaltyProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoyaltyProfileRepository extends JpaRepository<UserLoyaltyProfile, Long> {

    Optional<UserLoyaltyProfile> findByUser(User user);

    Optional<UserLoyaltyProfile> findByUserId(Long userId);

    /**
     * Find top users by XP (leaderboard).
     */
    @Query("SELECT p FROM UserLoyaltyProfile p ORDER BY p.currentXp DESC")
    Page<UserLoyaltyProfile> findTopByXp(Pageable pageable);

    /**
     * Find top users by total spend.
     */
    @Query("SELECT p FROM UserLoyaltyProfile p ORDER BY p.totalSpend DESC")
    Page<UserLoyaltyProfile> findTopBySpend(Pageable pageable);

    /**
     * Find profiles by level.
     */
    @Query("SELECT p FROM UserLoyaltyProfile p WHERE p.currentLevel.id = :levelId")
    List<UserLoyaltyProfile> findByLevelId(@Param("levelId") Long levelId);

    /**
     * Count users at each level.
     */
    @Query("SELECT p.currentLevel.levelName, COUNT(p) FROM UserLoyaltyProfile p GROUP BY p.currentLevel.levelName")
    List<Object[]> countUsersPerLevel();

    /**
     * Find users with active streaks.
     */
    @Query("SELECT p FROM UserLoyaltyProfile p WHERE p.currentStreak > 0 ORDER BY p.currentStreak DESC")
    List<UserLoyaltyProfile> findActiveStreaks();
}
