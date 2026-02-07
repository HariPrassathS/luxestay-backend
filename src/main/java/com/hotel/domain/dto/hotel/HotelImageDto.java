package com.hotel.domain.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for hotel images.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelImageDto {

    private Long id;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
}
