package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Booking entity representing a room reservation.
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 20)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "num_guests", nullable = false)
    @lombok.Builder.Default
    private Integer numGuests = 1;

    @Column(name = "total_nights", nullable = false)
    private Integer totalNights;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        if (numGuests == null) {
            numGuests = 1;
        }
        calculateTotalNights();
        calculateTotalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Helper Methods ====================

    /**
     * Calculate the number of nights between check-in and check-out.
     */
    public void calculateTotalNights() {
        if (checkInDate != null && checkOutDate != null) {
            this.totalNights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
    }

    /**
     * Calculate the total price based on nights and price per night.
     */
    public void calculateTotalPrice() {
        if (totalNights != null && pricePerNight != null) {
            this.totalPrice = pricePerNight.multiply(BigDecimal.valueOf(totalNights));
        }
    }

    /**
     * Cancel the booking with a reason.
     */
    public void cancel(String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Confirm the booking.
     */
    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    /**
     * Check if the booking can be cancelled.
     */
    public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    /**
     * Get the hotel name through the room relationship.
     */
    public String getHotelName() {
        return room != null && room.getHotel() != null ? room.getHotel().getName() : null;
    }

    /**
     * Get the room name.
     */
    public String getRoomName() {
        return room != null ? room.getName() : null;
    }

    /**
     * Get the user's email.
     */
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }

    /**
     * Get the user's full name.
     */
    public String getUserFullName() {
        return user != null ? user.getFullName() : null;
    }
}
