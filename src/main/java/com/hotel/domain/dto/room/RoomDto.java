package com.hotel.domain.dto.room;

import com.hotel.domain.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for room information in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private String roomNumber;
    private String name;
    private String description;
    private RoomType roomType;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String bedType;
    private BigDecimal sizeSqm;
    private String imageUrl;
    private List<String> amenities;
    private Boolean isAvailable;
    
    /** Timestamp of last update - used for cache-busting on frontend */
    private LocalDateTime updatedAt;
}
