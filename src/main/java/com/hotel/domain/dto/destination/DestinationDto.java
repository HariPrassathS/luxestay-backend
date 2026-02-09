package com.hotel.domain.dto.destination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for destination information in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationDto {

    private Long id;
    private String city;
    private String imageUrl;
    private String region;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime updatedAt;
}
