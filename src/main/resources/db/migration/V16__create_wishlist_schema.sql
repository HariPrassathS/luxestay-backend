-- =====================================================
-- V16: Create Wishlist/Favorites Schema
-- =====================================================
-- Phase 1 Feature: User hotel wishlist/favorites system
-- Supports: DB-backed persistence, no localStorage hacks
-- =====================================================

-- Wishlist table for user favorites
CREATE TABLE IF NOT EXISTS wishlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Prevent duplicate entries per user/hotel
    CONSTRAINT uk_wishlist_user_hotel UNIQUE (user_id, hotel_id),
    
    -- Foreign keys
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_hotel FOREIGN KEY (hotel_id) 
        REFERENCES hotels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for fast lookup
CREATE INDEX idx_wishlist_user_id ON wishlists(user_id);
CREATE INDEX idx_wishlist_hotel_id ON wishlists(hotel_id);
CREATE INDEX idx_wishlist_created_at ON wishlists(created_at DESC);

-- Comment for documentation
-- This schema enables:
-- 1. Users can add/remove hotels from their wishlist
-- 2. Fast lookup of user's wishlisted hotels
-- 3. Check if specific hotel is in user's wishlist
-- 4. Cascade delete when user or hotel is removed
