package com.hotel.domain.voice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Voice Request DTO
 * Represents a structured voice command from the frontend.
 * The frontend converts raw speech to this structured format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceRequest {
    
    /**
     * The detected intent from voice input
     */
    private VoiceIntent intent;
    
    /**
     * Raw transcript from speech recognition
     */
    private String transcript;
    
    /**
     * Confidence score from speech recognition (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * Language code (e.g., "en-US", "en-IN")
     */
    private String language;
    
    /**
     * Session context for conversation continuity
     */
    private VoiceContext context;
    
    /**
     * Extracted parameters from voice command
     */
    private VoiceParameters parameters;
    
    /**
     * Context object for session management
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceContext {
        private String sessionId;
        private String lastCity;
        private String lastHotelId;
        private String lastRoomId;
        private String lastIntent;
        private String conversationState;
    }
    
    /**
     * Extracted parameters from voice command
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceParameters {
        // Location parameters
        private String city;
        private String country;
        private String hotelName;
        
        // Filter parameters
        private Integer starRating;
        private Double minPrice;
        private Double maxPrice;
        private Integer guests;
        
        // Date parameters
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        
        // Room parameters
        private Long roomId;
        private Long hotelId;
        private String roomType;
        
        // Navigation parameters
        private String navigationTarget;
        
        // Special requests
        private String specialRequests;
    }
}
