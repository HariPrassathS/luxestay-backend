package com.hotel.repository;

import com.hotel.domain.entity.Review;
import com.hotel.domain.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Review entity operations.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Check if a review exists for a booking.
     */
    boolean existsByBooking_Id(Long bookingId);

    /**
     * Find review by booking ID.
     */
    java.util.Optional<Review> findByBooking_Id(Long bookingId);

    /**
     * Find all reviews by a user, ordered by creation date.
     */
    List<Review> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * Find approved reviews for a hotel.
     */
    @Query("SELECT r FROM Review r WHERE r.hotel.id = :hotelId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findApprovedByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Find approved reviews for a hotel with pagination.
     */
    @Query("SELECT r FROM Review r WHERE r.hotel.id = :hotelId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findApprovedByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    /**
     * Get review statistics for a hotel (approved reviews only).
     * Returns: [totalCount, avgRating, 5-star count, 4-star, 3-star, 2-star, 1-star]
     */
    @Query("SELECT COUNT(r), AVG(r.rating), " +
           "SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) " +
           "FROM Review r WHERE r.hotel.id = :hotelId AND r.status = 'APPROVED'")
    Object[] getReviewStatsForHotel(@Param("hotelId") Long hotelId);

    /**
     * Find all reviews for a hotel (all statuses - for owner/admin).
     */
    @Query("SELECT r FROM Review r WHERE r.hotel.id = :hotelId ORDER BY r.createdAt DESC")
    List<Review> findAllByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Find all reviews for a hotel with pagination.
     */
    @Query("SELECT r FROM Review r WHERE r.hotel.id = :hotelId ORDER BY r.createdAt DESC")
    Page<Review> findAllByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    /**
     * Find reviews for a hotel that don't have a reply.
     */
    @Query("SELECT r FROM Review r WHERE r.hotel.id = :hotelId AND r.reply IS NULL ORDER BY r.createdAt DESC")
    List<Review> findWithoutReplyByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Find reviews by status.
     */
    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    /**
     * Count reviews by status.
     */
    long countByStatus(ReviewStatus status);

    /**
     * Find reviews with filters (admin dashboard).
     */
    @Query("SELECT r FROM Review r WHERE " +
           "(:hotelId IS NULL OR r.hotel.id = :hotelId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:rating IS NULL OR r.rating = :rating) " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByFilters(@Param("hotelId") Long hotelId,
                               @Param("status") ReviewStatus status,
                               @Param("rating") Integer rating,
                               Pageable pageable);

    /**
     * Find reviews for a room.
     */
    @Query("SELECT r FROM Review r WHERE r.booking.room.id = :roomId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findApprovedByRoomId(@Param("roomId") Long roomId);

    /**
     * Get average rating for a hotel (approved reviews only).
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId AND r.status = 'APPROVED'")
    Double getAverageRatingForHotel(@Param("hotelId") Long hotelId);

    /**
     * Count approved reviews for a hotel.
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId AND r.status = 'APPROVED'")
    long countApprovedByHotelId(@Param("hotelId") Long hotelId);
}
