package com.hotel.domain.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chatbot request DTO.
 * Sent from frontend to backend for query processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {
    
    /**
     * The detected intent from frontend (lightweight detection)
     */
    private String intent;
    
    /**
     * The user's original message
     */
    private String message;
    
    /**
     * Session context for conversation continuity
     */
    private ChatbotContext context;
    
    /**
     * Get intent as enum type
     */
    public IntentType getIntentType() {
        return IntentType.fromString(intent);
    }
    
    /**
     * Context object for session management
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatbotContext {
        private String sessionId;
        private String lastCity;
        private String lastIntent;
    }
}
