package com.hotel.domain.dto.groupbooking;

import lombok.*;

import java.time.LocalDateTime;

/**
 * WebSocket event for real-time group booking updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBookingUpdateEvent {
    
    private Long groupId;
    private String groupCode;
    
    /**
     * Event types:
     * - PARTICIPANT_JOINED
     * - PARTICIPANT_LEFT
     * - ROOM_SELECTED
     * - GROUP_LOCKED
     * - GROUP_CONFIRMED
     * - GROUP_CANCELLED
     */
    private String eventType;
    
    private String message;
    private Integer participantCount;
    private String status;
    private LocalDateTime timestamp;
}
