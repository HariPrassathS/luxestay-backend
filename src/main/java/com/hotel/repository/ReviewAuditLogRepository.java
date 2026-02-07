package com.hotel.repository;

import com.hotel.domain.entity.ReviewAuditAction;
import com.hotel.domain.entity.ReviewAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ReviewAuditLog entity operations.
 */
@Repository
public interface ReviewAuditLogRepository extends JpaRepository<ReviewAuditLog, Long> {

    /**
     * Find all audit logs for a review.
     */
    List<ReviewAuditLog> findByReview_IdOrderByCreatedAtDesc(Long reviewId);

    /**
     * Find all audit logs by an admin.
     */
    List<ReviewAuditLog> findByAdmin_IdOrderByCreatedAtDesc(Long adminId);

    /**
     * Find audit logs by action type.
     */
    List<ReviewAuditLog> findByActionOrderByCreatedAtDesc(ReviewAuditAction action);

    /**
     * Find audit logs within a date range.
     */
    @Query("SELECT al FROM ReviewAuditLog al WHERE al.createdAt BETWEEN :start AND :end ORDER BY al.createdAt DESC")
    List<ReviewAuditLog> findByDateRange(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    /**
     * Find recent audit logs with pagination.
     */
    Page<ReviewAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Count actions by type within a date range.
     */
    @Query("SELECT al.action, COUNT(al) FROM ReviewAuditLog al " +
           "WHERE al.createdAt BETWEEN :start AND :end " +
           "GROUP BY al.action")
    List<Object[]> countByActionAndDateRange(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
