package com.hotel.controller;

import com.hotel.domain.dto.realtime.AvailabilityUpdate;
import com.hotel.service.RealTimeAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

/**
 * WebSocket controller for real-time availability updates.
 * Clients can subscribe to topics and request availability checks.
 * 
 * Subscribe topics:
 * - /topic/availability/hotel/{hotelId} - Hotel-level availability updates
 * - /topic/availability/room/{roomId} - Room-specific availability updates
 * - /user/queue/bookings - User's personal booking notifications
 * 
 * Send messages:
 * - /app/availability/check/{hotelId} - Request availability check
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AvailabilityWebSocketController {

    private final RealTimeAvailabilityService availabilityService;

    /**
     * Handle availability check request from client.
     * Client sends: { "checkIn": "2024-03-15", "checkOut": "2024-03-18" }
     * Response broadcast to: /topic/availability/hotel/{hotelId}
     */
    @MessageMapping("/availability/check/{hotelId}")
    @SendTo("/topic/availability/hotel/{hotelId}")
    public AvailabilityUpdate checkAvailability(
            @DestinationVariable Long hotelId,
            AvailabilityCheckRequest request) {
        
        log.info("Availability check requested for hotel {} from {} to {}", 
                hotelId, request.getCheckIn(), request.getCheckOut());
        
        return availabilityService.getAvailability(
                hotelId, 
                request.getCheckIn(), 
                request.getCheckOut()
        );
    }

    /**
     * Request DTO for availability check.
     */
    public static class AvailabilityCheckRequest {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate checkIn;
        
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate checkOut;

        public LocalDate getCheckIn() {
            return checkIn;
        }

        public void setCheckIn(LocalDate checkIn) {
            this.checkIn = checkIn;
        }

        public LocalDate getCheckOut() {
            return checkOut;
        }

        public void setCheckOut(LocalDate checkOut) {
            this.checkOut = checkOut;
        }
    }
}
