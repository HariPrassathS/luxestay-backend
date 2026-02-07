package com.hotel.domain.dto.virtualtour;

import lombok.*;

import java.util.List;

/**
 * DTO for Virtual Tour data.
 * 
 * LAZY LOADING SUPPORT:
 * - scenes list may be empty on initial load
 * - Individual scenes loaded on demand
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualTourDto {
    
    private Long id;
    private Long hotelId;
    private String name;
    private String description;
    private String thumbnailUrl;
    private Long viewCount;
    private Integer sceneCount;
    private Long startSceneId;
    
    /**
     * Scenes in this tour.
     * May be empty for lazy loading (use scene endpoint).
     */
    private List<VirtualTourSceneDto> scenes;
}
