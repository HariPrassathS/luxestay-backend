package com.hotel.domain.dto.review;

import com.hotel.domain.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating review moderation status (admin only).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReviewStatusRequest {

    @NotNull(message = "Status is required")
    private ReviewStatus status;

    /**
     * Required when status is REJECTED.
     */
    private String reason;
}
