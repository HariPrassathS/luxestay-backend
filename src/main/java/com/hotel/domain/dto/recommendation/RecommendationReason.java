package com.hotel.domain.dto.recommendation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum of all possible recommendation reasons.
 * Each reason has human-readable display text for UI.
 */
@Getter
@RequiredArgsConstructor
public enum RecommendationReason {
    
    // Personalization-based
    CITY_AFFINITY("Based on your past stays"),
    PRICE_MATCH("Within your budget"),
    STAR_PREFERENCE("Matches your preferences"),
    
    // Quality-based
    TOP_RATED("Highly rated"),
    POPULAR("Popular choice"),
    FEATURED("Editor's pick"),
    
    // Similarity-based
    SAME_CITY("Same destination"),
    SIMILAR_RATING("Similar category"),
    SIMILAR_PRICE("Similar price range"),
    SIMILAR_AMENITIES("Similar amenities"),
    
    // Context-based
    DESTINATION_MATCH("Great for this destination"),
    SEASONAL("Perfect for this season"),
    NEW_LISTING("Recently added"),
    
    // Social proof
    FRIENDS_STAYED("Friends have stayed here"),
    TRENDING("Trending now");
    
    private final String displayText;
}
