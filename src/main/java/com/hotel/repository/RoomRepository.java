package com.hotel.repository;

import com.hotel.domain.entity.Room;
import com.hotel.domain.entity.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Room entity operations.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find room by ID with pessimistic write lock.
     * Prevents concurrent booking of the same room.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> findByIdWithLock(@Param("roomId") Long roomId);

    /**
     * Find all rooms for a hotel.
     */
    List<Room> findByHotel_Id(Long hotelId);

    /**
     * Find available rooms for a hotel.
     */
    List<Room> findByHotel_IdAndIsAvailableTrue(Long hotelId);

    /**
     * Find a room by ID and hotel ID.
     */
    Optional<Room> findByIdAndHotel_Id(Long id, Long hotelId);

    /**
     * Check if a room number exists in a hotel.
     */
    boolean existsByHotel_IdAndRoomNumber(Long hotelId, String roomNumber);

    /**
     * Find rooms by type.
     */
    List<Room> findByRoomTypeAndIsAvailableTrue(RoomType roomType);

    /**
     * Find rooms by hotel and type.
     */
    List<Room> findByHotel_IdAndRoomTypeAndIsAvailableTrue(Long hotelId, RoomType roomType);

    /**
     * Find rooms within a price range.
     */
    List<Room> findByPricePerNightBetweenAndIsAvailableTrue(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find available rooms for a hotel with capacity >= specified.
     */
    List<Room> findByHotel_IdAndCapacityGreaterThanEqualAndIsAvailableTrue(Long hotelId, Integer capacity);

    /**
     * Find available rooms for a hotel within a date range (no overlapping bookings).
     * This is a critical query for the booking system.
     */
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.isAvailable = true " +
           "AND r.id NOT IN (" +
           "SELECT b.room.id FROM Booking b " +
           "WHERE b.room.hotel.id = :hotelId " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND ((b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn)))")
    List<Room> findAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Find available rooms with filters.
     */
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.isAvailable = true " +
           "AND r.capacity >= :guests " +
           "AND (:roomType IS NULL OR r.roomType = :roomType) " +
           "AND r.id NOT IN (" +
           "SELECT b.room.id FROM Booking b " +
           "WHERE b.room.hotel.id = :hotelId " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND ((b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn)))")
    List<Room> findAvailableRoomsWithFilters(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") Integer guests,
            @Param("roomType") RoomType roomType);

    /**
     * Check if a specific room is available for given dates.
     */
    @Query("SELECT CASE WHEN COUNT(b) = 0 THEN true ELSE false END " +
           "FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND ((b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn))")
    boolean isRoomAvailable(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Get room count by hotel.
     */
    long countByHotel_Id(Long hotelId);

    /**
     * Get available room count by hotel.
     */
    long countByHotel_IdAndIsAvailableTrue(Long hotelId);

    /**
     * Find rooms by hotel and room type.
     */
    List<Room> findByHotel_IdAndRoomType(Long hotelId, RoomType roomType);
}
