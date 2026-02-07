package com.hotel.controller;

import com.hotel.domain.chatbot.ChatbotRequest;
import com.hotel.domain.chatbot.ChatbotResponse;
import com.hotel.service.ChatbotService;
import com.hotel.service.TravelKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Chatbot Controller - REST API Endpoints
 * 
 * This controller provides all chatbot functionality through REST endpoints.
 * Frontend should ONLY communicate through these endpoints - no business logic on frontend.
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot", description = "AI Travel Concierge Chatbot API")
public class ChatbotController {
    
    private final ChatbotService chatbotService;
    private final TravelKnowledgeService knowledgeService;
    
    /**
     * Main query endpoint - processes all chatbot messages
     * 
     * @param request The chatbot request containing intent and message
     * @return ChatbotResponse with processed result
     */
    @PostMapping("/query")
    @Operation(summary = "Process chatbot query", description = "Main endpoint for chatbot interaction")
    public ResponseEntity<ChatbotResponse> processQuery(@RequestBody ChatbotRequest request) {
        log.info("Received chatbot query: {}", request.getMessage());
        
        try {
            ChatbotResponse response = chatbotService.processQuery(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chatbot query", e);
            return ResponseEntity.ok(ChatbotResponse.fallback(
                "Sorry, I encountered an error. Please try again.",
                List.of("Hotels in Chennai", "Help")
            ));
        }
    }
    
    /**
     * Quick search endpoint - for hotel searches
     */
    @PostMapping("/search")
    @Operation(summary = "Quick hotel search", description = "Search hotels by city or criteria")
    public ResponseEntity<ChatbotResponse> searchHotels(@RequestBody Map<String, Object> searchParams) {
        log.info("Received hotel search request: {}", searchParams);
        
        String city = (String) searchParams.getOrDefault("city", "");
        String type = (String) searchParams.getOrDefault("type", "general");
        
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage(city);
        
        switch (type.toLowerCase()) {
            case "luxury":
                request.setIntent("LUXURY_SEARCH");
                break;
            case "budget":
                request.setIntent("BUDGET_SEARCH");
                break;
            default:
                request.setIntent("HOTEL_SEARCH");
        }
        
        return ResponseEntity.ok(chatbotService.processQuery(request));
    }
    
    /**
     * Get quick reply suggestions
     */
    @GetMapping("/suggestions")
    @Operation(summary = "Get suggestions", description = "Get quick reply suggestions for chatbot")
    public ResponseEntity<Map<String, Object>> getSuggestions() {
        return ResponseEntity.ok(Map.of(
            "suggestions", chatbotService.getSuggestions(),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Get all available cities
     */
    @GetMapping("/cities")
    @Operation(summary = "Get cities", description = "Get all available cities with hotels or info")
    public ResponseEntity<Map<String, Object>> getCities() {
        return ResponseEntity.ok(Map.of(
            "cities", chatbotService.getAllCities(),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Calculate distance between two cities
     */
    @GetMapping("/distance")
    @Operation(summary = "Get distance", description = "Calculate distance between two cities")
    public ResponseEntity<Map<String, Object>> getDistance(
            @RequestParam String from,
            @RequestParam String to) {
        
        log.info("Distance query: {} to {}", from, to);
        
        var distance = chatbotService.getDistance(from, to);
        
        if (distance == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", String.format("Distance data not available for %s to %s", from, to)
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "from", from,
            "to", to,
            "distance", distance.getKm(),
            "travelTime", distance.getTravelTime(),
            "route", distance.getRoute()
        ));
    }
    
    /**
     * Get city information
     */
    @GetMapping("/city/{cityName}")
    @Operation(summary = "Get city info", description = "Get detailed information about a city")
    public ResponseEntity<Map<String, Object>> getCityInfo(@PathVariable String cityName) {
        var cityInfo = knowledgeService.getCityInfo(cityName);
        
        if (cityInfo == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", String.format("City '%s' not found in knowledge base", cityName)
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "city", cityInfo
        ));
    }
    
    /**
     * Get travel packages
     */
    @GetMapping("/packages")
    @Operation(summary = "Get packages", description = "Get all travel packages")
    public ResponseEntity<Map<String, Object>> getPackages(
            @RequestParam(required = false) String city) {
        
        List<TravelKnowledgeService.TravelPackage> packages = city != null ?
                knowledgeService.getPackagesByCity(city) :
                knowledgeService.getAllPackages();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "packages", packages,
            "count", packages.size()
        ));
    }
    
    /**
     * Health check for chatbot service
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if chatbot service is running")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "chatbot",
            "timestamp", System.currentTimeMillis(),
            "version", "1.0.0"
        ));
    }
    
    /**
     * Detect intent from message (lightweight endpoint for frontend)
     */
    @PostMapping("/detect-intent")
    @Operation(summary = "Detect intent", description = "Detect intent from user message")
    public ResponseEntity<Map<String, Object>> detectIntent(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        
        if (message == null || message.isBlank()) {
            return ResponseEntity.ok(Map.of(
                "intent", "GENERAL_QUERY",
                "confidence", 0.0
            ));
        }
        
        String intent = detectIntentFromMessage(message);
        String city = knowledgeService.extractCity(message);
        
        return ResponseEntity.ok(Map.of(
            "intent", intent,
            "confidence", calculateConfidence(message, intent),
            "extractedCity", city != null ? city : "",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    // ========== HELPER METHODS ==========
    
    private String detectIntentFromMessage(String message) {
        String lower = message.toLowerCase();
        
        if (lower.matches(".*(hi|hello|hey|greetings|good morning|good evening).*")) {
            return "GREETING";
        }
        if (lower.contains("help") || lower.contains("what can you")) {
            return "HELP";
        }
        if (lower.contains("luxury") || lower.contains("premium") || lower.contains("5 star")) {
            return "LUXURY_SEARCH";
        }
        if (lower.contains("cheap") || lower.contains("budget") || lower.contains("affordable") || lower.contains("under")) {
            return "BUDGET_SEARCH";
        }
        if (lower.contains("hotel") || lower.contains("stay") || lower.contains("room") || lower.contains("book")) {
            return "HOTEL_SEARCH";
        }
        if (lower.contains("distance") || lower.contains("how far") || lower.contains("km") || lower.contains("to ")) {
            return "DISTANCE_QUERY";
        }
        if (lower.contains("weather") || lower.contains("climate") || lower.contains("best time")) {
            return "WEATHER_QUERY";
        }
        if (lower.contains("food") || lower.contains("eat") || lower.contains("restaurant") || lower.contains("cuisine")) {
            return "FOOD_QUERY";
        }
        if (lower.contains("attraction") || lower.contains("places") || lower.contains("visit") || lower.contains("things to do")) {
            return "ATTRACTIONS_QUERY";
        }
        if (lower.contains("package") || lower.contains("tour") || lower.contains("trip")) {
            return "PACKAGE_QUERY";
        }
        if (lower.contains("price") || lower.contains("cost") || lower.contains("rate") || lower.contains("₹")) {
            return "PRICE_QUERY";
        }
        if (lower.contains("about") || lower.contains("tell me about") || lower.contains("info")) {
            return "CITY_INFO";
        }
        
        // Check if any city is mentioned
        if (knowledgeService.extractCity(message) != null) {
            return "CITY_INFO";
        }
        
        return "GENERAL_QUERY";
    }
    
    private double calculateConfidence(String message, String intent) {
        // Simple confidence calculation based on keyword matches
        String lower = message.toLowerCase();
        int matchCount = 0;
        int totalKeywords = 0;
        
        switch (intent) {
            case "HOTEL_SEARCH":
                totalKeywords = 4;
                if (lower.contains("hotel")) matchCount++;
                if (lower.contains("stay")) matchCount++;
                if (lower.contains("room")) matchCount++;
                if (lower.contains("book")) matchCount++;
                break;
            case "DISTANCE_QUERY":
                totalKeywords = 4;
                if (lower.contains("distance")) matchCount++;
                if (lower.contains("far")) matchCount++;
                if (lower.contains("km")) matchCount++;
                if (lower.contains("from") && lower.contains("to")) matchCount += 2;
                break;
            case "GREETING":
                totalKeywords = 3;
                if (lower.contains("hello")) matchCount++;
                if (lower.contains("hi")) matchCount++;
                if (lower.contains("hey")) matchCount++;
                break;
            default:
                return 0.7; // Default confidence
        }
        
        return totalKeywords > 0 ? Math.min(0.5 + (0.5 * matchCount / totalKeywords), 1.0) : 0.5;
    }
}
