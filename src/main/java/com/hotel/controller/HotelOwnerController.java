package com.hotel.controller;

import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.booking.UpdateBookingStatusRequest;
import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.common.PagedResponse;
import com.hotel.domain.dto.hotel.HotelDto;
import com.hotel.domain.dto.owner.*;
import com.hotel.domain.dto.room.CreateRoomRequest;
import com.hotel.domain.dto.room.RoomDto;
import com.hotel.service.HotelOwnerService;
import com.hotel.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for HOTEL_OWNER endpoints.
 * All endpoints require HOTEL_OWNER role and are scoped to the owner's hotel.
 */
@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('HOTEL_OWNER')")
@Tag(name = "Hotel Owner", description = "Hotel owner management endpoints (HOTEL_OWNER role required)")
@SecurityRequirement(name = "Bearer Authentication")
public class HotelOwnerController {

    private final HotelOwnerService hotelOwnerService;
    private final ReviewService reviewService;

    public HotelOwnerController(HotelOwnerService hotelOwnerService, ReviewService reviewService) {
        this.hotelOwnerService = hotelOwnerService;
        this.reviewService = reviewService;
    }

    // ==================== Dashboard ====================

    /**
     * Get owner's dashboard with hotel info, stats, and recent activity.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard", description = "Get hotel owner dashboard with stats and recent activity")
    public ResponseEntity<ApiResponse<OwnerDashboardDto>> getDashboard() {
        OwnerDashboardDto dashboard = hotelOwnerService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    /**
     * Get detailed statistics for the owner's hotel.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get statistics", description = "Get detailed statistics for the hotel")
    public ResponseEntity<ApiResponse<OwnerStatsDto>> getStats() {
        OwnerStatsDto stats = hotelOwnerService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ==================== Hotel Management ====================

    /**
     * Get the owner's hotel details.
     */
    @GetMapping("/hotel")
    @Operation(summary = "Get my hotel", description = "Get the hotel details for the authenticated owner")
    public ResponseEntity<ApiResponse<HotelDto>> getMyHotel() {
        HotelDto hotel = hotelOwnerService.getMyHotel();
        return ResponseEntity.ok(ApiResponse.success(hotel));
    }

    /**
     * Update the owner's hotel.
     */
    @PutMapping("/hotel")
    @Operation(summary = "Update my hotel", description = "Update hotel information (limited fields)")
    public ResponseEntity<ApiResponse<HotelDto>> updateMyHotel(
            @Valid @RequestBody OwnerHotelUpdateRequest request) {
        HotelDto hotel = hotelOwnerService.updateMyHotel(request);
        return ResponseEntity.ok(ApiResponse.success("Hotel updated successfully", hotel));
    }

    // ==================== Room Management ====================

    /**
     * Get all rooms for the owner's hotel.
     */
    @GetMapping("/rooms")
    @Operation(summary = "Get my rooms", description = "Get all rooms for the owner's hotel")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getMyRooms() {
        List<RoomDto> rooms = hotelOwnerService.getMyRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Get a specific room.
     */
    @GetMapping("/rooms/{id}")
    @Operation(summary = "Get room", description = "Get room details (with ownership validation)")
    public ResponseEntity<ApiResponse<RoomDto>> getRoom(@PathVariable Long id) {
        RoomDto room = hotelOwnerService.getRoom(id);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * Create a new room.
     */
    @PostMapping("/rooms")
    @Operation(summary = "Create room", description = "Create a new room in the owner's hotel")
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {
        RoomDto room = hotelOwnerService.createRoom(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", room));
    }

    /**
     * Update a room.
     */
    @PutMapping("/rooms/{id}")
    @Operation(summary = "Update room", description = "Update room details")
    public ResponseEntity<ApiResponse<RoomDto>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomRequest request) {
        RoomDto room = hotelOwnerService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", room));
    }

    /**
     * Toggle room availability (soft delete/enable).
     */
    @PatchMapping("/rooms/{id}/availability")
    @Operation(summary = "Toggle room availability", description = "Enable or disable a room")
    public ResponseEntity<ApiResponse<RoomDto>> toggleRoomAvailability(@PathVariable Long id) {
        RoomDto room = hotelOwnerService.toggleRoomAvailability(id);
        return ResponseEntity.ok(ApiResponse.success("Room availability updated", room));
    }

    // ==================== Booking Management ====================

    /**
     * Get all bookings for the owner's hotel.
     */
    @GetMapping("/bookings")
    @Operation(summary = "Get my bookings", description = "Get all bookings for the owner's hotel")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDto>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<BookingDto> bookingPage = hotelOwnerService.getMyBookings(PageRequest.of(page, size, sort));

        PagedResponse<BookingDto> response = PagedResponse.<BookingDto>builder()
                .content(bookingPage.getContent())
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .first(bookingPage.isFirst())
                .last(bookingPage.isLast())
                .hasNext(bookingPage.hasNext())
                .hasPrevious(bookingPage.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get a specific booking.
     */
    @GetMapping("/bookings/{id}")
    @Operation(summary = "Get booking", description = "Get booking details (with ownership validation)")
    public ResponseEntity<ApiResponse<BookingDto>> getBooking(@PathVariable Long id) {
        BookingDto booking = hotelOwnerService.getBooking(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Update booking status (check-in/check-out).
     * Owners cannot cancel bookings.
     */
    @PatchMapping("/bookings/{id}/status")
    @Operation(summary = "Update booking status", description = "Update booking status (check-in/check-out only)")
    public ResponseEntity<ApiResponse<BookingDto>> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        BookingDto booking = hotelOwnerService.updateBookingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Booking status updated", booking));
    }

    /**
     * Get today's check-ins.
     */
    @GetMapping("/bookings/today-checkins")
    @Operation(summary = "Get today's check-ins", description = "Get bookings with today's check-in date")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getTodayCheckIns() {
        List<BookingDto> bookings = hotelOwnerService.getTodayCheckIns();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get today's check-outs.
     */
    @GetMapping("/bookings/today-checkouts")
    @Operation(summary = "Get today's check-outs", description = "Get bookings with today's check-out date")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getTodayCheckOuts() {
        List<BookingDto> bookings = hotelOwnerService.getTodayCheckOuts();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    // ==================== Review Management ====================

    /**
     * Get all reviews for the owner's hotel.
     */
    @GetMapping("/reviews")
    @Operation(summary = "Get my reviews", description = "Get all reviews for the owner's hotel")
    public ResponseEntity<ApiResponse<PagedResponse<com.hotel.domain.dto.review.ReviewDto>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("asc") ?
                org.springframework.data.domain.Sort.by(sortBy).ascending() : 
                org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Page<com.hotel.domain.dto.review.ReviewDto> reviewPage = 
                reviewService.getOwnerReviews(org.springframework.data.domain.PageRequest.of(page, size, sort));

        PagedResponse<com.hotel.domain.dto.review.ReviewDto> response = PagedResponse.<com.hotel.domain.dto.review.ReviewDto>builder()
                .content(reviewPage.getContent())
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .first(reviewPage.isFirst())
                .last(reviewPage.isLast())
                .hasNext(reviewPage.hasNext())
                .hasPrevious(reviewPage.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Reply to a review.
     */
    @PostMapping("/reviews/{id}/reply")
    @Operation(summary = "Reply to review", description = "Reply to a customer review")
    public ResponseEntity<ApiResponse<com.hotel.domain.dto.review.ReviewReplyDto>> replyToReview(
            @PathVariable Long id,
            @Valid @RequestBody com.hotel.domain.dto.review.CreateReplyRequest request) {
        com.hotel.domain.dto.review.ReviewReplyDto reply = reviewService.replyToReview(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reply submitted successfully", reply));
    }

    /**
     * Flag a review for admin attention.
     */
    @PostMapping("/reviews/{id}/flag")
    @Operation(summary = "Flag review", description = "Flag a review for admin moderation")
    public ResponseEntity<ApiResponse<com.hotel.domain.dto.review.ReviewDto>> flagReview(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        com.hotel.domain.dto.review.ReviewDto review = reviewService.flagReview(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Review flagged for admin review", review));
    }

    /**
     * Get reviews without reply (need attention).
     */
    @GetMapping("/reviews/pending-reply")
    @Operation(summary = "Get reviews pending reply", description = "Get approved reviews without owner reply")
    public ResponseEntity<ApiResponse<List<com.hotel.domain.dto.review.ReviewDto>>> getReviewsPendingReply() {
        List<com.hotel.domain.dto.review.ReviewDto> reviews = reviewService.getReviewsWithoutReply();
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    // ==================== Account Management ====================

    /**
     * Change password.
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the owner's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        hotelOwnerService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
