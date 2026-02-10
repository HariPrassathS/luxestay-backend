package com.hotel.domain.dto.pulse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Hotel Live Pulse Data
 * 
 * Shows:
 * - Recent booking activity
 * - Current interest/viewers
 * - Booking momentum
 * - Availability status
 * 
 * Design Philosophy:
 * - Real data only, no fabrication
 * - Calm, not pushy
 * - Builds trust through transparency
 */
public class LivePulseDto {
    
    private Long hotelId;
    private PulseLevel pulseLevel;
    private int recentBookings24h;
    private int recentBookings7d;
    private int recentViews24h;
    private int availableRooms;
    private int totalRooms;
    private Double occupancyRate;
    private TrendDirection bookingTrend;
    private List<ActivityItem> recentActivity;
    private LocalDateTime lastUpdated;
    private boolean isPopular;
    private String popularReason;
    
    // Pulse levels (calm, not alarming)
    public enum PulseLevel {
        QUIET("Quiet", "Low activity, great time to book", 1),
        STEADY("Steady", "Regular interest", 2),
        ACTIVE("Active", "Healthy booking activity", 3),
        POPULAR("Popular", "High demand", 4),
        TRENDING("Trending", "Very popular right now", 5);
        
        private final String label;
        private final String description;
        private final int level;
        
        PulseLevel(String label, String description, int level) {
            this.label = label;
            this.description = description;
            this.level = level;
        }
        
        public String getLabel() { return label; }
        public String getDescription() { return description; }
        public int getLevel() { return level; }
    }
    
    public enum TrendDirection {
        UP("Increasing"),
        STABLE("Stable"),
        DOWN("Decreasing");
        
        private final String label;
        
        TrendDirection(String label) {
            this.label = label;
        }
        
        public String getLabel() { return label; }
    }
    
    /**
     * Activity item for the recent activity feed
     * Shows anonymized booking activity
     */
    public static class ActivityItem {
        private String type; // "BOOKING", "VIEW_SPIKE"
        private String message;
        private String timeAgo;
        private LocalDateTime timestamp;
        
        public ActivityItem() {}
        
        public ActivityItem(String type, String message, String timeAgo, LocalDateTime timestamp) {
            this.type = type;
            this.message = message;
            this.timeAgo = timeAgo;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getTimeAgo() { return timeAgo; }
        public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    // Factory method
    public static LivePulseDto forHotel(Long hotelId) {
        LivePulseDto dto = new LivePulseDto();
        dto.hotelId = hotelId;
        dto.lastUpdated = LocalDateTime.now();
        return dto;
    }
    
    // Convenience method for quick badge
    public static LivePulseDto quickBadge(Long hotelId, PulseLevel level, int bookings24h) {
        LivePulseDto dto = forHotel(hotelId);
        dto.pulseLevel = level;
        dto.recentBookings24h = bookings24h;
        return dto;
    }
    
    // Getters and setters
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    
    public PulseLevel getPulseLevel() { return pulseLevel; }
    public void setPulseLevel(PulseLevel pulseLevel) { this.pulseLevel = pulseLevel; }
    
    public int getRecentBookings24h() { return recentBookings24h; }
    public void setRecentBookings24h(int recentBookings24h) { this.recentBookings24h = recentBookings24h; }
    
    public int getRecentBookings7d() { return recentBookings7d; }
    public void setRecentBookings7d(int recentBookings7d) { this.recentBookings7d = recentBookings7d; }
    
    public int getRecentViews24h() { return recentViews24h; }
    public void setRecentViews24h(int recentViews24h) { this.recentViews24h = recentViews24h; }
    
    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    
    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
    
    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }
    
    public TrendDirection getBookingTrend() { return bookingTrend; }
    public void setBookingTrend(TrendDirection bookingTrend) { this.bookingTrend = bookingTrend; }
    
    public List<ActivityItem> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<ActivityItem> recentActivity) { this.recentActivity = recentActivity; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }
    
    public String getPopularReason() { return popularReason; }
    public void setPopularReason(String popularReason) { this.popularReason = popularReason; }
}
