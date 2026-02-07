package com.hotel.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.hotel.annotation.RateLimited;
import com.hotel.domain.entity.User;
import com.hotel.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aspect that enforces rate limiting on methods annotated with @RateLimited.
 * Uses Caffeine cache for in-memory rate tracking.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final Cache<String, AtomicInteger> rateLimitCache;

    @Around("@annotation(rateLimited)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String clientKey = getClientKey(rateLimited.key());
        
        AtomicInteger counter = rateLimitCache.get(clientKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();
        
        if (currentCount > rateLimited.limit()) {
            log.warn("Rate limit exceeded for key: {} (count: {}, limit: {})", 
                    clientKey, currentCount, rateLimited.limit());
            throw new RateLimitExceededException(rateLimited.message());
        }
        
        log.debug("Rate limit check passed for key: {} (count: {}/{})", 
                clientKey, currentCount, rateLimited.limit());
        
        return joinPoint.proceed();
    }

    /**
     * Build a unique key for rate limiting based on user ID (if authenticated) or IP address.
     */
    private String getClientKey(String prefix) {
        String identifier;
        
        // Try to get authenticated user ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            identifier = "user:" + user.getId();
        } else {
            // Fall back to IP address
            identifier = "ip:" + getClientIp();
        }
        
        return prefix + ":" + identifier;
    }

    /**
     * Extract client IP address from request, handling proxies.
     */
    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attrs.getRequest();
        
        // Check common proxy headers
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
}
