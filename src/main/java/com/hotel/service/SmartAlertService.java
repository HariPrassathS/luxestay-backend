package com.hotel.service;

import com.hotel.domain.dto.alerts.SmartAlertDto;
import com.hotel.domain.dto.alerts.SmartAlertDto.AlertData;
import com.hotel.domain.dto.alerts.SmartAlertDto.AlertPriority;
import com.hotel.domain.dto.alerts.SmartAlertDto.AlertType;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Room;
import com.hotel.domain.entity.User;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Smart Alerts
 * 
 * Generates helpful, non-spammy alerts based on:
 * - User's wishlist
 * - Viewed hotels (session-based)
 * - Price changes
 * - Availability changes
 * 
 * NO fake urgency, NO dark patterns
 * All alerts based on REAL data
 */
@Service
public class SmartAlertService {
    
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    
    // In-memory store for session alerts (simplified - would use Redis in production)
    private final Map<String, List<SmartAlertDto>> sessionAlerts = new HashMap<>();
    
    // Price tracking (simplified - would use a proper price history table in production)
    private final Map<Long, BigDecimal> lastKnownPrices = new HashMap<>();
    
    public SmartAlertService(HotelRepository hotelRepository,
                            RoomRepository roomRepository,
                            BookingRepository bookingRepository,
                            UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Get active alerts for a session (unauthenticated or authenticated)
     */
    public List<SmartAlertDto> getSessionAlerts(String sessionId, List<Long> viewedHotelIds) {
        List<SmartAlertDto> alerts = new ArrayList<>();
        
        // Generate alerts for recently viewed hotels
        if (viewedHotelIds != null && !viewedHotelIds.isEmpty()) {
            alerts.addAll(generateViewedHotelAlerts(viewedHotelIds));
        }
        
        // Filter out expired alerts
        return alerts.stream()
            .filter(a -> a.getExpiresAt() == null || a.getExpiresAt().isAfter(LocalDateTime.now()))
            .sorted(Comparator.comparing(a -> -a.getPriority().getLevel()))
            .limit(5) // Max 5 alerts to avoid overwhelming
            .collect(Collectors.toList());
    }
    
    /**
     * Get alerts for authenticated user (includes wishlist-based alerts)
     */
    public List<SmartAlertDto> getUserAlerts(Long userId) {
        List<SmartAlertDto> alerts = new ArrayList<>();
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return alerts;
        }
        
        // Get user's bookings to understand their preferences
        List<Booking> userBookings = bookingRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        
        // Generate alerts based on user activity
        alerts.addAll(generateBookingBasedAlerts(userId, userBookings));
        
        // Filter and sort
        return alerts.stream()
            .filter(a -> !a.isDismissed())
            .filter(a -> a.getExpiresAt() == null || a.getExpiresAt().isAfter(LocalDateTime.now()))
            .sorted(Comparator.comparing(a -> -a.getPriority().getLevel()))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * Generate alerts for hotels based on availability
     * Called when user provides specific hotel IDs and dates
     */
    public List<SmartAlertDto> getAvailabilityAlerts(List<Long> hotelIds, 
                                                     LocalDate checkIn, 
                                                     LocalDate checkOut) {
        List<SmartAlertDto> alerts = new ArrayList<>();
        
        for (Long hotelId : hotelIds) {
            Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
            if (hotelOpt.isEmpty()) continue;
            
            Hotel hotel = hotelOpt.get();
            List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
            
            // Count available rooms for the dates
            int availableCount = countAvailableRooms(rooms, checkIn, checkOut);
            
            if (availableCount > 0 && availableCount <= 3) {
                // Low availability alert
                SmartAlertDto alert = new SmartAlertDto();
                alert.setType(AlertType.LOW_AVAILABILITY);
                alert.setPriority(availableCount == 1 ? AlertPriority.HIGH : AlertPriority.MEDIUM);
                alert.setTitle("Limited availability at " + hotel.getName());
                alert.setMessage(availableCount == 1 
                    ? "Only 1 room left for your dates!" 
                    : "Only " + availableCount + " rooms left for your dates");
                
                AlertData data = new AlertData();
                data.setHotelId(hotelId);
                data.setHotelName(hotel.getName());
                data.setHotelImage(hotel.getHeroImageUrl());
                data.setCheckInDate(checkIn);
                data.setCheckOutDate(checkOut);
                data.setAvailableRooms(availableCount);
                data.setActionUrl(String.format("hotel-detail.html?id=%d&checkIn=%s&checkOut=%s",
                    hotelId, checkIn, checkOut));
                data.setActionText("View Rooms");
                alert.setData(data);
                
                alert.setCreatedAt(LocalDateTime.now());
                alert.setExpiresAt(checkIn.atStartOfDay());
                
                alerts.add(alert);
            }
        }
        
        return alerts;
    }
    
    /**
     * Check for price drops on a specific hotel
     */
    public Optional<SmartAlertDto> checkPriceDrop(Long hotelId, Long userId) {
        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        if (rooms.isEmpty()) return Optional.empty();
        
        // Get cheapest room
        Room cheapestRoom = rooms.stream()
            .min(Comparator.comparing(Room::getPricePerNight))
            .orElse(null);
        
        if (cheapestRoom == null) return Optional.empty();
        
        BigDecimal currentPrice = cheapestRoom.getPricePerNight();
        BigDecimal lastPrice = lastKnownPrices.get(hotelId);
        
        // Store current price for future comparison
        lastKnownPrices.put(hotelId, currentPrice);
        
        // If we have a previous price and it's higher, generate alert
        if (lastPrice != null && lastPrice.compareTo(currentPrice) > 0) {
            BigDecimal savings = lastPrice.subtract(currentPrice);
            double savingsPercent = savings.divide(lastPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
            
            // Only alert for significant discounts (5% or more)
            if (savingsPercent >= 5) {
                Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
                if (hotelOpt.isPresent()) {
                    return Optional.of(SmartAlertDto.priceDropAlert(
                        userId, hotelId, hotelOpt.get().getName(), lastPrice, currentPrice
                    ));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Dismiss an alert
     */
    public void dismissAlert(String sessionId, Long alertId) {
        List<SmartAlertDto> alerts = sessionAlerts.get(sessionId);
        if (alerts != null) {
            alerts.stream()
                .filter(a -> a.getId() != null && a.getId().equals(alertId))
                .forEach(a -> a.setDismissed(true));
        }
    }
    
    /**
     * Mark alert as read
     */
    public void markAsRead(String sessionId, Long alertId) {
        List<SmartAlertDto> alerts = sessionAlerts.get(sessionId);
        if (alerts != null) {
            alerts.stream()
                .filter(a -> a.getId() != null && a.getId().equals(alertId))
                .forEach(a -> a.setRead(true));
        }
    }
    
    // Private helper methods
    
    private List<SmartAlertDto> generateViewedHotelAlerts(List<Long> hotelIds) {
        List<SmartAlertDto> alerts = new ArrayList<>();
        
        // Limit to most recent 3 hotels
        List<Long> recentHotels = hotelIds.size() > 3 
            ? hotelIds.subList(hotelIds.size() - 3, hotelIds.size()) 
            : hotelIds;
        
        for (Long hotelId : recentHotels) {
            Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
            if (hotelOpt.isEmpty()) continue;
            
            Hotel hotel = hotelOpt.get();
            List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
            
            if (rooms.isEmpty()) continue;
            
            // Find cheapest available room
            Optional<Room> cheapestRoom = rooms.stream()
                .min(Comparator.comparing(Room::getPricePerNight));
            
            if (cheapestRoom.isPresent()) {
                Room room = cheapestRoom.get();
                
                // Generate a "continue browsing" type alert
                SmartAlertDto alert = new SmartAlertDto();
                alert.setType(AlertType.WISHLIST);
                alert.setPriority(AlertPriority.LOW);
                alert.setTitle("Still interested in " + hotel.getName() + "?");
                alert.setMessage("Rooms start from $" + room.getPricePerNight().intValue() + "/night");
                
                AlertData data = new AlertData();
                data.setHotelId(hotelId);
                data.setHotelName(hotel.getName());
                data.setHotelImage(hotel.getHeroImageUrl());
                data.setCurrentPrice(room.getPricePerNight());
                data.setActionUrl("hotel-detail.html?id=" + hotelId);
                data.setActionText("View Hotel");
                alert.setData(data);
                
                alert.setCreatedAt(LocalDateTime.now());
                alert.setExpiresAt(LocalDateTime.now().plusDays(1));
                
                alerts.add(alert);
            }
        }
        
        return alerts;
    }
    
    private List<SmartAlertDto> generateBookingBasedAlerts(Long userId, List<Booking> bookings) {
        List<SmartAlertDto> alerts = new ArrayList<>();
        
        // Find pending bookings that are coming up
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        
        for (Booking booking : bookings) {
            if (booking.getStatus() != null && 
                "CONFIRMED".equals(booking.getStatus().name())) {
                
                LocalDate checkIn = booking.getCheckInDate();
                if (checkIn != null && !checkIn.isBefore(today) && !checkIn.isAfter(nextWeek)) {
                    // Upcoming stay reminder
                    SmartAlertDto alert = new SmartAlertDto();
                    alert.setUserId(userId);
                    alert.setType(AlertType.BOOKING_REMINDER);
                    alert.setPriority(AlertPriority.MEDIUM);
                    alert.setTitle("Your stay is coming up!");
                    
                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, checkIn);
                    Hotel bookingHotel = booking.getRoom().getHotel();
                    alert.setMessage(daysUntil == 0 
                        ? "Check-in today at " + bookingHotel.getName()
                        : daysUntil + " days until your stay at " + bookingHotel.getName());
                    
                    AlertData data = new AlertData();
                    data.setHotelId(bookingHotel.getId());
                    data.setHotelName(bookingHotel.getName());
                    data.setCheckInDate(checkIn);
                    data.setCheckOutDate(booking.getCheckOutDate());
                    data.setActionUrl("my-bookings.html");
                    data.setActionText("View Booking");
                    alert.setData(data);
                    
                    alert.setCreatedAt(LocalDateTime.now());
                    alert.setExpiresAt(checkIn.atTime(23, 59));
                    
                    alerts.add(alert);
                }
            }
        }
        
        return alerts;
    }
    
    private int countAvailableRooms(List<Room> rooms, LocalDate checkIn, LocalDate checkOut) {
        int available = 0;
        
        for (Room room : rooms) {
            List<Booking> roomBookings = bookingRepository.findByRoom_Id(room.getId());
            
            boolean isBooked = roomBookings.stream()
                .filter(b -> b.getStatus() != null && 
                    ("CONFIRMED".equals(b.getStatus().name()) || "PENDING".equals(b.getStatus().name())))
                .anyMatch(b -> {
                    LocalDate bCheckIn = b.getCheckInDate();
                    LocalDate bCheckOut = b.getCheckOutDate();
                    if (bCheckIn == null || bCheckOut == null) return false;
                    // Check for overlap
                    return !(checkOut.isBefore(bCheckIn) || checkIn.isAfter(bCheckOut.minusDays(1)));
                });
            
            if (!isBooked) {
                available++;
            }
        }
        
        return available;
    }
}
