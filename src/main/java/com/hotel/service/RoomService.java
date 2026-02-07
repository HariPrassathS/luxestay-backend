package com.hotel.service;

import com.hotel.domain.dto.room.CreateRoomRequest;
import com.hotel.domain.dto.room.RoomDto;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Room;
import com.hotel.domain.entity.RoomType;
import com.hotel.exception.BadRequestException;
import com.hotel.exception.ConflictException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.mapper.RoomMapper;
import com.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling room operations.
 */
@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final HotelService hotelService;
    private final RoomMapper roomMapper;

    public RoomService(RoomRepository roomRepository, 
                       HotelService hotelService,
                       RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.hotelService = hotelService;
        this.roomMapper = roomMapper;
    }

    /**
     * Get all rooms for a hotel.
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getRoomsByHotelId(Long hotelId) {
        logger.debug("Fetching rooms for hotel ID: {}", hotelId);
        // Verify hotel exists
        hotelService.getHotelById(hotelId);
        
        return roomRepository.findByHotel_Id(hotelId).stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all rooms (Admin only).
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRoomsAdmin() {
        logger.debug("Fetching all rooms for admin");
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get available rooms for a hotel.
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRooms(Long hotelId) {
        logger.debug("Fetching available rooms for hotel ID: {}", hotelId);
        return roomRepository.findByHotel_IdAndIsAvailableTrue(hotelId).stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a room by ID.
     */
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id) {
        logger.debug("Fetching room with ID: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return roomMapper.toDto(room);
    }

    /**
     * Get room entity by ID.
     */
    @Transactional(readOnly = true)
    public Room getRoomEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
    }

    /**
     * Get available rooms for specific dates.
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRoomsForDates(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        logger.debug("Fetching available rooms for hotel {} from {} to {}", hotelId, checkIn, checkOut);
        
        validateDates(checkIn, checkOut);
        
        return roomRepository.findAvailableRooms(hotelId, checkIn, checkOut).stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get available rooms with filters.
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRoomsWithFilters(Long hotelId, 
                                                       LocalDate checkIn, 
                                                       LocalDate checkOut,
                                                       Integer guests,
                                                       RoomType roomType) {
        logger.debug("Fetching filtered available rooms for hotel {}", hotelId);
        
        validateDates(checkIn, checkOut);
        
        if (guests == null || guests < 1) {
            guests = 1;
        }
        
        return roomRepository.findAvailableRoomsWithFilters(hotelId, checkIn, checkOut, guests, roomType)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if a room is available for specific dates.
     */
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return roomRepository.isRoomAvailable(roomId, checkIn, checkOut);
    }

    // ==================== Admin Operations ====================

    /**
     * Create a new room (Admin only).
     */
    @Transactional
    public RoomDto createRoom(Long hotelId, CreateRoomRequest request) {
        logger.info("Creating new room for hotel ID: {}", hotelId);
        
        Hotel hotel = hotelService.getHotelEntityById(hotelId);
        
        // Check for duplicate room number
        if (roomRepository.existsByHotel_IdAndRoomNumber(hotelId, request.getRoomNumber())) {
            throw new ConflictException("Room number " + request.getRoomNumber() + " already exists in this hotel");
        }
        
        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);
        room = roomRepository.save(room);
        
        logger.info("Room created with ID: {}", room.getId());
        return roomMapper.toDto(room);
    }

    /**
     * Update a room (Admin only).
     */
    @Transactional
    public RoomDto updateRoom(Long id, CreateRoomRequest request) {
        logger.info("Updating room with ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        
        // Check for duplicate room number if changed
        if (!room.getRoomNumber().equals(request.getRoomNumber()) &&
            roomRepository.existsByHotel_IdAndRoomNumber(room.getHotel().getId(), request.getRoomNumber())) {
            throw new ConflictException("Room number " + request.getRoomNumber() + " already exists in this hotel");
        }
        
        roomMapper.updateEntity(room, request);
        room = roomRepository.save(room);
        
        logger.info("Room updated: {}", room.getId());
        return roomMapper.toDto(room);
    }

    /**
     * Delete a room (Admin only).
     */
    @Transactional
    public void deleteRoom(Long id) {
        logger.info("Deleting room with ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        
        // Soft delete by marking unavailable
        room.setIsAvailable(false);
        roomRepository.save(room);
        
        logger.info("Room marked as unavailable: {}", id);
    }

    /**
     * Toggle room availability (Admin only).
     */
    @Transactional
    public RoomDto toggleAvailability(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        
        room.setIsAvailable(!room.getIsAvailable());
        room = roomRepository.save(room);
        
        return roomMapper.toDto(room);
    }

    // ==================== Helper Methods ====================

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Check-in and check-out dates are required");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past");
        }
        if (checkOut.isBefore(checkIn) || checkOut.equals(checkIn)) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }
    }
}
