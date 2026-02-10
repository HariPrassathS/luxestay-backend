package com.hotel.controller;

import com.hotel.domain.dto.trust.TrustScoreDto;
import com.hotel.service.TrustScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TrustScore REST Controller
 * 
 * Provides endpoints for retrieving hotel trust scores.
 * TrustScore is a deterministic, explainable metric based on verified reviews.
 */
@RestController
@RequestMapping("/api/trust")
@Tag(name = "Trust Score", description = "Hotel trust and reliability metrics")
public class TrustScoreController {
    
    private final TrustScoreService trustScoreService;
    
    public TrustScoreController(TrustScoreService trustScoreService) {
        this.trustScoreService = trustScoreService;
    }
    
    /**
     * Get TrustScore for a specific hotel.
     * 
     * @param hotelId The hotel ID
     * @return TrustScoreDto with score, breakdown, praises, and concerns
     */
    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get hotel trust score", 
               description = "Returns the deterministic trust score for a hotel based on verified reviews")
    public ResponseEntity<TrustScoreDto> getHotelTrustScore(@PathVariable Long hotelId) {
        TrustScoreDto trustScore = trustScoreService.calculateTrustScore(hotelId);
        return ResponseEntity.ok(trustScore);
    }
    
    /**
     * Get TrustScore summary (lightweight version for listing pages).
     * Returns only score, level, and verified count - no breakdown.
     */
    @GetMapping("/hotel/{hotelId}/summary")
    @Operation(summary = "Get trust score summary",
               description = "Returns a lightweight trust score summary for hotel cards")
    public ResponseEntity<TrustScoreSummary> getHotelTrustScoreSummary(@PathVariable Long hotelId) {
        TrustScoreDto trustScore = trustScoreService.calculateTrustScore(hotelId);
        
        TrustScoreSummary summary = new TrustScoreSummary(
            trustScore.getScore(),
            trustScore.getLevel(),
            trustScore.getVerifiedReviewCount(),
            trustScore.getAverageRating(),
            trustScore.getHasEnoughData()
        );
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Lightweight summary record for hotel listing cards
     */
    public record TrustScoreSummary(
        Integer score,
        String level,
        Integer verifiedReviewCount,
        Double averageRating,
        Boolean hasEnoughData
    ) {}
}
