package com.hotel.domain.dto.owner;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for hotel owners to update their hotel information.
 * Restricted fields compared to admin hotel update.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerHotelUpdateRequest {

    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 500, message = "Hero image URL must be at most 500 characters")
    private String heroImageUrl;

    private String amenities; // JSON string array

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    // Note: star_rating, name, city, country are NOT editable by owner
    // These require admin approval to change
}
