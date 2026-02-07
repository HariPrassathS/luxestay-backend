package com.hotel.repository;

import com.hotel.domain.entity.VirtualTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Virtual Tour operations.
 */
@Repository
public interface VirtualTourRepository extends JpaRepository<VirtualTour, Long> {
    
    /**
     * Find all active tours for a hotel.
     */
    @Query("SELECT t FROM VirtualTour t WHERE t.hotel.id = :hotelId AND t.isActive = true ORDER BY t.sortOrder ASC")
    List<VirtualTour> findActiveByHotelId(@Param("hotelId") Long hotelId);
    
    /**
     * Find all tours for a hotel (including inactive - for admin).
     */
    List<VirtualTour> findByHotel_IdOrderBySortOrderAsc(Long hotelId);
    
    /**
     * Find an active tour by ID.
     */
    Optional<VirtualTour> findByIdAndIsActiveTrue(Long id);
    
    /**
     * Find a tour with all scenes eagerly loaded.
     */
    @Query("SELECT t FROM VirtualTour t LEFT JOIN FETCH t.scenes WHERE t.id = :id AND t.isActive = true")
    Optional<VirtualTour> findByIdWithScenes(@Param("id") Long id);
    
    /**
     * Increment view count (atomic update).
     */
    @Modifying
    @Query("UPDATE VirtualTour t SET t.viewCount = t.viewCount + 1 WHERE t.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * Check if a hotel has any virtual tours.
     */
    boolean existsByHotel_IdAndIsActiveTrue(Long hotelId);
    
    /**
     * Count active tours for a hotel.
     */
    long countByHotel_IdAndIsActiveTrue(Long hotelId);
}
