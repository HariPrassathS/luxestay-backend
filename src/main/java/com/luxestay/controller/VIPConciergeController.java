package com.luxestay.controller;

import com.hotel.domain.entity.User;
import com.luxestay.dto.VIPConciergeDto.*;
import com.luxestay.service.VIPConciergeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * VIP Concierge Controller
 * Premium support and VIP status endpoints
 * 
 * Week 12 Feature: VIP guest experience management
 */
@RestController
@RequestMapping("/api/vip")
public class VIPConciergeController {
    
    @Autowired
    private VIPConciergeService vipConciergeService;
    
    /**
     * Get user's VIP status
     * GET /api/vip/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getVIPStatus(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).body(errorResponse("Unauthorized"));
            }
            
            VIPStatus status = vipConciergeService.getVIPStatus(user.getId());
            
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(successResponse(status));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse("Failed to get VIP status: " + e.getMessage()));
        }
    }
    
    /**
     * Get tier progress
     * GET /api/vip/progress
     */
    @GetMapping("/progress")
    public ResponseEntity<?> getTierProgress(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).body(errorResponse("Unauthorized"));
            }
            
            TierProgress progress = vipConciergeService.getTierProgress(user.getId());
            
            if (progress == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(successResponse(progress));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse("Failed to get tier progress: " + e.getMessage()));
        }
    }
    
    /**
     * Get tier benefits overview (public endpoint for marketing)
     * GET /api/vip/benefits
     */
    @GetMapping("/benefits")
    public ResponseEntity<?> getTierBenefits() {
        Map<String, Object> benefits = new HashMap<>();
        
        for (VIPTier tier : VIPTier.values()) {
            Map<String, Object> tierInfo = new HashMap<>();
            tierInfo.put("displayName", tier.getDisplayName());
            tierInfo.put("icon", tier.getIcon());
            tierInfo.put("description", tier.getDescription());
            tierInfo.put("minBookings", tier.getMinBookings());
            tierInfo.put("minNights", tier.getMinNights());
            benefits.put(tier.name(), tierInfo);
        }
        
        return ResponseEntity.ok(successResponse(benefits));
    }
    
    /**
     * Helper: Success response
     */
    private Map<String, Object> successResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
    
    /**
     * Helper: Error response
     */
    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}
