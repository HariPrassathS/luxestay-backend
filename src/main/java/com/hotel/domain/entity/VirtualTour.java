package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Virtual Tour entity for 360° hotel experiences.
 * 
 * FEATURES:
 * - Multiple scenes per tour (lobby, rooms, pool, etc.)
 * - Hotspots for navigation between scenes
 * - Supports 360° images and videos
 * - Lazy-loaded media for performance
 */
@Entity
@Table(name = "virtual_tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualTour {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Thumbnail image for tour preview.
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    /**
     * Whether the tour is active and visible.
     */
    @Column(name = "is_active", nullable = false)
    @lombok.Builder.Default
    private Boolean isActive = true;
    
    /**
     * Display order for multiple tours.
     */
    @Column(name = "sort_order")
    @lombok.Builder.Default
    private Integer sortOrder = 0;
    
    /**
     * Total view count for analytics.
     */
    @Column(name = "view_count")
    @lombok.Builder.Default
    private Long viewCount = 0L;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // ==================== Relationships ====================
    
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<VirtualTourScene> scenes = new ArrayList<>();
    
    // ==================== Lifecycle ====================
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== Helpers ====================
    
    public void addScene(VirtualTourScene scene) {
        scenes.add(scene);
        scene.setTour(this);
    }
    
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }
    
    public VirtualTourScene getStartScene() {
        return scenes.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsStartScene()))
                .findFirst()
                .orElse(scenes.isEmpty() ? null : scenes.get(0));
    }
}
