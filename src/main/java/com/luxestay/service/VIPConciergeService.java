package com.luxestay.service;

import com.luxestay.dto.VIPConciergeDto.*;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.User;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * VIP Concierge Service
 * Premium support and elevated service management
 * 
 * Week 12 Feature: VIP guest experience and tier management
 */
@Service
public class VIPConciergeService {
    
    private static final Logger logger = LoggerFactory.getLogger(VIPConciergeService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Get VIP status for a user
     */
    @Cacheable(value = "vipStatus", key = "#userId", unless = "#result == null")
    public VIPStatus getVIPStatus(Long userId) {
        logger.info("Getting VIP status for user: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        
        User user = userOpt.get();
        
        // Get booking statistics
        BookingStats stats = calculateBookingStats(userId);
        
        // Calculate VIP tier
        VIPTier tier = VIPTier.calculateTier(stats.totalBookings, stats.totalNights);
        
        VIPStatus status = new VIPStatus();
        status.setVIP(tier != VIPTier.STANDARD);
        status.setTier(tier);
        status.setTotalBookings(stats.totalBookings);
        status.setTotalNights(stats.totalNights);
        status.setLoyaltyPoints(calculateLoyaltyPoints(stats));
        status.setActiveBenefits(getBenefitsForTier(tier));
        status.setAvailableServices(getServicesForTier(tier));
        status.setConciergeContact(getConciergeContact(tier));
        status.setPersonalGreeting(generatePersonalGreeting(user, tier));
        
        return status;
    }
    
    /**
     * Get progress towards next tier
     */
    public TierProgress getTierProgress(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        
        BookingStats stats = calculateBookingStats(userId);
        VIPTier currentTier = VIPTier.calculateTier(stats.totalBookings, stats.totalNights);
        
        TierProgress progress = new TierProgress();
        progress.setCurrentTier(currentTier);
        progress.setCurrentBookings(stats.totalBookings);
        progress.setCurrentNights(stats.totalNights);
        
        // Find next tier
        VIPTier nextTier = getNextTier(currentTier);
        
        if (nextTier != null) {
            progress.setNextTier(nextTier);
            progress.setBookingsToNext(Math.max(0, nextTier.getMinBookings() - stats.totalBookings));
            progress.setNightsToNext(Math.max(0, nextTier.getMinNights() - stats.totalNights));
            
            // Calculate progress percentage (average of both metrics)
            double bookingProgress = (double) stats.totalBookings / nextTier.getMinBookings() * 100;
            double nightsProgress = (double) stats.totalNights / nextTier.getMinNights() * 100;
            double avgProgress = Math.min(100, (bookingProgress + nightsProgress) / 2);
            progress.setProgressPercentage(avgProgress);
            
            progress.setMessage(generateProgressMessage(progress));
        } else {
            // Already at max tier
            progress.setProgressPercentage(100);
            progress.setMessage("You've reached our highest tier! Thank you for being a Diamond member.");
        }
        
        return progress;
    }
    
    /**
     * Calculate booking statistics
     */
    private BookingStats calculateBookingStats(Long userId) {
        // Get all past bookings
        LocalDate today = LocalDate.now();
        List<Booking> allCompletedBookings = bookingRepository.findPastBookings(userId, today);
        
        int totalBookings = allCompletedBookings.size();
        
        int totalNights = allCompletedBookings.stream()
            .mapToInt(b -> {
                if (b.getCheckInDate() != null && b.getCheckOutDate() != null) {
                    return (int) Math.max(1, ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()));
                }
                return 1;
            })
            .sum();
        
        return new BookingStats(totalBookings, totalNights);
    }
    
    /**
     * Calculate loyalty points (simplified - 10 per night + 50 per booking)
     */
    private int calculateLoyaltyPoints(BookingStats stats) {
        return (stats.totalNights * 10) + (stats.totalBookings * 50);
    }
    
    /**
     * Get next tier
     */
    private VIPTier getNextTier(VIPTier current) {
        VIPTier[] tiers = VIPTier.values();
        int currentIndex = current.ordinal();
        
        if (currentIndex < tiers.length - 1) {
            return tiers[currentIndex + 1];
        }
        return null; // Already at max tier
    }
    
    /**
     * Get benefits available for a tier
     */
    private List<VIPBenefit> getBenefitsForTier(VIPTier tier) {
        List<VIPBenefit> benefits = new ArrayList<>();
        
        // Standard benefits
        benefits.add(new VIPBenefit(
            "early_checkin",
            "⏰",
            "Early Check-in Request",
            "Request early check-in (subject to availability)",
            tier.ordinal() >= VIPTier.SILVER.ordinal(),
            VIPTier.SILVER
        ));
        
        benefits.add(new VIPBenefit(
            "late_checkout",
            "🌙",
            "Late Check-out Request",
            "Request late check-out (subject to availability)",
            tier.ordinal() >= VIPTier.SILVER.ordinal(),
            VIPTier.SILVER
        ));
        
        // Gold benefits
        benefits.add(new VIPBenefit(
            "room_upgrade",
            "⬆️",
            "Room Upgrade Priority",
            "Priority for complimentary room upgrades",
            tier.ordinal() >= VIPTier.GOLD.ordinal(),
            VIPTier.GOLD
        ));
        
        benefits.add(new VIPBenefit(
            "priority_support",
            "🎯",
            "Priority Support",
            "Faster response times from our support team",
            tier.ordinal() >= VIPTier.GOLD.ordinal(),
            VIPTier.GOLD
        ));
        
        // Platinum benefits
        benefits.add(new VIPBenefit(
            "dedicated_concierge",
            "👤",
            "Dedicated Concierge",
            "Personal concierge for all your travel needs",
            tier.ordinal() >= VIPTier.PLATINUM.ordinal(),
            VIPTier.PLATINUM
        ));
        
        benefits.add(new VIPBenefit(
            "exclusive_rates",
            "💰",
            "Exclusive Rates",
            "Access to member-only pricing and deals",
            tier.ordinal() >= VIPTier.PLATINUM.ordinal(),
            VIPTier.PLATINUM
        ));
        
        // Diamond benefits
        benefits.add(new VIPBenefit(
            "vip_lounge",
            "✨",
            "VIP Lounge Access",
            "Complimentary access to executive lounges",
            tier.ordinal() >= VIPTier.DIAMOND.ordinal(),
            VIPTier.DIAMOND
        ));
        
        benefits.add(new VIPBenefit(
            "guaranteed_availability",
            "🔐",
            "Guaranteed Availability",
            "48-hour booking guarantee at select properties",
            tier.ordinal() >= VIPTier.DIAMOND.ordinal(),
            VIPTier.DIAMOND
        ));
        
        return benefits;
    }
    
    /**
     * Get available services for a tier
     */
    private List<VIPService> getServicesForTier(VIPTier tier) {
        List<VIPService> services = new ArrayList<>();
        
        // Available to all
        services.add(new VIPService(
            "booking_assistance",
            "📅",
            "Booking Assistance",
            "Get help finding the perfect hotel",
            true,
            "#chatbot",
            "Chat with us"
        ));
        
        // Silver+
        if (tier.ordinal() >= VIPTier.SILVER.ordinal()) {
            services.add(new VIPService(
                "special_requests",
                "🎁",
                "Special Requests",
                "Room preferences, celebrations, and more",
                true,
                "mailto:concierge@luxestay.com",
                "Email us"
            ));
        }
        
        // Gold+
        if (tier.ordinal() >= VIPTier.GOLD.ordinal()) {
            services.add(new VIPService(
                "restaurant_reservations",
                "🍽️",
                "Restaurant Reservations",
                "Book dining at top restaurants",
                true,
                "#concierge",
                "Request"
            ));
            
            services.add(new VIPService(
                "transportation",
                "🚗",
                "Transportation",
                "Airport transfers and car service",
                true,
                "#concierge",
                "Arrange"
            ));
        }
        
        // Platinum+
        if (tier.ordinal() >= VIPTier.PLATINUM.ordinal()) {
            services.add(new VIPService(
                "experience_planning",
                "🎭",
                "Experience Planning",
                "Curated local experiences and tours",
                true,
                "#concierge",
                "Explore"
            ));
        }
        
        // Diamond
        if (tier.ordinal() >= VIPTier.DIAMOND.ordinal()) {
            services.add(new VIPService(
                "personal_shopper",
                "🛍️",
                "Personal Shopper",
                "Luxury shopping assistance",
                true,
                "#concierge",
                "Book"
            ));
        }
        
        return services;
    }
    
    /**
     * Get concierge contact based on tier
     */
    private ConciergeContact getConciergeContact(VIPTier tier) {
        if (tier.ordinal() >= VIPTier.PLATINUM.ordinal()) {
            return new ConciergeContact(
                "Your Personal Concierge",
                "VIP Guest Services",
                "vip@luxestay.com",
                "+1 (555) VIP-LUXE",
                "24/7 Priority Line",
                "< 30 minutes"
            );
        } else if (tier.ordinal() >= VIPTier.GOLD.ordinal()) {
            return new ConciergeContact(
                "LuxeStay Concierge",
                "Gold Member Services",
                "concierge@luxestay.com",
                "+1 (555) LUX-STAY",
                "Daily 8am - 10pm",
                "< 2 hours"
            );
        } else {
            return new ConciergeContact(
                "LuxeStay Support",
                "Guest Services",
                "support@luxestay.com",
                null,
                "Daily 9am - 6pm",
                "< 24 hours"
            );
        }
    }
    
    /**
     * Generate personalized greeting
     */
    private String generatePersonalGreeting(User user, VIPTier tier) {
        String name = user.getFirstName() != null ? user.getFirstName() : "Valued Guest";
        
        switch (tier) {
            case DIAMOND:
                return "Welcome back, " + name + "! As our Diamond member, your legacy of exceptional travel continues.";
            case PLATINUM:
                return "Welcome back, " + name + "! Your Platinum status unlocks the finest experiences.";
            case GOLD:
                return "Welcome back, " + name + "! Gold members like you deserve the best.";
            case SILVER:
                return "Welcome back, " + name + "! Thank you for being a valued Silver member.";
            default:
                return "Welcome, " + name + "! Start your journey to VIP status today.";
        }
    }
    
    /**
     * Generate progress message
     */
    private String generateProgressMessage(TierProgress progress) {
        if (progress.getBookingsToNext() <= 0 && progress.getNightsToNext() <= 0) {
            return "You're almost there! Complete one more stay to unlock " + 
                   progress.getNextTier().getDisplayName() + " status!";
        }
        
        StringBuilder message = new StringBuilder();
        message.append("Just ");
        
        if (progress.getBookingsToNext() > 0) {
            message.append(progress.getBookingsToNext()).append(" booking");
            if (progress.getBookingsToNext() > 1) message.append("s");
        }
        
        if (progress.getBookingsToNext() > 0 && progress.getNightsToNext() > 0) {
            message.append(" and ");
        }
        
        if (progress.getNightsToNext() > 0) {
            message.append(progress.getNightsToNext()).append(" night");
            if (progress.getNightsToNext() > 1) message.append("s");
        }
        
        message.append(" away from ").append(progress.getNextTier().getDisplayName()).append(" status!");
        
        return message.toString();
    }
    
    /**
     * Internal helper class for booking stats
     */
    private static class BookingStats {
        final int totalBookings;
        final int totalNights;
        
        BookingStats(int totalBookings, int totalNights) {
            this.totalBookings = totalBookings;
            this.totalNights = totalNights;
        }
    }
}
