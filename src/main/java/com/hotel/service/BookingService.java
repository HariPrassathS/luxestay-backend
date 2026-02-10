package com.hotel.service;

import com.hotel.annotation.RateLimited;
import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.booking.CreateBookingRequest;
import com.hotel.domain.dto.booking.UpdateBookingStatusRequest;
import com.hotel.domain.dto.realtime.BookingNotification;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.BookingStatus;
import com.hotel.domain.entity.Room;
import com.hotel.domain.entity.User;
import com.hotel.exception.BadRequestException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.exception.ForbiddenException;
import com.hotel.mapper.BookingMapper;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for managing bookings.
 * 
 * CONCURRENCY HANDLING:
 * - Uses pessimistic locking (findOverlappingBookingsWithLock) to prevent double-booking
 * - Lock acquired at availability check, held until transaction commits
 * - 3-second lock timeout prevents indefinite waits
 * - Real-time updates broadcast via WebSocket after successful booking
 */
@Service
@Transactional
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final LoyaltyService loyaltyService;
    private final RealTimeAvailabilityService realTimeAvailabilityService;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository,
                          BookingMapper bookingMapper,
                          LoyaltyService loyaltyService,
                          RealTimeAvailabilityService realTimeAvailabilityService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
        this.loyaltyService = loyaltyService;
        this.realTimeAvailabilityService = realTimeAvailabilityService;
    }

    /**
     * Create a new booking for the authenticated user.
     * Uses pessimistic locking to prevent double-booking race conditions.
     */
    @RateLimited(key = "booking", limit = 10, windowSeconds = 3600, 
                 message = "Too many booking attempts. Please wait before trying again.")
    public BookingDto createBooking(CreateBookingRequest request) {
        // 1. Validate dates
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        // 2. Get current authenticated user
        User user = getCurrentUser();

        // 3. Get room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        // 4. Validate capacity
        if (request.getNumGuests() > room.getCapacity()) {
            throw new BadRequestException("Number of guests exceeds room maximum capacity of " + room.getCapacity());
        }

        // 5. Check availability with PESSIMISTIC LOCK to prevent double-booking
        // This acquires an exclusive lock on existing bookings for this room+dates,
        // blocking any concurrent booking attempts until this transaction completes.
        List<Booking> overlapping = bookingRepository.findOverlappingBookingsWithLock(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            log.warn("Double-booking prevented: Room {} has overlapping booking for {} to {}",
                    request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }

        // 6. Build booking entity
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numGuests(request.getNumGuests())
                .pricePerNight(room.getPricePerNight())
                .status(BookingStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();
        
        // Calculate derived fields
        booking.calculateTotalNights();
        booking.calculateTotalPrice();

        // 7. Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // 8. Broadcast real-time availability update (async, won't block response)
        realTimeAvailabilityService.broadcastBookingCreated(savedBooking);
        
        // 9. Send user notification
        realTimeAvailabilityService.sendBookingNotification(
            user.getId(),
            BookingNotification.builder()
                .type(BookingNotification.NotificationType.BOOKING_CREATED)
                .bookingId(savedBooking.getId())
                .bookingReference(savedBooking.getBookingReference())
                .hotelId(room.getHotel().getId())
                .hotelName(room.getHotel().getName())
                .roomName(room.getName())
                .checkInDate(savedBooking.getCheckInDate())
                .checkOutDate(savedBooking.getCheckOutDate())
                .status(savedBooking.getStatus())
                .totalPrice(savedBooking.getTotalPrice())
                .message("Your booking has been created successfully!")
                .timestamp(System.currentTimeMillis())
                .build()
        );
        
        log.info("Booking created: {} for room {} by user {}", 
                savedBooking.getBookingReference(), room.getId(), user.getId());
        
        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Create booking for a specific user (used by group booking system).
     * This method bypasses the security context and creates a booking for the specified user.
     */
    @Transactional
    public BookingDto createBookingForUser(CreateBookingRequest request, Long userId) {
        // Validate dates
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        // Get specified user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Get room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        // Validate capacity
        if (request.getNumGuests() > room.getCapacity()) {
            throw new BadRequestException("Number of guests exceeds room maximum capacity of " + room.getCapacity());
        }

        // Check availability with PESSIMISTIC LOCK
        List<Booking> overlapping = bookingRepository.findOverlappingBookingsWithLock(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            log.warn("Double-booking prevented: Room {} has overlapping booking for {} to {}",
                    request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }

        // Build booking entity
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numGuests(request.getNumGuests())
                .pricePerNight(room.getPricePerNight())
                .status(BookingStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();
        
        booking.calculateTotalNights();
        booking.calculateTotalPrice();

        Booking savedBooking = bookingRepository.save(booking);
        
        log.info("Group booking created: {} for room {} for user {}", 
                savedBooking.getBookingReference(), room.getId(), user.getId());
        
        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Get all bookings for the authenticated user (sorted recent first).
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming bookings for the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getMyUpcomingBookings() {
        User user = getCurrentUser();
        return bookingRepository.findUpcomingBookings(user.getId(), LocalDate.now())
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get past bookings for the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getMyPastBookings() {
        User user = getCurrentUser();
        return bookingRepository.findPastBookings(user.getId(), LocalDate.now())
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific booking by ID.
     * Enforces that checks that the booking belongs to the current user.
     */
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id) {
        User user = getCurrentUser();
        
        // Direct security enforcement in repository lookup
        Booking booking = bookingRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
                
        return bookingMapper.toDto(booking);
    }

    /**
     * Get a booking by reference code.
     * Enforces ownership check.
     */
    @Transactional(readOnly = true)
    public BookingDto getBookingByReference(String reference) {
        User user = getCurrentUser();
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", reference));
        
        // Security Check
        if (!booking.getUser().getId().equals(user.getId())) {
             throw new ForbiddenException("You are not authorized to view this booking");
        }
        
        return bookingMapper.toDto(booking);
    }

    /**
     * Cancel a booking.
     */
    public BookingDto cancelBooking(Long id, String reason) {
        User user = getCurrentUser();
        
        Booking booking = bookingRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (!booking.isCancellable()) {
            throw new BadRequestException("Booking cannot be cancelled in its current status: " + booking.getStatus());
        }

        booking.cancel(reason);
        Booking savedBooking = bookingRepository.save(booking);
        
        // Broadcast that the room is now available
        realTimeAvailabilityService.broadcastBookingCancelled(savedBooking);
        
        // Notify user
        realTimeAvailabilityService.sendBookingNotification(
            user.getId(),
            BookingNotification.builder()
                .type(BookingNotification.NotificationType.BOOKING_CANCELLED)
                .bookingId(savedBooking.getId())
                .bookingReference(savedBooking.getBookingReference())
                .hotelId(savedBooking.getRoom().getHotel().getId())
                .hotelName(savedBooking.getRoom().getHotel().getName())
                .roomName(savedBooking.getRoom().getName())
                .checkInDate(savedBooking.getCheckInDate())
                .checkOutDate(savedBooking.getCheckOutDate())
                .status(savedBooking.getStatus())
                .message("Your booking has been cancelled.")
                .timestamp(System.currentTimeMillis())
                .build()
        );
        
        log.info("Booking cancelled: {} by user {}", savedBooking.getBookingReference(), user.getId());
        
        return bookingMapper.toDto(savedBooking);
    }

    // ==================== Admin Methods ====================

    @Transactional(readOnly = true)
    public Page<BookingDto> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(bookingMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> searchBookings(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.searchBookings(query, pageable)
                .map(bookingMapper::toDto);
    }

    public BookingDto updateBookingStatus(Long id, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        BookingStatus previousStatus = booking.getStatus();
        
        // Logic for status change
        booking.setStatus(request.getStatus());
        
        // If cancelled, set reason if provided
        if (request.getStatus() == BookingStatus.CANCELLED && request.getReason() != null) {
            booking.setCancellationReason(request.getReason());
            booking.setCancelledAt(java.time.LocalDateTime.now());
        }

        Booking savedBooking = bookingRepository.save(booking);
        
        // Award XP when booking is marked as CHECKED_OUT (completed stay)
        if (request.getStatus() == BookingStatus.CHECKED_OUT && previousStatus != BookingStatus.CHECKED_OUT) {
            try {
                loyaltyService.processBookingXp(savedBooking);
            } catch (Exception e) {
                // Log error but don't fail the status update
                log.error("Failed to process loyalty XP for booking {}: {}", id, e.getMessage());
            }
        }
        
        return bookingMapper.toDto(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByHotel(Long hotelId) {
        return bookingRepository.findByHotelId(hotelId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getTodayCheckIns() {
        return bookingRepository.findTodayCheckIns(LocalDate.now())
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getTodayCheckOuts() {
        return bookingRepository.findTodayCheckOuts(LocalDate.now())
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private String generateBookingReference() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
