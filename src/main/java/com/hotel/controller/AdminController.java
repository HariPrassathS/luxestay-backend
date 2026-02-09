package com.hotel.controller;

import com.hotel.domain.dto.admin.AdminStatsDto;
import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.booking.UpdateBookingStatusRequest;
import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.common.PagedResponse;
import com.hotel.domain.dto.destination.CreateDestinationRequest;
import com.hotel.domain.dto.destination.DestinationDto;
import com.hotel.domain.dto.hotel.CreateHotelRequest;
import com.hotel.domain.dto.hotel.HotelDto;
import com.hotel.domain.dto.owner.CreateHotelOwnerRequest;
import com.hotel.domain.dto.owner.HotelApprovalRequest;
import com.hotel.domain.dto.owner.HotelOwnerDto;
import com.hotel.domain.dto.room.CreateRoomRequest;
import com.hotel.domain.dto.room.RoomDto;
import com.hotel.domain.dto.user.UpdateUserRoleRequest;
import com.hotel.domain.dto.user.UserDto;
import com.hotel.domain.entity.User;
import com.hotel.service.BookingService;
import com.hotel.service.DestinationService;
import com.hotel.service.HotelOwnerService;
import com.hotel.service.HotelSeedService;
import com.hotel.service.HotelService;
import com.hotel.service.ReviewService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for admin endpoints.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative endpoints (ADMIN role required)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final UserService userService;
    private final HotelOwnerService hotelOwnerService;
    private final ReviewService reviewService;
    private final DestinationService destinationService;
    private final HotelSeedService hotelSeedService;

    public AdminController(HotelService hotelService, 
                           RoomService roomService, 
                           BookingService bookingService,
                           UserService userService,
                           HotelOwnerService hotelOwnerService,
                           ReviewService reviewService,
                           DestinationService destinationService,
                           HotelSeedService hotelSeedService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.hotelOwnerService = hotelOwnerService;
        this.reviewService = reviewService;
        this.destinationService = destinationService;
        this.hotelSeedService = hotelSeedService;
    }

    // ==================== Dashboard Stats ====================

    /**
     * Get admin dashboard statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard stats", description = "Retrieve statistics for admin dashboard")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getDashboardStats() {
        AdminStatsDto stats = userService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ==================== User Management ====================

    /**
     * Get all users with pagination.
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDto> userPage = userService.getAllUsers(page, size);
        
        PagedResponse<UserDto> response = PagedResponse.<UserDto>builder()
                .content(userPage.getContent())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by ID")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Update user role.
     */
    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role", description = "Update user role")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        UserDto user = userService.updateUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", user));
    }

    /**
     * Toggle user active status.
     */
    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Toggle user status", description = "Enable or disable a user account")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserStatus(@PathVariable Long id) {
        UserDto user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated", user));
    }

    // ==================== Hotel Management ====================

    /**
     * Get all hotels (including inactive).
     */
    @GetMapping("/hotels")
    @Operation(summary = "Get all hotels", description = "Retrieve all hotels including inactive ones")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getAllHotels() {
        List<HotelDto> hotels = hotelService.getAllHotelsAdmin();
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    /**
     * Create a new hotel.
     */
    @PostMapping("/hotels")
    @Operation(summary = "Create hotel", description = "Create a new hotel")
    public ResponseEntity<ApiResponse<HotelDto>> createHotel(
            @Valid @RequestBody CreateHotelRequest request) {
        HotelDto hotel = hotelService.createHotel(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotel created successfully", hotel));
    }

    /**
     * Update an existing hotel.
     */
    @PutMapping("/hotels/{id}")
    @Operation(summary = "Update hotel", description = "Update an existing hotel")
    public ResponseEntity<ApiResponse<HotelDto>> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody CreateHotelRequest request) {
        HotelDto hotel = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hotel updated successfully", hotel));
    }

    /**
     * Delete (deactivate) a hotel.
     */
    @DeleteMapping("/hotels/{id}")
    @Operation(summary = "Delete hotel", description = "Deactivate a hotel")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.ok(ApiResponse.success("Hotel deleted successfully"));
    }

    // ==================== Room Management ====================

    /**
     * Get all rooms (Admin only).
     */
    @GetMapping("/rooms")
    @Operation(summary = "Get all rooms", description = "Retrieve all rooms across all hotels")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAllRooms() {
        List<RoomDto> rooms = roomService.getAllRoomsAdmin();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Create a new room.
     */
    @PostMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "Create room", description = "Create a new room in a hotel")
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody CreateRoomRequest request) {
        RoomDto room = roomService.createRoom(hotelId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", room));
    }

    /**
     * Update an existing room.
     */
    @PutMapping("/rooms/{id}")
    @Operation(summary = "Update room", description = "Update an existing room")
    public ResponseEntity<ApiResponse<RoomDto>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomRequest request) {
        RoomDto room = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", room));
    }

    /**
     * Delete (disable) a room.
     */
    @DeleteMapping("/rooms/{id}")
    @Operation(summary = "Delete room", description = "Disable a room")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully"));
    }

    /**
     * Toggle room availability.
     */
    @PatchMapping("/rooms/{id}/availability")
    @Operation(summary = "Toggle room availability", description = "Toggle room availability status")
    public ResponseEntity<ApiResponse<RoomDto>> toggleRoomAvailability(@PathVariable Long id) {
        RoomDto room = roomService.toggleAvailability(id);
        return ResponseEntity.ok(ApiResponse.success("Room availability updated", room));
    }

    // ==================== Booking Management ====================

    /**
     * Get all bookings with pagination.
     */
    @GetMapping("/bookings")
    @Operation(summary = "Get all bookings", description = "Retrieve all bookings with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDto>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookingDto> bookingPage = bookingService.getAllBookings(page, size);
        
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
     * Search bookings.
     */
    @GetMapping("/bookings/search")
    @Operation(summary = "Search bookings", description = "Search bookings by reference or user info")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDto>>> searchBookings(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookingDto> bookingPage = bookingService.searchBookings(q, page, size);
        
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
     * Update booking status.
     */
    @PatchMapping("/bookings/{id}/status")
    @Operation(summary = "Update booking status", description = "Update the status of a booking")
    public ResponseEntity<ApiResponse<BookingDto>> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        BookingDto booking = bookingService.updateBookingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Booking status updated", booking));
    }

    /**
     * Get bookings for a hotel.
     */
    @GetMapping("/hotels/{hotelId}/bookings")
    @Operation(summary = "Get hotel bookings", description = "Retrieve all bookings for a specific hotel")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getHotelBookings(@PathVariable Long hotelId) {
        List<BookingDto> bookings = bookingService.getBookingsByHotel(hotelId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get today's check-ins.
     */
    @GetMapping("/bookings/today-checkins")
    @Operation(summary = "Get today's check-ins", description = "Retrieve bookings with today's check-in date")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getTodayCheckIns() {
        List<BookingDto> bookings = bookingService.getTodayCheckIns();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get today's check-outs.
     */
    @GetMapping("/bookings/today-checkouts")
    @Operation(summary = "Get today's check-outs", description = "Retrieve bookings with today's check-out date")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getTodayCheckOuts() {
        List<BookingDto> bookings = bookingService.getTodayCheckOuts();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    // ==================== Review Moderation ====================

    /**
     * Get all reviews with filters.
     */
    @GetMapping("/reviews")
    @Operation(summary = "Get all reviews", description = "Get all reviews with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<com.hotel.domain.dto.review.ReviewDto>>> getAllReviews(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) com.hotel.domain.entity.ReviewStatus status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        org.springframework.data.domain.Page<com.hotel.domain.dto.review.ReviewDto> reviewPage = 
                reviewService.getAdminReviews(hotelId, status, rating, 
                        org.springframework.data.domain.PageRequest.of(page, size));

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
     * Get pending reviews.
     */
    @GetMapping("/reviews/pending")
    @Operation(summary = "Get pending reviews", description = "Get reviews awaiting moderation")
    public ResponseEntity<ApiResponse<List<com.hotel.domain.dto.review.ReviewDto>>> getPendingReviews() {
        List<com.hotel.domain.dto.review.ReviewDto> reviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * Get flagged reviews.
     */
    @GetMapping("/reviews/flagged")
    @Operation(summary = "Get flagged reviews", description = "Get reviews flagged by hotel owners")
    public ResponseEntity<ApiResponse<List<com.hotel.domain.dto.review.ReviewDto>>> getFlaggedReviews() {
        List<com.hotel.domain.dto.review.ReviewDto> reviews = reviewService.getFlaggedReviews();
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * Get review statistics.
     */
    @GetMapping("/reviews/stats")
    @Operation(summary = "Get review stats", description = "Get review moderation statistics")
    public ResponseEntity<ApiResponse<com.hotel.domain.dto.review.AdminReviewStatsDto>> getReviewStats() {
        com.hotel.domain.dto.review.AdminReviewStatsDto stats = reviewService.getAdminReviewStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Update review status (approve/reject).
     */
    @PatchMapping("/reviews/{id}/status")
    @Operation(summary = "Update review status", description = "Approve or reject a review")
    public ResponseEntity<ApiResponse<com.hotel.domain.dto.review.ReviewDto>> updateReviewStatus(
            @PathVariable Long id,
            @Valid @RequestBody com.hotel.domain.dto.review.UpdateReviewStatusRequest request) {
        com.hotel.domain.dto.review.ReviewDto review = reviewService.updateReviewStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Review status updated", review));
    }

    /**
     * Edit review content (remove abusive content).
     */
    @PatchMapping("/reviews/{id}/content")
    @Operation(summary = "Edit review content", description = "Edit review content to remove abusive material")
    public ResponseEntity<ApiResponse<com.hotel.domain.dto.review.ReviewDto>> editReviewContent(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam String reason) {
        com.hotel.domain.dto.review.ReviewDto review = reviewService.editReviewContent(id, content, reason);
        return ResponseEntity.ok(ApiResponse.success("Review content updated", review));
    }

    /**
     * Delete a review.
     */
    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Delete review", description = "Delete a review permanently")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @RequestParam String reason) {
        reviewService.deleteReview(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }

    // ==================== Hotel Owner Management ====================

    /**
     * Get all hotel owners.
     */
    @GetMapping("/hotel-owners")
    @Operation(summary = "Get all hotel owners", description = "Retrieve all hotel owner accounts")
    public ResponseEntity<ApiResponse<List<HotelOwnerDto>>> getAllHotelOwners() {
        List<HotelOwnerDto> owners = hotelOwnerService.getAllHotelOwners();
        return ResponseEntity.ok(ApiResponse.success(owners));
    }

    /**
     * Create a hotel owner account for an existing hotel.
     */
    @PostMapping("/hotel-owners")
    @Operation(summary = "Create hotel owner", description = "Create a hotel owner account for an existing hotel")
    public ResponseEntity<ApiResponse<HotelOwnerDto>> createHotelOwner(
            @Valid @RequestBody CreateHotelOwnerRequest request) {
        HotelOwnerDto owner = hotelOwnerService.createHotelOwnerAccount(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotel owner account created successfully", owner));
    }

    /**
     * Toggle hotel owner account status.
     */
    @PatchMapping("/hotel-owners/{id}/status")
    @Operation(summary = "Toggle owner status", description = "Enable or disable a hotel owner account")
    public ResponseEntity<ApiResponse<HotelOwnerDto>> toggleOwnerStatus(@PathVariable Long id) {
        HotelOwnerDto owner = hotelOwnerService.toggleOwnerStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Hotel owner status updated", owner));
    }

    /**
     * Reset hotel owner password.
     */
    @PostMapping("/hotel-owners/{id}/reset-password")
    @Operation(summary = "Reset owner password", description = "Reset hotel owner password and require change on next login")
    public ResponseEntity<ApiResponse<String>> resetOwnerPassword(@PathVariable Long id) {
        String tempPassword = hotelOwnerService.resetOwnerPassword(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Password reset successfully. Temporary password: " + tempPassword,
                tempPassword));
    }

    /**
     * Get hotels pending approval.
     */
    @GetMapping("/hotels/pending")
    @Operation(summary = "Get pending hotels", description = "Retrieve hotels pending admin approval")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getPendingHotels() {
        List<HotelDto> hotels = hotelOwnerService.getPendingHotels();
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    /**
     * Approve or reject a hotel registration.
     */
    @PostMapping("/hotels/{id}/approve")
    @Operation(summary = "Approve/reject hotel", description = "Approve or reject a pending hotel registration")
    public ResponseEntity<ApiResponse<HotelDto>> approveHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelApprovalRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User admin = (User) auth.getPrincipal();
        HotelDto hotel = hotelOwnerService.approveHotel(id, request, admin.getId());
        String message = Boolean.TRUE.equals(request.getApproved()) ?
                "Hotel approved successfully" : "Hotel rejected";
        return ResponseEntity.ok(ApiResponse.success(message, hotel));
    }

    // ==================== Destination Management ====================

    /**
     * Get all destinations (including inactive).
     */
    @GetMapping("/destinations")
    @Operation(summary = "Get all destinations", description = "Retrieve all destinations for admin management")
    public ResponseEntity<ApiResponse<List<DestinationDto>>> getAllDestinations() {
        List<DestinationDto> destinations = destinationService.getAllDestinations();
        return ResponseEntity.ok(ApiResponse.success(destinations));
    }

    /**
     * Get destination by ID.
     */
    @GetMapping("/destinations/{id}")
    @Operation(summary = "Get destination by ID", description = "Retrieve destination details by ID")
    public ResponseEntity<ApiResponse<DestinationDto>> getDestinationById(@PathVariable Long id) {
        DestinationDto destination = destinationService.getDestinationById(id);
        return ResponseEntity.ok(ApiResponse.success(destination));
    }

    /**
     * Create a new destination.
     */
    @PostMapping("/destinations")
    @Operation(summary = "Create destination", description = "Create a new destination")
    public ResponseEntity<ApiResponse<DestinationDto>> createDestination(
            @Valid @RequestBody CreateDestinationRequest request) {
        DestinationDto destination = destinationService.createDestination(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Destination created successfully", destination));
    }

    /**
     * Update an existing destination by ID.
     */
    @PutMapping("/destinations/{id}")
    @Operation(summary = "Update destination", description = "Update an existing destination by ID")
    public ResponseEntity<ApiResponse<DestinationDto>> updateDestination(
            @PathVariable Long id,
            @Valid @RequestBody CreateDestinationRequest request) {
        DestinationDto destination = destinationService.updateDestination(id, request);
        return ResponseEntity.ok(ApiResponse.success("Destination updated successfully", destination));
    }

    /**
     * Update destination by city name.
     */
    @PutMapping("/destinations/city/{city}")
    @Operation(summary = "Update destination by city", description = "Update an existing destination by city name")
    public ResponseEntity<ApiResponse<DestinationDto>> updateDestinationByCity(
            @PathVariable String city,
            @Valid @RequestBody CreateDestinationRequest request) {
        DestinationDto destination = destinationService.updateDestinationByCity(city, request);
        return ResponseEntity.ok(ApiResponse.success("Destination updated successfully", destination));
    }

    /**
     * Delete destination by ID.
     */
    @DeleteMapping("/destinations/{id}")
    @Operation(summary = "Delete destination", description = "Delete a destination by ID")
    public ResponseEntity<ApiResponse<Void>> deleteDestination(@PathVariable Long id) {
        destinationService.deleteDestination(id);
        return ResponseEntity.ok(ApiResponse.success("Destination deleted successfully", null));
    }

    /**
     * Delete destination by city name.
     */
    @DeleteMapping("/destinations/city/{city}")
    @Operation(summary = "Delete destination by city", description = "Delete a destination by city name")
    public ResponseEntity<ApiResponse<Void>> deleteDestinationByCity(@PathVariable String city) {
        destinationService.deleteDestinationByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Destination deleted successfully", null));
    }

    /**
     * Seed default destinations (if none exist).
     */
    @PostMapping("/destinations/seed")
    @Operation(summary = "Seed destinations", description = "Seed default destinations if database is empty")
    public ResponseEntity<ApiResponse<List<DestinationDto>>> seedDestinations() {
        destinationService.seedDefaultDestinations();
        List<DestinationDto> destinations = destinationService.getAllDestinations();
        return ResponseEntity.ok(ApiResponse.success("Destinations seeded successfully", destinations));
    }

    // ==================== Hotel Seeding ====================

    /**
     * Seed hotels for missing cities (Las Vegas, Los Angeles, London, Barcelona).
     * Each city gets 1 hotel with 4 rooms.
     */
    @PostMapping("/hotels/seed-cities")
    @Operation(summary = "Seed hotels for cities", description = "Seed hotels for Las Vegas, Los Angeles, London, and Barcelona destinations")
    public ResponseEntity<ApiResponse<String>> seedCityHotels() {
        var seededHotels = hotelSeedService.seedMissingCityHotels();
        if (seededHotels.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("All cities already have hotels", "No new hotels added"));
        }
        String message = "Seeded " + seededHotels.size() + " hotel(s) with 4 rooms each: " + 
                         seededHotels.stream().map(h -> h.getCity()).reduce((a, b) -> a + ", " + b).orElse("");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, message));
    }
}
