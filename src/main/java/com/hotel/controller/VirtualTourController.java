package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.virtualtour.VirtualTourDto;
import com.hotel.domain.dto.virtualtour.VirtualTourSceneDto;
import com.hotel.service.VirtualTourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Virtual Hotel Tours.
 * 
 * PERFORMANCE STRATEGY:
 * - /tours/{hotelId} - Returns tour metadata only (fast)
 * - /tours/view/{tourId} - Full tour with all scenes
 * - /tours/{tourId}/scenes/{sceneId} - Single scene (lazy load)
 */
@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
@Tag(name = "Virtual Tours", description = "360° virtual hotel tour APIs")
public class VirtualTourController {
    
    private final VirtualTourService tourService;
    
    /**
     * Get all tours for a hotel (metadata only - for listing).
     */
    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get hotel tours",
               description = "Returns available virtual tours for a hotel (metadata only)")
    public ResponseEntity<ApiResponse<List<VirtualTourDto>>> getToursByHotel(
            @PathVariable Long hotelId) {
        
        List<VirtualTourDto> tours = tourService.getToursByHotel(hotelId);
        return ResponseEntity.ok(ApiResponse.success(tours));
    }
    
    /**
     * Get full tour with all scenes.
     * This is the main endpoint for viewing a tour.
     */
    @GetMapping("/view/{tourId}")
    @Operation(summary = "View virtual tour",
               description = "Returns full tour data with all scenes and hotspots")
    public ResponseEntity<ApiResponse<VirtualTourDto>> viewTour(
            @PathVariable Long tourId) {
        
        return tourService.getTourWithScenes(tourId)
                .map(tour -> ResponseEntity.ok(ApiResponse.success(tour)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get a single scene (for lazy loading).
     */
    @GetMapping("/{tourId}/scenes/{sceneId}")
    @Operation(summary = "Get tour scene",
               description = "Returns a single scene with hotspots (for lazy loading)")
    public ResponseEntity<ApiResponse<VirtualTourSceneDto>> getScene(
            @PathVariable Long tourId,
            @PathVariable Long sceneId) {
        
        return tourService.getScene(tourId, sceneId)
                .map(scene -> ResponseEntity.ok(ApiResponse.success(scene)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Check if a hotel has virtual tours.
     */
    @GetMapping("/check/{hotelId}")
    @Operation(summary = "Check tour availability",
               description = "Returns whether a hotel has virtual tours available")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkTourAvailability(
            @PathVariable Long hotelId) {
        
        boolean hasTours = tourService.hasVirtualTours(hotelId);
        long count = hasTours ? tourService.getTourCount(hotelId) : 0;
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "hasTours", hasTours,
                "tourCount", count
        )));
    }
}
