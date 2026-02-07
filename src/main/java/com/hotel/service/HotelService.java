package com.hotel.service;

import com.hotel.domain.dto.hotel.CreateHotelRequest;
import com.hotel.domain.dto.hotel.HotelDto;
import com.hotel.domain.entity.Hotel;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.mapper.HotelMapper;
import com.hotel.repository.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling hotel operations.
 */
@Service
public class HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    public HotelService(HotelRepository hotelRepository, HotelMapper hotelMapper) {
        this.hotelRepository = hotelRepository;
        this.hotelMapper = hotelMapper;
    }

    /**
     * Get all active hotels.
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotels() {
        logger.debug("Fetching all active hotels");
        return hotelRepository.findByIsActiveTrue().stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all hotels with pagination.
     */
    @Transactional(readOnly = true)
    public Page<HotelDto> getHotelsPaged(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return hotelRepository.findByIsActiveTrue(pageable)
                .map(hotelMapper::toDto);
    }

    /**
     * Get hotels with dynamic filters and pagination.
     */
    @Transactional(readOnly = true)
    public Page<HotelDto> getHotelsWithFilters(String location, Integer minStars, java.math.BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return hotelRepository.findWithFilters(location, minStars, maxPrice, pageable)
                .map(hotelMapper::toDto);
    }

    /**
     * Get hotel by ID.
     */
    @Transactional(readOnly = true)
    public HotelDto getHotelById(Long id) {
        logger.debug("Fetching hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        return hotelMapper.toDto(hotel);
    }

    /**
     * Get hotel entity by ID.
     */
    @Transactional(readOnly = true)
    public Hotel getHotelEntityById(Long id) {
        return hotelRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
    }

    /**
     * Search hotels by query.
     */
    @Transactional(readOnly = true)
    public List<HotelDto> searchHotels(String query) {
        logger.debug("Searching hotels with query: {}", query);
        return hotelRepository.searchHotels(query).stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search hotels by query with pagination and filters.
     */
    @Transactional(readOnly = true)
    public Page<HotelDto> searchHotelsPaged(String query, Integer minStars, java.math.BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Handle empty query
        String searchQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        
        return hotelRepository.searchHotelsWithFilters(searchQuery, minStars, maxPrice, pageable)
                .map(hotelMapper::toDto);
    }

    /**
     * Get hotels by city.
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getHotelsByCity(String city) {
        return hotelRepository.findByCityIgnoreCaseAndIsActiveTrue(city).stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get hotels by country.
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getHotelsByCountry(String country) {
        return hotelRepository.findByCountryIgnoreCaseAndIsActiveTrue(country).stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get featured hotels.
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getFeaturedHotels(int limit) {
        return hotelRepository.findFeaturedHotels(PageRequest.of(0, limit)).stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all unique cities.
     */
    @Transactional(readOnly = true)
    public List<String> getAllCities() {
        return hotelRepository.findAllCities();
    }

    /**
     * Get all unique countries.
     */
    @Transactional(readOnly = true)
    public List<String> getAllCountries() {
        return hotelRepository.findAllCountries();
    }

    /**
     * Get hotels within map bounding box (for map view).
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getHotelsInBounds(Double southLat, Double northLat, 
                                             Double westLng, Double eastLng) {
        logger.debug("Fetching hotels in bounds: S:{}, N:{}, W:{}, E:{}", 
                     southLat, northLat, westLng, eastLng);
        return hotelRepository.findHotelsInBounds(southLat, northLat, westLng, eastLng)
                .stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all hotels with coordinates (for map view).
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotelsWithCoordinates() {
        logger.debug("Fetching all hotels with coordinates");
        return hotelRepository.findAllWithCoordinates().stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Admin Operations ====================

    /**
     * Create a new hotel (Admin only).
     */
    @Transactional
    public HotelDto createHotel(CreateHotelRequest request) {
        logger.info("Creating new hotel: {}", request.getName());
        
        Hotel hotel = hotelMapper.toEntity(request);
        hotel.setIsActive(true);
        hotel = hotelRepository.save(hotel);
        
        logger.info("Hotel created with ID: {}", hotel.getId());
        return hotelMapper.toDto(hotel);
    }

    /**
     * Update an existing hotel (Admin only).
     */
    @Transactional
    public HotelDto updateHotel(Long id, CreateHotelRequest request) {
        logger.info("Updating hotel with ID: {}", id);
        
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        
        hotelMapper.updateEntity(hotel, request);
        hotel = hotelRepository.save(hotel);
        
        logger.info("Hotel updated: {}", hotel.getId());
        return hotelMapper.toDto(hotel);
    }

    /**
     * Delete a hotel (soft delete by deactivating).
     */
    @Transactional
    public void deleteHotel(Long id) {
        logger.info("Deleting hotel with ID: {}", id);
        
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        
        hotel.setIsActive(false);
        hotelRepository.save(hotel);
        
        logger.info("Hotel deactivated: {}", id);
    }

    /**
     * Get all hotels including inactive (Admin only).
     */
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotelsAdmin() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }
}
