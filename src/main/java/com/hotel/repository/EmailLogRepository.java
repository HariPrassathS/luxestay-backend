package com.hotel.repository;

import com.hotel.domain.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for EmailLog entity operations.
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    /**
     * Find emails by recipient.
     */
    List<EmailLog> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);

    /**
     * Find emails by status.
     */
    List<EmailLog> findByStatusOrderByCreatedAtAsc(EmailLog.EmailStatus status);

    /**
     * Find failed emails that can be retried.
     */
    @Query("SELECT e FROM EmailLog e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetries ORDER BY e.createdAt ASC")
    List<EmailLog> findRetryableEmails(@Param("maxRetries") int maxRetries);

    /**
     * Count emails by type and status in a date range.
     */
    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.emailType = :type AND e.status = :status AND e.createdAt >= :since")
    long countByTypeAndStatusSince(
            @Param("type") EmailLog.EmailType type,
            @Param("status") EmailLog.EmailStatus status,
            @Param("since") LocalDateTime since);
}
