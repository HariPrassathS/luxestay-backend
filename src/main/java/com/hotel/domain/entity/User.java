package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User entity representing both customers and administrators.
 * Implements UserDetails for Spring Security integration.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(name = "referral_code", unique = true, length = 20)
    private String referralCode;

    @Column(name = "referred_by_code", length = 20)
    private String referredByCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    @lombok.Builder.Default
    private Boolean isActive = true;

    // ==================== Hotel Owner Specific Fields ====================

    /**
     * The hotel ID this user owns (only for HOTEL_OWNER role).
     * Maps to the existing hotel_id column in the database.
     */
    @Column(name = "hotel_id")
    private Long hotelId;

    /**
     * The hotel entity this user owns (lazy loaded).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel managedHotel;

    /**
     * Flag indicating if the user must change their password on next login.
     * Used for auto-generated hotel owner accounts.
     */
    @Column(name = "must_change_password", nullable = false)
    @lombok.Builder.Default
    private Boolean mustChangePassword = false;

    /**
     * Timestamp of last password change.
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // ==================== Login Tracking Fields ====================

    /**
     * Timestamp of last successful login.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Total number of successful logins.
     */
    @Column(name = "login_count", nullable = false)
    @lombok.Builder.Default
    private Integer loginCount = 0;

    /**
     * Number of consecutive failed login attempts.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @lombok.Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * Account locked until this timestamp (for rate limiting).
     */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /**
     * Flag indicating if user has completed first login setup.
     * Used to trigger first-time login email and password setup flow.
     */
    @Column(name = "first_login_completed", nullable = false)
    @lombok.Builder.Default
    private Boolean firstLoginCompleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== UserDetails Implementation ====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Check if account is active and not temporarily locked
        if (!isActive) {
            return false;
        }
        // Check if locked due to failed attempts
        if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) {
            role = Role.USER;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Helper Methods ====================

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isHotelOwner() {
        return Role.HOTEL_OWNER.equals(this.role);
    }

    /**
     * Check if this is the user's first login (never logged in before).
     */
    public boolean isFirstTimeLogin() {
        return lastLoginAt == null || !Boolean.TRUE.equals(firstLoginCompleted);
    }

    /**
     * Check if account is currently locked due to failed attempts.
     */
    public boolean isTemporarilyLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    /**
     * Check if this hotel owner can access the specified hotel.
     * Returns true only if the user is a HOTEL_OWNER and owns the given hotel.
     */
    public boolean ownsHotel(Long checkHotelId) {
        return isHotelOwner() && hotelId != null && hotelId.equals(checkHotelId);
    }

    /**
     * Check if user has management access (ADMIN or HOTEL_OWNER).
     */
    public boolean hasManagementAccess() {
        return isAdmin() || isHotelOwner();
    }
}
