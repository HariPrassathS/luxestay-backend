package com.hotel.domain.dto.hotel;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTOs for the Mood-Based Hotel Finder feature.
 * Enables intelligent, data-driven hotel recommendations based on travel mood.
 */
public class MoodFinderDto {

    /**
     * Supported travel moods for hotel discovery.
     */
    public enum MoodType {
        ROMANTIC_GETAWAY("Romantic Getaway", "Perfect for couples seeking intimate experiences", "fa-heart"),
        ADVENTURE("Adventure", "For thrill-seekers and outdoor enthusiasts", "fa-mountain"),
        RELAXATION("Relaxation", "Unwind and rejuvenate in peaceful settings", "fa-spa"),
        FAMILY_FUN("Family Fun", "Create lasting memories with loved ones", "fa-users"),
        BUSINESS("Business", "Productive stays with professional amenities", "fa-briefcase");

        private final String displayName;
        private final String description;
        private final String icon;

        MoodType(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    /**
     * Request to find hotels matching a specific mood.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodSearchRequest {
        private MoodType mood;
        private String location;       // Optional: filter by city/country
        private Integer minStars;      // Optional: minimum star rating
        private BigDecimal maxPrice;   // Optional: maximum price per night
        private Integer limit;         // Optional: max results (default 10)
    }

    /**
     * Response containing mood-matched hotels with explanations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodSearchResponse {
        private MoodType mood;
        private String moodDescription;
        private int totalMatches;
        private List<MoodHotelMatch> hotels;
        private MoodCriteria criteriaUsed;
    }

    /**
     * A hotel matching the selected mood with scoring details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodHotelMatch {
        private Long hotelId;
        private String hotelName;
        private String city;
        private String country;
        private Integer starRating;
        private String heroImageUrl;
        private BigDecimal minPrice;
        private String currency;
        
        // Mood matching details
        private int matchScore;              // 0-100 score
        private String matchLevel;           // EXCELLENT, GOOD, FAIR
        private List<String> matchReasons;   // Why this hotel matches
        private Map<String, Integer> scoreBreakdown; // Detailed scoring
        
        // Supporting data
        private List<String> relevantAmenities;  // Amenities that contributed to match
        private List<String> relevantRoomTypes;  // Room types that fit the mood
    }

    /**
     * Criteria used for mood matching - for transparency.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodCriteria {
        private List<String> primaryAmenities;    // Must-have amenities
        private List<String> bonusAmenities;      // Nice-to-have amenities
        private List<String> preferredRoomTypes;  // Ideal room types
        private List<String> locationKeywords;    // Location indicators
        private List<String> descriptionKeywords; // Description signals
        private Integer minimumStars;             // Suggested minimum rating
        private String scoringNotes;              // How scoring works
    }

    /**
     * Summary of available moods with counts.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodSummary {
        private MoodType mood;
        private String displayName;
        private String description;
        private String icon;
        private int matchingHotelsCount;
        private String topMatchPreview;  // Name of best matching hotel
    }

    /**
     * Response listing all available moods.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodListResponse {
        private List<MoodSummary> moods;
        private int totalHotels;
    }
}
