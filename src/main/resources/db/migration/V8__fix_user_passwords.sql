-- =====================================================
-- FIX USER PASSWORDS
-- =====================================================
-- The original seed data had incorrect BCrypt hashes.
-- This migration updates all users to use the correct hash for Admin@123

UPDATE users SET password_hash = '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW' WHERE email = 'admin@luxestay.com';
UPDATE users SET password_hash = '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW' WHERE email = 'john.doe@email.com';
UPDATE users SET password_hash = '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW' WHERE email = 'jane.smith@email.com';
UPDATE users SET password_hash = '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW' WHERE email = 'mike.wilson@email.com';
