package com.hotel.controller;

import com.hotel.domain.dto.pulse.LivePulseDto;
import com.hotel.service.LivePulseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Live Pulse endpoints
 * 
 * Provides real-time hotel activity metrics:
 * - Recent booking activity
 * - Availability status
 * - Popularity indicators
 * 
 * All data is REAL - no fabrication, no dark patterns
 */
@RestController
@RequestMapping("/api/pulse")
@Tag(name = "Live Pulse", description = "Real-time hotel activity and popularity metrics")
public class LivePulseController {
    
    private final LivePulseService livePulseService;
    
    public LivePulseController(LivePulseService livePulseService) {
        this.livePulseService = livePulseService;
    }
    
    /**
     * Get full pulse data for a hotel
     * 
     * Returns:
     * - Pulse level (QUIET, STEADY, ACTIVE, POPULAR, TRENDING)
     * - Recent booking counts (24h, 7d)
     * - Room availability
     * - Occupancy rate
     * - Booking trend
     * - Recent activity feed
     */
    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get hotel live pulse", 
               description = "Returns real-time activity metrics for a hotel")
    public ResponseEntity<Map<String, Object>> getHotelPulse(@PathVariable Long hotelId) {
        try {
            LivePulseDto pulse = livePulseService.getHotelPulse(hotelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pulse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to retrieve pulse data");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get quick pulse badge data (minimal, for list views)
     * 
     * Returns only:
     * - Pulse level
     * - Recent bookings count (24h)
     */
    @GetMapping("/hotel/{hotelId}/badge")
    @Operation(summary = "Get quick pulse badge", 
               description = "Returns minimal pulse data for badge display")
    public ResponseEntity<Map<String, Object>> getQuickPulse(@PathVariable Long hotelId) {
        try {
            LivePulseDto pulse = livePulseService.getQuickPulse(hotelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "hotelId", pulse.getHotelId(),
                "pulseLevel", pulse.getPulseLevel().name(),
                "pulseLevelLabel", pulse.getPulseLevel().getLabel(),
                "recentBookings24h", pulse.getRecentBookings24h()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to retrieve pulse data");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get availability snapshot for a hotel
     * 
     * Quick check of current room availability
     */
    @GetMapping("/hotel/{hotelId}/availability")
    @Operation(summary = "Get availability snapshot", 
               description = "Returns current room availability status")
    public ResponseEntity<Map<String, Object>> getAvailabilitySnapshot(@PathVariable Long hotelId) {
        try {
            LivePulseDto pulse = livePulseService.getHotelPulse(hotelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "hotelId", pulse.getHotelId(),
                "availableRooms", pulse.getAvailableRooms(),
                "totalRooms", pulse.getTotalRooms(),
                "occupancyRate", pulse.getOccupancyRate() != null ? pulse.getOccupancyRate() : 0,
                "isLimited", pulse.getAvailableRooms() <= pulse.getTotalRooms() * 0.2,
                "lastUpdated", pulse.getLastUpdated()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to retrieve availability data");
            return ResponseEntity.ok(error);
        }
    }
}
