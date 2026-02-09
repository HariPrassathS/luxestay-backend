package com.hotel.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.domain.dto.hotel.CreateHotelRequest;
import com.hotel.domain.dto.hotel.HotelDto;
import com.hotel.domain.dto.hotel.HotelImageDto;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.HotelImage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Hotel entity and DTOs.
 */
@Component
public class HotelMapper {

    private final ObjectMapper objectMapper;

    public HotelMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convert Hotel entity to HotelDto.
     */
    public HotelDto toDto(Hotel hotel) {
        if (hotel == null) {
            return null;
        }

        List<HotelImageDto> imageDtos = hotel.getImages() != null
                ? hotel.getImages().stream().map(this::toImageDto).collect(Collectors.toList())
                : Collections.emptyList();

        return HotelDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .postalCode(hotel.getPostalCode())
                .phone(hotel.getPhone())
                .email(hotel.getEmail())
                .starRating(hotel.getStarRating())
                .heroImageUrl(hotel.getHeroImageUrl())
                .amenities(parseAmenities(hotel.getAmenities()))
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .isActive(hotel.getIsActive())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .minPrice(hotel.getMinPrice())
                .availableRooms(hotel.getAvailableRoomCount())
                .images(imageDtos)
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }

    /**
     * Convert HotelImage entity to HotelImageDto.
     */
    public HotelImageDto toImageDto(HotelImage image) {
        if (image == null) {
            return null;
        }

        return HotelImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .build();
    }

    /**
     * Convert CreateHotelRequest to Hotel entity.
     */
    public Hotel toEntity(CreateHotelRequest request) {
        if (request == null) {
            return null;
        }

        return Hotel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .starRating(request.getStarRating())
                .heroImageUrl(request.getHeroImageUrl())
                .amenities(serializeAmenities(request.getAmenities()))
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .build();
    }

    /**
     * Update Hotel entity from CreateHotelRequest.
     */
    public void updateEntity(Hotel hotel, CreateHotelRequest request) {
        if (hotel == null || request == null) {
            return;
        }

        if (request.getName() != null) hotel.setName(request.getName());
        if (request.getDescription() != null) hotel.setDescription(request.getDescription());
        if (request.getAddress() != null) hotel.setAddress(request.getAddress());
        if (request.getCity() != null) hotel.setCity(request.getCity());
        if (request.getCountry() != null) hotel.setCountry(request.getCountry());
        if (request.getPostalCode() != null) hotel.setPostalCode(request.getPostalCode());
        if (request.getPhone() != null) hotel.setPhone(request.getPhone());
        if (request.getEmail() != null) hotel.setEmail(request.getEmail());
        if (request.getStarRating() != null) hotel.setStarRating(request.getStarRating());
        if (request.getHeroImageUrl() != null) hotel.setHeroImageUrl(request.getHeroImageUrl());
        if (request.getAmenities() != null) hotel.setAmenities(serializeAmenities(request.getAmenities()));
        if (request.getCheckInTime() != null) hotel.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutTime() != null) hotel.setCheckOutTime(request.getCheckOutTime());
    }

    /**
     * Parse JSON amenities string to list.
     */
    private List<String> parseAmenities(String amenitiesJson) {
        if (amenitiesJson == null || amenitiesJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(amenitiesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Serialize amenities list to JSON string.
     */
    private String serializeAmenities(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(amenities);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
