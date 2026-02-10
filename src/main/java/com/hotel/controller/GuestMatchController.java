package com.hotel.controller;

import com.hotel.domain.dto.match.GuestMatchDto;
import com.hotel.domain.entity.User;
import com.hotel.service.GuestMatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Guest Match - Personalized Recommendations
 * 
 * Provides endpoints for:
 * - Personalized hotel recommendations
 * - Contextual recommendations based on current browsing
 * - Popular recommendations for new users
 */
@RestController
@RequestMapping("/api/guest-match")

public class GuestMatchController {
    
    private final GuestMatchService guestMatchService;
    
    public GuestMatchController(GuestMatchService guestMatchService) {
        this.guestMatchService = guestMatchService;
    }
    
    /**
     * Get personalized recommendations for authenticated user
     * 
     * @param user Current authenticated user
     * @return Personalized hotel and room recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<GuestMatchDto> getRecommendations(@AuthenticationPrincipal User user) {
        
        if (user == null) {
            // Return popular recommendations for anonymous users
            return ResponseEntity.ok(guestMatchService.getPopularRecommendations());
        }
        
        GuestMatchDto recommendations = guestMatchService.getRecommendations(user.getId());
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Get contextual recommendations while viewing a specific hotel
     * 
     * @param user Current authenticated user (optional)
     * @param currentHotelId ID of currently viewed hotel (to exclude)
     * @return Contextual recommendations
     */
    @GetMapping("/recommendations/context")
    public ResponseEntity<GuestMatchDto> getContextualRecommendations(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long currentHotelId) {
        
        if (user == null) {
            GuestMatchDto popular = guestMatchService.getPopularRecommendations();
            // Filter out current hotel if provided
            if (currentHotelId != null) {
                popular.getRecommendedHotels().removeIf(h -> h.getHotelId().equals(currentHotelId));
            }
            return ResponseEntity.ok(popular);
        }
        
        GuestMatchDto recommendations = guestMatchService.getContextualRecommendations(user.getId(), currentHotelId);
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Get popular recommendations for new/anonymous users
     * 
     * @return Popular hotel recommendations
     */
    @GetMapping("/popular")
    public ResponseEntity<GuestMatchDto> getPopularRecommendations() {
        GuestMatchDto recommendations = guestMatchService.getPopularRecommendations();
        return ResponseEntity.ok(recommendations);
    }
}
