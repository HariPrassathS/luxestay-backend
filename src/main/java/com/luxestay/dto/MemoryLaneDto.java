package com.luxestay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Memory Lane DTOs
 * "Remember your stay at..." nostalgia triggers
 * 
 * Week 11 Feature: Past stay memories with emotional connection
 */
public class MemoryLaneDto {
    
    /**
     * Past Stay Memory
     */
    public static class PastStay {
        private Long bookingId;
        private Long hotelId;
        private String hotelName;
        private String hotelCity;
        private String hotelImage;
        private Integer hotelStarRating;
        private String roomType;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer nights;
        private BigDecimal totalPaid;
        private String memoryTitle;
        private String timeSinceStay;
        private List<String> highlights;
        private MemoryMood mood;
        private boolean hasReview;
        private Double userRating;
        private String userReviewSnippet;
        private boolean canBookAgain;
        private List<Milestone> stayMilestones;
        
        // Getters and Setters
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getHotelId() { return hotelId; }
        public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
        
        public String getHotelName() { return hotelName; }
        public void setHotelName(String hotelName) { this.hotelName = hotelName; }
        
        public String getHotelCity() { return hotelCity; }
        public void setHotelCity(String hotelCity) { this.hotelCity = hotelCity; }
        
        public String getHotelImage() { return hotelImage; }
        public void setHotelImage(String hotelImage) { this.hotelImage = hotelImage; }
        
        public Integer getHotelStarRating() { return hotelStarRating; }
        public void setHotelStarRating(Integer hotelStarRating) { this.hotelStarRating = hotelStarRating; }
        
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
        
        public LocalDate getCheckIn() { return checkIn; }
        public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
        
        public LocalDate getCheckOut() { return checkOut; }
        public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
        
        public Integer getNights() { return nights; }
        public void setNights(Integer nights) { this.nights = nights; }
        
        public BigDecimal getTotalPaid() { return totalPaid; }
        public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
        
        public String getMemoryTitle() { return memoryTitle; }
        public void setMemoryTitle(String memoryTitle) { this.memoryTitle = memoryTitle; }
        
        public String getTimeSinceStay() { return timeSinceStay; }
        public void setTimeSinceStay(String timeSinceStay) { this.timeSinceStay = timeSinceStay; }
        
        public List<String> getHighlights() { return highlights; }
        public void setHighlights(List<String> highlights) { this.highlights = highlights; }
        
        public MemoryMood getMood() { return mood; }
        public void setMood(MemoryMood mood) { this.mood = mood; }
        
        public boolean isHasReview() { return hasReview; }
        public void setHasReview(boolean hasReview) { this.hasReview = hasReview; }
        
        public Double getUserRating() { return userRating; }
        public void setUserRating(Double userRating) { this.userRating = userRating; }
        
        public String getUserReviewSnippet() { return userReviewSnippet; }
        public void setUserReviewSnippet(String userReviewSnippet) { this.userReviewSnippet = userReviewSnippet; }
        
        public boolean isCanBookAgain() { return canBookAgain; }
        public void setCanBookAgain(boolean canBookAgain) { this.canBookAgain = canBookAgain; }
        
        public List<Milestone> getStayMilestones() { return stayMilestones; }
        public void setStayMilestones(List<Milestone> stayMilestones) { this.stayMilestones = stayMilestones; }
    }
    
    /**
     * Memory Mood - emotional context
     */
    public enum MemoryMood {
        ANNIVERSARY("💍", "Anniversary Trip", "A special celebration"),
        ROMANTIC("❤️", "Romantic Getaway", "Love was in the air"),
        FAMILY("👨‍👩‍👧‍👦", "Family Vacation", "Quality time together"),
        ADVENTURE("🏔️", "Adventure Trip", "Exploring new horizons"),
        RELAXATION("🧘", "Relaxation Retreat", "Peace and tranquility"),
        BUSINESS("💼", "Business Stay", "Work away from home"),
        CELEBRATION("🎉", "Special Occasion", "A time to celebrate"),
        SOLO("✨", "Solo Journey", "Time for yourself"),
        WEEKEND("🌅", "Weekend Escape", "A quick getaway");
        
        private final String emoji;
        private final String displayName;
        private final String description;
        
        MemoryMood(String emoji, String displayName, String description) {
            this.emoji = emoji;
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getEmoji() { return emoji; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Stay Milestone - memorable moments from the stay
     */
    public static class Milestone {
        private String icon;
        private String title;
        private String description;
        
        public Milestone() {}
        
        public Milestone(String icon, String title, String description) {
            this.icon = icon;
            this.title = title;
            this.description = description;
        }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * Memory Lane Response
     */
    public static class MemoryLaneResponse {
        private List<PastStay> memories;
        private MemorySummary summary;
        private String greeting;
        private List<String> suggestions;
        
        // Getters and Setters
        public List<PastStay> getMemories() { return memories; }
        public void setMemories(List<PastStay> memories) { this.memories = memories; }
        
        public MemorySummary getSummary() { return summary; }
        public void setSummary(MemorySummary summary) { this.summary = summary; }
        
        public String getGreeting() { return greeting; }
        public void setGreeting(String greeting) { this.greeting = greeting; }
        
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    }
    
    /**
     * Memory Summary - aggregate stats of all past stays
     */
    public static class MemorySummary {
        private int totalStays;
        private int totalNights;
        private int uniqueDestinations;
        private BigDecimal totalSpent;
        private String mostVisitedCity;
        private String favoriteHotel;
        private LocalDate memberSince;
        private String travelStyle;
        
        // Getters and Setters
        public int getTotalStays() { return totalStays; }
        public void setTotalStays(int totalStays) { this.totalStays = totalStays; }
        
        public int getTotalNights() { return totalNights; }
        public void setTotalNights(int totalNights) { this.totalNights = totalNights; }
        
        public int getUniqueDestinations() { return uniqueDestinations; }
        public void setUniqueDestinations(int uniqueDestinations) { this.uniqueDestinations = uniqueDestinations; }
        
        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
        
        public String getMostVisitedCity() { return mostVisitedCity; }
        public void setMostVisitedCity(String mostVisitedCity) { this.mostVisitedCity = mostVisitedCity; }
        
        public String getFavoriteHotel() { return favoriteHotel; }
        public void setFavoriteHotel(String favoriteHotel) { this.favoriteHotel = favoriteHotel; }
        
        public LocalDate getMemberSince() { return memberSince; }
        public void setMemberSince(LocalDate memberSince) { this.memberSince = memberSince; }
        
        public String getTravelStyle() { return travelStyle; }
        public void setTravelStyle(String travelStyle) { this.travelStyle = travelStyle; }
    }
    
    /**
     * Helper to calculate time since stay
     */
    public static String calculateTimeSince(LocalDate checkOut) {
        if (checkOut == null) return "Unknown";
        
        LocalDate now = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(checkOut, now);
        
        if (daysBetween < 0) {
            return "Upcoming";
        } else if (daysBetween == 0) {
            return "Today";
        } else if (daysBetween == 1) {
            return "Yesterday";
        } else if (daysBetween < 7) {
            return daysBetween + " days ago";
        } else if (daysBetween < 14) {
            return "1 week ago";
        } else if (daysBetween < 30) {
            return (daysBetween / 7) + " weeks ago";
        } else if (daysBetween < 60) {
            return "1 month ago";
        } else if (daysBetween < 365) {
            return (daysBetween / 30) + " months ago";
        } else if (daysBetween < 730) {
            return "1 year ago";
        } else {
            return (daysBetween / 365) + " years ago";
        }
    }
    
    /**
     * Generate memory title based on stay context
     */
    public static String generateMemoryTitle(String hotelName, String city, LocalDate checkIn, int nights) {
        if (checkIn == null) {
            return "Your stay at " + hotelName;
        }
        
        int month = checkIn.getMonthValue();
        String season;
        
        if (month >= 3 && month <= 5) {
            season = "spring";
        } else if (month >= 6 && month <= 8) {
            season = "summer";
        } else if (month >= 9 && month <= 11) {
            season = "autumn";
        } else {
            season = "winter";
        }
        
        if (nights == 1) {
            return "One night in " + city;
        } else if (nights == 2) {
            return "A weekend in " + city;
        } else if (nights <= 4) {
            return "Your " + season + " escape to " + city;
        } else if (nights <= 7) {
            return "A week at " + hotelName;
        } else {
            return "Your extended stay in " + city;
        }
    }
}
