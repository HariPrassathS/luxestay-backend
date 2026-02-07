package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for tracking login events for security audit.
 */
@Entity
@Table(name = "login_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LoginAction action;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "is_first_login", nullable = false)
    @lombok.Builder.Default
    private Boolean isFirstLogin = false;

    @Column(name = "is_successful", nullable = false)
    @lombok.Builder.Default
    private Boolean isSuccessful = false;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Login action types for audit tracking.
     */
    public enum LoginAction {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        PASSWORD_CHANGED,
        FIRST_LOGIN,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED
    }
}
