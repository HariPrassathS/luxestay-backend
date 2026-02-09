package com.hotel.domain.dto.destination;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a destination.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDestinationRequest {

    @NotBlank(message = "City name is required")
    @Size(max = 120, message = "City name must not exceed 120 characters")
    private String city;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @Size(max = 50, message = "Region must not exceed 50 characters")
    private String region;

    private Boolean isActive;
    
    private Integer sortOrder;
}
