package com.hotel.repository;

import com.hotel.domain.entity.GroupBooking;
import com.hotel.domain.entity.GroupBooking.GroupBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Group Booking operations.
 */
@Repository
public interface GroupBookingRepository extends JpaRepository<GroupBooking, Long> {
    
    /**
     * Find a group booking by its shareable code.
     */
    Optional<GroupBooking> findByGroupCode(String groupCode);
    
    /**
     * Find all group bookings organized by a user.
     */
    List<GroupBooking> findByOrganizer_IdOrderByCreatedAtDesc(Long organizerId);
    
    /**
     * Find group bookings where user is a participant.
     */
    @Query("SELECT DISTINCT g FROM GroupBooking g " +
           "JOIN g.participants p " +
           "WHERE p.user.id = :userId " +
           "ORDER BY g.createdAt DESC")
    List<GroupBooking> findByParticipantUserId(@Param("userId") Long userId);
    
    /**
     * Find all group bookings for a hotel.
     */
    List<GroupBooking> findByHotel_IdOrderByCreatedAtDesc(Long hotelId);
    
    /**
     * Find group bookings for a hotel with pagination.
     */
    Page<GroupBooking> findByHotel_Id(Long hotelId, Pageable pageable);
    
    /**
     * Find open group bookings for a hotel (joinable).
     */
    @Query("SELECT g FROM GroupBooking g WHERE g.hotel.id = :hotelId " +
           "AND g.status = 'OPEN' " +
           "AND (g.joinDeadline IS NULL OR g.joinDeadline > CURRENT_TIMESTAMP) " +
           "ORDER BY g.checkInDate ASC")
    List<GroupBooking> findOpenGroupsForHotel(@Param("hotelId") Long hotelId);
    
    /**
     * Find group bookings by status.
     */
    List<GroupBooking> findByStatusOrderByCreatedAtDesc(GroupBookingStatus status);
    
    /**
     * Find upcoming group bookings (check-in date >= today).
     */
    @Query("SELECT g FROM GroupBooking g WHERE g.checkInDate >= :today " +
           "AND g.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "ORDER BY g.checkInDate ASC")
    List<GroupBooking> findUpcomingGroups(@Param("today") LocalDate today);
    
    /**
     * Count active group bookings for a hotel.
     */
    @Query("SELECT COUNT(g) FROM GroupBooking g WHERE g.hotel.id = :hotelId " +
           "AND g.status NOT IN ('CANCELLED', 'COMPLETED')")
    long countActiveByHotelId(@Param("hotelId") Long hotelId);
    
    /**
     * Check if user is participant in a group.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM GroupBookingParticipant p " +
           "WHERE p.groupBooking.id = :groupId AND p.user.id = :userId")
    boolean isUserParticipant(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * Find with participants eagerly loaded.
     */
    @Query("SELECT g FROM GroupBooking g " +
           "LEFT JOIN FETCH g.participants p " +
           "LEFT JOIN FETCH p.user " +
           "LEFT JOIN FETCH p.room " +
           "WHERE g.id = :id")
    Optional<GroupBooking> findByIdWithParticipants(@Param("id") Long id);
    
    /**
     * Find by group code with participants.
     */
    @Query("SELECT g FROM GroupBooking g " +
           "LEFT JOIN FETCH g.participants p " +
           "LEFT JOIN FETCH p.user " +
           "LEFT JOIN FETCH p.room " +
           "WHERE g.groupCode = :groupCode")
    Optional<GroupBooking> findByGroupCodeWithParticipants(@Param("groupCode") String groupCode);
}
