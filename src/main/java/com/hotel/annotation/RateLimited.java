package com.hotel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply rate limiting to API endpoints.
 * When applied to a method, the RateLimitAspect enforces the specified limits.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Maximum number of requests allowed within the time window.
     */
    int limit() default 10;
    
    /**
     * Time window in seconds.
     */
    int windowSeconds() default 3600;  // 1 hour default
    
    /**
     * Key prefix for rate limit bucket (e.g., "review", "booking").
     * Combined with user ID or IP for unique rate limit key.
     */
    String key() default "default";
    
    /**
     * Error message shown when rate limit exceeded.
     */
    String message() default "Too many requests. Please try again later.";
}
