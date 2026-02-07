package com.hotel.domain.dto.owner;

import com.hotel.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for hotel owner information including their hotel details.
 * Used in admin views for managing hotel owners.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelOwnerDto {

    // User information
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private LocalDateTime createdAt;

    // Hotel information
    private Long hotelId;
    private String hotelName;
    private String hotelCity;
    private String hotelApprovalStatus;
    private Boolean hotelIsActive;

    // Convenience method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
