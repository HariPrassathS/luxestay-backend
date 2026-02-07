package com.hotel.domain.dto.room;

import com.hotel.domain.entity.RoomType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating or updating a room.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    private String roomNumber;

    @Size(max = 150, message = "Room name must not exceed 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.01", message = "Price per night must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal pricePerNight;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Capacity must not exceed 10")
    private Integer capacity;

    @Size(max = 50, message = "Bed type must not exceed 50 characters")
    private String bedType;

    @DecimalMin(value = "1.00", message = "Size must be at least 1 square meter")
    @Digits(integer = 4, fraction = 2, message = "Size format is invalid")
    private BigDecimal sizeSqm;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    private List<String> amenities;

    @lombok.Builder.Default
    private Boolean isAvailable = true;
}
