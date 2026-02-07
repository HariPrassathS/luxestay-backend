package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.hotel.HotelDto;
import com.hotel.domain.dto.room.RoomDto;
import com.hotel.domain.dto.search.RoomComparisonDto;
import com.hotel.domain.entity.RoomType;
import com.hotel.service.HotelService;
import com.hotel.service.RoomService;
import com.hotel.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for public hotel endpoints.
 * All endpoints are accessible without authentication.
 */
@RestController
@RequestMapping("/api/hotels")
@Tag(name = "Hotels", description = "Public hotel browsing endpoints")
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final SearchService searchService;

    public HotelController(HotelService hotelService, RoomService roomService, SearchService searchService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.searchService = searchService;
    }

    /**
     * Get all active hotels (paginated and filtered).
     */
    @GetMapping
    @Operation(summary = "Get all hotels", description = "Retrieve list of all active hotels with pagination and filters")
    public ResponseEntity<ApiResponse<Page<HotelDto>>> getAllHotels(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<HotelDto> hotels = hotelService.getHotelsWithFilters(location, minStars, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    /**
     * Get featured hotels (top rated).
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured hotels", description = "Retrieve top-rated hotels")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getFeaturedHotels(
            @RequestParam(defaultValue = "6") int limit) {
        List<HotelDto> hotels = hotelService.getFeaturedHotels(limit);
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    /**
     * Search hotels by query (paginated with filters).
     */
    @GetMapping("/search")
    @Operation(summary = "Search hotels", description = "Search hotels by name, city, or country with filters and pagination")
    public ResponseEntity<ApiResponse<Page<HotelDto>>> searchHotels(
            @RequestParam(required = false) @Parameter(description = "Search query") String q,
            @RequestParam(required = false) @Parameter(description = "Minimum star rating") Integer minStars,
            @RequestParam(required = false) @Parameter(description = "Maximum price per night") java.math.BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Page<HotelDto> hotels = hotelService.searchHotelsPaged(q, minStars, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    /**
     * Get hotel by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get hotel details", description = "Retrieve detailed information about a specific hotel")
    public ResponseEntity<ApiResponse<HotelDto>> getHotelById(@PathVariable Long id) {
        HotelDto hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(ApiResponse.success(hotel));
    }

    /**
     * Get hotels by city.
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "Get hotels by city", description = "Retrieve hotels in a specific city")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getHotelsByCity(@PathVariable String city) {
        List<HotelDto> hotels = hotelService.getHotelsByCity(city);
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    /**
     * Get all unique cities.
     */
    @GetMapping("/cities")
    @Operation(summary = "Get all cities", description = "Retrieve list of all cities with hotels")
    public ResponseEntity<ApiResponse<List<String>>> getAllCities() {
        List<String> cities = hotelService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    /**
     * Get all unique countries.
     */
    @GetMapping("/countries")
    @Operation(summary = "Get all countries", description = "Retrieve list of all countries with hotels")
    public ResponseEntity<ApiResponse<List<String>>> getAllCountries() {
        List<String> countries = hotelService.getAllCountries();
        return ResponseEntity.ok(ApiResponse.success(countries));
    }

    /**
     * Get hotels within map bounding box (for map view).
     */
    @GetMapping("/map/bounds")
    @Operation(summary = "Get hotels in bounds", description = "Retrieve hotels within map viewport bounds")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getHotelsInBounds(
            @RequestParam @Parameter(description = "South latitude") Double south,
            @RequestParam @Parameter(description = "North latitude") Double north,
            @RequestParam @Parameter(description = "West longitude") Double west,
            @RequestParam @Parameter(description = "East longitude") Double east) {
        List<HotelDto> hotels = hotelService.getHotelsInBounds(south, north, west, east);
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    /**
     * Get all hotels with coordinates (for map view).
     */
    @GetMapping("/map")
    @Operation(summary = "Get all mapped hotels", description = "Retrieve all hotels with coordinates for map display")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getAllMappedHotels() {
        List<HotelDto> hotels = hotelService.getAllHotelsWithCoordinates();
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    // ==================== Room Endpoints ====================

    /**
     * Get all rooms for a hotel.
     */
    @GetMapping("/{hotelId}/rooms")
    @Operation(summary = "Get hotel rooms", description = "Retrieve all rooms for a specific hotel")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getHotelRooms(@PathVariable Long hotelId) {
        List<RoomDto> rooms = roomService.getRoomsByHotelId(hotelId);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Get available rooms for specific dates.
     */
    @GetMapping("/{hotelId}/rooms/available")
    @Operation(summary = "Get available rooms", description = "Retrieve rooms available for specific dates")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) RoomType roomType) {
        
        List<RoomDto> rooms = roomService.getAvailableRoomsWithFilters(
                hotelId, checkIn, checkOut, guests, roomType);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Get room by ID.
     */
    @GetMapping("/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Get room details", description = "Retrieve detailed information about a specific room")
    public ResponseEntity<ApiResponse<RoomDto>> getRoomById(
            @PathVariable Long hotelId,
            @PathVariable Long roomId) {
        RoomDto room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * Check room availability.
     */
    @GetMapping("/rooms/{roomId}/availability")
    @Operation(summary = "Check room availability", description = "Check if a room is available for specific dates")
    public ResponseEntity<ApiResponse<Boolean>> checkRoomAvailability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        
        boolean available = roomService.isRoomAvailable(roomId, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.success(
                available ? "Room is available" : "Room is not available", available));
    }

    /**
     * Compare multiple rooms side-by-side.
     */
    @GetMapping("/{hotelId}/compare-rooms")
    @Operation(summary = "Compare rooms", description = "Get comparison data for selected rooms")
    public ResponseEntity<ApiResponse<RoomComparisonDto>> compareRooms(
            @PathVariable Long hotelId,
            @RequestParam String roomIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        
        List<Long> roomIdList = Arrays.stream(roomIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        RoomComparisonDto comparison = searchService.compareRooms(hotelId, roomIdList, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.success(comparison));
    }
}
