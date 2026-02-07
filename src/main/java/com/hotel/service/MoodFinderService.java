package com.hotel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.domain.dto.hotel.MoodFinderDto.*;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Room;
import com.hotel.domain.entity.RoomType;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mood-Based Hotel Finder Service.
 * 
 * Provides intelligent, data-driven hotel recommendations based on travel mood.
 * All scoring is based on real database data - no hardcoded recommendations.
 * 
 * Scoring Methodology:
 * - Amenity Match: 40% weight (primary + bonus amenities)
 * - Room Type Match: 20% weight (preferred room types available)
 * - Description Signals: 15% weight (keyword analysis)
 * - Star Rating: 15% weight (mood-appropriate rating)
 * - Booking Patterns: 10% weight (historical data signals)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MoodFinderService {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    // Scoring weights
    private static final double WEIGHT_AMENITIES = 0.40;
    private static final double WEIGHT_ROOM_TYPES = 0.20;
    private static final double WEIGHT_DESCRIPTION = 0.15;
    private static final double WEIGHT_STAR_RATING = 0.15;
    private static final double WEIGHT_BOOKING_PATTERNS = 0.10;

    // ==================== Mood Criteria Definitions ====================
    
    /**
     * Get the criteria configuration for a specific mood.
     * This defines what makes a hotel suitable for each mood.
     */
    public MoodCriteria getMoodCriteria(MoodType mood) {
        return switch (mood) {
            case ROMANTIC_GETAWAY -> MoodCriteria.builder()
                .primaryAmenities(List.of("Spa", "Pool", "Room Service", "Bar", "Restaurant"))
                .bonusAmenities(List.of("Jacuzzi", "Rooftop", "Fine Dining", "Couples", "Romantic", 
                    "Private Beach", "Butler", "Champagne", "Wellness", "Hot Tub", "Terrace"))
                .preferredRoomTypes(List.of("SUITE", "PRESIDENTIAL", "DELUXE"))
                .locationKeywords(List.of("beach", "ocean", "sea", "mountain", "view", "resort", 
                    "paradise", "island", "coastal", "waterfront"))
                .descriptionKeywords(List.of("romantic", "intimate", "couples", "getaway", "honeymoon", 
                    "anniversary", "luxur", "elegant", "exclusive", "private", "serene", "sunset"))
                .minimumStars(4)
                .scoringNotes("Prioritizes luxury amenities, romantic settings, and premium room types")
                .build();

            case ADVENTURE -> MoodCriteria.builder()
                .primaryAmenities(List.of("Free Parking", "Fitness", "Outdoor"))
                .bonusAmenities(List.of("Hiking", "Ski", "Water Sports", "Mountain", "Surfing", 
                    "Biking", "Kayak", "Tours", "Adventure", "Sports", "Beach Access", "Trail"))
                .preferredRoomTypes(List.of("DOUBLE", "TWIN", "FAMILY"))
                .locationKeywords(List.of("mountain", "alpine", "beach", "ocean", "trail", "park", 
                    "outdoor", "nature", "wilderness", "lake", "forest", "canyon"))
                .descriptionKeywords(List.of("adventure", "outdoor", "hiking", "ski", "sport", "active", 
                    "explore", "thrill", "excitement", "expedition", "journey", "discover"))
                .minimumStars(3)
                .scoringNotes("Focuses on outdoor activities, adventure amenities, and active locations")
                .build();

            case RELAXATION -> MoodCriteria.builder()
                .primaryAmenities(List.of("Spa", "Pool", "Wellness", "Fitness"))
                .bonusAmenities(List.of("Yoga", "Meditation", "Massage", "Sauna", "Steam", "Garden", 
                    "Jacuzzi", "Hot Tub", "Organic", "Detox", "Treatment", "Retreat", "Holistic"))
                .preferredRoomTypes(List.of("SUITE", "DELUXE", "PRESIDENTIAL"))
                .locationKeywords(List.of("retreat", "wellness", "peaceful", "tranquil", "serene", 
                    "quiet", "garden", "nature", "sanctuary", "oasis"))
                .descriptionKeywords(List.of("relax", "wellness", "rejuvenat", "peaceful", "tranquil", 
                    "serene", "spa", "unwind", "escape", "retreat", "restore", "heal", "calm"))
                .minimumStars(4)
                .scoringNotes("Emphasizes wellness facilities, spa services, and peaceful environments")
                .build();

            case FAMILY_FUN -> MoodCriteria.builder()
                .primaryAmenities(List.of("Pool", "Free WiFi", "Restaurant", "Free Parking"))
                .bonusAmenities(List.of("Kids Club", "Family", "Playground", "Game Room", "Water Park", 
                    "BBQ", "Kitchen", "Laundry", "Babysitting", "Children", "Activities", "Beach"))
                .preferredRoomTypes(List.of("FAMILY", "SUITE", "DOUBLE"))
                .locationKeywords(List.of("beach", "resort", "theme", "park", "zoo", "attraction", 
                    "family", "kid", "child"))
                .descriptionKeywords(List.of("family", "kid", "child", "fun", "play", "vacation", 
                    "memories", "activities", "entertainment", "adventure", "together"))
                .minimumStars(3)
                .scoringNotes("Prioritizes family amenities, spacious rooms, and kid-friendly facilities")
                .build();

            case BUSINESS -> MoodCriteria.builder()
                .primaryAmenities(List.of("Free WiFi", "Business Center", "Fitness"))
                .bonusAmenities(List.of("Meeting Room", "Conference", "Executive", "Lounge", "Concierge", 
                    "Valet", "Room Service", "24-hour", "Printer", "Work Desk", "Fiber", "High-Speed"))
                .preferredRoomTypes(List.of("SINGLE", "DOUBLE", "SUITE"))
                .locationKeywords(List.of("downtown", "financial", "business", "city center", "central", 
                    "metropolitan", "corporate", "district"))
                .descriptionKeywords(List.of("business", "professional", "corporate", "executive", 
                    "productive", "efficient", "modern", "convenient", "central"))
                .minimumStars(3)
                .scoringNotes("Focuses on productivity amenities, convenient location, and professional services")
                .build();
        };
    }

    // ==================== Main Search Methods ====================

    /**
     * Find hotels matching a specific mood.
     */
    public MoodSearchResponse findHotelsByMood(MoodSearchRequest request) {
        log.info("Finding hotels for mood: {}", request.getMood());
        
        MoodType mood = request.getMood();
        MoodCriteria criteria = getMoodCriteria(mood);
        
        // Get all active hotels
        List<Hotel> allHotels = hotelRepository.findByIsActiveTrue();
        
        // Apply optional filters
        List<Hotel> filteredHotels = applyFilters(allHotels, request);
        
        // Score each hotel against the mood criteria
        List<MoodHotelMatch> scoredHotels = filteredHotels.stream()
            .map(hotel -> scoreHotel(hotel, mood, criteria))
            .filter(match -> match.getMatchScore() > 20) // Minimum relevance threshold
            .sorted(Comparator.comparingInt(MoodHotelMatch::getMatchScore).reversed())
            .limit(request.getLimit() != null ? request.getLimit() : 10)
            .collect(Collectors.toList());
        
        log.info("Found {} hotels matching mood {} (from {} candidates)", 
            scoredHotels.size(), mood, filteredHotels.size());
        
        return MoodSearchResponse.builder()
            .mood(mood)
            .moodDescription(mood.getDescription())
            .totalMatches(scoredHotels.size())
            .hotels(scoredHotels)
            .criteriaUsed(criteria)
            .build();
    }

    /**
     * Get all available moods with hotel counts.
     */
    public MoodListResponse getAllMoodsWithCounts() {
        List<Hotel> allHotels = hotelRepository.findByIsActiveTrue();
        
        List<MoodSummary> moodSummaries = Arrays.stream(MoodType.values())
            .map(mood -> {
                MoodCriteria criteria = getMoodCriteria(mood);
                
                // Score all hotels for this mood
                List<MoodHotelMatch> matches = allHotels.stream()
                    .map(hotel -> scoreHotel(hotel, mood, criteria))
                    .filter(match -> match.getMatchScore() > 40) // Good match threshold
                    .sorted(Comparator.comparingInt(MoodHotelMatch::getMatchScore).reversed())
                    .toList();
                
                String topMatch = matches.isEmpty() ? null : matches.get(0).getHotelName();
                
                return MoodSummary.builder()
                    .mood(mood)
                    .displayName(mood.getDisplayName())
                    .description(mood.getDescription())
                    .icon(mood.getIcon())
                    .matchingHotelsCount(matches.size())
                    .topMatchPreview(topMatch)
                    .build();
            })
            .collect(Collectors.toList());
        
        return MoodListResponse.builder()
            .moods(moodSummaries)
            .totalHotels(allHotels.size())
            .build();
    }

    // ==================== Scoring Methods ====================

    /**
     * Score a hotel against mood criteria.
     */
    private MoodHotelMatch scoreHotel(Hotel hotel, MoodType mood, MoodCriteria criteria) {
        Map<String, Integer> scoreBreakdown = new LinkedHashMap<>();
        List<String> matchReasons = new ArrayList<>();
        List<String> relevantAmenities = new ArrayList<>();
        List<String> relevantRoomTypes = new ArrayList<>();
        
        // 1. Score amenities (40%)
        int amenityScore = scoreAmenities(hotel, criteria, relevantAmenities, matchReasons);
        scoreBreakdown.put("amenities", amenityScore);
        
        // 2. Score room types (20%)
        int roomTypeScore = scoreRoomTypes(hotel, criteria, relevantRoomTypes, matchReasons);
        scoreBreakdown.put("roomTypes", roomTypeScore);
        
        // 3. Score description (15%)
        int descriptionScore = scoreDescription(hotel, criteria, matchReasons);
        scoreBreakdown.put("description", descriptionScore);
        
        // 4. Score star rating (15%)
        int starScore = scoreStarRating(hotel, criteria, mood, matchReasons);
        scoreBreakdown.put("starRating", starScore);
        
        // 5. Score booking patterns (10%)
        int bookingScore = scoreBookingPatterns(hotel, mood, matchReasons);
        scoreBreakdown.put("bookingPatterns", bookingScore);
        
        // Calculate weighted total
        int totalScore = (int) Math.round(
            amenityScore * WEIGHT_AMENITIES +
            roomTypeScore * WEIGHT_ROOM_TYPES +
            descriptionScore * WEIGHT_DESCRIPTION +
            starScore * WEIGHT_STAR_RATING +
            bookingScore * WEIGHT_BOOKING_PATTERNS
        );
        
        // Determine match level
        String matchLevel = determineMatchLevel(totalScore);
        
        // Get minimum price
        BigDecimal minPrice = hotel.getRooms().stream()
            .filter(Room::getIsAvailable)
            .map(Room::getPricePerNight)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        return MoodHotelMatch.builder()
            .hotelId(hotel.getId())
            .hotelName(hotel.getName())
            .city(hotel.getCity())
            .country(hotel.getCountry())
            .starRating(hotel.getStarRating())
            .heroImageUrl(hotel.getHeroImageUrl())
            .minPrice(minPrice)
            .currency("INR")
            .matchScore(totalScore)
            .matchLevel(matchLevel)
            .matchReasons(matchReasons)
            .scoreBreakdown(scoreBreakdown)
            .relevantAmenities(relevantAmenities)
            .relevantRoomTypes(relevantRoomTypes)
            .build();
    }

    /**
     * Score hotel amenities against mood criteria.
     */
    private int scoreAmenities(Hotel hotel, MoodCriteria criteria, 
                               List<String> relevantAmenities, List<String> matchReasons) {
        List<String> hotelAmenities = parseAmenities(hotel.getAmenities());
        if (hotelAmenities.isEmpty()) return 0;
        
        int primaryMatches = 0;
        int bonusMatches = 0;
        
        // Check primary amenities (weighted more heavily)
        for (String required : criteria.getPrimaryAmenities()) {
            if (containsAmenity(hotelAmenities, required)) {
                primaryMatches++;
                relevantAmenities.add(required);
            }
        }
        
        // Check bonus amenities
        for (String bonus : criteria.getBonusAmenities()) {
            if (containsAmenity(hotelAmenities, bonus)) {
                bonusMatches++;
                if (!relevantAmenities.contains(bonus)) {
                    relevantAmenities.add(bonus);
                }
            }
        }
        
        // Calculate score: primary amenities worth more
        int primaryScore = (primaryMatches * 100) / Math.max(criteria.getPrimaryAmenities().size(), 1);
        int bonusScore = (bonusMatches * 50) / Math.max(criteria.getBonusAmenities().size(), 1);
        int totalScore = Math.min(100, (primaryScore * 70 + bonusScore * 30) / 100);
        
        if (primaryMatches > 0) {
            matchReasons.add("Has " + primaryMatches + " key amenities: " + 
                String.join(", ", relevantAmenities.subList(0, Math.min(3, relevantAmenities.size()))));
        }
        
        return totalScore;
    }

    /**
     * Score room types against mood preferences.
     */
    private int scoreRoomTypes(Hotel hotel, MoodCriteria criteria,
                               List<String> relevantRoomTypes, List<String> matchReasons) {
        List<Room> availableRooms = hotel.getRooms().stream()
            .filter(Room::getIsAvailable)
            .toList();
        
        if (availableRooms.isEmpty()) return 0;
        
        Set<String> preferredTypes = new HashSet<>(criteria.getPreferredRoomTypes());
        int matches = 0;
        
        for (Room room : availableRooms) {
            String roomTypeName = room.getRoomType().name();
            if (preferredTypes.contains(roomTypeName)) {
                matches++;
                if (!relevantRoomTypes.contains(roomTypeName)) {
                    relevantRoomTypes.add(roomTypeName);
                }
            }
        }
        
        int score = (matches * 100) / Math.max(preferredTypes.size(), 1);
        score = Math.min(100, score);
        
        if (!relevantRoomTypes.isEmpty()) {
            matchReasons.add("Offers " + String.join(", ", relevantRoomTypes) + " rooms");
        }
        
        return score;
    }

    /**
     * Score hotel description for mood-relevant keywords.
     */
    private int scoreDescription(Hotel hotel, MoodCriteria criteria, List<String> matchReasons) {
        String description = hotel.getDescription();
        if (description == null || description.isEmpty()) return 30; // Neutral score
        
        String lowerDesc = description.toLowerCase();
        String lowerName = hotel.getName().toLowerCase();
        String combined = lowerDesc + " " + lowerName;
        
        int keywordMatches = 0;
        List<String> foundKeywords = new ArrayList<>();
        
        for (String keyword : criteria.getDescriptionKeywords()) {
            if (combined.contains(keyword.toLowerCase())) {
                keywordMatches++;
                foundKeywords.add(keyword);
            }
        }
        
        // Also check location keywords
        for (String locKeyword : criteria.getLocationKeywords()) {
            String cityLower = hotel.getCity().toLowerCase();
            if (combined.contains(locKeyword.toLowerCase()) || cityLower.contains(locKeyword.toLowerCase())) {
                keywordMatches++;
            }
        }
        
        int maxKeywords = criteria.getDescriptionKeywords().size() + criteria.getLocationKeywords().size();
        int score = (keywordMatches * 100) / Math.max(maxKeywords, 1);
        score = Math.min(100, score * 2); // Boost since partial matches are good
        
        if (!foundKeywords.isEmpty() && foundKeywords.size() >= 2) {
            matchReasons.add("Description emphasizes: " + 
                String.join(", ", foundKeywords.subList(0, Math.min(3, foundKeywords.size()))));
        }
        
        return score;
    }

    /**
     * Score star rating appropriateness for mood.
     */
    private int scoreStarRating(Hotel hotel, MoodCriteria criteria, 
                                MoodType mood, List<String> matchReasons) {
        int stars = hotel.getStarRating() != null ? hotel.getStarRating() : 3;
        int minStars = criteria.getMinimumStars();
        
        int score;
        
        switch (mood) {
            case ROMANTIC_GETAWAY, RELAXATION -> {
                // Prefer higher ratings
                if (stars >= 5) score = 100;
                else if (stars == 4) score = 80;
                else if (stars == 3) score = 50;
                else score = 20;
                
                if (stars >= 4) {
                    matchReasons.add(stars + "-star luxury property");
                }
            }
            case BUSINESS -> {
                // 3-5 stars are all good
                if (stars >= 4) score = 100;
                else if (stars == 3) score = 90;
                else score = 60;
            }
            case ADVENTURE, FAMILY_FUN -> {
                // More flexible on ratings
                if (stars >= 4) score = 100;
                else if (stars == 3) score = 85;
                else score = 70;
            }
            default -> {
                score = stars >= minStars ? 80 : 50;
            }
        }
        
        return score;
    }

    /**
     * Score based on booking patterns (if available).
     * Analyzes special requests and booking characteristics.
     */
    private int scoreBookingPatterns(Hotel hotel, MoodType mood, List<String> matchReasons) {
        // Get hotel bookings
        var bookings = bookingRepository.findByHotelId(hotel.getId());
        
        if (bookings.isEmpty()) {
            return 50; // Neutral score if no booking history
        }
        
        int patternScore = 50; // Base score
        
        // Analyze special requests and booking patterns
        long relevantRequests = bookings.stream()
            .filter(b -> b.getSpecialRequests() != null)
            .filter(b -> {
                String requests = b.getSpecialRequests().toLowerCase();
                return switch (mood) {
                    case ROMANTIC_GETAWAY -> requests.contains("anniversary") || 
                        requests.contains("honeymoon") || requests.contains("romantic") ||
                        requests.contains("celebration") || requests.contains("couple");
                    case FAMILY_FUN -> requests.contains("family") || 
                        requests.contains("kids") || requests.contains("children") ||
                        requests.contains("crib") || requests.contains("baby");
                    case BUSINESS -> requests.contains("business") || 
                        requests.contains("meeting") || requests.contains("conference") ||
                        requests.contains("early checkout") || requests.contains("late");
                    case ADVENTURE -> requests.contains("early") || 
                        requests.contains("hiking") || requests.contains("tour") ||
                        requests.contains("adventure") || requests.contains("outdoor");
                    case RELAXATION -> requests.contains("quiet") || 
                        requests.contains("relax") || requests.contains("spa") ||
                        requests.contains("peaceful") || requests.contains("late checkout");
                };
            })
            .count();
        
        if (relevantRequests > 0) {
            patternScore += Math.min(50, (int)(relevantRequests * 10));
            matchReasons.add("Popular with " + mood.getDisplayName().toLowerCase() + " travelers");
        }
        
        // Analyze room type popularity for the mood
        Map<RoomType, Long> roomTypeCounts = bookings.stream()
            .collect(Collectors.groupingBy(
                b -> b.getRoom().getRoomType(),
                Collectors.counting()
            ));
        
        // Boost if mood-appropriate rooms are frequently booked
        MoodCriteria criteria = getMoodCriteria(mood);
        for (String preferredType : criteria.getPreferredRoomTypes()) {
            try {
                RoomType type = RoomType.valueOf(preferredType);
                if (roomTypeCounts.getOrDefault(type, 0L) > 0) {
                    patternScore += 10;
                }
            } catch (IllegalArgumentException ignored) {}
        }
        
        return Math.min(100, patternScore);
    }

    // ==================== Helper Methods ====================

    private List<Hotel> applyFilters(List<Hotel> hotels, MoodSearchRequest request) {
        return hotels.stream()
            .filter(h -> {
                // Location filter
                if (request.getLocation() != null && !request.getLocation().isEmpty()) {
                    String loc = request.getLocation().toLowerCase();
                    boolean matches = h.getCity().toLowerCase().contains(loc) ||
                        h.getCountry().toLowerCase().contains(loc);
                    if (!matches) return false;
                }
                
                // Star rating filter
                if (request.getMinStars() != null) {
                    if (h.getStarRating() == null || h.getStarRating() < request.getMinStars()) {
                        return false;
                    }
                }
                
                // Price filter
                if (request.getMaxPrice() != null) {
                    BigDecimal minPrice = h.getRooms().stream()
                        .filter(Room::getIsAvailable)
                        .map(Room::getPricePerNight)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                    if (minPrice.compareTo(request.getMaxPrice()) > 0) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }

    private List<String> parseAmenities(String amenitiesJson) {
        if (amenitiesJson == null || amenitiesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(amenitiesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse amenities JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean containsAmenity(List<String> hotelAmenities, String searchAmenity) {
        String searchLower = searchAmenity.toLowerCase();
        return hotelAmenities.stream()
            .anyMatch(a -> a.toLowerCase().contains(searchLower) || 
                          searchLower.contains(a.toLowerCase()));
    }

    private String determineMatchLevel(int score) {
        if (score >= 75) return "EXCELLENT";
        if (score >= 50) return "GOOD";
        if (score >= 30) return "FAIR";
        return "LOW";
    }
}
