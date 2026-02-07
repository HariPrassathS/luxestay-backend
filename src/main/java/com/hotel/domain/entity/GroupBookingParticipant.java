package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Participant in a group booking.
 * 
 * PARTICIPATION FLOW:
 * 1. User joins group with PENDING status
 * 2. User selects a room → status becomes ROOM_SELECTED
 * 3. Organizer confirms group → individual bookings created
 * 4. After payment → status becomes CONFIRMED
 */
@Entity
@Table(name = "group_booking_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_booking_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBookingParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_booking_id", nullable = false)
    private GroupBooking groupBooking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Room selected by this participant.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    
    /**
     * Individual booking created when group is confirmed.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    /**
     * Status of this participant.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private ParticipantStatus status = ParticipantStatus.PENDING;
    
    /**
     * Number of guests for this participant's room.
     */
    @Column(name = "num_guests")
    @lombok.Builder.Default
    private Integer numGuests = 1;
    
    /**
     * Special requests for this participant.
     */
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    /**
     * Whether this participant is the organizer.
     */
    @Column(name = "is_organizer", nullable = false)
    @lombok.Builder.Default
    private Boolean isOrganizer = false;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    @Column(name = "room_selected_at")
    private LocalDateTime roomSelectedAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    // ==================== Lifecycle ====================
    
    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
    
    // ==================== Helpers ====================
    
    public void selectRoom(Room room) {
        this.room = room;
        this.roomSelectedAt = LocalDateTime.now();
        this.status = ParticipantStatus.ROOM_SELECTED;
    }
    
    public void confirm(Booking booking) {
        this.booking = booking;
        this.confirmedAt = LocalDateTime.now();
        this.status = ParticipantStatus.CONFIRMED;
    }
    
    public void cancel() {
        this.status = ParticipantStatus.CANCELLED;
    }
    
    // ==================== Status Enum ====================
    
    public enum ParticipantStatus {
        PENDING,        // Joined but not selected room
        ROOM_SELECTED,  // Room selected, awaiting confirmation
        CONFIRMED,      // Booking confirmed
        CANCELLED       // Cancelled participation
    }
}
