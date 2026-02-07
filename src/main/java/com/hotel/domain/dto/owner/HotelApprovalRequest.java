package com.hotel.domain.dto.owner;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin to approve or reject a hotel registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelApprovalRequest {

    @NotNull(message = "Approved flag is required")
    private Boolean approved;

    /**
     * Required if approved is false.
     * Reason for rejection shown to the hotel owner.
     */
    @Size(max = 1000, message = "Rejection reason must be at most 1000 characters")
    private String rejectionReason;
}
