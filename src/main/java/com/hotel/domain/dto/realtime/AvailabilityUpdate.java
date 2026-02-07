package com.hotel.domain.dto.realtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for real-time availability updates sent via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityUpdate {
    
    public enum UpdateType {
        ROOM_BOOKED,      // A room was just booked
        ROOM_AVAILABLE,   // A booking was cancelled, room is available
        AVAILABILITY_CHECK, // Response to availability check request
        REFRESH           // Full availability refresh
    }
    
    private UpdateType type;
    private Long hotelId;
    private Long roomId;
    private String roomName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer availableCount;
    private Integer totalRooms;
    private List<Long> availableRoomIds;
    private Long timestamp;
    private String message;
}
