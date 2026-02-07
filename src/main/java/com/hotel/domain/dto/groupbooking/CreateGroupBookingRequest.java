package com.hotel.domain.dto.groupbooking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a group booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupBookingRequest {
    
    @NotBlank(message = "Group name is required")
    private String name;
    
    @NotNull(message = "Hotel ID is required")
    private Long hotelId;
    
    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;
    
    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;
    
    @Min(value = 2, message = "Minimum 2 participants required")
    @Max(value = 50, message = "Maximum 50 participants allowed")
    private Integer maxParticipants;
    
    private String notes;
    
    private LocalDateTime joinDeadline;
}
