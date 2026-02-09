package com.hotel.service;

import com.hotel.domain.dto.destination.CreateDestinationRequest;
import com.hotel.domain.dto.destination.DestinationDto;
import com.hotel.domain.entity.Destination;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing destinations displayed on homepage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DestinationService {

    private final DestinationRepository destinationRepository;

    /**
     * Get all active destinations for public display.
     */
    public List<DestinationDto> getActiveDestinations() {
        return destinationRepository.findByIsActiveTrueOrderBySortOrderAscCityAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all destinations (for admin).
     */
    public List<DestinationDto> getAllDestinations() {
        return destinationRepository.findAllByOrderBySortOrderAscCityAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get destinations by region.
     */
    public List<DestinationDto> getDestinationsByRegion(String region) {
        return destinationRepository.findByRegionIgnoreCaseAndIsActiveTrueOrderBySortOrderAscCityAsc(region)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get destination by ID.
     */
    public DestinationDto getDestinationById(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));
        return toDto(destination);
    }

    /**
     * Get destination by city name.
     */
    public DestinationDto getDestinationByCity(String city) {
        Destination destination = destinationRepository.findByCityIgnoreCase(city)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + city));
        return toDto(destination);
    }

    /**
     * Create a new destination.
     */
    @Transactional
    public DestinationDto createDestination(CreateDestinationRequest request) {
        // Check if city already exists
        if (destinationRepository.existsByCityIgnoreCase(request.getCity())) {
            throw new IllegalArgumentException("Destination already exists: " + request.getCity());
        }

        Destination destination = Destination.builder()
                .city(request.getCity())
                .imageUrl(request.getImageUrl())
                .region(request.getRegion() != null ? request.getRegion().toLowerCase() : "other")
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        destination = destinationRepository.save(destination);
        log.info("Created destination: {}", destination.getCity());
        return toDto(destination);
    }

    /**
     * Update an existing destination.
     */
    @Transactional
    public DestinationDto updateDestination(Long id, CreateDestinationRequest request) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));

        // Check if new city name conflicts with another destination
        if (!destination.getCity().equalsIgnoreCase(request.getCity()) &&
                destinationRepository.existsByCityIgnoreCase(request.getCity())) {
            throw new IllegalArgumentException("Destination already exists: " + request.getCity());
        }

        destination.setCity(request.getCity());
        destination.setImageUrl(request.getImageUrl());
        if (request.getRegion() != null) {
            destination.setRegion(request.getRegion().toLowerCase());
        }
        if (request.getIsActive() != null) {
            destination.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            destination.setSortOrder(request.getSortOrder());
        }

        destination = destinationRepository.save(destination);
        log.info("Updated destination: {}", destination.getCity());
        return toDto(destination);
    }

    /**
     * Update destination by city name.
     */
    @Transactional
    public DestinationDto updateDestinationByCity(String city, CreateDestinationRequest request) {
        Destination destination = destinationRepository.findByCityIgnoreCase(city)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + city));

        destination.setImageUrl(request.getImageUrl());
        if (request.getRegion() != null) {
            destination.setRegion(request.getRegion().toLowerCase());
        }
        if (request.getIsActive() != null) {
            destination.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            destination.setSortOrder(request.getSortOrder());
        }

        destination = destinationRepository.save(destination);
        log.info("Updated destination by city: {}", destination.getCity());
        return toDto(destination);
    }

    /**
     * Delete a destination.
     */
    @Transactional
    public void deleteDestination(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));
        destinationRepository.delete(destination);
        log.info("Deleted destination: {}", destination.getCity());
    }

    /**
     * Delete destination by city name.
     */
    @Transactional
    public void deleteDestinationByCity(String city) {
        Destination destination = destinationRepository.findByCityIgnoreCase(city)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + city));
        destinationRepository.delete(destination);
        log.info("Deleted destination by city: {}", city);
    }

    /**
     * Search destinations by city name.
     */
    public List<DestinationDto> searchDestinations(String query) {
        return destinationRepository.searchByCity(query)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Seed default destinations if none exist.
     */
    @Transactional
    public void seedDefaultDestinations() {
        if (destinationRepository.count() > 0) {
            log.info("Destinations already exist, skipping seed");
            return;
        }

        log.info("Seeding default destinations...");
        
        // Tamil Nadu
        createIfNotExists("Chennai", "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?w=600", "tamilnadu");
        createIfNotExists("Coimbatore", "https://images.unsplash.com/photo-1590077428593-a55bb07c4665?w=600", "tamilnadu");
        createIfNotExists("Madurai", "https://images.unsplash.com/photo-1548013146-72479768bada?w=600", "tamilnadu");
        createIfNotExists("Ooty", "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600", "tamilnadu");
        createIfNotExists("Kodaikanal", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600", "tamilnadu");
        createIfNotExists("Pondicherry", "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?w=600", "tamilnadu");
        createIfNotExists("Kanyakumari", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600", "tamilnadu");
        createIfNotExists("Mahabalipuram", "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600", "tamilnadu");
        createIfNotExists("Coonoor", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600", "tamilnadu");
        
        // USA
        createIfNotExists("New York", "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=600", "usa");
        createIfNotExists("Miami", "https://images.unsplash.com/photo-1533106497176-45ae19e68ba2?w=600", "usa");
        createIfNotExists("Chicago", "https://images.unsplash.com/photo-1494522855154-9297ac14b55f?w=600", "usa");
        createIfNotExists("Los Angeles", "https://images.unsplash.com/photo-1534190760961-74e8c1c5c3da?w=600", "usa");
        createIfNotExists("Las Vegas", "https://images.unsplash.com/photo-1605833556294-ea5c7a74f57d?w=600", "usa");
        createIfNotExists("Aspen", "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600", "usa");

        // Europe
        createIfNotExists("Paris", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600", "europe");
        createIfNotExists("London", "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=600", "europe");
        createIfNotExists("Rome", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=600", "europe");
        createIfNotExists("Barcelona", "https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=600", "europe");

        // Asia
        createIfNotExists("Tokyo", "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=600", "asia");
        createIfNotExists("Singapore", "https://images.unsplash.com/photo-1525625293386-3f8f99389edd?w=600", "asia");
        createIfNotExists("Dubai", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=600", "asia");

        // Other
        createIfNotExists("Sydney", "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?w=600", "other");
        createIfNotExists("Maldives", "https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=600", "other");

        log.info("Default destinations seeded successfully");
    }

    private void createIfNotExists(String city, String imageUrl, String region) {
        if (!destinationRepository.existsByCityIgnoreCase(city)) {
            Destination destination = Destination.builder()
                    .city(city)
                    .imageUrl(imageUrl)
                    .region(region)
                    .isActive(true)
                    .sortOrder(0)
                    .build();
            destinationRepository.save(destination);
        }
    }

    /**
     * Convert entity to DTO.
     */
    private DestinationDto toDto(Destination destination) {
        return DestinationDto.builder()
                .id(destination.getId())
                .city(destination.getCity())
                .imageUrl(destination.getImageUrl())
                .region(destination.getRegion())
                .isActive(destination.getIsActive())
                .sortOrder(destination.getSortOrder())
                .updatedAt(destination.getUpdatedAt())
                .build();
    }
}
