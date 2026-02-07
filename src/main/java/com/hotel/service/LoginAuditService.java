package com.hotel.service;

import com.hotel.domain.entity.LoginAuditLog;
import com.hotel.domain.entity.User;
import com.hotel.repository.LoginAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for auditing login events.
 * Records all login attempts for security analysis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAuditService {

    private final LoginAuditLogRepository loginAuditLogRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    /**
     * Record a successful login event.
     */
    @Async
    @Transactional
    public void recordLoginSuccess(User user, String ipAddress, String userAgent, boolean isFirstLogin) {
        log.info("Recording successful login for user: {} (first login: {})", user.getEmail(), isFirstLogin);

        LoginAuditLog.LoginAction action = isFirstLogin 
                ? LoginAuditLog.LoginAction.FIRST_LOGIN 
                : LoginAuditLog.LoginAction.LOGIN_SUCCESS;

        LoginAuditLog auditLog = LoginAuditLog.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .action(action)
                .ipAddress(ipAddress)
                .userAgent(truncateUserAgent(userAgent))
                .isFirstLogin(isFirstLogin)
                .isSuccessful(true)
                .build();

        loginAuditLogRepository.save(auditLog);
    }

    /**
     * Record a failed login attempt.
     */
    @Async
    @Transactional
    public void recordLoginFailure(String email, String ipAddress, String userAgent, String failureReason, Long userId) {
        log.warn("Recording failed login attempt for: {} from IP: {}", email, ipAddress);

        LoginAuditLog auditLog = LoginAuditLog.builder()
                .userId(userId)
                .email(email)
                .action(LoginAuditLog.LoginAction.LOGIN_FAILED)
                .ipAddress(ipAddress)
                .userAgent(truncateUserAgent(userAgent))
                .isFirstLogin(false)
                .isSuccessful(false)
                .failureReason(failureReason)
                .build();

        loginAuditLogRepository.save(auditLog);
    }

    /**
     * Record account lockout event.
     */
    @Async
    @Transactional
    public void recordAccountLocked(User user, String ipAddress, String userAgent) {
        log.warn("Recording account lockout for user: {}", user.getEmail());

        LoginAuditLog auditLog = LoginAuditLog.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .action(LoginAuditLog.LoginAction.ACCOUNT_LOCKED)
                .ipAddress(ipAddress)
                .userAgent(truncateUserAgent(userAgent))
                .isFirstLogin(false)
                .isSuccessful(false)
                .failureReason("Too many failed attempts")
                .build();

        loginAuditLogRepository.save(auditLog);
    }

    /**
     * Record logout event.
     */
    @Async
    @Transactional
    public void recordLogout(User user, String ipAddress) {
        log.info("Recording logout for user: {}", user.getEmail());

        LoginAuditLog auditLog = LoginAuditLog.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .action(LoginAuditLog.LoginAction.LOGOUT)
                .ipAddress(ipAddress)
                .isSuccessful(true)
                .build();

        loginAuditLogRepository.save(auditLog);
    }

    /**
     * Record password change event.
     */
    @Async
    @Transactional
    public void recordPasswordChanged(User user, String ipAddress) {
        log.info("Recording password change for user: {}", user.getEmail());

        LoginAuditLog auditLog = LoginAuditLog.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .action(LoginAuditLog.LoginAction.PASSWORD_CHANGED)
                .ipAddress(ipAddress)
                .isSuccessful(true)
                .build();

        loginAuditLogRepository.save(auditLog);
    }

    /**
     * Count recent failed login attempts for an email.
     */
    @Transactional(readOnly = true)
    public int countRecentFailedAttempts(String email) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        return (int) loginAuditLogRepository.countFailedAttemptsSince(email, since);
    }

    /**
     * Count recent failed login attempts from an IP address.
     */
    @Transactional(readOnly = true)
    public int countRecentFailedAttemptsFromIp(String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        return (int) loginAuditLogRepository.countFailedAttemptsFromIpSince(ipAddress, since);
    }

    /**
     * Check if login should be blocked based on failed attempts.
     */
    @Transactional(readOnly = true)
    public boolean shouldBlockLogin(String email, String ipAddress) {
        int emailFailures = countRecentFailedAttempts(email);
        int ipFailures = countRecentFailedAttemptsFromIp(ipAddress);

        boolean blocked = emailFailures >= MAX_FAILED_ATTEMPTS || ipFailures >= (MAX_FAILED_ATTEMPTS * 2);
        
        if (blocked) {
            log.warn("Login blocked - Email failures: {}, IP failures: {} (email: {}, IP: {})", 
                    emailFailures, ipFailures, email, ipAddress);
        }
        
        return blocked;
    }

    /**
     * Get remaining lockout time in minutes.
     */
    @Transactional(readOnly = true)
    public int getRemainingLockoutMinutes(String email) {
        // For simplicity, return full lockout duration
        // In production, calculate based on last failed attempt time
        return LOCKOUT_DURATION_MINUTES;
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) return null;
        if (userAgent.length() > 500) {
            return userAgent.substring(0, 500);
        }
        return userAgent;
    }
}
