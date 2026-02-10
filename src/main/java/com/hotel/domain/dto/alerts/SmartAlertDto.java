package com.hotel.domain.dto.alerts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Smart Alerts
 * 
 * Alert Types:
 * - PRICE_DROP: Price decreased for a saved/viewed hotel
 * - AVAILABILITY: Room became available for dates
 * - WISHLIST: Updates for wishlisted hotels
 * - BOOKING_REMINDER: Incomplete booking reminder
 * 
 * Design Philosophy:
 * - Helpful, not spammy
 * - Real data only
 * - User controls frequency
 */
public class SmartAlertDto {
    
    private Long id;
    private Long userId;
    private AlertType type;
    private AlertPriority priority;
    private String title;
    private String message;
    private AlertData data;
    private boolean read;
    private boolean dismissed;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    public enum AlertType {
        PRICE_DROP("Price Drop", "fa-tag"),
        AVAILABILITY("Now Available", "fa-calendar-check"),
        WISHLIST("Wishlist Update", "fa-heart"),
        BOOKING_REMINDER("Continue Booking", "fa-shopping-cart"),
        LOW_AVAILABILITY("Limited Rooms", "fa-exclamation-circle"),
        DEAL("Special Deal", "fa-percent");
        
        private final String label;
        private final String icon;
        
        AlertType(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }
        
        public String getLabel() { return label; }
        public String getIcon() { return icon; }
    }
    
    public enum AlertPriority {
        LOW(1),
        MEDIUM(2),
        HIGH(3);
        
        private final int level;
        
        AlertPriority(int level) {
            this.level = level;
        }
        
        public int getLevel() { return level; }
    }
    
    /**
     * Alert-specific data payload
     */
    public static class AlertData {
        private Long hotelId;
        private String hotelName;
        private String hotelImage;
        private Long roomId;
        private String roomName;
        private BigDecimal originalPrice;
        private BigDecimal currentPrice;
        private BigDecimal savings;
        private Double savingsPercent;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private int availableRooms;
        private String actionUrl;
        private String actionText;
        
        // Getters and setters
        public Long getHotelId() { return hotelId; }
        public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
        
        public String getHotelName() { return hotelName; }
        public void setHotelName(String hotelName) { this.hotelName = hotelName; }
        
        public String getHotelImage() { return hotelImage; }
        public void setHotelImage(String hotelImage) { this.hotelImage = hotelImage; }
        
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        
        public BigDecimal getSavings() { return savings; }
        public void setSavings(BigDecimal savings) { this.savings = savings; }
        
        public Double getSavingsPercent() { return savingsPercent; }
        public void setSavingsPercent(Double savingsPercent) { this.savingsPercent = savingsPercent; }
        
        public LocalDate getCheckInDate() { return checkInDate; }
        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
        
        public LocalDate getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
        
        public int getAvailableRooms() { return availableRooms; }
        public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
        
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        
        public String getActionText() { return actionText; }
        public void setActionText(String actionText) { this.actionText = actionText; }
    }
    
    // Factory methods
    public static SmartAlertDto priceDropAlert(Long userId, Long hotelId, String hotelName,
                                               BigDecimal oldPrice, BigDecimal newPrice) {
        SmartAlertDto alert = new SmartAlertDto();
        alert.userId = userId;
        alert.type = AlertType.PRICE_DROP;
        alert.priority = AlertPriority.HIGH;
        
        BigDecimal savings = oldPrice.subtract(newPrice);
        double savingsPercent = savings.divide(oldPrice, 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue();
        
        alert.title = "Price dropped at " + hotelName;
        alert.message = String.format("Save $%.0f (%.0f%% off) on your next stay", 
            savings.doubleValue(), savingsPercent);
        
        AlertData data = new AlertData();
        data.setHotelId(hotelId);
        data.setHotelName(hotelName);
        data.setOriginalPrice(oldPrice);
        data.setCurrentPrice(newPrice);
        data.setSavings(savings);
        data.setSavingsPercent(savingsPercent);
        data.setActionUrl("hotel-detail.html?id=" + hotelId);
        data.setActionText("View Deal");
        alert.data = data;
        
        alert.createdAt = LocalDateTime.now();
        alert.expiresAt = LocalDateTime.now().plusDays(7);
        
        return alert;
    }
    
    public static SmartAlertDto availabilityAlert(Long userId, Long hotelId, String hotelName,
                                                  LocalDate checkIn, LocalDate checkOut, 
                                                  int availableRooms) {
        SmartAlertDto alert = new SmartAlertDto();
        alert.userId = userId;
        alert.type = AlertType.AVAILABILITY;
        alert.priority = availableRooms <= 2 ? AlertPriority.HIGH : AlertPriority.MEDIUM;
        alert.title = "Rooms available at " + hotelName;
        alert.message = availableRooms == 1 
            ? "Only 1 room left for your dates" 
            : availableRooms + " rooms now available for your dates";
        
        AlertData data = new AlertData();
        data.setHotelId(hotelId);
        data.setHotelName(hotelName);
        data.setCheckInDate(checkIn);
        data.setCheckOutDate(checkOut);
        data.setAvailableRooms(availableRooms);
        data.setActionUrl(String.format("hotel-detail.html?id=%d&checkIn=%s&checkOut=%s",
            hotelId, checkIn, checkOut));
        data.setActionText("Book Now");
        alert.data = data;
        
        alert.createdAt = LocalDateTime.now();
        alert.expiresAt = checkIn.atStartOfDay();
        
        return alert;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }
    
    public AlertPriority getPriority() { return priority; }
    public void setPriority(AlertPriority priority) { this.priority = priority; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public AlertData getData() { return data; }
    public void setData(AlertData data) { this.data = data; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    
    public boolean isDismissed() { return dismissed; }
    public void setDismissed(boolean dismissed) { this.dismissed = dismissed; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
