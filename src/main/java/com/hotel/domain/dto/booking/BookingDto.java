package com.hotel.domain.dto.booking;

import com.hotel.domain.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for booking information in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {

    private Long id;
    private String bookingReference;
    
    // User info
    private Long userId;
    private String userEmail;
    private String userFullName;
    
    // Room info
    private Long roomId;
    private String roomName;
    private String roomType;
    private String roomImageUrl;
    
    // Hotel info
    private Long hotelId;
    private String hotelName;
    private String hotelCity;
    private String hotelImageUrl;
    
    // Booking details
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numGuests;
    private Integer totalNights;
    private BigDecimal pricePerNight;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String specialRequests;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
}
