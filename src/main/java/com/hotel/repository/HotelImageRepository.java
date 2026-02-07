package com.hotel.repository;

import com.hotel.domain.entity.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for HotelImage entity operations.
 */
@Repository
public interface HotelImageRepository extends JpaRepository<HotelImage, Long> {

    /**
     * Find all images for a hotel ordered by sort order.
     */
    List<HotelImage> findByHotelIdOrderBySortOrderAsc(Long hotelId);

    /**
     * Delete all images for a hotel.
     */
    void deleteByHotelId(Long hotelId);
}
