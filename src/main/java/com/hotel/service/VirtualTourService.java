package com.hotel.service;

import com.hotel.domain.dto.virtualtour.VirtualTourDto;
import com.hotel.domain.dto.virtualtour.VirtualTourSceneDto;
import com.hotel.domain.entity.VirtualTour;
import com.hotel.domain.entity.VirtualTourHotspot;
import com.hotel.domain.entity.VirtualTourScene;
import com.hotel.repository.VirtualTourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for Virtual Hotel Tours.
 * 
 * PERFORMANCE FEATURES:
 * - Lazy loading of scenes (loaded on demand)
 * - Preview images for fast initial render
 * - Low-res to high-res progressive loading
 * - View count tracking (async)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VirtualTourService {
    
    private final VirtualTourRepository tourRepository;
    
    /**
     * Get all tours for a hotel (metadata only, no scenes).
     */
    public List<VirtualTourDto> getToursByHotel(Long hotelId) {
        return tourRepository.findActiveByHotelId(hotelId).stream()
                .map(this::toDto)
                .toList();
    }
    
    /**
     * Get tour details with all scenes.
     * Increments view count.
     */
    @Transactional
    public Optional<VirtualTourDto> getTourWithScenes(Long tourId) {
        Optional<VirtualTour> tourOpt = tourRepository.findByIdWithScenes(tourId);
        
        if (tourOpt.isPresent()) {
            tourRepository.incrementViewCount(tourId);
            return tourOpt.map(this::toDtoWithScenes);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get a single scene with hotspots.
     * For lazy loading individual scenes.
     */
    public Optional<VirtualTourSceneDto> getScene(Long tourId, Long sceneId) {
        return tourRepository.findByIdWithScenes(tourId)
                .flatMap(tour -> tour.getScenes().stream()
                        .filter(s -> s.getId().equals(sceneId))
                        .findFirst()
                        .map(this::toSceneDto));
    }
    
    /**
     * Check if a hotel has virtual tours available.
     */
    public boolean hasVirtualTours(Long hotelId) {
        return tourRepository.existsByHotel_IdAndIsActiveTrue(hotelId);
    }
    
    /**
     * Get tour count for a hotel.
     */
    public long getTourCount(Long hotelId) {
        return tourRepository.countByHotel_IdAndIsActiveTrue(hotelId);
    }
    
    // ==================== DTO CONVERSIONS ====================
    
    private VirtualTourDto toDto(VirtualTour tour) {
        return VirtualTourDto.builder()
                .id(tour.getId())
                .hotelId(tour.getHotel().getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .thumbnailUrl(tour.getThumbnailUrl())
                .viewCount(tour.getViewCount())
                .sceneCount(tour.getScenes().size())
                .scenes(Collections.emptyList()) // Lazy - not loaded here
                .build();
    }
    
    private VirtualTourDto toDtoWithScenes(VirtualTour tour) {
        VirtualTourScene startScene = tour.getStartScene();
        
        return VirtualTourDto.builder()
                .id(tour.getId())
                .hotelId(tour.getHotel().getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .thumbnailUrl(tour.getThumbnailUrl())
                .viewCount(tour.getViewCount())
                .sceneCount(tour.getScenes().size())
                .startSceneId(startScene != null ? startScene.getId() : null)
                .scenes(tour.getScenes().stream()
                        .map(this::toSceneDto)
                        .toList())
                .build();
    }
    
    private VirtualTourSceneDto toSceneDto(VirtualTourScene scene) {
        return VirtualTourSceneDto.builder()
                .id(scene.getId())
                .name(scene.getName())
                .description(scene.getDescription())
                .sceneType(scene.getSceneType().name())
                .mediaUrl(scene.getMediaUrl())
                .previewUrl(scene.getPreviewUrl())
                .thumbnailUrl(scene.getThumbnailUrl())
                .initialPitch(scene.getInitialPitch())
                .initialYaw(scene.getInitialYaw())
                .initialHfov(scene.getInitialHfov())
                .isStartScene(scene.getIsStartScene())
                .hotspots(scene.getHotspots().stream()
                        .map(this::toHotspotDto)
                        .toList())
                .build();
    }
    
    private VirtualTourSceneDto.HotspotDto toHotspotDto(VirtualTourHotspot hotspot) {
        return VirtualTourSceneDto.HotspotDto.builder()
                .id(hotspot.getId())
                .type(hotspot.getHotspotType().name())
                .text(hotspot.getText())
                .pitch(hotspot.getPitch())
                .yaw(hotspot.getYaw())
                .targetSceneId(hotspot.getTargetSceneId())
                .targetPitch(hotspot.getTargetPitch())
                .targetYaw(hotspot.getTargetYaw())
                .cssClass(hotspot.getCssClass())
                .url(hotspot.getUrl())
                .build();
    }
}
