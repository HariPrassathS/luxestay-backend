package com.hotel.service;

import com.hotel.domain.dto.review.*;
import com.hotel.domain.entity.*;
import com.hotel.exception.BadRequestException;
import com.hotel.exception.ForbiddenException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing hotel reviews and feedback.
 */
@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewAuditLogRepository reviewAuditLogRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    @SuppressWarnings("unused") // Reserved for future user validation features
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewReplyRepository reviewReplyRepository,
                         ReviewAuditLogRepository reviewAuditLogRepository,
                         BookingRepository bookingRepository,
                         HotelRepository hotelRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewReplyRepository = reviewReplyRepository;
        this.reviewAuditLogRepository = reviewAuditLogRepository;
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    // ==================== User Operations ====================

    /**
     * Create a new review for a booking.
     * Only allowed if:
     * - User owns the booking
     * - Booking status is CHECKED_OUT
     * - No review exists for this booking
     */
    public ReviewDto createReview(CreateReviewRequest request) {
        User currentUser = getCurrentUser();

        // Get booking and verify ownership
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only review your own bookings");
        }

        // Verify booking is completed (CHECKED_OUT or past checkout date with CONFIRMED status)
        boolean isCheckedOut = booking.getStatus() == BookingStatus.CHECKED_OUT;
        boolean isPastConfirmed = booking.getStatus() == BookingStatus.CONFIRMED && 
                                  booking.getCheckOutDate().isBefore(java.time.LocalDate.now());
        
        if (!isCheckedOut && !isPastConfirmed) {
            throw new BadRequestException("You can only review after checkout");
        }

        // Check if review already exists
        if (reviewRepository.existsByBooking_Id(request.getBookingId())) {
            throw new BadRequestException("You have already reviewed this booking");
        }

        // Sanitize input against XSS
        String sanitizedComment = HtmlUtils.htmlEscape(request.getComment());
        String sanitizedTitle = request.getTitle() != null ? 
                HtmlUtils.htmlEscape(request.getTitle()) : null;

        // Create review
        Review review = Review.builder()
                .booking(booking)
                .hotel(booking.getRoom().getHotel())
                .user(currentUser)
                .rating(request.getRating())
                .title(sanitizedTitle)
                .comment(sanitizedComment)
                .status(ReviewStatus.PENDING)
                .isVerifiedStay(true)
                .build();

        review = reviewRepository.save(review);
        return mapToDto(review);
    }

    /**
     * Get user's own reviews.
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getMyReviews() {
        User currentUser = getCurrentUser();
        List<Review> reviews = reviewRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.getId());
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Check if user can review a booking.
     */
    @Transactional(readOnly = true)
    public boolean canReviewBooking(Long bookingId) {
        User currentUser = getCurrentUser();
        
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }

        // Must own the booking
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            return false;
        }

        // Must be checked out OR past confirmed/checked-in booking
        boolean isCheckedOut = booking.getStatus() == BookingStatus.CHECKED_OUT;
        boolean isPastConfirmed = booking.getStatus() == BookingStatus.CONFIRMED && 
                                  booking.getCheckOutDate().isBefore(java.time.LocalDate.now());
        boolean isPastCheckedIn = booking.getStatus() == BookingStatus.CHECKED_IN && 
                                  booking.getCheckOutDate().isBefore(java.time.LocalDate.now());
        if (!isCheckedOut && !isPastConfirmed && !isPastCheckedIn) {
            return false;
        }

        // Must not have existing review
        boolean hasReview = reviewRepository.existsByBooking_Id(bookingId);
        if (hasReview) {
            return false;
        }
        
        return true;
    }

    /**
     * Get review for a specific booking (user must own the booking).
     */
    @Transactional(readOnly = true)
    public ReviewDto getReviewForBooking(Long bookingId) {
        User currentUser = getCurrentUser();
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        // Must own the booking
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view reviews for your own bookings");
        }
        
        Review review = reviewRepository.findByBooking_Id(bookingId).orElse(null);
        return review != null ? mapToDto(review) : null;
    }

    /**
     * Update an existing review (only if status is PENDING).
     */
    @Transactional
    public ReviewDto updateReview(Long reviewId, CreateReviewRequest request) {
        User currentUser = getCurrentUser();
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Must own the review
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own reviews");
        }
        
        // Can only edit pending reviews
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Cannot edit a review that has already been " + 
                    review.getStatus().toString().toLowerCase());
        }
        
        // Update review fields
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setUpdatedAt(java.time.LocalDateTime.now());
        
        review = reviewRepository.save(review);
        return mapToDto(review);
    }

    // ==================== Public Operations ====================

    /**
     * Get approved reviews for a hotel (public view).
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getApprovedReviewsForHotel(Long hotelId) {
        // Verify hotel exists
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found");
        }
        
        List<Review> reviews = reviewRepository.findApprovedByHotelId(hotelId);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Get approved reviews with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getApprovedReviewsForHotel(Long hotelId, int page, int size) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found");
        }
        
        Page<Review> reviewPage = reviewRepository.findApprovedByHotelId(hotelId, PageRequest.of(page, size));
        return reviewPage.map(this::mapToDto);
    }

    /**
     * Get review statistics for a hotel.
     */
    @Transactional(readOnly = true)
    public HotelReviewStatsDto getHotelReviewStats(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Object[] rawStats = reviewRepository.getReviewStatsForHotel(hotelId);
        
        // Handle null or empty stats result
        if (rawStats == null || rawStats.length == 0) {
            return buildEmptyStats(hotelId, hotel.getName());
        }
        
        // The query returns a single row with aggregate values
        // If it's wrapped in another array, unwrap it
        Object[] stats = rawStats;
        if (rawStats[0] instanceof Object[]) {
            stats = (Object[]) rawStats[0];
        }
        
        // If no reviews exist, all values will be null or 0
        if (stats == null || stats.length == 0 || stats[0] == null) {
            return buildEmptyStats(hotelId, hotel.getName());
        }
        
        HotelReviewStatsDto dto = HotelReviewStatsDto.builder()
                .hotelId(hotelId)
                .hotelName(hotel.getName())
                .totalReviews(stats[0] != null ? ((Number) stats[0]).intValue() : 0)
                .averageRating(stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0)
                .fiveStarCount(stats.length > 2 && stats[2] != null ? ((Number) stats[2]).intValue() : 0)
                .fourStarCount(stats.length > 3 && stats[3] != null ? ((Number) stats[3]).intValue() : 0)
                .threeStarCount(stats.length > 4 && stats[4] != null ? ((Number) stats[4]).intValue() : 0)
                .twoStarCount(stats.length > 5 && stats[5] != null ? ((Number) stats[5]).intValue() : 0)
                .oneStarCount(stats.length > 6 && stats[6] != null ? ((Number) stats[6]).intValue() : 0)
                .build();
        
        dto.calculatePercentages();
        return dto;
    }
    
    private HotelReviewStatsDto buildEmptyStats(Long hotelId, String hotelName) {
        return HotelReviewStatsDto.builder()
                .hotelId(hotelId)
                .hotelName(hotelName)
                .totalReviews(0)
                .averageRating(0.0)
                .fiveStarCount(0)
                .fourStarCount(0)
                .threeStarCount(0)
                .twoStarCount(0)
                .oneStarCount(0)
                .fiveStarPercent(0.0)
                .fourStarPercent(0.0)
                .threeStarPercent(0.0)
                .twoStarPercent(0.0)
                .oneStarPercent(0.0)
                .build();
    }

    // ==================== Hotel Owner Operations ====================

    /**
     * Get all reviews for owner's hotel.
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getOwnerReviews() {
        User owner = getCurrentUser();
        validateHotelOwner(owner);

        List<Review> reviews = reviewRepository.findAllByHotelId(owner.getHotelId());
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Get owner reviews with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getOwnerReviews(Pageable pageable) {
        User owner = getCurrentUser();
        validateHotelOwner(owner);

        Page<Review> reviewPage = reviewRepository.findAllByHotelId(owner.getHotelId(), pageable);
        return reviewPage.map(this::mapToDto);
    }

    /**
     * Reply to a review (owner only).
     */
    public ReviewReplyDto replyToReview(Long reviewId, CreateReplyRequest request) {
        User owner = getCurrentUser();
        validateHotelOwner(owner);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Verify owner owns the hotel
        if (!review.getHotel().getId().equals(owner.getHotelId())) {
            throw new ForbiddenException("You can only reply to reviews for your hotel");
        }

        // Check if reply already exists
        if (reviewReplyRepository.existsByReview_Id(reviewId)) {
            throw new BadRequestException("You have already replied to this review");
        }

        // Only reply to approved reviews
        if (review.getStatus() != ReviewStatus.APPROVED) {
            throw new BadRequestException("You can only reply to approved reviews");
        }

        // Sanitize input
        String sanitizedReply = HtmlUtils.htmlEscape(request.getReplyText());

        ReviewReply reply = ReviewReply.builder()
                .review(review)
                .owner(owner)
                .replyText(sanitizedReply)
                .build();

        reply = reviewReplyRepository.save(reply);
        return mapReplyToDto(reply);
    }

    /**
     * Flag a review for admin attention.
     */
    public ReviewDto flagReview(Long reviewId, String reason) {
        User owner = getCurrentUser();
        validateHotelOwner(owner);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Verify owner owns the hotel
        if (!review.getHotel().getId().equals(owner.getHotelId())) {
            throw new ForbiddenException("You can only flag reviews for your hotel");
        }

        // Can only flag approved reviews
        if (review.getStatus() != ReviewStatus.APPROVED) {
            throw new BadRequestException("Only approved reviews can be flagged");
        }

        review.flag();
        review = reviewRepository.save(review);
        
        return mapToDto(review);
    }

    /**
     * Get reviews waiting for reply.
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsWithoutReply() {
        User owner = getCurrentUser();
        validateHotelOwner(owner);

        List<Review> reviews = reviewRepository.findWithoutReplyByHotelId(owner.getHotelId());
        return reviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.APPROVED)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ==================== Admin Operations ====================

    /**
     * Get all reviews with filters (admin).
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAdminReviews(Long hotelId, ReviewStatus status, Integer rating, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByFilters(hotelId, status, rating, pageable);
        return reviewPage.map(this::mapToDto);
    }

    /**
     * Get all pending reviews (admin).
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getPendingReviews() {
        List<Review> reviews = reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Get all flagged reviews (admin).
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getFlaggedReviews() {
        List<Review> reviews = reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.FLAGGED);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Update review status (admin).
     */
    public ReviewDto updateReviewStatus(Long reviewId, UpdateReviewStatusRequest request) {
        User admin = getCurrentUser();
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        String previousStatus = review.getStatus().name();

        // Validate rejection requires reason
        if (request.getStatus() == ReviewStatus.REJECTED && 
            (request.getReason() == null || request.getReason().isBlank())) {
            throw new BadRequestException("Rejection reason is required");
        }

        // Update status
        switch (request.getStatus()) {
            case APPROVED:
                review.approve();
                break;
            case REJECTED:
                review.reject(request.getReason());
                break;
            case PENDING:
                review.setStatus(ReviewStatus.PENDING);
                review.setRejectionReason(null);
                break;
            case FLAGGED:
                review.flag();
                break;
        }

        review = reviewRepository.save(review);

        // Create audit log
        createAuditLog(review, admin, 
                request.getStatus() == ReviewStatus.APPROVED ? ReviewAuditAction.APPROVE :
                request.getStatus() == ReviewStatus.REJECTED ? ReviewAuditAction.REJECT :
                request.getStatus() == ReviewStatus.FLAGGED ? ReviewAuditAction.FLAG : ReviewAuditAction.UNFLAG,
                previousStatus, request.getStatus().name(), request.getReason());

        return mapToDto(review);
    }

    /**
     * Edit review content (admin - for removing abusive content).
     */
    public ReviewDto editReviewContent(Long reviewId, String newComment, String reason) {
        User admin = getCurrentUser();
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        String sanitizedComment = HtmlUtils.htmlEscape(newComment);
        review.setComment(sanitizedComment);
        review = reviewRepository.save(review);

        // Create audit log
        createAuditLog(review, admin, ReviewAuditAction.EDIT,
                review.getStatus().name(), review.getStatus().name(), reason);

        return mapToDto(review);
    }

    /**
     * Delete a review (admin).
     */
    public void deleteReview(Long reviewId, String reason) {
        User admin = getCurrentUser();
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Create audit log before deletion
        createAuditLog(review, admin, ReviewAuditAction.DELETE,
                review.getStatus().name(), "DELETED", reason);

        reviewRepository.delete(review);
    }

    /**
     * Get review statistics for admin dashboard.
     */
    @Transactional(readOnly = true)
    public AdminReviewStatsDto getAdminReviewStats() {
        long totalReviews = reviewRepository.count();
        long pendingReviews = reviewRepository.countByStatus(ReviewStatus.PENDING);
        long approvedReviews = reviewRepository.countByStatus(ReviewStatus.APPROVED);
        long rejectedReviews = reviewRepository.countByStatus(ReviewStatus.REJECTED);
        long flaggedReviews = reviewRepository.countByStatus(ReviewStatus.FLAGGED);

        return AdminReviewStatsDto.builder()
                .totalReviews(totalReviews)
                .pendingReviews(pendingReviews)
                .approvedReviews(approvedReviews)
                .rejectedReviews(rejectedReviews)
                .flaggedReviews(flaggedReviews)
                .build();
    }

    // ==================== Helper Methods ====================

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    private void validateHotelOwner(User user) {
        if (!user.isHotelOwner()) {
            throw new ForbiddenException("Only hotel owners can access this resource");
        }
        if (user.getHotelId() == null) {
            throw new BadRequestException("Hotel owner has no assigned hotel");
        }
    }

    private void createAuditLog(Review review, User admin, ReviewAuditAction action,
                                String previousStatus, String newStatus, String reason) {
        ReviewAuditLog auditLog = ReviewAuditLog.builder()
                .review(review)
                .admin(admin)
                .action(action)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
        reviewAuditLogRepository.save(auditLog);
    }

    private ReviewDto mapToDto(Review review) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        
        ReviewDto dto = ReviewDto.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .bookingReference(review.getBooking().getBookingReference())
                .hotelId(review.getHotel().getId())
                .hotelName(review.getHotel().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .status(review.getStatus())
                .rejectionReason(review.getRejectionReason())
                .isVerifiedStay(review.getIsVerifiedStay())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();

        // Add stay dates
        if (review.getBooking() != null) {
            String stayDates = review.getBooking().getCheckInDate().format(dateFormatter) + 
                               " - " + review.getBooking().getCheckOutDate().format(dateFormatter);
            dto.setStayDates(stayDates);
            
            if (review.getBooking().getRoom() != null) {
                dto.setRoomName(review.getBooking().getRoom().getName());
            }
        }

        // Add reply if exists
        if (review.getReply() != null) {
            dto.setReply(mapReplyToDto(review.getReply()));
        }

        return dto;
    }

    private ReviewReplyDto mapReplyToDto(ReviewReply reply) {
        return ReviewReplyDto.builder()
                .id(reply.getId())
                .reviewId(reply.getReview().getId())
                .ownerId(reply.getOwner().getId())
                .ownerName(reply.getOwner().getFullName())
                .replyText(reply.getReplyText())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}
