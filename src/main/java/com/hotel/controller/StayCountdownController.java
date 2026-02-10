package com.hotel.controller;

import com.hotel.domain.dto.countdown.StayCountdownDto;
import com.hotel.domain.entity.User;
import com.hotel.service.StayCountdownService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Stay Countdown - Journey Progress & Milestones
 * 
 * Provides endpoints for:
 * - Single booking countdown
 * - User's all active countdowns
 */
@RestController
@RequestMapping("/api/countdown")
@CrossOrigin(origins = "*")
public class StayCountdownController {
    
    private final StayCountdownService stayCountdownService;
    
    public StayCountdownController(StayCountdownService stayCountdownService) {
        this.stayCountdownService = stayCountdownService;
    }
    
    /**
     * Get countdown for a specific booking
     * 
     * @param bookingId Booking ID
     * @return Countdown details with milestones and preparation tips
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<StayCountdownDto> getBookingCountdown(@PathVariable Long bookingId) {
        
        StayCountdownDto countdown = stayCountdownService.getBookingCountdown(bookingId);
        
        if (countdown == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(countdown);
    }
    
    /**
     * Get all active countdowns for the authenticated user
     * 
     * @param user Current authenticated user
     * @return List of countdown details for upcoming bookings
     */
    @GetMapping("/my-countdowns")
    public ResponseEntity<List<StayCountdownDto>> getMyCountdowns(@AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<StayCountdownDto> countdowns = stayCountdownService.getUserCountdowns(user.getId());
        
        return ResponseEntity.ok(countdowns);
    }
}
