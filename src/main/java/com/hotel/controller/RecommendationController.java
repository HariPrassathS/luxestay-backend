package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.recommendation.RecommendationDto;
import com.hotel.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for personalized hotel recommendations.
 * 
 * EXPLAINABILITY:
 * - All recommendations include scoring breakdown
 * - Users can see WHY hotels are recommended
 * - Transparent, DB-driven personalization
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Personalized hotel recommendation APIs")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    private final com.hotel.repository.UserRepository userRepository;
    
    /**
     * Get personalized recommendations for the authenticated user.
     * Falls back to popular recommendations for anonymous users.
     */
    @GetMapping
    @Operation(summary = "Get personalized recommendations",
               description = "Returns personalized hotel recommendations based on booking history")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "6") int limit) {
        
        List<RecommendationDto> recommendations;
        
        if (userDetails != null) {
            var user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                recommendations = recommendationService.getPersonalizedRecommendations(user.getId(), limit);
                if (recommendations.isEmpty()) {
                    // Fall back to popular if user has no booking history
                    recommendations = recommendationService.getPopularRecommendations(limit);
                }
            } else {
                recommendations = recommendationService.getPopularRecommendations(limit);
            }
        } else {
            recommendations = recommendationService.getPopularRecommendations(limit);
        }
        
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
    
    /**
     * Get popular/trending recommendations (no auth required).
     */
    @GetMapping("/popular")
    @Operation(summary = "Get popular recommendations",
               description = "Returns popular hotels based on bookings and ratings")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getPopularRecommendations(
            @RequestParam(defaultValue = "6") int limit) {
        
        List<RecommendationDto> recommendations = recommendationService.getPopularRecommendations(limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
    
    /**
     * Get similar hotels to a specific hotel.
     */
    @GetMapping("/similar/{hotelId}")
    @Operation(summary = "Get similar hotels",
               description = "Returns hotels similar to the specified hotel")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getSimilarHotels(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "4") int limit) {
        
        List<RecommendationDto> recommendations = recommendationService.getSimilarHotels(hotelId, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
    
    /**
     * Get recommendations for a specific destination.
     */
    @GetMapping("/destination/{city}")
    @Operation(summary = "Get destination recommendations",
               description = "Returns recommended hotels in a specific city")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getDestinationRecommendations(
            @PathVariable String city,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "6") int limit) {
        
        Long userId = null;
        if (userDetails != null) {
            var user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }
        
        List<RecommendationDto> recommendations = 
                recommendationService.getDestinationRecommendations(city, userId, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}
