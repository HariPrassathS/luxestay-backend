package com.hotel.domain.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for wishlist item responses.
 * Includes hotel summary data for display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemDto {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private String hotelCity;
    private String hotelCountry;
    private String hotelImageUrl;
    private Integer starRating;
    private java.math.BigDecimal minPrice;
    private Boolean featured;
    private LocalDateTime addedAt;
}
