package com.hotel.repository;

import com.hotel.domain.entity.BookingPayment;
import com.hotel.domain.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BookingPayment entity operations.
 */
@Repository
public interface BookingPaymentRepository extends JpaRepository<BookingPayment, Long> {

    /**
     * Find all payments for a booking.
     */
    List<BookingPayment> findByBookingId(Long bookingId);

    /**
     * Find payments by status.
     */
    List<BookingPayment> findByPaymentStatus(PaymentStatus status);

    /**
     * Find a payment by transaction ID.
     */
    Optional<BookingPayment> findByTransactionId(String transactionId);

    /**
     * Check if a booking has a completed payment.
     */
    boolean existsByBookingIdAndPaymentStatus(Long bookingId, PaymentStatus status);
}
