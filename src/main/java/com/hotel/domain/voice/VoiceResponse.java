package com.hotel.domain.voice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Voice Response DTO
 * Structured response for voice assistant.
 * Includes both display data and speech output.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceResponse {
    
    /**
     * Whether the command was processed successfully
     */
    private boolean success;
    
    /**
     * The intent that was processed
     */
    private VoiceIntent intent;
    
    /**
     * Text to be spoken back to the user (TTS output)
     */
    private String speechText;
    
    /**
     * Display message (can contain HTML/formatting)
     */
    private String displayMessage;
    
    /**
     * Structured data (hotels, rooms, booking details, etc.)
     */
    private Map<String, Object> data;
    
    /**
     * Voice action to perform
     */
    private VoiceAction action;
    
    /**
     * Follow-up suggestions (spoken and displayed)
     */
    private List<String> suggestions;
    
    /**
     * Whether clarification is needed
     */
    private Boolean needsClarification;
    
    /**
     * Clarification question to ask
     */
    private String clarificationQuestion;
    
    /**
     * Options for clarification
     */
    private List<String> clarificationOptions;
    
    /**
     * Confidence score
     */
    private Double confidence;
    
    /**
     * Whether this is a fallback response
     */
    private Boolean fallback;
    
    /**
     * Next conversation state
     */
    private String nextState;
    
    /**
     * Voice action enum
     */
    public enum VoiceAction {
        DISPLAY_RESULTS,    // Show search results
        NAVIGATE,           // Navigate to a page
        CONFIRM_BOOKING,    // Ask for booking confirmation
        COMPLETE_BOOKING,   // Booking completed
        REQUEST_LOGIN,      // User needs to login
        CLARIFY,            // Need more information
        SPEAK_ONLY,         // Just speak, no UI change
        SHOW_DETAILS,       // Show hotel/room details
        APPLY_FILTER,       // Apply search filter
        CANCEL_ACTION       // Cancel current action
    }
    
    // ========== FACTORY METHODS ==========
    
    /**
     * Create a successful response with results
     */
    public static VoiceResponse success(VoiceIntent intent, String speechText, 
            String displayMessage, Map<String, Object> data, VoiceAction action,
            List<String> suggestions) {
        return VoiceResponse.builder()
                .success(true)
                .intent(intent)
                .speechText(speechText)
                .displayMessage(displayMessage)
                .data(data)
                .action(action)
                .suggestions(suggestions)
                .confidence(0.95)
                .build();
    }
    
    /**
     * Create a clarification response
     */
    public static VoiceResponse needsClarification(VoiceIntent intent, 
            String question, List<String> options) {
        return VoiceResponse.builder()
                .success(true)
                .intent(intent)
                .needsClarification(true)
                .speechText(question)
                .displayMessage(question)
                .clarificationQuestion(question)
                .clarificationOptions(options)
                .suggestions(options)
                .action(VoiceAction.CLARIFY)
                .build();
    }
    
    /**
     * Create an error/fallback response
     */
    public static VoiceResponse fallback(String message, List<String> suggestions) {
        return VoiceResponse.builder()
                .success(false)
                .fallback(true)
                .speechText(message)
                .displayMessage(message)
                .suggestions(suggestions)
                .action(VoiceAction.SPEAK_ONLY)
                .build();
    }
    
    /**
     * Create a navigation response
     */
    public static VoiceResponse navigate(String destination, String speechText) {
        return VoiceResponse.builder()
                .success(true)
                .intent(VoiceIntent.NAVIGATE)
                .speechText(speechText)
                .displayMessage(speechText)
                .action(VoiceAction.NAVIGATE)
                .data(Map.of("destination", destination))
                .build();
    }
    
    /**
     * Create a login required response
     */
    public static VoiceResponse requiresLogin(String action) {
        return VoiceResponse.builder()
                .success(false)
                .speechText("You need to be logged in to " + action + ". Would you like to login now?")
                .displayMessage("Please login to " + action)
                .action(VoiceAction.REQUEST_LOGIN)
                .suggestions(List.of("Login", "Cancel"))
                .build();
    }
}
