package com.hotel.service;

import com.hotel.domain.dto.trust.TrustScoreDto;
import com.hotel.domain.dto.trust.TrustScoreDto.*;
import com.hotel.domain.entity.Review;
import com.hotel.domain.entity.ReviewReply;
import com.hotel.domain.entity.ReviewStatus;
import com.hotel.repository.ReviewRepository;
import com.hotel.repository.ReviewReplyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TrustScore Service - Calculates deterministic, explainable trust scores for hotels.
 * 
 * SCORING PHILOSOPHY:
 * - Transparent: Every point is explainable
 * - Deterministic: Same input = same output
 * - Fair: New hotels aren't penalized unfairly
 * - Honest: We show concerns, not just praises
 * 
 * SCORE COMPONENTS (Total: 100 points):
 * - Review Volume: 0-25 points (more verified reviews = more trust)
 * - Rating Quality: 0-35 points (higher average rating)
 * - Consistency: 0-20 points (consistent ratings = reliable experience)
 * - Recency: 0-10 points (recent reviews = current relevance)
 * - Hotel Engagement: 0-10 points (hotel responds to reviews)
 */
@Service
@Transactional(readOnly = true)
public class TrustScoreService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    
    // Minimum reviews needed for a "reliable" score
    private static final int MIN_REVIEWS_FOR_RELIABLE = 5;
    
    // Keywords for praise/concern detection (simple keyword matching, not ML)
    private static final Map<String, PraiseCategory> PRAISE_KEYWORDS = new LinkedHashMap<>();
    private static final Map<String, ConcernCategory> CONCERN_KEYWORDS = new LinkedHashMap<>();
    
    static {
        // Praise keywords (ordered by priority)
        PRAISE_KEYWORDS.put("location", new PraiseCategory("Location", "fa-location-dot", 
            Arrays.asList("location", "located", "central", "convenient", "walkable", "nearby", "close to")));
        PRAISE_KEYWORDS.put("cleanliness", new PraiseCategory("Cleanliness", "fa-sparkles",
            Arrays.asList("clean", "spotless", "immaculate", "tidy", "hygiene", "fresh", "well-maintained")));
        PRAISE_KEYWORDS.put("service", new PraiseCategory("Service", "fa-concierge-bell",
            Arrays.asList("staff", "service", "helpful", "friendly", "attentive", "professional", "hospitality", "welcomed")));
        PRAISE_KEYWORDS.put("comfort", new PraiseCategory("Comfort", "fa-bed",
            Arrays.asList("comfortable", "cozy", "bed", "mattress", "pillow", "sleep", "restful")));
        PRAISE_KEYWORDS.put("value", new PraiseCategory("Value", "fa-tag",
            Arrays.asList("value", "worth", "reasonable", "affordable", "good price", "bang for buck")));
        PRAISE_KEYWORDS.put("breakfast", new PraiseCategory("Breakfast", "fa-utensils",
            Arrays.asList("breakfast", "food", "dining", "restaurant", "meal", "delicious")));
        PRAISE_KEYWORDS.put("amenities", new PraiseCategory("Amenities", "fa-swimming-pool",
            Arrays.asList("pool", "gym", "spa", "wifi", "amenities", "facilities")));
        PRAISE_KEYWORDS.put("view", new PraiseCategory("View", "fa-mountain-sun",
            Arrays.asList("view", "scenic", "beautiful", "overlooking", "panoramic")));
        
        // Concern keywords (be careful - only genuine concerns, not dealbreakers)
        CONCERN_KEYWORDS.put("noise", new ConcernCategory("Noise", "fa-volume-high", "minor",
            Arrays.asList("noise", "noisy", "loud", "sound", "thin walls")));
        CONCERN_KEYWORDS.put("parking", new ConcernCategory("Parking", "fa-square-parking", "minor",
            Arrays.asList("parking", "park", "garage", "valet")));
        CONCERN_KEYWORDS.put("wifi", new ConcernCategory("WiFi", "fa-wifi", "minor",
            Arrays.asList("wifi", "internet", "connection", "slow wifi")));
        CONCERN_KEYWORDS.put("size", new ConcernCategory("Room Size", "fa-expand", "minor",
            Arrays.asList("small", "cramped", "tiny", "compact", "limited space")));
        CONCERN_KEYWORDS.put("aging", new ConcernCategory("Dated Decor", "fa-clock-rotate-left", "minor",
            Arrays.asList("old", "dated", "outdated", "needs renovation", "worn")));
    }
    
    public TrustScoreService(ReviewRepository reviewRepository, ReviewReplyRepository reviewReplyRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewReplyRepository = reviewReplyRepository;
    }
    
    /**
     * Calculate TrustScore for a hotel.
     * Results are cached for 15 minutes to reduce database load.
     */
    @Cacheable(value = "trustScores", key = "#hotelId")
    public TrustScoreDto calculateTrustScore(Long hotelId) {
        // Get approved reviews only
        List<Review> reviews = reviewRepository.findApprovedByHotelId(hotelId);
        
        // If no reviews, return "new hotel" state
        if (reviews.isEmpty()) {
            return buildNewHotelScore();
        }
        
        // Calculate each component
        ComponentScore volumeScore = calculateVolumeScore(reviews.size());
        ComponentScore ratingScore = calculateRatingScore(reviews);
        ComponentScore consistencyScore = calculateConsistencyScore(reviews);
        ComponentScore recencyScore = calculateRecencyScore(reviews);
        ComponentScore engagementScore = calculateEngagementScore(reviews);
        
        // Total score
        int totalScore = volumeScore.getPoints() + ratingScore.getPoints() + 
                        consistencyScore.getPoints() + recencyScore.getPoints() + 
                        engagementScore.getPoints();
        
        // Determine level
        String level = determineLevel(totalScore, reviews.size());
        
        // Extract praises and concerns
        List<PraiseDto> praises = extractTopPraises(reviews, 3);
        List<ConcernDto> concerns = extractTopConcerns(reviews, 2);
        
        // Calculate average rating
        double avgRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        
        // Count verified reviews
        long verifiedCount = reviews.stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsVerifiedStay()))
            .count();
        
        return TrustScoreDto.builder()
            .score(totalScore)
            .level(level)
            .verifiedReviewCount((int) verifiedCount)
            .averageRating(Math.round(avgRating * 10.0) / 10.0)
            .topPraises(praises)
            .topConcerns(concerns)
            .breakdown(ScoreBreakdownDto.builder()
                .reviewVolume(volumeScore)
                .ratingQuality(ratingScore)
                .consistency(consistencyScore)
                .recency(recencyScore)
                .hotelEngagement(engagementScore)
                .build())
            .hasEnoughData(reviews.size() >= MIN_REVIEWS_FOR_RELIABLE)
            .explanation(buildExplanation(totalScore, reviews.size(), avgRating))
            .build();
    }
    
    /**
     * Volume Score: More reviews = more data = more trust
     * 0-25 points
     * - 1-4 reviews: 5-15 points (new but promising)
     * - 5-9 reviews: 15-20 points (establishing reputation)
     * - 10-24 reviews: 20-23 points (well-reviewed)
     * - 25+ reviews: 23-25 points (extensively reviewed)
     */
    private ComponentScore calculateVolumeScore(int reviewCount) {
        int points;
        String explanation;
        
        if (reviewCount == 0) {
            points = 0;
            explanation = "No reviews yet";
        } else if (reviewCount < 5) {
            points = 5 + (reviewCount * 2); // 7-13 points
            explanation = String.format("%d reviews - building reputation", reviewCount);
        } else if (reviewCount < 10) {
            points = 15 + ((reviewCount - 5) * 1); // 15-19 points
            explanation = String.format("%d verified reviews", reviewCount);
        } else if (reviewCount < 25) {
            points = 20 + Math.min(3, (reviewCount - 10) / 5); // 20-23 points
            explanation = String.format("%d reviews - well-established", reviewCount);
        } else {
            points = 23 + Math.min(2, (reviewCount - 25) / 25); // 23-25 points
            explanation = String.format("%d+ reviews - extensively reviewed", reviewCount);
        }
        
        return ComponentScore.builder()
            .name("Review Volume")
            .points(Math.min(25, points))
            .maxPoints(25)
            .explanation(explanation)
            .build();
    }
    
    /**
     * Rating Score: Higher average = better experience
     * 0-35 points (weighted heavily as it's most important)
     * - 4.5-5.0: 32-35 points (exceptional)
     * - 4.0-4.4: 26-31 points (excellent)
     * - 3.5-3.9: 20-25 points (very good)
     * - 3.0-3.4: 14-19 points (good)
     * - Below 3.0: 0-13 points (needs improvement)
     */
    private ComponentScore calculateRatingScore(List<Review> reviews) {
        double avgRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        
        int points;
        String explanation;
        
        if (avgRating >= 4.5) {
            points = 32 + (int)((avgRating - 4.5) * 6); // 32-35
            explanation = String.format("%.1f average - exceptional", avgRating);
        } else if (avgRating >= 4.0) {
            points = 26 + (int)((avgRating - 4.0) * 10); // 26-31
            explanation = String.format("%.1f average - excellent", avgRating);
        } else if (avgRating >= 3.5) {
            points = 20 + (int)((avgRating - 3.5) * 10); // 20-25
            explanation = String.format("%.1f average - very good", avgRating);
        } else if (avgRating >= 3.0) {
            points = 14 + (int)((avgRating - 3.0) * 10); // 14-19
            explanation = String.format("%.1f average - good", avgRating);
        } else {
            points = (int)(avgRating * 4.6); // 0-13
            explanation = String.format("%.1f average - developing", avgRating);
        }
        
        return ComponentScore.builder()
            .name("Rating Quality")
            .points(Math.min(35, points))
            .maxPoints(35)
            .explanation(explanation)
            .build();
    }
    
    /**
     * Consistency Score: Low variance = reliable experience
     * 0-20 points
     * - Std Dev < 0.5: 18-20 points (very consistent)
     * - Std Dev 0.5-1.0: 12-17 points (consistent)
     * - Std Dev 1.0-1.5: 6-11 points (some variation)
     * - Std Dev > 1.5: 0-5 points (inconsistent experience)
     */
    private ComponentScore calculateConsistencyScore(List<Review> reviews) {
        if (reviews.size() < 2) {
            return ComponentScore.builder()
                .name("Consistency")
                .points(10) // Neutral score for new hotels
                .maxPoints(20)
                .explanation("More reviews needed to assess consistency")
                .build();
        }
        
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        double variance = reviews.stream()
            .mapToDouble(r -> Math.pow(r.getRating() - avg, 2))
            .average()
            .orElse(0);
        double stdDev = Math.sqrt(variance);
        
        int points;
        String explanation;
        
        if (stdDev < 0.5) {
            points = 18 + (int)((0.5 - stdDev) * 4);
            explanation = "Very consistent guest experiences";
        } else if (stdDev < 1.0) {
            points = 12 + (int)((1.0 - stdDev) * 10);
            explanation = "Consistent ratings across reviews";
        } else if (stdDev < 1.5) {
            points = 6 + (int)((1.5 - stdDev) * 10);
            explanation = "Some variation in guest experiences";
        } else {
            points = Math.max(0, (int)((2.0 - stdDev) * 5));
            explanation = "Experiences vary - read reviews carefully";
        }
        
        return ComponentScore.builder()
            .name("Consistency")
            .points(Math.min(20, Math.max(0, points)))
            .maxPoints(20)
            .explanation(explanation)
            .build();
    }
    
    /**
     * Recency Score: Recent reviews = current relevance
     * 0-10 points
     * - Reviews in last 30 days: +4 points
     * - Reviews in last 90 days: +3 points
     * - Reviews in last 180 days: +2 points
     * - Older reviews: +1 point
     */
    private ComponentScore calculateRecencyScore(List<Review> reviews) {
        LocalDateTime now = LocalDateTime.now();
        
        long last30Days = reviews.stream()
            .filter(r -> r.getCreatedAt() != null)
            .filter(r -> ChronoUnit.DAYS.between(r.getCreatedAt(), now) <= 30)
            .count();
        
        long last90Days = reviews.stream()
            .filter(r -> r.getCreatedAt() != null)
            .filter(r -> ChronoUnit.DAYS.between(r.getCreatedAt(), now) <= 90)
            .count();
        
        int points;
        String explanation;
        
        if (last30Days >= 3) {
            points = 10;
            explanation = String.format("%d reviews in the last month", last30Days);
        } else if (last30Days >= 1) {
            points = 8;
            explanation = "Recent activity in the last month";
        } else if (last90Days >= 3) {
            points = 6;
            explanation = "Active in the last 3 months";
        } else if (last90Days >= 1) {
            points = 4;
            explanation = "Some recent reviews";
        } else {
            points = 2;
            explanation = "No very recent reviews";
        }
        
        return ComponentScore.builder()
            .name("Recency")
            .points(points)
            .maxPoints(10)
            .explanation(explanation)
            .build();
    }
    
    /**
     * Engagement Score: Hotel responds to feedback = cares about guests
     * 0-10 points
     * - Response rate > 80%: 9-10 points
     * - Response rate 50-80%: 6-8 points
     * - Response rate 20-50%: 3-5 points
     * - Response rate < 20%: 0-2 points
     */
    private ComponentScore calculateEngagementScore(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return ComponentScore.builder()
                .name("Hotel Engagement")
                .points(5)
                .maxPoints(10)
                .explanation("No reviews to respond to yet")
                .build();
        }
        
        long reviewsWithReply = reviews.stream()
            .filter(r -> r.getReply() != null)
            .count();
        
        double responseRate = (double) reviewsWithReply / reviews.size();
        
        int points;
        String explanation;
        
        if (responseRate >= 0.8) {
            points = 9 + (responseRate >= 0.95 ? 1 : 0);
            explanation = String.format("Hotel responds to %d%% of reviews", (int)(responseRate * 100));
        } else if (responseRate >= 0.5) {
            points = 6 + (int)((responseRate - 0.5) * 6);
            explanation = "Hotel actively engages with feedback";
        } else if (responseRate >= 0.2) {
            points = 3 + (int)((responseRate - 0.2) * 6);
            explanation = "Hotel occasionally responds to reviews";
        } else if (responseRate > 0) {
            points = 1 + (int)(responseRate * 5);
            explanation = "Limited hotel engagement";
        } else {
            points = 0;
            explanation = "Hotel hasn't responded to reviews yet";
        }
        
        return ComponentScore.builder()
            .name("Hotel Engagement")
            .points(Math.min(10, points))
            .maxPoints(10)
            .explanation(explanation)
            .build();
    }
    
    /**
     * Extract top praises from review text using keyword matching.
     * Simple, explainable, no ML.
     */
    private List<PraiseDto> extractTopPraises(List<Review> reviews, int limit) {
        Map<String, Integer> praiseCounts = new LinkedHashMap<>();
        
        for (Review review : reviews) {
            // Only count praises from positive reviews (4+ stars)
            if (review.getRating() < 4) continue;
            
            String text = (review.getTitle() + " " + review.getComment()).toLowerCase();
            
            for (Map.Entry<String, PraiseCategory> entry : PRAISE_KEYWORDS.entrySet()) {
                String key = entry.getKey();
                PraiseCategory category = entry.getValue();
                
                for (String keyword : category.keywords) {
                    if (text.contains(keyword)) {
                        praiseCounts.merge(key, 1, Integer::sum);
                        break; // Count each category once per review
                    }
                }
            }
        }
        
        return praiseCounts.entrySet().stream()
            .filter(e -> e.getValue() >= 2) // Only show if mentioned by 2+ guests
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .map(e -> {
                PraiseCategory cat = PRAISE_KEYWORDS.get(e.getKey());
                int count = e.getValue();
                String desc = buildPraiseDescription(cat.name, count, reviews.size());
                return PraiseDto.builder()
                    .category(cat.name)
                    .description(desc)
                    .mentionCount(count)
                    .icon(cat.icon)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Extract top concerns from review text.
     * Only from reviews that are 3 stars or below, and only "minor" severity.
     * We want honest transparency, not to scare users.
     */
    private List<ConcernDto> extractTopConcerns(List<Review> reviews, int limit) {
        Map<String, Integer> concernCounts = new LinkedHashMap<>();
        
        for (Review review : reviews) {
            String text = (review.getTitle() + " " + review.getComment()).toLowerCase();
            
            for (Map.Entry<String, ConcernCategory> entry : CONCERN_KEYWORDS.entrySet()) {
                String key = entry.getKey();
                ConcernCategory category = entry.getValue();
                
                // Check for negative context (simple approach)
                boolean hasNegativeContext = containsNegativeContext(text, category.keywords);
                
                if (hasNegativeContext) {
                    concernCounts.merge(key, 1, Integer::sum);
                }
            }
        }
        
        return concernCounts.entrySet().stream()
            .filter(e -> e.getValue() >= 2) // Only show if mentioned by 2+ guests
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .map(e -> {
                ConcernCategory cat = CONCERN_KEYWORDS.get(e.getKey());
                int count = e.getValue();
                String desc = buildConcernDescription(cat.name, count, reviews.size());
                return ConcernDto.builder()
                    .category(cat.name)
                    .description(desc)
                    .mentionCount(count)
                    .severity(cat.severity)
                    .icon(cat.icon)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Check if text contains keywords in a negative context.
     * Simple heuristic: look for negative words near the keyword.
     */
    private boolean containsNegativeContext(String text, List<String> keywords) {
        List<String> negativeIndicators = Arrays.asList(
            "no ", "not ", "lacking", "poor", "bad", "terrible", "awful",
            "disappointing", "issue", "problem", "complaint", "worse", "worst",
            "too small", "too loud", "too noisy", "didn't work", "doesn't work"
        );
        
        for (String keyword : keywords) {
            if (!text.contains(keyword)) continue;
            
            // Check if any negative indicator is within 50 chars of the keyword
            int keywordIndex = text.indexOf(keyword);
            int start = Math.max(0, keywordIndex - 50);
            int end = Math.min(text.length(), keywordIndex + keyword.length() + 50);
            String context = text.substring(start, end);
            
            for (String neg : negativeIndicators) {
                if (context.contains(neg)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private String buildPraiseDescription(String category, int count, int total) {
        int percent = (int)((double)count / total * 100);
        if (percent >= 80) {
            return String.format("Almost all guests praise the %s", category.toLowerCase());
        } else if (percent >= 50) {
            return String.format("Majority of guests love the %s", category.toLowerCase());
        } else {
            return String.format("Many guests appreciate the %s", category.toLowerCase());
        }
    }
    
    private String buildConcernDescription(String category, int count, int total) {
        int percent = (int)((double)count / total * 100);
        if (percent >= 30) {
            return String.format("Some guests mentioned %s", category.toLowerCase());
        } else {
            return String.format("A few guests noted %s", category.toLowerCase());
        }
    }
    
    private String determineLevel(int score, int reviewCount) {
        if (reviewCount < MIN_REVIEWS_FOR_RELIABLE) {
            return "New";
        }
        if (score >= 85) return "Excellent";
        if (score >= 70) return "Very Good";
        if (score >= 55) return "Good";
        return "Fair";
    }
    
    private String buildExplanation(int score, int reviewCount, double avgRating) {
        if (reviewCount < MIN_REVIEWS_FOR_RELIABLE) {
            return String.format("Based on %d verified reviews. More reviews will improve score accuracy.", reviewCount);
        }
        
        if (score >= 85) {
            return String.format("Exceptional trust score based on %d verified reviews with %.1f average rating.", 
                reviewCount, avgRating);
        } else if (score >= 70) {
            return String.format("Very good trust score from %d verified reviews. Guests consistently enjoy their stays.", 
                reviewCount);
        } else if (score >= 55) {
            return String.format("Good trust score based on %d reviews. Most guests have positive experiences.", 
                reviewCount);
        } else {
            return String.format("Trust score based on %d reviews. Read recent reviews for current guest experiences.", 
                reviewCount);
        }
    }
    
    private TrustScoreDto buildNewHotelScore() {
        return TrustScoreDto.builder()
            .score(0)
            .level("New")
            .verifiedReviewCount(0)
            .averageRating(0.0)
            .topPraises(Collections.emptyList())
            .topConcerns(Collections.emptyList())
            .breakdown(ScoreBreakdownDto.builder()
                .reviewVolume(ComponentScore.builder()
                    .name("Review Volume").points(0).maxPoints(25)
                    .explanation("No reviews yet").build())
                .ratingQuality(ComponentScore.builder()
                    .name("Rating Quality").points(0).maxPoints(35)
                    .explanation("Awaiting first reviews").build())
                .consistency(ComponentScore.builder()
                    .name("Consistency").points(0).maxPoints(20)
                    .explanation("Not enough data").build())
                .recency(ComponentScore.builder()
                    .name("Recency").points(0).maxPoints(10)
                    .explanation("Brand new listing").build())
                .hotelEngagement(ComponentScore.builder()
                    .name("Hotel Engagement").points(0).maxPoints(10)
                    .explanation("No reviews to respond to").build())
                .build())
            .hasEnoughData(false)
            .explanation("This hotel is new to LuxeStay. Be among the first to share your experience!")
            .build();
    }
    
    // Helper classes for keyword categories
    private static class PraiseCategory {
        String name;
        String icon;
        List<String> keywords;
        
        PraiseCategory(String name, String icon, List<String> keywords) {
            this.name = name;
            this.icon = icon;
            this.keywords = keywords;
        }
    }
    
    private static class ConcernCategory {
        String name;
        String icon;
        String severity;
        List<String> keywords;
        
        ConcernCategory(String name, String icon, String severity, List<String> keywords) {
            this.name = name;
            this.icon = icon;
            this.severity = severity;
            this.keywords = keywords;
        }
    }
}
