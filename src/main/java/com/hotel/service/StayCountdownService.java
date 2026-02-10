package com.hotel.service;

import com.hotel.domain.dto.countdown.StayCountdownDto;
import com.hotel.domain.dto.countdown.StayCountdownDto.*;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.BookingStatus;
import com.hotel.domain.entity.Hotel;
import com.hotel.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for Stay Countdown - Journey Progress & Milestones
 * 
 * Provides:
 * - Countdown to check-in
 * - Journey milestones
 * - Preparation reminders
 * - Weather previews (based on destination/season)
 * - Packing suggestions
 * 
 * Builds excitement and helps guests prepare
 */
@Service
public class StayCountdownService {
    
    private final BookingRepository bookingRepository;
    
    // Excitement messages based on days remaining
    private static final Map<Integer, String> EXCITEMENT_MESSAGES = new LinkedHashMap<>();
    static {
        EXCITEMENT_MESSAGES.put(0, "🎉 Your stay begins today! Have an amazing time!");
        EXCITEMENT_MESSAGES.put(1, "🌟 Tomorrow is the big day! Get some rest tonight.");
        EXCITEMENT_MESSAGES.put(2, "✨ Just 2 more sleeps until your adventure!");
        EXCITEMENT_MESSAGES.put(3, "🎒 3 days to go! Time to finalize your packing.");
        EXCITEMENT_MESSAGES.put(7, "📅 One week countdown! The excitement is building.");
        EXCITEMENT_MESSAGES.put(14, "🗓️ Two weeks away! Perfect time to plan activities.");
        EXCITEMENT_MESSAGES.put(30, "📆 One month until your getaway! Start dreaming.");
    }
    
    public StayCountdownService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    
    /**
     * Get countdown details for a specific booking
     */
    public StayCountdownDto getBookingCountdown(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return null;
        }
        
        Booking booking = bookingOpt.get();
        Hotel hotel = booking.getRoom().getHotel();
        
        // Create countdown DTO
        StayCountdownDto dto = StayCountdownDto.forBooking(
            booking.getId(),
            hotel.getId(),
            hotel.getName(),
            booking.getCheckInDate(),
            booking.getCheckOutDate()
        );
        
        dto.setHotelCity(hotel.getCity());
        if (hotel.getHeroImageUrl() != null && !hotel.getHeroImageUrl().isEmpty()) {
            dto.setHotelImageUrl(hotel.getHeroImageUrl());
        }
        
        // Calculate days and nights
        LocalDate today = LocalDate.now();
        LocalDate checkIn = booking.getCheckInDate();
        LocalDate checkOut = booking.getCheckOutDate();
        
        int daysUntilCheckIn = (int) ChronoUnit.DAYS.between(today, checkIn);
        int nightsBooked = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        
        dto.setDaysUntilCheckIn(daysUntilCheckIn);
        dto.setNightsBooked(nightsBooked);
        
        // Determine phase
        CountdownPhase phase = determinePhase(daysUntilCheckIn, today, checkIn, checkOut);
        dto.setPhase(phase);
        dto.setPhaseMessage(phase.getMessage());
        
        // Set excitement message
        String excitementMessage = getExcitementMessage(daysUntilCheckIn);
        dto.setExcitementMessage(excitementMessage);
        
        // Build milestones
        List<Milestone> milestones = buildMilestones(daysUntilCheckIn);
        dto.setMilestones(milestones);
        
        // Find next milestone
        Milestone nextMilestone = findNextMilestone(milestones, daysUntilCheckIn);
        dto.setNextMilestone(nextMilestone);
        
        // Count completed milestones
        int completed = (int) milestones.stream().filter(Milestone::isCompleted).count();
        dto.setCompletedMilestones(completed);
        
        // Add weather preview (based on season/location)
        WeatherPreview weather = getWeatherPreview(hotel.getCity(), checkIn);
        dto.setWeatherPreview(weather);
        
        // Add packing suggestions
        List<PackingSuggestion> packingSuggestions = getPackingSuggestions(hotel.getCity(), checkIn);
        dto.setPackingSuggestions(packingSuggestions);
        
        return dto;
    }
    
    /**
     * Get all active countdowns for a user
     */
    public List<StayCountdownDto> getUserCountdowns(Long userId) {
        List<Booking> upcomingBookings = bookingRepository.findUpcomingBookings(
            userId, LocalDate.now()
        );
        
        List<StayCountdownDto> countdowns = new ArrayList<>();
        for (Booking booking : upcomingBookings) {
            // Only include confirmed bookings
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                StayCountdownDto countdown = getBookingCountdown(booking.getId());
                if (countdown != null) {
                    countdowns.add(countdown);
                }
            }
        }
        
        // Sort by check-in date (soonest first)
        countdowns.sort(Comparator.comparing(StayCountdownDto::getCheckInDate));
        
        return countdowns;
    }
    
    /**
     * Determine countdown phase based on days remaining
     */
    private CountdownPhase determinePhase(int daysUntilCheckIn, LocalDate today, 
                                          LocalDate checkIn, LocalDate checkOut) {
        if (today.isAfter(checkOut) || today.isEqual(checkOut)) {
            return CountdownPhase.COMPLETED;
        }
        
        if (today.isAfter(checkIn) || today.isEqual(checkIn)) {
            if (today.isBefore(checkOut)) {
                return CountdownPhase.IN_PROGRESS;
            }
        }
        
        if (daysUntilCheckIn <= 0) {
            return CountdownPhase.TODAY;
        } else if (daysUntilCheckIn == 1) {
            return CountdownPhase.TOMORROW;
        } else if (daysUntilCheckIn <= 7) {
            return CountdownPhase.DAYS_AWAY;
        } else if (daysUntilCheckIn <= 30) {
            return CountdownPhase.WEEKS_AWAY;
        } else if (daysUntilCheckIn <= 90) {
            return CountdownPhase.FAR_AWAY;
        } else {
            return CountdownPhase.JUST_BOOKED;
        }
    }
    
    /**
     * Get excitement message for days remaining
     */
    private String getExcitementMessage(int daysUntilCheckIn) {
        if (daysUntilCheckIn < 0) {
            return "We hope you're enjoying your stay! ✨";
        }
        
        // Find the closest matching message
        for (Map.Entry<Integer, String> entry : EXCITEMENT_MESSAGES.entrySet()) {
            if (daysUntilCheckIn <= entry.getKey()) {
                return entry.getValue();
            }
        }
        
        return "Your dream vacation awaits! 🌴";
    }
    
    /**
     * Build milestone list
     */
    private List<Milestone> buildMilestones(int daysUntilCheckIn) {
        List<Milestone> milestones = new ArrayList<>();
        
        // Booking confirmed milestone (always completed)
        Milestone booking = new Milestone(
            "booking",
            "Booking Confirmed",
            "Your reservation is secured",
            "✓",
            Integer.MAX_VALUE,
            Milestone.MilestoneType.BOOKING
        );
        booking.setCompleted(true);
        milestones.add(booking);
        
        // 30 days milestone
        if (daysUntilCheckIn <= 30) {
            Milestone oneMonth = new Milestone(
                "one_month",
                "One Month Away",
                "Start planning activities",
                "📅",
                30,
                Milestone.MilestoneType.PREPARATION
            );
            oneMonth.setCompleted(true);
            milestones.add(oneMonth);
        }
        
        // 14 days milestone
        if (daysUntilCheckIn <= 14) {
            Milestone twoWeeks = new Milestone(
                "two_weeks",
                "Two Weeks Away",
                "Review your travel plans",
                "🗓️",
                14,
                Milestone.MilestoneType.REMINDER
            );
            twoWeeks.setCompleted(true);
            milestones.add(twoWeeks);
        }
        
        // 7 days milestone
        Milestone oneWeek = new Milestone(
            "one_week",
            "One Week Countdown",
            "Time to start packing",
            "📦",
            7,
            Milestone.MilestoneType.PREPARATION
        );
        oneWeek.setCompleted(daysUntilCheckIn <= 7);
        milestones.add(oneWeek);
        
        // 3 days milestone
        Milestone threeDays = new Milestone(
            "three_days",
            "Almost There",
            "Final preparations",
            "🎒",
            3,
            Milestone.MilestoneType.EXCITEMENT
        );
        threeDays.setCompleted(daysUntilCheckIn <= 3);
        milestones.add(threeDays);
        
        // 1 day milestone
        Milestone tomorrow = new Milestone(
            "tomorrow",
            "Tomorrow!",
            "Get a good night's rest",
            "🌙",
            1,
            Milestone.MilestoneType.EXCITEMENT
        );
        tomorrow.setCompleted(daysUntilCheckIn <= 1);
        milestones.add(tomorrow);
        
        // Check-in day
        Milestone checkInDay = new Milestone(
            "checkin",
            "Check-in Day",
            "Your adventure begins!",
            "🎉",
            0,
            Milestone.MilestoneType.ARRIVAL
        );
        checkInDay.setCompleted(daysUntilCheckIn <= 0);
        milestones.add(checkInDay);
        
        return milestones;
    }
    
    /**
     * Find the next uncompleted milestone
     */
    private Milestone findNextMilestone(List<Milestone> milestones, int daysUntilCheckIn) {
        for (Milestone m : milestones) {
            if (!m.isCompleted() && daysUntilCheckIn > m.getDaysBeforeCheckIn()) {
                return m;
            }
        }
        return null;
    }
    
    /**
     * Get weather preview based on destination and date
     * This uses seasonal averages - in production, would use a weather API
     */
    private WeatherPreview getWeatherPreview(String city, LocalDate checkIn) {
        Month month = checkIn.getMonth();
        
        // Default to temperate weather
        int avgHigh = 72;
        int avgLow = 55;
        String condition = "Pleasant";
        String icon = "☀️";
        String recommendation = "Great weather for sightseeing!";
        
        // Season-based adjustments (Northern Hemisphere assumptions)
        switch (month) {
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
                avgHigh = 45;
                avgLow = 32;
                condition = "Cold";
                icon = "❄️";
                recommendation = "Pack warm layers and a heavy coat";
                break;
            case MARCH:
            case APRIL:
                avgHigh = 60;
                avgLow = 42;
                condition = "Mild";
                icon = "🌤️";
                recommendation = "Bring layers for variable weather";
                break;
            case MAY:
                avgHigh = 70;
                avgLow = 52;
                condition = "Pleasant";
                icon = "☀️";
                recommendation = "Perfect weather for outdoor activities";
                break;
            case JUNE:
            case JULY:
            case AUGUST:
                avgHigh = 85;
                avgLow = 68;
                condition = "Warm";
                icon = "🌞";
                recommendation = "Stay hydrated and bring sunscreen";
                break;
            case SEPTEMBER:
            case OCTOBER:
                avgHigh = 68;
                avgLow = 50;
                condition = "Mild";
                icon = "🍂";
                recommendation = "Great weather for exploring";
                break;
            case NOVEMBER:
                avgHigh = 52;
                avgLow = 38;
                condition = "Cool";
                icon = "🌥️";
                recommendation = "Bring a warm jacket";
                break;
        }
        
        // City-specific adjustments
        if (city != null) {
            String cityLower = city.toLowerCase();
            if (cityLower.contains("miami") || cityLower.contains("florida")) {
                avgHigh += 15;
                avgLow += 15;
                condition = month.getValue() >= 6 && month.getValue() <= 9 ? "Hot & Humid" : "Warm";
                icon = "🌴";
                recommendation = "Pack light clothing and swim gear!";
            } else if (cityLower.contains("denver") || cityLower.contains("colorado")) {
                avgHigh -= 5;
                avgLow -= 10;
                recommendation = "Mountain weather can change quickly - layer up!";
            } else if (cityLower.contains("seattle") || cityLower.contains("portland")) {
                condition = "Rainy";
                icon = "🌧️";
                recommendation = "Don't forget your rain jacket!";
            }
        }
        
        return new WeatherPreview(condition, icon, avgHigh, avgLow, recommendation);
    }
    
    /**
     * Get packing suggestions based on destination and season
     */
    private List<PackingSuggestion> getPackingSuggestions(String city, LocalDate checkIn) {
        List<PackingSuggestion> suggestions = new ArrayList<>();
        Month month = checkIn.getMonth();
        
        // Essential items (always recommend)
        suggestions.add(new PackingSuggestion(
            "ID & Booking Confirmation",
            "Required for check-in",
            "Documents",
            true
        ));
        
        suggestions.add(new PackingSuggestion(
            "Phone Charger",
            "Stay connected",
            "Electronics",
            true
        ));
        
        // Season-specific
        if (month.getValue() >= 6 && month.getValue() <= 8) {
            suggestions.add(new PackingSuggestion(
                "Sunscreen",
                "Protect your skin",
                "Personal Care",
                true
            ));
            suggestions.add(new PackingSuggestion(
                "Sunglasses",
                "For sunny days",
                "Accessories",
                false
            ));
            suggestions.add(new PackingSuggestion(
                "Light clothing",
                "Stay cool in summer weather",
                "Clothing",
                true
            ));
        } else if (month.getValue() >= 12 || month.getValue() <= 2) {
            suggestions.add(new PackingSuggestion(
                "Warm coat",
                "Essential for winter weather",
                "Clothing",
                true
            ));
            suggestions.add(new PackingSuggestion(
                "Warm layers",
                "Sweaters and long sleeves",
                "Clothing",
                true
            ));
            suggestions.add(new PackingSuggestion(
                "Hat & Gloves",
                "Keep extremities warm",
                "Accessories",
                false
            ));
        } else {
            suggestions.add(new PackingSuggestion(
                "Light jacket",
                "For cooler evenings",
                "Clothing",
                true
            ));
        }
        
        // Destination-specific
        if (city != null) {
            String cityLower = city.toLowerCase();
            if (cityLower.contains("beach") || cityLower.contains("miami") || 
                cityLower.contains("caribbean") || cityLower.contains("hawaii")) {
                suggestions.add(new PackingSuggestion(
                    "Swimsuit",
                    "For beach and pool",
                    "Clothing",
                    true
                ));
                suggestions.add(new PackingSuggestion(
                    "Beach towel",
                    "For beach outings",
                    "Accessories",
                    false
                ));
            }
        }
        
        return suggestions;
    }
}
