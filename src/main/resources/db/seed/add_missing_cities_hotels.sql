-- =====================================================
-- ADD HOTELS FOR MISSING DESTINATIONS
-- Barcelona, Las Vegas, London, Los Angeles
-- Each city: 1 Hotel with 4 Rooms
-- =====================================================

-- =====================================================
-- LAS VEGAS HOTEL
-- =====================================================
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active, latitude, longitude) VALUES
('The Venetian Grand Las Vegas', 'Experience the glamour of Las Vegas at The Venetian Grand. Stunning replica canals, world-class casinos, Michelin-starred restaurants, and legendary entertainment await. Your ultimate Vegas adventure starts here.', '3355 Las Vegas Blvd S', 'Las Vegas', 'USA', '89109', '+1 702-414-1000', 'reservations@venetiangrand.com', 5, 'https://images.unsplash.com/photo-1605833556294-ea5c7a74f57d?w=1200', '["Free WiFi", "Casino", "Multiple Pools", "Spa", "Fine Dining", "Show Tickets", "Gondola Rides", "Shopping Mall", "Nightclub", "Convention Center"]', '15:00:00', '11:00:00', TRUE, 36.1215, -115.1739);

-- Get the Las Vegas hotel ID and insert rooms
SET @vegas_hotel_id = LAST_INSERT_ID();

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at) VALUES
(@vegas_hotel_id, 'V101', 'Strip View Deluxe Room', 'Stunning room overlooking the famous Las Vegas Strip with floor-to-ceiling windows', 'DELUXE', 299.00, 2, 'King', 42, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Strip View", "Minibar", "Smart TV", "Marble Bathroom", "In-Room Safe"]', TRUE, NOW(), NOW()),
(@vegas_hotel_id, 'V201', 'Luxury Canal Suite', 'Elegant suite with views of the Grand Canal and Venetian architecture', 'SUITE', 549.00, 2, 'King', 65, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Canal View", "Living Room", "Jacuzzi", "Butler Service", "Premium Minibar", "Bose Sound System"]', TRUE, NOW(), NOW()),
(@vegas_hotel_id, 'V301', 'High Roller Family Suite', 'Spacious family suite with separate kids area and gaming setup', 'FAMILY', 449.00, 4, '2 Queens', 75, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Kids Area", "Gaming Console", "Two Bathrooms", "Pool Access", "Zoo Tickets Included"]', TRUE, NOW(), NOW()),
(@vegas_hotel_id, 'V401', 'Penthouse Casino Suite', 'Ultimate luxury penthouse with private poker room and Strip panorama', 'PRESIDENTIAL', 1999.00, 4, 'King + Sofa Bed', 150, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "360° Strip View", "Private Poker Room", "Infinity Jacuzzi", "Personal Butler", "Limo Service", "VIP Casino Access", "Private Chef"]', TRUE, NOW(), NOW());

-- Hotel images for Las Vegas
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(@vegas_hotel_id, 'https://images.unsplash.com/photo-1605833556294-ea5c7a74f57d?w=800', 'The Venetian Grand Exterior Las Vegas Strip', 1),
(@vegas_hotel_id, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', 'Luxury Suite Interior', 2),
(@vegas_hotel_id, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', 'Grand Canal Shops', 3);

-- =====================================================
-- LOS ANGELES HOTEL
-- =====================================================
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active, latitude, longitude) VALUES
('Beverly Hills Luxe Resort', 'Live like a Hollywood star at Beverly Hills Luxe Resort. Nestled in the heart of Beverly Hills, enjoy celebrity-worthy amenities, rooftop pools with city views, and easy access to Rodeo Drive shopping.', '9876 Wilshire Blvd', 'Los Angeles', 'USA', '90210', '+1 310-555-0123', 'concierge@bhiluxe.com', 5, 'https://images.unsplash.com/photo-1534190760961-74e8c1c5c3da?w=1200', '["Free WiFi", "Rooftop Pool", "Spa", "Fine Dining", "Fitness Center", "Tesla House Cars", "Hollywood Tours", "Personal Stylist", "Celebrity Chef Restaurant"]', '15:00:00', '12:00:00', TRUE, 34.0736, -118.4004);

SET @la_hotel_id = LAST_INSERT_ID();

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at) VALUES
(@la_hotel_id, 'LA101', 'Hollywood Hills View Room', 'Chic room with stunning views of the Hollywood sign and hills', 'DELUXE', 379.00, 2, 'King', 38, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Hollywood View", "Smart TV", "Nespresso Machine", "Rain Shower", "Balcony"]', TRUE, NOW(), NOW()),
(@la_hotel_id, 'LA201', 'Rodeo Drive Suite', 'Luxury suite with walk-in closet and shopping concierge service', 'SUITE', 699.00, 2, 'King', 70, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Walk-in Closet", "Living Area", "Shopping Concierge", "Champagne Welcome", "Spa Access"]', TRUE, NOW(), NOW()),
(@la_hotel_id, 'LA301', 'Celebrity Family Bungalow', 'Private bungalow perfect for families with kids pool access', 'FAMILY', 599.00, 4, '2 Queens', 85, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Private Patio", "Kids Pool", "Game Room Access", "Universal Studios Tickets", "Family Cooking Class"]', TRUE, NOW(), NOW()),
(@la_hotel_id, 'LA401', 'Sunset Boulevard Penthouse', 'Iconic penthouse with infinity pool and panoramic LA skyline views', 'PRESIDENTIAL', 2499.00, 4, 'King + Sofa Bed', 180, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Infinity Pool", "360° Views", "Home Theater", "Private Chef", "Rolls Royce Service", "Helicopter Pad Access", "Personal Butler"]', TRUE, NOW(), NOW());

INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(@la_hotel_id, 'https://images.unsplash.com/photo-1534190760961-74e8c1c5c3da?w=800', 'Beverly Hills Resort Exterior', 1),
(@la_hotel_id, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', 'Rooftop Pool with LA Views', 2),
(@la_hotel_id, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800', 'Luxury Suite Interior', 3);

-- =====================================================
-- LONDON HOTEL (If not already exists)
-- =====================================================
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active, latitude, longitude) 
SELECT 'The Royal Westminster London', 'Experience British elegance at The Royal Westminster. Overlooking Big Ben and the Houses of Parliament, indulge in afternoon tea, world-class dining, and impeccable service in the heart of London.', '1 Parliament Square', 'London', 'United Kingdom', 'SW1A 0AA', '+44 20 7123 4567', 'reservations@royalwestminster.co.uk', 5, 'https://images.unsplash.com/photo-1529290130-4ca3753253ae?w=1200', '["Free WiFi", "Afternoon Tea", "Spa", "Big Ben View", "Concierge", "Valet Parking", "Fine Dining", "Fitness Center", "English Breakfast"]', '15:00:00', '11:00:00', TRUE, 51.5007, -0.1246
WHERE NOT EXISTS (SELECT 1 FROM hotels WHERE city = 'London' LIMIT 1);

SET @london_hotel_id = LAST_INSERT_ID();

-- Only insert rooms if hotel was inserted (ID > 0 means new insert)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at) 
SELECT @london_hotel_id, 'LDN101', 'Classic British Room', 'Elegant room with traditional British décor and city views', 'DELUXE', 349.00, 2, 'Queen', 32, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Tea Set", "City View", "Minibar", "Marble Bathroom"]', TRUE, NOW(), NOW()
WHERE @london_hotel_id > 0 AND NOT EXISTS (SELECT 1 FROM rooms WHERE hotel_id = @london_hotel_id);

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at)
SELECT @london_hotel_id, 'LDN201', 'Big Ben View Suite', 'Magnificent suite with direct views of Big Ben and Parliament', 'SUITE', 799.00, 2, 'King', 65, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Big Ben View", "Afternoon Tea", "Living Area", "Butler Service", "Champagne"]', TRUE, NOW(), NOW()
WHERE @london_hotel_id > 0;

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at)
SELECT @london_hotel_id, 'LDN301', 'Royal Family Suite', 'Spacious suite fit for a royal family visit to London', 'FAMILY', 649.00, 4, '2 Queens', 80, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Connecting Rooms", "Kids Amenities", "London Eye Tickets", "Harry Potter Tour"]', TRUE, NOW(), NOW()
WHERE @london_hotel_id > 0;

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at)
SELECT @london_hotel_id, 'LDN401', 'Thames Presidential Suite', 'Ultimate luxury with panoramic Thames and Westminster views', 'PRESIDENTIAL', 2799.00, 4, 'King + Sofa Bed', 160, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Thames View", "Private Terrace", "Grand Piano", "Butler", "Bentley Service", "Private Dining"]', TRUE, NOW(), NOW()
WHERE @london_hotel_id > 0;

-- =====================================================
-- BARCELONA HOTEL (If not already exists)
-- =====================================================
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active, latitude, longitude)
SELECT 'Casa Gaudí Barcelona', 'Discover the magic of Barcelona at Casa Gaudí. Our modernist hotel celebrates the genius of Antoni Gaudí with stunning architecture, rooftop pool with Sagrada Família views, and authentic Catalan hospitality.', '92 Passeig de Gràcia', 'Barcelona', 'Spain', '08008', '+34 93 272 1111', 'reservations@casagaudi.es', 5, 'https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=1200', '["Free WiFi", "Rooftop Pool", "Gaudí Tours", "Tapas Bar", "Spa", "Concierge", "Art Collection", "Cooking Classes", "Flamenco Shows"]', '15:00:00', '11:00:00', TRUE, 41.3917, 2.1649
WHERE NOT EXISTS (SELECT 1 FROM hotels WHERE city = 'Barcelona' LIMIT 1);

SET @barcelona_hotel_id = LAST_INSERT_ID();

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at)
SELECT @barcelona_hotel_id, 'BCN101', 'Modernist Design Room', 'Beautiful room inspired by Gaudí mosaic designs', 'DELUXE', 289.00, 2, 'Queen', 30, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Design Room", "Minibar", "Breakfast Included", "Balcony"]', TRUE, NOW(), NOW()
WHERE @barcelona_hotel_id > 0 AND NOT EXISTS (SELECT 1 FROM rooms WHERE hotel_id = @barcelona_hotel_id);

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at)
SELECT @barcelona_hotel_id, 'BCN201', 'Sagrada Família View Suite', 'Stunning suite with direct Sagrada Família cathedral views', 'SUITE', 599.00, 2, 'King', 60, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Sagrada View", "Living Area", "Cava Welcome", "Private Gaudí Tour", "Spa Credits"]', TRUE, NOW(), NOW()
WHERE @barcelona_hotel_id > 0;

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at)
SELECT @barcelona_hotel_id, 'BCN301', 'Mediterranean Family Suite', 'Spacious family suite with beach access and kids activities', 'FAMILY', 499.00, 4, '2 Queens', 72, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Beach Access", "Kids Club", "Family Cooking Class", "Aquarium Tickets", "Two Bathrooms"]', TRUE, NOW(), NOW()
WHERE @barcelona_hotel_id > 0;

INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available, created_at, updated_at)
SELECT @barcelona_hotel_id, 'BCN401', 'La Rambla Penthouse', 'Iconic penthouse with rooftop terrace and 360° Barcelona views', 'PRESIDENTIAL', 1899.00, 4, 'King + Sofa Bed', 140, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Rooftop Terrace", "Private Pool", "360° Views", "Personal Butler", "Mercedes Service", "Private Chef", "VIP Flamenco Show"]', TRUE, NOW(), NOW()
WHERE @barcelona_hotel_id > 0;

-- Verify
SELECT 'Hotels in target cities:' as Status;
SELECT city, COUNT(*) as hotel_count FROM hotels WHERE city IN ('Las Vegas', 'Los Angeles', 'London', 'Barcelona') GROUP BY city;
SELECT 'Total rooms in target cities:' as Status;
SELECT h.city, COUNT(r.id) as room_count FROM hotels h LEFT JOIN rooms r ON h.id = r.hotel_id WHERE h.city IN ('Las Vegas', 'Los Angeles', 'London', 'Barcelona') GROUP BY h.city;
