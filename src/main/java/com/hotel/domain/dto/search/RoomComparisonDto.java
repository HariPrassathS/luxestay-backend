package com.hotel.domain.dto.search;

import com.hotel.domain.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for room comparison response.
 * Contains comparable data for multiple rooms.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomComparisonDto {

    private Long hotelId;
    private String hotelName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int nights;

    @Builder.Default
    private List<RoomCompareItem> rooms = new ArrayList<>();
    
    @Builder.Default
    private List<String> comparisonFields = List.of(
        "price", "capacity", "size", "view", "bedType", "amenities"
    );

    /**
     * Single room comparison item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomCompareItem {
        private Long id;
        private String name;
        private RoomType type;
        private String description;
        private BigDecimal pricePerNight;
        private BigDecimal totalPrice;
        private int maxGuests;
        private Integer sizeSqMeters;
        private String bedType;
        private String viewType;
        private String imageUrl;
        private boolean available;
        
        @Builder.Default
        private List<String> amenities = new ArrayList<>();
        
        @Builder.Default
        private List<String> highlights = new ArrayList<>();
        
        // Comparison scores (0-100)
        private int valueScore;
        private int spaceScore;
        private int amenityScore;
    }
}
