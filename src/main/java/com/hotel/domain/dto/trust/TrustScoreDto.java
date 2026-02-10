package com.hotel.domain.dto.trust;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TrustScore DTO - Represents the trust metrics for a hotel.
 * 
 * TrustScore is a deterministic, explainable metric (0-100) based on:
 * - Verified stay reviews
 * - Rating consistency
 * - Review recency
 * - Hotel response rate
 * 
 * NO machine learning. NO opaque scoring.
 * Every component is visible and understandable to the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreDto {
    
    /**
     * Overall trust score (0-100)
     */
    private Integer score;
    
    /**
     * Trust level label: "Excellent", "Very Good", "Good", "Fair", "New"
     */
    private String level;
    
    /**
     * Total number of verified stay reviews
     */
    private Integer verifiedReviewCount;
    
    /**
     * Average rating from verified reviews (1.0 - 5.0)
     */
    private Double averageRating;
    
    /**
     * Top 3 praise highlights from reviews (what guests love)
     */
    private List<PraiseDto> topPraises;
    
    /**
     * Top 2 concern areas (honest transparency)
     */
    private List<ConcernDto> topConcerns;
    
    /**
     * Breakdown of how the score is calculated (transparency)
     */
    private ScoreBreakdownDto breakdown;
    
    /**
     * Whether this hotel has enough data for a reliable score
     */
    private Boolean hasEnoughData;
    
    /**
     * Human-readable explanation of the score
     */
    private String explanation;
    
    /**
     * Praise DTO - What guests consistently love
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PraiseDto {
        private String category;      // e.g., "Location", "Cleanliness", "Service"
        private String description;   // e.g., "Guests consistently praise the central location"
        private Integer mentionCount; // How many reviews mentioned this
        private String icon;          // FontAwesome icon class
    }
    
    /**
     * Concern DTO - Areas that some guests noted
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConcernDto {
        private String category;      // e.g., "Noise", "Parking"
        private String description;   // e.g., "Some guests mentioned street noise"
        private Integer mentionCount; // How many reviews mentioned this
        private String severity;      // "minor", "moderate" (never "severe" - we wouldn't list severe)
        private String icon;          // FontAwesome icon class
    }
    
    /**
     * Score breakdown - Shows exactly how the score is calculated
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreBreakdownDto {
        private ComponentScore reviewVolume;      // Points from number of reviews (0-25)
        private ComponentScore ratingQuality;     // Points from average rating (0-35)
        private ComponentScore consistency;       // Points from rating consistency (0-20)
        private ComponentScore recency;           // Points from recent reviews (0-10)
        private ComponentScore hotelEngagement;   // Points from hotel responses (0-10)
    }
    
    /**
     * Individual score component
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentScore {
        private String name;          // Human-readable name
        private Integer points;       // Points earned
        private Integer maxPoints;    // Maximum possible points
        private String explanation;   // Why this score
    }
}
