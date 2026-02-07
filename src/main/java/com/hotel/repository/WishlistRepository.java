package com.hotel.repository;

import com.hotel.domain.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Wishlist entity operations.
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Find all wishlist items for a user, ordered by most recent first.
     */
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Check if a hotel is in user's wishlist.
     */
    boolean existsByUserIdAndHotelId(Long userId, Long hotelId);

    /**
     * Find a specific wishlist item.
     */
    Optional<Wishlist> findByUserIdAndHotelId(Long userId, Long hotelId);

    /**
     * Delete by user and hotel.
     */
    void deleteByUserIdAndHotelId(Long userId, Long hotelId);

    /**
     * Count wishlisted hotels for a user.
     */
    long countByUserId(Long userId);

    /**
     * Get list of hotel IDs in user's wishlist (for bulk checking).
     */
    @Query("SELECT w.hotel.id FROM Wishlist w WHERE w.user.id = :userId")
    List<Long> findHotelIdsByUserId(@Param("userId") Long userId);
}
