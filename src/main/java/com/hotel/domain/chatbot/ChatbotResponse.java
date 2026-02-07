package com.hotel.domain.chatbot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chatbot response DTO.
 * Structured response from backend to frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatbotResponse {
    
    /**
     * Whether the query was processed successfully
     */
    private boolean success;
    
    /**
     * The intent that was processed
     */
    private String intent;
    
    /**
     * Human-readable message to display
     */
    private String message;
    
    /**
     * Structured data (hotels, city info, etc.)
     */
    private Map<String, Object> data;
    
    /**
     * Quick reply suggestions for the user
     */
    private List<String> quickReplies;
    
    /**
     * Confidence score (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * Whether this is a fallback response
     */
    private Boolean fallback;
    
    /**
     * Static content for fallback mode
     */
    private Map<String, Object> staticContent;
    
    /**
     * Create a successful response
     */
    public static ChatbotResponse success(String intent, String message, Map<String, Object> data, List<String> quickReplies) {
        return ChatbotResponse.builder()
                .success(true)
                .intent(intent)
                .message(message)
                .data(data)
                .quickReplies(quickReplies)
                .confidence(0.95)
                .build();
    }
    
    /**
     * Create an error/fallback response
     */
    public static ChatbotResponse fallback(String message, List<String> quickReplies) {
        return ChatbotResponse.builder()
                .success(false)
                .fallback(true)
                .message(message)
                .quickReplies(quickReplies)
                .staticContent(Map.of(
                    "popularCities", List.of("Chennai", "Madurai", "Ooty", "Pondicherry"),
                    "helpText", "You can browse our hotels page directly for real-time availability."
                ))
                .build();
    }
}
