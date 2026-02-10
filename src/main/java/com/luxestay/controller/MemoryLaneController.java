package com.luxestay.controller;

import com.hotel.domain.entity.User;
import com.luxestay.dto.MemoryLaneDto.*;
import com.luxestay.service.MemoryLaneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Memory Lane Controller
 * "Remember your stay at..." Past Stay Memories
 * 
 * Week 11 Feature: Nostalgia endpoints for completed bookings
 */
@RestController
@RequestMapping("/api/memory-lane")
public class MemoryLaneController {
    
    @Autowired
    private MemoryLaneService memoryLaneService;
    
    /**
     * Get user's full memory lane
     * GET /api/memory-lane
     */
    @GetMapping
    public ResponseEntity<?> getMemoryLane(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).body(errorResponse("Unauthorized"));
            }
            
            MemoryLaneResponse memories = memoryLaneService.getMemoryLane(user.getId());
            return ResponseEntity.ok(successResponse(memories));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse("Failed to load memories: " + e.getMessage()));
        }
    }
    
    /**
     * Get a specific memory
     * GET /api/memory-lane/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getMemory(
            @AuthenticationPrincipal User user,
            @PathVariable Long bookingId) {
        try {
            if (user == null) {
                return ResponseEntity.status(401).body(errorResponse("Unauthorized"));
            }
            
            PastStay memory = memoryLaneService.getMemory(user.getId(), bookingId);
            
            if (memory == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(successResponse(memory));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse("Failed to load memory: " + e.getMessage()));
        }
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
