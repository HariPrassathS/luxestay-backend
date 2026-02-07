package com.hotel.repository;

import com.hotel.domain.entity.LoginAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for LoginAuditLog entity operations.
 */
@Repository
public interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, Long> {

    /**
     * Find login history for a user.
     */
    List<LoginAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find login history by email (for tracking before user creation).
     */
    List<LoginAuditLog> findByEmailOrderByCreatedAtDesc(String email);

    /**
     * Find recent login events with pagination.
     */
    Page<LoginAuditLog> findByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find failed login attempts for an email in a time window.
     */
    @Query("SELECT COUNT(l) FROM LoginAuditLog l WHERE l.email = :email AND l.action = 'LOGIN_FAILED' AND l.createdAt >= :since")
    long countFailedAttemptsSince(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Find failed login attempts from an IP in a time window.
     */
    @Query("SELECT COUNT(l) FROM LoginAuditLog l WHERE l.ipAddress = :ip AND l.action = 'LOGIN_FAILED' AND l.createdAt >= :since")
    long countFailedAttemptsFromIpSince(@Param("ip") String ip, @Param("since") LocalDateTime since);

    /**
     * Find recent logins by action type.
     */
    List<LoginAuditLog> findByActionOrderByCreatedAtDesc(LoginAuditLog.LoginAction action, Pageable pageable);

    /**
     * Find first logins.
     */
    List<LoginAuditLog> findByIsFirstLoginTrueOrderByCreatedAtDesc(Pageable pageable);
}
