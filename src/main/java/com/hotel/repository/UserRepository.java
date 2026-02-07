package com.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotel.domain.entity.Role;
import com.hotel.domain.entity.User;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email address.
     * Used for authentication and duplicate checking.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by email and active status.
     */
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);

    /**
     * Find all users with a specific role.
     */
    List<User> findByRole(Role role);

    /**
     * Find a user by referral code.
     */
    Optional<User> findByReferralCode(String referralCode);

    // ==================== Hotel Owner Queries ====================

    /**
     * Find the hotel owner for a specific hotel.
     */
    Optional<User> findByHotelIdAndRole(Long hotelId, Role role);

    /**
     * Find all hotel owners.
     */
    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    /**
     * Check if a hotel already has an owner.
     */
    boolean existsByHotelId(Long hotelId);

    /**
     * Find hotel owner by hotel ID.
     */
    Optional<User> findByHotelId(Long hotelId);

    /**
     * Find all hotel owners with their hotel information.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.managedHotel WHERE u.role = :role ORDER BY u.createdAt DESC")
    List<User> findHotelOwnersWithHotels(@Param("role") Role role);

    /**
     * Count hotel owners.
     */
    long countByRole(Role role);

    /**
     * Find hotel owners by active status.
     */
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);
}
