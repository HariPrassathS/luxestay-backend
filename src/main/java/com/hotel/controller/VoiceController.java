package com.hotel.controller;

import com.hotel.domain.voice.VoiceIntent;
import com.hotel.domain.voice.VoiceRequest;
import com.hotel.domain.voice.VoiceResponse;
import com.hotel.service.VoiceAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Voice Controller - REST API Endpoints for Voice Assistant
 * 
 * This controller provides all voice assistant functionality through REST endpoints.
 * Frontend sends processed voice commands (transcript + detected intent) here.
 * Backend validates, processes, and returns structured responses.
 */
@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Voice Assistant", description = "Voice-controlled hotel search and booking API")
public class VoiceController {
    
    private final VoiceAssistantService voiceAssistantService;
    
    /**
     * Main voice command endpoint
     * Processes voice commands and returns structured responses
     * 
     * @param request VoiceRequest containing transcript and detected intent
     * @return VoiceResponse with action, speech text, and data
     */
    @PostMapping("/command")
    @Operation(summary = "Process voice command", 
               description = "Main endpoint for processing voice commands. " +
                           "Accepts transcript and intent, returns structured response with speech output.")
    public ResponseEntity<VoiceResponse> processCommand(@RequestBody VoiceRequest request) {
        log.info("Received voice command: intent={}, transcript={}", 
                request.getIntent(), request.getTranscript());
        
        try {
            // If intent is not detected by frontend, detect it here
            if (request.getIntent() == null || request.getIntent() == VoiceIntent.UNKNOWN) {
                VoiceIntent detectedIntent = voiceAssistantService.detectIntent(request.getTranscript());
                request.setIntent(detectedIntent);
                log.info("Detected intent: {}", detectedIntent);
            }
            
            // Extract parameters if not provided
            if (request.getParameters() == null && request.getTranscript() != null) {
                request.setParameters(voiceAssistantService.extractParameters(request.getTranscript()));
            }
            
            VoiceResponse response = voiceAssistantService.processVoiceCommand(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing voice command", e);
            return ResponseEntity.ok(VoiceResponse.fallback(
                "I'm sorry, I couldn't process that command. Please try again or use text search.",
                List.of("Help", "Search hotels", "Go to home")
            ));
        }
    }
    
    /**
     * Quick search by voice - simplified endpoint for hotel searches
     */
    @PostMapping("/search")
    @Operation(summary = "Voice hotel search", 
               description = "Simplified endpoint for voice-based hotel searches")
    public ResponseEntity<VoiceResponse> voiceSearch(@RequestBody Map<String, Object> searchParams) {
        log.info("Received voice search request: {}", searchParams);
        
        String city = (String) searchParams.getOrDefault("city", "");
        String type = (String) searchParams.getOrDefault("type", "general");
        Integer stars = searchParams.containsKey("stars") ? 
                ((Number) searchParams.get("stars")).intValue() : null;
        Double maxPrice = searchParams.containsKey("maxPrice") ? 
                ((Number) searchParams.get("maxPrice")).doubleValue() : null;
        
        VoiceIntent intent = switch (type.toLowerCase()) {
            case "luxury" -> VoiceIntent.SEARCH_LUXURY;
            case "budget" -> VoiceIntent.SEARCH_BUDGET;
            default -> VoiceIntent.SEARCH_HOTELS;
        };
        
        VoiceRequest request = VoiceRequest.builder()
                .intent(intent)
                .transcript(city + " hotels")
                .parameters(VoiceRequest.VoiceParameters.builder()
                        .city(city.isEmpty() ? null : city)
                        .starRating(stars)
                        .maxPrice(maxPrice)
                        .build())
                .build();
        
        return ResponseEntity.ok(voiceAssistantService.processVoiceCommand(request));
    }
    
    /**
     * Detect intent from raw transcript
     * Useful for frontend to pre-process before full command processing
     */
    @PostMapping("/detect-intent")
    @Operation(summary = "Detect intent from transcript", 
               description = "Analyzes transcript and returns detected intent with confidence")
    public ResponseEntity<Map<String, Object>> detectIntent(@RequestBody Map<String, String> body) {
        String transcript = body.getOrDefault("transcript", "");
        
        if (transcript.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Transcript is required"
            ));
        }
        
        VoiceIntent intent = voiceAssistantService.detectIntent(transcript);
        VoiceRequest.VoiceParameters params = voiceAssistantService.extractParameters(transcript);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "intent", intent.name(),
            "description", intent.getDescription(),
            "requiresAuth", intent.requiresAuthentication(),
            "isSearch", intent.isSearchIntent(),
            "isBooking", intent.isBookingIntent(),
            "isNavigation", intent.isNavigationIntent(),
            "parameters", params != null ? params : Map.of()
        ));
    }
    
    /**
     * Get voice command suggestions/examples
     */
    @GetMapping("/suggestions")
    @Operation(summary = "Get voice command suggestions", 
               description = "Returns example voice commands for different categories")
    public ResponseEntity<Map<String, Object>> getSuggestions() {
        return ResponseEntity.ok(Map.of(
            "categories", List.of(
                Map.of(
                    "name", "Hotel Search",
                    "icon", "search",
                    "commands", List.of(
                        "Find hotels in Chennai",
                        "Show luxury hotels",
                        "Budget hotels near me",
                        "5-star hotels in Mumbai"
                    )
                ),
                Map.of(
                    "name", "Filters",
                    "icon", "filter",
                    "commands", List.of(
                        "Hotels under 5000 rupees",
                        "4-star hotels",
                        "Rooms for 2 guests",
                        "Show family rooms"
                    )
                ),
                Map.of(
                    "name", "Booking",
                    "icon", "calendar",
                    "commands", List.of(
                        "Book a room",
                        "Check availability",
                        "My bookings",
                        "Cancel my booking"
                    )
                ),
                Map.of(
                    "name", "Navigation",
                    "icon", "compass",
                    "commands", List.of(
                        "Go to home",
                        "Show map",
                        "Open my profile",
                        "Go back"
                    )
                )
            ),
            "quickCommands", List.of(
                "Hotels in Chennai",
                "Luxury hotels",
                "Book a room",
                "Help"
            )
        ));
    }
    
    /**
     * Health check for voice service
     */
    @GetMapping("/health")
    @Operation(summary = "Voice service health check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "voice-assistant",
            "version", "1.0.0",
            "features", List.of(
                "hotel-search",
                "voice-booking",
                "navigation",
                "price-filter",
                "star-rating-filter"
            )
        ));
    }
    
    /**
     * Get supported languages
     */
    @GetMapping("/languages")
    @Operation(summary = "Get supported languages for voice recognition")
    public ResponseEntity<Map<String, Object>> getLanguages() {
        return ResponseEntity.ok(Map.of(
            "default", "en-US",
            "supported", List.of(
                Map.of("code", "en-US", "name", "English (US)", "flag", "🇺🇸"),
                Map.of("code", "en-IN", "name", "English (India)", "flag", "🇮🇳"),
                Map.of("code", "en-GB", "name", "English (UK)", "flag", "🇬🇧")
            )
        ));
    }
}
