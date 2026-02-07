package com.hotel.domain.dto.virtualtour;

import lombok.*;

import java.util.List;

/**
 * DTO for Virtual Tour Scene.
 * 
 * MEDIA LOADING STRATEGY:
 * 1. thumbnailUrl - Fast initial load
 * 2. previewUrl - Low-res placeholder
 * 3. mediaUrl - Full resolution on view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualTourSceneDto {
    
    private Long id;
    private String name;
    private String description;
    
    /**
     * Type: IMAGE_360, VIDEO_360, IMAGE_STANDARD
     */
    private String sceneType;
    
    /**
     * Full-resolution 360° image/video URL.
     */
    private String mediaUrl;
    
    /**
     * Low-resolution preview for fast loading.
     */
    private String previewUrl;
    
    /**
     * Thumbnail for scene selection.
     */
    private String thumbnailUrl;
    
    /**
     * Initial view angles and field of view.
     */
    private Double initialPitch;
    private Double initialYaw;
    private Double initialHfov;
    
    private Boolean isStartScene;
    
    /**
     * Navigation hotspots in this scene.
     */
    private List<HotspotDto> hotspots;
    
    /**
     * Hotspot data for scene navigation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotspotDto {
        private Long id;
        private String type; // SCENE, INFO, LINK
        private String text;
        private Double pitch;
        private Double yaw;
        private Long targetSceneId;
        private Double targetPitch;
        private Double targetYaw;
        private String cssClass;
        private String url;
    }
}
