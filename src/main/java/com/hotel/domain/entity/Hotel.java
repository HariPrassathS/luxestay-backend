package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hotel entity representing a hotel property.
 */
@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 120)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "star_rating")
    @lombok.Builder.Default
    private Integer starRating = 3;

    @Column(name = "hero_image_url", columnDefinition = "TEXT")
    private String heroImageUrl;

    @Column(columnDefinition = "JSON")
    private String amenities;

    @Column(name = "check_in_time")
    @lombok.Builder.Default
    private LocalTime checkInTime = LocalTime.of(14, 0);

    @Column(name = "check_out_time")
    @lombok.Builder.Default
    private LocalTime checkOutTime = LocalTime.of(11, 0);

    @Column(name = "is_active", nullable = false)
    @lombok.Builder.Default
    private Boolean isActive = true;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // ==================== Approval & Owner Fields ====================

    /**
     * Approval status for newly registered hotels.
     * PENDING = awaiting admin approval
     * APPROVED = visible to public
     * REJECTED = not approved, reason should be in rejection_reason
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    @lombok.Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    /**
     * Reason for rejection if approvalStatus is REJECTED.
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Timestamp when the hotel was approved or rejected.
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Admin who approved/rejected the hotel.
     */
    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Relationships ====================

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<HotelImage> images = new ArrayList<>();

    /**
     * The owner of this hotel (HOTEL_OWNER user).
     * Note: This is a read-only relationship; the actual FK is in users.hotel_id.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "hotel_id", insertable = false, updatable = false)
    private User owner;

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (starRating == null) {
            starRating = 3;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Helper Methods ====================

    public void addRoom(Room room) {
        rooms.add(room);
        room.setHotel(this);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        room.setHotel(null);
    }

    public void addImage(HotelImage image) {
        images.add(image);
        image.setHotel(this);
    }

    public void removeImage(HotelImage image) {
        images.remove(image);
        image.setHotel(null);
    }

    /**
     * Get the minimum price per night across all available rooms.
     */
    public Double getMinPrice() {
        return rooms.stream()
                .filter(Room::getIsAvailable)
                .mapToDouble(room -> room.getPricePerNight().doubleValue())
                .min()
                .orElse(0.0);
    }

    /**
     * Get the count of available rooms.
     */
    public long getAvailableRoomCount() {
        return rooms.stream()
                .filter(Room::getIsAvailable)
                .count();
    }

    /**
     * Check if the hotel is approved and active (visible to public).
     */
    public boolean isVisibleToPublic() {
        return Boolean.TRUE.equals(isActive) && ApprovalStatus.APPROVED.equals(approvalStatus);
    }

    /**
     * Check if the hotel is pending approval.
     */
    public boolean isPendingApproval() {
        return ApprovalStatus.PENDING.equals(approvalStatus);
    }

    // ==================== Approval Status Enum ====================

    /**
     * Approval status for hotel registration workflow.
     */
    public enum ApprovalStatus {
        PENDING,   // Awaiting admin approval
        APPROVED,  // Approved and visible to public
        REJECTED   // Rejected by admin
    }
}
