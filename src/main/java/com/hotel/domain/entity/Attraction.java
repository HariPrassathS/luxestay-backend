package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Attraction entity representing tourist attractions, restaurants, activities
 * near hotel locations for smart itinerary generation.
 */
@Entity
@Table(name = "attractions", indexes = {
    @Index(name = "idx_attractions_city", columnList = "city"),
    @Index(name = "idx_attractions_category", columnList = "category"),
    @Index(name = "idx_attractions_coordinates", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 120)
    private String country;

    @Column(length = 255)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /**
     * Category of attraction: LANDMARK, RESTAURANT, ACTIVITY, NATURE, CULTURAL, SHOPPING, ENTERTAINMENT
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttractionCategory category;

    /**
     * Best time of day to visit: MORNING, AFTERNOON, EVENING, ANY
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "best_time", nullable = false, length = 20)
    @lombok.Builder.Default
    private TimeOfDay bestTime = TimeOfDay.ANY;

    /**
     * Estimated duration in minutes to experience this attraction
     */
    @Column(name = "duration_minutes", nullable = false)
    @lombok.Builder.Default
    private Integer durationMinutes = 60;

    /**
     * Mood tags as JSON array: ["ROMANTIC_GETAWAY", "RELAXATION"]
     */
    @Column(name = "mood_tags", columnDefinition = "JSON")
    private String moodTags;

    /**
     * Rating out of 5.0
     */
    @Column(precision = 2)
    @lombok.Builder.Default
    private Double rating = 4.0;

    /**
     * Approximate price level: 1 (budget) to 4 (luxury)
     */
    @Column(name = "price_level")
    @lombok.Builder.Default
    private Integer priceLevel = 2;

    /**
     * Image URL for the attraction
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Opening hours (e.g., "09:00-18:00")
     */
    @Column(name = "opening_hours", length = 50)
    private String openingHours;

    /**
     * Whether the attraction is currently active/available
     */
    @Column(name = "is_active", nullable = false)
    @lombok.Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Enums ====================

    public enum AttractionCategory {
        LANDMARK("Landmark", "Historic sites and monuments"),
        RESTAURANT("Restaurant", "Dining experiences"),
        ACTIVITY("Activity", "Tours and experiences"),
        NATURE("Nature", "Parks and natural attractions"),
        CULTURAL("Cultural", "Museums and galleries"),
        SHOPPING("Shopping", "Markets and shopping districts"),
        ENTERTAINMENT("Entertainment", "Shows and nightlife"),
        WELLNESS("Wellness", "Spas and wellness centers"),
        BEACH("Beach", "Beaches and water activities"),
        TEMPLE("Temple", "Religious and spiritual sites");

        private final String displayName;
        private final String description;

        AttractionCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public enum TimeOfDay {
        MORNING("Morning", "Best visited in the morning"),
        AFTERNOON("Afternoon", "Best visited in the afternoon"),
        EVENING("Evening", "Best visited in the evening"),
        ANY("Any Time", "Can be visited any time");

        private final String displayName;
        private final String description;

        TimeOfDay(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
