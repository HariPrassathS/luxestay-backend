package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.destination.DestinationDto;
import com.hotel.service.DestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for public destination endpoints.
 * All endpoints are accessible without authentication.
 */
@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
@Tag(name = "Destinations", description = "Public destination endpoints")
public class DestinationController {

    private final DestinationService destinationService;

    /**
     * Get all active destinations for public display.
     */
    @GetMapping
    @Operation(summary = "Get all destinations", description = "Retrieve all active destinations for homepage")
    public ResponseEntity<ApiResponse<List<DestinationDto>>> getAllDestinations() {
        List<DestinationDto> destinations = destinationService.getActiveDestinations();
        return ResponseEntity.ok(ApiResponse.success(destinations));
    }

    /**
     * Get destinations by region.
     */
    @GetMapping("/region/{region}")
    @Operation(summary = "Get destinations by region", description = "Retrieve destinations filtered by region")
    public ResponseEntity<ApiResponse<List<DestinationDto>>> getDestinationsByRegion(@PathVariable String region) {
        List<DestinationDto> destinations = destinationService.getDestinationsByRegion(region);
        return ResponseEntity.ok(ApiResponse.success(destinations));
    }

    /**
     * Get destination by city name.
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "Get destination by city", description = "Retrieve destination details by city name")
    public ResponseEntity<ApiResponse<DestinationDto>> getDestinationByCity(@PathVariable String city) {
        DestinationDto destination = destinationService.getDestinationByCity(city);
        return ResponseEntity.ok(ApiResponse.success(destination));
    }

    /**
     * Search destinations.
     */
    @GetMapping("/search")
    @Operation(summary = "Search destinations", description = "Search destinations by city name")
    public ResponseEntity<ApiResponse<List<DestinationDto>>> searchDestinations(@RequestParam String q) {
        List<DestinationDto> destinations = destinationService.searchDestinations(q);
        return ResponseEntity.ok(ApiResponse.success(destinations));
    }
}
