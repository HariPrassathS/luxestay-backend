package com.hotel.security;

import com.hotel.domain.entity.Role;
import com.hotel.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 * Enhanced to include role and hotelId claims for HOTEL_OWNER support.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Custom claim keys
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_HOTEL_ID = "hotelId";
    public static final String CLAIM_MUST_CHANGE_PASSWORD = "mustChangePassword";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Securely generate the key from the secret string.
        // This ensures the key is always the correct size for the algorithm.
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT token from Authentication object.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails instanceof User user) {
            return generateToken(user);
        }
        return generateToken(userDetails.getUsername());
    }

    /**
     * Generate JWT token from User entity with full claims.
     * Includes role, hotelId (for HOTEL_OWNER), and mustChangePassword flag.
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ROLE, user.getRole().name());
        claims.put(CLAIM_MUST_CHANGE_PASSWORD, user.getMustChangePassword());

        // Include hotelId for HOTEL_OWNER role
        if (Role.HOTEL_OWNER.equals(user.getRole()) && user.getHotelId() != null) {
            claims.put(CLAIM_HOTEL_ID, user.getHotelId());
        }

        return Jwts.builder()
                .subject(user.getEmail())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Generate JWT token from username/email (basic token without extra claims).
     * Use generateToken(User) for full claims including role and hotelId.
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Get username/email from JWT token.
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * Get role from JWT token.
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get(CLAIM_ROLE, String.class);
    }

    /**
     * Get hotelId from JWT token (for HOTEL_OWNER).
     */
    public Long getHotelIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object hotelIdObj = claims.get(CLAIM_HOTEL_ID);
        if (hotelIdObj != null) {
            if (hotelIdObj instanceof Number) {
                return ((Number) hotelIdObj).longValue();
            }
            return Long.parseLong(hotelIdObj.toString());
        }
        return null;
    }

    /**
     * Check if user must change password.
     */
    public Boolean getMustChangePasswordFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get(CLAIM_MUST_CHANGE_PASSWORD, Boolean.class);
    }

    /**
     * Get all claims from token.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate JWT token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        } catch (SecurityException ex) {
            logger.error("JWT signature validation failed");
        }
        return false;
    }

    /**
     * Get token expiration time in milliseconds.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}
