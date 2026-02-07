package com.hotel.service;

import com.hotel.domain.dto.admin.AdminStatsDto;
import com.hotel.domain.dto.auth.ChangePasswordRequest;
import com.hotel.domain.dto.auth.SetPasswordRequest;
import com.hotel.domain.dto.user.UpdateUserRequest;
import com.hotel.domain.dto.user.UpdateUserRoleRequest;
import com.hotel.domain.dto.user.UserDto;
import com.hotel.domain.entity.BookingStatus;
import com.hotel.domain.entity.User;
import com.hotel.exception.BadRequestException;
import com.hotel.exception.ForbiddenException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.mapper.UserMapper;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service handling user operations (Admin and User profile).
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final LoginAuditService loginAuditService;

    public UserService(UserRepository userRepository,
                       HotelRepository hotelRepository,
                       RoomRepository roomRepository,
                       BookingRepository bookingRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       LoginAuditService loginAuditService) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.loginAuditService = loginAuditService;
    }

    /**
     * Get all users with pagination (Admin only).
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(int page, int size) {
        logger.debug("Fetching all users, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).map(user -> {
            UserDto dto = userMapper.toDto(user);
            dto.setBookingsCount(bookingRepository.countByUser_Id(user.getId()));
            return dto;
        });
    }

    /**
     * Get user by ID (Admin only).
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        logger.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        UserDto dto = userMapper.toDto(user);
        dto.setBookingsCount(bookingRepository.countByUser_Id(user.getId()));
        return dto;
    }

    /**
     * Update user role (Admin only).
     */
    @Transactional
    public UserDto updateUserRole(Long userId, UpdateUserRoleRequest request) {
        logger.info("Updating user {} role to {}", userId, request.getRole());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setRole(request.getRole());
        user = userRepository.save(user);
        
        logger.info("User {} role updated to {}", userId, request.getRole());
        
        UserDto dto = userMapper.toDto(user);
        dto.setBookingsCount(bookingRepository.countByUser_Id(user.getId()));
        return dto;
    }

    /**
     * Toggle user active status (Admin only).
     */
    @Transactional
    public UserDto toggleUserStatus(Long userId) {
        logger.info("Toggling active status for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsActive(!user.getIsActive());
        user = userRepository.save(user);
        
        UserDto dto = userMapper.toDto(user);
        dto.setBookingsCount(bookingRepository.countByUser_Id(user.getId()));
        return dto;
    }

    /**
     * Get admin dashboard statistics.
     */
    @Transactional(readOnly = true)
    public AdminStatsDto getAdminStats() {
        logger.debug("Fetching admin dashboard stats");
        
        LocalDate today = LocalDate.now();
        
        long totalHotels = hotelRepository.count();
        long totalRooms = roomRepository.count();
        long totalBookings = bookingRepository.count();
        long totalUsers = userRepository.count();
        
        // Count bookings by status
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        
        // Today's check-ins and check-outs
        long checkedInToday = bookingRepository.countByCheckInDate(today);
        long checkingOutToday = bookingRepository.countByCheckOutDate(today);
        
        // Calculate total revenue from confirmed/completed bookings
        BigDecimal totalRevenue = bookingRepository.sumTotalPriceByStatusIn(
                List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT)
        );
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        return AdminStatsDto.builder()
                .totalHotels(totalHotels)
                .totalRooms(totalRooms)
                .totalBookings(totalBookings)
                .totalUsers(totalUsers)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .checkedInToday(checkedInToday)
                .checkingOutToday(checkingOutToday)
                .totalRevenue(totalRevenue)
                .build();
    }

    /**
     * Update current user's profile.
     */
    @Transactional
    public UserDto updateProfile(Long userId, UpdateUserRequest request) {
        logger.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        user = userRepository.save(user);
        logger.info("Profile updated for user: {}", userId);
        
        UserDto dto = userMapper.toDto(user);
        dto.setBookingsCount(bookingRepository.countByUser_Id(user.getId()));
        return dto;
    }

    /**
     * Set password for first-time login users.
     * This is for users with mustChangePassword = true.
     */
    @Transactional
    public UserDto setPassword(Long userId, SetPasswordRequest request, String ipAddress) {
        logger.info("Setting password for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify user must change password
        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new ForbiddenException("Password change not required. Use change-password endpoint instead.");
        }
        
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(LocalDateTime.now());
        
        user = userRepository.save(user);
        logger.info("Password set successfully for user: {}", userId);
        
        // Send confirmation email (async)
        emailService.sendPasswordChangedEmail(user, ipAddress);
        
        // Record audit log (async)
        loginAuditService.recordPasswordChanged(user, ipAddress);
        
        UserDto dto = userMapper.toDto(user);
        dto.setBookingsCount(bookingRepository.countByUser_Id(user.getId()));
        return dto;
    }

    /**
     * Change password for existing users (requires current password).
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, String ipAddress) {
        logger.info("Changing password for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Validate new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New passwords do not match");
        }
        
        // Validate new password differs from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(LocalDateTime.now());
        
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", userId);
        
        // Send confirmation email (async)
        emailService.sendPasswordChangedEmail(user, ipAddress);
        
        // Record audit log (async)
        loginAuditService.recordPasswordChanged(user, ipAddress);
    }
}
