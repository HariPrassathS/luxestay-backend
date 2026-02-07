package com.hotel.repository;

import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Booking entity operations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find a booking by its unique reference.
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Find all bookings for a user.
     */
    List<Booking> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all bookings for a user with pagination.
     */
    Page<Booking> findByUser_Id(Long userId, Pageable pageable);

    /**
     * Find a booking by ID and user ID (for security).
     */
    Optional<Booking> findByIdAndUser_Id(Long id, Long userId);

    /**
     * Find bookings by status.
     */
    List<Booking> findByStatus(BookingStatus status);

    /**
     * Find bookings for a room.
     */
    List<Booking> findByRoom_Id(Long roomId);

    /**
     * Find bookings for a hotel.
     */
    @Query("SELECT b FROM Booking b WHERE b.room.hotel.id = :hotelId ORDER BY b.createdAt DESC")
    List<Booking> findByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Find bookings for a hotel with pagination.
     */
    @Query("SELECT b FROM Booking b WHERE b.room.hotel.id = :hotelId ORDER BY b.createdAt DESC")
    Page<Booking> findByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    /**
     * Find upcoming bookings for a user (check-in date >= today).
     */
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
           "AND b.checkInDate >= :today " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "ORDER BY b.checkInDate ASC")
    List<Booking> findUpcomingBookings(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * Find past bookings for a user.
     */
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
           "AND (b.checkOutDate < :today OR b.status IN ('CANCELLED', 'CHECKED_OUT')) " +
           "ORDER BY b.checkOutDate DESC")
    List<Booking> findPastBookings(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * Check for overlapping bookings for a room.
     * Used to prevent double bookings.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND ((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    boolean hasOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Check for overlapping booking with pessimistic write lock.
     * This prevents race conditions where two concurrent requests could
     * both pass the availability check before either booking is saved.
     * 
     * CONCURRENCY STRATEGY:
     * 1. Acquire exclusive lock on the room's existing bookings
     * 2. Check if any overlap exists
     * 3. If clear, proceed with booking (lock held until transaction commits)
     * 
     * Uses PESSIMISTIC_WRITE to block concurrent booking attempts.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND ((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    List<Booking> findOverlappingBookingsWithLock(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Find overlapping bookings excluding a specific booking (for updates).
     */
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.id != :excludeBookingId " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND ((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    List<Booking> findOverlappingBookingsExcluding(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeBookingId") Long excludeBookingId);

    /**
     * Count bookings by status.
     */
    long countByStatus(BookingStatus status);

    /**
     * Count bookings for a user.
     */
    long countByUser_Id(Long userId);

    /**
     * Get the next booking reference number.
     */
    @Query("SELECT COUNT(b) + 1 FROM Booking b")
    Long getNextBookingNumber();

    /**
     * Find bookings that need check-in today.
     */
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :today " +
           "AND b.status = 'CONFIRMED' ORDER BY b.room.hotel.name")
    List<Booking> findTodayCheckIns(@Param("today") LocalDate today);

    /**
     * Find bookings that need check-out today.
     */
    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :today " +
           "AND b.status = 'CHECKED_IN' ORDER BY b.room.hotel.name")
    List<Booking> findTodayCheckOuts(@Param("today") LocalDate today);

    /**
     * Find bookings that need check-in today for a specific hotel.
     */
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :today " +
           "AND b.room.hotel.id = :hotelId " +
           "AND b.status = 'CONFIRMED' ORDER BY b.createdAt DESC")
    List<Booking> findTodayCheckInsByHotel(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    /**
     * Find bookings that need check-out today for a specific hotel.
     */
    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :today " +
           "AND b.room.hotel.id = :hotelId " +
           "AND b.status = 'CHECKED_IN' ORDER BY b.createdAt DESC")
    List<Booking> findTodayCheckOutsByHotel(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    /**
     * Count bookings by status for a specific hotel.
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.id = :hotelId AND b.status = :status")
    long countByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") BookingStatus status);

    /**
     * Count bookings by check-in date for a specific hotel.
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.id = :hotelId AND b.checkInDate = :date")
    long countByHotelIdAndCheckInDate(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    /**
     * Count bookings by check-out date for a specific hotel.
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.id = :hotelId AND b.checkOutDate = :date")
    long countByHotelIdAndCheckOutDate(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    /**
     * Sum total price for bookings with specific statuses for a hotel.
     */
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.room.hotel.id = :hotelId AND b.status IN :statuses")
    java.math.BigDecimal sumTotalPriceByHotelIdAndStatusIn(@Param("hotelId") Long hotelId, @Param("statuses") List<BookingStatus> statuses);

    /**
     * Count total bookings for a hotel.
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.id = :hotelId")
    long countByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Get all bookings with pagination (admin).
     */
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Search bookings by reference or user email.
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.user.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.user.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Booking> searchBookings(@Param("query") String query, Pageable pageable);

    /**
     * Count bookings by check-in date.
     */
    long countByCheckInDate(LocalDate checkInDate);

    /**
     * Count bookings by check-out date.
     */
    long countByCheckOutDate(LocalDate checkOutDate);

    /**
     * Sum total price for bookings with specific statuses.
     */
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status IN :statuses")
    java.math.BigDecimal sumTotalPriceByStatusIn(@Param("statuses") List<BookingStatus> statuses);
}
