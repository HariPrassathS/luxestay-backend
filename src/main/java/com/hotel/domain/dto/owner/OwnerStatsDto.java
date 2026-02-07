package com.hotel.domain.dto.owner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for hotel owner statistics and analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerStatsDto {

    // Booking metrics
    private Long totalBookings;
    private Long confirmedBookings;
    private Long pendingBookings;
    private Long cancelledBookings;
    private Long completedBookings;

    // Today's stats
    private Long todayCheckIns;
    private Long todayCheckOuts;
    private Long todayNewBookings;

    // Room metrics
    private Integer totalRooms;
    private Integer availableRooms;
    private Double occupancyRate; // Percentage 0-100

    // Revenue metrics
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal averageBookingValue;

    // Guest metrics
    private Long totalGuests;
    private Long repeatGuests;
    private Double averageStayDuration; // Days
}
