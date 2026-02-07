package com.hotel.repository;

import com.hotel.domain.entity.HotelOwnerAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for HotelOwnerAuditLog entity operations.
 */
@Repository
public interface HotelOwnerAuditLogRepository extends JpaRepository<HotelOwnerAuditLog, Long> {

    /**
     * Find audit logs by hotel ID.
     */
    List<HotelOwnerAuditLog> findByHotelIdOrderByCreatedAtDesc(Long hotelId);

    /**
     * Find audit logs by hotel ID with pagination.
     */
    Page<HotelOwnerAuditLog> findByHotelId(Long hotelId, Pageable pageable);

    /**
     * Find audit logs by owner ID.
     */
    List<HotelOwnerAuditLog> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /**
     * Find audit logs by action type.
     */
    List<HotelOwnerAuditLog> findByHotelIdAndActionType(
            Long hotelId, HotelOwnerAuditLog.ActionType actionType);

    /**
     * Find audit logs by entity type.
     */
    List<HotelOwnerAuditLog> findByHotelIdAndEntityType(
            Long hotelId, HotelOwnerAuditLog.EntityType entityType);

    /**
     * Find audit logs within a date range.
     */
    List<HotelOwnerAuditLog> findByHotelIdAndCreatedAtBetween(
            Long hotelId, LocalDateTime start, LocalDateTime end);

    /**
     * Find audit logs for a specific entity.
     */
    List<HotelOwnerAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            HotelOwnerAuditLog.EntityType entityType, Long entityId);

    /**
     * Count audit logs by hotel ID.
     */
    long countByHotelId(Long hotelId);

    /**
     * Find recent audit logs for a hotel.
     */
    List<HotelOwnerAuditLog> findTop20ByHotelIdOrderByCreatedAtDesc(Long hotelId);
}
