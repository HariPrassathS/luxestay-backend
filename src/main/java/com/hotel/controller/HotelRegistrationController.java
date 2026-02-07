package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.hotel.HotelDto;
import com.hotel.domain.dto.owner.HotelRegistrationRequest;
import com.hotel.service.HotelOwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for public hotel registration.
 * Allows prospective hotel owners to register their hotels.
 */
@RestController
@RequestMapping("/api/register-hotel")
@Tag(name = "Hotel Registration", description = "Public endpoints for hotel registration")
public class HotelRegistrationController {

    private final HotelOwnerService hotelOwnerService;

    public HotelRegistrationController(HotelOwnerService hotelOwnerService) {
        this.hotelOwnerService = hotelOwnerService;
    }

    /**
     * Register a new hotel with owner account.
     * The hotel is created in PENDING status and requires admin approval.
     */
    @PostMapping
    @Operation(summary = "Register hotel",
            description = "Register a new hotel. Hotel will be pending admin approval before going live.")
    public ResponseEntity<ApiResponse<HotelDto>> registerHotel(
            @Valid @RequestBody HotelRegistrationRequest request) {
        HotelDto hotel = hotelOwnerService.registerHotel(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Hotel registration submitted successfully. Your hotel is pending admin approval. " +
                        "You will receive an email when your hotel is approved.",
                        hotel));
    }
}
