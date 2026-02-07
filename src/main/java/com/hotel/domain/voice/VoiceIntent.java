package com.hotel.domain.voice;

/**
 * Voice Intent Enumeration
 * Defines all supported voice commands for the hotel booking system.
 */
public enum VoiceIntent {
    
    // ========== SEARCH INTENTS ==========
    SEARCH_HOTELS("Search for hotels", "hotel, hotels, stay, accommodation, find hotel"),
    SEARCH_BY_CITY("Search hotels in a specific city", "in, at, near"),
    SEARCH_LUXURY("Search for luxury hotels", "luxury, five star, 5 star, premium, high end"),
    SEARCH_BUDGET("Search for budget hotels", "budget, cheap, affordable, low price, economical"),
    
    // ========== FILTER INTENTS ==========
    FILTER_STAR_RATING("Filter by star rating", "star, stars, rating, rated"),
    FILTER_PRICE_RANGE("Filter by price range", "price, cost, under, below, between, range"),
    FILTER_GUESTS("Filter by guest capacity", "guests, people, persons, capacity"),
    
    // ========== BOOKING INTENTS ==========
    BOOK_ROOM("Book a hotel room", "book, reserve, booking, reservation"),
    SELECT_DATES("Select check-in/check-out dates", "date, dates, check in, check out, from, to"),
    CONFIRM_BOOKING("Confirm a booking", "confirm, yes, proceed, go ahead"),
    CANCEL_BOOKING("Cancel a booking", "cancel booking, cancel reservation"),
    VIEW_BOOKINGS("View my bookings", "my bookings, reservations, booked"),
    
    // ========== NAVIGATION INTENTS ==========
    NAVIGATE("Navigate to a page", "go to, open, show, take me to, navigate"),
    GO_HOME("Go to home page", "home, main page, start"),
    GO_HOTELS("Go to hotels page", "hotels page, all hotels, browse hotels"),
    GO_MAP("Go to map page", "map, show map, view map"),
    GO_PROFILE("Go to profile page", "profile, my account, settings"),
    GO_BACK("Go back to previous page", "back, go back, previous"),
    
    // ========== HOTEL INFO INTENTS ==========
    HOTEL_DETAILS("Get hotel details", "details, about, tell me about, information, info"),
    ROOM_DETAILS("Get room details", "room, rooms, room details, available rooms"),
    CHECK_AVAILABILITY("Check room availability", "available, availability, free, vacant"),
    
    // ========== GENERAL INTENTS ==========
    HELP("Get help", "help, what can you do, commands, assist"),
    GREETING("User greeting", "hello, hi, hey, good morning, good evening"),
    CANCEL("Cancel current action", "cancel, stop, never mind, forget it"),
    REPEAT("Repeat last response", "repeat, say again, what, pardon"),
    
    // ========== UNKNOWN ==========
    UNKNOWN("Unknown intent", "");
    
    private final String description;
    private final String keywords;
    
    VoiceIntent(String description, String keywords) {
        this.description = description;
        this.keywords = keywords;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    /**
     * Parse intent from string, defaulting to UNKNOWN if not found
     */
    public static VoiceIntent fromString(String intent) {
        if (intent == null || intent.isBlank()) {
            return UNKNOWN;
        }
        try {
            return VoiceIntent.valueOf(intent.toUpperCase().replace("-", "_").replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
    
    /**
     * Check if this intent requires authentication
     */
    public boolean requiresAuthentication() {
        return switch (this) {
            case BOOK_ROOM, CONFIRM_BOOKING, CANCEL_BOOKING, VIEW_BOOKINGS, GO_PROFILE -> true;
            default -> false;
        };
    }
    
    /**
     * Check if this is a search-related intent
     */
    public boolean isSearchIntent() {
        return switch (this) {
            case SEARCH_HOTELS, SEARCH_BY_CITY, SEARCH_LUXURY, SEARCH_BUDGET,
                 FILTER_STAR_RATING, FILTER_PRICE_RANGE, FILTER_GUESTS -> true;
            default -> false;
        };
    }
    
    /**
     * Check if this is a booking-related intent
     */
    public boolean isBookingIntent() {
        return switch (this) {
            case BOOK_ROOM, SELECT_DATES, CONFIRM_BOOKING, CANCEL_BOOKING, VIEW_BOOKINGS -> true;
            default -> false;
        };
    }
    
    /**
     * Check if this is a navigation intent
     */
    public boolean isNavigationIntent() {
        return switch (this) {
            case NAVIGATE, GO_HOME, GO_HOTELS, GO_MAP, GO_PROFILE, GO_BACK -> true;
            default -> false;
        };
    }
}
