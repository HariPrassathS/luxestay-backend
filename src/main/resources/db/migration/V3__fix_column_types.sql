-- =====================================================
-- V3: Fix column types for Hibernate compatibility
-- =====================================================
-- This migration fixes column type mismatches between the Flyway schema and Hibernate expectations.

-- Change star_rating from TINYINT to INT for Hibernate compatibility
ALTER TABLE hotels MODIFY COLUMN star_rating INT DEFAULT 3;
