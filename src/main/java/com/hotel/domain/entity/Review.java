package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Review entity representing a guest review for a hotel stay
 */
@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;
    
    @Column(name = "is_verified_stay")
    @Builder.Default
    private Boolean isVerifiedStay = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReviewReply reply;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public void approve() {
        this.status = ReviewStatus.APPROVED;
        this.rejectionReason = null;
    }
    
    public void reject() {
        this.status = ReviewStatus.REJECTED;
    }
    
    public void reject(String reason) {
        this.status = ReviewStatus.REJECTED;
        this.rejectionReason = reason;
    }
    
    public void flag() {
        this.status = ReviewStatus.FLAGGED;
    }
    
    public boolean isPending() {
        return this.status == ReviewStatus.PENDING;
    }
    
    public boolean isApproved() {
        return this.status == ReviewStatus.APPROVED;
    }
    
    public boolean isFlagged() {
        return this.status == ReviewStatus.FLAGGED;
    }
}
