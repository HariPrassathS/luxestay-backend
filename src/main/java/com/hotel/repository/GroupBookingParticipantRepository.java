package com.hotel.repository;

import com.hotel.domain.entity.GroupBookingParticipant;
import com.hotel.domain.entity.GroupBookingParticipant.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Group Booking Participant operations.
 */
@Repository
public interface GroupBookingParticipantRepository extends JpaRepository<GroupBookingParticipant, Long> {
    
    /**
     * Find participant by group and user.
     */
    Optional<GroupBookingParticipant> findByGroupBooking_IdAndUser_Id(Long groupId, Long userId);
    
    /**
     * Find all participants for a group.
     */
    List<GroupBookingParticipant> findByGroupBooking_IdOrderByJoinedAtAsc(Long groupId);
    
    /**
     * Find participants by status.
     */
    List<GroupBookingParticipant> findByGroupBooking_IdAndStatus(Long groupId, ParticipantStatus status);
    
    /**
     * Count participants in a group.
     */
    long countByGroupBooking_Id(Long groupId);
    
    /**
     * Count participants with rooms selected.
     */
    @Query("SELECT COUNT(p) FROM GroupBookingParticipant p " +
           "WHERE p.groupBooking.id = :groupId AND p.room IS NOT NULL")
    long countWithRoomSelected(@Param("groupId") Long groupId);
    
    /**
     * Check if a room is selected by any participant in the group.
     */
    boolean existsByGroupBooking_IdAndRoom_Id(Long groupId, Long roomId);
    
    /**
     * Find participant by booking.
     */
    Optional<GroupBookingParticipant> findByBooking_Id(Long bookingId);
}
