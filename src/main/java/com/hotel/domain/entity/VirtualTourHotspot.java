package com.hotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Hotspot in a virtual tour scene for navigation.
 * 
 * HOTSPOT TYPES:
 * - SCENE: Navigate to another scene
 * - INFO: Display information tooltip
 * - LINK: Open external URL
 */
@Entity
@Table(name = "virtual_tour_hotspots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualTourHotspot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private VirtualTourScene scene;
    
    /**
     * Type of hotspot interaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "hotspot_type", nullable = false)
    @lombok.Builder.Default
    private HotspotType hotspotType = HotspotType.SCENE;
    
    /**
     * Display text for the hotspot.
     */
    @Column(nullable = false, length = 255)
    private String text;
    
    /**
     * Pitch position (vertical angle in degrees).
     */
    @Column(nullable = false)
    private Double pitch;
    
    /**
     * Yaw position (horizontal angle in degrees).
     */
    @Column(nullable = false)
    private Double yaw;
    
    /**
     * Target scene ID for SCENE type hotspots.
     */
    @Column(name = "target_scene_id")
    private Long targetSceneId;
    
    /**
     * Target pitch when navigating to the target scene.
     */
    @Column(name = "target_pitch")
    private Double targetPitch;
    
    /**
     * Target yaw when navigating to the target scene.
     */
    @Column(name = "target_yaw")
    private Double targetYaw;
    
    /**
     * CSS class for styling (e.g., for different icons).
     */
    @Column(name = "css_class", length = 100)
    private String cssClass;
    
    /**
     * URL for LINK type hotspots.
     */
    @Column(length = 500)
    private String url;
    
    // ==================== Enums ====================
    
    public enum HotspotType {
        SCENE,  // Navigate to another scene
        INFO,   // Show information popup
        LINK    // Open external link
    }
}
