package com.hotel.domain.dto.owner;

import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.hotel.HotelDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for hotel owner dashboard containing hotel info, stats, and recent bookings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDashboardDto {

    // Hotel information
    private HotelDto hotel;

    // Quick stats
    private OwnerStatsDto stats;

    // Today's activity
    private List<BookingDto> todayCheckIns;
    private List<BookingDto> todayCheckOuts;

    // Recent bookings (last 10)
    private List<BookingDto> recentBookings;

    // Room availability summary
    private Integer totalRooms;
    private Integer availableRooms;
    private Integer occupiedRooms;

    // Revenue summary
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
}
