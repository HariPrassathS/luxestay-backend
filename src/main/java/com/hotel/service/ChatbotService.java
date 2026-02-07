package com.hotel.service;

import com.hotel.domain.chatbot.ChatbotRequest;
import com.hotel.domain.chatbot.ChatbotResponse;
import com.hotel.domain.entity.Hotel;
import com.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Chatbot Service - Intelligence Layer
 * This is the SINGLE SOURCE OF TRUTH for chatbot query processing.
 * All business logic and database queries happen here, NOT in frontend.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatbotService {
    
    private final HotelRepository hotelRepository;
    private final TravelKnowledgeService knowledgeService;
    
    /**
     * Main entry point for processing chatbot queries
     */
    public ChatbotResponse processQuery(ChatbotRequest request) {
        log.info("Processing chatbot query: intent={}, message={}", request.getIntent(), request.getMessage());
        
        try {
            return switch (request.getIntentType()) {
                case HOTEL_SEARCH -> handleHotelSearch(request);
                case LUXURY_SEARCH -> handleLuxurySearch(request);
                case BUDGET_SEARCH -> handleBudgetSearch(request);
                case PRICE_QUERY -> handlePriceQuery(request);
                case CITY_INFO -> handleCityInfo(request);
                case DISTANCE_QUERY -> handleDistanceQuery(request);
                case WEATHER_QUERY -> handleWeatherQuery(request);
                case FOOD_QUERY -> handleFoodQuery(request);
                case ATTRACTIONS_QUERY -> handleAttractionsQuery(request);
                case PACKAGE_QUERY -> handlePackageQuery(request);
                case GREETING -> handleGreeting(request);
                case HELP -> handleHelp(request);
                default -> handleGeneralQuery(request);
            };
        } catch (Exception e) {
            log.error("Error processing chatbot query", e);
            return ChatbotResponse.fallback(
                "I apologize, but I encountered an error processing your request. Please try again.",
                List.of("Hotels in Chennai", "Help", "Browse Hotels")
            );
        }
    }
    
    // ========== INTENT HANDLERS ==========
    
    private ChatbotResponse handleHotelSearch(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        if (city == null) {
            // No city specified, show all cities
            List<String> cities = getCitiesWithHotels();
            return ChatbotResponse.success(
                "HOTEL_SEARCH",
                "🏨 Which city would you like to find hotels in?",
                Map.of("availableCities", cities),
                cities.stream().limit(5).map(c -> "Hotels in " + c).toList()
            );
        }
        
        // Search hotels by city
        List<Hotel> hotels = hotelRepository.findByCityIgnoreCaseAndIsActiveTrue(city);
        
        if (hotels.isEmpty()) {
            // Try partial city match
            hotels = hotelRepository.searchHotels(city);
        }
        
        // Sort by star rating and limit
        List<Map<String, Object>> hotelData = hotels.stream()
                .sorted((a, b) -> Integer.compare(
                    b.getStarRating() != null ? b.getStarRating() : 0,
                    a.getStarRating() != null ? a.getStarRating() : 0))
                .limit(5)
                .map(this::hotelToMap)
                .toList();
        
        if (hotelData.isEmpty()) {
            var cityInfo = knowledgeService.getCityInfo(city);
            if (cityInfo != null) {
                return ChatbotResponse.success(
                    "HOTEL_SEARCH",
                    String.format("📍 %s is a beautiful destination! %s\n\nWe don't have hotels listed here yet. Check our other destinations!", 
                        city, cityInfo.getDescription()),
                    Map.of("cityInfo", cityInfo),
                    List.of("Hotels in Chennai", "Hotels in Ooty", "All destinations")
                );
            }
            return ChatbotResponse.success(
                "HOTEL_SEARCH",
                String.format("I don't have hotels in %s yet. Try these popular cities:", city),
                Map.of(),
                List.of("Hotels in Chennai", "Hotels in Madurai", "Hotels in Ooty")
            );
        }
        
        return ChatbotResponse.success(
            "HOTEL_SEARCH",
            String.format("🏨 Found <strong>%d hotels</strong> in %s!\n\n⭐ Showing top rated options:", hotelData.size(), city),
            Map.of("hotels", hotelData, "totalCount", hotelData.size(), "city", city),
            List.of("Budget hotels in " + city, "Luxury hotels in " + city, "View on map")
        );
    }
    
    private ChatbotResponse handleLuxurySearch(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        var query = hotelRepository.findByIsActiveTrue().stream()
                .filter(h -> h.getStarRating() != null && h.getStarRating() >= 4);
        
        if (city != null) {
            String finalCity = city;
            query = query.filter(h -> h.getCity() != null && 
                    h.getCity().toLowerCase().contains(finalCity.toLowerCase()));
        }
        
        List<Map<String, Object>> luxuryHotels = query
                .sorted((a, b) -> Integer.compare(
                    b.getStarRating() != null ? b.getStarRating() : 0,
                    a.getStarRating() != null ? a.getStarRating() : 0))
                .limit(5)
                .map(this::hotelToMap)
                .toList();
        
        String locationText = city != null ? " in " + city : "";
        
        if (luxuryHotels.isEmpty()) {
            return ChatbotResponse.success(
                "LUXURY_SEARCH",
                String.format("✨ No luxury hotels found%s. Try a different city!", locationText),
                Map.of(),
                List.of("Luxury in Chennai", "Luxury in Ooty", "All hotels")
            );
        }
        
        return ChatbotResponse.success(
            "LUXURY_SEARCH",
            String.format("✨ <strong>Luxury Hotels%s</strong>\n\nOur finest properties with world-class amenities:", locationText),
            Map.of("hotels", luxuryHotels, "totalCount", luxuryHotels.size()),
            List.of("Show more luxury", "Mid-range options", "View amenities")
        );
    }
    
    private ChatbotResponse handleBudgetSearch(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        int maxPrice = extractMaxPrice(request.getMessage(), 5000);
        
        var query = hotelRepository.findByIsActiveTrue().stream();
        
        if (city != null) {
            String finalCity = city;
            query = query.filter(h -> h.getCity() != null && 
                    h.getCity().toLowerCase().contains(finalCity.toLowerCase()));
        }
        
        List<Map<String, Object>> budgetHotels = query
                .filter(h -> h.getMinPrice() != null && h.getMinPrice() <= maxPrice)
                .sorted((a, b) -> Double.compare(a.getMinPrice(), b.getMinPrice()))
                .limit(5)
                .map(this::hotelToMap)
                .toList();
        
        String locationText = city != null ? " in " + city : "";
        
        if (budgetHotels.isEmpty()) {
            return ChatbotResponse.success(
                "BUDGET_SEARCH",
                String.format("💰 No budget hotels under ₹%,d found%s.", maxPrice, locationText),
                Map.of(),
                List.of("Under ₹5000", "Under ₹7000", "All hotels")
            );
        }
        
        return ChatbotResponse.success(
            "BUDGET_SEARCH",
            String.format("💰 <strong>Budget-Friendly Hotels%s</strong>\n\nGreat value under ₹%,d/night:", locationText, maxPrice),
            Map.of("hotels", budgetHotels, "totalCount", budgetHotels.size(), "maxPrice", maxPrice),
            List.of("Under ₹3000", "Under ₹5000", "Show more")
        );
    }
    
    private ChatbotResponse handlePriceQuery(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        // If city specified, get price range from knowledge base
        if (city != null) {
            var cityInfo = knowledgeService.getCityInfo(city);
            if (cityInfo != null && cityInfo.getPriceRange() != null) {
                var range = cityInfo.getPriceRange();
                String msg = String.format(
                    "💰 <strong>Hotel Prices in %s</strong>\n\n" +
                    "🏠 Budget: %s/night\n" +
                    "🏨 Mid-Range: %s/night\n" +
                    "🏰 Luxury: %s/night",
                    cityInfo.getName(), range.getBudget(), range.getMidRange(), range.getLuxury()
                );
                return ChatbotResponse.success(
                    "PRICE_QUERY",
                    msg,
                    Map.of("city", city, "priceRange", range),
                    List.of("Budget hotels in " + city, "Luxury hotels in " + city)
                );
            }
        }
        
        // Get overall price stats from database
        List<Hotel> allHotels = hotelRepository.findByIsActiveTrue().stream()
                .filter(h -> h.getMinPrice() != null && h.getMinPrice() > 0)
                .toList();
        
        if (allHotels.isEmpty()) {
            return ChatbotResponse.success(
                "PRICE_QUERY",
                "Our hotels range from ₹1,500 to ₹25,000+ per night. What's your budget?",
                Map.of(),
                List.of("Under ₹3000", "₹3000-₹7000", "Above ₹7000")
            );
        }
        
        double minPrice = allHotels.stream().mapToDouble(Hotel::getMinPrice).min().orElse(0);
        double maxPrice = allHotels.stream().mapToDouble(Hotel::getMinPrice).max().orElse(0);
        double avgPrice = allHotels.stream().mapToDouble(Hotel::getMinPrice).average().orElse(0);
        
        String msg = String.format(
            "💰 <strong>Hotel Prices at LuxeStay</strong>\n\n" +
            "📉 Starting from: <strong>₹%,.0f</strong>/night\n" +
            "📊 Average: <strong>₹%,.0f</strong>/night\n" +
            "📈 Luxury: up to <strong>₹%,.0f</strong>/night\n\n" +
            "💡 <em>Use filters to find hotels in your budget!</em>",
            minPrice, avgPrice, maxPrice
        );
        
        return ChatbotResponse.success(
            "PRICE_QUERY",
            msg,
            Map.of("minPrice", minPrice, "maxPrice", maxPrice, "avgPrice", avgPrice),
            List.of("Budget hotels", "Luxury hotels", "Best deals")
        );
    }
    
    private ChatbotResponse handleCityInfo(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        if (city == null) {
            return ChatbotResponse.success(
                "CITY_INFO",
                "🗺️ Which city would you like to know about?",
                Map.of("availableCities", knowledgeService.getAllCities()),
                List.of("About Chennai", "About Ooty", "About Madurai")
            );
        }
        
        var cityInfo = knowledgeService.getCityInfo(city);
        if (cityInfo == null) {
            return ChatbotResponse.success(
                "CITY_INFO",
                String.format("I don't have detailed information about %s. Try these cities:", city),
                Map.of(),
                List.of("About Chennai", "About Ooty", "About Madurai")
            );
        }
        
        String msg = String.format(
            "🏙️ <strong>%s</strong> - %s\n\n%s\n\n" +
            "🌤️ <strong>Best Time:</strong> %s\n" +
            "🌡️ <strong>Climate:</strong> %s",
            cityInfo.getName(), cityInfo.getType(), cityInfo.getDescription(),
            cityInfo.getBestTime(), cityInfo.getWeather()
        );
        
        return ChatbotResponse.success(
            "CITY_INFO",
            msg,
            Map.of("cityInfo", cityInfo),
            List.of("Hotels in " + city, "Attractions in " + city, "Food in " + city)
        );
    }
    
    private ChatbotResponse handleDistanceQuery(ChatbotRequest request) {
        String[] cities = knowledgeService.extractCitiesForDistance(request.getMessage());
        
        if (cities == null || cities.length < 2) {
            return ChatbotResponse.success(
                "DISTANCE_QUERY",
                "📏 I can calculate distances between Tamil Nadu cities! Try asking:\n\n" +
                "• \"Distance from Chennai to Ooty\"\n" +
                "• \"How far is Madurai from Kodaikanal?\"",
                Map.of(),
                List.of("Chennai to Ooty", "Madurai to Kodaikanal", "Chennai to Madurai")
            );
        }
        
        var distance = knowledgeService.getDistance(cities[0], cities[1]);
        if (distance == null) {
            return ChatbotResponse.success(
                "DISTANCE_QUERY",
                String.format("I don't have distance data between %s and %s.", cities[0], cities[1]),
                Map.of(),
                List.of("Chennai to Ooty", "Madurai to Rameswaram")
            );
        }
        
        var city1Info = knowledgeService.getCityInfo(cities[0]);
        var city2Info = knowledgeService.getCityInfo(cities[1]);
        String city1Name = city1Info != null ? city1Info.getName() : cities[0];
        String city2Name = city2Info != null ? city2Info.getName() : cities[1];
        
        String msg = String.format(
            "📏 <strong>Distance: %s → %s</strong>\n\n" +
            "🚗 <strong>Distance:</strong> %d km\n" +
            "⏱️ <strong>Travel Time:</strong> %s by road\n\n" +
            "🛤️ <strong>Route:</strong> %s",
            city1Name, city2Name, distance.getKm(), distance.getTravelTime(), distance.getRoute()
        );
        
        return ChatbotResponse.success(
            "DISTANCE_QUERY",
            msg,
            Map.of("from", city1Name, "to", city2Name, "distance", distance),
            List.of("Hotels in " + city2Name, "About " + city2Name, "Other routes")
        );
    }
    
    private ChatbotResponse handleWeatherQuery(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        if (city == null) {
            return ChatbotResponse.success(
                "WEATHER_QUERY",
                "🌤️ Which city's weather would you like to know?",
                Map.of(),
                List.of("Weather in Ooty", "Best time for Kodaikanal", "Chennai climate")
            );
        }
        
        var cityInfo = knowledgeService.getCityInfo(city);
        if (cityInfo == null) {
            return ChatbotResponse.success(
                "WEATHER_QUERY",
                String.format("I don't have weather data for %s.", city),
                Map.of(),
                List.of("Weather in Ooty", "Weather in Chennai")
            );
        }
        
        String msg = String.format(
            "🌤️ <strong>Weather in %s</strong>\n\n" +
            "📅 <strong>Best Time to Visit:</strong> %s\n\n" +
            "🌡️ <strong>Climate:</strong> %s\n\n" +
            "☀️ Pack accordingly and enjoy your trip!",
            cityInfo.getName(), cityInfo.getBestTime(), cityInfo.getWeather()
        );
        
        return ChatbotResponse.success(
            "WEATHER_QUERY",
            msg,
            Map.of("city", city, "bestTime", cityInfo.getBestTime(), "weather", cityInfo.getWeather()),
            List.of("Hotels in " + city, "Attractions in " + city, "Travel tips")
        );
    }
    
    private ChatbotResponse handleFoodQuery(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        if (city == null) {
            return ChatbotResponse.success(
                "FOOD_QUERY",
                "🍽️ Tamil Nadu has amazing cuisine! Which city's food would you like to explore?",
                Map.of(),
                List.of("Chennai food", "Madurai food", "Find restaurants")
            );
        }
        
        var cityInfo = knowledgeService.getCityInfo(city);
        if (cityInfo == null || cityInfo.getFood() == null) {
            return ChatbotResponse.success(
                "FOOD_QUERY",
                String.format("I don't have food data for %s.", city),
                Map.of(),
                List.of("Chennai food", "Madurai food")
            );
        }
        
        String foodList = cityInfo.getFood().stream()
                .map(f -> "• " + f)
                .collect(Collectors.joining("\n"));
        
        String msg = String.format(
            "🍽️ <strong>Must-Try Food in %s</strong>\n\n%s\n\n" +
            "😋 Don't miss these local specialties!",
            cityInfo.getName(), foodList
        );
        
        return ChatbotResponse.success(
            "FOOD_QUERY",
            msg,
            Map.of("city", city, "food", cityInfo.getFood()),
            List.of("Hotels in " + city, "Attractions in " + city)
        );
    }
    
    private ChatbotResponse handleAttractionsQuery(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        if (city == null) {
            return ChatbotResponse.success(
                "ATTRACTIONS_QUERY",
                "🎯 Which city's attractions would you like to explore?",
                Map.of(),
                List.of("Places in Chennai", "Things to do in Ooty", "Madurai attractions")
            );
        }
        
        var cityInfo = knowledgeService.getCityInfo(city);
        if (cityInfo == null || cityInfo.getAttractions() == null) {
            return ChatbotResponse.success(
                "ATTRACTIONS_QUERY",
                String.format("I don't have attraction data for %s.", city),
                Map.of(),
                List.of("Chennai attractions", "Ooty attractions")
            );
        }
        
        String attractionsList = cityInfo.getAttractions().stream()
                .map(a -> "• " + a)
                .collect(Collectors.joining("\n"));
        
        String msg = String.format(
            "🎯 <strong>Top Attractions in %s</strong>\n\n%s",
            cityInfo.getName(), attractionsList
        );
        
        return ChatbotResponse.success(
            "ATTRACTIONS_QUERY",
            msg,
            Map.of("city", city, "attractions", cityInfo.getAttractions()),
            List.of("Hotels in " + city, "Weather in " + city, "Food in " + city)
        );
    }
    
    private ChatbotResponse handlePackageQuery(ChatbotRequest request) {
        String city = knowledgeService.extractCity(request.getMessage());
        
        List<TravelKnowledgeService.TravelPackage> packages = city != null ?
                knowledgeService.getPackagesByCity(city) :
                knowledgeService.getAllPackages();
        
        StringBuilder msg = new StringBuilder("📦 <strong>Travel Packages</strong>\n\n");
        
        for (var pkg : packages) {
            msg.append(String.format("🎫 <strong>%s</strong>\n", pkg.getName()));
            msg.append(String.format("📅 %s | 💰 From %s\n", pkg.getDuration(), pkg.getPriceFrom()));
            msg.append(String.format("📍 %s\n\n", String.join(" → ", pkg.getCities())));
        }
        
        return ChatbotResponse.success(
            "PACKAGE_QUERY",
            msg.toString(),
            Map.of("packages", packages),
            List.of("Temple tour", "Hill station package", "Contact us")
        );
    }
    
    private ChatbotResponse handleGreeting(ChatbotRequest request) {
        long hotelCount = hotelRepository.count();
        List<String> cities = getCitiesWithHotels();
        
        String msg = String.format(
            "Hello! 👋 Welcome to <strong>LuxeStay</strong>!\n\n" +
            "I have access to <strong>%d hotels</strong> across <strong>%d cities</strong>. " +
            "I specialize in Tamil Nadu destinations!\n\n" +
            "Ask me about:\n" +
            "• 🏨 Hotels in any city\n" +
            "• 📏 Distances between cities\n" +
            "• 🎯 Attractions & things to do\n" +
            "• 🌤️ Best time to visit\n" +
            "• 🍽️ Local food & specialties",
            hotelCount, cities.size()
        );
        
        return ChatbotResponse.success(
            "GREETING",
            msg,
            Map.of("hotelCount", hotelCount, "cityCount", cities.size()),
            List.of("Hotels in Chennai", "Tamil Nadu destinations", "Distance calculator", "Travel tips")
        );
    }
    
    private ChatbotResponse handleHelp(ChatbotRequest request) {
        String msg = "🤖 <strong>I can help you with:</strong>\n\n" +
            "🏨 <strong>Hotels</strong> - \"Hotels in Chennai\", \"Luxury hotels\", \"Budget options\"\n" +
            "📏 <strong>Distance</strong> - \"Distance from Chennai to Ooty\"\n" +
            "🌤️ <strong>Weather</strong> - \"Best time to visit Kodaikanal\"\n" +
            "🎯 <strong>Attractions</strong> - \"Things to do in Pondicherry\"\n" +
            "🍽️ <strong>Food</strong> - \"Food in Madurai\"\n" +
            "📦 <strong>Packages</strong> - \"Travel packages\"\n" +
            "💰 <strong>Prices</strong> - \"Hotel prices in Ooty\"\n\n" +
            "Just type your question!";
        
        return ChatbotResponse.success(
            "HELP",
            msg,
            Map.of(),
            List.of("Hotels in Chennai", "Distance calculator", "Travel packages")
        );
    }
    
    private ChatbotResponse handleGeneralQuery(ChatbotRequest request) {
        // Try to extract any useful intent from the message
        String msg = request.getMessage();
        if (msg != null) {
            // Check for city mentions
            String city = knowledgeService.extractCity(msg);
            if (city != null) {
                return handleCityInfo(request);
            }
        }
        
        return ChatbotResponse.success(
            "GENERAL_QUERY",
            "I'm not sure about that, but I can help you with:\n\n" +
            "🏨 <strong>Hotels</strong> - Find & book hotels\n" +
            "📍 <strong>Destinations</strong> - Tamil Nadu cities\n" +
            "📏 <strong>Distances</strong> - City to city\n" +
            "🌤️ <strong>Weather</strong> - Best time to visit\n" +
            "🍽️ <strong>Food</strong> - Local specialties\n\n" +
            "Try asking: \"Hotels in Chennai\" or \"Distance from Chennai to Ooty\"",
            Map.of(),
            List.of("Hotels in Chennai", "Tamil Nadu destinations", "Help")
        );
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Convert Hotel entity to Map for JSON response
     */
    private Map<String, Object> hotelToMap(Hotel hotel) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hotel.getId());
        map.put("name", hotel.getName());
        map.put("city", hotel.getCity());
        map.put("rating", hotel.getStarRating() != null ? hotel.getStarRating() : 3);
        map.put("minPrice", hotel.getMinPrice());
        map.put("imageUrl", hotel.getHeroImageUrl());
        map.put("address", hotel.getAddress());
        return map;
    }
    
    private List<String> getCitiesWithHotels() {
        return hotelRepository.findByIsActiveTrue().stream()
                .map(Hotel::getCity)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }
    
    private int extractMaxPrice(String message, int defaultValue) {
        if (message == null) return defaultValue;
        
        var matcher = java.util.regex.Pattern.compile("under\\s*₹?\\s*(\\d+)|below\\s*₹?\\s*(\\d+)|(\\d+)\\s*rupees?")
                .matcher(message.toLowerCase());
        
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    return Integer.parseInt(matcher.group(i));
                }
            }
        }
        return defaultValue;
    }
    
    // ========== PUBLIC METHODS FOR CONTROLLER ==========
    
    /**
     * Get all available cities
     */
    public List<String> getAllCities() {
        Set<String> allCities = new HashSet<>();
        allCities.addAll(knowledgeService.getAllCities());
        allCities.addAll(getCitiesWithHotels());
        return allCities.stream().sorted().toList();
    }
    
    /**
     * Get suggestions for quick replies
     */
    public List<String> getSuggestions() {
        return List.of(
            "Hotels in Chennai",
            "Distance from Chennai to Ooty",
            "Best time to visit Kodaikanal",
            "Luxury hotels",
            "Travel packages",
            "Tamil Nadu destinations"
        );
    }
    
    /**
     * Get distance between two cities
     */
    public TravelKnowledgeService.DistanceInfo getDistance(String from, String to) {
        return knowledgeService.getDistance(from, to);
    }
}
