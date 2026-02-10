package com.hotel.controller;

import com.hotel.domain.dto.alerts.SmartAlertDto;
import com.hotel.service.SmartAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST Controller for Smart Alerts
 * 
 * Provides personalized, helpful alerts:
 * - Price drop notifications
 * - Availability alerts
 * - Booking reminders
 * 
 * NO spam, NO dark patterns - just helpful info
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Smart Alerts", description = "Personalized hotel alerts and notifications")
public class SmartAlertController {
    
    private final SmartAlertService smartAlertService;
    
    public SmartAlertController(SmartAlertService smartAlertService) {
        this.smartAlertService = smartAlertService;
    }
    
    /**
     * Get alerts for session (works for both authenticated and unauthenticated users)
     * 
     * @param viewedHotelIds Optional list of recently viewed hotel IDs
     */
    @GetMapping("/session")
    @Operation(summary = "Get session alerts", 
               description = "Returns alerts based on session activity (viewed hotels)")
    public ResponseEntity<Map<String, Object>> getSessionAlerts(
            @RequestParam(required = false) List<Long> viewedHotelIds,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        
        try {
            List<SmartAlertDto> alerts = smartAlertService.getSessionAlerts(
                sessionId != null ? sessionId : UUID.randomUUID().toString(),
                viewedHotelIds
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "alerts", alerts,
                "count", alerts.size(),
                "unreadCount", alerts.stream().filter(a -> !a.isRead()).count()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to retrieve alerts");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get alerts for authenticated user
     */
    @GetMapping("/user")
    @Operation(summary = "Get user alerts", 
               description = "Returns personalized alerts for authenticated user")
    public ResponseEntity<Map<String, Object>> getUserAlerts(Authentication auth) {
        try {
            if (auth == null || auth.getPrincipal() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", Map.of("alerts", List.of(), "count", 0));
                return ResponseEntity.ok(response);
            }
            
            // Get user ID from authentication (implementation depends on your auth setup)
            Long userId = extractUserId(auth);
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", Map.of("alerts", List.of(), "count", 0));
                return ResponseEntity.ok(response);
            }
            
            List<SmartAlertDto> alerts = smartAlertService.getUserAlerts(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "alerts", alerts,
                "count", alerts.size(),
                "unreadCount", alerts.stream().filter(a -> !a.isRead()).count()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to retrieve alerts");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Check availability alerts for specific hotels and dates
     */
    @GetMapping("/availability")
    @Operation(summary = "Check availability alerts", 
               description = "Returns availability alerts for specified hotels and dates")
    public ResponseEntity<Map<String, Object>> checkAvailabilityAlerts(
            @RequestParam List<Long> hotelIds,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        
        try {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);
            
            List<SmartAlertDto> alerts = smartAlertService.getAvailabilityAlerts(
                hotelIds, checkInDate, checkOutDate
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", alerts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to check availability");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Check for price drop on a hotel
     */
    @GetMapping("/price-drop/{hotelId}")
    @Operation(summary = "Check price drop", 
               description = "Checks if price has dropped for a hotel")
    public ResponseEntity<Map<String, Object>> checkPriceDrop(
            @PathVariable Long hotelId,
            Authentication auth) {
        
        try {
            Long userId = auth != null ? extractUserId(auth) : null;
            
            Optional<SmartAlertDto> alert = smartAlertService.checkPriceDrop(hotelId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "hasPriceDrop", alert.isPresent(),
                "alert", alert.orElse(null)
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to check price drop");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Dismiss an alert
     */
    @PostMapping("/{alertId}/dismiss")
    @Operation(summary = "Dismiss alert", description = "Dismisses an alert so it won't appear again")
    public ResponseEntity<Map<String, Object>> dismissAlert(
            @PathVariable Long alertId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        
        try {
            smartAlertService.dismissAlert(
                sessionId != null ? sessionId : "default",
                alertId
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Alert dismissed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to dismiss alert");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Mark alert as read
     */
    @PostMapping("/{alertId}/read")
    @Operation(summary = "Mark alert as read", description = "Marks an alert as read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long alertId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        
        try {
            smartAlertService.markAsRead(
                sessionId != null ? sessionId : "default",
                alertId
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Alert marked as read");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unable to mark alert as read");
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Extract user ID from authentication
     */
    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        
        Object principal = auth.getPrincipal();
        
        // Handle different principal types
        if (principal instanceof com.hotel.domain.entity.User) {
            return ((com.hotel.domain.entity.User) principal).getId();
        }
        
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Try to extract from username (email)
            // This would need to lookup user by email
            return null;
        }
        
        return null;
    }
}
