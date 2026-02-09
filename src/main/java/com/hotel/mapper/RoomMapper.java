package com.hotel.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.domain.dto.room.CreateRoomRequest;
import com.hotel.domain.dto.room.RoomDto;
import com.hotel.domain.entity.Room;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper for Room entity and DTOs.
 */
@Component
public class RoomMapper {

    private final ObjectMapper objectMapper;

    public RoomMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convert Room entity to RoomDto.
     */
    public RoomDto toDto(Room room) {
        if (room == null) {
            return null;
        }

        return RoomDto.builder()
                .id(room.getId())
                .hotelId(room.getHotelId())
                .hotelName(room.getHotelName())
                .roomNumber(room.getRoomNumber())
                .name(room.getName())
                .description(room.getDescription())
                .roomType(room.getRoomType())
                .pricePerNight(room.getPricePerNight())
                .capacity(room.getCapacity())
                .bedType(room.getBedType())
                .sizeSqm(room.getSizeSqm())
                .imageUrl(room.getImageUrl())
                .amenities(parseAmenities(room.getAmenities()))
                .isAvailable(room.getIsAvailable())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateRoomRequest to Room entity.
     */
    public Room toEntity(CreateRoomRequest request) {
        if (request == null) {
            return null;
        }

        return Room.builder()
                .roomNumber(request.getRoomNumber())
                .name(request.getName())
                .description(request.getDescription())
                .roomType(request.getRoomType())
                .pricePerNight(request.getPricePerNight())
                .capacity(request.getCapacity())
                .bedType(request.getBedType())
                .sizeSqm(request.getSizeSqm())
                .imageUrl(request.getImageUrl())
                .amenities(serializeAmenities(request.getAmenities()))
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();
    }

    /**
     * Update Room entity from CreateRoomRequest.
     */
    public void updateEntity(Room room, CreateRoomRequest request) {
        if (room == null || request == null) {
            return;
        }

        if (request.getRoomNumber() != null) room.setRoomNumber(request.getRoomNumber());
        if (request.getName() != null) room.setName(request.getName());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());
        if (request.getPricePerNight() != null) room.setPricePerNight(request.getPricePerNight());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getBedType() != null) room.setBedType(request.getBedType());
        if (request.getSizeSqm() != null) room.setSizeSqm(request.getSizeSqm());
        if (request.getImageUrl() != null) room.setImageUrl(request.getImageUrl());
        if (request.getAmenities() != null) room.setAmenities(serializeAmenities(request.getAmenities()));
        if (request.getIsAvailable() != null) room.setIsAvailable(request.getIsAvailable());
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
