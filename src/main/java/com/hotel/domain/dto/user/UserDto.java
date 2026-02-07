package com.hotel.domain.dto.user;

import com.hotel.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user information in API responses.
 * Excludes sensitive data like password hash.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long bookingsCount;
    
    // Hotel Owner specific fields
    private Long hotelId;
    private String hotelName;
    private Boolean mustChangePassword;
    
    // Login tracking fields
    private LocalDateTime lastLoginAt;
    private Boolean isFirstLogin;
    private Boolean firstLoginCompleted;

    /**
     * Get full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
