package com.hotel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.domain.dto.hotel.MoodFinderDto.MoodType;
import com.hotel.domain.dto.itinerary.ItineraryDto.*;
import com.hotel.domain.entity.Attraction;
import com.hotel.domain.entity.Attraction.AttractionCategory;
import com.hotel.domain.entity.Attraction.TimeOfDay;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.Hotel;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.AttractionRepository;
import com.hotel.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Trip Intelligence & Smart Itinerary Generator Service.
 * 
 * Generates realistic, data-driven day-by-day trip itineraries using:
 * - Hotel location and coordinates
 * - Booking dates and duration
 * - Database attractions filtered by distance and mood
 * - Intelligent activity distribution across days
 * - Explainable recommendations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final BookingRepository bookingRepository;
    private final AttractionRepository attractionRepository;
    private final ObjectMapper objectMapper;

    // Configuration constants
    private static final double DEFAULT_SEARCH_RADIUS_KM = 50.0;
    private static final int AVERAGE_SPEED_KMH = 30; // Average city travel speed

    // Mood-based activity limits per day (less for relaxation, more for adventure)
    private static final Map<MoodType, Integer> MOOD_ACTIVITY_LIMITS = Map.of(
            MoodType.ROMANTIC_GETAWAY, 2,
            MoodType.RELAXATION, 2,
            MoodType.BUSINESS, 2,
            MoodType.FAMILY_FUN, 3,
            MoodType.ADVENTURE, 4
    );

    // ==================== Main Itinerary Generation ====================

    /**
     * Generate a complete trip itinerary for a booking.
     */
    @Transactional(readOnly = true)
    public ItineraryResponse generateItinerary(Long bookingId, MoodType travelMood) {
        log.info("Generating itinerary for booking {} with mood {}", bookingId, travelMood);

        // 1. Fetch booking with related data
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        Hotel hotel = booking.getRoom().getHotel();
        
        // Validate hotel has coordinates
        if (hotel.getLatitude() == null || hotel.getLongitude() == null) {
            log.warn("Hotel {} has no coordinates, using city-based search", hotel.getId());
        }

        // 2. Build trip context
        TripContext tripContext = buildTripContext(booking, hotel, travelMood);

        // 3. Fetch nearby attractions
        List<Attraction> attractions = fetchNearbyAttractions(hotel, travelMood);
        log.info("Found {} attractions for itinerary in {}", attractions.size(), hotel.getCity());

        // 4. Generate day plans (ALWAYS generates plans - uses fallbacks if needed)
        List<DayPlan> dayPlans = generateDayPlans(tripContext, attractions, hotel, travelMood);

        // 5. Build summary
        ItinerarySummary summary = buildItinerarySummary(dayPlans, travelMood);

        // 6. Generate tips
        List<String> tips = generateTravelTips(tripContext, hotel, travelMood);

        return ItineraryResponse.builder()
                .bookingId(bookingId)
                .bookingReference(booking.getBookingReference())
                .tripContext(tripContext)
                .dayPlans(dayPlans)
                .summary(summary)
                .tips(tips)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    /**
     * Generate itinerary for a specific user with ownership validation.
     * Ensures the user can only access itineraries for their own bookings.
     */
    @Transactional(readOnly = true)
    public ItineraryResponse generateItineraryForUser(Long bookingId, MoodType travelMood, String userEmail) {
        log.info("Generating itinerary for booking {} for user {}", bookingId, userEmail);

        // Validate ownership - user must own this booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "You are not authorized to access this booking's itinerary");
        }
        
        // Delegate to main method
        return generateItinerary(bookingId, travelMood);
    }

    // ==================== Trip Context Building ====================

    private TripContext buildTripContext(Booking booking, Hotel hotel, MoodType travelMood) {
        int totalNights = (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        int totalDays = totalNights + 1; // Include check-in day

        List<String> amenities = parseAmenities(hotel.getAmenities());

        return TripContext.builder()
                .hotelName(hotel.getName())
                .hotelAddress(hotel.getAddress())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .hotelLatitude(hotel.getLatitude())
                .hotelLongitude(hotel.getLongitude())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalNights(totalNights)
                .totalDays(totalDays)
                .checkInTime(hotel.getCheckInTime() != null ? hotel.getCheckInTime().toString() : "14:00")
                .checkOutTime(hotel.getCheckOutTime() != null ? hotel.getCheckOutTime().toString() : "11:00")
                .travelMood(travelMood)
                .hotelAmenities(amenities)
                .build();
    }

    private List<String> parseAmenities(String amenitiesJson) {
        if (amenitiesJson == null || amenitiesJson.isBlank()) {
            return List.of("Room Service", "Restaurant", "Wi-Fi"); // Default amenities
        }
        try {
            List<String> amenities = objectMapper.readValue(amenitiesJson, new TypeReference<List<String>>() {});
            return amenities.isEmpty() ? List.of("Room Service", "Restaurant", "Wi-Fi") : amenities;
        } catch (Exception e) {
            log.warn("Failed to parse amenities JSON: {}", e.getMessage());
            return List.of("Room Service", "Restaurant", "Wi-Fi");
        }
    }

    // ==================== Attraction Fetching ====================

    private List<Attraction> fetchNearbyAttractions(Hotel hotel, MoodType travelMood) {
        String city = hotel.getCity();
        
        List<Attraction> attractions = new ArrayList<>();
        
        // Try coordinate-based search first
        if (hotel.getLatitude() != null && hotel.getLongitude() != null) {
            attractions = new ArrayList<>(attractionRepository.findNearbyAttractions(
                    hotel.getLatitude(),
                    hotel.getLongitude(),
                    city,
                    DEFAULT_SEARCH_RADIUS_KM
            ));
        }
        
        // Fallback to city-based search if no results
        if (attractions.isEmpty()) {
            attractions = new ArrayList<>(attractionRepository.findByCityIgnoreCaseAndIsActiveTrue(city));
        }

        // If still empty, try broader search without city filter
        if (attractions.isEmpty()) {
            log.warn("No attractions found for city: {}, attempting broader search", city);
            attractions = new ArrayList<>(attractionRepository.findAll().stream()
                    .filter(Attraction::getIsActive)
                    .limit(20)
                    .toList());
        }

        // Filter by mood if specified and we have enough attractions
        if (travelMood != null && !attractions.isEmpty()) {
            List<Attraction> moodFiltered = filterByMood(attractions, travelMood);
            // Only use filtered if we still have at least 3 attractions
            if (moodFiltered.size() >= 3) {
                attractions = moodFiltered;
            }
        }

        // Sort by rating and distance
        attractions.sort((a, b) -> {
            int ratingCompare = Double.compare(b.getRating(), a.getRating());
            if (ratingCompare != 0) return ratingCompare;
            return a.getCategory().compareTo(b.getCategory());
        });

        return attractions;
    }

    private List<Attraction> filterByMood(List<Attraction> attractions, MoodType mood) {
        String moodStr = mood.name();
        
        return attractions.stream()
                .filter(a -> {
                    // Check if mood tag matches
                    if (a.getMoodTags() != null && a.getMoodTags().contains(moodStr)) {
                        return true;
                    }
                    // Also include based on category-mood alignment
                    return isCategoryMoodAligned(a.getCategory(), mood);
                })
                .collect(Collectors.toList());
    }

    private boolean isCategoryMoodAligned(AttractionCategory category, MoodType mood) {
        return switch (mood) {
            case ROMANTIC_GETAWAY -> Set.of(
                    AttractionCategory.RESTAURANT, AttractionCategory.BEACH,
                    AttractionCategory.WELLNESS, AttractionCategory.NATURE
            ).contains(category);
            case ADVENTURE -> Set.of(
                    AttractionCategory.ACTIVITY, AttractionCategory.NATURE,
                    AttractionCategory.LANDMARK
            ).contains(category);
            case RELAXATION -> Set.of(
                    AttractionCategory.WELLNESS, AttractionCategory.BEACH,
                    AttractionCategory.NATURE, AttractionCategory.TEMPLE
            ).contains(category);
            case FAMILY_FUN -> Set.of(
                    AttractionCategory.ACTIVITY, AttractionCategory.ENTERTAINMENT,
                    AttractionCategory.NATURE, AttractionCategory.CULTURAL
            ).contains(category);
            case BUSINESS -> Set.of(
                    AttractionCategory.RESTAURANT, AttractionCategory.SHOPPING,
                    AttractionCategory.CULTURAL
            ).contains(category);
        };
    }

    // ==================== Day Plan Generation ====================

    private List<DayPlan> generateDayPlans(TripContext context, List<Attraction> attractions, Hotel hotel, MoodType mood) {
        List<DayPlan> dayPlans = new ArrayList<>();
        Set<Long> usedAttractions = new HashSet<>();
        
        int totalDays = context.getTotalDays();
        LocalDate currentDate = context.getCheckInDate();

        // Get activity limit based on mood (fewer activities for relaxation, more for adventure)
        int activitiesPerDay = MOOD_ACTIVITY_LIMITS.getOrDefault(mood, 3);

        for (int day = 1; day <= totalDays; day++) {
            boolean isArrivalDay = (day == 1);
            boolean isDepartureDay = (day == totalDays);
            
            DayPlan dayPlan = generateSingleDayPlan(
                    day, currentDate, context, attractions,
                    usedAttractions, hotel, mood, isArrivalDay, isDepartureDay, activitiesPerDay
            );
            
            dayPlans.add(dayPlan);
            currentDate = currentDate.plusDays(1);
        }

        return dayPlans;
    }

    private DayPlan generateSingleDayPlan(
            int dayNumber,
            LocalDate date,
            TripContext context,
            List<Attraction> allAttractions,
            Set<Long> usedAttractions,
            Hotel hotel,
            MoodType mood,
            boolean isArrivalDay,
            boolean isDepartureDay,
            int maxActivities
    ) {
        List<ScheduledActivity> activities = new ArrayList<>();
        
        // Determine available time slots
        List<TimeSlot> availableSlots = getAvailableTimeSlots(isArrivalDay, isDepartureDay);
        
        // Adjust max activities for special days
        int dayMaxActivities = isArrivalDay ? Math.min(2, maxActivities) 
                : isDepartureDay ? 1 
                : maxActivities;
        
        // Determine day theme based on available attractions
        String theme = determineDayTheme(dayNumber, context.getTotalDays(), mood, isArrivalDay, isDepartureDay);
        String dayLabel = generateDayLabel(dayNumber, isArrivalDay, isDepartureDay);

        int activitiesAdded = 0;

        // Select activities for each time slot
        for (TimeSlot slot : availableSlots) {
            if (activitiesAdded >= dayMaxActivities) break;
            
            Attraction selected = selectBestAttraction(
                    allAttractions, usedAttractions, slot, mood, hotel
            );
            
            if (selected != null) {
                usedAttractions.add(selected.getId());
                activities.add(buildScheduledActivity(selected, slot, hotel, mood));
                activitiesAdded++;
            } else {
                // FALLBACK: Generate hotel-centric or city-based activity
                ScheduledActivity fallback = generateFallbackActivity(
                        slot, hotel, context.getHotelAmenities(), mood, context.getCity()
                );
                activities.add(fallback);
                activitiesAdded++;
            }
        }

        // ENSURE we always have at least one activity
        if (activities.isEmpty()) {
            activities.add(generateFallbackActivity(
                    TimeSlot.AFTERNOON, hotel, context.getHotelAmenities(), mood, context.getCity()
            ));
        }

        // Generate hotel time suggestion
        HotelTime hotelTime = generateHotelTime(context.getHotelAmenities(), mood, isArrivalDay, isDepartureDay);

        // Build day summary
        DaySummary daySummary = buildDaySummary(activities, mood);

        return DayPlan.builder()
                .dayNumber(dayNumber)
                .date(date)
                .dayLabel(dayLabel)
                .theme(theme)
                .activities(activities)
                .hotelTime(hotelTime)
                .daySummary(daySummary)
                .build();
    }

    private List<TimeSlot> getAvailableTimeSlots(boolean isArrivalDay, boolean isDepartureDay) {
        if (isArrivalDay) {
            // Arrival day: afternoon and evening only
            return List.of(TimeSlot.AFTERNOON, TimeSlot.EVENING);
        } else if (isDepartureDay) {
            // Departure day: morning only
            return List.of(TimeSlot.MORNING);
        } else {
            // Full day: all slots
            return List.of(TimeSlot.MORNING, TimeSlot.AFTERNOON, TimeSlot.EVENING);
        }
    }

    private Attraction selectBestAttraction(
            List<Attraction> allAttractions,
            Set<Long> usedAttractions,
            TimeSlot slot,
            MoodType mood,
            Hotel hotel
    ) {
        // Map time slot to preferred TimeOfDay
        TimeOfDay preferredTime = switch (slot) {
            case MORNING -> TimeOfDay.MORNING;
            case AFTERNOON -> TimeOfDay.AFTERNOON;
            case EVENING -> TimeOfDay.EVENING;
        };

        // Find unused attractions matching time preference
        List<Attraction> candidates = allAttractions.stream()
                .filter(a -> !usedAttractions.contains(a.getId()))
                .filter(a -> a.getBestTime() == preferredTime || a.getBestTime() == TimeOfDay.ANY)
                .sorted((a, b) -> {
                    double scoreA = calculateAttractionScore(a, mood, slot);
                    double scoreB = calculateAttractionScore(b, mood, slot);
                    return Double.compare(scoreB, scoreA);
                })
                .toList();

        if (!candidates.isEmpty()) {
            return candidates.get(0);
        }

        // Fallback: any unused attraction
        return allAttractions.stream()
                .filter(a -> !usedAttractions.contains(a.getId()))
                .findFirst()
                .orElse(null);
    }

    private double calculateAttractionScore(Attraction attraction, MoodType mood, TimeSlot slot) {
        double score = 0;
        
        // Rating score (0-25 points)
        score += (attraction.getRating() / 5.0) * 25;
        
        // Mood alignment (0-35 points)
        if (mood != null && attraction.getMoodTags() != null 
                && attraction.getMoodTags().contains(mood.name())) {
            score += 35;
        } else if (mood != null && isCategoryMoodAligned(attraction.getCategory(), mood)) {
            score += 20;
        }
        
        // Time slot alignment (0-20 points)
        if (attraction.getBestTime() == TimeOfDay.valueOf(slot.name())) {
            score += 20;
        } else if (attraction.getBestTime() == TimeOfDay.ANY) {
            score += 15;
        }
        
        // Category bonus for specific slots (0-20 points)
        score += getSlotCategoryBonus(attraction.getCategory(), slot);
        
        return score;
    }

    private double getSlotCategoryBonus(AttractionCategory category, TimeSlot slot) {
        return switch (slot) {
            case MORNING -> switch (category) {
                case TEMPLE, NATURE, BEACH, ACTIVITY -> 20;
                case LANDMARK, CULTURAL -> 15;
                default -> 5;
            };
            case AFTERNOON -> switch (category) {
                case CULTURAL, SHOPPING, LANDMARK -> 20;
                case ACTIVITY, NATURE -> 15;
                default -> 5;
            };
            case EVENING -> switch (category) {
                case RESTAURANT, ENTERTAINMENT -> 20;
                case BEACH, SHOPPING -> 15;
                default -> 5;
            };
        };
    }

    // ==================== Fallback Activity Generation ====================

    /**
     * Generate a hotel-centric or city-based fallback activity.
     * Used when database attractions are exhausted or unavailable.
     * ENSURES every time slot has a meaningful activity.
     */
    private ScheduledActivity generateFallbackActivity(
            TimeSlot slot,
            Hotel hotel,
            List<String> amenities,
            MoodType mood,
            String city
    ) {
        FallbackActivity fallback = getFallbackForSlotAndMood(slot, mood, amenities, city, hotel.getName());

        return ScheduledActivity.builder()
                .attractionId(null) // No database attraction
                .name(fallback.name)
                .description(fallback.description)
                .category(fallback.category)
                .categoryDisplay(fallback.category.getDisplayName())
                .timeSlot(slot)
                .suggestedTime(fallback.suggestedTime)
                .durationMinutes(fallback.duration)
                .distanceFromHotel(fallback.distance)
                .distanceDisplay(fallback.distance == 0 ? "At hotel" : formatDistance(fallback.distance))
                .travelTimeMinutes(fallback.distance == 0 ? 0 : 10)
                .travelTimeDisplay(fallback.distance == 0 ? "No travel needed" : "10 min walk")
                .address(fallback.distance == 0 ? hotel.getAddress() : "Near " + hotel.getName())
                .rating(4.5)
                .priceLevel(fallback.priceLevel)
                .priceLevelDisplay(formatPriceLevel(fallback.priceLevel))
                .imageUrl(null)
                .openingHours(fallback.openingHours)
                .whyRecommended(fallback.whyRecommended)
                .moodTags(List.of(mood != null ? mood.name() : "RELAXATION"))
                .build();
    }

    private record FallbackActivity(
            String name,
            String description,
            AttractionCategory category,
            String suggestedTime,
            int duration,
            double distance,
            int priceLevel,
            String openingHours,
            String whyRecommended
    ) {}

    private FallbackActivity getFallbackForSlotAndMood(
            TimeSlot slot, 
            MoodType mood, 
            List<String> amenities,
            String city,
            String hotelName
    ) {
        boolean hasSpa = amenities.stream().anyMatch(a -> a.toLowerCase().contains("spa"));
        boolean hasPool = amenities.stream().anyMatch(a -> a.toLowerCase().contains("pool"));
        boolean hasRestaurant = amenities.stream().anyMatch(a -> a.toLowerCase().contains("restaurant") || a.toLowerCase().contains("dining"));

        MoodType effectiveMood = mood != null ? mood : MoodType.RELAXATION;

        return switch (slot) {
            case MORNING -> switch (effectiveMood) {
                case RELAXATION, ROMANTIC_GETAWAY -> hasSpa 
                    ? new FallbackActivity(
                        "Morning Wellness Session",
                        "Start your day with a rejuvenating spa treatment or yoga session at the hotel",
                        AttractionCategory.WELLNESS,
                        "08:00 - 10:00",
                        120, 0, 2, "08:00 - 20:00",
                        "Perfect way to start a relaxing day without leaving the comfort of your hotel"
                    )
                    : new FallbackActivity(
                        "Leisurely Breakfast & Garden Walk",
                        "Enjoy an extended breakfast followed by a peaceful walk around the hotel grounds",
                        AttractionCategory.RESTAURANT,
                        "08:30 - 10:30",
                        120, 0, 2, "07:00 - 10:30",
                        "A calm start to your day, designed for relaxation and quality time"
                    );
                case ADVENTURE, FAMILY_FUN -> hasPool
                    ? new FallbackActivity(
                        "Morning Pool & Breakfast",
                        "Early morning swim followed by a hearty breakfast to fuel your adventures",
                        AttractionCategory.ACTIVITY,
                        "07:00 - 09:30",
                        150, 0, 1, "06:00 - 22:00",
                        "Active start to energize you for the day ahead"
                    )
                    : new FallbackActivity(
                        "Neighborhood Discovery Walk",
                        "Explore the charming neighborhood around " + hotelName + " and discover local cafes",
                        AttractionCategory.ACTIVITY,
                        "07:30 - 09:30",
                        120, 0.5, 1, "Always open",
                        "Great way to get oriented and find hidden gems near your hotel"
                    );
                case BUSINESS -> new FallbackActivity(
                    "Business Breakfast & Planning",
                    "Productive breakfast at the hotel while planning your day ahead",
                    AttractionCategory.RESTAURANT,
                    "07:30 - 09:30",
                    120, 0, 2, "24 hours",
                    "Efficient start balancing productivity with a good meal"
                );
            };
            
            case AFTERNOON -> switch (effectiveMood) {
                case RELAXATION -> hasSpa
                    ? new FallbackActivity(
                        "Afternoon Spa Retreat",
                        "Indulge in a relaxing massage or body treatment at the hotel spa",
                        AttractionCategory.WELLNESS,
                        "14:00 - 16:00",
                        120, 0, 3, "10:00 - 20:00",
                        "Mid-day relaxation to rejuvenate your body and mind"
                    )
                    : new FallbackActivity(
                        "Rest & Local Discovery",
                        "Rest at the hotel followed by a gentle walk to explore nearby shops and cafes",
                        AttractionCategory.SHOPPING,
                        "14:00 - 17:00",
                        180, 1.0, 1, "10:00 - 21:00",
                        "Balanced afternoon with rest time and light exploration"
                    );
                case ROMANTIC_GETAWAY -> new FallbackActivity(
                    "Couple's Leisure Time",
                    "Private time at the hotel - enjoy the pool, lounge, or in-room relaxation together",
                    AttractionCategory.WELLNESS,
                    "14:00 - 17:00",
                    180, 0, 2, "All day",
                    "Intimate afternoon designed for couples to enjoy together"
                );
                case ADVENTURE -> new FallbackActivity(
                    "City Exploration Walk",
                    "Venture out and explore the streets, local markets, and scenic spots near " + city,
                    AttractionCategory.ACTIVITY,
                    "14:00 - 17:00",
                    180, 2.0, 1, "Always open",
                    "Self-guided adventure to discover the authentic local atmosphere"
                );
                case FAMILY_FUN -> hasPool
                    ? new FallbackActivity(
                        "Pool Time & Family Games",
                        "Enjoy the pool together followed by board games or activities at the hotel",
                        AttractionCategory.ENTERTAINMENT,
                        "14:00 - 17:00",
                        180, 0, 1, "All day",
                        "Perfect family bonding time without the hassle of traveling"
                    )
                    : new FallbackActivity(
                        "Family Leisure & Snack Time",
                        "Rest at the hotel and enjoy snacks together, play games or explore hotel grounds",
                        AttractionCategory.ENTERTAINMENT,
                        "14:00 - 17:00",
                        180, 0, 1, "All day",
                        "Quality family time with flexibility for kids' energy levels"
                    );
                case BUSINESS -> new FallbackActivity(
                    "Working Lunch & Networking",
                    "Business lunch at the hotel restaurant, review materials, or meet colleagues",
                    AttractionCategory.RESTAURANT,
                    "12:30 - 15:00",
                    150, 0, 2, "12:00 - 15:00",
                    "Productive afternoon that keeps you focused and well-fed"
                );
            };
            
            case EVENING -> switch (effectiveMood) {
                case ROMANTIC_GETAWAY -> hasRestaurant
                    ? new FallbackActivity(
                        "Romantic Dinner at Hotel",
                        "Candlelit dinner at the hotel's restaurant with fine dining and intimate ambiance",
                        AttractionCategory.RESTAURANT,
                        "19:00 - 21:30",
                        150, 0, 3, "18:00 - 23:00",
                        "End your day with a memorable romantic dining experience"
                    )
                    : new FallbackActivity(
                        "Sunset Stroll & Dinner",
                        "Evening stroll to catch the sunset followed by dinner at a nearby restaurant",
                        AttractionCategory.RESTAURANT,
                        "18:30 - 21:00",
                        150, 1.0, 2, "18:00 - 23:00",
                        "Romantic evening combining nature and cuisine"
                    );
                case RELAXATION -> new FallbackActivity(
                    "Quiet Evening & Light Dinner",
                    "Peaceful evening with a light dinner and perhaps some reading or stargazing",
                    AttractionCategory.RESTAURANT,
                    "18:30 - 21:00",
                    150, 0, 2, "18:00 - 22:00",
                    "Calm end to a restful day, designed for deep relaxation"
                );
                case ADVENTURE -> new FallbackActivity(
                    "Evening Food Exploration",
                    "Discover local street food or a popular restaurant near the hotel",
                    AttractionCategory.RESTAURANT,
                    "18:30 - 21:00",
                    150, 1.5, 1, "18:00 - 23:00",
                    "Culinary adventure to taste authentic local flavors"
                );
                case FAMILY_FUN -> new FallbackActivity(
                    "Family Dinner & Evening Fun",
                    "Enjoyable dinner together followed by games or a movie night at the hotel",
                    AttractionCategory.ENTERTAINMENT,
                    "18:00 - 21:00",
                    180, 0, 2, "18:00 - 22:00",
                    "Fun family evening with good food and entertainment"
                );
                case BUSINESS -> new FallbackActivity(
                    "Networking Dinner",
                    "Professional dinner at the hotel or a nearby upscale restaurant",
                    AttractionCategory.RESTAURANT,
                    "19:00 - 21:30",
                    150, 0, 3, "18:00 - 23:00",
                    "Combine business networking with an excellent dining experience"
                );
            };
        };
    }

    // ==================== Activity Building ====================

    private ScheduledActivity buildScheduledActivity(Attraction attraction, TimeSlot slot, Hotel hotel, MoodType mood) {
        double distanceKm = calculateDistance(
                hotel.getLatitude(), hotel.getLongitude(),
                attraction.getLatitude(), attraction.getLongitude()
        );
        
        int travelMinutes = calculateTravelTime(distanceKm);
        
        String suggestedTime = generateSuggestedTime(slot, attraction.getDurationMinutes());
        String whyRecommended = generateRecommendationReason(attraction, slot, mood);
        
        List<String> moodTags = parseMoodTags(attraction.getMoodTags());

        return ScheduledActivity.builder()
                .attractionId(attraction.getId())
                .name(attraction.getName())
                .description(attraction.getDescription())
                .category(attraction.getCategory())
                .categoryDisplay(attraction.getCategory().getDisplayName())
                .timeSlot(slot)
                .suggestedTime(suggestedTime)
                .durationMinutes(attraction.getDurationMinutes())
                .distanceFromHotel(Math.round(distanceKm * 10.0) / 10.0)
                .distanceDisplay(formatDistance(distanceKm))
                .travelTimeMinutes(travelMinutes)
                .travelTimeDisplay(formatTravelTime(travelMinutes))
                .address(attraction.getAddress())
                .rating(attraction.getRating())
                .priceLevel(attraction.getPriceLevel())
                .priceLevelDisplay(formatPriceLevel(attraction.getPriceLevel()))
                .imageUrl(attraction.getImageUrl())
                .openingHours(attraction.getOpeningHours())
                .whyRecommended(whyRecommended)
                .moodTags(moodTags)
                .build();
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 5.0; // Default 5km if coordinates missing
        }
        
        final int R = 6371; // Earth's radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int calculateTravelTime(double distanceKm) {
        // Simple calculation: distance / speed + 5 min buffer
        return (int) Math.ceil((distanceKm / AVERAGE_SPEED_KMH) * 60) + 5;
    }

    private String generateSuggestedTime(TimeSlot slot, int durationMinutes) {
        return switch (slot) {
            case MORNING -> String.format("09:00 - %02d:%02d", 
                    9 + durationMinutes / 60, durationMinutes % 60);
            case AFTERNOON -> String.format("14:00 - %02d:%02d", 
                    14 + durationMinutes / 60, durationMinutes % 60);
            case EVENING -> String.format("18:00 - %02d:%02d", 
                    18 + durationMinutes / 60, durationMinutes % 60);
        };
    }

    private String generateRecommendationReason(Attraction attraction, TimeSlot slot, MoodType mood) {
        StringBuilder reason = new StringBuilder();
        
        // Mood-based reason first
        if (mood != null) {
            reason.append(switch (mood) {
                case ROMANTIC_GETAWAY -> "Perfect for a romantic experience. ";
                case ADVENTURE -> "Great for adventure seekers. ";
                case RELAXATION -> "Ideal for a peaceful visit. ";
                case FAMILY_FUN -> "Fun for the whole family. ";
                case BUSINESS -> "Suitable for business travelers. ";
            });
        }
        
        // Rating-based reason
        if (attraction.getRating() >= 4.5) {
            reason.append("Highly rated by visitors. ");
        } else if (attraction.getRating() >= 4.0) {
            reason.append("Popular local attraction. ");
        }
        
        // Time-based reason
        if (attraction.getBestTime() == TimeOfDay.valueOf(slot.name())) {
            reason.append("Best experienced in the ").append(slot.getDisplayName().toLowerCase()).append(".");
        }
        
        return reason.toString().trim();
    }

    private List<String> parseMoodTags(String moodTagsJson) {
        if (moodTagsJson == null || moodTagsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(moodTagsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ==================== Formatting Helpers ====================

    private String formatDistance(double distanceKm) {
        if (distanceKm < 0.1) {
            return "At hotel";
        } else if (distanceKm < 1) {
            return String.format("%d m from hotel", (int) (distanceKm * 1000));
        }
        return String.format("%.1f km from hotel", distanceKm);
    }

    private String formatTravelTime(int minutes) {
        if (minutes < 60) {
            return minutes + " min drive";
        }
        return String.format("%d hr %d min drive", minutes / 60, minutes % 60);
    }

    private String formatPriceLevel(Integer level) {
        if (level == null) return "₹₹";
        return switch (level) {
            case 1 -> "₹";
            case 2 -> "₹₹";
            case 3 -> "₹₹₹";
            case 4 -> "₹₹₹₹";
            default -> "₹₹";
        };
    }

    private String generateDayLabel(int dayNumber, boolean isArrival, boolean isDeparture) {
        if (isArrival) return "Day " + dayNumber + " - Arrival";
        if (isDeparture) return "Day " + dayNumber + " - Departure";
        return "Day " + dayNumber + " - Exploration";
    }

    private String determineDayTheme(int dayNumber, int totalDays, MoodType mood, boolean isArrivalDay, boolean isDepartureDay) {
        if (isArrivalDay) return "Arrival & Settle In";
        if (isDepartureDay) return "Departure Day";
        
        if (mood != null) {
            List<String> themes = switch (mood) {
                case ROMANTIC_GETAWAY -> List.of("Romantic Discovery", "Intimate Experiences", "Love & Leisure", "Couple's Adventure");
                case ADVENTURE -> List.of("Adventure Awaits", "Thrilling Exploration", "Active Discovery", "New Horizons");
                case RELAXATION -> List.of("Peaceful Exploration", "Wellness & Calm", "Serene Discovery", "Mindful Day");
                case FAMILY_FUN -> List.of("Family Adventures", "Fun & Learning", "Memorable Moments", "Together Time");
                case BUSINESS -> List.of("Business & Leisure", "Cultural Insights", "Networking & Relaxation", "Productive Day");
            };
            return themes.get((dayNumber - 2) % themes.size());
        }
        
        return List.of("Cultural Discovery", "Local Experiences", "Hidden Gems", "City Exploration")
                .get((dayNumber - 2) % 4);
    }

    // ==================== Hotel Time & Summary ====================

    private HotelTime generateHotelTime(List<String> amenities, MoodType mood, boolean isArrivalDay, boolean isDepartureDay) {
        if (isDepartureDay) {
            return HotelTime.builder()
                    .label("Morning Checkout")
                    .suggestedTime("07:00 - 11:00")
                    .suggestion("Enjoy a leisurely breakfast before checkout. Take your time to pack and say goodbye to this beautiful place.")
                    .relevantAmenities(List.of("Restaurant", "Room Service", "Luggage Storage"))
                    .build();
        }
        
        boolean hasSpa = amenities.stream().anyMatch(a -> a.toLowerCase().contains("spa"));
        boolean hasPool = amenities.stream().anyMatch(a -> a.toLowerCase().contains("pool"));

        if (isArrivalDay) {
            String suggestion = hasSpa 
                    ? "Check-in and freshen up. Consider a relaxing spa treatment to unwind from your journey."
                    : hasPool 
                            ? "Check-in and refresh with a dip in the pool before heading out."
                            : "Check-in, settle into your room, and take a moment to relax before exploring.";
            
            return HotelTime.builder()
                    .label("Arrival & Rest")
                    .suggestedTime("14:00 - 16:00")
                    .suggestion(suggestion)
                    .relevantAmenities(amenities.isEmpty() ? List.of("Room amenities") : amenities.subList(0, Math.min(3, amenities.size())))
                    .build();
        }

        // Regular day - mood-based hotel time
        MoodType effectiveMood = mood != null ? mood : MoodType.RELAXATION;
        
        return switch (effectiveMood) {
            case RELAXATION, ROMANTIC_GETAWAY -> HotelTime.builder()
                    .label("Afternoon Relaxation")
                    .suggestedTime("13:00 - 15:00")
                    .suggestion(hasSpa 
                            ? "Indulge in a spa session to rejuvenate mind and body." 
                            : hasPool 
                                    ? "Take a refreshing dip in the pool and unwind."
                                    : "Rest in your room, order room service, and recharge.")
                    .relevantAmenities(hasSpa ? List.of("Spa", "Wellness Center") : hasPool ? List.of("Pool", "Lounge") : List.of("Room Service"))
                    .build();
            case ADVENTURE, FAMILY_FUN -> HotelTime.builder()
                    .label("Midday Break")
                    .suggestedTime("12:30 - 14:00")
                    .suggestion("Quick lunch and rest to recharge for more adventures. Stay hydrated!")
                    .relevantAmenities(List.of("Restaurant", "Room Service"))
                    .build();
            case BUSINESS -> HotelTime.builder()
                    .label("Working Break")
                    .suggestedTime("12:00 - 14:00")
                    .suggestion("Catch up on emails, attend virtual meetings, or prepare for your next session.")
                    .relevantAmenities(List.of("Business Center", "Wi-Fi", "Meeting Rooms"))
                    .build();
        };
    }

    private DaySummary buildDaySummary(List<ScheduledActivity> activities, MoodType mood) {
        int totalDuration = activities.stream()
                .mapToInt(a -> a.getDurationMinutes() + a.getTravelTimeMinutes())
                .sum();
        
        double totalDistance = activities.stream()
                .mapToDouble(ScheduledActivity::getDistanceFromHotel)
                .sum();
        
        // Intensity based on mood and activities
        MoodType effectiveMood = mood != null ? mood : MoodType.RELAXATION;
        String intensity = switch (effectiveMood) {
            case RELAXATION, ROMANTIC_GETAWAY -> totalDuration < 300 ? "Very Relaxed" : "Relaxed";
            case BUSINESS -> "Balanced";
            case FAMILY_FUN -> totalDuration < 360 ? "Moderate" : "Active";
            case ADVENTURE -> totalDuration < 300 ? "Moderate" : "Active";
        };

        return DaySummary.builder()
                .totalActivities(activities.size())
                .totalDurationMinutes(totalDuration)
                .totalDistanceKm(Math.round(totalDistance * 10.0) / 10.0)
                .intensityLevel(intensity)
                .build();
    }

    private ItinerarySummary buildItinerarySummary(List<DayPlan> dayPlans, MoodType mood) {
        List<ScheduledActivity> allActivities = dayPlans.stream()
                .flatMap(d -> d.getActivities().stream())
                .toList();
        
        Map<String, Integer> categoryBreakdown = allActivities.stream()
                .collect(Collectors.groupingBy(
                        ScheduledActivity::getCategoryDisplay,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        double avgRating = allActivities.stream()
                .mapToDouble(ScheduledActivity::getRating)
                .average()
                .orElse(4.5);
        
        double totalDistance = allActivities.stream()
                .mapToDouble(ScheduledActivity::getDistanceFromHotel)
                .sum();

        // Calculate mood match
        long moodMatchCount = mood != null ? allActivities.stream()
                .filter(a -> a.getMoodTags() != null && a.getMoodTags().contains(mood.name()))
                .count() : allActivities.size();
        
        String moodMatch = mood == null ? "Personalized" 
                : moodMatchCount >= allActivities.size() * 0.6 ? "Excellent"
                : moodMatchCount >= allActivities.size() * 0.3 ? "Good"
                : "Customized";

        return ItinerarySummary.builder()
                .totalActivities(allActivities.size())
                .uniqueCategories(categoryBreakdown.size())
                .totalDistanceKm(Math.round(totalDistance * 10.0) / 10.0)
                .categoryBreakdown(categoryBreakdown)
                .overallMoodMatch(moodMatch)
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .build();
    }

    private List<String> generateTravelTips(TripContext context, Hotel hotel, MoodType mood) {
        List<String> tips = new ArrayList<>();
        
        // Mood-specific tips first
        if (mood != null) {
            tips.add(switch (mood) {
                case ROMANTIC_GETAWAY -> "Take your time at each location - this trip is about quality moments, not quantity";
                case ADVENTURE -> "Carry water, sunscreen, and comfortable shoes for your active explorations";
                case RELAXATION -> "Don't rush - feel free to skip activities if you'd rather rest at the hotel";
                case FAMILY_FUN -> "Build in extra time for bathroom breaks and snack stops with kids";
                case BUSINESS -> "Keep your schedule flexible for unexpected meetings or calls";
            });
        }
        
        // City-specific tips
        String city = context.getCity().toLowerCase();
        if (city.contains("chennai")) {
            tips.add("Try the local filter coffee and authentic South Indian breakfast");
            tips.add("Marina Beach is best visited during early morning or sunset");
        } else if (city.contains("ooty") || city.contains("nilgiri")) {
            tips.add("Carry light woolens - evenings can be chilly even in summer");
            tips.add("Book Nilgiri Mountain Railway tickets in advance for a memorable experience");
        } else if (city.contains("pondicherry") || city.contains("puducherry")) {
            tips.add("Rent a bicycle to explore the charming French Quarter");
            tips.add("Visit Auroville early morning to avoid crowds");
        } else if (city.contains("madurai")) {
            tips.add("Visit Meenakshi Temple during evening for the spectacular ceremony");
            tips.add("Try the famous Madurai jigarthanda for a refreshing treat");
        } else if (city.contains("kodaikanal")) {
            tips.add("Early morning is the best time for boating and nature walks");
            tips.add("Pack layers - the weather can change quickly in the hills");
        } else if (city.contains("coimbatore")) {
            tips.add("Visit Marudhamalai Temple for stunning views of the city");
            tips.add("Try the local South Indian cuisine - Coimbatore is known for its food");
        } else if (city.contains("trichy") || city.contains("tiruchirappalli")) {
            tips.add("Rock Fort Temple climb is best done early morning to avoid heat");
            tips.add("Don't miss the Sri Ranganathaswamy Temple - one of the largest in India");
        }
        
        // General tips
        tips.add("Keep some buffer time between activities for unexpected discoveries");
        tips.add("Download offline maps for navigation in areas with poor connectivity");
        tips.add("Stay hydrated and take breaks - there's no rush to see everything");
        
        return tips.stream().limit(5).toList();
    }

    // ==================== Public Utility Methods ====================

    /**
     * Get attractions available in a city.
     */
    @Transactional(readOnly = true)
    public CityAttractionsResponse getCityAttractions(String city) {
        List<Attraction> attractions = attractionRepository.findByCityIgnoreCaseAndIsActiveTrue(city);
        
        Map<String, Integer> categoryBreakdown = attractions.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCategory().getDisplayName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        List<AttractionSummary> topAttractions = attractions.stream()
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                .limit(10)
                .map(a -> AttractionSummary.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .description(a.getDescription())
                        .category(a.getCategory().getDisplayName())
                        .categoryIcon(getCategoryIcon(a.getCategory()))
                        .rating(a.getRating())
                        .priceLevel(a.getPriceLevel())
                        .imageUrl(a.getImageUrl())
                        .moodTags(parseMoodTags(a.getMoodTags()))
                        .build())
                .toList();

        return CityAttractionsResponse.builder()
                .city(city)
                .totalAttractions(attractions.size())
                .categoryBreakdown(categoryBreakdown)
                .topAttractions(topAttractions)
                .build();
    }

    private String getCategoryIcon(AttractionCategory category) {
        return switch (category) {
            case LANDMARK -> "fa-landmark";
            case RESTAURANT -> "fa-utensils";
            case ACTIVITY -> "fa-hiking";
            case NATURE -> "fa-tree";
            case CULTURAL -> "fa-museum";
            case SHOPPING -> "fa-shopping-bag";
            case ENTERTAINMENT -> "fa-theater-masks";
            case WELLNESS -> "fa-spa";
            case BEACH -> "fa-umbrella-beach";
            case TEMPLE -> "fa-place-of-worship";
        };
    }
}
