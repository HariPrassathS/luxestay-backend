package com.hotel.domain.dto.auth;

import com.hotel.domain.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses containing JWT token and user info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    @lombok.Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDto user;

    public AuthResponse(String accessToken, Long expiresIn, UserDto user) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
