package com.hotel.service;

import com.hotel.domain.dto.wishlist.WishlistItemDto;
import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.User;
import com.hotel.domain.entity.Wishlist;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling wishlist/favorites operations.
 */
@Service
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistRepository wishlistRepository;
    private final HotelRepository hotelRepository;
    private final AuthService authService;

    public WishlistService(WishlistRepository wishlistRepository, 
                          HotelRepository hotelRepository,
                          AuthService authService) {
        this.wishlistRepository = wishlistRepository;
        this.hotelRepository = hotelRepository;
        this.authService = authService;
    }

    /**
     * Get all wishlist items for current user.
     */
    @Transactional(readOnly = true)
    public List<WishlistItemDto> getWishlist() {
        User user = authService.getCurrentUserEntity();
        logger.debug("Getting wishlist for user: {}", user.getId());
        
        List<Wishlist> items = wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Add hotel to wishlist.
     */
    @Transactional
    public WishlistItemDto addToWishlist(Long hotelId) {
        User user = authService.getCurrentUserEntity();
        logger.info("User {} adding hotel {} to wishlist", user.getId(), hotelId);
        
        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndHotelId(user.getId(), hotelId)) {
            logger.debug("Hotel {} already in user {} wishlist", hotelId, user.getId());
            Wishlist existing = wishlistRepository.findByUserIdAndHotelId(user.getId(), hotelId)
                    .orElseThrow();
            return toDto(existing);
        }
        
        // Get hotel
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId));
        
        // Create wishlist entry
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .hotel(hotel)
                .build();
        
        wishlist = wishlistRepository.save(wishlist);
        logger.info("Hotel {} added to user {} wishlist", hotelId, user.getId());
        
        return toDto(wishlist);
    }

    /**
     * Remove hotel from wishlist.
     */
    @Transactional
    public void removeFromWishlist(Long hotelId) {
        User user = authService.getCurrentUserEntity();
        logger.info("User {} removing hotel {} from wishlist", user.getId(), hotelId);
        
        wishlistRepository.deleteByUserIdAndHotelId(user.getId(), hotelId);
        logger.info("Hotel {} removed from user {} wishlist", hotelId, user.getId());
    }

    /**
     * Check if a hotel is in current user's wishlist.
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long hotelId) {
        User user = authService.getCurrentUserEntity();
        return wishlistRepository.existsByUserIdAndHotelId(user.getId(), hotelId);
    }

    /**
     * Get set of hotel IDs in current user's wishlist.
     * Useful for bulk checking when rendering hotel lists.
     */
    @Transactional(readOnly = true)
    public Set<Long> getWishlistedHotelIds() {
        User user = authService.getCurrentUserEntity();
        return wishlistRepository.findHotelIdsByUserId(user.getId())
                .stream().collect(Collectors.toSet());
    }

    /**
     * Toggle hotel in wishlist (add if not present, remove if present).
     * Returns true if added, false if removed.
     */
    @Transactional
    public boolean toggleWishlist(Long hotelId) {
        User user = authService.getCurrentUserEntity();
        
        if (wishlistRepository.existsByUserIdAndHotelId(user.getId(), hotelId)) {
            wishlistRepository.deleteByUserIdAndHotelId(user.getId(), hotelId);
            logger.info("Hotel {} removed from user {} wishlist (toggle)", hotelId, user.getId());
            return false;
        } else {
            Hotel hotel = hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId));
            
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .hotel(hotel)
                    .build();
            wishlistRepository.save(wishlist);
            logger.info("Hotel {} added to user {} wishlist (toggle)", hotelId, user.getId());
            return true;
        }
    }

    /**
     * Get wishlist count for current user.
     */
    @Transactional(readOnly = true)
    public long getWishlistCount() {
        User user = authService.getCurrentUserEntity();
        return wishlistRepository.countByUserId(user.getId());
    }

    /**
     * Convert Wishlist entity to DTO.
     */
    private WishlistItemDto toDto(Wishlist wishlist) {
        Hotel hotel = wishlist.getHotel();
        Double minPriceDouble = hotel.getMinPrice();
        java.math.BigDecimal minPrice = minPriceDouble != null 
                ? java.math.BigDecimal.valueOf(minPriceDouble) 
                : java.math.BigDecimal.ZERO;
        
        return WishlistItemDto.builder()
                .id(wishlist.getId())
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelCity(hotel.getCity())
                .hotelCountry(hotel.getCountry())
                .hotelImageUrl(hotel.getHeroImageUrl())
                .starRating(hotel.getStarRating())
                .minPrice(minPrice)
                .featured(false) // Hotel entity doesn't have featured field
                .addedAt(wishlist.getCreatedAt())
                .build();
    }
}
