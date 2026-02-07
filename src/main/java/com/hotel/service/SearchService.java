package com.hotel.service;

import com.hotel.domain.dto.search.PriceCalendarDto;
import com.hotel.domain.dto.search.RoomComparisonDto;
import com.hotel.domain.dto.search.SearchSuggestionDto;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for smart search features.
 * Handles autocomplete, price calendar, and room comparison.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SearchService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    // Popular destinations data (could be in DB)
    private static final List<SearchSuggestionDto.DestinationSuggestion> POPULAR_DESTINATIONS = List.of(
        SearchSuggestionDto.DestinationSuggestion.builder()
            .name("Chennai").description("Gateway to South India").type("city").build(),
        SearchSuggestionDto.DestinationSuggestion.builder()
            .name("Ooty").description("Queen of Hill Stations").type("mountain").build(),
        SearchSuggestionDto.DestinationSuggestion.builder()
            .name("Madurai").description("Temple City of India").type("heritage").build(),
        SearchSuggestionDto.DestinationSuggestion.builder()
            .name("Mumbai").description("City of Dreams").type("city").build(),
        SearchSuggestionDto.DestinationSuggestion.builder()
            .name("Paris").description("City of Love").type("city").build()
    );

    /**
     * Get search suggestions for autocomplete.
     */
    public SearchSuggestionDto getSuggestions(String query, int limit) {
        log.debug("Getting suggestions for query: {}", query);
        
        // Search hotels
        List<Hotel> matchingHotels = hotelRepository.searchHotels(query);
        List<SearchSuggestionDto.HotelSuggestion> hotelSuggestions = matchingHotels.stream()
                .limit(limit)
                .map(this::toHotelSuggestion)
                .collect(Collectors.toList());
        
        // Search cities
        List<String> allCities = hotelRepository.findAllCities();
        List<String> allCountries = hotelRepository.findAllCountries();
        
        String lowerQuery = query.toLowerCase();
        
        List<SearchSuggestionDto.CitySuggestion> citySuggestions = new ArrayList<>();
        
        // Match cities
        allCities.stream()
                .filter(city -> city.toLowerCase().contains(lowerQuery))
                .limit(limit)
                .forEach(city -> {
                    long count = hotelRepository.findByCityIgnoreCaseAndIsActiveTrue(city).size();
                    citySuggestions.add(SearchSuggestionDto.CitySuggestion.builder()
                            .name(city)
                            .country(getCountryForCity(city))
                            .hotelCount((int) count)
                            .type("city")
                            .build());
                });
        
        // Match countries
        allCountries.stream()
                .filter(country -> country.toLowerCase().contains(lowerQuery))
                .limit(limit - citySuggestions.size())
                .forEach(country -> {
                    long count = hotelRepository.findByCountryIgnoreCaseAndIsActiveTrue(country).size();
                    citySuggestions.add(SearchSuggestionDto.CitySuggestion.builder()
                            .name(country)
                            .country(country)
                            .hotelCount((int) count)
                            .type("country")
                            .build());
                });
        
        // Match popular destinations
        List<SearchSuggestionDto.DestinationSuggestion> destinationSuggestions = POPULAR_DESTINATIONS.stream()
                .filter(d -> d.getName().toLowerCase().contains(lowerQuery) ||
                           d.getDescription().toLowerCase().contains(lowerQuery))
                .limit(3)
                .map(d -> {
                    int count = hotelRepository.searchHotels(d.getName()).size();
                    return SearchSuggestionDto.DestinationSuggestion.builder()
                            .name(d.getName())
                            .description(d.getDescription())
                            .type(d.getType())
                            .hotelCount(count)
                            .build();
                })
                .collect(Collectors.toList());
        
        return SearchSuggestionDto.builder()
                .hotels(hotelSuggestions)
                .cities(citySuggestions)
                .destinations(destinationSuggestions)
                .build();
    }

    /**
     * Get popular searches for empty state.
     */
    public SearchSuggestionDto getPopularSearches() {
        // Get top cities by hotel count
        List<String> cities = hotelRepository.findAllCities();
        List<SearchSuggestionDto.CitySuggestion> citySuggestions = cities.stream()
                .limit(5)
                .map(city -> {
                    long count = hotelRepository.findByCityIgnoreCaseAndIsActiveTrue(city).size();
                    return SearchSuggestionDto.CitySuggestion.builder()
                            .name(city)
                            .country(getCountryForCity(city))
                            .hotelCount((int) count)
                            .type("city")
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getHotelCount(), a.getHotelCount()))
                .collect(Collectors.toList());
        
        // Get featured hotels
        List<Hotel> featured = hotelRepository.findFeaturedHotels(PageRequest.of(0, 5));
        List<SearchSuggestionDto.HotelSuggestion> hotelSuggestions = featured.stream()
                .map(this::toHotelSuggestion)
                .collect(Collectors.toList());
        
        return SearchSuggestionDto.builder()
                .hotels(hotelSuggestions)
                .cities(citySuggestions)
                .destinations(POPULAR_DESTINATIONS)
                .build();
    }

    /**
     * Get price calendar for a hotel.
     */
    public PriceCalendarDto getPriceCalendar(Long hotelId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting price calendar for hotel {} from {} to {}", hotelId, startDate, endDate);
        
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found: " + hotelId));
        
        List<Room> rooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotelId);
        
        if (rooms.isEmpty()) {
            return PriceCalendarDto.builder()
                    .hotelId(hotelId)
                    .hotelName(hotel.getName())
                    .startDate(startDate)
                    .endDate(endDate)
                    .prices(List.of())
                    .build();
        }
        
        // Get base min price from rooms
        BigDecimal baseMinPrice = rooms.stream()
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        @SuppressWarnings("unused") // Reserved for future price range display
        BigDecimal baseMaxPrice = rooms.stream()
                .map(Room::getPricePerNight)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        // Generate daily prices
        List<PriceCalendarDto.DayPrice> prices = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal lowestPrice = null;
        BigDecimal highestPrice = null;
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            
            // Calculate price with weekend surcharge
            BigDecimal dayPrice = baseMinPrice;
            if (isWeekend) {
                dayPrice = dayPrice.multiply(BigDecimal.valueOf(1.15))
                        .setScale(0, RoundingMode.CEILING);
            }
            
            // Check room availability for this date
            final LocalDate checkDate = current;
            int availableCount = (int) rooms.stream()
                    .filter(room -> !bookingRepository.hasOverlappingBooking(
                            room.getId(), checkDate, checkDate.plusDays(1)))
                    .count();
            
            // Track min/max
            if (lowestPrice == null || dayPrice.compareTo(lowestPrice) < 0) {
                lowestPrice = dayPrice;
            }
            if (highestPrice == null || dayPrice.compareTo(highestPrice) > 0) {
                highestPrice = dayPrice;
            }
            totalPrice = totalPrice.add(dayPrice);
            
            prices.add(PriceCalendarDto.DayPrice.builder()
                    .date(current)
                    .price(dayPrice)
                    .available(availableCount > 0)
                    .availableRooms(availableCount)
                    .dayOfWeek(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .isWeekend(isWeekend)
                    .build());
            
            current = current.plusDays(1);
        }
        
        // Calculate average and thresholds
        BigDecimal avgPrice = prices.isEmpty() ? BigDecimal.ZERO :
                totalPrice.divide(BigDecimal.valueOf(prices.size()), 0, RoundingMode.HALF_UP);
        
        // Set price tiers (low = < 85% of avg, high = > 115% of avg)
        BigDecimal lowThreshold = avgPrice.multiply(BigDecimal.valueOf(0.85))
                .setScale(0, RoundingMode.DOWN);
        BigDecimal highThreshold = avgPrice.multiply(BigDecimal.valueOf(1.15))
                .setScale(0, RoundingMode.UP);
        
        // Apply tier to each day
        for (PriceCalendarDto.DayPrice dp : prices) {
            if (dp.getPrice().compareTo(lowThreshold) <= 0) {
                dp.setPriceTier("low");
            } else if (dp.getPrice().compareTo(highThreshold) >= 0) {
                dp.setPriceTier("high");
            } else {
                dp.setPriceTier("medium");
            }
        }
        
        return PriceCalendarDto.builder()
                .hotelId(hotelId)
                .hotelName(hotel.getName())
                .startDate(startDate)
                .endDate(endDate)
                .prices(prices)
                .lowestPrice(lowestPrice)
                .highestPrice(highestPrice)
                .averagePrice(avgPrice)
                .lowPriceThreshold(lowThreshold)
                .highPriceThreshold(highThreshold)
                .build();
    }

    /**
     * Get room comparison data.
     */
    public RoomComparisonDto compareRooms(Long hotelId, List<Long> roomIds, 
                                          LocalDate checkIn, LocalDate checkOut) {
        log.debug("Comparing rooms {} for hotel {} from {} to {}", 
                roomIds, hotelId, checkIn, checkOut);
        
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found: " + hotelId));
        
        int nights = (int) (checkOut.toEpochDay() - checkIn.toEpochDay());
        
        List<RoomComparisonDto.RoomCompareItem> compareItems = new ArrayList<>();
        
        for (Long roomId : roomIds) {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null || !room.getHotel().getId().equals(hotelId)) continue;
            
            // Check availability
            boolean available = !bookingRepository.hasOverlappingBooking(roomId, checkIn, checkOut);
            
            // Calculate total price
            BigDecimal totalPrice = room.getPricePerNight()
                    .multiply(BigDecimal.valueOf(nights));
            
            // Parse amenities (stored as JSON array string)
            List<String> amenities = parseAmenities(room.getAmenities());
            
            // Generate highlights
            List<String> highlights = generateHighlights(room);
            
            // Calculate scores
            int valueScore = calculateValueScore(room, nights);
            int spaceScore = calculateSpaceScore(room);
            int amenityScore = calculateAmenityScore(amenities);
            
            // Convert sizeSqm to Integer if present
            Integer sizeSqMeters = room.getSizeSqm() != null ? 
                    room.getSizeSqm().intValue() : null;
            
            compareItems.add(RoomComparisonDto.RoomCompareItem.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .type(room.getRoomType())
                    .description(room.getDescription())
                    .pricePerNight(room.getPricePerNight())
                    .totalPrice(totalPrice)
                    .maxGuests(room.getCapacity() != null ? room.getCapacity() : 2)
                    .sizeSqMeters(sizeSqMeters)
                    .bedType(room.getBedType())
                    .viewType(null) // Room entity doesn't have viewType
                    .imageUrl(room.getImageUrl())
                    .available(available)
                    .amenities(amenities)
                    .highlights(highlights)
                    .valueScore(valueScore)
                    .spaceScore(spaceScore)
                    .amenityScore(amenityScore)
                    .build());
        }
        
        return RoomComparisonDto.builder()
                .hotelId(hotelId)
                .hotelName(hotel.getName())
                .checkIn(checkIn)
                .checkOut(checkOut)
                .nights(nights)
                .rooms(compareItems)
                .build();
    }

    // ========== HELPER METHODS ==========
    
    private SearchSuggestionDto.HotelSuggestion toHotelSuggestion(Hotel hotel) {
        BigDecimal minPrice = hotel.getRooms().stream()
                .filter(Room::getIsAvailable)
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(null);
        
        return SearchSuggestionDto.HotelSuggestion.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .starRating(hotel.getStarRating())
                .minPrice(minPrice)
                .imageUrl(hotel.getHeroImageUrl())
                .build();
    }
    
    private String getCountryForCity(String city) {
        return hotelRepository.findByCityIgnoreCaseAndIsActiveTrue(city).stream()
                .findFirst()
                .map(Hotel::getCountry)
                .orElse(null);
    }
    
    private List<String> parseAmenities(String amenitiesJson) {
        if (amenitiesJson == null || amenitiesJson.isBlank()) {
            return List.of();
        }
        
        // Simple JSON array parsing
        try {
            amenitiesJson = amenitiesJson.trim();
            if (amenitiesJson.startsWith("[") && amenitiesJson.endsWith("]")) {
                amenitiesJson = amenitiesJson.substring(1, amenitiesJson.length() - 1);
                return Arrays.stream(amenitiesJson.split(","))
                        .map(s -> s.trim().replace("\"", ""))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to parse amenities: {}", amenitiesJson);
        }
        return List.of();
    }
    
    private List<String> generateHighlights(Room room) {
        List<String> highlights = new ArrayList<>();
        
        if (room.getSizeSqm() != null && room.getSizeSqm().intValue() >= 40) {
            highlights.add("Spacious room");
        }
        // Room entity doesn't have viewType, skip those checks
        if (room.getCapacity() != null && room.getCapacity() >= 4) {
            highlights.add("Family friendly");
        }
        if (room.getRoomType() != null && room.getRoomType().name().contains("SUITE")) {
            highlights.add("Suite upgrade");
        }
        
        return highlights;
    }
    
    private int calculateValueScore(Room room, int nights) {
        // Simple value scoring based on price per guest
        int capacity = room.getCapacity() != null ? room.getCapacity() : 2;
        BigDecimal pricePerGuest = room.getPricePerNight()
                .divide(BigDecimal.valueOf(capacity), 0, RoundingMode.HALF_UP);
        
        // Scale: < 1000 = 100, > 5000 = 50
        if (pricePerGuest.compareTo(BigDecimal.valueOf(1000)) < 0) return 100;
        if (pricePerGuest.compareTo(BigDecimal.valueOf(5000)) > 0) return 50;
        
        // Linear scale between
        double ratio = 1 - (pricePerGuest.doubleValue() - 1000) / 4000;
        return (int) (50 + ratio * 50);
    }
    
    private int calculateSpaceScore(Room room) {
        if (room.getSizeSqm() == null) return 50;
        int size = room.getSizeSqm().intValue();
        
        // Scale: < 20 = 30, > 60 = 100
        if (size < 20) return 30;
        if (size > 60) return 100;
        
        // Linear scale
        return (int) (30 + (size - 20.0) / 40 * 70);
    }
    
    private int calculateAmenityScore(List<String> amenities) {
        // Base score + additional for premium amenities
        int score = Math.min(amenities.size() * 10, 60);
        
        Set<String> premium = Set.of("jacuzzi", "balcony", "minibar", "bathtub", 
                                     "room service", "spa", "kitchen");
        
        for (String amenity : amenities) {
            if (premium.stream().anyMatch(p -> amenity.toLowerCase().contains(p))) {
                score += 8;
            }
        }
        
        return Math.min(score, 100);
    }
}
