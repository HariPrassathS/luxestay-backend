package com.hotel.repository;

import com.hotel.domain.entity.Referral;
import com.hotel.domain.entity.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {

    Optional<Referral> findByReferralCode(String referralCode);

    Optional<Referral> findByReferredId(Long referredUserId);

    List<Referral> findByReferrerIdOrderByCreatedAtDesc(Long referrerId);

    List<Referral> findByReferrerIdAndStatus(Long referrerId, ReferralStatus status);

    /**
     * Count successful referrals for a user.
     */
    long countByReferrerIdAndStatus(Long referrerId, ReferralStatus status);

    /**
     * Sum XP earned from referrals.
     */
    @Query("SELECT COALESCE(SUM(r.referrerXpEarned), 0) FROM Referral r WHERE r.referrer.id = :userId")
    Integer sumReferralXpEarned(@Param("userId") Long userId);

    boolean existsByReferredId(Long referredUserId);
}
