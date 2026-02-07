package com.hotel.domain.dto.review;

import com.hotel.domain.entity.ReviewStatus;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for review data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    
    private Long id;
    private Long bookingId;
    private String bookingReference;
    private Long hotelId;
    private String hotelName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private ReviewStatus status;
    private String rejectionReason;
    private Boolean isVerifiedStay;
    private Integer helpfulCount;
    private String roomName;
    private String stayDates;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ReviewReplyDto reply;
}
