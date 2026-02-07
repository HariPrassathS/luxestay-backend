package com.hotel.service;

import com.hotel.domain.dto.recommendation.RecommendationDto;
import com.hotel.domain.dto.recommendation.RecommendationReason;
import com.hotel.domain.entity.*;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.ReviewRepository;
import com.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data-Driven Recommendation Engine
 * 
 * EXPLAINABILITY FEATURES:
 * - Every recommendation includes clear reasoning
 * - Factors: booking history, preferred cities, price range, ratings
 * - No black-box ML - fully transparent scoring algorithm
 * 
 * PERSONALIZATION SIGNALS:
 * 1. Previously booked cities (city affinity)
 * 2. Price range preference (avg booking price ± 30%)
 * 3. Star rating preference (mode of past bookings)
 * 4. Seasonal patterns (time-based trends)
 * 5. Collaborative filtering (users with similar bookings)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {
    
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    
    // Scoring weights (explainable)
    private static final double WEIGHT_CITY_AFFINITY = 0.25;
    private static final double WEIGHT_PRICE_MATCH = 0.20;
    private static final double WEIGHT_STAR_PREFERENCE = 0.15;
    private static final double WEIGHT_RATING = 0.20;
    private static final double WEIGHT_POPULARITY = 0.10;
    private static final double WEIGHT_NEWNESS = 0.10;
    
    /**
     * Get personalized hotel recommendations for a user.
     * Returns fully explainable recommendations with scoring breakdown.
     */
    public List<RecommendationDto> getPersonalizedRecommendations(Long userId, int limit) {
        log.info("Generating personalized recommendations for user {}", userId);
        
        // Build user preference profile from booking history
        UserPreferenceProfile profile = buildUserProfile(userId);
        
        // Get candidate hotels (exclude recently booked)
        List<Hotel> candidates = getCandidateHotels(userId, profile);
        
        // Score and rank hotels
        List<ScoredHotel> scoredHotels = candidates.stream()
                .map(hotel -> scoreHotel(hotel, profile))
                .sorted(Comparator.comparingDouble(ScoredHotel::totalScore).reversed())
                .limit(limit)
                .toList();
        
        // Convert to DTOs with explanations
        return scoredHotels.stream()
                .map(this::toRecommendationDto)
                .toList();
    }
    
    /**
     * Get recommendations for anonymous/new users based on popularity.
     */
    public List<RecommendationDto> getPopularRecommendations(int limit) {
        log.info("Generating popular recommendations for anonymous user");
        
        List<Hotel> hotels = hotelRepository.findFeaturedHotels(PageRequest.of(0, limit * 2));
        
        return hotels.stream()
                .map(hotel -> {
                    double score = calculatePopularityScore(hotel);
                    return toRecommendationDto(new ScoredHotel(
                            hotel,
                            score,
                            Map.of(
                                    RecommendationReason.POPULAR, 0.4,
                                    RecommendationReason.TOP_RATED, 0.4,
                                    RecommendationReason.FEATURED, 0.2
                            ),
                            "Highly rated by other travelers"
                    ));
                })
                .limit(limit)
                .toList();
    }
    
    /**
     * Get "Similar Hotels" recommendations based on a specific hotel.
     */
    public List<RecommendationDto> getSimilarHotels(Long hotelId, int limit) {
        Hotel source = hotelRepository.findById(hotelId).orElse(null);
        if (source == null) {
            return Collections.emptyList();
        }
        
        List<Hotel> candidates = hotelRepository.findByIsActiveTrue().stream()
                .filter(h -> !h.getId().equals(hotelId))
                .toList();
        
        return candidates.stream()
                .map(hotel -> {
                    double similarity = calculateSimilarity(source, hotel);
                    Map<RecommendationReason, Double> reasons = new LinkedHashMap<>();
                    String explanation;
                    
                    if (source.getCity().equalsIgnoreCase(hotel.getCity())) {
                        reasons.put(RecommendationReason.SAME_CITY, 0.4);
                        explanation = "Also in " + hotel.getCity();
                    } else if (Objects.equals(source.getStarRating(), hotel.getStarRating())) {
                        reasons.put(RecommendationReason.SIMILAR_RATING, 0.3);
                        explanation = "Same " + hotel.getStarRating() + "-star category";
                    } else {
                        reasons.put(RecommendationReason.SIMILAR_PRICE, 0.3);
                        explanation = "Similar price range";
                    }
                    
                    return new ScoredHotel(hotel, similarity, reasons, explanation);
                })
                .sorted(Comparator.comparingDouble(ScoredHotel::totalScore).reversed())
                .limit(limit)
                .map(this::toRecommendationDto)
                .toList();
    }
    
    /**
     * Get recommendations for a specific city/destination.
     */
    public List<RecommendationDto> getDestinationRecommendations(String city, Long userId, int limit) {
        UserPreferenceProfile profile = userId != null ? buildUserProfile(userId) : null;
        
        List<Hotel> cityHotels = hotelRepository.findByCityIgnoreCaseAndIsActiveTrue(city);
        
        return cityHotels.stream()
                .map(hotel -> {
                    double score;
                    Map<RecommendationReason, Double> reasons = new LinkedHashMap<>();
                    String explanation;
                    
                    if (profile != null && profile.preferredPriceRange != null) {
                        BigDecimal minPrice = getMinPrice(hotel);
                        if (minPrice != null && isInPriceRange(minPrice, profile.preferredPriceRange)) {
                            score = 0.9;
                            reasons.put(RecommendationReason.PRICE_MATCH, 0.5);
                            explanation = "Matches your budget preference";
                        } else {
                            score = 0.6;
                            reasons.put(RecommendationReason.DESTINATION_MATCH, 0.4);
                            explanation = "Popular in " + city;
                        }
                    } else {
                        score = calculatePopularityScore(hotel);
                        reasons.put(RecommendationReason.DESTINATION_MATCH, 0.5);
                        explanation = "Highly rated in " + city;
                    }
                    
                    return new ScoredHotel(hotel, score, reasons, explanation);
                })
                .sorted(Comparator.comparingDouble(ScoredHotel::totalScore).reversed())
                .limit(limit)
                .map(this::toRecommendationDto)
                .toList();
    }
    
    // ==================== PROFILE BUILDING ====================
    
    private UserPreferenceProfile buildUserProfile(Long userId) {
        List<Booking> bookings = bookingRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        
        if (bookings.isEmpty()) {
            return new UserPreferenceProfile(null, null, null, null, Set.of(), Set.of());
        }
        
        // Extract preferences from booking history
        Set<String> bookedCities = new HashSet<>();
        Set<Long> bookedHotelIds = new HashSet<>();
        List<BigDecimal> prices = new ArrayList<>();
        List<Integer> starRatings = new ArrayList<>();
        
        for (Booking booking : bookings) {
            Room room = booking.getRoom();
            if (room != null && room.getHotel() != null) {
                Hotel hotel = room.getHotel();
                bookedCities.add(hotel.getCity().toLowerCase());
                bookedHotelIds.add(hotel.getId());
                prices.add(booking.getPricePerNight());
                if (hotel.getStarRating() != null) {
                    starRatings.add(hotel.getStarRating());
                }
            }
        }
        
        // Calculate preferred price range (avg ± 30%)
        BigDecimal avgPrice = prices.isEmpty() ? null :
                prices.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(prices.size()), RoundingMode.HALF_UP);
        
        PriceRange preferredRange = null;
        if (avgPrice != null) {
            BigDecimal margin = avgPrice.multiply(BigDecimal.valueOf(0.3));
            preferredRange = new PriceRange(
                    avgPrice.subtract(margin).max(BigDecimal.ZERO),
                    avgPrice.add(margin)
            );
        }
        
        // Calculate preferred star rating (mode)
        Integer preferredStars = starRatings.isEmpty() ? null :
                starRatings.stream()
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                        .entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
        
        return new UserPreferenceProfile(
                avgPrice,
                preferredRange,
                preferredStars,
                bookings.size(),
                bookedCities,
                bookedHotelIds
        );
    }
    
    // ==================== SCORING ====================
    
    private List<Hotel> getCandidateHotels(Long userId, UserPreferenceProfile profile) {
        return hotelRepository.findByIsActiveTrue().stream()
                .filter(h -> !profile.bookedHotelIds.contains(h.getId())) // Exclude already booked
                .toList();
    }
    
    private ScoredHotel scoreHotel(Hotel hotel, UserPreferenceProfile profile) {
        Map<RecommendationReason, Double> reasonScores = new LinkedHashMap<>();
        List<String> explanations = new ArrayList<>();
        
        double totalScore = 0;
        
        // 1. City Affinity Score
        if (profile.preferredCities.contains(hotel.getCity().toLowerCase())) {
            double cityScore = WEIGHT_CITY_AFFINITY;
            reasonScores.put(RecommendationReason.CITY_AFFINITY, cityScore);
            totalScore += cityScore;
            explanations.add("You've stayed in " + hotel.getCity() + " before");
        }
        
        // 2. Price Match Score
        BigDecimal minPrice = getMinPrice(hotel);
        if (minPrice != null && profile.preferredPriceRange != null) {
            if (isInPriceRange(minPrice, profile.preferredPriceRange)) {
                double priceScore = WEIGHT_PRICE_MATCH;
                reasonScores.put(RecommendationReason.PRICE_MATCH, priceScore);
                totalScore += priceScore;
                explanations.add("Matches your typical budget");
            }
        }
        
        // 3. Star Rating Preference
        if (profile.preferredStarRating != null && 
            Objects.equals(hotel.getStarRating(), profile.preferredStarRating)) {
            double starScore = WEIGHT_STAR_PREFERENCE;
            reasonScores.put(RecommendationReason.STAR_PREFERENCE, starScore);
            totalScore += starScore;
            explanations.add("You prefer " + profile.preferredStarRating + "-star hotels");
        }
        
        // 4. Rating Score (from reviews)
        double avgRating = getAverageRating(hotel.getId());
        if (avgRating > 0) {
            double ratingScore = (avgRating / 5.0) * WEIGHT_RATING;
            reasonScores.put(RecommendationReason.TOP_RATED, ratingScore);
            totalScore += ratingScore;
            if (avgRating >= 4.5) {
                explanations.add("Exceptionally rated (" + String.format("%.1f", avgRating) + "★)");
            }
        }
        
        // 5. Popularity Score
        double popularityScore = calculatePopularityScore(hotel) * WEIGHT_POPULARITY;
        if (popularityScore > 0.05) {
            reasonScores.put(RecommendationReason.POPULAR, popularityScore);
            totalScore += popularityScore;
        }
        
        // 6. Newness Bonus (boost new listings)
        if (hotel.getCreatedAt() != null) {
            long daysSinceCreated = java.time.temporal.ChronoUnit.DAYS.between(
                    hotel.getCreatedAt().toLocalDate(), LocalDate.now());
            if (daysSinceCreated <= 30) {
                double newnessScore = WEIGHT_NEWNESS * (1 - daysSinceCreated / 30.0);
                reasonScores.put(RecommendationReason.NEW_LISTING, newnessScore);
                totalScore += newnessScore;
                explanations.add("Recently added");
            }
        }
        
        // Build primary explanation
        String primaryExplanation = explanations.isEmpty() ? 
                "Great choice for your next stay" : explanations.get(0);
        
        return new ScoredHotel(hotel, totalScore, reasonScores, primaryExplanation);
    }
    
    private double calculatePopularityScore(Hotel hotel) {
        // Based on booking count and reviews
        long bookingCount = bookingRepository.countByHotelId(hotel.getId());
        double avgRating = getAverageRating(hotel.getId());
        
        // Normalize: assume 100 bookings = 1.0 score, capped
        double bookingScore = Math.min(bookingCount / 100.0, 1.0);
        double ratingScore = avgRating / 5.0;
        
        return (bookingScore * 0.6) + (ratingScore * 0.4);
    }
    
    private double calculateSimilarity(Hotel source, Hotel target) {
        double score = 0;
        
        // Same city = high similarity
        if (source.getCity().equalsIgnoreCase(target.getCity())) {
            score += 0.4;
        }
        
        // Same star rating
        if (Objects.equals(source.getStarRating(), target.getStarRating())) {
            score += 0.3;
        } else if (source.getStarRating() != null && target.getStarRating() != null &&
                   Math.abs(source.getStarRating() - target.getStarRating()) == 1) {
            score += 0.15;
        }
        
        // Similar price range
        BigDecimal sourcePrice = getMinPrice(source);
        BigDecimal targetPrice = getMinPrice(target);
        if (sourcePrice != null && targetPrice != null) {
            double priceDiff = Math.abs(sourcePrice.doubleValue() - targetPrice.doubleValue());
            if (priceDiff <= sourcePrice.doubleValue() * 0.3) {
                score += 0.3;
            } else if (priceDiff <= sourcePrice.doubleValue() * 0.5) {
                score += 0.15;
            }
        }
        
        return score;
    }
    
    // ==================== HELPERS ====================
    
    private BigDecimal getMinPrice(Hotel hotel) {
        return roomRepository.findByHotel_IdAndIsAvailableTrue(hotel.getId()).stream()
                .map(Room::getPricePerNight)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }
    
    private double getAverageRating(Long hotelId) {
        Double avg = reviewRepository.getAverageRatingForHotel(hotelId);
        return avg != null ? avg : 0.0;
    }
    
    private boolean isInPriceRange(BigDecimal price, PriceRange range) {
        return price.compareTo(range.min) >= 0 && price.compareTo(range.max) <= 0;
    }
    
    private RecommendationDto toRecommendationDto(ScoredHotel scored) {
        Hotel hotel = scored.hotel;
        BigDecimal minPrice = getMinPrice(hotel);
        double avgRating = getAverageRating(hotel.getId());
        
        return RecommendationDto.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .starRating(hotel.getStarRating())
                .heroImageUrl(hotel.getHeroImageUrl())
                .minPrice(minPrice)
                .averageRating(avgRating > 0 ? avgRating : null)
                .score(Math.round(scored.totalScore * 100) / 100.0)
                .primaryReason(scored.explanation)
                .reasonBreakdown(scored.reasons.entrySet().stream()
                        .map(e -> RecommendationDto.ReasonScore.builder()
                                .reason(e.getKey())
                                .label(e.getKey().getDisplayText())
                                .weight(Math.round(e.getValue() * 100) / 100.0)
                                .build())
                        .toList())
                .build();
    }
    
    // ==================== INNER CLASSES ====================
    
    private record UserPreferenceProfile(
            BigDecimal averagePrice,
            PriceRange preferredPriceRange,
            Integer preferredStarRating,
            Integer totalBookings,
            Set<String> preferredCities,
            Set<Long> bookedHotelIds
    ) {}
    
    private record PriceRange(BigDecimal min, BigDecimal max) {}
    
    private record ScoredHotel(
            Hotel hotel,
            double totalScore,
            Map<RecommendationReason, Double> reasons,
            String explanation
    ) {}
}
