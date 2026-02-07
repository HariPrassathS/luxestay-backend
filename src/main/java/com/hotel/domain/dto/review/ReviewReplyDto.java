package com.hotel.domain.dto.review;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for review reply data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReplyDto {
    
    private Long id;
    private Long reviewId;
    private Long ownerId;
    private String ownerName;
    private String replyText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
