package com.hotel.service;

import com.hotel.domain.entity.HotelOwnerAuditLog;
import com.hotel.domain.entity.HotelOwnerAuditLog.ActionType;
import com.hotel.domain.entity.HotelOwnerAuditLog.EntityType;
import com.hotel.domain.entity.User;
import com.hotel.repository.HotelOwnerAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Service for creating audit logs for hotel owner actions.
 * All logging is done asynchronously to not impact request performance.
 */
@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final HotelOwnerAuditLogRepository auditLogRepository;

    public AuditLogService(HotelOwnerAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Log an action asynchronously.
     */
    @Async
    @Transactional
    public void logAction(Long hotelId, Long ownerId, ActionType actionType,
                          EntityType entityType, Long entityId, String description) {
        logAction(hotelId, ownerId, actionType, entityType, entityId, null, null, description);
    }

    /**
     * Log an action with old and new values.
     */
    @Async
    @Transactional
    public void logAction(Long hotelId, Long ownerId, ActionType actionType,
                          EntityType entityType, Long entityId,
                          String oldValue, String newValue, String description) {
        try {
            String ipAddress = null;
            String userAgent = null;

            // Try to get request information
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipAddress = getClientIP(request);
                userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500);
                }
            }

            HotelOwnerAuditLog log = HotelOwnerAuditLog.builder()
                    .hotelId(hotelId)
                    .ownerId(ownerId)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .description(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(log);
            logger.debug("Audit log created: {} {} on {} {}",
                    actionType, entityType, entityId, description);

        } catch (Exception e) {
            // Don't let audit logging failures affect the main operation
            logger.error("Failed to create audit log", e);
        }
    }

    /**
     * Log an action for the current user.
     */
    public void logCurrentUserAction(ActionType actionType, EntityType entityType,
                                     Long entityId, String description) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User user) {
                if (user.getHotelId() != null) {
                    logAction(user.getHotelId(), user.getId(), actionType,
                            entityType, entityId, description);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to log current user action", e);
        }
    }

    /**
     * Log a price change.
     */
    public void logPriceChange(Long hotelId, Long ownerId, Long roomId,
                               String oldPrice, String newPrice) {
        logAction(hotelId, ownerId, ActionType.PRICE_CHANGE, EntityType.ROOM, roomId,
                oldPrice, newPrice, "Room price changed from " + oldPrice + " to " + newPrice);
    }

    /**
     * Log an availability change.
     */
    public void logAvailabilityChange(Long hotelId, Long ownerId, Long roomId,
                                      boolean oldAvailability, boolean newAvailability) {
        logAction(hotelId, ownerId, ActionType.AVAILABILITY_CHANGE, EntityType.ROOM, roomId,
                String.valueOf(oldAvailability), String.valueOf(newAvailability),
                "Room availability changed to " + (newAvailability ? "available" : "unavailable"));
    }

    /**
     * Log a booking status change.
     */
    public void logBookingStatusChange(Long hotelId, Long ownerId, Long bookingId,
                                       String oldStatus, String newStatus) {
        ActionType actionType = switch (newStatus) {
            case "CHECKED_IN" -> ActionType.CHECK_IN;
            case "CHECKED_OUT" -> ActionType.CHECK_OUT;
            default -> ActionType.STATUS_CHANGE;
        };
        logAction(hotelId, ownerId, actionType, EntityType.BOOKING, bookingId,
                oldStatus, newStatus, "Booking status changed from " + oldStatus + " to " + newStatus);
    }

    /**
     * Get audit logs for a hotel.
     */
    @Transactional(readOnly = true)
    public List<HotelOwnerAuditLog> getHotelAuditLogs(Long hotelId) {
        return auditLogRepository.findByHotelIdOrderByCreatedAtDesc(hotelId);
    }

    /**
     * Get recent audit logs for a hotel.
     */
    @Transactional(readOnly = true)
    public List<HotelOwnerAuditLog> getRecentAuditLogs(Long hotelId) {
        return auditLogRepository.findTop20ByHotelIdOrderByCreatedAtDesc(hotelId);
    }

    /**
     * Get client IP address from request, handling proxies.
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs (take first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
