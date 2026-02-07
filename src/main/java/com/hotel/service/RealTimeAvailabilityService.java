package com.hotel.service;

import com.hotel.domain.dto.realtime.AvailabilityUpdate;
import com.hotel.domain.dto.realtime.BookingNotification;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for real-time availability updates via WebSocket.
 * Broadcasts availability changes to subscribed clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeAvailabilityService {

    private final SimpMessagingTemplate messagingTemplate;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    /**
     * Broadcast availability update when a booking is created.
     * Async to not block the booking transaction.
     */
    @Async
    public void broadcastBookingCreated(Booking booking) {
        try {
            Long hotelId = booking.getRoom().getHotel().getId();
            Long roomId = booking.getRoom().getId();
            
            // Build availability update
            AvailabilityUpdate update = AvailabilityUpdate.builder()
                    .type(AvailabilityUpdate.UpdateType.ROOM_BOOKED)
                    .hotelId(hotelId)
                    .roomId(roomId)
                    .roomName(booking.getRoom().getName())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .availableCount(getAvailableRoomsCount(hotelId, booking.getCheckInDate(), booking.getCheckOutDate()))
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            // Broadcast to hotel-specific topic
            messagingTemplate.convertAndSend(
                    "/topic/availability/hotel/" + hotelId, 
                    update
            );
            
            // Broadcast to room-specific topic
            messagingTemplate.convertAndSend(
                    "/topic/availability/room/" + roomId, 
                    update
            );
            
            log.info("Broadcast availability update for hotel {} room {}: BOOKED for {} to {}",
                    hotelId, roomId, booking.getCheckInDate(), booking.getCheckOutDate());
                    
        } catch (Exception e) {
            log.error("Failed to broadcast booking created: {}", e.getMessage());
        }
    }

    /**
     * Broadcast availability update when a booking is cancelled.
     */
    @Async
    public void broadcastBookingCancelled(Booking booking) {
        try {
            Long hotelId = booking.getRoom().getHotel().getId();
            Long roomId = booking.getRoom().getId();
            
            AvailabilityUpdate update = AvailabilityUpdate.builder()
                    .type(AvailabilityUpdate.UpdateType.ROOM_AVAILABLE)
                    .hotelId(hotelId)
                    .roomId(roomId)
                    .roomName(booking.getRoom().getName())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .availableCount(getAvailableRoomsCount(hotelId, booking.getCheckInDate(), booking.getCheckOutDate()))
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            messagingTemplate.convertAndSend(
                    "/topic/availability/hotel/" + hotelId, 
                    update
            );
            
            messagingTemplate.convertAndSend(
                    "/topic/availability/room/" + roomId, 
                    update
            );
            
            log.info("Broadcast availability update for hotel {} room {}: AVAILABLE for {} to {}",
                    hotelId, roomId, booking.getCheckInDate(), booking.getCheckOutDate());
                    
        } catch (Exception e) {
            log.error("Failed to broadcast booking cancelled: {}", e.getMessage());
        }
    }

    /**
     * Send booking notification to a specific user.
     */
    @Async
    public void sendBookingNotification(Long userId, BookingNotification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/bookings",
                    notification
            );
            log.debug("Sent booking notification to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send booking notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Get real-time availability for a hotel on specific dates.
     */
    @Transactional(readOnly = true)
    public AvailabilityUpdate getAvailability(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        List<Room> allRooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotelId);
        
        List<Long> unavailableRoomIds = allRooms.stream()
                .filter(room -> bookingRepository.hasOverlappingBooking(room.getId(), checkIn, checkOut))
                .map(Room::getId)
                .collect(Collectors.toList());
        
        List<Room> availableRooms = allRooms.stream()
                .filter(room -> !unavailableRoomIds.contains(room.getId()))
                .collect(Collectors.toList());
        
        return AvailabilityUpdate.builder()
                .type(AvailabilityUpdate.UpdateType.AVAILABILITY_CHECK)
                .hotelId(hotelId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .availableCount(availableRooms.size())
                .totalRooms(allRooms.size())
                .availableRoomIds(availableRooms.stream().map(Room::getId).collect(Collectors.toList()))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Broadcast general availability refresh (e.g., for date changes, search).
     */
    public void broadcastAvailabilityRefresh(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        AvailabilityUpdate update = getAvailability(hotelId, checkIn, checkOut);
        update.setType(AvailabilityUpdate.UpdateType.REFRESH);
        
        messagingTemplate.convertAndSend(
                "/topic/availability/hotel/" + hotelId, 
                update
        );
    }

    /**
     * Helper to count available rooms for a hotel on given dates.
     */
    private int getAvailableRoomsCount(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        List<Room> allRooms = roomRepository.findByHotel_IdAndIsAvailableTrue(hotelId);
        return (int) allRooms.stream()
                .filter(room -> !bookingRepository.hasOverlappingBooking(room.getId(), checkIn, checkOut))
                .count();
    }
}
