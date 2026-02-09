package com.hotel.repository;

import com.hotel.domain.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Destination entity.
 */
@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    /**
     * Find destination by city name (case-insensitive).
     */
    Optional<Destination> findByCityIgnoreCase(String city);

    /**
     * Check if destination exists by city name.
     */
    boolean existsByCityIgnoreCase(String city);

    /**
     * Find all active destinations ordered by sortOrder then city.
     */
    List<Destination> findByIsActiveTrueOrderBySortOrderAscCityAsc();

    /**
     * Find destinations by region.
     */
    List<Destination> findByRegionIgnoreCaseAndIsActiveTrueOrderBySortOrderAscCityAsc(String region);

    /**
     * Find all destinations ordered by sortOrder then city.
     */
    List<Destination> findAllByOrderBySortOrderAscCityAsc();

    /**
     * Search destinations by city name.
     */
    @Query("SELECT d FROM Destination d WHERE LOWER(d.city) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY d.sortOrder ASC, d.city ASC")
    List<Destination> searchByCity(String query);
}
