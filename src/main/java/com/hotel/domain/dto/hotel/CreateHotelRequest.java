package com.hotel.domain.dto.hotel;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO for creating or updating a hotel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHotelRequest {

    @NotBlank(message = "Hotel name is required")
    @Size(max = 255, message = "Hotel name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 120, message = "City must not exceed 120 characters")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 120, message = "Country must not exceed 120 characters")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating must not exceed 5")
    @lombok.Builder.Default
    private Integer starRating = 3;

    @Size(max = 500, message = "Hero image URL must not exceed 500 characters")
    private String heroImageUrl;

    private List<String> amenities;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;
}
