package com.hotel.mapper;

import com.hotel.domain.dto.user.UserDto;
import com.hotel.domain.entity.Role;
import com.hotel.domain.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for User entity and DTOs.
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserDto.
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto.UserDtoBuilder builder = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .mustChangePassword(user.getMustChangePassword())
                .lastLoginAt(user.getLastLoginAt())
                .isFirstLogin(user.isFirstTimeLogin())
                .firstLoginCompleted(user.getFirstLoginCompleted());

        // Include hotel information for HOTEL_OWNER
        if (Role.HOTEL_OWNER.equals(user.getRole())) {
            builder.hotelId(user.getHotelId());
            if (user.getManagedHotel() != null) {
                builder.hotelName(user.getManagedHotel().getName());
            }
        }

        return builder.build();
    }

    /**
     * Convert User entity to UserDto with hotel name.
     * Use this when you have the hotel name available separately.
     */
    public UserDto toDto(User user, String hotelName) {
        if (user == null) {
            return null;
        }

        UserDto dto = toDto(user);
        if (Role.HOTEL_OWNER.equals(user.getRole())) {
            dto.setHotelName(hotelName);
        }
        return dto;
    }
}
