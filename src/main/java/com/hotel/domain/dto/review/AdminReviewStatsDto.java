package com.hotel.domain.dto.review;

import lombok.*;

/**
 * DTO for admin review dashboard statistics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReviewStatsDto {

    private Long totalReviews;
    private Long pendingReviews;
    private Long approvedReviews;
    private Long rejectedReviews;
    private Long flaggedReviews;
}
