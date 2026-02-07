package com.hotel.controller;

import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.booking.CreateBookingRequest;
import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for booking endpoints.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Booking management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a new booking.
     * Only users with USER role can create bookings.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create booking", description = "Create a new room booking (USER role only)")
    public ResponseEntity<ApiResponse<BookingDto>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingDto booking = bookingService.createBooking(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    /**
     * Get all bookings for current user.
     */
    @GetMapping("/me")
    @Operation(summary = "Get my bookings", description = "Retrieve all bookings for the current user")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getMyBookings() {
        List<BookingDto> bookings = bookingService.getMyBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get upcoming bookings for current user.
     */
    @GetMapping("/me/upcoming")
    @Operation(summary = "Get upcoming bookings", description = "Retrieve upcoming bookings for the current user")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getMyUpcomingBookings() {
        List<BookingDto> bookings = bookingService.getMyUpcomingBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get past bookings for current user.
     */
    @GetMapping("/me/past")
    @Operation(summary = "Get past bookings", description = "Retrieve past bookings for the current user")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getMyPastBookings() {
        List<BookingDto> bookings = bookingService.getMyPastBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get booking by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get booking", description = "Retrieve a specific booking by ID")
    public ResponseEntity<ApiResponse<BookingDto>> getBookingById(@PathVariable Long id) {
        BookingDto booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Get booking by reference.
     */
    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get booking by reference", description = "Retrieve a booking by its reference number")
    public ResponseEntity<ApiResponse<BookingDto>> getBookingByReference(@PathVariable String reference) {
        BookingDto booking = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Cancel a booking.
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingDto>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        BookingDto booking = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }
}
