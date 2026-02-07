package com.hotel.domain.entity;

/**
 * Enumeration representing user roles in the system.
 * Used for role-based access control (RBAC).
 */
public enum Role {
    USER,        // Regular customer who can browse and make bookings
    ADMIN,       // System administrator with full platform access
    HOTEL_OWNER  // Hotel owner with restricted access to their own hotel only
}
