package com.hotel.mapper;

import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.entity.Booking;
import org.springframework.stereotype.Component;

/**
 * Mapper for Booking entity and DTOs.
 */
@Component
public class BookingMapper {

    /**
     * Convert Booking entity to BookingDto.
     */
    public BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto.BookingDtoBuilder builder = BookingDto.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numGuests(booking.getNumGuests())
                .totalNights(booking.getTotalNights())
                .pricePerNight(booking.getPricePerNight())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .specialRequests(booking.getSpecialRequests())
                .createdAt(booking.getCreatedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason());

        // User info
        if (booking.getUser() != null) {
            builder.userId(booking.getUser().getId())
                    .userEmail(booking.getUser().getEmail())
                    .userFullName(booking.getUser().getFullName());
        }

        // Room info
        if (booking.getRoom() != null) {
            builder.roomId(booking.getRoom().getId())
                    .roomName(booking.getRoom().getName())
                    .roomType(booking.getRoom().getRoomType().name())
                    .roomImageUrl(booking.getRoom().getImageUrl());

            // Hotel info
            if (booking.getRoom().getHotel() != null) {
                builder.hotelId(booking.getRoom().getHotel().getId())
                        .hotelName(booking.getRoom().getHotel().getName())
                        .hotelCity(booking.getRoom().getHotel().getCity())
                        .hotelImageUrl(booking.getRoom().getHotel().getHeroImageUrl());
            }
        }

        return builder.build();
    }
}
