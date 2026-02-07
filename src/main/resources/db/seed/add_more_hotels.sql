-- =====================================================
-- ADD MORE HOTELS, ROOMS, AND IMAGES TO LUXESTAY DATABASE
-- Execute after initial seed data
-- =====================================================

-- =====================================================
-- HOTELS FOR NEW CITIES
-- =====================================================

-- Paris Hotels (IDs 7-9)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Le Château Royal Paris', 'Experience Parisian elegance at its finest. Our luxury hotel offers breathtaking views of the Eiffel Tower, world-class dining, and impeccable service in the heart of the City of Light.', '15 Avenue des Champs-Élysées', 'Paris', 'France', '75008', '+33 1 44 56 78 90', 'reservations@chateauroyal.com', 5, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200', '["Free WiFi", "Spa", "Fine Dining", "Eiffel Tower View", "Concierge", "Valet Parking", "Fitness Center", "Rooftop Bar"]', '15:00:00', '11:00:00', TRUE),

('Hotel Montmartre Boutique', 'Charming boutique hotel nestled in the artistic heart of Montmartre. Wake up to stunning views of Sacré-Cœur and explore the bohemian streets that inspired countless artists.', '28 Rue Lepic', 'Paris', 'France', '75018', '+33 1 42 23 45 67', 'info@montmartreboutique.com', 4, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=1200', '["Free WiFi", "Breakfast Included", "Art Gallery", "Wine Bar", "Garden Terrace", "Bicycle Rental"]', '14:00:00', '11:00:00', TRUE),

('Seine River Grand Hotel', 'Luxurious waterfront hotel with panoramic Seine River views. Located steps from Notre-Dame and the Louvre, offering an authentic Parisian experience with modern amenities.', '42 Quai de la Tournelle', 'Paris', 'France', '75005', '+33 1 43 54 32 10', 'contact@seinegrand.com', 5, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200', '["Free WiFi", "River View", "Michelin Restaurant", "Spa", "Private Boat Tours", "Luxury Suites", "Butler Service"]', '15:00:00', '12:00:00', TRUE);

-- London Hotels (IDs 10-12)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('The Westminster Palace Hotel', 'Iconic luxury hotel overlooking Big Ben and the Houses of Parliament. Experience British elegance with afternoon tea service and world-class hospitality.', '1 Parliament Square', 'London', 'United Kingdom', 'SW1A 0AA', '+44 20 7123 4567', 'reservations@westminsterpalace.com', 5, 'https://images.unsplash.com/photo-1529290130-4ca3753253ae?w=1200', '["Free WiFi", "Afternoon Tea", "Spa", "Big Ben View", "Concierge", "Valet Parking", "Fine Dining", "Fitness Center"]', '15:00:00', '11:00:00', TRUE),

('Kensington Garden Suites', 'Elegant all-suite hotel in prestigious Kensington, moments from Hyde Park and the Royal Albert Hall. Perfect blend of classic British charm and contemporary comfort.', '15 Kensington High Street', 'London', 'United Kingdom', 'W8 5NP', '+44 20 7937 8888', 'info@kensingtongarden.com', 4, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=1200', '["Free WiFi", "Kitchen Suites", "Garden Access", "24-Hour Room Service", "Business Center", "Pet Friendly"]', '14:00:00', '11:00:00', TRUE),

('Shoreditch Modern Hotel', 'Trendy boutique hotel in the heart of East London creative district. Industrial-chic design meets modern luxury with rooftop views of the city skyline.', '88 Shoreditch High Street', 'London', 'United Kingdom', 'E1 6JJ', '+44 20 7739 9999', 'hello@shoreditchmodern.com', 4, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200', '["Free WiFi", "Rooftop Bar", "Co-Working Space", "Street Art Tours", "Vinyl Lounge", "Craft Coffee"]', '15:00:00', '10:00:00', TRUE);

-- Tokyo Hotels (IDs 13-15)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Tokyo Imperial Grand', 'Ultra-luxury hotel combining Japanese tradition with cutting-edge technology. Enjoy stunning views of the Imperial Palace Gardens and Tokyo skyline from your suite.', '1-1-1 Marunouchi, Chiyoda', 'Tokyo', 'Japan', '100-0005', '+81 3 3504 1111', 'reservations@tokyoimperial.jp', 5, 'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=1200', '["Free WiFi", "Onsen Spa", "Sushi Bar", "Imperial View", "Kimono Rental", "Tea Ceremony", "Michelin Dining"]', '15:00:00', '11:00:00', TRUE),

('Shibuya Sky Tower Hotel', 'Contemporary high-rise hotel in the vibrant Shibuya district. Experience Tokyo nightlife and pop culture with panoramic city views from every room.', '2-24-12 Shibuya', 'Tokyo', 'Japan', '150-0002', '+81 3 6452 2222', 'info@shibuyaskytower.jp', 4, 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=1200', '["Free WiFi", "Sky Lounge", "Anime Shop", "Karaoke Rooms", "Robot Cafe", "Capsule Spa"]', '14:00:00', '10:00:00', TRUE),

('Kyoto-Style Tokyo Ryokan', 'Traditional Japanese inn experience in the heart of Tokyo. Tatami rooms, kaiseki cuisine, and authentic onsen baths bring Kyoto serenity to the capital.', '5-15-3 Asakusa, Taito', 'Tokyo', 'Japan', '111-0032', '+81 3 3842 3333', 'omotenashi@tokyoryokan.jp', 4, 'https://images.unsplash.com/photo-1480796927426-f609979314bd?w=1200', '["Free WiFi", "Tatami Rooms", "Onsen Bath", "Kaiseki Dining", "Garden View", "Yukata Provided", "Sake Bar"]', '16:00:00', '10:00:00', TRUE);

-- Dubai Hotels (IDs 16-18)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Burj Al Arab View Resort', 'Opulent beachfront resort with iconic Burj Al Arab views. Experience Arabian luxury with private beach, world-class spa, and exceptional dining experiences.', 'Jumeirah Beach Road', 'Dubai', 'United Arab Emirates', '00000', '+971 4 301 7777', 'reservations@burjview.ae', 5, 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1200', '["Free WiFi", "Private Beach", "Infinity Pool", "Desert Safari", "Gold Spa", "Butler Service", "Helicopter Tours"]', '14:00:00', '12:00:00', TRUE),

('Marina Skyline Hotel', 'Modern luxury hotel in Dubai Marina with stunning yacht harbor and skyline views. Steps from the beach with direct access to shopping and nightlife.', 'Dubai Marina Walk', 'Dubai', 'United Arab Emirates', '00000', '+971 4 399 8888', 'stay@marinaskyline.ae', 4, 'https://images.unsplash.com/photo-1518684079-3c830dcef090?w=1200', '["Free WiFi", "Marina View", "Rooftop Pool", "Yacht Club", "Shopping Access", "Beach Shuttle", "Fitness Center"]', '15:00:00', '11:00:00', TRUE),

('Desert Oasis Luxury Camp', 'Glamping experience in the Arabian desert with five-star amenities. Private tented villas, camel rides, and stargazing under the desert sky.', 'Al Marmoom Desert Conservation Reserve', 'Dubai', 'United Arab Emirates', '00000', '+971 4 832 9999', 'experience@desertoasis.ae', 5, 'https://images.unsplash.com/photo-1548041347-390744c58da8?w=1200', '["Free WiFi", "Private Tent Villa", "Desert Safari", "Stargazing", "Falconry", "Arabic BBQ", "Hot Air Balloon"]', '16:00:00', '10:00:00', TRUE);

-- Sydney Hotels (IDs 19-21)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Opera House Harbour Hotel', 'Iconic waterfront hotel with unparalleled Sydney Opera House and Harbour Bridge views. Located at Circular Quay, the gateway to Sydney vibrant attractions.', '1 Circular Quay East', 'Sydney', 'Australia', '2000', '+61 2 9250 1111', 'reservations@operaharbour.com.au', 5, 'https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?w=1200', '["Free WiFi", "Harbour View", "Opera Packages", "Fine Dining", "Rooftop Bar", "Spa", "Concierge"]', '15:00:00', '11:00:00', TRUE),

('Bondi Beach Resort', 'Laid-back luxury steps from Australias most famous beach. Surf lessons, beachside dining, and stunning Pacific Ocean sunrises await.', '178 Campbell Parade, Bondi Beach', 'Sydney', 'Australia', '2026', '+61 2 9365 2222', 'surf@bondiresort.com.au', 4, 'https://images.unsplash.com/photo-1523482580672-f109ba8cb9be?w=1200', '["Free WiFi", "Ocean View", "Surf School", "Beach Access", "Pool", "Yoga Classes", "Organic Restaurant"]', '14:00:00', '10:00:00', TRUE),

('Blue Mountains Retreat', 'Secluded luxury retreat in the UNESCO World Heritage Blue Mountains. Bushwalking trails, waterfalls, and spectacular valley views from your private chalet.', '88 Echo Point Road, Katoomba', 'Sydney', 'Australia', '2780', '+61 2 4782 3333', 'nature@bluemtnsretreat.com.au', 4, 'https://images.unsplash.com/photo-1494233892892-84542a694e72?w=1200', '["Free WiFi", "Valley View", "Bushwalking", "Spa", "Fireplace", "Wildlife Tours", "Gourmet Kitchen"]', '15:00:00', '11:00:00', TRUE);

-- Barcelona Hotels (IDs 22-24)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Casa Gaudí Barcelona', 'Modernist masterpiece hotel inspired by Gaudís architectural genius. Located on Passeig de Gràcia, walking distance to La Sagrada Família and Casa Batlló.', '92 Passeig de Gràcia', 'Barcelona', 'Spain', '08008', '+34 93 272 1111', 'reservations@casagaudi.es', 5, 'https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=1200', '["Free WiFi", "Rooftop Pool", "Gaudí Tours", "Tapas Bar", "Spa", "Concierge", "Art Collection"]', '15:00:00', '11:00:00', TRUE),

('Gothic Quarter Boutique', 'Intimate boutique hotel in medieval stone building in the historic Gothic Quarter. Cobblestone streets, hidden plazas, and authentic Barcelona charm.', '15 Carrer del Bisbe', 'Barcelona', 'Spain', '08002', '+34 93 315 2222', 'info@gothicquarter.es', 4, 'https://images.unsplash.com/photo-1583422409516-2895a77efded?w=1200', '["Free WiFi", "Historic Building", "Wine Cellar", "Flamenco Shows", "Walking Tours", "Terrace Bar"]', '14:00:00', '11:00:00', TRUE),

('Barceloneta Beach Club Hotel', 'Modern beachfront hotel on Barceloneta Beach. Mediterranean Sea views, fresh seafood dining, and vibrant beach club atmosphere.', '1 Passeig Marítim', 'Barcelona', 'Spain', '08003', '+34 93 221 3333', 'beach@barcelonetaclub.es', 4, 'https://images.unsplash.com/photo-1562883676-8c7feb83f09b?w=1200', '["Free WiFi", "Beach Access", "Infinity Pool", "Seafood Restaurant", "DJ Nights", "Water Sports", "Cabanas"]', '15:00:00', '10:00:00', TRUE);

-- Singapore Hotels (IDs 25-27)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Marina Bay Sands View Hotel', 'Luxury hotel with spectacular views of Marina Bay Sands and the Singapore skyline. Experience the Lions City premier shopping and entertainment district.', '10 Bayfront Avenue', 'Singapore', 'Singapore', '018956', '+65 6688 1111', 'reservations@mbsview.sg', 5, 'https://images.unsplash.com/photo-1525625293386-3f8f99389edd?w=1200', '["Free WiFi", "Marina View", "Infinity Pool", "Casino Access", "Fine Dining", "Spa", "Gardens by Bay Tours"]', '15:00:00', '11:00:00', TRUE),

('Orchard Road Luxury Suites', 'Premier shopping district location with direct mall access. Sophisticated suites with kitchenettes, perfect for extended luxury stays in Singapore.', '391 Orchard Road', 'Singapore', 'Singapore', '238872', '+65 6735 2222', 'stay@orchardsuites.sg', 4, 'https://images.unsplash.com/photo-1565967511849-76a60a516170?w=1200', '["Free WiFi", "Shopping Access", "Kitchen Suites", "Pool", "Fitness Center", "Business Center", "Airport Shuttle"]', '14:00:00', '12:00:00', TRUE),

('Sentosa Island Resort', 'Tropical paradise resort on Sentosa Island. Private beach, Universal Studios access, and family-friendly activities in a stunning island setting.', '8 Sentosa Gateway', 'Singapore', 'Singapore', '098269', '+65 6577 3333', 'paradise@sentosaresort.sg', 5, 'https://images.unsplash.com/photo-1508062878650-88b52897f298?w=1200', '["Free WiFi", "Private Beach", "Water Park", "Universal Studios", "Kids Club", "Multiple Pools", "Beach Cabanas"]', '15:00:00', '11:00:00', TRUE);

-- Maldives Hotels (IDs 28-29)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Crystal Waters Overwater Resort', 'Exclusive overwater villa resort in the Maldives. Glass floor bungalows, private infinity pools, and direct access to crystal-clear turquoise waters.', 'North Malé Atoll', 'Malé', 'Maldives', '20026', '+960 664 1111', 'paradise@crystalwaters.mv', 5, 'https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=1200', '["Free WiFi", "Overwater Villa", "Private Pool", "Snorkeling", "Diving Center", "Underwater Restaurant", "Spa"]', '14:00:00', '12:00:00', TRUE),

('Sunset Beach Island Resort', 'Private island resort with powder-white beaches and spectacular sunsets. Ultimate seclusion with world-class amenities and butler service.', 'South Ari Atoll', 'Malé', 'Maldives', '20026', '+960 668 2222', 'island@sunsetbeach.mv', 5, 'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=1200', '["Free WiFi", "Private Island", "Beach Villa", "Dolphin Watching", "Manta Rays", "Sunset Cruises", "Private Chef"]', '14:00:00', '12:00:00', TRUE);

-- Rome Hotels (IDs 30-31)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active) VALUES
('Colosseum View Grand Hotel', 'Historic luxury hotel with breathtaking Colosseum views. Ancient Roman elegance meets modern comfort in the heart of the Eternal City.', 'Via dei Fori Imperiali, 25', 'Rome', 'Italy', '00186', '+39 06 6789 1111', 'reservations@colosseumview.it', 5, 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=1200', '["Free WiFi", "Colosseum View", "Rooftop Restaurant", "Spa", "Roman History Tours", "Concierge", "Wine Cellar"]', '15:00:00', '11:00:00', TRUE),

('Trastevere Charm Hotel', 'Boutique hotel in the charming Trastevere neighborhood. Cobblestone streets, authentic trattorias, and the real Roman lifestyle await.', 'Piazza di Santa Maria, 5', 'Rome', 'Italy', '00153', '+39 06 5812 2222', 'info@trasteverecharm.it', 4, 'https://images.unsplash.com/photo-1515542622106-78bda8ba0e5b?w=1200', '["Free WiFi", "Historic Building", "Italian Breakfast", "Wine Tasting", "Cooking Classes", "Bike Rental"]', '14:00:00', '10:00:00', TRUE);

-- =====================================================
-- HOTEL IMAGES
-- =====================================================

-- Paris Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(7, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800', 'Le Château Royal Paris Exterior', 1),
(7, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', 'Luxury Suite with Eiffel View', 2),
(7, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', 'Elegant Lobby', 3),
(8, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800', 'Montmartre Boutique Exterior', 1),
(8, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800', 'Cozy Room Interior', 2),
(9, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800', 'Seine River Hotel View', 1),
(9, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800', 'Waterfront Suite', 2);

-- London Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(10, 'https://images.unsplash.com/photo-1529290130-4ca3753253ae?w=800', 'Westminster Palace Hotel Exterior', 1),
(10, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800', 'Big Ben View Room', 2),
(11, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800', 'Kensington Suite', 1),
(11, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800', 'Garden View', 2),
(12, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', 'Shoreditch Modern Lobby', 1),
(12, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800', 'Industrial Chic Room', 2);

-- Tokyo Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(13, 'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800', 'Tokyo Imperial Entrance', 1),
(13, 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800', 'Imperial View Suite', 2),
(14, 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800', 'Shibuya Night View', 1),
(14, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800', 'Modern Japanese Room', 2),
(15, 'https://images.unsplash.com/photo-1480796927426-f609979314bd?w=800', 'Traditional Ryokan', 1),
(15, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800', 'Tatami Room', 2);

-- Dubai Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(16, 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800', 'Burj Al Arab View', 1),
(16, 'https://images.unsplash.com/photo-1518684079-3c830dcef090?w=800', 'Beach Resort Pool', 2),
(17, 'https://images.unsplash.com/photo-1518684079-3c830dcef090?w=800', 'Marina Skyline', 1),
(17, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800', 'Marina View Room', 2),
(18, 'https://images.unsplash.com/photo-1548041347-390744c58da8?w=800', 'Desert Luxury Camp', 1),
(18, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800', 'Glamping Tent Interior', 2);

-- Sydney Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(19, 'https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?w=800', 'Opera House View', 1),
(19, 'https://images.unsplash.com/photo-1523482580672-f109ba8cb9be?w=800', 'Harbour Suite', 2),
(20, 'https://images.unsplash.com/photo-1523482580672-f109ba8cb9be?w=800', 'Bondi Beach', 1),
(20, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', 'Ocean View Room', 2),
(21, 'https://images.unsplash.com/photo-1494233892892-84542a694e72?w=800', 'Blue Mountains View', 1),
(21, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800', 'Mountain Chalet', 2);

-- Barcelona Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(22, 'https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=800', 'Gaudí Inspired Architecture', 1),
(22, 'https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800', 'Rooftop Pool Barcelona', 2),
(23, 'https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800', 'Gothic Quarter Street', 1),
(23, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', 'Boutique Room', 2),
(24, 'https://images.unsplash.com/photo-1562883676-8c7feb83f09b?w=800', 'Barceloneta Beach', 1),
(24, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800', 'Beach View Suite', 2);

-- Singapore Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(25, 'https://images.unsplash.com/photo-1525625293386-3f8f99389edd?w=800', 'Marina Bay Skyline', 1),
(25, 'https://images.unsplash.com/photo-1565967511849-76a60a516170?w=800', 'Infinity Pool View', 2),
(26, 'https://images.unsplash.com/photo-1565967511849-76a60a516170?w=800', 'Orchard Road View', 1),
(26, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800', 'Luxury Suite', 2),
(27, 'https://images.unsplash.com/photo-1508062878650-88b52897f298?w=800', 'Sentosa Beach', 1),
(27, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800', 'Resort Room', 2);

-- Maldives Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(28, 'https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=800', 'Overwater Villas', 1),
(28, 'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800', 'Crystal Clear Waters', 2),
(29, 'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800', 'Private Beach', 1),
(29, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800', 'Beach Villa Interior', 2);

-- Rome Hotels Images
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) VALUES
(30, 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800', 'Colosseum View', 1),
(30, 'https://images.unsplash.com/photo-1515542622106-78bda8ba0e5b?w=800', 'Roman Luxury Suite', 2),
(31, 'https://images.unsplash.com/photo-1515542622106-78bda8ba0e5b?w=800', 'Trastevere Street', 1),
(31, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', 'Charming Room', 2);

-- =====================================================
-- ROOMS FOR ALL HOTELS
-- =====================================================

-- Rooms for Le Château Royal Paris (Hotel 7)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(7, '701', 'Classic Parisian Room', 'Elegant room with French décor and city views', 'DOUBLE', 350.00, 2, 'Queen', 28, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Minibar", "Safe", "Air Conditioning"]', TRUE),
(7, '702', 'Eiffel Tower View Suite', 'Stunning suite with direct Eiffel Tower views', 'SUITE', 750.00, 2, 'King', 55, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Eiffel View", "Living Area", "Jacuzzi", "Butler Service"]', TRUE),
(7, '703', 'Presidential Penthouse', 'Top floor penthouse with 360° Paris views', 'PRESIDENTIAL', 2500.00, 4, 'King + Sofa Bed', 150, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Panoramic View", "Private Terrace", "Kitchen", "Butler", "Champagne"]', TRUE),
(7, '704', 'Family Suite Paris', 'Spacious suite perfect for families', 'FAMILY', 550.00, 4, '2 Queens', 65, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Connecting Rooms", "Kids Amenities", "Game Console"]', TRUE);

-- Rooms for Hotel Montmartre Boutique (Hotel 8)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(8, '801', 'Artist Studio Room', 'Cozy room with Sacré-Cœur views', 'SINGLE', 150.00, 1, 'Twin', 18, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Art Supplies", "Coffee Machine"]', TRUE),
(8, '802', 'Montmartre Double', 'Charming double room with balcony', 'DOUBLE', 220.00, 2, 'Queen', 25, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Balcony", "Minibar", "Breakfast Included"]', TRUE),
(8, '803', 'Bohemian Suite', 'Artistic suite with vintage décor', 'SUITE', 380.00, 2, 'King', 40, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Living Area", "Vinyl Player", "Wine Selection"]', TRUE);

-- Rooms for Seine River Grand Hotel (Hotel 9)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(9, '901', 'River View Deluxe', 'Elegant room overlooking the Seine', 'DELUXE', 420.00, 2, 'King', 35, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "River View", "Marble Bathroom", "Nespresso"]', TRUE),
(9, '902', 'Notre-Dame Suite', 'Suite with views of Notre-Dame Cathedral', 'SUITE', 680.00, 2, 'King', 50, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Cathedral View", "Butler Service", "Spa Bath"]', TRUE),
(9, '903', 'Presidential Riverfront', 'Ultimate luxury on the Seine', 'PRESIDENTIAL', 1800.00, 4, 'King + Sofa Bed', 120, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Private Terrace", "Kitchen", "Boat Access", "Chef Service"]', TRUE);

-- Rooms for The Westminster Palace Hotel (Hotel 10)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(10, '1001', 'Classic Westminster Room', 'Traditional British elegance', 'DOUBLE', 380.00, 2, 'Queen', 30, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Tea Service", "Safe", "Air Conditioning"]', TRUE),
(10, '1002', 'Big Ben View Suite', 'Suite with iconic clock tower views', 'SUITE', 850.00, 2, 'King', 60, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Big Ben View", "Afternoon Tea", "Butler Service"]', TRUE),
(10, '1003', 'Royal Penthouse', 'Fit for royalty with Parliament views', 'PRESIDENTIAL', 3000.00, 4, 'King + Sofa Bed', 180, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Panoramic View", "Grand Piano", "Private Dining", "Rolls Royce Service"]', TRUE);

-- Rooms for Kensington Garden Suites (Hotel 11)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(11, '1101', 'Garden Studio', 'Compact suite with kitchenette', 'SINGLE', 200.00, 1, 'Double', 25, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Kitchenette", "Garden Access"]', TRUE),
(11, '1102', 'Kensington Suite', 'Spacious suite near Hyde Park', 'SUITE', 450.00, 2, 'King', 55, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Full Kitchen", "Living Area", "Park View"]', TRUE),
(11, '1103', 'Royal Albert Suite', 'Premium suite with concert hall views', 'DELUXE', 580.00, 2, 'King', 65, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Kitchen", "Balcony", "Concert Packages"]', TRUE);

-- Rooms for Shoreditch Modern Hotel (Hotel 12)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(12, '1201', 'Creative Pod', 'Industrial-chic compact room', 'SINGLE', 120.00, 1, 'Single', 15, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "USB Ports", "Smart TV"]', TRUE),
(12, '1202', 'Loft Double', 'Trendy loft-style room', 'DOUBLE', 220.00, 2, 'Queen', 28, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Exposed Brick", "Vinyl Player", "Craft Beer Minibar"]', TRUE),
(12, '1203', 'Skyline Suite', 'Top floor with city views', 'SUITE', 380.00, 2, 'King', 45, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Rooftop Access", "Living Area", "DJ Equipment"]', TRUE);

-- Rooms for Tokyo Imperial Grand (Hotel 13)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(13, '1301', 'Zen Garden Room', 'Tranquil room with garden views', 'DOUBLE', 400.00, 2, 'King', 32, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Garden View", "Japanese Bath", "Tea Set"]', TRUE),
(13, '1302', 'Imperial Suite', 'Luxurious suite with palace views', 'SUITE', 950.00, 2, 'King', 70, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Imperial View", "Private Onsen", "Butler Service"]', TRUE),
(13, '1303', 'Tokyo Penthouse', 'Ultimate Tokyo luxury experience', 'PRESIDENTIAL', 3500.00, 4, 'King + Sofa Bed', 200, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "360° View", "Private Spa", "Chef", "Helicopter Pad Access"]', TRUE);

-- Rooms for Shibuya Sky Tower Hotel (Hotel 14)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(14, '1401', 'Neon City Room', 'Vibrant room with Shibuya views', 'SINGLE', 180.00, 1, 'Double', 18, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Smart Room", "City View"]', TRUE),
(14, '1402', 'Crossing View Double', 'Room overlooking famous crossing', 'DOUBLE', 280.00, 2, 'Queen', 26, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Crossing View", "Anime Art", "Gaming Console"]', TRUE),
(14, '1403', 'Sky Lounge Suite', 'High-floor suite with Tokyo panorama', 'SUITE', 520.00, 2, 'King', 50, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Panoramic View", "Private Karaoke", "Bar Setup"]', TRUE);

-- Rooms for Kyoto-Style Tokyo Ryokan (Hotel 15)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(15, '1501', 'Traditional Tatami Room', 'Authentic Japanese sleeping experience', 'TWIN', 200.00, 2, 'Futon', 16, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Tatami", "Yukata", "Green Tea"]', TRUE),
(15, '1502', 'Garden View Tatami', 'Japanese room with zen garden views', 'DOUBLE', 280.00, 2, 'Futon', 22, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Garden View", "Private Bath", "Kaiseki Dinner"]', TRUE),
(15, '1503', 'Imperial Ryokan Suite', 'Premium traditional Japanese suite', 'SUITE', 480.00, 4, 'Futon', 45, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Onsen", "Garden", "Full Kaiseki", "Tea Ceremony"]', TRUE);

-- Rooms for Burj Al Arab View Resort (Hotel 16)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(16, '1601', 'Arabian Deluxe Room', 'Elegant room with Arabian Gulf views', 'DELUXE', 450.00, 2, 'King', 40, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Sea View", "Marble Bathroom", "24-Hour Dining"]', TRUE),
(16, '1602', 'Burj View Suite', 'Suite with iconic Burj Al Arab views', 'SUITE', 1200.00, 2, 'King', 80, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Burj View", "Private Pool", "Butler Service", "Gold Amenities"]', TRUE),
(16, '1603', 'Royal Beach Villa', 'Private villa on the beach', 'PRESIDENTIAL', 5000.00, 6, '2 Kings + Sofa Bed', 250, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Beach", "Infinity Pool", "Personal Chef", "Yacht Access"]', TRUE);

-- Rooms for Marina Skyline Hotel (Hotel 17)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(17, '1701', 'Marina View Room', 'Modern room with marina views', 'DOUBLE', 280.00, 2, 'Queen', 30, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Marina View", "Balcony", "Smart TV"]', TRUE),
(17, '1702', 'Skyline Suite', 'Suite with Dubai skyline panorama', 'SUITE', 650.00, 2, 'King', 55, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Panoramic View", "Living Area", "Premium Minibar"]', TRUE),
(17, '1703', 'Marina Family Suite', 'Perfect for families visiting Dubai', 'FAMILY', 480.00, 4, '2 Queens', 70, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Connecting Rooms", "Kids Club Access", "Beach Shuttle"]', TRUE);

-- Rooms for Desert Oasis Luxury Camp (Hotel 18)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(18, '1801', 'Bedouin Tent', 'Authentic desert glamping experience', 'DOUBLE', 380.00, 2, 'King', 35, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Desert View", "Private Deck", "Stargazing Telescope"]', TRUE),
(18, '1802', 'Royal Desert Suite', 'Luxury tent with private plunge pool', 'SUITE', 850.00, 2, 'King', 60, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Private Pool", "Outdoor Shower", "Camel Rides", "BBQ"]', TRUE),
(18, '1803', 'Sheikhs Pavilion', 'Ultimate desert luxury pavilion', 'PRESIDENTIAL', 2200.00, 4, 'King + Sofa Bed', 120, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Camp", "Infinity Pool", "Falcon Experience", "Private Chef"]', TRUE);

-- Rooms for Opera House Harbour Hotel (Hotel 19)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(19, '1901', 'Harbour View Room', 'Room with Sydney Harbour views', 'DOUBLE', 350.00, 2, 'Queen', 30, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Harbour View", "Coffee Machine", "Minibar"]', TRUE),
(19, '1902', 'Opera Suite', 'Suite with Opera House views', 'SUITE', 780.00, 2, 'King', 65, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Opera View", "Living Area", "Opera Tickets", "Butler Service"]', TRUE),
(19, '1903', 'Bridge Presidential', 'Penthouse with Bridge & Opera views', 'PRESIDENTIAL', 2800.00, 4, 'King + Sofa Bed', 150, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Panoramic View", "Private Terrace", "Champagne", "Harbour Cruise"]', TRUE);

-- Rooms for Bondi Beach Resort (Hotel 20)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(20, '2001', 'Surfer Room', 'Casual beachside accommodation', 'SINGLE', 150.00, 1, 'Double', 20, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Beach Access", "Surfboard Storage"]', TRUE),
(20, '2002', 'Ocean View Double', 'Room with Pacific Ocean views', 'DOUBLE', 280.00, 2, 'Queen', 28, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Ocean View", "Balcony", "Surf Lessons"]', TRUE),
(20, '2003', 'Beach House Suite', 'Suite with private beach access', 'SUITE', 520.00, 4, 'King + Sofa Bed', 55, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Beachfront", "Kitchen", "BBQ Area", "Kayaks"]', TRUE);

-- Rooms for Blue Mountains Retreat (Hotel 21)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(21, '2101', 'Valley View Cabin', 'Cozy cabin with valley views', 'DOUBLE', 220.00, 2, 'Queen', 25, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Valley View", "Fireplace", "Hiking Maps"]', TRUE),
(21, '2102', 'Three Sisters Suite', 'Suite overlooking Three Sisters rock', 'SUITE', 420.00, 2, 'King', 45, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Iconic View", "Spa Bath", "Gourmet Breakfast"]', TRUE),
(21, '2103', 'Bushland Lodge', 'Private lodge surrounded by nature', 'DELUXE', 550.00, 4, 'King + Bunk Beds', 65, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Deck", "Full Kitchen", "Wildlife Guides", "Hot Tub"]', TRUE);

-- Rooms for Casa Gaudí Barcelona (Hotel 22)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(22, '2201', 'Modernist Room', 'Room with Gaudí-inspired design', 'DOUBLE', 320.00, 2, 'Queen', 28, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Design Room", "Minibar", "Breakfast"]', TRUE),
(22, '2202', 'Passeig de Gràcia Suite', 'Suite on Barcelonas famous avenue', 'SUITE', 650.00, 2, 'King', 55, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Avenue View", "Living Area", "Gaudí Tour", "Cava Welcome"]', TRUE),
(22, '2203', 'Sagrada Família Penthouse', 'Penthouse with cathedral views', 'PRESIDENTIAL', 1500.00, 4, 'King + Sofa Bed', 100, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Sagrada View", "Rooftop Terrace", "Private Pool", "Chef Service"]', TRUE);

-- Rooms for Gothic Quarter Boutique (Hotel 23)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(23, '2301', 'Medieval Charm Room', 'Cozy room in historic building', 'SINGLE', 130.00, 1, 'Double', 16, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Stone Walls", "AC", "Breakfast"]', TRUE),
(23, '2302', 'Cathedral Double', 'Room with cathedral plaza views', 'DOUBLE', 220.00, 2, 'Queen', 24, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Plaza View", "Wine Selection", "Walking Tours"]', TRUE),
(23, '2303', 'Gothic Suite', 'Spacious suite with terrace', 'SUITE', 380.00, 2, 'King', 40, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Terrace", "Exposed Beams", "Tapas & Wine Experience"]', TRUE);

-- Rooms for Barceloneta Beach Club Hotel (Hotel 24)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(24, '2401', 'Beach Pod', 'Compact room steps from the beach', 'SINGLE', 140.00, 1, 'Double', 18, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Beach Access", "Beach Towels"]', TRUE),
(24, '2402', 'Sea View Room', 'Room with Mediterranean views', 'DOUBLE', 260.00, 2, 'Queen', 26, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Sea View", "Balcony", "Beach Club Access"]', TRUE),
(24, '2403', 'Beach Club Suite', 'Suite with pool and beach access', 'SUITE', 480.00, 2, 'King', 50, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Beachfront", "Cabana Access", "Water Sports", "Seafood Dinner"]', TRUE);

-- Rooms for Marina Bay Sands View Hotel (Hotel 25)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(25, '2501', 'City View Room', 'Modern room with Singapore skyline', 'DOUBLE', 320.00, 2, 'Queen', 30, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "City View", "Smart Room", "Minibar"]', TRUE),
(25, '2502', 'Marina Bay Suite', 'Suite overlooking Marina Bay', 'SUITE', 680.00, 2, 'King', 55, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Marina View", "Living Area", "Club Access", "Butler"]', TRUE),
(25, '2503', 'Infinity Sky Penthouse', 'Penthouse with private infinity pool', 'PRESIDENTIAL', 2500.00, 4, 'King + Sofa Bed', 140, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Pool", "Panoramic View", "Chef", "Limousine"]', TRUE);

-- Rooms for Orchard Road Luxury Suites (Hotel 26)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(26, '2601', 'Shopping Suite Studio', 'Studio with mall access', 'SINGLE', 180.00, 1, 'Double', 25, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Kitchenette", "Mall Access", "Laundry"]', TRUE),
(26, '2602', 'Orchard Executive Suite', 'Executive suite for business travelers', 'SUITE', 380.00, 2, 'King', 50, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Full Kitchen", "Work Desk", "Meeting Room Access"]', TRUE),
(26, '2603', 'Family Shopping Suite', 'Spacious suite for families', 'FAMILY', 480.00, 4, '2 Queens', 70, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Full Kitchen", "Kids Amenities", "Mall Access", "Babysitting"]', TRUE);

-- Rooms for Sentosa Island Resort (Hotel 27)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(27, '2701', 'Island View Room', 'Room with Sentosa beach views', 'DOUBLE', 280.00, 2, 'Queen', 28, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Beach View", "Pool Access", "Theme Park Shuttle"]', TRUE),
(27, '2702', 'Beach Villa', 'Private villa on the beach', 'SUITE', 650.00, 4, 'King + Sofa Bed', 65, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Private Beach", "Outdoor Shower", "BBQ", "Universal Studios Passes"]', TRUE),
(27, '2703', 'Presidential Beach House', 'Ultimate family beach house', 'PRESIDENTIAL', 1800.00, 6, '2 Kings + Bunk Bed', 150, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Beach", "Pool", "Water Park Access", "Butler", "Private Yacht"]', TRUE);

-- Rooms for Crystal Waters Overwater Resort (Hotel 28)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(28, '2801', 'Overwater Bungalow', 'Classic overwater villa', 'DELUXE', 800.00, 2, 'King', 55, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Glass Floor", "Direct Ocean Access", "Snorkeling Gear"]', TRUE),
(28, '2802', 'Sunset Overwater Suite', 'Premium villa facing sunset', 'SUITE', 1200.00, 2, 'King', 80, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Sunset View", "Private Pool", "Butler", "Underwater Restaurant"]', TRUE),
(28, '2803', 'Presidential Water Villa', 'Ultimate Maldives luxury', 'PRESIDENTIAL', 3500.00, 4, 'King + Sofa Bed', 150, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Two Pools", "Spa Room", "Private Chef", "Yacht", "Diving Butler"]', TRUE);

-- Rooms for Sunset Beach Island Resort (Hotel 29)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(29, '2901', 'Beach Bungalow', 'Beachfront bungalow', 'DELUXE', 650.00, 2, 'King', 50, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Beach Access", "Outdoor Shower", "Hammock"]', TRUE),
(29, '2902', 'Sunset Beach Villa', 'Villa with stunning sunset views', 'SUITE', 1100.00, 2, 'King', 75, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Sunset View", "Private Pool", "Dolphin Watching", "Spa"]', TRUE),
(29, '2903', 'Island Presidential Suite', 'Private island within island', 'PRESIDENTIAL', 4000.00, 6, '2 Kings + Sofa Bed', 200, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Island Section", "Infinity Pool", "Private Chef", "Yacht", "Spa Suite"]', TRUE);

-- Rooms for Colosseum View Grand Hotel (Hotel 30)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(30, '3001', 'Roman Classic Room', 'Elegant room with Roman décor', 'DOUBLE', 320.00, 2, 'Queen', 28, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Marble Bathroom", "Minibar", "Italian Breakfast"]', TRUE),
(30, '3002', 'Colosseum View Suite', 'Suite overlooking the Colosseum', 'SUITE', 750.00, 2, 'King', 60, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Colosseum View", "Living Area", "Roman History Guide", "Wine Selection"]', TRUE),
(30, '3003', 'Imperial Rome Penthouse', 'Penthouse fit for an emperor', 'PRESIDENTIAL', 2200.00, 4, 'King + Sofa Bed', 120, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "360° Rome View", "Private Terrace", "Chef", "Gladiator Experience"]', TRUE);

-- Rooms for Trastevere Charm Hotel (Hotel 31)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(31, '3101', 'Trastevere Single', 'Cozy room in charming neighborhood', 'SINGLE', 120.00, 1, 'Double', 16, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "AC", "Italian Breakfast", "Neighborhood Guide"]', TRUE),
(31, '3102', 'Piazza View Double', 'Room overlooking the piazza', 'DOUBLE', 220.00, 2, 'Queen', 24, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Piazza View", "Wine & Cheese Welcome", "Cooking Class"]', TRUE),
(31, '3103', 'Trastevere Suite', 'Charming suite with terrace', 'SUITE', 380.00, 2, 'King', 42, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Private Terrace", "Full Breakfast", "Vespa Rental", "Trattoria Tour"]', TRUE);

-- =====================================================
-- ADD MORE ROOMS TO EXISTING HOTELS (Hotels 1-6)
-- =====================================================

-- Additional rooms for Aurora Grand Hotel & Spa (Hotel 1)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(1, '105', 'Twin City View', 'Twin room with Manhattan views', 'TWIN', 320.00, 2, '2 Twins', 28, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "City View", "Minibar", "Smart TV"]', TRUE),
(1, '106', 'Family Suite NYC', 'Large suite for families', 'FAMILY', 580.00, 5, '2 Queens + Sofa Bed', 75, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Connecting Rooms", "Kids Amenities", "Game Console", "Central Park View"]', TRUE);

-- Additional rooms for Harbor View Resort (Hotel 2)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(2, '205', 'Beach Bungalow', 'Private bungalow near the beach', 'DELUXE', 420.00, 2, 'King', 45, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Private Patio", "Beach Access", "Outdoor Shower"]', TRUE),
(2, '206', 'Ocean Family Room', 'Family room with ocean views', 'FAMILY', 380.00, 4, '2 Queens', 55, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Ocean View", "Bunk Beds Available", "Kids Pool Access"]', TRUE);

-- Additional rooms for Summit Alpine Lodge (Hotel 3)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(3, '305', 'Ski-In Suite', 'Direct ski slope access suite', 'SUITE', 650.00, 2, 'King', 60, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Ski-In/Ski-Out", "Fireplace", "Hot Tub", "Ski Storage"]', TRUE),
(3, '306', 'Mountain Family Chalet', 'Chalet for ski families', 'FAMILY', 780.00, 6, '2 Kings + Bunk Beds', 90, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Full Kitchen", "Game Room", "Ski Lessons Included"]', TRUE);

-- Additional rooms for The Metropolitan Suites (Hotel 4)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(4, '405', 'Business Executive', 'Room for business travelers', 'DOUBLE', 280.00, 2, 'King', 32, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600', '["Free WiFi", "Work Desk", "Meeting Room Access", "Express Checkout"]', TRUE),
(4, '406', 'Lake View Suite', 'Suite with Lake Michigan views', 'SUITE', 520.00, 2, 'King', 55, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Lake View", "Living Area", "Private Bar"]', TRUE);

-- Additional rooms for Serenity Wellness Retreat (Hotel 5)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(5, '505', 'Meditation Suite', 'Suite designed for mindfulness', 'SUITE', 480.00, 2, 'King', 50, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Meditation Space", "Sound Therapy", "Private Garden"]', TRUE),
(5, '506', 'Couples Wellness Villa', 'Private villa for couples', 'DELUXE', 620.00, 2, 'King', 65, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', '["Free WiFi", "Couples Spa", "Private Hot Tub", "Desert Views", "Yoga Sessions"]', TRUE);

-- Additional rooms for Oceanfront Paradise Inn (Hotel 6)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available) VALUES
(6, '605', 'Surfer Suite', 'Suite for surf enthusiasts', 'SUITE', 380.00, 2, 'King', 45, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600', '["Free WiFi", "Surfboard Storage", "Beach Access", "Wetsuit Rental"]', TRUE),
(6, '606', 'Coastal Family Room', 'Room for beach-loving families', 'FAMILY', 420.00, 5, '2 Queens + Sofa Bed', 60, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600', '["Free WiFi", "Ocean View", "Kids Beach Gear", "Tide Pool Tours"]', TRUE);

-- Verify the data
SELECT 'Hotels Added:' as Status, COUNT(*) as Count FROM hotels;
SELECT 'Rooms Added:' as Status, COUNT(*) as Count FROM rooms;
SELECT 'Hotel Images Added:' as Status, COUNT(*) as Count FROM hotel_images;
