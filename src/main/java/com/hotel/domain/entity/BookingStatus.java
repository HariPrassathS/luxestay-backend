package com.hotel.domain.entity;

/**
 * Enumeration representing the status of a booking.
 */
public enum BookingStatus {
    PENDING,     // Booking created but not yet confirmed
    CONFIRMED,   // Booking confirmed and guaranteed
    CHECKED_IN,  // Guest has arrived and checked in
    CHECKED_OUT, // Guest has completed stay and checked out
    CANCELLED    // Booking was cancelled
}
