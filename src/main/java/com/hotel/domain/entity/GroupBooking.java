package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Group Booking entity for coordinated multi-room reservations.
 * 
 * OWNERSHIP MODEL:
 * - One organizer who creates and manages the group
 * - Multiple participants who can join
 * - Group-level dates and confirmation
 * - Individual room assignments per participant
 * 
 * REAL-TIME FEATURES:
 * - WebSocket updates for participant changes
 * - Shared booking reference
 * - Synchronized availability checks
 */
@Entity
@Table(name = "group_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBooking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique shareable code for joining the group.
     */
    @Column(name = "group_code", nullable = false, unique = true, length = 20)
    private String groupCode;
    
    /**
     * Human-readable group name.
     */
    @Column(nullable = false, length = 255)
    private String name;
    
    /**
     * User who created and owns this group booking.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
    
    /**
     * Hotel being booked.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    /**
     * Shared check-in date for all participants.
     */
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;
    
    /**
     * Shared check-out date for all participants.
     */
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;
    
    /**
     * Maximum number of participants allowed.
     */
    @Column(name = "max_participants", nullable = false)
    @lombok.Builder.Default
    private Integer maxParticipants = 10;
    
    /**
     * Total price across all participant bookings.
     */
    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;
    
    /**
     * Current status of the group booking.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private GroupBookingStatus status = GroupBookingStatus.OPEN;
    
    /**
     * Optional notes/instructions for participants.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Deadline for participants to join (optional).
     */
    @Column(name = "join_deadline")
    private LocalDateTime joinDeadline;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    // ==================== Relationships ====================
    
    /**
     * Participants in this group booking.
     */
    @OneToMany(mappedBy = "groupBooking", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("joinedAt ASC")
    @Builder.Default
    private List<GroupBookingParticipant> participants = new ArrayList<>();
    
    // ==================== Lifecycle ====================
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (groupCode == null) {
            groupCode = generateGroupCode();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== Helpers ====================
    
    private String generateGroupCode() {
        return "GRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public void addParticipant(GroupBookingParticipant participant) {
        participants.add(participant);
        participant.setGroupBooking(this);
    }
    
    public void removeParticipant(GroupBookingParticipant participant) {
        participants.remove(participant);
        participant.setGroupBooking(null);
    }
    
    public int getCurrentParticipantCount() {
        return participants.size();
    }
    
    public boolean canJoin() {
        if (status != GroupBookingStatus.OPEN) return false;
        if (getCurrentParticipantCount() >= maxParticipants) return false;
        if (joinDeadline != null && LocalDateTime.now().isAfter(joinDeadline)) return false;
        return true;
    }
    
    public boolean isOrganizer(Long userId) {
        return organizer != null && organizer.getId().equals(userId);
    }
    
    public void recalculateTotalPrice() {
        this.totalPrice = participants.stream()
                .filter(p -> p.getBooking() != null)
                .map(p -> p.getBooking().getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // ==================== Status Enum ====================
    
    public enum GroupBookingStatus {
        OPEN,       // Accepting participants
        LOCKED,     // No more participants, pending confirmation
        CONFIRMED,  // All bookings confirmed
        CANCELLED,  // Group booking cancelled
        COMPLETED   // All stays completed
    }
}
