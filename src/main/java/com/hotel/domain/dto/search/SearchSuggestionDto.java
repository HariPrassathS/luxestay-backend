package com.hotel.domain.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for search autocomplete suggestions.
 * Contains categorized results for hotels, cities, and popular destinations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchSuggestionDto {

    @Builder.Default
    private List<HotelSuggestion> hotels = new ArrayList<>();
    
    @Builder.Default
    private List<CitySuggestion> cities = new ArrayList<>();
    
    @Builder.Default
    private List<DestinationSuggestion> destinations = new ArrayList<>();
    
    @Builder.Default
    private List<String> recentSearches = new ArrayList<>();

    /**
     * Create empty suggestions.
     */
    public static SearchSuggestionDto empty() {
        return SearchSuggestionDto.builder().build();
    }

    /**
     * Hotel suggestion item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HotelSuggestion {
        private Long id;
        private String name;
        private String city;
        private String country;
        private Integer starRating;
        private BigDecimal minPrice;
        private String imageUrl;
    }

    /**
     * City suggestion item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CitySuggestion {
        private String name;
        private String country;
        private int hotelCount;
        private String type; // "city" or "country"
    }

    /**
     * Popular destination suggestion.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DestinationSuggestion {
        private String name;
        private String description;
        private String imageUrl;
        private String type; // "beach", "mountain", "city", "heritage"
        private int hotelCount;
    }
}
