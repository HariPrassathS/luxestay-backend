package com.hotel.repository;

import com.hotel.domain.entity.LoyaltyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyLevelRepository extends JpaRepository<LoyaltyLevel, Long> {

    Optional<LoyaltyLevel> findByLevelNumber(Integer levelNumber);

    Optional<LoyaltyLevel> findByLevelName(String levelName);

    /**
     * Find the level that contains the given XP amount.
     */
    @Query("SELECT l FROM LoyaltyLevel l WHERE l.minXp <= :xp AND (l.maxXp IS NULL OR l.maxXp >= :xp)")
    Optional<LoyaltyLevel> findLevelForXp(@Param("xp") Integer xp);

    /**
     * Find all levels ordered by level number.
     */
    List<LoyaltyLevel> findAllByOrderByLevelNumberAsc();

    /**
     * Find the next level after the current one.
     */
    @Query("SELECT l FROM LoyaltyLevel l WHERE l.levelNumber = :currentLevel + 1")
    Optional<LoyaltyLevel> findNextLevel(@Param("currentLevel") Integer currentLevelNumber);

    /**
     * Find the first/starter level.
     */
    @Query("SELECT l FROM LoyaltyLevel l WHERE l.levelNumber = 1")
    Optional<LoyaltyLevel> findStarterLevel();
}
