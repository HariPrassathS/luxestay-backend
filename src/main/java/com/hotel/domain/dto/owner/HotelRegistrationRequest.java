package com.hotel.domain.dto.owner;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for new hotel registration by prospective hotel owners.
 * Creates both a Hotel (in PENDING status) and a HOTEL_OWNER user account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRegistrationRequest {

    // ==================== Hotel Information ====================

    @NotBlank(message = "Hotel name is required")
    @Size(max = 255, message = "Hotel name must be at most 255 characters")
    private String hotelName;

    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 120, message = "City must be at most 120 characters")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 120, message = "Country must be at most 120 characters")
    @Builder.Default
    private String country = "India";

    @Size(max = 20, message = "Postal code must be at most 20 characters")
    private String postalCode;

    @Size(max = 20, message = "Hotel phone must be at most 20 characters")
    private String hotelPhone;

    @Email(message = "Invalid hotel email format")
    @Size(max = 255, message = "Hotel email must be at most 255 characters")
    private String hotelEmail;

    @Min(value = 1, message = "Star rating must be between 1 and 5")
    @Max(value = 5, message = "Star rating must be between 1 and 5")
    @Builder.Default
    private Integer starRating = 3;

    @Size(max = 2000, message = "Hero image URL must be at most 2000 characters")
    private String heroImageUrl;

    private String amenities; // JSON string array

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private Double latitude;

    private Double longitude;

    // ==================== Owner Account Information ====================

    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid owner email format")
    @Size(max = 255, message = "Owner email must be at most 255 characters")
    private String ownerEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @Size(max = 20, message = "Owner phone must be at most 20 characters")
    private String ownerPhone;
}
