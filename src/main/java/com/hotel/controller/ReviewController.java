package com.hotel.controller;

import com.hotel.annotation.RateLimited;
import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.review.*;
import com.hotel.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for review endpoints.
 * Contains both public and authenticated endpoints.
 * 
 * TRUST & ABUSE PREVENTION:
 * - Rate limiting on review submission (5 per hour per user)
 * - All reviews require verified stay (booking checked out)
 * - Admin moderation enforced (PENDING→APPROVED flow)
 * - XSS protection via HTML escaping in service layer
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Hotel review and feedback endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ==================== Public Endpoints ====================

    /**
     * Get approved reviews for a hotel (public).
     */
    @GetMapping("/hotels/{hotelId}")
    @Operation(summary = "Get hotel reviews", description = "Get approved reviews for a hotel (public)")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getHotelReviews(@PathVariable Long hotelId) {
        List<ReviewDto> reviews = reviewService.getApprovedReviewsForHotel(hotelId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * Get approved reviews with pagination (public).
     */
    @GetMapping("/hotels/{hotelId}/paged")
    @Operation(summary = "Get hotel reviews (paged)", description = "Get approved reviews with pagination")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getHotelReviewsPaged(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReviewDto> reviews = reviewService.getApprovedReviewsForHotel(hotelId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * Get review statistics for a hotel (public).
     */
    @GetMapping("/hotels/{hotelId}/stats")
    @Operation(summary = "Get hotel review stats", description = "Get rating statistics for a hotel")
    public ResponseEntity<ApiResponse<HotelReviewStatsDto>> getHotelReviewStats(
            @PathVariable Long hotelId) {
        HotelReviewStatsDto stats = reviewService.getHotelReviewStats(hotelId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ==================== User Endpoints (Authenticated) ====================

    /**
     * Create a new review.
     * Only allowed for users with CHECKED_OUT bookings.
     * Rate limited to 5 reviews per hour per user.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @RateLimited(key = "review", limit = 5, windowSeconds = 3600, 
                 message = "You can only submit 5 reviews per hour. Please try again later.")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create review", description = "Submit a review for a completed booking (USER only)")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewDto review = reviewService.createReview(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully. Pending approval.", review));
    }

    /**
     * Get current user's reviews.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my reviews", description = "Get all reviews submitted by the current user")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getMyReviews() {
        List<ReviewDto> reviews = reviewService.getMyReviews();
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * Check if user can review a booking.
     */
    @GetMapping("/can-review/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Check review eligibility", description = "Check if user can review a specific booking")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> canReviewBooking(
            @PathVariable Long bookingId) {
        boolean canReview = reviewService.canReviewBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("canReview", canReview)));
    }

    /**
     * Get review info for a booking (for edit/view functionality).
     */
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get review for booking", description = "Get the review submitted for a specific booking")
    public ResponseEntity<ApiResponse<ReviewDto>> getReviewForBooking(
            @PathVariable Long bookingId) {
        ReviewDto review = reviewService.getReviewForBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    /**
     * Update an existing review (only if status is PENDING).
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update review", description = "Update a pending review")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewDto review = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully.", review));
    }
}
