-- V14: Fix hero_image_url column to support longer URLs
-- The VARCHAR(500) is too short for some image URLs with query parameters

-- Alter the hero_image_url column in hotels table to TEXT
ALTER TABLE hotels MODIFY COLUMN hero_image_url TEXT;

-- Also fix image_url columns in rooms and hotel_images tables for consistency
ALTER TABLE rooms MODIFY COLUMN image_url TEXT;
ALTER TABLE hotel_images MODIFY COLUMN image_url TEXT;
