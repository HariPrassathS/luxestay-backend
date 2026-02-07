package com.hotel.controller;

import com.hotel.domain.dto.booking.BookingConfidenceDto.*;
import com.hotel.service.BookingConfidenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller for Booking Confidence & Risk Intelligence.
 * 
 * Provides transparent, data-driven booking assessments to help
 * users make informed decisions without fear-based messaging.
 */
@RestController
@RequestMapping("/api/bookings/confidence")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Confidence", description = "Booking risk and confidence intelligence APIs")
public class BookingConfidenceController {

    private final BookingConfidenceService confidenceService;

    /**
     * Get booking confidence assessment for a specific room and date range.
     * 
     * @param roomId      The ID of the room to assess
     * @param checkInDate Check-in date (YYYY-MM-DD)
     * @param checkOutDate Check-out date (YYYY-MM-DD)
     * @return Comprehensive confidence assessment with signals and insights
     */
    @GetMapping
    @Operation(
        summary = "Get booking confidence assessment",
        description = "Returns a comprehensive confidence assessment including availability, " +
                      "pricing, demand signals, and actionable insights to help users make " +
                      "informed booking decisions."
    )
    public ResponseEntity<BookingConfidenceResponse> getBookingConfidence(
            @Parameter(description = "Room ID to assess", required = true)
            @RequestParam Long roomId,
            
            @Parameter(description = "Check-in date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            
            @Parameter(description = "Check-out date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate
    ) {
        log.info("Confidence assessment requested for room {} from {} to {}", 
                roomId, checkInDate, checkOutDate);

        BookingConfidenceRequest request = BookingConfidenceRequest.builder()
                .roomId(roomId)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .build();

        BookingConfidenceResponse response = confidenceService.assessBookingConfidence(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get booking confidence assessment via POST with full request body.
     * Useful for more complex queries or when including user context.
     */
    @PostMapping
    @Operation(
        summary = "Get booking confidence assessment (POST)",
        description = "Alternative POST endpoint for confidence assessment. " +
                      "Accepts a full request body for more detailed queries."
    )
    public ResponseEntity<BookingConfidenceResponse> assessBookingConfidence(
            @RequestBody BookingConfidenceRequest request
    ) {
        log.info("Confidence assessment (POST) requested for room {} from {} to {}", 
                request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());

        BookingConfidenceResponse response = confidenceService.assessBookingConfidence(request);
        
        return ResponseEntity.ok(response);
    }
}
