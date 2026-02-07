package com.hotel.domain.dto.booking;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for the Booking Confidence & Risk Intelligence Module.
 * Provides transparent, data-driven insights to help users make confident booking decisions.
 */
public class BookingConfidenceDto {

    // ==================== Confidence Level Enum ====================

    /**
     * Confidence level classification.
     * HIGH = Strong indicators for successful booking
     * MEDIUM = Mixed signals, proceed with awareness
     * LOW = Limited availability or high demand period
     */
    public enum ConfidenceLevel {
        HIGH("High confidence", "Great conditions for booking"),
        MEDIUM("Medium confidence", "Good time to book with some considerations"),
        LOW("Lower confidence", "Limited availability - book soon if interested");

        private final String label;
        private final String description;

        ConfidenceLevel(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    // ==================== Main Response DTO ====================

    /**
     * Complete booking confidence assessment response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingConfidenceResponse {
        private Long roomId;
        private Long hotelId;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        
        // Overall confidence assessment
        private ConfidenceLevel confidenceLevel;
        private Integer confidenceScore;  // 0-100
        private String summary;           // Human-readable summary
        
        // Individual signals
        private AvailabilitySignal availability;
        private PriceSignal pricing;
        private DemandSignal demand;
        private BookingSuccessSignal successLikelihood;
        
        // Helpful insights (non-fear-based)
        private List<InsightItem> insights;
        
        // Metadata
        private String disclaimer;
        private Long calculatedAt;  // Unix timestamp
    }

    // ==================== Signal DTOs ====================

    /**
     * Availability pressure signal.
     * Based on current room availability for the requested dates.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilitySignal {
        private String status;           // "Available", "Limited", "Last Room"
        private Integer availableRooms;  // Rooms available of this type
        private Integer totalRooms;      // Total rooms of this type
        private String explanation;      // "5 of 8 rooms available"
        private Integer signalScore;     // 0-100 contribution to confidence
    }

    /**
     * Price stability signal.
     * Based on historical price volatility for this room/period.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceSignal {
        private String status;              // "Stable", "Moderate", "Volatile"
        private BigDecimal currentPrice;
        private BigDecimal averagePrice;    // 30-day average
        private BigDecimal priceTrend;      // % change vs average
        private String explanation;         // "Price is 5% below the 30-day average"
        private Integer signalScore;        // 0-100 contribution to confidence
    }

    /**
     * Demand intensity signal.
     * Based on booking velocity and seasonal trends.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemandSignal {
        private String status;              // "Normal", "Elevated", "Peak"
        private String seasonType;          // "Off-Season", "Regular", "Peak Season"
        private Integer recentBookings;     // Bookings in last 7 days
        private Integer averageBookings;    // Typical bookings for this period
        private String explanation;         // "Normal demand for this time of year"
        private Integer signalScore;        // 0-100 contribution to confidence
    }

    /**
     * Booking success likelihood signal.
     * Based on historical completion rates and cancellation patterns.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingSuccessSignal {
        private String status;              // "Excellent", "Good", "Fair"
        private BigDecimal completionRate;  // Historical completion %
        private BigDecimal cancellationRate;// Historical cancellation %
        private String explanation;         // "95% of bookings at this hotel are completed"
        private Integer signalScore;        // 0-100 contribution to confidence
    }

    // ==================== Insight Item ====================

    /**
     * Individual insight for the user.
     * Always positive/neutral framing - never fear-based.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsightItem {
        private String type;        // "tip", "info", "highlight"
        private String icon;        // Font Awesome icon class
        private String title;       // Short title
        private String message;     // Detailed message
    }

    // ==================== Request DTO ====================

    /**
     * Request for booking confidence assessment.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingConfidenceRequest {
        private Long roomId;
        private Long hotelId;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer numGuests;
    }

    // ==================== Historical Data DTOs ====================

    /**
     * Room availability snapshot for analytics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilitySnapshot {
        private Long roomId;
        private Long hotelId;
        private LocalDate date;
        private Integer availableCount;
        private Integer totalCount;
        private BigDecimal occupancyRate;
    }

    /**
     * Price history entry for volatility analysis.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceHistoryEntry {
        private Long roomId;
        private LocalDate date;
        private BigDecimal price;
        private String priceType;  // "REGULAR", "WEEKEND", "HOLIDAY"
    }

    /**
     * Booking velocity metrics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingVelocity {
        private Long hotelId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Integer bookingCount;
        private BigDecimal dailyAverage;
        private String trend;  // "INCREASING", "STABLE", "DECREASING"
    }

    /**
     * Cancellation pattern summary.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancellationPattern {
        private Long hotelId;
        private Integer totalBookings;
        private Integer cancelledBookings;
        private BigDecimal cancellationRate;
        private Integer avgDaysBeforeCancellation;
        private String primaryReason;  // Most common cancellation reason
    }
}
