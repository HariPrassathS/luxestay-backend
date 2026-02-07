package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ReviewAuditLog entity for tracking admin moderation actions
 */
@Entity
@Table(name = "review_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewAuditAction action;
    
    @Column(name = "previous_status", length = 20)
    private String previousStatus;
    
    @Column(name = "new_status", length = 20)
    private String newStatus;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
