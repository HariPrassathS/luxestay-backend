package com.hotel.domain.dto.itinerary;

import com.hotel.domain.dto.hotel.MoodFinderDto.MoodType;
import com.hotel.domain.entity.Attraction.AttractionCategory;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs for the Trip Intelligence & Smart Itinerary Generator.
 * Provides explainable, data-driven day-by-day trip itineraries.
 */
public class ItineraryDto {

    // ==================== Request DTOs ====================

    /**
     * Request to generate an itinerary for a booking.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItineraryRequest {
        private Long bookingId;
        private MoodType travelMood;           // Optional: user's travel mood preference
        private Integer maxActivitiesPerDay;   // Optional: limit activities (default: 3-4)
        private boolean includeRestTime;       // Include hotel relaxation time
        private List<String> excludeCategories; // Categories to exclude
    }

    // ==================== Response DTOs ====================

    /**
     * Complete trip itinerary response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItineraryResponse {
        private Long bookingId;
        private String bookingReference;
        private TripContext tripContext;
        private List<DayPlan> dayPlans;
        private ItinerarySummary summary;
        private List<String> tips;
        private String generatedAt;
    }

    /**
     * Context information about the trip.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripContext {
        private String hotelName;
        private String hotelAddress;
        private String city;
        private String country;
        private Double hotelLatitude;
        private Double hotelLongitude;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer totalNights;
        private Integer totalDays;
        private String checkInTime;
        private String checkOutTime;
        private MoodType travelMood;
        private List<String> hotelAmenities;
    }

    /**
     * A single day's itinerary plan.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayPlan {
        private Integer dayNumber;
        private LocalDate date;
        private String dayLabel;               // "Day 1 - Arrival", "Day 2 - Exploration"
        private String theme;                  // "Cultural Discovery", "Beach & Relaxation"
        private List<ScheduledActivity> activities;
        private HotelTime hotelTime;           // Suggested hotel relaxation
        private DaySummary daySummary;
    }

    /**
     * A scheduled activity within a day.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduledActivity {
        private Long attractionId;
        private String name;
        private String description;
        private AttractionCategory category;
        private String categoryDisplay;
        private TimeSlot timeSlot;             // MORNING, AFTERNOON, EVENING
        private String suggestedTime;          // "09:00 - 11:00"
        private Integer durationMinutes;
        private Double distanceFromHotel;      // in kilometers
        private String distanceDisplay;        // "2.5 km from hotel"
        private Integer travelTimeMinutes;     // estimated travel time
        private String travelTimeDisplay;      // "10 min drive"
        private String address;
        private Double rating;
        private Integer priceLevel;
        private String priceLevelDisplay;      // "₹", "₹₹", "₹₹₹", "₹₹₹₹"
        private String imageUrl;
        private String openingHours;
        private String whyRecommended;         // Explainable reason
        private List<String> moodTags;
    }

    /**
     * Time slot within a day.
     */
    public enum TimeSlot {
        MORNING("Morning", "08:00 - 12:00", "Perfect for outdoor activities and sightseeing"),
        AFTERNOON("Afternoon", "12:00 - 17:00", "Ideal for indoor attractions and dining"),
        EVENING("Evening", "17:00 - 21:00", "Great for restaurants and entertainment");

        private final String displayName;
        private final String timeRange;
        private final String description;

        TimeSlot(String displayName, String timeRange, String description) {
            this.displayName = displayName;
            this.timeRange = timeRange;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getTimeRange() { return timeRange; }
        public String getDescription() { return description; }
    }

    /**
     * Suggested hotel relaxation time.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelTime {
        private String label;                  // "Afternoon Rest", "Spa Time"
        private String suggestedTime;          // "14:00 - 16:00"
        private String suggestion;             // "Enjoy the hotel pool and spa facilities"
        private List<String> relevantAmenities;
    }

    /**
     * Summary statistics for a day.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaySummary {
        private Integer totalActivities;
        private Integer totalDurationMinutes;
        private Double totalDistanceKm;
        private String intensityLevel;         // "Relaxed", "Moderate", "Active"
    }

    /**
     * Overall itinerary summary.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItinerarySummary {
        private Integer totalActivities;
        private Integer uniqueCategories;
        private Double totalDistanceKm;
        private Map<String, Integer> categoryBreakdown;  // Category -> count
        private String overallMoodMatch;                 // "Excellent", "Good", "Fair"
        private Double averageRating;
    }

    // ==================== Attraction Display DTOs ====================

    /**
     * Simplified attraction for listing.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttractionSummary {
        private Long id;
        private String name;
        private String description;
        private String category;
        private String categoryIcon;
        private Double rating;
        private Integer priceLevel;
        private String imageUrl;
        private Double distanceKm;
        private List<String> moodTags;
    }

    /**
     * Available attractions for a city.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityAttractionsResponse {
        private String city;
        private Integer totalAttractions;
        private Map<String, Integer> categoryBreakdown;
        private List<AttractionSummary> topAttractions;
    }
}
