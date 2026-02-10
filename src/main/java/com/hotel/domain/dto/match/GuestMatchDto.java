package com.hotel.domain.dto.match;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Guest Match - Personalized Recommendations
 * 
 * "Guests like you enjoyed..."
 * 
 * Provides personalized hotel and room recommendations
 * based on user preferences, booking history, and behavior
 */
public class GuestMatchDto {
    
    private Long userId;
    private String guestProfile;
    private List<MatchedHotel> recommendedHotels = new ArrayList<>();
    private List<MatchedRoom> recommendedRooms = new ArrayList<>();
    private List<String> matchFactors = new ArrayList<>();
    private MatchConfidence confidence;
    
    // Match confidence levels
    public enum MatchConfidence {
        HIGH("Highly Personalized", "Based on your preferences and booking history"),
        MEDIUM("Personalized", "Based on your browsing behavior"),
        LOW("Popular Picks", "Based on similar guests");
        
        private final String label;
        private final String description;
        
        MatchConfidence(String label, String description) {
            this.label = label;
            this.description = description;
        }
        
        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }
    
    // Matched hotel recommendation
    public static class MatchedHotel {
        private Long hotelId;
        private String name;
        private String city;
        private String imageUrl;
        private Double rating;
        private int reviewCount;
        private BigDecimal startingPrice;
        private int matchScore; // 0-100
        private List<String> matchReasons = new ArrayList<>();
        private MatchType matchType;
        
        public enum MatchType {
            SIMILAR_PREFERENCE("Similar to hotels you've booked"),
            WISHLIST_MATCH("Matches your wishlist preferences"),
            LOCATION_BASED("In your favorite destinations"),
            RATING_MATCH("Highly rated like your past choices"),
            PRICE_RANGE("Within your typical budget"),
            AMENITY_MATCH("Has amenities you love");
            
            private final String reason;
            
            MatchType(String reason) {
                this.reason = reason;
            }
            
            public String getReason() { return reason; }
        }
        
        public MatchedHotel() {}
        
        // Getters and Setters
        public Long getHotelId() { return hotelId; }
        public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public int getReviewCount() { return reviewCount; }
        public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
        
        public BigDecimal getStartingPrice() { return startingPrice; }
        public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
        
        public int getMatchScore() { return matchScore; }
        public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
        
        public List<String> getMatchReasons() { return matchReasons; }
        public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }
        
        public MatchType getMatchType() { return matchType; }
        public void setMatchType(MatchType matchType) { this.matchType = matchType; }
    }
    
    // Matched room recommendation
    public static class MatchedRoom {
        private Long roomId;
        private Long hotelId;
        private String hotelName;
        private String roomType;
        private String description;
        private String imageUrl;
        private BigDecimal pricePerNight;
        private int matchScore;
        private List<String> matchReasons = new ArrayList<>();
        
        public MatchedRoom() {}
        
        // Getters and Setters
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        
        public Long getHotelId() { return hotelId; }
        public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
        
        public String getHotelName() { return hotelName; }
        public void setHotelName(String hotelName) { this.hotelName = hotelName; }
        
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public BigDecimal getPricePerNight() { return pricePerNight; }
        public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
        
        public int getMatchScore() { return matchScore; }
        public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
        
        public List<String> getMatchReasons() { return matchReasons; }
        public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }
    }
    
    // Guest preference profile
    public static class GuestPreferences {
        private BigDecimal avgPriceRange;
        private List<String> preferredCities = new ArrayList<>();
        private List<String> preferredRoomTypes = new ArrayList<>();
        private Double preferredMinRating;
        private List<String> preferredAmenities = new ArrayList<>();
        private int totalBookings;
        
        public BigDecimal getAvgPriceRange() { return avgPriceRange; }
        public void setAvgPriceRange(BigDecimal avgPriceRange) { this.avgPriceRange = avgPriceRange; }
        
        public List<String> getPreferredCities() { return preferredCities; }
        public void setPreferredCities(List<String> preferredCities) { this.preferredCities = preferredCities; }
        
        public List<String> getPreferredRoomTypes() { return preferredRoomTypes; }
        public void setPreferredRoomTypes(List<String> preferredRoomTypes) { this.preferredRoomTypes = preferredRoomTypes; }
        
        public Double getPreferredMinRating() { return preferredMinRating; }
        public void setPreferredMinRating(Double preferredMinRating) { this.preferredMinRating = preferredMinRating; }
        
        public List<String> getPreferredAmenities() { return preferredAmenities; }
        public void setPreferredAmenities(List<String> preferredAmenities) { this.preferredAmenities = preferredAmenities; }
        
        public int getTotalBookings() { return totalBookings; }
        public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }
    }
    
    // Factory method
    public static GuestMatchDto forUser(Long userId) {
        GuestMatchDto dto = new GuestMatchDto();
        dto.setUserId(userId);
        return dto;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getGuestProfile() { return guestProfile; }
    public void setGuestProfile(String guestProfile) { this.guestProfile = guestProfile; }
    
    public List<MatchedHotel> getRecommendedHotels() { return recommendedHotels; }
    public void setRecommendedHotels(List<MatchedHotel> recommendedHotels) { this.recommendedHotels = recommendedHotels; }
    
    public List<MatchedRoom> getRecommendedRooms() { return recommendedRooms; }
    public void setRecommendedRooms(List<MatchedRoom> recommendedRooms) { this.recommendedRooms = recommendedRooms; }
    
    public List<String> getMatchFactors() { return matchFactors; }
    public void setMatchFactors(List<String> matchFactors) { this.matchFactors = matchFactors; }
    
    public MatchConfidence getConfidence() { return confidence; }
    public void setConfidence(MatchConfidence confidence) { this.confidence = confidence; }
}
