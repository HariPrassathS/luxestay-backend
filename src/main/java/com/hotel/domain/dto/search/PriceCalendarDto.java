package com.hotel.domain.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for price calendar response.
 * Contains daily prices for a hotel within a date range.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceCalendarDto {

    private Long hotelId;
    private String hotelName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Builder.Default
    private List<DayPrice> prices = new ArrayList<>();
    
    private BigDecimal lowestPrice;
    private BigDecimal highestPrice;
    private BigDecimal averagePrice;
    
    // Price tier thresholds for color coding
    private BigDecimal lowPriceThreshold;  // Below this = "deal"
    private BigDecimal highPriceThreshold; // Above this = "premium"

    /**
     * Single day price entry.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayPrice {
        private LocalDate date;
        private BigDecimal price;
        private boolean available;
        private int availableRooms;
        private String priceTier; // "low", "medium", "high"
        private String dayOfWeek;
        private boolean isWeekend;
    }
}
