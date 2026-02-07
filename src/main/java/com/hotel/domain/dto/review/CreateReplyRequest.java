package com.hotel.domain.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a reply to a review.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReplyRequest {

    @NotBlank(message = "Reply text is required")
    @Size(min = 10, max = 2000, message = "Reply must be between 10 and 2000 characters")
    private String replyText;
}
