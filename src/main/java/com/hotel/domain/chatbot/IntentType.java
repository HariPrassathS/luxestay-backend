package com.hotel.domain.chatbot;

/**
 * Enumeration of supported chatbot intents.
 * Frontend detects intent, Backend executes intelligence.
 */
public enum IntentType {
    
    // Hotel related intents
    HOTEL_SEARCH("Search for hotels in a city"),
    LUXURY_SEARCH("Search for luxury/5-star hotels"),
    BUDGET_SEARCH("Search for budget-friendly hotels"),
    PRICE_QUERY("Query about hotel prices"),
    
    // Location intents
    CITY_INFO("Get information about a city"),
    DISTANCE_QUERY("Calculate distance between cities"),
    ATTRACTIONS_QUERY("Get attractions in a city"),
    
    // Travel planning intents
    WEATHER_QUERY("Get weather and best time to visit"),
    FOOD_QUERY("Get local food specialties"),
    PACKAGE_QUERY("Get travel packages"),
    
    // General intents
    GREETING("User greeting"),
    HELP("User asking for help"),
    GENERAL_QUERY("General or unknown query");
    
    private final String description;
    
    IntentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Parse intent from string, defaulting to GENERAL_QUERY if not found
     */
    public static IntentType fromString(String intent) {
        if (intent == null || intent.isBlank()) {
            return GENERAL_QUERY;
        }
        try {
            return IntentType.valueOf(intent.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return GENERAL_QUERY;
        }
    }
}
