package com.hotel.service;

import com.hotel.domain.dto.pulse.LivePulseDto;
import com.hotel.domain.dto.pulse.LivePulseDto.ActivityItem;
import com.hotel.domain.dto.pulse.LivePulseDto.PulseLevel;
import com.hotel.domain.dto.pulse.LivePulseDto.TrendDirection;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for calculating Hotel Live Pulse metrics
 * 
 * All calculations based on REAL data:
 * - Actual bookings (not fabricated)
 * - Real room availability
 * - Genuine trends
 * 
 * NO fake urgency, NO dark patterns
 */
@Service
public class LivePulseService {
    
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    
    // Cache for pulse data (10 minutes TTL to reduce DB load)
    private final Map<Long, CachedPulse> pulseCache = new HashMap<>();
    private static final long CACHE_TTL_MINUTES = 10;
    
    public LivePulseService(BookingRepository bookingRepository,
                           HotelRepository hotelRepository,
                           RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
    }
    
    /**
     * Get live pulse data for a hotel
     * Includes caching to reduce database load
     */
    public LivePulseDto getHotelPulse(Long hotelId) {
        // Check cache first
        CachedPulse cached = pulseCache.get(hotelId);
        if (cached != null && !cached.isExpired()) {
            return cached.pulse;
        }
        
        // Calculate fresh pulse data
        LivePulseDto pulse = calculatePulse(hotelId);
        
        // Cache the result
        pulseCache.put(hotelId, new CachedPulse(pulse));
        
        return pulse;
    }
    
    /**
     * Get quick pulse badge (minimal data for list views)
     */
    public LivePulseDto getQuickPulse(Long hotelId) {
        LivePulseDto fullPulse = getHotelPulse(hotelId);
        return LivePulseDto.quickBadge(
            hotelId,
            fullPulse.getPulseLevel(),
            fullPulse.getRecentBookings24h()
        );
    }
    
    /**
     * Calculate pulse metrics from real data
     */
    private LivePulseDto calculatePulse(Long hotelId) {
        LivePulseDto pulse = LivePulseDto.forHotel(hotelId);
        
        // Verify hotel exists
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
        if (hotelOpt.isEmpty()) {
            pulse.setPulseLevel(PulseLevel.QUIET);
            return pulse;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusHours(24);
        LocalDateTime lastWeek = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);
        
        // Get rooms for this hotel
        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        pulse.setTotalRooms(rooms.size());
        
        // Get recent bookings from database
        List<Booking> allBookings = bookingRepository.findByHotelId(hotelId);
        
        // Calculate metrics
        int bookings24h = countBookingsSince(allBookings, yesterday);
        int bookings7d = countBookingsSince(allBookings, lastWeek);
        int bookingsPrevWeek = countBookingsBetween(allBookings, twoWeeksAgo, lastWeek);
        
        pulse.setRecentBookings24h(bookings24h);
        pulse.setRecentBookings7d(bookings7d);
        
        // Calculate availability (simplified - rooms with no conflicting bookings for today)
        LocalDate today = LocalDate.now();
        int availableRooms = calculateAvailableRooms(rooms, allBookings, today);
        pulse.setAvailableRooms(availableRooms);
        
        // Calculate occupancy rate
        if (rooms.size() > 0) {
            double occupancy = ((double)(rooms.size() - availableRooms) / rooms.size()) * 100;
            pulse.setOccupancyRate(Math.round(occupancy * 10) / 10.0);
        }
        
        // Determine booking trend
        TrendDirection trend = calculateTrend(bookings7d, bookingsPrevWeek);
        pulse.setBookingTrend(trend);
        
        // Determine pulse level based on activity
        PulseLevel level = calculatePulseLevel(bookings24h, bookings7d, availableRooms, rooms.size());
        pulse.setPulseLevel(level);
        
        // Build recent activity feed (anonymized)
        List<ActivityItem> recentActivity = buildActivityFeed(allBookings, now);
        pulse.setRecentActivity(recentActivity);
        
        // Check if popular
        if (level == PulseLevel.POPULAR || level == PulseLevel.TRENDING) {
            pulse.setPopular(true);
            pulse.setPopularReason(generatePopularReason(bookings24h, bookings7d, trend));
        }
        
        return pulse;
    }
    
    /**
     * Count bookings since a given timestamp
     */
    private int countBookingsSince(List<Booking> bookings, LocalDateTime since) {
        return (int) bookings.stream()
            .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isAfter(since))
            .count();
    }
    
    /**
     * Count bookings between two timestamps
     */
    private int countBookingsBetween(List<Booking> bookings, LocalDateTime start, LocalDateTime end) {
        return (int) bookings.stream()
            .filter(b -> b.getCreatedAt() != null)
            .filter(b -> b.getCreatedAt().isAfter(start) && b.getCreatedAt().isBefore(end))
            .count();
    }
    
    /**
     * Calculate available rooms for a given date
     * (simplified version - checks for overlapping bookings)
     */
    private int calculateAvailableRooms(List<Room> rooms, List<Booking> bookings, LocalDate date) {
        int bookedRoomCount = 0;
        
        for (Room room : rooms) {
            boolean isBooked = bookings.stream()
                .filter(b -> b.getRoom() != null && b.getRoom().getId().equals(room.getId()))
                .filter(b -> b.getStatus() != null && 
                    (b.getStatus().name().equals("CONFIRMED") || b.getStatus().name().equals("PENDING")))
                .anyMatch(b -> {
                    if (b.getCheckInDate() == null || b.getCheckOutDate() == null) return false;
                    return !date.isBefore(b.getCheckInDate()) && !date.isAfter(b.getCheckOutDate().minusDays(1));
                });
            
            if (isBooked) bookedRoomCount++;
        }
        
        return Math.max(0, rooms.size() - bookedRoomCount);
    }
    
    /**
     * Calculate booking trend
     */
    private TrendDirection calculateTrend(int currentWeek, int previousWeek) {
        if (currentWeek > previousWeek * 1.2) {
            return TrendDirection.UP;
        } else if (currentWeek < previousWeek * 0.8) {
            return TrendDirection.DOWN;
        }
        return TrendDirection.STABLE;
    }
    
    /**
     * Determine pulse level based on activity metrics
     * Calm, honest thresholds - no fake urgency
     */
    private PulseLevel calculatePulseLevel(int bookings24h, int bookings7d, int available, int total) {
        // High recent activity
        if (bookings24h >= 5 || (bookings7d >= 20 && bookings24h >= 2)) {
            return PulseLevel.TRENDING;
        }
        
        // Good activity and limited availability
        if (bookings24h >= 3 || (bookings7d >= 10 && available <= total * 0.3)) {
            return PulseLevel.POPULAR;
        }
        
        // Moderate activity
        if (bookings24h >= 1 || bookings7d >= 5) {
            return PulseLevel.ACTIVE;
        }
        
        // Some activity
        if (bookings7d >= 2) {
            return PulseLevel.STEADY;
        }
        
        return PulseLevel.QUIET;
    }
    
    /**
     * Build activity feed with anonymized recent bookings
     * No personal info, no dark patterns
     */
    private List<ActivityItem> buildActivityFeed(List<Booking> bookings, LocalDateTime now) {
        List<ActivityItem> activities = new ArrayList<>();
        
        // Get recent confirmed bookings (last 48 hours for natural activity)
        bookings.stream()
            .filter(b -> b.getCreatedAt() != null)
            .filter(b -> b.getCreatedAt().isAfter(now.minusHours(48)))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .forEach(booking -> {
                String timeAgo = formatTimeAgo(booking.getCreatedAt(), now);
                String message = "Someone booked a room";
                
                // Add room type if available (anonymized)
                if (booking.getRoom() != null && booking.getRoom().getRoomType() != null) {
                    String roomType = booking.getRoom().getRoomType().name().toLowerCase()
                        .replace("_", " ");
                    message = "A " + roomType + " was booked";
                }
                
                activities.add(new ActivityItem(
                    "BOOKING",
                    message,
                    timeAgo,
                    booking.getCreatedAt()
                ));
            });
        
        return activities;
    }
    
    /**
     * Format time ago in human-friendly way
     */
    private String formatTimeAgo(LocalDateTime timestamp, LocalDateTime now) {
        Duration duration = Duration.between(timestamp, now);
        
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes <= 1 ? "just now" : minutes + " minutes ago";
        }
        
        long hours = duration.toHours();
        if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        }
        
        long days = duration.toDays();
        return days == 1 ? "yesterday" : days + " days ago";
    }
    
    /**
     * Generate reason for popularity (honest, not pushy)
     */
    private String generatePopularReason(int bookings24h, int bookings7d, TrendDirection trend) {
        if (bookings24h >= 5) {
            return "Very active today";
        } else if (trend == TrendDirection.UP) {
            return "Gaining popularity";
        } else if (bookings7d >= 15) {
            return "Consistently popular";
        } else if (bookings24h >= 2) {
            return "Active today";
        }
        return "Popular choice";
    }
    
    /**
     * Cache wrapper with TTL
     */
    private static class CachedPulse {
        final LivePulseDto pulse;
        final LocalDateTime cachedAt;
        
        CachedPulse(LivePulseDto pulse) {
            this.pulse = pulse;
            this.cachedAt = LocalDateTime.now();
        }
        
        boolean isExpired() {
            return Duration.between(cachedAt, LocalDateTime.now()).toMinutes() >= CACHE_TTL_MINUTES;
        }
    }
}
