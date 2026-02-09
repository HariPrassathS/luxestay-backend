package com.hotel.repository;

import com.hotel.domain.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Hotel entity operations.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Check if a hotel exists in a city (for seeding).
     */
    boolean existsByCity(String city);

    /**
     * Find all active hotels.
     */
    List<Hotel> findByIsActiveTrue();

    /**
     * Find all active hotels with pagination.
     */
    Page<Hotel> findByIsActiveTrue(Pageable pageable);

    /**
     * Find a hotel by ID only if active.
     */
    Optional<Hotel> findByIdAndIsActiveTrue(Long id);

    /**
     * Find hotels by city (case-insensitive).
     */
    List<Hotel> findByCityIgnoreCaseAndIsActiveTrue(String city);

    /**
     * Find hotels by country (case-insensitive).
     */
    List<Hotel> findByCountryIgnoreCaseAndIsActiveTrue(String country);

    /**
     * Search hotels by name, city, or country (case-insensitive).
     */
    @Query("SELECT h FROM Hotel h WHERE h.isActive = true AND " +
           "(LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(LOWER(:query) LIKE '%tamil nadu%' AND h.country = 'India'))")
    List<Hotel> searchHotels(@Param("query") String query);

    /**
     * Search hotels with pagination.
     */
    @Query("SELECT h FROM Hotel h WHERE h.isActive = true AND " +
           "(LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(LOWER(:query) LIKE '%tamil nadu%' AND h.country = 'India'))")
    Page<Hotel> searchHotels(@Param("query") String query, Pageable pageable);

    /**
     * Search hotels with dynamic filters (stars, price, query).
     */
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "WHERE h.isActive = true " +
           "AND (:query IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "    OR LOWER(h.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "    OR LOWER(h.address) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "    OR LOWER(h.city) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "    OR LOWER(h.country) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "    OR (LOWER(:query) LIKE '%tamil nadu%' AND h.country = 'India')) " +
           "AND (:minStars IS NULL OR h.starRating >= :minStars) " +
           "AND (:maxPrice IS NULL OR EXISTS (SELECT r FROM Room r WHERE r.hotel = h AND (r.pricePerNight IS NOT NULL AND r.pricePerNight <= :maxPrice)))")
    Page<Hotel> searchHotelsWithFilters(
            @Param("query") String query,
            @Param("minStars") Integer minStars,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Find hotels by star rating.
     */
    List<Hotel> findByStarRatingAndIsActiveTrue(Integer starRating);

    /**
     * Find featured hotels (highest rated).
     */
    @Query("SELECT h FROM Hotel h WHERE h.isActive = true ORDER BY h.starRating DESC, h.name ASC")
    List<Hotel> findFeaturedHotels(Pageable pageable);

    /**
     * Get unique cities from active hotels.
     */
    @Query("SELECT DISTINCT h.city FROM Hotel h WHERE h.isActive = true ORDER BY h.city")
    List<String> findAllCities();

    /**
     * Get unique countries from active hotels.
     */
    @Query("SELECT DISTINCT h.country FROM Hotel h WHERE h.isActive = true ORDER BY h.country")
    List<String> findAllCountries();

    /**
     * Find hotels with dynamic filters.
     */
    @Query("SELECT h FROM Hotel h WHERE h.isActive = true " +
           "AND (:location IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "OR LOWER(h.description) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "OR LOWER(h.address) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "OR LOWER(h.city) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "OR LOWER(h.country) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "OR (LOWER(:location) LIKE '%tamil nadu%' AND h.country = 'India')) " +
           "AND (:minStars IS NULL OR h.starRating >= :minStars) " +
           "AND (:maxPrice IS NULL OR EXISTS (SELECT r FROM Room r WHERE r.hotel = h AND r.pricePerNight <= :maxPrice))")
    Page<Hotel> findWithFilters(
            @Param("location") String location,
            @Param("minStars") Integer minStars,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Find hotels within map bounding box (for map view).
     * Returns hotels with coordinates within the specified lat/lng bounds.
     */
    @Query("SELECT h FROM Hotel h WHERE h.isActive = true " +
           "AND h.latitude IS NOT NULL AND h.longitude IS NOT NULL " +
           "AND h.latitude BETWEEN :southLat AND :northLat " +
           "AND h.longitude BETWEEN :westLng AND :eastLng")
    List<Hotel> findHotelsInBounds(
            @Param("southLat") Double southLat,
            @Param("northLat") Double northLat,
            @Param("westLng") Double westLng,
            @Param("eastLng") Double eastLng);

    /**
     * Find all hotels that have coordinates (for map view).
     */
    @Query("SELECT h FROM Hotel h WHERE h.isActive = true " +
           "AND h.latitude IS NOT NULL AND h.longitude IS NOT NULL")
    List<Hotel> findAllWithCoordinates();
}
