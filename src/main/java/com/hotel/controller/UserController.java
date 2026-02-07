package com.hotel.controller;

import com.hotel.domain.dto.auth.ChangePasswordRequest;
import com.hotel.domain.dto.auth.SetPasswordRequest;
import com.hotel.domain.dto.common.ApiResponse;
import com.hotel.domain.dto.user.UpdateUserRequest;
import com.hotel.domain.dto.user.UserDto;
import com.hotel.domain.entity.User;
import com.hotel.service.AuthService;
import com.hotel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 * Requires authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Get current user's profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get the profile of the currently authenticated user")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        UserDto user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Update current user's profile.
     */
    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update the current user's profile information")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@Valid @RequestBody UpdateUserRequest request) {
        User currentUser = authService.getCurrentUserEntity();
        UserDto updatedUser = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }

    /**
     * Set password for first-time login users.
     * This endpoint is for users who must change their password after first login.
     */
    @PostMapping("/set-password")
    @Operation(summary = "Set password", description = "Set a new password for first-time login or password reset")
    public ResponseEntity<ApiResponse<UserDto>> setPassword(
            @Valid @RequestBody SetPasswordRequest request,
            HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUserEntity();
        String ipAddress = extractIpAddress(httpRequest);
        UserDto updatedUser = userService.setPassword(currentUser.getId(), request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Password set successfully", updatedUser));
    }

    /**
     * Change password for existing users (requires current password).
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password (requires current password verification)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUserEntity();
        String ipAddress = extractIpAddress(httpRequest);
        userService.changePassword(currentUser.getId(), request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    /**
     * Extract client IP address considering proxies.
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
