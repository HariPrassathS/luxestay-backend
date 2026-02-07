package com.hotel.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for admin dashboard statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDto {
    private long totalHotels;
    private long totalRooms;
    private long totalBookings;
    private long totalUsers;
    private long pendingBookings;
    private long confirmedBookings;
    private long checkedInToday;
    private long checkingOutToday;
    private BigDecimal totalRevenue;
}
