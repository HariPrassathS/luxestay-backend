package com.hotel.domain.dto.groupbooking;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Group Booking data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBookingDto {
    
    private Long id;
    private String groupCode; // Shareable code for joining
    private String name;
    
    private Long hotelId;
    private String hotelName;
    
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    
    private Integer maxParticipants;
    private Integer currentParticipants;
    
    private String status; // OPEN, LOCKED, CONFIRMED, CANCELLED, COMPLETED
    private String notes;
    private LocalDateTime joinDeadline;
    
    private BigDecimal totalPrice;
    
    private Long organizerId;
    private String organizerName;
    
    private Boolean canJoin;
    
    private List<ParticipantDto> participants;
    
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    
    /**
     * Participant data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {
        private Long id;
        private Long userId;
        private String userName;
        private Boolean isOrganizer;
        private String status; // PENDING, ROOM_SELECTED, CONFIRMED, CANCELLED
        private Long roomId;
        private String roomName;
        private Integer numGuests;
        private Long bookingId;
        private String bookingReference;
        private LocalDateTime joinedAt;
    }
}
