package com.hotel.service;

import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.booking.UpdateBookingStatusRequest;
import com.hotel.domain.dto.hotel.HotelDto;
import com.hotel.domain.dto.owner.*;
import com.hotel.domain.dto.room.CreateRoomRequest;
import com.hotel.domain.dto.room.RoomDto;
import com.hotel.domain.entity.*;
import com.hotel.exception.*;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.HotelMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for HOTEL_OWNER operations.
 * Provides hotel-scoped access with strict ownership validation.
 */
@Service
public class HotelOwnerService {

    private static final Logger logger = LoggerFactory.getLogger(HotelOwnerService.class);
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final HotelMapper hotelMapper;
    private final RoomMapper roomMapper;
    private final BookingMapper bookingMapper;

    public HotelOwnerService(UserRepository userRepository,
                             HotelRepository hotelRepository,
                             RoomRepository roomRepository,
                             BookingRepository bookingRepository,
                             PasswordEncoder passwordEncoder,
                             HotelMapper hotelMapper,
                             RoomMapper roomMapper,
                             BookingMapper bookingMapper) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.hotelMapper = hotelMapper;
        this.roomMapper = roomMapper;
        this.bookingMapper = bookingMapper;
    }

    // ==================== Authentication & Security ====================

    /**
     * Get the currently authenticated hotel owner.
     */
    public User getCurrentOwner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }

        User user = (User) auth.getPrincipal();
        if (!Role.HOTEL_OWNER.equals(user.getRole())) {
            throw new ForbiddenException("Access denied. HOTEL_OWNER role required.");
        }

        return user;
    }

    /**
     * Get the hotel ID for the current owner.
     */
    public Long getCurrentOwnerHotelId() {
        User owner = getCurrentOwner();
        if (owner.getHotelId() == null) {
            throw new BadRequestException("No hotel associated with this owner account");
        }
        return owner.getHotelId();
    }

    /**
     * Validate that the current owner has access to the specified hotel.
     */
    public void validateHotelAccess(Long hotelId) {
        Long ownerHotelId = getCurrentOwnerHotelId();
        if (!ownerHotelId.equals(hotelId)) {
            logger.warn("Hotel owner {} attempted to access hotel {} but owns hotel {}",
                    getCurrentOwner().getEmail(), hotelId, ownerHotelId);
            throw new ForbiddenException("You do not have access to this hotel");
        }
    }

    /**
     * Validate that a room belongs to the owner's hotel.
     */
    public void validateRoomAccess(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        validateHotelAccess(room.getHotel().getId());
    }

    /**
     * Validate that a booking belongs to the owner's hotel.
     */
    public void validateBookingAccess(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        validateHotelAccess(booking.getRoom().getHotel().getId());
    }

    // ==================== Dashboard ====================

    /**
     * Get owner's dashboard with hotel info, stats, and recent activity.
     */
    @Transactional(readOnly = true)
    public OwnerDashboardDto getDashboard() {
        Long hotelId = getCurrentOwnerHotelId();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        LocalDate today = LocalDate.now();

        // Get stats
        OwnerStatsDto stats = getStats();

        // Get today's activity
        List<BookingDto> todayCheckIns = bookingRepository.findTodayCheckInsByHotel(today, hotelId)
                .stream().map(bookingMapper::toDto).collect(Collectors.toList());
        List<BookingDto> todayCheckOuts = bookingRepository.findTodayCheckOutsByHotel(today, hotelId)
                .stream().map(bookingMapper::toDto).collect(Collectors.toList());

        // Get recent bookings
        Page<Booking> recentBookingsPage = bookingRepository.findByHotelId(hotelId, PageRequest.of(0, 10));
        List<BookingDto> recentBookings = recentBookingsPage.getContent()
                .stream().map(bookingMapper::toDto).collect(Collectors.toList());

        // Room availability
        List<Room> allRooms = roomRepository.findByHotel_Id(hotelId);
        int totalRooms = allRooms.size();
        int availableRooms = (int) allRooms.stream().filter(Room::getIsAvailable).count();

        // Calculate revenue
        List<BookingStatus> revenueStatuses = Arrays.asList(
                BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT);
        BigDecimal monthRevenue = bookingRepository.sumTotalPriceByHotelIdAndStatusIn(hotelId, revenueStatuses);

        return OwnerDashboardDto.builder()
                .hotel(hotelMapper.toDto(hotel))
                .stats(stats)
                .todayCheckIns(todayCheckIns)
                .todayCheckOuts(todayCheckOuts)
                .recentBookings(recentBookings)
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .occupiedRooms(totalRooms - availableRooms)
                .monthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO)
                .build();
    }

    /**
     * Get detailed statistics for the owner's hotel.
     */
    @Transactional(readOnly = true)
    public OwnerStatsDto getStats() {
        Long hotelId = getCurrentOwnerHotelId();
        LocalDate today = LocalDate.now();

        // Booking counts by status
        long totalBookings = bookingRepository.countByHotelId(hotelId);
        long confirmedBookings = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.CONFIRMED);
        long pendingBookings = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.PENDING);
        long cancelledBookings = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.CANCELLED);
        long completedBookings = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.CHECKED_OUT);

        // Today's activity
        long todayCheckIns = bookingRepository.countByHotelIdAndCheckInDate(hotelId, today);
        long todayCheckOuts = bookingRepository.countByHotelIdAndCheckOutDate(hotelId, today);

        // Room stats
        List<Room> allRooms = roomRepository.findByHotel_Id(hotelId);
        int totalRooms = allRooms.size();
        int availableRooms = (int) allRooms.stream().filter(Room::getIsAvailable).count();
        double occupancyRate = totalRooms > 0 ? ((double) (totalRooms - availableRooms) / totalRooms) * 100 : 0;

        // Revenue
        List<BookingStatus> revenueStatuses = Arrays.asList(
                BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT);
        BigDecimal totalRevenue = bookingRepository.sumTotalPriceByHotelIdAndStatusIn(hotelId, revenueStatuses);

        return OwnerStatsDto.builder()
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .pendingBookings(pendingBookings)
                .cancelledBookings(cancelledBookings)
                .completedBookings(completedBookings)
                .todayCheckIns(todayCheckIns)
                .todayCheckOuts(todayCheckOuts)
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .occupancyRate(Math.round(occupancyRate * 100.0) / 100.0)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .build();
    }

    // ==================== Hotel Management ====================

    /**
     * Get the owner's hotel details.
     */
    @Transactional(readOnly = true)
    public HotelDto getMyHotel() {
        Long hotelId = getCurrentOwnerHotelId();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        return hotelMapper.toDto(hotel);
    }

    /**
     * Update the owner's hotel (limited fields).
     */
    @Transactional
    public HotelDto updateMyHotel(OwnerHotelUpdateRequest request) {
        Long hotelId = getCurrentOwnerHotelId();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // Only update allowed fields
        if (request.getDescription() != null) {
            hotel.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            hotel.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            hotel.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            hotel.setEmail(request.getEmail());
        }
        if (request.getHeroImageUrl() != null) {
            hotel.setHeroImageUrl(request.getHeroImageUrl());
        }
        if (request.getAmenities() != null) {
            hotel.setAmenities(request.getAmenities());
        }
        if (request.getCheckInTime() != null) {
            hotel.setCheckInTime(request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            hotel.setCheckOutTime(request.getCheckOutTime());
        }

        hotel = hotelRepository.save(hotel);
        logger.info("Hotel {} updated by owner {}", hotelId, getCurrentOwner().getEmail());

        return hotelMapper.toDto(hotel);
    }

    // ==================== Room Management ====================

    /**
     * Get all rooms for the owner's hotel.
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getMyRooms() {
        Long hotelId = getCurrentOwnerHotelId();
        return roomRepository.findByHotel_Id(hotelId)
                .stream().map(roomMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get a specific room (with ownership validation).
     */
    @Transactional(readOnly = true)
    public RoomDto getRoom(Long roomId) {
        validateRoomAccess(roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return roomMapper.toDto(room);
    }

    /**
     * Create a new room in the owner's hotel.
     */
    @Transactional
    public RoomDto createRoom(CreateRoomRequest request) {
        Long hotelId = getCurrentOwnerHotelId();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // Convert amenities list to JSON string
        String amenitiesJson = null;
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            amenitiesJson = "[\"" + String.join("\",\"", request.getAmenities()) + "\"]";
        }

        Room room = Room.builder()
                .hotel(hotel)
                .roomType(request.getRoomType())
                .roomNumber(request.getRoomNumber())
                .name(request.getName() != null ? request.getName() : request.getRoomType().name() + " Room")
                .description(request.getDescription())
                .pricePerNight(request.getPricePerNight())
                .capacity(request.getCapacity())
                .bedType(request.getBedType())
                .sizeSqm(request.getSizeSqm())
                .amenities(amenitiesJson)
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        room = roomRepository.save(room);
        logger.info("Room {} created in hotel {} by owner {}", room.getId(), hotelId, getCurrentOwner().getEmail());

        return roomMapper.toDto(room);
    }

    /**
     * Update a room (with ownership validation).
     */
    @Transactional
    public RoomDto updateRoom(Long roomId, CreateRoomRequest request) {
        validateRoomAccess(roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());
        if (request.getRoomNumber() != null) room.setRoomNumber(request.getRoomNumber());
        if (request.getName() != null) room.setName(request.getName());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getPricePerNight() != null) room.setPricePerNight(request.getPricePerNight());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getBedType() != null) room.setBedType(request.getBedType());
        if (request.getSizeSqm() != null) room.setSizeSqm(request.getSizeSqm());
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            String amenitiesJson = "[\"" + String.join("\",\"", request.getAmenities()) + "\"]";
            room.setAmenities(amenitiesJson);
        }
        if (request.getImageUrl() != null) room.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) room.setIsAvailable(request.getIsAvailable());

        room = roomRepository.save(room);
        logger.info("Room {} updated by owner {}", roomId, getCurrentOwner().getEmail());

        return roomMapper.toDto(room);
    }

    /**
     * Toggle room availability (soft delete/enable).
     */
    @Transactional
    public RoomDto toggleRoomAvailability(Long roomId) {
        validateRoomAccess(roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setIsAvailable(!room.getIsAvailable());
        room = roomRepository.save(room);
        logger.info("Room {} availability toggled to {} by owner {}",
                roomId, room.getIsAvailable(), getCurrentOwner().getEmail());

        return roomMapper.toDto(room);
    }

    // ==================== Booking Management ====================

    /**
     * Get all bookings for the owner's hotel.
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getMyBookings() {
        Long hotelId = getCurrentOwnerHotelId();
        return bookingRepository.findByHotelId(hotelId)
                .stream().map(bookingMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get bookings with pagination.
     */
    @Transactional(readOnly = true)
    public Page<BookingDto> getMyBookings(Pageable pageable) {
        Long hotelId = getCurrentOwnerHotelId();
        return bookingRepository.findByHotelId(hotelId, pageable)
                .map(bookingMapper::toDto);
    }

    /**
     * Get a specific booking (with ownership validation).
     */
    @Transactional(readOnly = true)
    public BookingDto getBooking(Long bookingId) {
        validateBookingAccess(bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return bookingMapper.toDto(booking);
    }

    /**
     * Update booking status (check-in/check-out only for owners).
     * Owners cannot forcibly cancel user bookings.
     */
    @Transactional
    public BookingDto updateBookingStatus(Long bookingId, UpdateBookingStatusRequest request) {
        validateBookingAccess(bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        BookingStatus newStatus = request.getStatus();

        // Owners can only update to CHECK_IN or CHECK_OUT status
        // They cannot cancel bookings - that's a user or admin action
        if (newStatus == BookingStatus.CANCELLED) {
            throw new ForbiddenException("Hotel owners cannot cancel bookings. " +
                    "Please contact the guest or admin for cancellation.");
        }

        // Validate status transitions
        BookingStatus currentStatus = booking.getStatus();
        boolean validTransition = switch (newStatus) {
            case CHECKED_IN -> currentStatus == BookingStatus.CONFIRMED;
            case CHECKED_OUT -> currentStatus == BookingStatus.CHECKED_IN;
            case CONFIRMED -> currentStatus == BookingStatus.PENDING;
            default -> false;
        };

        if (!validTransition) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        booking.setStatus(newStatus);
        booking = bookingRepository.save(booking);
        logger.info("Booking {} status updated to {} by owner {}",
                bookingId, newStatus, getCurrentOwner().getEmail());

        return bookingMapper.toDto(booking);
    }

    /**
     * Get today's check-ins for the owner's hotel.
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getTodayCheckIns() {
        Long hotelId = getCurrentOwnerHotelId();
        return bookingRepository.findTodayCheckInsByHotel(LocalDate.now(), hotelId)
                .stream().map(bookingMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get today's check-outs for the owner's hotel.
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getTodayCheckOuts() {
        Long hotelId = getCurrentOwnerHotelId();
        return bookingRepository.findTodayCheckOutsByHotel(LocalDate.now(), hotelId)
                .stream().map(bookingMapper::toDto).collect(Collectors.toList());
    }

    // ==================== Password Management ====================

    /**
     * Change password (used for forced password change and voluntary updates).
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User owner = getCurrentOwner();

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), owner.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        // Validate new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), owner.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Update password
        owner.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        owner.setMustChangePassword(false);
        owner.setPasswordChangedAt(LocalDateTime.now());

        userRepository.save(owner);
        logger.info("Password changed for hotel owner: {}", owner.getEmail());
    }

    // ==================== Hotel Registration (Public) ====================

    /**
     * Register a new hotel with owner account.
     * Hotel is created in PENDING status for admin approval.
     */
    @Transactional
    public HotelDto registerHotel(HotelRegistrationRequest request) {
        // Check if owner email already exists
        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new ConflictException("Email address is already registered");
        }

        // Create hotel in PENDING status
        Hotel hotel = Hotel.builder()
                .name(request.getHotelName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .postalCode(request.getPostalCode())
                .phone(request.getHotelPhone())
                .email(request.getHotelEmail())
                .starRating(request.getStarRating() != null ? request.getStarRating() : 3)
                .heroImageUrl(request.getHeroImageUrl())
                .amenities(request.getAmenities())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(false) // Inactive until approved
                .approvalStatus(Hotel.ApprovalStatus.PENDING)
                .build();

        hotel = hotelRepository.save(hotel);
        logger.info("New hotel registered: {} (pending approval)", hotel.getName());

        // Create hotel owner account
        User owner = User.builder()
                .email(request.getOwnerEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getOwnerPhone())
                .role(Role.HOTEL_OWNER)
                .hotelId(hotel.getId())
                .isActive(false) // Inactive until hotel is approved
                .mustChangePassword(false)
                .build();

        userRepository.save(owner);
        logger.info("Hotel owner account created: {} for hotel {}", owner.getEmail(), hotel.getName());

        return hotelMapper.toDto(hotel);
    }

    // ==================== Admin Operations ====================

    /**
     * Create a hotel owner account for an existing hotel (admin only).
     */
    @Transactional
    public HotelOwnerDto createHotelOwnerAccount(CreateHotelOwnerRequest request) {
        // Validate hotel exists
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + request.getHotelId()));

        // Check if hotel already has an owner
        if (userRepository.existsByHotelId(request.getHotelId())) {
            throw new ConflictException("Hotel already has an owner account");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email address is already registered");
        }

        // Generate or use provided password
        String password;
        boolean mustChange;
        if (Boolean.TRUE.equals(request.getGenerateTemporaryPassword())) {
            password = generateTemporaryPassword();
            mustChange = true;
        } else {
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new BadRequestException("Password is required when not generating temporary password");
            }
            password = request.getPassword();
            mustChange = false;
        }

        // Create owner account
        User owner = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(password))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .role(Role.HOTEL_OWNER)
                .hotelId(hotel.getId())
                .isActive(true)
                .mustChangePassword(mustChange)
                .build();

        owner = userRepository.save(owner);
        logger.info("Hotel owner account created by admin: {} for hotel {}",
                owner.getEmail(), hotel.getName());

        HotelOwnerDto dto = HotelOwnerDto.builder()
                .id(owner.getId())
                .email(owner.getEmail())
                .firstName(owner.getFirstName())
                .lastName(owner.getLastName())
                .phone(owner.getPhone())
                .role(owner.getRole())
                .isActive(owner.getIsActive())
                .mustChangePassword(owner.getMustChangePassword())
                .createdAt(owner.getCreatedAt())
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelCity(hotel.getCity())
                .hotelApprovalStatus(hotel.getApprovalStatus().name())
                .hotelIsActive(hotel.getIsActive())
                .build();

        // If temporary password was generated, include it in logs (in production, send email)
        if (mustChange) {
            logger.info("Temporary password for {}: {}", owner.getEmail(), password);
            // TODO: Send email with temporary password
        }

        return dto;
    }

    /**
     * Get all hotel owners (admin only).
     */
    @Transactional(readOnly = true)
    public List<HotelOwnerDto> getAllHotelOwners() {
        return userRepository.findHotelOwnersWithHotels(Role.HOTEL_OWNER)
                .stream()
                .map(owner -> {
                    HotelOwnerDto.HotelOwnerDtoBuilder builder = HotelOwnerDto.builder()
                            .id(owner.getId())
                            .email(owner.getEmail())
                            .firstName(owner.getFirstName())
                            .lastName(owner.getLastName())
                            .phone(owner.getPhone())
                            .role(owner.getRole())
                            .isActive(owner.getIsActive())
                            .mustChangePassword(owner.getMustChangePassword())
                            .createdAt(owner.getCreatedAt())
                            .hotelId(owner.getHotelId());

                    if (owner.getManagedHotel() != null) {
                        Hotel hotel = owner.getManagedHotel();
                        builder.hotelName(hotel.getName())
                                .hotelCity(hotel.getCity())
                                .hotelApprovalStatus(hotel.getApprovalStatus().name())
                                .hotelIsActive(hotel.getIsActive());
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Approve or reject a hotel registration (admin only).
     */
    @Transactional
    public HotelDto approveHotel(Long hotelId, HotelApprovalRequest request, Long adminId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + hotelId));

        if (hotel.getApprovalStatus() != Hotel.ApprovalStatus.PENDING) {
            throw new BadRequestException("Hotel is not pending approval");
        }

        final String hotelName = hotel.getName(); // Capture for lambda

        if (Boolean.TRUE.equals(request.getApproved())) {
            hotel.setApprovalStatus(Hotel.ApprovalStatus.APPROVED);
            hotel.setIsActive(true);
            hotel.setApprovedAt(LocalDateTime.now());
            hotel.setApprovedBy(adminId);

            // Activate the owner account
            userRepository.findByHotelId(hotelId).ifPresent(owner -> {
                owner.setIsActive(true);
                userRepository.save(owner);
                logger.info("Hotel owner {} activated for approved hotel {}", owner.getEmail(), hotelName);
            });

            logger.info("Hotel {} approved by admin {}", hotelName, adminId);
        } else {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new BadRequestException("Rejection reason is required");
            }
            hotel.setApprovalStatus(Hotel.ApprovalStatus.REJECTED);
            hotel.setRejectionReason(request.getRejectionReason());
            hotel.setApprovedAt(LocalDateTime.now());
            hotel.setApprovedBy(adminId);

            logger.info("Hotel {} rejected by admin {}: {}", hotelName, adminId, request.getRejectionReason());
        }

        hotel = hotelRepository.save(hotel);
        return hotelMapper.toDto(hotel);
    }

    /**
     * Get hotels pending approval (admin only).
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getPendingHotels() {
        // This would need a new repository method
        return hotelRepository.findAll().stream()
                .filter(h -> h.getApprovalStatus() == Hotel.ApprovalStatus.PENDING)
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Reset hotel owner password (admin only).
     * Generates a new temporary password and forces password change.
     */
    @Transactional
    public String resetOwnerPassword(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerId));

        if (!Role.HOTEL_OWNER.equals(owner.getRole())) {
            throw new BadRequestException("User is not a hotel owner");
        }

        String tempPassword = generateTemporaryPassword();
        owner.setPasswordHash(passwordEncoder.encode(tempPassword));
        owner.setMustChangePassword(true);
        owner.setPasswordChangedAt(LocalDateTime.now());

        userRepository.save(owner);
        logger.info("Password reset for hotel owner: {}", owner.getEmail());

        // In production, send email instead of returning password
        return tempPassword;
    }

    /**
     * Toggle hotel owner account status (admin only).
     */
    @Transactional
    public HotelOwnerDto toggleOwnerStatus(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerId));

        if (!Role.HOTEL_OWNER.equals(owner.getRole())) {
            throw new BadRequestException("User is not a hotel owner");
        }

        owner.setIsActive(!owner.getIsActive());
        owner = userRepository.save(owner);
        logger.info("Hotel owner {} status toggled to {}", owner.getEmail(), owner.getIsActive());

        Hotel hotel = owner.getHotelId() != null ?
                hotelRepository.findById(owner.getHotelId()).orElse(null) : null;

        return HotelOwnerDto.builder()
                .id(owner.getId())
                .email(owner.getEmail())
                .firstName(owner.getFirstName())
                .lastName(owner.getLastName())
                .phone(owner.getPhone())
                .role(owner.getRole())
                .isActive(owner.getIsActive())
                .mustChangePassword(owner.getMustChangePassword())
                .createdAt(owner.getCreatedAt())
                .hotelId(owner.getHotelId())
                .hotelName(hotel != null ? hotel.getName() : null)
                .hotelCity(hotel != null ? hotel.getCity() : null)
                .hotelApprovalStatus(hotel != null ? hotel.getApprovalStatus().name() : null)
                .hotelIsActive(hotel != null ? hotel.getIsActive() : null)
                .build();
    }

    // ==================== Utility Methods ====================

    /**
     * Generate a secure temporary password.
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
