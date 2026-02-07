package com.hotel.repository;

import com.hotel.domain.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ReviewReply entity operations.
 */
@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

    /**
     * Find the reply for a specific review.
     */
    Optional<ReviewReply> findByReview_Id(Long reviewId);

    /**
     * Check if a reply exists for a review.
     */
    boolean existsByReview_Id(Long reviewId);

    /**
     * Find all replies by an owner.
     */
    List<ReviewReply> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    /**
     * Delete the reply for a review.
     */
    void deleteByReview_Id(Long reviewId);
}
