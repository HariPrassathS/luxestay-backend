package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.search.SearchSuggestionDto;
import com.hotel.domain.dto.search.PriceCalendarDto;
import com.hotel.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for smart search features.
 * Provides autocomplete, suggestions, and price calendar endpoints.
 */
@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Smart search and discovery endpoints")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Get search suggestions for autocomplete.
     * Returns hotels, cities, and destinations matching the query.
     */
    @GetMapping("/suggestions")
    @Operation(summary = "Get search suggestions", 
               description = "Returns autocomplete suggestions for hotels, cities, and destinations")
    public ResponseEntity<ApiResponse<SearchSuggestionDto>> getSuggestions(
            @RequestParam @Parameter(description = "Search query (min 2 chars)") String q,
            @RequestParam(defaultValue = "5") @Parameter(description = "Max results per category") int limit) {
        
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(ApiResponse.success(SearchSuggestionDto.empty()));
        }
        
        SearchSuggestionDto suggestions = searchService.getSuggestions(q.trim(), limit);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    /**
     * Get popular search terms (for empty state suggestions).
     */
    @GetMapping("/popular")
    @Operation(summary = "Get popular searches", 
               description = "Returns popular destinations and hotel categories")
    public ResponseEntity<ApiResponse<SearchSuggestionDto>> getPopularSearches() {
        SearchSuggestionDto popular = searchService.getPopularSearches();
        return ResponseEntity.ok(ApiResponse.success(popular));
    }

    /**
     * Get price calendar for a specific hotel.
     * Returns daily prices for the specified date range.
     */
    @GetMapping("/price-calendar/{hotelId}")
    @Operation(summary = "Get price calendar", 
               description = "Returns daily minimum room prices for a hotel within date range")
    public ResponseEntity<ApiResponse<PriceCalendarDto>> getPriceCalendar(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                @Parameter(description = "Start date (yyyy-MM-dd)") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                @Parameter(description = "End date (yyyy-MM-dd)") LocalDate endDate) {
        
        // Limit to 90 days max
        if (startDate.plusDays(90).isBefore(endDate)) {
            endDate = startDate.plusDays(90);
        }
        
        PriceCalendarDto calendar = searchService.getPriceCalendar(hotelId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(calendar));
    }

    /**
     * Get recent searches for authenticated user.
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent searches", 
               description = "Returns user's recent search history")
    public ResponseEntity<ApiResponse<List<String>>> getRecentSearches() {
        // For now, return empty - can be enhanced with user-specific history
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
