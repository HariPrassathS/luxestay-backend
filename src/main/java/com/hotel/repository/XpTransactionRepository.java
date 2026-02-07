package com.hotel.repository;

import com.hotel.domain.entity.XpSourceType;
import com.hotel.domain.entity.XpTransaction;
import com.hotel.domain.entity.XpTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface XpTransactionRepository extends JpaRepository<XpTransaction, Long> {

    /**
     * Find all transactions for a user (history).
     */
    Page<XpTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find transactions by type for a user.
     */
    List<XpTransaction> findByUserIdAndTransactionType(Long userId, XpTransactionType type);

    /**
     * Find transactions by source (e.g., all XP from a specific booking).
     */
    List<XpTransaction> findBySourceTypeAndSourceId(XpSourceType sourceType, Long sourceId);

    /**
     * Check if XP was already awarded for a source (prevent duplicates).
     */
    boolean existsByUserIdAndSourceTypeAndSourceIdAndTransactionType(
            Long userId, XpSourceType sourceType, Long sourceId, XpTransactionType type);

    /**
     * Find level-up transactions for a user.
     */
    @Query("SELECT t FROM XpTransaction t WHERE t.user.id = :userId AND t.isLevelUp = true ORDER BY t.createdAt DESC")
    List<XpTransaction> findLevelUpHistory(@Param("userId") Long userId);

    /**
     * Sum XP earned in a time period.
     */
    @Query("SELECT COALESCE(SUM(t.xpAmount), 0) FROM XpTransaction t WHERE t.user.id = :userId AND t.xpAmount > 0 AND t.createdAt BETWEEN :start AND :end")
    Integer sumXpEarnedBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Get XP breakdown by transaction type.
     */
    @Query("SELECT t.transactionType, SUM(t.xpAmount) FROM XpTransaction t WHERE t.user.id = :userId AND t.xpAmount > 0 GROUP BY t.transactionType")
    List<Object[]> getXpBreakdownByType(@Param("userId") Long userId);

    /**
     * Recent transactions for activity feed.
     */
    @Query("SELECT t FROM XpTransaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    Page<XpTransaction> findRecentActivity(@Param("userId") Long userId, Pageable pageable);
}
