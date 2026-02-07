package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.wishlist.WishlistItemDto;
import com.hotel.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for wishlist/favorites operations.
 * Requires authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist", description = "User wishlist/favorites management")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /**
     * Get current user's wishlist.
     */
    @GetMapping
    @Operation(summary = "Get wishlist", description = "Get all hotels in current user's wishlist")
    public ResponseEntity<ApiResponse<List<WishlistItemDto>>> getWishlist() {
        List<WishlistItemDto> wishlist = wishlistService.getWishlist();
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }

    /**
     * Add hotel to wishlist.
     */
    @PostMapping("/{hotelId}")
    @Operation(summary = "Add to wishlist", description = "Add a hotel to current user's wishlist")
    public ResponseEntity<ApiResponse<WishlistItemDto>> addToWishlist(@PathVariable Long hotelId) {
        WishlistItemDto item = wishlistService.addToWishlist(hotelId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Added to wishlist", item));
    }

    /**
     * Remove hotel from wishlist.
     */
    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Remove from wishlist", description = "Remove a hotel from current user's wishlist")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable Long hotelId) {
        wishlistService.removeFromWishlist(hotelId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }

    /**
     * Toggle hotel in wishlist.
     * Returns whether the hotel is now in wishlist (true=added, false=removed).
     */
    @PostMapping("/{hotelId}/toggle")
    @Operation(summary = "Toggle wishlist", description = "Add if not present, remove if present")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleWishlist(@PathVariable Long hotelId) {
        boolean isNowInWishlist = wishlistService.toggleWishlist(hotelId);
        String message = isNowInWishlist ? "Added to wishlist" : "Removed from wishlist";
        return ResponseEntity.ok(ApiResponse.success(message, Map.of("inWishlist", isNowInWishlist)));
    }

    /**
     * Check if a hotel is in wishlist.
     */
    @GetMapping("/{hotelId}/check")
    @Operation(summary = "Check wishlist", description = "Check if a hotel is in current user's wishlist")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlist(@PathVariable Long hotelId) {
        boolean inWishlist = wishlistService.isInWishlist(hotelId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inWishlist", inWishlist)));
    }

    /**
     * Get all wishlisted hotel IDs (for bulk checking).
     */
    @GetMapping("/ids")
    @Operation(summary = "Get wishlisted hotel IDs", description = "Get list of hotel IDs in user's wishlist")
    public ResponseEntity<ApiResponse<Set<Long>>> getWishlistedIds() {
        Set<Long> hotelIds = wishlistService.getWishlistedHotelIds();
        return ResponseEntity.ok(ApiResponse.success(hotelIds));
    }

    /**
     * Get wishlist count.
     */
    @GetMapping("/count")
    @Operation(summary = "Get wishlist count", description = "Get number of hotels in user's wishlist")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getWishlistCount() {
        long count = wishlistService.getWishlistCount();
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }
}
