package com.hotel.domain.dto.realtime;

import com.hotel.domain.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for booking notifications sent to users via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingNotification {
    
    public enum NotificationType {
        BOOKING_CREATED,
        BOOKING_CONFIRMED,
        BOOKING_CANCELLED,
        CHECK_IN_REMINDER,
        CHECK_OUT_REMINDER,
        STATUS_UPDATE
    }
    
    private NotificationType type;
    private Long bookingId;
    private String bookingReference;
    private Long hotelId;
    private String hotelName;
    private String roomName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private String message;
    private Long timestamp;
}
