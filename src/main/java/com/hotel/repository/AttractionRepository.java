package com.hotel.repository;

import com.hotel.domain.entity.Attraction;
import com.hotel.domain.entity.Attraction.AttractionCategory;
import com.hotel.domain.entity.Attraction.TimeOfDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Attraction entity with spatial queries for nearby attractions.
 */
@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    /**
     * Find all active attractions in a specific city
     */
    List<Attraction> findByCityIgnoreCaseAndIsActiveTrue(String city);

    /**
     * Find attractions by category in a city
     */
    List<Attraction> findByCityIgnoreCaseAndCategoryAndIsActiveTrue(String city, AttractionCategory category);

    /**
     * Find attractions by best time of day in a city
     */
    List<Attraction> findByCityIgnoreCaseAndBestTimeAndIsActiveTrue(String city, TimeOfDay bestTime);

    /**
     * Find attractions within a radius using Haversine formula.
     * Distance is calculated in kilometers.
     */
    @Query(value = """
        SELECT a.*, 
               (6371 * ACOS(
                   COS(RADIANS(:lat)) * COS(RADIANS(a.latitude)) * 
                   COS(RADIANS(a.longitude) - RADIANS(:lon)) + 
                   SIN(RADIANS(:lat)) * SIN(RADIANS(a.latitude))
               )) AS distance_km
        FROM attractions a
        WHERE a.is_active = true
          AND a.city = :city
          AND (6371 * ACOS(
                   COS(RADIANS(:lat)) * COS(RADIANS(a.latitude)) * 
                   COS(RADIANS(a.longitude) - RADIANS(:lon)) + 
                   SIN(RADIANS(:lat)) * SIN(RADIANS(a.latitude))
               )) <= :radiusKm
        ORDER BY distance_km ASC
        """, nativeQuery = true)
    List<Attraction> findNearbyAttractions(
            @Param("lat") Double latitude,
            @Param("lon") Double longitude,
            @Param("city") String city,
            @Param("radiusKm") Double radiusKm
    );

    /**
     * Find attractions within radius filtered by category
     */
    @Query(value = """
        SELECT a.*, 
               (6371 * ACOS(
                   COS(RADIANS(:lat)) * COS(RADIANS(a.latitude)) * 
                   COS(RADIANS(a.longitude) - RADIANS(:lon)) + 
                   SIN(RADIANS(:lat)) * SIN(RADIANS(a.latitude))
               )) AS distance_km
        FROM attractions a
        WHERE a.is_active = true
          AND a.city = :city
          AND a.category = :category
          AND (6371 * ACOS(
                   COS(RADIANS(:lat)) * COS(RADIANS(a.latitude)) * 
                   COS(RADIANS(a.longitude) - RADIANS(:lon)) + 
                   SIN(RADIANS(:lat)) * SIN(RADIANS(a.latitude))
               )) <= :radiusKm
        ORDER BY distance_km ASC
        """, nativeQuery = true)
    List<Attraction> findNearbyAttractionsByCategory(
            @Param("lat") Double latitude,
            @Param("lon") Double longitude,
            @Param("city") String city,
            @Param("category") String category,
            @Param("radiusKm") Double radiusKm
    );

    /**
     * Find attractions matching mood tags (using JSON search)
     */
    @Query(value = """
        SELECT a.*
        FROM attractions a
        WHERE a.is_active = true
          AND a.city = :city
          AND JSON_CONTAINS(a.mood_tags, CONCAT('"', :mood, '"'))
        ORDER BY a.rating DESC
        """, nativeQuery = true)
    List<Attraction> findByMoodTag(
            @Param("city") String city,
            @Param("mood") String mood
    );

    /**
     * Find top-rated attractions in a city
     */
    @Query("SELECT a FROM Attraction a WHERE a.city = :city AND a.isActive = true ORDER BY a.rating DESC")
    List<Attraction> findTopRatedInCity(@Param("city") String city);

    /**
     * Count attractions by city
     */
    long countByCityIgnoreCaseAndIsActiveTrue(String city);
}
