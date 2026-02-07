package com.hotel.controller;

import com.hotel.domain.dto.hotel.MoodFinderDto.*;
import com.hotel.service.MoodFinderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller for Mood-Based Hotel Finder.
 * 
 * Enables users to discover hotels based on their travel mood.
 * All logic is server-side for security and scalability.
 */
@RestController
@RequestMapping("/api/hotels/mood-finder")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mood Finder", description = "Discover hotels based on your travel mood")
public class MoodFinderController {

    private final MoodFinderService moodFinderService;

    /**
     * Get all available moods with hotel counts.
     * Returns mood options for the user to choose from.
     */
    @GetMapping("/moods")
    @Operation(
        summary = "Get available moods",
        description = "Returns all travel moods with matching hotel counts for each"
    )
    public ResponseEntity<MoodListResponse> getAvailableMoods() {
        log.info("Fetching available moods with hotel counts");
        MoodListResponse response = moodFinderService.getAllMoodsWithCounts();
        return ResponseEntity.ok(response);
    }

    /**
     * Get the criteria used for a specific mood.
     * Provides transparency on how hotels are matched.
     */
    @GetMapping("/moods/{mood}/criteria")
    @Operation(
        summary = "Get mood criteria",
        description = "Returns the criteria used to match hotels for a specific mood"
    )
    public ResponseEntity<MoodCriteria> getMoodCriteria(
            @PathVariable @Parameter(description = "The travel mood") MoodType mood) {
        log.info("Fetching criteria for mood: {}", mood);
        MoodCriteria criteria = moodFinderService.getMoodCriteria(mood);
        return ResponseEntity.ok(criteria);
    }

    /**
     * Search hotels by mood with optional filters.
     * Main endpoint for mood-based recommendations.
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search hotels by mood",
        description = "Find hotels matching a specific travel mood with intelligent scoring"
    )
    public ResponseEntity<MoodSearchResponse> searchByMood(
            @RequestParam @Parameter(description = "The travel mood to match") MoodType mood,
            @RequestParam(required = false) @Parameter(description = "Filter by location") String location,
            @RequestParam(required = false) @Parameter(description = "Minimum star rating") Integer minStars,
            @RequestParam(required = false) @Parameter(description = "Maximum price per night") BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "10") @Parameter(description = "Max results") Integer limit) {
        
        log.info("Searching hotels for mood: {} | location: {} | minStars: {} | maxPrice: {} | limit: {}", 
            mood, location, minStars, maxPrice, limit);
        
        MoodSearchRequest request = MoodSearchRequest.builder()
            .mood(mood)
            .location(location)
            .minStars(minStars)
            .maxPrice(maxPrice)
            .limit(limit)
            .build();
        
        MoodSearchResponse response = moodFinderService.findHotelsByMood(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST endpoint for more complex search requests.
     */
    @PostMapping("/search")
    @Operation(
        summary = "Search hotels by mood (POST)",
        description = "Find hotels matching a specific travel mood with full request body"
    )
    public ResponseEntity<MoodSearchResponse> searchByMoodPost(
            @RequestBody MoodSearchRequest request) {
        
        log.info("POST search for mood: {}", request.getMood());
        MoodSearchResponse response = moodFinderService.findHotelsByMood(request);
        return ResponseEntity.ok(response);
    }
}
