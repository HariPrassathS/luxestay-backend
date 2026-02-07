package com.hotel.controller;

import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.groupbooking.CreateGroupBookingRequest;
import com.hotel.domain.dto.groupbooking.GroupBookingDto;
import com.hotel.domain.entity.User;
import com.hotel.repository.UserRepository;
import com.hotel.service.GroupBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Group Bookings.
 * 
 * MOBILE-FIRST DESIGN:
 * - Minimal request/response payloads
 * - Real-time updates via WebSocket
 * - Clear ownership model
 */
@RestController
@RequestMapping("/api/group-bookings")
@RequiredArgsConstructor
@Tag(name = "Group Bookings", description = "Coordinated multi-room booking APIs")
@SecurityRequirement(name = "bearer-jwt")
public class GroupBookingController {
    
    private final GroupBookingService groupBookingService;
    private final UserRepository userRepository;
    
    // ==================== CREATE & JOIN ====================
    
    /**
     * Create a new group booking.
     */
    @PostMapping
    @Operation(summary = "Create group booking",
               description = "Create a new group booking as organizer")
    public ResponseEntity<ApiResponse<GroupBookingDto>> createGroup(
            @Valid @RequestBody CreateGroupBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        GroupBookingDto group = groupBookingService.createGroup(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(group));
    }
    
    /**
     * Join an existing group by code.
     */
    @PostMapping("/join/{groupCode}")
    @Operation(summary = "Join group",
               description = "Join an existing group booking using the group code")
    public ResponseEntity<ApiResponse<GroupBookingDto>> joinGroup(
            @PathVariable String groupCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        GroupBookingDto group = groupBookingService.joinGroup(groupCode, userId);
        return ResponseEntity.ok(ApiResponse.success(group));
    }
    
    // ==================== GET ====================
    
    /**
     * Get group by ID.
     * User must be a participant or organizer of the group.
     */
    @GetMapping("/{groupId}")
    @Operation(summary = "Get group details",
               description = "Returns full group details with all participants (only accessible by group members)")
    public ResponseEntity<ApiResponse<GroupBookingDto>> getGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return groupBookingService.getGroupForUser(groupId, userId)
                .map(group -> ResponseEntity.ok(ApiResponse.success(group)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get group by code.
     * User must be a participant or organizer of the group.
     */
    @GetMapping("/code/{groupCode}")
    @Operation(summary = "Get group by code",
               description = "Returns group details for a shareable code (only accessible by group members)")
    public ResponseEntity<ApiResponse<GroupBookingDto>> getGroupByCode(
            @PathVariable String groupCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return groupBookingService.getGroupByCodeForUser(groupCode, userId)
                .map(group -> ResponseEntity.ok(ApiResponse.success(group)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get current user's groups.
     */
    @GetMapping("/my-groups")
    @Operation(summary = "Get my groups",
               description = "Returns groups where user is organizer or participant")
    public ResponseEntity<ApiResponse<List<GroupBookingDto>>> getMyGroups(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        List<GroupBookingDto> groups = groupBookingService.getUserGroups(userId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }
    
    // ==================== ROOM SELECTION ====================
    
    /**
     * Select a room for the current user.
     */
    @PostMapping("/{groupId}/select-room")
    @Operation(summary = "Select room",
               description = "Select a room for the current participant")
    public ResponseEntity<ApiResponse<GroupBookingDto>> selectRoom(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        Long roomId = ((Number) request.get("roomId")).longValue();
        Integer numGuests = request.get("numGuests") != null ? 
                ((Number) request.get("numGuests")).intValue() : 1;
        
        GroupBookingDto group = groupBookingService.selectRoom(groupId, userId, roomId, numGuests);
        return ResponseEntity.ok(ApiResponse.success(group));
    }
    
    // ==================== ORGANIZER ACTIONS ====================
    
    /**
     * Lock the group (no more participants).
     */
    @PostMapping("/{groupId}/lock")
    @Operation(summary = "Lock group",
               description = "Organizer only: Lock group to prevent new participants")
    public ResponseEntity<ApiResponse<GroupBookingDto>> lockGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        GroupBookingDto group = groupBookingService.lockGroup(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success(group));
    }
    
    /**
     * Confirm all bookings in the group.
     */
    @PostMapping("/{groupId}/confirm")
    @Operation(summary = "Confirm group",
               description = "Organizer only: Confirm all bookings in the group")
    public ResponseEntity<ApiResponse<GroupBookingDto>> confirmGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        GroupBookingDto group = groupBookingService.confirmGroup(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success(group));
    }
    
    /**
     * Cancel the entire group.
     */
    @PostMapping("/{groupId}/cancel")
    @Operation(summary = "Cancel group",
               description = "Organizer only: Cancel the entire group booking")
    public ResponseEntity<ApiResponse<GroupBookingDto>> cancelGroup(
            @PathVariable Long groupId,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        String reason = request != null ? request.get("reason") : null;
        GroupBookingDto group = groupBookingService.cancelGroup(groupId, userId, reason);
        return ResponseEntity.ok(ApiResponse.success(group));
    }
    
    // ==================== PARTICIPANT ACTIONS ====================
    
    /**
     * Leave a group.
     */
    @PostMapping("/{groupId}/leave")
    @Operation(summary = "Leave group",
               description = "Leave a group booking (cannot be organizer)")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        groupBookingService.leaveGroup(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    // ==================== HELPERS ====================
    
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
