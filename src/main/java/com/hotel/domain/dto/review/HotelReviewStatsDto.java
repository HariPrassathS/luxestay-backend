package com.hotel.domain.dto.review;

import lombok.*;

/**
 * DTO for hotel review statistics
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelReviewStatsDto {
    
    private Long hotelId;
    private String hotelName;
    private int totalReviews;
    private Double averageRating;
    private int fiveStarCount;
    private int fourStarCount;
    private int threeStarCount;
    private int twoStarCount;
    private int oneStarCount;
    private Double fiveStarPercent;
    private Double fourStarPercent;
    private Double threeStarPercent;
    private Double twoStarPercent;
    private Double oneStarPercent;
    
    /**
     * Calculate star rating percentages based on counts.
     */
    public void calculatePercentages() {
        if (totalReviews > 0) {
            double total = (double) totalReviews;
            this.fiveStarPercent = (fiveStarCount / total) * 100.0;
            this.fourStarPercent = (fourStarCount / total) * 100.0;
            this.threeStarPercent = (threeStarCount / total) * 100.0;
            this.twoStarPercent = (twoStarCount / total) * 100.0;
            this.oneStarPercent = (oneStarCount / total) * 100.0;
        } else {
            this.fiveStarPercent = 0.0;
            this.fourStarPercent = 0.0;
            this.threeStarPercent = 0.0;
            this.twoStarPercent = 0.0;
            this.oneStarPercent = 0.0;
        }
    }
}
