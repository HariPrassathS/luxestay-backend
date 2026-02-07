package com.hotel.service;

import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.booking.CreateBookingRequest;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Room;
import com.hotel.domain.entity.User;
import com.hotel.domain.voice.VoiceIntent;
import com.hotel.domain.voice.VoiceRequest;
import com.hotel.domain.voice.VoiceResponse;
import com.hotel.domain.voice.VoiceResponse.VoiceAction;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Voice Assistant Service
 * Processes voice commands and converts them to actionable operations.
 * This is the intelligence layer for voice interactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VoiceAssistantService {
    
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    @SuppressWarnings("unused") // Reserved for future travel knowledge integration
    private final TravelKnowledgeService knowledgeService;
    
    // Common city names for fuzzy matching
    private static final Set<String> KNOWN_CITIES = Set.of(
        "chennai", "mumbai", "delhi", "bangalore", "kolkata", "hyderabad",
        "madurai", "coimbatore", "ooty", "kodaikanal", "pondicherry", "trichy",
        "kanyakumari", "mahabalipuram", "rameswaram", "thanjavur", "coonoor",
        "paris", "london", "tokyo", "dubai", "sydney", "new york", "miami",
        "los angeles", "singapore", "barcelona", "rome", "maldives"
    );
    
    // Navigation mappings
    private static final Map<String, String> NAVIGATION_TARGETS = Map.ofEntries(
        Map.entry("home", "/index.html"),
        Map.entry("hotels", "/hotels.html"),
        Map.entry("map", "/map.html"),
        Map.entry("profile", "/profile.html"),
        Map.entry("my bookings", "/my-bookings.html"),
        Map.entry("bookings", "/my-bookings.html"),
        Map.entry("login", "/login.html"),
        Map.entry("signup", "/signup.html"),
        Map.entry("register", "/signup.html")
    );
    
    /**
     * Main entry point for processing voice commands
     */
    public VoiceResponse processVoiceCommand(VoiceRequest request) {
        log.info("Processing voice command: intent={}, transcript={}", 
                request.getIntent(), request.getTranscript());
        
        try {
            // Check if intent requires authentication
            if (request.getIntent() != null && request.getIntent().requiresAuthentication()) {
                if (!isAuthenticated()) {
                    return VoiceResponse.requiresLogin(request.getIntent().getDescription());
                }
            }
            
            // Process based on intent
            return switch (request.getIntent()) {
                case SEARCH_HOTELS, SEARCH_BY_CITY -> handleHotelSearch(request);
                case SEARCH_LUXURY -> handleLuxurySearch(request);
                case SEARCH_BUDGET -> handleBudgetSearch(request);
                case FILTER_STAR_RATING -> handleStarFilter(request);
                case FILTER_PRICE_RANGE -> handlePriceFilter(request);
                case FILTER_GUESTS -> handleGuestFilter(request);
                case BOOK_ROOM -> handleBookRoom(request);
                case SELECT_DATES -> handleDateSelection(request);
                case CONFIRM_BOOKING -> handleConfirmBooking(request);
                case CANCEL_BOOKING -> handleCancelBooking(request);
                case VIEW_BOOKINGS -> handleViewBookings(request);
                case NAVIGATE, GO_HOME, GO_HOTELS, GO_MAP, GO_PROFILE, GO_BACK -> handleNavigation(request);
                case HOTEL_DETAILS -> handleHotelDetails(request);
                case ROOM_DETAILS -> handleRoomDetails(request);
                case CHECK_AVAILABILITY -> handleCheckAvailability(request);
                case HELP -> handleHelp();
                case GREETING -> handleGreeting();
                case CANCEL -> handleCancel();
                case REPEAT -> handleRepeat(request);
                default -> handleUnknown(request);
            };
        } catch (Exception e) {
            log.error("Error processing voice command", e);
            return VoiceResponse.fallback(
                "I'm sorry, I encountered an error. Please try again or use text search.",
                List.of("Search hotels", "Help", "Go to hotels page")
            );
        }
    }
    
    /**
     * Parse intent from raw transcript
     */
    public VoiceIntent detectIntent(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return VoiceIntent.UNKNOWN;
        }
        
        String lower = transcript.toLowerCase().trim();
        
        // Greeting patterns
        if (lower.matches("^(hello|hi|hey|good morning|good afternoon|good evening).*")) {
            return VoiceIntent.GREETING;
        }
        
        // Help patterns
        if (lower.contains("help") || lower.contains("what can you do") || 
            lower.contains("how do i") || lower.contains("commands")) {
            return VoiceIntent.HELP;
        }
        
        // Cancel patterns
        if (lower.matches("^(cancel|stop|never mind|forget it|no thanks).*")) {
            return VoiceIntent.CANCEL;
        }
        
        // Booking patterns (must check before search)
        if (lower.contains("book") || lower.contains("reserve") || lower.contains("booking")) {
            if (lower.contains("my booking") || lower.contains("my reservation") || lower.contains("view")) {
                return VoiceIntent.VIEW_BOOKINGS;
            }
            if (lower.contains("cancel")) {
                return VoiceIntent.CANCEL_BOOKING;
            }
            if (lower.contains("confirm") || lower.contains("yes") || lower.contains("proceed")) {
                return VoiceIntent.CONFIRM_BOOKING;
            }
            return VoiceIntent.BOOK_ROOM;
        }
        
        // Navigation patterns
        if (lower.startsWith("go to") || lower.startsWith("open") || 
            lower.startsWith("navigate") || lower.startsWith("take me to") ||
            lower.startsWith("show me")) {
            if (lower.contains("home")) return VoiceIntent.GO_HOME;
            if (lower.contains("hotel")) return VoiceIntent.GO_HOTELS;
            if (lower.contains("map")) return VoiceIntent.GO_MAP;
            if (lower.contains("profile") || lower.contains("account")) return VoiceIntent.GO_PROFILE;
            if (lower.contains("booking")) return VoiceIntent.VIEW_BOOKINGS;
            return VoiceIntent.NAVIGATE;
        }
        
        if (lower.contains("go back") || lower.equals("back")) {
            return VoiceIntent.GO_BACK;
        }
        
        // Filter patterns
        if (lower.matches(".*\\d+\\s*star.*") || lower.contains("star rating")) {
            return VoiceIntent.FILTER_STAR_RATING;
        }
        
        if (lower.contains("price") || lower.contains("under") || lower.contains("budget") ||
            lower.contains("cheap") || lower.contains("affordable")) {
            if (lower.contains("budget") || lower.contains("cheap") || lower.contains("affordable")) {
                return VoiceIntent.SEARCH_BUDGET;
            }
            return VoiceIntent.FILTER_PRICE_RANGE;
        }
        
        if (lower.matches(".*\\d+\\s*(guest|people|person).*")) {
            return VoiceIntent.FILTER_GUESTS;
        }
        
        // Search patterns
        if (lower.contains("luxury") || lower.contains("5 star") || lower.contains("five star") ||
            lower.contains("premium") || lower.contains("high end")) {
            return VoiceIntent.SEARCH_LUXURY;
        }
        
        if (lower.contains("hotel") || lower.contains("stay") || lower.contains("accommodation") ||
            lower.contains("find") || lower.contains("search") || lower.contains("show")) {
            return VoiceIntent.SEARCH_HOTELS;
        }
        
        // Check for city names (might be a search)
        for (String city : KNOWN_CITIES) {
            if (lower.contains(city)) {
                return VoiceIntent.SEARCH_BY_CITY;
            }
        }
        
        // Details patterns
        if (lower.contains("detail") || lower.contains("about") || lower.contains("information")) {
            if (lower.contains("room")) return VoiceIntent.ROOM_DETAILS;
            return VoiceIntent.HOTEL_DETAILS;
        }
        
        // Availability
        if (lower.contains("available") || lower.contains("availability") || lower.contains("free")) {
            return VoiceIntent.CHECK_AVAILABILITY;
        }
        
        // Date selection
        if (lower.contains("check in") || lower.contains("check out") || lower.contains("dates")) {
            return VoiceIntent.SELECT_DATES;
        }
        
        return VoiceIntent.UNKNOWN;
    }
    
    /**
     * Extract parameters from transcript
     */
    public VoiceRequest.VoiceParameters extractParameters(String transcript) {
        VoiceRequest.VoiceParameters.VoiceParametersBuilder params = 
            VoiceRequest.VoiceParameters.builder();
        
        String lower = transcript.toLowerCase();
        
        // Extract city
        String city = extractCity(lower);
        if (city != null) {
            params.city(city);
        }
        
        // Extract star rating (e.g., "3 star", "4 stars", "five star")
        Integer stars = extractStarRating(lower);
        if (stars != null) {
            params.starRating(stars);
        }
        
        // Extract price (e.g., "under 5000", "below 10000", "between 2000 and 5000")
        extractPriceRange(lower, params);
        
        // Extract guests (e.g., "for 2 guests", "2 people", "family of 4")
        Integer guests = extractGuests(lower);
        if (guests != null) {
            params.guests(guests);
        }
        
        // Extract dates
        extractDates(lower, params);
        
        // Extract hotel/room ID if mentioned
        extractIds(lower, params);
        
        return params.build();
    }
    
    // ========== INTENT HANDLERS ==========
    
    private VoiceResponse handleHotelSearch(VoiceRequest request) {
        String city = null;
        
        if (request.getParameters() != null && request.getParameters().getCity() != null) {
            city = request.getParameters().getCity();
        } else {
            city = extractCity(request.getTranscript());
        }
        
        if (city == null) {
            // Ask for city
            return VoiceResponse.needsClarification(
                VoiceIntent.SEARCH_HOTELS,
                "Which city would you like to search hotels in?",
                List.of("Chennai", "Ooty", "Madurai", "Mumbai", "Paris")
            );
        }
        
        // Search hotels
        List<Hotel> hotels = hotelRepository.findByCityIgnoreCaseAndIsActiveTrue(city);
        
        if (hotels.isEmpty()) {
            // Try partial match
            hotels = hotelRepository.searchHotels(city);
        }
        
        if (hotels.isEmpty()) {
            return VoiceResponse.success(
                VoiceIntent.SEARCH_HOTELS,
                String.format("I couldn't find any hotels in %s. Would you like to try another city?", city),
                String.format("No hotels found in %s", city),
                Map.of("city", city, "count", 0),
                VoiceAction.SPEAK_ONLY,
                List.of("Hotels in Chennai", "Hotels in Mumbai", "Show all hotels")
            );
        }
        
        // Apply additional filters if present
        if (request.getParameters() != null) {
            hotels = applyFilters(hotels, request.getParameters());
        }
        
        // Sort by rating and limit
        List<Map<String, Object>> hotelData = hotels.stream()
                .sorted((a, b) -> Integer.compare(
                    b.getStarRating() != null ? b.getStarRating() : 0,
                    a.getStarRating() != null ? a.getStarRating() : 0))
                .limit(5)
                .map(this::hotelToMap)
                .toList();
        
        String speechText = String.format("I found %d hotels in %s. Here are the top rated options.", 
                hotels.size(), city);
        
        return VoiceResponse.success(
            VoiceIntent.SEARCH_HOTELS,
            speechText,
            String.format("Found <strong>%d hotels</strong> in %s", hotels.size(), city),
            Map.of(
                "hotels", hotelData, 
                "totalCount", hotels.size(), 
                "city", city,
                "searchUrl", "/hotels.html?location=" + city
            ),
            VoiceAction.DISPLAY_RESULTS,
            List.of("Book a room", "Show on map", "Filter by price")
        );
    }
    
    private VoiceResponse handleLuxurySearch(VoiceRequest request) {
        String city = request.getParameters() != null ? 
                request.getParameters().getCity() : extractCity(request.getTranscript());
        
        List<Hotel> hotels = hotelRepository.findByIsActiveTrue().stream()
                .filter(h -> h.getStarRating() != null && h.getStarRating() >= 4)
                .collect(Collectors.toList());
        
        if (city != null) {
            String finalCity = city;
            hotels = hotels.stream()
                    .filter(h -> h.getCity() != null && 
                            h.getCity().toLowerCase().contains(finalCity.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> hotelData = hotels.stream()
                .sorted((a, b) -> Integer.compare(
                    b.getStarRating() != null ? b.getStarRating() : 0,
                    a.getStarRating() != null ? a.getStarRating() : 0))
                .limit(5)
                .map(this::hotelToMap)
                .toList();
        
        String locationText = city != null ? " in " + city : "";
        String speechText = String.format("I found %d luxury hotels%s.", hotelData.size(), locationText);
        
        return VoiceResponse.success(
            VoiceIntent.SEARCH_LUXURY,
            speechText,
            String.format("Found <strong>%d luxury hotels</strong>%s", hotelData.size(), locationText),
            Map.of("hotels", hotelData, "totalCount", hotelData.size(), "type", "luxury"),
            VoiceAction.DISPLAY_RESULTS,
            List.of("Book a room", "Budget hotels", "Show details")
        );
    }
    
    private VoiceResponse handleBudgetSearch(VoiceRequest request) {
        String city = request.getParameters() != null ? 
                request.getParameters().getCity() : extractCity(request.getTranscript());
        
        // Budget = price <= 5000 INR
        List<Hotel> hotels = hotelRepository.findByIsActiveTrue();
        
        List<Map<String, Object>> hotelData = new ArrayList<>();
        for (Hotel hotel : hotels) {
            if (city != null && (hotel.getCity() == null || 
                    !hotel.getCity().toLowerCase().contains(city.toLowerCase()))) {
                continue;
            }
            
            // Get minimum room price
            var rooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotel.getId());
            Optional<BigDecimal> minPrice = rooms.stream()
                    .map(Room::getPricePerNight)
                    .min(BigDecimal::compareTo);
            
            if (minPrice.isPresent() && minPrice.get().compareTo(BigDecimal.valueOf(5000)) <= 0) {
                Map<String, Object> hotelMap = hotelToMap(hotel);
                hotelMap.put("minPrice", minPrice.get());
                hotelData.add(hotelMap);
            }
            
            if (hotelData.size() >= 5) break;
        }
        
        String locationText = city != null ? " in " + city : "";
        String speechText = String.format("I found %d budget-friendly hotels%s under 5000 rupees.", 
                hotelData.size(), locationText);
        
        return VoiceResponse.success(
            VoiceIntent.SEARCH_BUDGET,
            speechText,
            String.format("Found <strong>%d budget hotels</strong>%s", hotelData.size(), locationText),
            Map.of("hotels", hotelData, "totalCount", hotelData.size(), "type", "budget"),
            VoiceAction.DISPLAY_RESULTS,
            List.of("Book a room", "Luxury hotels", "Show on map")
        );
    }
    
    private VoiceResponse handleStarFilter(VoiceRequest request) {
        Integer stars = request.getParameters() != null ? 
                request.getParameters().getStarRating() : extractStarRating(request.getTranscript());
        
        if (stars == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.FILTER_STAR_RATING,
                "What star rating would you like? 3, 4, or 5 stars?",
                List.of("3 star hotels", "4 star hotels", "5 star hotels")
            );
        }
        
        String city = request.getParameters() != null ? request.getParameters().getCity() : 
                request.getContext() != null ? request.getContext().getLastCity() : null;
        
        List<Hotel> hotels = hotelRepository.findByIsActiveTrue().stream()
                .filter(h -> h.getStarRating() != null && h.getStarRating().equals(stars))
                .collect(Collectors.toList());
        
        if (city != null) {
            String finalCity = city;
            hotels = hotels.stream()
                    .filter(h -> h.getCity() != null && 
                            h.getCity().toLowerCase().contains(finalCity.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> hotelData = hotels.stream()
                .limit(5)
                .map(this::hotelToMap)
                .toList();
        
        String speechText = String.format("I found %d %d-star hotels.", hotelData.size(), stars);
        
        return VoiceResponse.success(
            VoiceIntent.FILTER_STAR_RATING,
            speechText,
            String.format("Found <strong>%d %d-star hotels</strong>", hotelData.size(), stars),
            Map.of("hotels", hotelData, "totalCount", hotelData.size(), "starRating", stars),
            VoiceAction.APPLY_FILTER,
            List.of("Book a room", "Change filter", "Show all hotels")
        );
    }
    
    private VoiceResponse handlePriceFilter(VoiceRequest request) {
        Double maxPrice = request.getParameters() != null ? request.getParameters().getMaxPrice() : null;
        Double minPrice = request.getParameters() != null ? request.getParameters().getMinPrice() : null;
        
        if (maxPrice == null && minPrice == null) {
            // Try to extract from transcript
            String transcript = request.getTranscript().toLowerCase();
            Pattern underPattern = Pattern.compile("under\\s+(\\d+)");
            Matcher matcher = underPattern.matcher(transcript);
            if (matcher.find()) {
                maxPrice = Double.parseDouble(matcher.group(1));
            }
        }
        
        if (maxPrice == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.FILTER_PRICE_RANGE,
                "What's your maximum budget per night?",
                List.of("Under 3000", "Under 5000", "Under 10000")
            );
        }
        
        String speechText = String.format("Filtering hotels under %.0f rupees per night.", maxPrice);
        
        return VoiceResponse.success(
            VoiceIntent.FILTER_PRICE_RANGE,
            speechText,
            String.format("Showing hotels under ₹%.0f/night", maxPrice),
            Map.of("maxPrice", maxPrice, "filterApplied", true),
            VoiceAction.APPLY_FILTER,
            List.of("Book a room", "Clear filters", "Show all")
        );
    }
    
    private VoiceResponse handleGuestFilter(VoiceRequest request) {
        Integer guests = request.getParameters() != null ? request.getParameters().getGuests() : 
                extractGuests(request.getTranscript());
        
        if (guests == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.FILTER_GUESTS,
                "How many guests will be staying?",
                List.of("1 guest", "2 guests", "Family of 4")
            );
        }
        
        String speechText = String.format("Filtering rooms that can accommodate %d guests.", guests);
        
        return VoiceResponse.success(
            VoiceIntent.FILTER_GUESTS,
            speechText,
            String.format("Showing rooms for %d guests", guests),
            Map.of("guests", guests, "filterApplied", true),
            VoiceAction.APPLY_FILTER,
            List.of("Book a room", "Change guests", "Show all rooms")
        );
    }
    
    @Transactional
    private VoiceResponse handleBookRoom(VoiceRequest request) {
        if (!isAuthenticated()) {
            return VoiceResponse.requiresLogin("book a room");
        }
        
        // Check what information we have - use null-safe extraction with defaults
        Long hotelId = null;
        Long roomId = null;
        LocalDate checkIn = null;
        LocalDate checkOut = null;
        Integer guests = 1; // Default to 1 guest
        
        if (request.getParameters() != null) {
            hotelId = request.getParameters().getHotelId();
            roomId = request.getParameters().getRoomId();
            checkIn = request.getParameters().getCheckInDate();
            checkOut = request.getParameters().getCheckOutDate();
            if (request.getParameters().getGuests() != null) {
                guests = request.getParameters().getGuests();
            }
        }
        
        // Try to get from context
        if (hotelId == null && request.getContext() != null && request.getContext().getLastHotelId() != null) {
            try {
                hotelId = Long.parseLong(request.getContext().getLastHotelId());
            } catch (NumberFormatException e) {
                hotelId = null;
            }
        }
        
        // Try to extract hotel name from transcript
        if (hotelId == null) {
            String transcript = request.getTranscript().toLowerCase();
            // Search for hotel by name in transcript
            var allHotels = hotelRepository.findByIsActiveTrue();
            for (var hotel : allHotels) {
                if (transcript.contains(hotel.getName().toLowerCase())) {
                    hotelId = hotel.getId();
                    break;
                }
            }
        }
        
        // If we still don't have hotel, show popular hotels to choose from
        if (hotelId == null) {
            var hotels = hotelRepository.findFeaturedHotels(PageRequest.of(0, 5));
            if (!hotels.isEmpty()) {
                List<Map<String, Object>> hotelData = hotels.stream()
                        .map(this::hotelToMap)
                        .toList();
                
                return VoiceResponse.success(
                    VoiceIntent.BOOK_ROOM,
                    "Which hotel would you like to book? Here are our top recommendations.",
                    "Select a hotel to book",
                    Map.of("hotels", hotelData, "action", "selectHotel", "isBookingContext", true),
                    VoiceAction.DISPLAY_RESULTS,
                    hotels.stream().limit(3).map(h -> "Book at " + h.getName()).toList()
                );
            }
            
            return VoiceResponse.needsClarification(
                VoiceIntent.BOOK_ROOM,
                "Which hotel would you like to book? You can search by city first.",
                List.of("Hotels in Chennai", "Hotels in Mumbai", "Show luxury hotels")
            );
        }
        
        // If we don't have room, show available rooms
        if (roomId == null) {
            var rooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotelId);
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            
            if (rooms.isEmpty()) {
                return VoiceResponse.fallback(
                    "Sorry, there are no rooms available at this hotel right now.",
                    List.of("Search other hotels", "Go back")
                );
            }
            
            List<Map<String, Object>> roomData = rooms.stream()
                    .limit(5)
                    .map(this::roomToMap)
                    .toList();
            
            String hotelName = hotel != null ? hotel.getName() : "this hotel";
            return VoiceResponse.success(
                VoiceIntent.BOOK_ROOM,
                String.format("Here are the available rooms at %s. Which one would you like to book?", hotelName),
                "Select a room to book",
                Map.of("rooms", roomData, "hotelId", hotelId, "hotelName", hotelName, "action", "selectRoom"),
                VoiceAction.DISPLAY_RESULTS,
                rooms.stream().limit(3).map(r -> "Book " + r.getName()).toList()
            );
        }
        
        // If we don't have dates, suggest quick options
        if (checkIn == null || checkOut == null) {
            // Provide default dates for convenience
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDate defaultCheckout = tomorrow.plusDays(2);
            
            // Room and hotel info can be fetched if needed for display
            
            return VoiceResponse.success(
                VoiceIntent.SELECT_DATES,
                "When would you like to stay? You can say 'tomorrow for 2 nights' or 'this weekend'.",
                "Select your dates",
                Map.of(
                    "hotelId", hotelId,
                    "roomId", roomId,
                    "suggestedCheckIn", tomorrow.toString(),
                    "suggestedCheckOut", defaultCheckout.toString(),
                    "action", "selectDates"
                ),
                VoiceAction.DISPLAY_RESULTS,
                List.of("Tomorrow for 2 nights", "This weekend", "Next week for 3 nights")
            );
        }
        
        // We have everything, prepare booking confirmation
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return VoiceResponse.fallback(
                "Sorry, I couldn't find that room. Please try again.",
                List.of("Show rooms", "Search hotels")
            );
        }
        
        Hotel hotel = room.getHotel();
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        
        String speechText = String.format(
            "Ready to book %s at %s for %d nights, from %s to %s. Total price is %.0f rupees. Say 'confirm' to proceed.",
            room.getName(), hotel.getName(), nights, 
            checkIn.format(DateTimeFormatter.ofPattern("MMMM d")),
            checkOut.format(DateTimeFormatter.ofPattern("MMMM d")),
            totalPrice.doubleValue()
        );
        
        return VoiceResponse.success(
            VoiceIntent.BOOK_ROOM,
            speechText,
            "Confirm your booking",
            Map.of(
                "hotel", hotelToMap(hotel),
                "room", roomToMap(room),
                "checkInDate", checkIn.toString(),
                "checkOutDate", checkOut.toString(),
                "nights", nights,
                "totalPrice", totalPrice,
                "guests", guests,
                "action", "confirmBooking"
            ),
            VoiceAction.CONFIRM_BOOKING,
            List.of("Confirm booking", "Change dates", "Cancel")
        );
    }
    
    private VoiceResponse handleDateSelection(VoiceRequest request) {
        LocalDate checkIn = request.getParameters() != null ? request.getParameters().getCheckInDate() : null;
        LocalDate checkOut = request.getParameters() != null ? request.getParameters().getCheckOutDate() : null;
        
        if (checkIn == null) {
            // Try natural language parsing
            String transcript = request.getTranscript().toLowerCase();
            
            if (transcript.contains("tomorrow")) {
                checkIn = LocalDate.now().plusDays(1);
            } else if (transcript.contains("today")) {
                checkIn = LocalDate.now();
            } else if (transcript.contains("next week")) {
                checkIn = LocalDate.now().plusWeeks(1);
            } else if (transcript.contains("this weekend")) {
                // Find next Saturday
                LocalDate now = LocalDate.now();
                int daysUntilSaturday = (6 - now.getDayOfWeek().getValue() + 7) % 7;
                if (daysUntilSaturday == 0) daysUntilSaturday = 7;
                checkIn = now.plusDays(daysUntilSaturday);
            }
            
            // Extract nights
            Pattern nightsPattern = Pattern.compile("(\\d+)\\s*night");
            Matcher matcher = nightsPattern.matcher(transcript);
            if (matcher.find() && checkIn != null) {
                int nights = Integer.parseInt(matcher.group(1));
                checkOut = checkIn.plusDays(nights);
            }
        }
        
        if (checkIn == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.SELECT_DATES,
                "When would you like to check in?",
                List.of("Tomorrow", "This weekend", "Next Friday")
            );
        }
        
        if (checkOut == null) {
            checkOut = checkIn.plusDays(1);
        }
        
        String speechText = String.format("Dates set: Check-in on %s, check-out on %s.",
                checkIn.format(DateTimeFormatter.ofPattern("MMMM d")),
                checkOut.format(DateTimeFormatter.ofPattern("MMMM d")));
        
        return VoiceResponse.success(
            VoiceIntent.SELECT_DATES,
            speechText,
            "Dates selected",
            Map.of("checkInDate", checkIn.toString(), "checkOutDate", checkOut.toString()),
            VoiceAction.SPEAK_ONLY,
            List.of("Proceed to book", "Change dates", "Cancel")
        );
    }
    
    @Transactional
    private VoiceResponse handleConfirmBooking(VoiceRequest request) {
        if (!isAuthenticated()) {
            return VoiceResponse.requiresLogin("confirm this booking");
        }
        
        // Extract booking details from context/parameters
        Long roomId = request.getParameters() != null ? request.getParameters().getRoomId() : null;
        LocalDate checkIn = request.getParameters() != null ? request.getParameters().getCheckInDate() : null;
        LocalDate checkOut = request.getParameters() != null ? request.getParameters().getCheckOutDate() : null;
        Integer guests = request.getParameters() != null ? request.getParameters().getGuests() : 1;
        String specialRequests = request.getParameters() != null ? 
                request.getParameters().getSpecialRequests() : null;
        
        if (roomId == null || checkIn == null || checkOut == null) {
            return VoiceResponse.fallback(
                "I don't have all the booking details. Please start the booking process again.",
                List.of("Book a room", "Search hotels")
            );
        }
        
        try {
            CreateBookingRequest bookingRequest = CreateBookingRequest.builder()
                    .roomId(roomId)
                    .checkInDate(checkIn)
                    .checkOutDate(checkOut)
                    .numGuests(guests)
                    .specialRequests(specialRequests)
                    .build();
            
            BookingDto booking = bookingService.createBooking(bookingRequest);
            
            String speechText = String.format(
                "Congratulations! Your booking is confirmed. Your reference number is %s. " +
                "A confirmation has been sent to your email. Have a wonderful stay!",
                booking.getBookingReference()
            );
            
            return VoiceResponse.success(
                VoiceIntent.CONFIRM_BOOKING,
                speechText,
                "Booking Confirmed! 🎉",
                Map.of(
                    "booking", booking,
                    "reference", booking.getBookingReference()
                ),
                VoiceAction.COMPLETE_BOOKING,
                List.of("View my bookings", "Book another room", "Go to home")
            );
        } catch (Exception e) {
            log.error("Error creating booking via voice", e);
            return VoiceResponse.fallback(
                "Sorry, I couldn't complete the booking. " + e.getMessage(),
                List.of("Try again", "Contact support")
            );
        }
    }
    
    @Transactional
    private VoiceResponse handleCancelBooking(VoiceRequest request) {
        if (!isAuthenticated()) {
            return VoiceResponse.requiresLogin("cancel a booking");
        }
        
        // Get current user's bookings
        User user = getCurrentUser();
        var bookings = bookingRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        
        if (bookings.isEmpty()) {
            return VoiceResponse.success(
                VoiceIntent.CANCEL_BOOKING,
                "You don't have any active bookings to cancel.",
                "No active bookings",
                Map.of(),
                VoiceAction.SPEAK_ONLY,
                List.of("Book a room", "Go to home")
            );
        }
        
        // Show cancellable bookings
        var cancellableBookings = bookings.stream()
                .filter(b -> b.isCancellable())
                .limit(5)
                .toList();
        
        if (cancellableBookings.isEmpty()) {
            return VoiceResponse.success(
                VoiceIntent.CANCEL_BOOKING,
                "You don't have any bookings that can be cancelled.",
                "No cancellable bookings",
                Map.of(),
                VoiceAction.SPEAK_ONLY,
                List.of("View my bookings", "Go to home")
            );
        }
        
        String speechText = String.format(
            "You have %d booking(s) that can be cancelled. Which one would you like to cancel?",
            cancellableBookings.size()
        );
        
        List<Map<String, Object>> bookingData = cancellableBookings.stream()
                .map(this::bookingToMap)
                .toList();
        
        return VoiceResponse.success(
            VoiceIntent.CANCEL_BOOKING,
            speechText,
            "Select booking to cancel",
            Map.of("bookings", bookingData),
            VoiceAction.DISPLAY_RESULTS,
            cancellableBookings.stream()
                    .limit(3)
                    .map(b -> "Cancel " + b.getBookingReference())
                    .toList()
        );
    }
    
    private VoiceResponse handleViewBookings(VoiceRequest request) {
        if (!isAuthenticated()) {
            return VoiceResponse.requiresLogin("view your bookings");
        }
        
        User user = getCurrentUser();
        var bookings = bookingRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        
        if (bookings.isEmpty()) {
            return VoiceResponse.success(
                VoiceIntent.VIEW_BOOKINGS,
                "You don't have any bookings yet. Would you like to search for hotels?",
                "No bookings found",
                Map.of("count", 0),
                VoiceAction.SPEAK_ONLY,
                List.of("Search hotels", "Go to home")
            );
        }
        
        List<Map<String, Object>> bookingData = bookings.stream()
                .limit(5)
                .map(this::bookingToMap)
                .toList();
        
        String speechText = String.format("You have %d booking(s). Here are your recent reservations.", 
                bookings.size());
        
        return VoiceResponse.success(
            VoiceIntent.VIEW_BOOKINGS,
            speechText,
            String.format("Found <strong>%d bookings</strong>", bookings.size()),
            Map.of(
                "bookings", bookingData,
                "totalCount", bookings.size()
            ),
            VoiceAction.DISPLAY_RESULTS,
            List.of("Book another room", "Cancel a booking", "Go to home")
        );
    }
    
    private VoiceResponse handleNavigation(VoiceRequest request) {
        String target = null;
        
        // Determine target from intent
        target = switch (request.getIntent()) {
            case GO_HOME -> "/index.html";
            case GO_HOTELS -> "/hotels.html";
            case GO_MAP -> "/map.html";
            case GO_PROFILE -> "/profile.html";
            case GO_BACK -> "back";
            default -> {
                // Extract from transcript
                String transcript = request.getTranscript().toLowerCase();
                String foundTarget = null;
                for (Map.Entry<String, String> entry : NAVIGATION_TARGETS.entrySet()) {
                    if (transcript.contains(entry.getKey())) {
                        foundTarget = entry.getValue();
                        break;
                    }
                }
                yield foundTarget;
            }
        };
        
        if (target == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.NAVIGATE,
                "Where would you like to go?",
                List.of("Home", "Hotels", "Map", "My bookings", "Profile")
            );
        }
        
        if ("back".equals(target)) {
            return VoiceResponse.navigate("back", "Going back to the previous page.");
        }
        
        String pageName = target.replace("/", "").replace(".html", "")
                .replace("-", " ").replace("_", " ");
        if (pageName.isEmpty()) pageName = "home";
        
        return VoiceResponse.navigate(target, "Taking you to the " + pageName + " page.");
    }
    
    private VoiceResponse handleHotelDetails(VoiceRequest request) {
        Long hotelId = request.getParameters() != null ? request.getParameters().getHotelId() : null;
        String hotelName = request.getParameters() != null ? request.getParameters().getHotelName() : null;
        
        Hotel hotel = null;
        
        if (hotelId != null) {
            hotel = hotelRepository.findById(hotelId).orElse(null);
        } else if (hotelName != null) {
            var hotels = hotelRepository.searchHotels(hotelName);
            if (!hotels.isEmpty()) {
                hotel = hotels.get(0);
            }
        }
        
        if (hotel == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.HOTEL_DETAILS,
                "Which hotel would you like to know about?",
                List.of("Search hotels first", "Hotels in Chennai")
            );
        }
        
        var rooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotel.getId());
        Optional<BigDecimal> minPrice = rooms.stream()
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo);
        
        String speechText = String.format(
            "%s is a %d-star hotel in %s, %s. %s Prices start from %.0f rupees per night.",
            hotel.getName(), 
            hotel.getStarRating() != null ? hotel.getStarRating() : 3,
            hotel.getCity(), hotel.getCountry(),
            hotel.getDescription() != null ? hotel.getDescription().substring(0, Math.min(100, hotel.getDescription().length())) : "",
            minPrice.orElse(BigDecimal.ZERO).doubleValue()
        );
        
        return VoiceResponse.success(
            VoiceIntent.HOTEL_DETAILS,
            speechText,
            hotel.getName(),
            Map.of(
                "hotel", hotelToMap(hotel),
                "rooms", rooms.stream().limit(5).map(this::roomToMap).toList(),
                "roomCount", rooms.size(),
                "minPrice", minPrice.orElse(BigDecimal.ZERO)
            ),
            VoiceAction.SHOW_DETAILS,
            List.of("Book a room", "Show rooms", "View on map")
        );
    }
    
    private VoiceResponse handleRoomDetails(VoiceRequest request) {
        Long hotelId = request.getParameters() != null ? request.getParameters().getHotelId() : null;
        
        if (hotelId == null && request.getContext() != null && request.getContext().getLastHotelId() != null) {
            hotelId = Long.parseLong(request.getContext().getLastHotelId());
        }
        
        if (hotelId == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.ROOM_DETAILS,
                "Which hotel's rooms would you like to see?",
                List.of("Hotels in Chennai", "Search hotels")
            );
        }
        
        var rooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        
        if (rooms.isEmpty()) {
            return VoiceResponse.success(
                VoiceIntent.ROOM_DETAILS,
                "There are no available rooms at this hotel right now.",
                "No rooms available",
                Map.of(),
                VoiceAction.SPEAK_ONLY,
                List.of("Search other hotels", "Go back")
            );
        }
        
        String speechText = String.format(
            "%s has %d available rooms, ranging from %.0f to %.0f rupees per night.",
            hotel != null ? hotel.getName() : "This hotel",
            rooms.size(),
            rooms.stream().map(Room::getPricePerNight).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO).doubleValue(),
            rooms.stream().map(Room::getPricePerNight).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO).doubleValue()
        );
        
        return VoiceResponse.success(
            VoiceIntent.ROOM_DETAILS,
            speechText,
            "Available Rooms",
            Map.of("rooms", rooms.stream().map(this::roomToMap).toList(), "hotelId", hotelId),
            VoiceAction.DISPLAY_RESULTS,
            List.of("Book a room", "Go back", "Other hotels")
        );
    }
    
    private VoiceResponse handleCheckAvailability(VoiceRequest request) {
        Long hotelId = request.getParameters() != null ? request.getParameters().getHotelId() : null;
        LocalDate checkIn = request.getParameters() != null ? request.getParameters().getCheckInDate() : null;
        LocalDate checkOut = request.getParameters() != null ? request.getParameters().getCheckOutDate() : null;
        
        if (hotelId == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.CHECK_AVAILABILITY,
                "Which hotel would you like to check availability for?",
                List.of("Hotels in Chennai", "Search hotels")
            );
        }
        
        if (checkIn == null || checkOut == null) {
            return VoiceResponse.needsClarification(
                VoiceIntent.CHECK_AVAILABILITY,
                "For which dates would you like to check availability?",
                List.of("Tomorrow", "This weekend", "Next week")
            );
        }
        
        var rooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotelId);
        
        // Filter out rooms with conflicting bookings
        List<Room> availableRooms = rooms.stream()
                .filter(room -> !bookingRepository.hasOverlappingBooking(room.getId(), checkIn, checkOut))
                .toList();
        
        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        
        String speechText = String.format(
            "%s has %d rooms available from %s to %s.",
            hotel != null ? hotel.getName() : "This hotel",
            availableRooms.size(),
            checkIn.format(DateTimeFormatter.ofPattern("MMMM d")),
            checkOut.format(DateTimeFormatter.ofPattern("MMMM d"))
        );
        
        return VoiceResponse.success(
            VoiceIntent.CHECK_AVAILABILITY,
            speechText,
            String.format("%d rooms available", availableRooms.size()),
            Map.of(
                "rooms", availableRooms.stream().map(this::roomToMap).toList(),
                "checkIn", checkIn.toString(),
                "checkOut", checkOut.toString()
            ),
            VoiceAction.DISPLAY_RESULTS,
            List.of("Book a room", "Change dates", "Other hotels")
        );
    }
    
    private VoiceResponse handleHelp() {
        String speechText = "I can help you search for hotels, filter by price or rating, " +
                "make bookings, and navigate the website. Try saying things like: " +
                "'Find hotels in Chennai', 'Show 5-star hotels', 'Book a room', or 'Go to my bookings'.";
        
        return VoiceResponse.success(
            VoiceIntent.HELP,
            speechText,
            "How can I help you?",
            Map.of(
                "commands", List.of(
                    Map.of("category", "Search", "examples", List.of(
                        "Hotels in Chennai", "Luxury hotels", "Budget hotels", "5-star hotels"
                    )),
                    Map.of("category", "Booking", "examples", List.of(
                        "Book a room", "My bookings", "Cancel booking"
                    )),
                    Map.of("category", "Navigation", "examples", List.of(
                        "Go to home", "Show map", "My profile"
                    )),
                    Map.of("category", "Filters", "examples", List.of(
                        "Under 5000 rupees", "For 2 guests", "4-star hotels"
                    ))
                )
            ),
            VoiceAction.SPEAK_ONLY,
            List.of("Search hotels", "Book a room", "Go to hotels")
        );
    }
    
    private VoiceResponse handleGreeting() {
        String[] greetings = {
            "Hello! Welcome to LuxeStay. How can I help you find your perfect stay today?",
            "Hi there! I'm your LuxeStay voice assistant. Would you like to search for hotels?",
            "Welcome! I can help you find and book hotels. Where would you like to stay?"
        };
        
        String speechText = greetings[new Random().nextInt(greetings.length)];
        
        return VoiceResponse.success(
            VoiceIntent.GREETING,
            speechText,
            "Welcome to LuxeStay! 👋",
            Map.of(),
            VoiceAction.SPEAK_ONLY,
            List.of("Search hotels", "Show luxury hotels", "Help")
        );
    }
    
    private VoiceResponse handleCancel() {
        return VoiceResponse.success(
            VoiceIntent.CANCEL,
            "Okay, I've cancelled that action. Let me know if you need anything else.",
            "Action cancelled",
            Map.of(),
            VoiceAction.CANCEL_ACTION,
            List.of("Search hotels", "Help", "Go to home")
        );
    }
    
    private VoiceResponse handleRepeat(VoiceRequest request) {
        // In a full implementation, this would retrieve the last response from session
        return VoiceResponse.success(
            VoiceIntent.REPEAT,
            "I'm sorry, I don't have a previous message to repeat. How can I help you?",
            "How can I help?",
            Map.of(),
            VoiceAction.SPEAK_ONLY,
            List.of("Search hotels", "Help")
        );
    }
    
    private VoiceResponse handleUnknown(VoiceRequest request) {
        String transcript = request.getTranscript();
        
        // Try to detect city and suggest search
        String city = extractCity(transcript);
        if (city != null) {
            return handleHotelSearch(VoiceRequest.builder()
                    .intent(VoiceIntent.SEARCH_HOTELS)
                    .transcript(transcript)
                    .parameters(VoiceRequest.VoiceParameters.builder().city(city).build())
                    .build());
        }
        
        return VoiceResponse.fallback(
            "I'm not sure I understood that. You can search for hotels, make bookings, " +
            "or navigate the website. Try saying 'Help' for more options.",
            List.of("Help", "Search hotels", "Go to home")
        );
    }
    
    // ========== HELPER METHODS ==========
    
    private String extractCity(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        
        for (String city : KNOWN_CITIES) {
            if (lower.contains(city.toLowerCase())) {
                // Capitalize first letter
                return city.substring(0, 1).toUpperCase() + city.substring(1);
            }
        }
        
        // Check database for cities
        var cities = hotelRepository.findAllCities();
        for (String city : cities) {
            if (lower.contains(city.toLowerCase())) {
                return city;
            }
        }
        
        return null;
    }
    
    private Integer extractStarRating(String text) {
        if (text == null) return null;
        
        // Match patterns like "5 star", "five star", "5-star"
        Map<String, Integer> wordToNumber = Map.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5
        );
        
        for (Map.Entry<String, Integer> entry : wordToNumber.entrySet()) {
            if (text.toLowerCase().contains(entry.getKey() + " star")) {
                return entry.getValue();
            }
        }
        
        Pattern pattern = Pattern.compile("(\\d)\\s*[-]?\\s*star");
        Matcher matcher = pattern.matcher(text.toLowerCase());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        return null;
    }
    
    private void extractPriceRange(String text, VoiceRequest.VoiceParameters.VoiceParametersBuilder params) {
        // "under 5000", "below 10000"
        Pattern underPattern = Pattern.compile("(?:under|below|less than)\\s*(\\d+)");
        Matcher underMatcher = underPattern.matcher(text);
        if (underMatcher.find()) {
            params.maxPrice(Double.parseDouble(underMatcher.group(1)));
        }
        
        // "between 2000 and 5000"
        Pattern betweenPattern = Pattern.compile("between\\s*(\\d+)\\s*(?:and|to)\\s*(\\d+)");
        Matcher betweenMatcher = betweenPattern.matcher(text);
        if (betweenMatcher.find()) {
            params.minPrice(Double.parseDouble(betweenMatcher.group(1)));
            params.maxPrice(Double.parseDouble(betweenMatcher.group(2)));
        }
    }
    
    private Integer extractGuests(String text) {
        if (text == null) return null;
        
        // "for 2 guests", "2 people", "family of 4"
        Pattern guestPattern = Pattern.compile("(?:for\\s*)?(\\d+)\\s*(?:guest|people|person|member)");
        Matcher matcher = guestPattern.matcher(text.toLowerCase());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        Pattern familyPattern = Pattern.compile("family\\s*(?:of)?\\s*(\\d+)");
        Matcher familyMatcher = familyPattern.matcher(text.toLowerCase());
        if (familyMatcher.find()) {
            return Integer.parseInt(familyMatcher.group(1));
        }
        
        return null;
    }
    
    private void extractDates(String text, VoiceRequest.VoiceParameters.VoiceParametersBuilder params) {
        // Parse natural language dates
        LocalDate today = LocalDate.now();
        String lower = text.toLowerCase();
        
        if (lower.contains("tomorrow")) {
            params.checkInDate(today.plusDays(1));
        } else if (lower.contains("today")) {
            params.checkInDate(today);
        } else if (lower.contains("next week")) {
            params.checkInDate(today.plusWeeks(1));
        }
        
        // Extract nights
        Pattern nightsPattern = Pattern.compile("(\\d+)\\s*night");
        Matcher matcher = nightsPattern.matcher(lower);
        if (matcher.find()) {
            int nights = Integer.parseInt(matcher.group(1));
            LocalDate checkIn = params.build().getCheckInDate();
            if (checkIn != null) {
                params.checkOutDate(checkIn.plusDays(nights));
            }
        }
        
        // Try to parse date patterns like "January 15" or "15th January"
        try {
            Pattern datePattern = Pattern.compile("(\\d{1,2})(?:st|nd|rd|th)?\\s*(?:of)?\\s*(january|february|march|april|may|june|july|august|september|october|november|december)");
            Matcher dateMatcher = datePattern.matcher(lower);
            if (dateMatcher.find()) {
                int day = Integer.parseInt(dateMatcher.group(1));
                String monthStr = dateMatcher.group(2);
                int month = getMonthNumber(monthStr);
                int year = today.getYear();
                LocalDate parsed = LocalDate.of(year, month, day);
                if (parsed.isBefore(today)) {
                    parsed = parsed.plusYears(1);
                }
                params.checkInDate(parsed);
            }
        } catch (Exception ignored) {}
    }
    
    private int getMonthNumber(String month) {
        return switch (month.toLowerCase()) {
            case "january" -> 1;
            case "february" -> 2;
            case "march" -> 3;
            case "april" -> 4;
            case "may" -> 5;
            case "june" -> 6;
            case "july" -> 7;
            case "august" -> 8;
            case "september" -> 9;
            case "october" -> 10;
            case "november" -> 11;
            case "december" -> 12;
            default -> 1;
        };
    }
    
    private void extractIds(String text, VoiceRequest.VoiceParameters.VoiceParametersBuilder params) {
        // Extract hotel ID if mentioned (usually not via voice)
        Pattern hotelIdPattern = Pattern.compile("hotel\\s*(?:id)?\\s*(\\d+)");
        Matcher hotelMatcher = hotelIdPattern.matcher(text.toLowerCase());
        if (hotelMatcher.find()) {
            params.hotelId(Long.parseLong(hotelMatcher.group(1)));
        }
        
        // Extract room ID
        Pattern roomIdPattern = Pattern.compile("room\\s*(?:id)?\\s*(\\d+)");
        Matcher roomMatcher = roomIdPattern.matcher(text.toLowerCase());
        if (roomMatcher.find()) {
            params.roomId(Long.parseLong(roomMatcher.group(1)));
        }
    }
    
    private List<Hotel> applyFilters(List<Hotel> hotels, VoiceRequest.VoiceParameters params) {
        var stream = hotels.stream();
        
        if (params.getStarRating() != null) {
            stream = stream.filter(h -> h.getStarRating() != null && 
                    h.getStarRating().equals(params.getStarRating()));
        }
        
        return stream.collect(Collectors.toList());
    }
    
    private Map<String, Object> hotelToMap(Hotel hotel) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", hotel.getId());
        map.put("name", hotel.getName());
        map.put("city", hotel.getCity());
        map.put("country", hotel.getCountry());
        map.put("starRating", hotel.getStarRating());
        map.put("heroImageUrl", hotel.getHeroImageUrl());
        map.put("description", hotel.getDescription());
        map.put("address", hotel.getAddress());
        if (hotel.getLatitude() != null) map.put("latitude", hotel.getLatitude());
        if (hotel.getLongitude() != null) map.put("longitude", hotel.getLongitude());
        return map;
    }
    
    private Map<String, Object> roomToMap(Room room) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", room.getId());
        map.put("name", room.getName());
        map.put("roomNumber", room.getRoomNumber());
        map.put("roomType", room.getRoomType().name());
        map.put("pricePerNight", room.getPricePerNight());
        map.put("capacity", room.getCapacity());
        map.put("bedType", room.getBedType());
        map.put("imageUrl", room.getImageUrl());
        map.put("description", room.getDescription());
        return map;
    }
    
    private Map<String, Object> bookingToMap(com.hotel.domain.entity.Booking booking) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", booking.getId());
        map.put("bookingReference", booking.getBookingReference());
        map.put("hotelName", booking.getHotelName());
        map.put("roomName", booking.getRoomName());
        map.put("checkInDate", booking.getCheckInDate().toString());
        map.put("checkOutDate", booking.getCheckOutDate().toString());
        map.put("totalPrice", booking.getTotalPrice());
        map.put("status", booking.getStatus().name());
        map.put("numGuests", booking.getNumGuests());
        return map;
    }
    
    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && 
                !"anonymousUser".equals(auth.getPrincipal());
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}
