package com.hotel.domain.dto.countdown;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Stay Countdown - Journey Progress & Milestones
 * 
 * Tracks:
 * - Days until check-in
 * - Journey milestones
 * - Preparation reminders
 * - Excitement builders
 */
public class StayCountdownDto {
    
    private Long bookingId;
    private Long hotelId;
    private String hotelName;
    private String hotelCity;
    private String hotelImageUrl;
    
    // Countdown details
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int daysUntilCheckIn;
    private int nightsBooked;
    
    // Countdown status
    private CountdownPhase phase;
    private String phaseMessage;
    private String excitementMessage;
    
    // Milestones
    private List<Milestone> milestones = new ArrayList<>();
    private Milestone nextMilestone;
    private int completedMilestones;
    
    // Weather preview (optional)
    private WeatherPreview weatherPreview;
    
    // Packing suggestions based on destination
    private List<PackingSuggestion> packingSuggestions = new ArrayList<>();
    
    // Phase enum
    public enum CountdownPhase {
        JUST_BOOKED("Just booked", "Your adventure begins!"),
        FAR_AWAY("Months away", "Plenty of time to plan"),
        WEEKS_AWAY("Weeks away", "Time to start preparing"),
        DAYS_AWAY("Days away", "Almost there!"),
        TOMORROW("Tomorrow", "Get ready!"),
        TODAY("Today", "It's here!"),
        IN_PROGRESS("Currently staying", "Enjoy your stay!"),
        COMPLETED("Completed", "Hope you had a wonderful stay!");
        
        private final String label;
        private final String message;
        
        CountdownPhase(String label, String message) {
            this.label = label;
            this.message = message;
        }
        
        public String getLabel() { return label; }
        public String getMessage() { return message; }
    }
    
    // Milestone class
    public static class Milestone {
        private String id;
        private String title;
        private String description;
        private String icon;
        private boolean completed;
        private int daysBeforeCheckIn;
        private MilestoneType type;
        
        public enum MilestoneType {
            BOOKING, PREPARATION, REMINDER, EXCITEMENT, ARRIVAL
        }
        
        public Milestone() {}
        
        public Milestone(String id, String title, String description, String icon, 
                        int daysBeforeCheckIn, MilestoneType type) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.daysBeforeCheckIn = daysBeforeCheckIn;
            this.type = type;
            this.completed = false;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        
        public int getDaysBeforeCheckIn() { return daysBeforeCheckIn; }
        public void setDaysBeforeCheckIn(int daysBeforeCheckIn) { this.daysBeforeCheckIn = daysBeforeCheckIn; }
        
        public MilestoneType getType() { return type; }
        public void setType(MilestoneType type) { this.type = type; }
    }
    
    // Weather preview
    public static class WeatherPreview {
        private String condition;
        private String icon;
        private int avgHighTemp;
        private int avgLowTemp;
        private String recommendation;
        
        public WeatherPreview() {}
        
        public WeatherPreview(String condition, String icon, int avgHighTemp, int avgLowTemp, String recommendation) {
            this.condition = condition;
            this.icon = icon;
            this.avgHighTemp = avgHighTemp;
            this.avgLowTemp = avgLowTemp;
            this.recommendation = recommendation;
        }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public int getAvgHighTemp() { return avgHighTemp; }
        public void setAvgHighTemp(int avgHighTemp) { this.avgHighTemp = avgHighTemp; }
        
        public int getAvgLowTemp() { return avgLowTemp; }
        public void setAvgLowTemp(int avgLowTemp) { this.avgLowTemp = avgLowTemp; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }
    
    // Packing suggestion
    public static class PackingSuggestion {
        private String item;
        private String reason;
        private String category;
        private boolean essential;
        
        public PackingSuggestion() {}
        
        public PackingSuggestion(String item, String reason, String category, boolean essential) {
            this.item = item;
            this.reason = reason;
            this.category = category;
            this.essential = essential;
        }
        
        public String getItem() { return item; }
        public void setItem(String item) { this.item = item; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public boolean isEssential() { return essential; }
        public void setEssential(boolean essential) { this.essential = essential; }
    }
    
    // Factory method
    public static StayCountdownDto forBooking(Long bookingId, Long hotelId, String hotelName,
                                              LocalDate checkInDate, LocalDate checkOutDate) {
        StayCountdownDto dto = new StayCountdownDto();
        dto.setBookingId(bookingId);
        dto.setHotelId(hotelId);
        dto.setHotelName(hotelName);
        dto.setCheckInDate(checkInDate);
        dto.setCheckOutDate(checkOutDate);
        return dto;
    }
    
    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    
    public String getHotelCity() { return hotelCity; }
    public void setHotelCity(String hotelCity) { this.hotelCity = hotelCity; }
    
    public String getHotelImageUrl() { return hotelImageUrl; }
    public void setHotelImageUrl(String hotelImageUrl) { this.hotelImageUrl = hotelImageUrl; }
    
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public int getDaysUntilCheckIn() { return daysUntilCheckIn; }
    public void setDaysUntilCheckIn(int daysUntilCheckIn) { this.daysUntilCheckIn = daysUntilCheckIn; }
    
    public int getNightsBooked() { return nightsBooked; }
    public void setNightsBooked(int nightsBooked) { this.nightsBooked = nightsBooked; }
    
    public CountdownPhase getPhase() { return phase; }
    public void setPhase(CountdownPhase phase) { this.phase = phase; }
    
    public String getPhaseMessage() { return phaseMessage; }
    public void setPhaseMessage(String phaseMessage) { this.phaseMessage = phaseMessage; }
    
    public String getExcitementMessage() { return excitementMessage; }
    public void setExcitementMessage(String excitementMessage) { this.excitementMessage = excitementMessage; }
    
    public List<Milestone> getMilestones() { return milestones; }
    public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }
    
    public Milestone getNextMilestone() { return nextMilestone; }
    public void setNextMilestone(Milestone nextMilestone) { this.nextMilestone = nextMilestone; }
    
    public int getCompletedMilestones() { return completedMilestones; }
    public void setCompletedMilestones(int completedMilestones) { this.completedMilestones = completedMilestones; }
    
    public WeatherPreview getWeatherPreview() { return weatherPreview; }
    public void setWeatherPreview(WeatherPreview weatherPreview) { this.weatherPreview = weatherPreview; }
    
    public List<PackingSuggestion> getPackingSuggestions() { return packingSuggestions; }
    public void setPackingSuggestions(List<PackingSuggestion> packingSuggestions) { this.packingSuggestions = packingSuggestions; }
}
