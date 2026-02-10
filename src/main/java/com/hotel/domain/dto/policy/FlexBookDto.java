package com.hotel.domain.dto.policy;

import com.hotel.domain.entity.CancellationPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FlexBook DTO - Provides clear, visual cancellation policy information.
 * 
 * Design Philosophy:
 * - No hidden rules
 * - Visual timeline format
 * - Human-readable dates
 * - Clear refund amounts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlexBookDto {
    
    /**
     * Policy tier: FLEXIBLE, MODERATE, STRICT
     */
    private String policyType;
    
    /**
     * Display name: "Flexible", "Moderate", "Strict"
     */
    private String policyName;
    
    /**
     * One-line summary
     */
    private String summary;
    
    /**
     * Detailed policy explanation
     */
    private String fullDescription;
    
    /**
     * Whether free cancellation is currently available
     */
    private Boolean freeCancellationAvailable;
    
    /**
     * Free cancellation deadline (human-readable)
     */
    private String freeCancellationDeadline;
    
    /**
     * Free cancellation deadline (ISO date)
     */
    private LocalDateTime freeCancellationDeadlineDate;
    
    /**
     * Days until free cancellation expires
     */
    private Integer daysUntilDeadline;
    
    /**
     * Refund timeline explanation
     */
    private String refundTimeline;
    
    /**
     * Visual timeline milestones for UI
     */
    private List<TimelineMilestone> timeline;
    
    /**
     * Calculated refund amount if cancelled now (for booking context)
     */
    private BigDecimal refundAmountIfCancelledNow;
    
    /**
     * Refund percentage if cancelled now
     */
    private Integer refundPercentage;
    
    /**
     * Icon class for the policy (FontAwesome)
     */
    private String icon;
    
    /**
     * Color theme for the policy badge
     */
    private String colorTheme;
    
    /**
     * Timeline milestone for visual representation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineMilestone {
        private String label;           // "Today", "Jan 15", "Check-in"
        private String description;     // "100% refund", "50% refund", "No refund"
        private String status;          // "past", "current", "future", "deadline"
        private LocalDate date;         // Actual date
        private Integer refundPercent;  // Refund % at this point
        private Boolean isDeadline;     // Is this the free cancellation deadline?
    }
    
    /**
     * Create FlexBook response for a room without booking context
     */
    public static FlexBookDto forRoom(CancellationPolicy policy) {
        return FlexBookDto.builder()
            .policyType(policy.name())
            .policyName(policy.getDisplayName())
            .summary(policy.getShortDescription())
            .fullDescription(buildFullDescription(policy))
            .refundTimeline(buildRefundTimeline(policy))
            .icon(getIconForPolicy(policy))
            .colorTheme(getColorThemeForPolicy(policy))
            .build();
    }
    
    /**
     * Create FlexBook response with booking-specific details
     */
    public static FlexBookDto forBooking(CancellationPolicy policy, 
                                         LocalDate checkInDate, 
                                         BigDecimal totalPrice) {
        FlexBookDto dto = forRoom(policy);
        
        LocalDateTime deadline = calculateDeadline(policy, checkInDate);
        LocalDateTime now = LocalDateTime.now();
        
        dto.setFreeCancellationDeadlineDate(deadline);
        dto.setFreeCancellationAvailable(now.isBefore(deadline));
        dto.setFreeCancellationDeadline(formatDeadline(deadline));
        dto.setDaysUntilDeadline(calculateDaysUntilDeadline(deadline));
        dto.setTimeline(buildTimeline(policy, checkInDate));
        
        // Calculate current refund
        int refundPercent = calculateRefundPercentage(policy, checkInDate);
        dto.setRefundPercentage(refundPercent);
        if (totalPrice != null) {
            dto.setRefundAmountIfCancelledNow(
                totalPrice.multiply(BigDecimal.valueOf(refundPercent / 100.0))
            );
        }
        
        return dto;
    }
    
    private static String buildFullDescription(CancellationPolicy policy) {
        return switch (policy) {
            case FLEXIBLE -> """
                Cancel up to 24 hours before check-in for a full refund. \
                Cancellations within 24 hours are non-refundable. \
                Refunds typically process within 3-5 business days.""";
            case MODERATE -> """
                Cancel up to 5 days before check-in for a full refund. \
                Cancel 1-5 days before for a 50% refund. \
                Cancellations within 24 hours are non-refundable. \
                Refunds typically process within 5-7 business days.""";
            case STRICT -> """
                Cancel up to 7 days before check-in for a full refund. \
                Cancel 3-7 days before for a 50% refund. \
                Cancellations within 3 days are non-refundable. \
                Refunds typically process within 7-10 business days.""";
        };
    }
    
    private static String buildRefundTimeline(CancellationPolicy policy) {
        return String.format("Refunds process within %d-%d business days", 
            policy.getRefundDaysMin(), policy.getRefundDaysMax());
    }
    
    private static LocalDateTime calculateDeadline(CancellationPolicy policy, LocalDate checkInDate) {
        // Check-in is typically at 3:00 PM
        LocalDateTime checkInTime = checkInDate.atTime(15, 0);
        return checkInTime.minusHours(policy.getFreeHoursBefore());
    }
    
    private static String formatDeadline(LocalDateTime deadline) {
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' h:mm a");
        return deadline.format(formatter);
    }
    
    private static Integer calculateDaysUntilDeadline(LocalDateTime deadline) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(), deadline.toLocalDate());
        return (int) Math.max(0, days);
    }
    
    private static int calculateRefundPercentage(CancellationPolicy policy, LocalDate checkInDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = calculateDeadline(policy, checkInDate);
        
        if (now.isBefore(deadline)) {
            return 100;
        }
        
        long hoursUntilCheckIn = java.time.temporal.ChronoUnit.HOURS.between(
            now, checkInDate.atTime(15, 0));
        
        return switch (policy) {
            case FLEXIBLE -> hoursUntilCheckIn >= 24 ? 100 : 0;
            case MODERATE -> {
                if (hoursUntilCheckIn >= 120) yield 100;  // 5+ days
                if (hoursUntilCheckIn >= 24) yield 50;    // 1-5 days
                yield 0;
            }
            case STRICT -> {
                if (hoursUntilCheckIn >= 168) yield 100;  // 7+ days
                if (hoursUntilCheckIn >= 72) yield 50;    // 3-7 days
                yield 0;
            }
        };
    }
    
    private static List<TimelineMilestone> buildTimeline(CancellationPolicy policy, LocalDate checkInDate) {
        LocalDate today = LocalDate.now();
        LocalDateTime deadline = calculateDeadline(policy, checkInDate);
        LocalDate deadlineDate = deadline.toLocalDate();
        
        java.util.List<TimelineMilestone> milestones = new java.util.ArrayList<>();
        
        // Today
        milestones.add(TimelineMilestone.builder()
            .label("Today")
            .description(today.isBefore(deadlineDate) ? "100% refund" : getRefundDescription(policy, today, checkInDate))
            .status("current")
            .date(today)
            .refundPercent(calculateRefundPercentage(policy, checkInDate))
            .isDeadline(false)
            .build());
        
        // Deadline
        if (deadlineDate.isAfter(today)) {
            milestones.add(TimelineMilestone.builder()
                .label(formatShortDate(deadlineDate))
                .description("Free cancellation ends")
                .status("deadline")
                .date(deadlineDate)
                .refundPercent(100)
                .isDeadline(true)
                .build());
        }
        
        // Check-in
        milestones.add(TimelineMilestone.builder()
            .label("Check-in")
            .description(formatShortDate(checkInDate))
            .status(checkInDate.isAfter(today) ? "future" : "past")
            .date(checkInDate)
            .refundPercent(0)
            .isDeadline(false)
            .build());
        
        return milestones;
    }
    
    private static String getRefundDescription(CancellationPolicy policy, LocalDate current, LocalDate checkIn) {
        int percent = calculateRefundPercentage(policy, checkIn);
        if (percent == 100) return "100% refund";
        if (percent == 50) return "50% refund";
        return "Non-refundable";
    }
    
    private static String formatShortDate(LocalDate date) {
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("MMM d");
        return date.format(formatter);
    }
    
    private static String getIconForPolicy(CancellationPolicy policy) {
        return switch (policy) {
            case FLEXIBLE -> "fa-calendar-check";
            case MODERATE -> "fa-calendar";
            case STRICT -> "fa-calendar-xmark";
        };
    }
    
    private static String getColorThemeForPolicy(CancellationPolicy policy) {
        return switch (policy) {
            case FLEXIBLE -> "green";
            case MODERATE -> "blue";
            case STRICT -> "orange";
        };
    }
}
