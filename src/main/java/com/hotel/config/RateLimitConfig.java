package com.hotel.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting configuration using Caffeine cache.
 * Provides in-memory rate limiting for API abuse prevention.
 */
@Configuration
public class RateLimitConfig {

    /**
     * Cache for storing rate limit counters.
     * Key: "userId:action" or "ip:action"
     * Value: AtomicInteger counter with request count
     * 
     * Entries expire after 1 hour (sliding window approximation).
     */
    @Bean
    public Cache<String, AtomicInteger> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(10000)
                .build();
    }
    
    /**
     * Rate limit configuration values.
     */
    public static class Limits {
        // Reviews: max 5 per hour per user
        public static final int REVIEW_SUBMIT_LIMIT = 5;
        public static final int REVIEW_SUBMIT_WINDOW_HOURS = 1;
        
        // Reply: max 10 per hour per hotel owner
        public static final int REPLY_LIMIT = 10;
        public static final int REPLY_WINDOW_HOURS = 1;
        
        // Bookings: max 10 per hour per user
        public static final int BOOKING_LIMIT = 10;
        public static final int BOOKING_WINDOW_HOURS = 1;
        
        // Helpful votes: max 20 per hour per user
        public static final int HELPFUL_VOTE_LIMIT = 20;
        public static final int HELPFUL_VOTE_WINDOW_HOURS = 1;
    }
}
