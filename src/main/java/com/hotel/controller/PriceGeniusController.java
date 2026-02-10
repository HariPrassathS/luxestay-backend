package com.hotel.controller;

import com.hotel.domain.dto.price.PriceGeniusDto;
import com.hotel.service.PriceGeniusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Price Genius - Price Analysis & Insights
 * 
 * Provides endpoints for:
 * - Room price analysis
 * - Hotel best deal analysis
 * - Market comparisons
 */
@RestController
@RequestMapping("/api/price-genius")
public class PriceGeniusController {
    
    private final PriceGeniusService priceGeniusService;
    
    public PriceGeniusController(PriceGeniusService priceGeniusService) {
        this.priceGeniusService = priceGeniusService;
    }
    
    /**
     * Get price insights for a specific room
     * 
     * @param hotelId Hotel ID
     * @param roomId Room ID
     * @return Price analysis with deal rating, market comparison, and booking advice
     */
    @GetMapping("/hotel/{hotelId}/room/{roomId}")
    public ResponseEntity<PriceGeniusDto> getRoomPriceInsights(
            @PathVariable Long hotelId,
            @PathVariable Long roomId) {
        
        PriceGeniusDto insights = priceGeniusService.getRoomPriceInsights(hotelId, roomId);
        
        if (insights == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(insights);
    }
    
    /**
     * Get price insights for a hotel (best deal among all rooms)
     * 
     * @param hotelId Hotel ID
     * @return Price analysis for the best deal room
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<PriceGeniusDto> getHotelPriceInsights(@PathVariable Long hotelId) {
        
        PriceGeniusDto insights = priceGeniusService.getHotelPriceInsights(hotelId);
        
        if (insights == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(insights);
    }
}
