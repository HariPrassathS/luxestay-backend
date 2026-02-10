package com.luxestay.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VIP Concierge DTOs
 * Premium support access and elevated service
 * 
 * Week 12 Feature: VIP guest experience management
 */
public class VIPConciergeDto {
    
    /**
     * VIP Status Response
     */
    public static class VIPStatus {
        private boolean isVIP;
        private VIPTier tier;
        private int totalBookings;
        private int totalNights;
        private int loyaltyPoints;
        private List<VIPBenefit> activeBenefits;
        private List<VIPService> availableServices;
        private ConciergeContact conciergeContact;
        private String personalGreeting;
        
        // Getters and Setters
        public boolean isVIP() { return isVIP; }
        public void setVIP(boolean isVIP) { this.isVIP = isVIP; }
        
        public VIPTier getTier() { return tier; }
        public void setTier(VIPTier tier) { this.tier = tier; }
        
        public int getTotalBookings() { return totalBookings; }
        public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }
        
        public int getTotalNights() { return totalNights; }
        public void setTotalNights(int totalNights) { this.totalNights = totalNights; }
        
        public int getLoyaltyPoints() { return loyaltyPoints; }
        public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
        
        public List<VIPBenefit> getActiveBenefits() { return activeBenefits; }
        public void setActiveBenefits(List<VIPBenefit> activeBenefits) { this.activeBenefits = activeBenefits; }
        
        public List<VIPService> getAvailableServices() { return availableServices; }
        public void setAvailableServices(List<VIPService> availableServices) { this.availableServices = availableServices; }
        
        public ConciergeContact getConciergeContact() { return conciergeContact; }
        public void setConciergeContact(ConciergeContact conciergeContact) { this.conciergeContact = conciergeContact; }
        
        public String getPersonalGreeting() { return personalGreeting; }
        public void setPersonalGreeting(String personalGreeting) { this.personalGreeting = personalGreeting; }
    }
    
    /**
     * VIP Tier Levels
     */
    public enum VIPTier {
        STANDARD("Standard", "🌟", "Starting your journey", 0, 0),
        SILVER("Silver", "🥈", "Valued guest", 3, 7),
        GOLD("Gold", "🥇", "Preferred guest", 7, 20),
        PLATINUM("Platinum", "💎", "Elite traveler", 15, 50),
        DIAMOND("Diamond", "👑", "Legendary status", 30, 100);
        
        private final String displayName;
        private final String icon;
        private final String description;
        private final int minBookings;
        private final int minNights;
        
        VIPTier(String displayName, String icon, String description, int minBookings, int minNights) {
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
            this.minBookings = minBookings;
            this.minNights = minNights;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
        public int getMinBookings() { return minBookings; }
        public int getMinNights() { return minNights; }
        
        /**
         * Calculate tier based on bookings and nights
         */
        public static VIPTier calculateTier(int bookings, int nights) {
            if (bookings >= DIAMOND.minBookings && nights >= DIAMOND.minNights) {
                return DIAMOND;
            } else if (bookings >= PLATINUM.minBookings && nights >= PLATINUM.minNights) {
                return PLATINUM;
            } else if (bookings >= GOLD.minBookings && nights >= GOLD.minNights) {
                return GOLD;
            } else if (bookings >= SILVER.minBookings && nights >= SILVER.minNights) {
                return SILVER;
            }
            return STANDARD;
        }
    }
    
    /**
     * VIP Benefit
     */
    public static class VIPBenefit {
        private String id;
        private String icon;
        private String title;
        private String description;
        private boolean unlocked;
        private VIPTier requiredTier;
        
        public VIPBenefit() {}
        
        public VIPBenefit(String id, String icon, String title, String description, boolean unlocked, VIPTier requiredTier) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.unlocked = unlocked;
            this.requiredTier = requiredTier;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isUnlocked() { return unlocked; }
        public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
        
        public VIPTier getRequiredTier() { return requiredTier; }
        public void setRequiredTier(VIPTier requiredTier) { this.requiredTier = requiredTier; }
    }
    
    /**
     * VIP Service
     */
    public static class VIPService {
        private String id;
        private String icon;
        private String name;
        private String description;
        private boolean available;
        private String actionUrl;
        private String actionText;
        
        public VIPService() {}
        
        public VIPService(String id, String icon, String name, String description, boolean available, String actionUrl, String actionText) {
            this.id = id;
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.available = available;
            this.actionUrl = actionUrl;
            this.actionText = actionText;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        
        public String getActionText() { return actionText; }
        public void setActionText(String actionText) { this.actionText = actionText; }
    }
    
    /**
     * Concierge Contact Information
     */
    public static class ConciergeContact {
        private String name;
        private String title;
        private String email;
        private String phone;
        private String availability;
        private String responseTime;
        
        public ConciergeContact() {}
        
        public ConciergeContact(String name, String title, String email, String phone, String availability, String responseTime) {
            this.name = name;
            this.title = title;
            this.email = email;
            this.phone = phone;
            this.availability = availability;
            this.responseTime = responseTime;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getAvailability() { return availability; }
        public void setAvailability(String availability) { this.availability = availability; }
        
        public String getResponseTime() { return responseTime; }
        public void setResponseTime(String responseTime) { this.responseTime = responseTime; }
    }
    
    /**
     * Support Request DTO
     */
    public static class SupportRequest {
        private Long id;
        private Long userId;
        private Long bookingId;
        private String type;
        private String subject;
        private String message;
        private String status;
        private String priority;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    /**
     * Progress to next tier
     */
    public static class TierProgress {
        private VIPTier currentTier;
        private VIPTier nextTier;
        private int currentBookings;
        private int bookingsToNext;
        private int currentNights;
        private int nightsToNext;
        private double progressPercentage;
        private String message;
        
        // Getters and Setters
        public VIPTier getCurrentTier() { return currentTier; }
        public void setCurrentTier(VIPTier currentTier) { this.currentTier = currentTier; }
        
        public VIPTier getNextTier() { return nextTier; }
        public void setNextTier(VIPTier nextTier) { this.nextTier = nextTier; }
        
        public int getCurrentBookings() { return currentBookings; }
        public void setCurrentBookings(int currentBookings) { this.currentBookings = currentBookings; }
        
        public int getBookingsToNext() { return bookingsToNext; }
        public void setBookingsToNext(int bookingsToNext) { this.bookingsToNext = bookingsToNext; }
        
        public int getCurrentNights() { return currentNights; }
        public void setCurrentNights(int currentNights) { this.currentNights = currentNights; }
        
        public int getNightsToNext() { return nightsToNext; }
        public void setNightsToNext(int nightsToNext) { this.nightsToNext = nightsToNext; }
        
        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
