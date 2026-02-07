package com.hotel.controller;

import com.hotel.domain.dto.hotel.MoodFinderDto.MoodType;
import com.hotel.domain.dto.itinerary.ItineraryDto.*;
import com.hotel.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Trip Intelligence & Smart Itinerary Generator.
 * 
 * Provides endpoints to generate intelligent, data-driven trip itineraries
 * based on booking details, hotel location, and available attractions.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Trip Intelligence", description = "Smart Itinerary Generator APIs")
public class ItineraryController {

    private final ItineraryService itineraryService;

    /**
     * Generate a complete trip itinerary for a booking.
     * 
     * Uses the booking's hotel location, dates, and optionally the user's travel mood
     * to create a realistic day-by-day itinerary with nearby attractions.
     * Requires authentication and verifies the user owns the booking.
     */
    @GetMapping("/bookings/{bookingId}/itinerary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Generate trip itinerary",
            description = "Generates a smart, data-driven trip itinerary based on booking details and optional travel mood"
    )
    @ApiResponse(responseCode = "200", description = "Itinerary generated successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @ApiResponse(responseCode = "403", description = "Not authorized to access this booking")
    public ResponseEntity<ItineraryResponse> generateItinerary(
            @PathVariable 
            @Parameter(description = "Booking ID") 
            Long bookingId,
            
            @RequestParam(required = false) 
            @Parameter(description = "Travel mood for personalized recommendations") 
            MoodType mood,
            
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Generating itinerary for booking {} with mood {} for user {}", bookingId, mood, userDetails.getUsername());
        
        ItineraryResponse response = itineraryService.generateItineraryForUser(bookingId, mood, userDetails.getUsername());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Generate itinerary using POST with request body for more complex requests.
     */
    @PostMapping("/bookings/{bookingId}/itinerary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Generate trip itinerary with preferences",
            description = "Generates a smart itinerary with detailed preference customization"
    )
    public ResponseEntity<ItineraryResponse> generateItineraryWithRequest(
            @PathVariable Long bookingId,
            @RequestBody ItineraryRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Generating itinerary for booking {} with request: {} for user {}", bookingId, request, userDetails.getUsername());
        
        // Use mood from request
        MoodType mood = request.getTravelMood();
        
        ItineraryResponse response = itineraryService.generateItineraryForUser(bookingId, mood, userDetails.getUsername());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get attractions available in a specific city.
     */
    @GetMapping("/attractions/city/{city}")
    @Operation(
            summary = "Get city attractions",
            description = "Returns all available attractions in a city with category breakdown"
    )
    @ApiResponse(responseCode = "200", description = "Attractions retrieved successfully")
    public ResponseEntity<CityAttractionsResponse> getCityAttractions(
            @PathVariable 
            @Parameter(description = "City name (e.g., Chennai, Ooty)") 
            String city
    ) {
        log.info("Fetching attractions for city: {}", city);
        
        CityAttractionsResponse response = itineraryService.getCityAttractions(city);
        
        return ResponseEntity.ok(response);
    }
}
