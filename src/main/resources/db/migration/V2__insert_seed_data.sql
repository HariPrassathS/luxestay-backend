-- =====================================================
-- HOTEL RESERVATION SYSTEM - SEED DATA
-- Version: 1.0
-- Description: Initial seed data for development and testing
-- =====================================================

-- ==================== ADMIN USER ====================
-- Password: Admin@123 (BCrypt hashed)
INSERT INTO users (email, password_hash, first_name, last_name, phone, role, is_active) VALUES
('admin@luxestay.com', '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW', 'System', 'Administrator', '+1-555-000-0001', 'ADMIN', TRUE);

-- ==================== SAMPLE USERS ====================
-- Password: Admin@123 (BCrypt hashed - same as admin for demo)
INSERT INTO users (email, password_hash, first_name, last_name, phone, role, is_active) VALUES
('john.doe@email.com', '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW', 'John', 'Doe', '+1-555-123-4567', 'USER', TRUE),
('jane.smith@email.com', '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW', 'Jane', 'Smith', '+1-555-234-5678', 'USER', TRUE),
('mike.wilson@email.com', '$2a$10$57v.qjJSyWDWcaf1LAOqaet402H46ry73OptXHVsJ58h82/.kb/OW', 'Mike', 'Wilson', '+1-555-345-6789', 'USER', TRUE);

-- ==================== HOTELS ====================
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
(
    'Aurora Grand Hotel & Spa',
    'Experience unparalleled luxury at Aurora Grand, where timeless elegance meets modern sophistication. Nestled in the heart of Manhattan, our five-star sanctuary offers breathtaking city views, world-class dining, and an award-winning spa. Each moment here is crafted to exceed your expectations.',
    '789 Fifth Avenue',
    'New York',
    'United States',
    '10022',
    '+1-212-555-0100',
    'reservations@auroragrand.com',
    5,
    'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200',
    '["Free WiFi", "Swimming Pool", "Spa & Wellness", "Fitness Center", "Fine Dining", "Room Service", "Concierge", "Valet Parking", "Business Center", "Rooftop Bar"]',
    '15:00:00',
    '11:00:00',
    TRUE
),
(
    'Harbor View Resort',
    'Discover coastal paradise at Harbor View Resort. Wake up to stunning ocean panoramas, unwind on pristine private beaches, and indulge in fresh seafood at our waterfront restaurants. Perfect for romantic getaways and family vacations alike.',
    '1500 Ocean Boulevard',
    'Miami',
    'United States',
    '33139',
    '+1-305-555-0200',
    'hello@harborviewresort.com',
    4,
    'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=1200',
    '["Free WiFi", "Private Beach", "Outdoor Pool", "Water Sports", "Kids Club", "Restaurant", "Bar", "Free Parking", "Airport Shuttle"]',
    '16:00:00',
    '10:00:00',
    TRUE
),
(
    'Summit Alpine Lodge',
    'Escape to the majestic mountains at Summit Alpine Lodge. Whether you seek adventure on world-class ski slopes or tranquility by a crackling fireplace, our lodge offers an authentic alpine experience with contemporary comforts.',
    '2500 Mountain Peak Road',
    'Aspen',
    'United States',
    '81611',
    '+1-970-555-0300',
    'stay@summitalpine.com',
    4,
    'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=1200',
    '["Free WiFi", "Ski-in/Ski-out", "Hot Tub", "Fireplace", "Restaurant", "Ski Storage", "Heated Pool", "Spa", "Free Parking"]',
    '16:00:00',
    '11:00:00',
    TRUE
),
(
    'The Metropolitan Suites',
    'Business meets pleasure at The Metropolitan Suites. Located in the financial district, we offer sophisticated accommodations with state-of-the-art meeting facilities. After a productive day, relax in our executive lounge or rooftop pool.',
    '100 Financial Center',
    'Chicago',
    'United States',
    '60601',
    '+1-312-555-0400',
    'corporate@metropolitansuites.com',
    4,
    'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=1200',
    '["Free WiFi", "Business Center", "Meeting Rooms", "Executive Lounge", "Rooftop Pool", "Fitness Center", "Restaurant", "Valet Parking"]',
    '15:00:00',
    '12:00:00',
    TRUE
),
(
    'Serenity Wellness Retreat',
    'Transform your mind, body, and spirit at Serenity Wellness Retreat. Our holistic sanctuary offers personalized wellness programs, organic cuisine, meditation gardens, and healing therapies. Disconnect from the world and reconnect with yourself.',
    '8800 Canyon Vista Drive',
    'Sedona',
    'United States',
    '86336',
    '+1-928-555-0500',
    'wellness@serenityretreat.com',
    5,
    'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200',
    '["Free WiFi", "Spa & Wellness", "Yoga Studio", "Meditation Garden", "Organic Restaurant", "Hiking Trails", "Pool", "Fitness Center", "Wellness Programs"]',
    '15:00:00',
    '11:00:00',
    TRUE
),
(
    'Oceanfront Paradise Inn',
    'Your beachfront escape awaits at Oceanfront Paradise Inn. Step directly onto golden sands, snorkel in crystal-clear waters, and watch breathtaking sunsets from your private balcony. Paradise is not a place—its a feeling we create.',
    '500 Beachfront Way',
    'San Diego',
    'United States',
    '92109',
    '+1-619-555-0600',
    'reservations@oceanfrontparadise.com',
    3,
    'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200',
    '["Free WiFi", "Beach Access", "Pool", "Restaurant", "Bar", "Water Sports", "Free Parking", "BBQ Area"]',
    '15:00:00',
    '10:00:00',
    TRUE
);

-- ==================== HOTEL IMAGES ====================
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(1, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800', 'Aurora Grand Hotel Exterior', 1),
(1, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', 'Luxury Suite Interior', 2),
(1, 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800', 'Spa and Wellness Center', 3),
(2, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800', 'Harbor View Resort Pool', 1),
(2, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800', 'Ocean View Room', 2),
(3, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800', 'Alpine Lodge Winter View', 1),
(3, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800', 'Cozy Fireplace Lounge', 2),
(4, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800', 'Metropolitan Suites Lobby', 1),
(5, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800', 'Serenity Retreat Exterior', 1),
(6, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800', 'Oceanfront Paradise Beach', 1);

-- ==================== ROOMS ====================
-- Aurora Grand Hotel & Spa (Hotel ID: 1)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(1, '101', 'Classic King Room', 'Elegant room featuring a plush king bed, marble bathroom, and city views.', 'DOUBLE', 350.00, 2, 'King', 35.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["King Bed", "City View", "Marble Bathroom", "Mini Bar", "Smart TV", "Work Desk"]', TRUE),
(1, '201', 'Deluxe Corner Suite', 'Spacious corner suite with panoramic views, separate living area, and luxury amenities.', 'SUITE', 650.00, 3, 'King + Sofa Bed', 65.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["King Bed", "Panoramic View", "Living Area", "Jacuzzi Tub", "Butler Service", "Premium Mini Bar"]', TRUE),
(1, '301', 'Presidential Suite', 'The pinnacle of luxury. Two bedrooms, private terrace, dining room, and 24/7 butler service.', 'PRESIDENTIAL', 2500.00, 4, '2 King Beds', 150.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["2 King Beds", "Private Terrace", "Dining Room", "Kitchen", "Butler Service", "Limo Transfer"]', TRUE),
(1, '102', 'Twin Room', 'Comfortable room with two twin beds, perfect for friends or colleagues traveling together.', 'TWIN', 320.00, 2, 'Twin', 32.00, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800', '["2 Twin Beds", "City View", "Work Desk", "Mini Bar", "Smart TV"]', TRUE);

-- Harbor View Resort (Hotel ID: 2)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(2, 'A101', 'Ocean View Room', 'Wake up to stunning ocean views from your private balcony.', 'DOUBLE', 280.00, 2, 'Queen', 30.00, 'https://images.unsplash.com/photo-1602002418082-a4443e081dd1?w=800', '["Queen Bed", "Ocean View", "Balcony", "Mini Fridge", "Smart TV"]', TRUE),
(2, 'A201', 'Beachfront Suite', 'Direct beach access with a spacious suite featuring modern coastal design.', 'SUITE', 480.00, 4, 'King + Sofa Bed', 55.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["King Bed", "Beach Access", "Living Area", "Kitchenette", "Private Deck"]', TRUE),
(2, 'B101', 'Family Beach House', 'Perfect for families with two bedrooms, full kitchen, and garden patio.', 'FAMILY', 550.00, 6, '1 King + 2 Twin', 80.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', '["King Bed", "2 Twin Beds", "Full Kitchen", "Garden Patio", "BBQ Grill", "Washer/Dryer"]', TRUE);

-- Summit Alpine Lodge (Hotel ID: 3)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(3, 'M101', 'Mountain View Room', 'Cozy room with fireplace and stunning mountain panoramas.', 'DOUBLE', 320.00, 2, 'Queen', 28.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["Queen Bed", "Mountain View", "Fireplace", "Coffee Maker", "Heated Floors"]', TRUE),
(3, 'M201', 'Alpine Suite', 'Rustic luxury with exposed beams, hot tub, and wraparound deck.', 'SUITE', 580.00, 4, 'King + Sofa Bed', 70.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["King Bed", "Private Hot Tub", "Fireplace", "Wraparound Deck", "Ski Storage"]', TRUE),
(3, 'C101', 'Chalet Room', 'Authentic alpine experience with hand-crafted wooden furniture.', 'DELUXE', 420.00, 2, 'King', 45.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["King Bed", "Valley View", "Fireplace", "Balcony", "Mini Bar"]', TRUE);

-- The Metropolitan Suites (Hotel ID: 4)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(4, 'E101', 'Executive Room', 'Designed for the modern professional with ergonomic workspace and high-speed internet.', 'DOUBLE', 250.00, 2, 'King', 32.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["King Bed", "Work Station", "Fiber Internet", "Espresso Machine", "Smart TV"]', TRUE),
(4, 'E201', 'Business Suite', 'Separate meeting area, premium amenities, and executive lounge access.', 'SUITE', 450.00, 3, 'King + Sofa Bed', 55.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["King Bed", "Meeting Area", "Lounge Access", "Premium Mini Bar", "Printer"]', TRUE),
(4, 'S101', 'Standard Queen', 'Comfortable and efficient accommodations for short business stays.', 'SINGLE', 180.00, 1, 'Queen', 25.00, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800', '["Queen Bed", "Work Desk", "High-Speed WiFi", "Smart TV"]', TRUE);

-- Serenity Wellness Retreat (Hotel ID: 5)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(5, 'Z101', 'Zen Garden Room', 'Minimalist design with private garden access for morning meditation.', 'DOUBLE', 380.00, 2, 'King', 40.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["King Bed", "Garden Access", "Meditation Corner", "Organic Toiletries", "Yoga Mat"]', TRUE),
(5, 'Z201', 'Wellness Suite', 'Complete wellness experience with in-room spa treatments and outdoor soaking tub.', 'SUITE', 750.00, 2, 'King', 80.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["King Bed", "Outdoor Tub", "In-Room Spa", "Meditation Garden", "Wellness Concierge"]', TRUE),
(5, 'Z301', 'Rejuvenation Villa', 'Private villa with full spa facilities, infinity pool, and personal wellness guide.', 'PRESIDENTIAL', 1800.00, 4, '2 King Beds', 200.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["2 King Beds", "Private Pool", "Full Spa", "Kitchen", "Personal Guide", "Daily Treatments"]', TRUE);

-- Oceanfront Paradise Inn (Hotel ID: 6)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(6, 'P101', 'Beach View Room', 'Simple and charming room steps from the sand.', 'DOUBLE', 150.00, 2, 'Queen', 24.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["Queen Bed", "Beach View", "Patio", "Mini Fridge", "Coffee Maker"]', TRUE),
(6, 'P201', 'Sunset Suite', 'Best seats for golden hour with west-facing windows and outdoor lounge.', 'SUITE', 280.00, 3, 'King + Sofa Bed', 45.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["King Bed", "Sunset View", "Outdoor Lounge", "Kitchenette", "BBQ Access"]', TRUE),
(6, 'P102', 'Surfer Bunk', 'Budget-friendly with direct beach access and surfboard storage.', 'SINGLE', 85.00, 1, 'Full', 18.00, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800', '["Full Bed", "Beach Access", "Board Storage", "Outdoor Shower"]', TRUE);

-- ==================== SAMPLE BOOKINGS ====================
INSERT INTO bookings (booking_reference, user_id, room_id, check_in_date, check_out_date, num_guests, total_nights, price_per_night, total_price, status, special_requests) VALUES
('BK-2026-000001', 2, 1, '2026-02-15', '2026-02-18', 2, 3, 350.00, 1050.00, 'CONFIRMED', 'Late check-in around 10 PM please. Celebrating anniversary.'),
('BK-2026-000002', 3, 5, '2026-02-20', '2026-02-25', 2, 5, 280.00, 1400.00, 'PENDING', 'Would like a room away from the elevator.'),
('BK-2026-000003', 4, 12, '2026-01-28', '2026-01-30', 1, 2, 250.00, 500.00, 'CONFIRMED', NULL);

-- ==================== SAMPLE PAYMENTS ====================
INSERT INTO booking_payments (booking_id, amount, payment_method, payment_status, transaction_id, paid_at) VALUES
(1, 1050.00, 'CREDIT_CARD', 'COMPLETED', 'TXN-2026-CC-001234', '2026-01-20 14:30:00'),
(3, 500.00, 'CREDIT_CARD', 'COMPLETED', 'TXN-2026-CC-001235', '2026-01-24 09:15:00');
