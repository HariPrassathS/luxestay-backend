package com.luxestay.service;

import com.luxestay.dto.MemoryLaneDto;
import com.luxestay.dto.MemoryLaneDto.*;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Review;
import com.hotel.domain.entity.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Memory Lane Service
 * "Remember your stay at..." Past Stay Memories
 * 
 * Week 11 Feature: Nostalgia triggers for completed bookings
 */
@Service
public class MemoryLaneService {
    
    private static final Logger logger = LoggerFactory.getLogger(MemoryLaneService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    /**
     * Get user's memory lane - all past completed stays
     */
    @Cacheable(value = "memoryLane", key = "#userId", unless = "#result == null || #result.memories.isEmpty()")
    public MemoryLaneResponse getMemoryLane(Long userId) {
        logger.info("Loading memory lane for user: {}", userId);
        
        // Get all past bookings for user (completed or past checkout date)
        LocalDate today = LocalDate.now();
        List<Booking> completedBookings = bookingRepository.findPastBookings(userId, today);
        
        // Remove duplicates and sort by checkout date descending
        completedBookings = completedBookings.stream()
            .distinct()
            .sorted((a, b) -> {
                if (a.getCheckOutDate() == null) return 1;
                if (b.getCheckOutDate() == null) return -1;
                return b.getCheckOutDate().compareTo(a.getCheckOutDate());
            })
            .limit(20) // Limit to most recent 20 memories
            .collect(Collectors.toList());
        
        // Get user's reviews for these hotels
        Map<Long, Review> userReviews = getUserReviewsMap(userId, completedBookings);
        
        // Convert to PastStay memories
        List<PastStay> memories = completedBookings.stream()
            .map(booking -> createPastStay(booking, userReviews))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        // Build response
        MemoryLaneResponse response = new MemoryLaneResponse();
        response.setMemories(memories);
        response.setSummary(buildSummary(completedBookings));
        response.setGreeting(generateGreeting(memories.size()));
        response.setSuggestions(generateSuggestions(memories));
        
        return response;
    }
    
    /**
     * Get a specific past stay memory
     */
    public PastStay getMemory(Long userId, Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        
        if (bookingOpt.isEmpty()) {
            return null;
        }
        
        Booking booking = bookingOpt.get();
        
        // Verify booking belongs to user
        if (!booking.getUser().getId().equals(userId)) {
            return null;
        }
        
        // Check if it's a past booking
        if (booking.getCheckOutDate() == null || !booking.getCheckOutDate().isBefore(LocalDate.now())) {
            return null;
        }
        
        // Get user review if exists
        Map<Long, Review> userReviews = new HashMap<>();
        Hotel hotel = booking.getRoom().getHotel();
        List<Review> reviews = reviewRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        reviews.stream()
            .filter(r -> r.getHotel().getId().equals(hotel.getId()))
            .findFirst()
            .ifPresent(r -> userReviews.put(hotel.getId(), r));
        
        return createPastStay(booking, userReviews);
    }
    
    /**
     * Create PastStay from Booking
     */
    private PastStay createPastStay(Booking booking, Map<Long, Review> userReviews) {
        if (booking.getRoom() == null || booking.getRoom().getHotel() == null) {
            return null;
        }
        
        Room room = booking.getRoom();
        Hotel hotel = room.getHotel();
        
        PastStay stay = new PastStay();
        stay.setBookingId(booking.getId());
        stay.setHotelId(hotel.getId());
        stay.setHotelName(hotel.getName());
        stay.setHotelCity(hotel.getCity());
        stay.setHotelImage(hotel.getHeroImageUrl());
        stay.setHotelStarRating(hotel.getStarRating());
        stay.setRoomType(room.getRoomType() != null ? room.getRoomType().name() : "Standard");
        stay.setCheckIn(booking.getCheckInDate());
        stay.setCheckOut(booking.getCheckOutDate());
        
        // Calculate nights
        if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
            int nights = (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            stay.setNights(Math.max(nights, 1));
        } else {
            stay.setNights(1);
        }
        
        stay.setTotalPaid(booking.getTotalPrice());
        
        // Generate memory title
        stay.setMemoryTitle(MemoryLaneDto.generateMemoryTitle(
            hotel.getName(), 
            hotel.getCity(), 
            booking.getCheckInDate(), 
            stay.getNights()
        ));
        
        // Calculate time since stay
        stay.setTimeSinceStay(MemoryLaneDto.calculateTimeSince(booking.getCheckOutDate()));
        
        // Generate highlights
        stay.setHighlights(generateHighlights(booking, hotel, room));
        
        // Determine mood
        stay.setMood(determineMood(booking, stay.getNights()));
        
        // Check for user review
        Review userReview = userReviews.get(hotel.getId());
        if (userReview != null) {
            stay.setHasReview(true);
            stay.setUserRating(userReview.getRating().doubleValue());
            if (userReview.getComment() != null && userReview.getComment().length() > 100) {
                stay.setUserReviewSnippet(userReview.getComment().substring(0, 100) + "...");
            } else {
                stay.setUserReviewSnippet(userReview.getComment());
            }
        } else {
            stay.setHasReview(false);
        }
        
        // Check if hotel is still active and can be booked again
        stay.setCanBookAgain(hotel.getIsActive() != null && hotel.getIsActive());
        
        // Generate stay milestones
        stay.setStayMilestones(generateMilestones(booking, hotel, stay.getNights()));
        
        return stay;
    }
    
    /**
     * Get map of user reviews by hotel ID
     */
    private Map<Long, Review> getUserReviewsMap(Long userId, List<Booking> bookings) {
        Set<Long> hotelIds = bookings.stream()
            .filter(b -> b.getRoom() != null && b.getRoom().getHotel() != null)
            .map(b -> b.getRoom().getHotel().getId())
            .collect(Collectors.toSet());
        
        Map<Long, Review> reviewMap = new HashMap<>();
        
        // Get all user reviews and filter by hotel IDs
        List<Review> userReviews = reviewRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        for (Review review : userReviews) {
            if (review.getHotel() != null && hotelIds.contains(review.getHotel().getId())) {
                reviewMap.putIfAbsent(review.getHotel().getId(), review);
            }
        }
        
        return reviewMap;
    }
    
    /**
     * Generate highlights for a stay
     */
    private List<String> generateHighlights(Booking booking, Hotel hotel, Room room) {
        List<String> highlights = new ArrayList<>();
        
        // Room type highlight
        if (room.getRoomType() != null) {
            highlights.add(room.getRoomType() + " room");
        }
        
        // Star rating
        if (hotel.getStarRating() != null && hotel.getStarRating() >= 4) {
            highlights.add(hotel.getStarRating() + "-star luxury");
        }
        
        // Location
        if (hotel.getCity() != null) {
            highlights.add(hotel.getCity());
        }
        
        // Room amenities if available
        if (room.getAmenities() != null && !room.getAmenities().isEmpty()) {
            String[] amenities = room.getAmenities().split(",");
            if (amenities.length > 0) {
                highlights.add(amenities[0].trim());
            }
        }
        
        // Limit to 4 highlights
        return highlights.stream().limit(4).collect(Collectors.toList());
    }
    
    /**
     * Determine mood based on booking context
     */
    private MemoryMood determineMood(Booking booking, int nights) {
        if (booking.getCheckInDate() == null) {
            return MemoryMood.RELAXATION;
        }
        
        int dayOfWeek = booking.getCheckInDate().getDayOfWeek().getValue();
        int month = booking.getCheckInDate().getMonthValue();
        
        // Weekend escape (Friday or Saturday check-in, 1-2 nights)
        if ((dayOfWeek == 5 || dayOfWeek == 6) && nights <= 2) {
            return MemoryMood.WEEKEND;
        }
        
        // Extended stay suggests business or relaxation
        if (nights >= 7) {
            // Could be business or relaxation
            if (dayOfWeek == 1) { // Monday check-in suggests business
                return MemoryMood.BUSINESS;
            }
            return MemoryMood.RELAXATION;
        }
        
        // Holiday seasons
        if (month == 2 && booking.getCheckInDate().getDayOfMonth() >= 10 && 
            booking.getCheckInDate().getDayOfMonth() <= 16) {
            return MemoryMood.ROMANTIC; // Valentine's week
        }
        
        if (month == 12 && booking.getCheckInDate().getDayOfMonth() >= 20) {
            return MemoryMood.CELEBRATION; // Christmas/New Year
        }
        
        // Summer months often family vacations
        if (month >= 5 && month <= 7 && nights >= 3) {
            return MemoryMood.FAMILY;
        }
        
        // Short stays might be adventure
        if (nights <= 3 && nights > 1) {
            return MemoryMood.ADVENTURE;
        }
        
        // Single night
        if (nights == 1) {
            return MemoryMood.SOLO;
        }
        
        return MemoryMood.RELAXATION;
    }
    
    /**
     * Generate milestones for the stay
     */
    private List<Milestone> generateMilestones(Booking booking, Hotel hotel, int nights) {
        List<Milestone> milestones = new ArrayList<>();
        
        if (booking.getCheckInDate() != null) {
            milestones.add(new Milestone(
                "🎒",
                "Checked In",
                "Started your journey at " + hotel.getName()
            ));
        }
        
        if (nights > 1) {
            milestones.add(new Milestone(
                "🛏️",
                nights + " Nights",
                "Enjoyed " + nights + " nights of comfort"
            ));
        }
        
        if (hotel.getCity() != null) {
            milestones.add(new Milestone(
                "📍",
                hotel.getCity(),
                "Explored this beautiful destination"
            ));
        }
        
        if (booking.getCheckOutDate() != null) {
            milestones.add(new Milestone(
                "✨",
                "Memory Made",
                "Created lasting memories"
            ));
        }
        
        return milestones;
    }
    
    /**
     * Build summary statistics
     */
    private MemorySummary buildSummary(List<Booking> bookings) {
        MemorySummary summary = new MemorySummary();
        
        summary.setTotalStays(bookings.size());
        
        // Total nights
        int totalNights = bookings.stream()
            .mapToInt(b -> {
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    return (int) Math.max(ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()), 1);
                }
                return 1;
            })
            .sum();
        summary.setTotalNights(totalNights);
        
        // Unique destinations (cities)
        Set<String> cities = bookings.stream()
            .filter(b -> b.getRoom() != null && b.getRoom().getHotel() != null && b.getRoom().getHotel().getCity() != null)
            .map(b -> b.getRoom().getHotel().getCity())
            .collect(Collectors.toSet());
        summary.setUniqueDestinations(cities.size());
        
        // Total spent
        BigDecimal totalSpent = bookings.stream()
            .map(Booking::getTotalPrice)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalSpent(totalSpent);
        
        // Most visited city
        Map<String, Long> cityCounts = bookings.stream()
            .filter(b -> b.getRoom() != null && b.getRoom().getHotel() != null && b.getRoom().getHotel().getCity() != null)
            .map(b -> b.getRoom().getHotel().getCity())
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
        
        String mostVisited = cityCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        summary.setMostVisitedCity(mostVisited);
        
        // Favorite hotel (most booked)
        Map<String, Long> hotelCounts = bookings.stream()
            .filter(b -> b.getRoom() != null && b.getRoom().getHotel() != null)
            .map(b -> b.getRoom().getHotel().getName())
            .collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        
        String favoriteHotel = hotelCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        summary.setFavoriteHotel(favoriteHotel);
        
        // Member since (earliest booking)
        LocalDate earliest = bookings.stream()
            .map(Booking::getCheckInDate)
            .filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(null);
        summary.setMemberSince(earliest);
        
        // Determine travel style
        summary.setTravelStyle(determineTravelStyle(bookings, totalNights));
        
        return summary;
    }
    
    /**
     * Determine user's travel style
     */
    private String determineTravelStyle(List<Booking> bookings, int totalNights) {
        if (bookings.isEmpty()) {
            return "Explorer";
        }
        
        double avgNights = (double) totalNights / bookings.size();
        
        // Check for luxury preference
        long luxuryStays = bookings.stream()
            .filter(b -> b.getRoom() != null && b.getRoom().getHotel() != null)
            .filter(b -> b.getRoom().getHotel().getStarRating() != null && b.getRoom().getHotel().getStarRating() >= 4)
            .count();
        
        double luxuryRatio = (double) luxuryStays / bookings.size();
        
        if (luxuryRatio > 0.7 && avgNights >= 3) {
            return "Luxury Traveler";
        } else if (avgNights <= 2) {
            return "Weekend Explorer";
        } else if (avgNights >= 7) {
            return "Extended Stay Pro";
        } else if (bookings.size() >= 5) {
            return "Frequent Traveler";
        } else {
            return "Casual Traveler";
        }
    }
    
    /**
     * Generate personalized greeting
     */
    private String generateGreeting(int memoryCount) {
        if (memoryCount == 0) {
            return "Start creating memories with LuxeStay!";
        } else if (memoryCount == 1) {
            return "Your journey with us began...";
        } else if (memoryCount <= 3) {
            return "A few wonderful memories together";
        } else if (memoryCount <= 10) {
            return "Your travel story continues to grow";
        } else {
            return "An incredible journey of " + memoryCount + " adventures";
        }
    }
    
    /**
     * Generate suggestions based on past stays
     */
    private List<String> generateSuggestions(List<PastStay> memories) {
        List<String> suggestions = new ArrayList<>();
        
        if (memories.isEmpty()) {
            suggestions.add("Book your first stay and start creating memories!");
            return suggestions;
        }
        
        // Find stays without reviews
        long unreviewed = memories.stream().filter(m -> !m.isHasReview()).count();
        if (unreviewed > 0) {
            suggestions.add("Share your experience with a review!");
        }
        
        // Find most recent stay that can be booked again
        memories.stream()
            .filter(PastStay::isCanBookAgain)
            .findFirst()
            .ifPresent(stay -> suggestions.add("Book again at " + stay.getHotelName() + "?"));
        
        // General suggestions
        if (memories.size() >= 3) {
            suggestions.add("Explore new destinations!");
        }
        
        return suggestions.stream().limit(3).collect(Collectors.toList());
    }
}
