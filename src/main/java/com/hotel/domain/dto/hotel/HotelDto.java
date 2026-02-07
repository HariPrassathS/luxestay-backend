package com.hotel.domain.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO for hotel information in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private Integer starRating;
    private String heroImageUrl;
    private List<String> amenities;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Boolean isActive;
    private Double latitude;
    private Double longitude;
    private Double minPrice;
    private Long availableRooms;
    private List<HotelImageDto> images;
}
