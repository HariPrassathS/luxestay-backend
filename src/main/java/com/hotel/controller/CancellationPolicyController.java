package com.hotel.controller;

import com.hotel.domain.dto.policy.FlexBookDto;
import com.hotel.service.CancellationPolicyService;
import com.hotel.service.CancellationPolicyService.RefundCalculation;
import com.hotel.service.CancellationPolicyService.PolicySummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * FlexBook / Cancellation Policy REST Controller
 * 
 * Provides clear, honest cancellation policy information.
 * No hidden rules. Visual timelines. Human-readable deadlines.
 */
@RestController
@RequestMapping("/api/policies")
@Tag(name = "FlexBook", description = "Cancellation policy and refund information")
public class CancellationPolicyController {
    
    private final CancellationPolicyService policyService;
    
    public CancellationPolicyController(CancellationPolicyService policyService) {
        this.policyService = policyService;
    }
    
    /**
     * Get cancellation policy for a room (without booking dates).
     */
    @GetMapping("/room/{roomId}")
    @Operation(summary = "Get room cancellation policy",
               description = "Returns the cancellation policy for a room")
    public ResponseEntity<FlexBookDto> getRoomPolicy(@PathVariable Long roomId) {
        FlexBookDto policy = policyService.getRoomPolicy(roomId);
        return ResponseEntity.ok(policy);
    }
    
    /**
     * Get cancellation policy with booking-specific timeline and calculations.
     */
    @GetMapping("/room/{roomId}/calculate")
    @Operation(summary = "Calculate policy for dates",
               description = "Returns policy with specific refund amounts and deadlines for given dates")
    public ResponseEntity<FlexBookDto> getRoomPolicyForDates(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        
        FlexBookDto policy = policyService.getRoomPolicyForDates(roomId, checkIn, checkOut);
        return ResponseEntity.ok(policy);
    }
    
    /**
     * Get cancellation policy for an existing booking.
     */
    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get booking cancellation policy",
               description = "Returns policy details for an existing booking with current refund calculation")
    public ResponseEntity<FlexBookDto> getBookingPolicy(@PathVariable Long bookingId) {
        FlexBookDto policy = policyService.getBookingPolicy(bookingId);
        return ResponseEntity.ok(policy);
    }
    
    /**
     * Calculate refund amount if booking is cancelled now.
     */
    @GetMapping("/booking/{bookingId}/refund")
    @Operation(summary = "Calculate current refund",
               description = "Calculates the exact refund amount if booking is cancelled immediately")
    public ResponseEntity<RefundCalculation> calculateRefund(@PathVariable Long bookingId) {
        RefundCalculation refund = policyService.calculateRefund(bookingId);
        return ResponseEntity.ok(refund);
    }
    
    /**
     * Check if booking can be cancelled with full refund.
     */
    @GetMapping("/booking/{bookingId}/can-refund")
    @Operation(summary = "Check full refund eligibility",
               description = "Returns whether the booking can still receive a full refund")
    public ResponseEntity<Map<String, Boolean>> canCancelWithFullRefund(@PathVariable Long bookingId) {
        boolean canRefund = policyService.canCancelWithFullRefund(bookingId);
        return ResponseEntity.ok(Map.of("canGetFullRefund", canRefund));
    }
    
    /**
     * Get all policy types (for displaying policy comparison).
     */
    @GetMapping("/types")
    @Operation(summary = "Get all policy types",
               description = "Returns all cancellation policy types with descriptions")
    public ResponseEntity<Map<String, PolicySummary>> getAllPolicyTypes() {
        Map<String, PolicySummary> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }
}
