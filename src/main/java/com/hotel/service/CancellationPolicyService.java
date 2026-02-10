package com.hotel.service;

import com.hotel.domain.dto.policy.FlexBookDto;
import com.hotel.domain.entity.Booking;
import com.hotel.domain.entity.CancellationPolicy;
import com.hotel.domain.entity.Room;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * CancellationPolicy Service - FlexBook feature logic.
 * 
 * Provides clear, honest cancellation policy information with:
 * - Visual timelines
 * - Calculated refund amounts
 * - Human-readable deadlines
 * 
 * No hidden rules. No fine print.
 */
@Service
@Transactional(readOnly = true)
public class CancellationPolicyService {
    
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    
    public CancellationPolicyService(RoomRepository roomRepository, 
                                     BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }
    
    /**
     * Get cancellation policy for a room.
     * If room has no policy set, returns default MODERATE policy.
     */
    public FlexBookDto getRoomPolicy(Long roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        CancellationPolicy policy = getEffectivePolicy(room);
        return FlexBookDto.forRoom(policy);
    }
    
    /**
     * Get cancellation policy with booking-specific calculations.
     * Shows refund amounts and deadlines specific to the booking dates.
     */
    public FlexBookDto getRoomPolicyForDates(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        CancellationPolicy policy = getEffectivePolicy(room);
        
        // Calculate total price for refund calculation
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        
        return FlexBookDto.forBooking(policy, checkInDate, totalPrice);
    }
    
    /**
     * Get cancellation policy for an existing booking.
     */
    public FlexBookDto getBookingPolicy(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        CancellationPolicy policy = getEffectivePolicy(booking.getRoom());
        
        return FlexBookDto.forBooking(policy, booking.getCheckInDate(), booking.getTotalPrice());
    }
    
    /**
     * Calculate refund amount for cancelling a booking now.
     */
    public RefundCalculation calculateRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        CancellationPolicy policy = getEffectivePolicy(booking.getRoom());
        int refundPercent = calculateCurrentRefundPercentage(policy, booking.getCheckInDate());
        
        BigDecimal refundAmount = booking.getTotalPrice()
            .multiply(BigDecimal.valueOf(refundPercent / 100.0));
        
        return new RefundCalculation(
            refundPercent,
            refundAmount,
            booking.getTotalPrice().subtract(refundAmount),
            policy.getRefundDaysMin(),
            policy.getRefundDaysMax(),
            refundPercent < 100 ? buildRefundExplanation(policy, refundPercent) : "Full refund to original payment method"
        );
    }
    
    /**
     * Check if a booking can be cancelled with full refund.
     */
    public boolean canCancelWithFullRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        CancellationPolicy policy = getEffectivePolicy(booking.getRoom());
        return calculateCurrentRefundPercentage(policy, booking.getCheckInDate()) == 100;
    }
    
    /**
     * Get all policy types with descriptions (for UI dropdowns)
     */
    public Map<String, PolicySummary> getAllPolicies() {
        Map<String, PolicySummary> policies = new HashMap<>();
        
        for (CancellationPolicy policy : CancellationPolicy.values()) {
            policies.put(policy.name(), new PolicySummary(
                policy.getDisplayName(),
                policy.getShortDescription(),
                policy.getFreeDaysBefore(),
                FlexBookDto.forRoom(policy).getIcon(),
                FlexBookDto.forRoom(policy).getColorTheme()
            ));
        }
        
        return policies;
    }
    
    // ==================== Private Helpers ====================
    
    private CancellationPolicy getEffectivePolicy(Room room) {
        // For now, return MODERATE as default since we haven't added the field to Room yet
        // This is safe - we're not breaking existing functionality
        // In Phase 2, we'll read from room.getCancellationPolicy() if set
        return CancellationPolicy.MODERATE;
    }
    
    private int calculateCurrentRefundPercentage(CancellationPolicy policy, LocalDate checkInDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkInTime = checkInDate.atTime(15, 0); // 3 PM check-in
        
        long hoursUntilCheckIn = ChronoUnit.HOURS.between(now, checkInTime);
        
        if (hoursUntilCheckIn < 0) {
            return 0; // Already past check-in
        }
        
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
    
    private String buildRefundExplanation(CancellationPolicy policy, int refundPercent) {
        if (refundPercent == 50) {
            return "50% refund based on " + policy.getDisplayName() + " cancellation policy";
        }
        return "Non-refundable under " + policy.getDisplayName() + " policy - cancellation window has passed";
    }
    
    // ==================== Response Records ====================
    
    public record RefundCalculation(
        int refundPercent,
        BigDecimal refundAmount,
        BigDecimal nonRefundableAmount,
        int refundDaysMin,
        int refundDaysMax,
        String explanation
    ) {}
    
    public record PolicySummary(
        String displayName,
        String shortDescription,
        int freeDaysBefore,
        String icon,
        String colorTheme
    ) {}
}
