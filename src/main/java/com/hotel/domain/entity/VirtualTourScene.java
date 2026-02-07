package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Virtual Tour Scene - individual 360° view within a tour.
 * 
 * SCENE TYPES:
 * - IMAGE_360: Static 360° panoramic image
 * - VIDEO_360: 360° video content
 * - IMAGE_STANDARD: Regular image with pan/zoom
 */
@Entity
@Table(name = "virtual_tour_scenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualTourScene {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private VirtualTour tour;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Type of scene content.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scene_type", nullable = false)
    @lombok.Builder.Default
    private SceneType sceneType = SceneType.IMAGE_360;
    
    /**
     * Main media URL (360° image or video).
     */
    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;
    
    /**
     * Low-resolution preview for lazy loading.
     */
    @Column(name = "preview_url", length = 500)
    private String previewUrl;
    
    /**
     * Thumbnail for scene selection.
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    /**
     * Initial view pitch (vertical angle in degrees).
     */
    @Column(name = "initial_pitch")
    @lombok.Builder.Default
    private Double initialPitch = 0.0;
    
    /**
     * Initial view yaw (horizontal angle in degrees).
     */
    @Column(name = "initial_yaw")
    @lombok.Builder.Default
    private Double initialYaw = 0.0;
    
    /**
     * Initial horizontal field of view.
     */
    @Column(name = "initial_hfov")
    @lombok.Builder.Default
    private Double initialHfov = 100.0;
    
    /**
     * Whether this is the starting scene for the tour.
     */
    @Column(name = "is_start_scene")
    @lombok.Builder.Default
    private Boolean isStartScene = false;
    
    @Column(name = "sort_order")
    @lombok.Builder.Default
    private Integer sortOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // ==================== Relationships ====================
    
    /**
     * Hotspots in this scene that link to other scenes.
     */
    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VirtualTourHotspot> hotspots = new ArrayList<>();
    
    // ==================== Lifecycle ====================
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // ==================== Helpers ====================
    
    public void addHotspot(VirtualTourHotspot hotspot) {
        hotspots.add(hotspot);
        hotspot.setScene(this);
    }
    
    // ==================== Enums ====================
    
    public enum SceneType {
        IMAGE_360,      // 360° panoramic image
        VIDEO_360,      // 360° video
        IMAGE_STANDARD  // Regular image with pan/zoom
    }
}
