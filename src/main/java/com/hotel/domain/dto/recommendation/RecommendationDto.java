package com.hotel.domain.dto.recommendation;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for hotel recommendation with full explainability.
 * 
 * TRANSPARENCY:
 * - Every recommendation includes scoring breakdown
 * - Users can see exactly WHY a hotel was recommended
 * - No hidden ML models - explicit scoring factors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    
    private Long hotelId;
    private String hotelName;
    private String city;
    private String country;
    private Integer starRating;
    private String heroImageUrl;
    private BigDecimal minPrice;
    private Double averageRating;
    
    /**
     * Overall recommendation score (0-1 scale).
     */
    private Double score;
    
    /**
     * Human-readable primary reason for recommendation.
     * Example: "You've stayed in Chennai before"
     */
    private String primaryReason;
    
    /**
     * Detailed scoring breakdown for transparency.
     */
    private List<ReasonScore> reasonBreakdown;
    
    /**
     * Individual reason scoring component.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasonScore {
        private RecommendationReason reason;
        private String label;
        private Double weight;
    }
}
