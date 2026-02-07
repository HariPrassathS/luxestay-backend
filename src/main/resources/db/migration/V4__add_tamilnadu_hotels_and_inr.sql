-- =====================================================
-- HOTEL RESERVATION SYSTEM - TAMIL NADU HOTELS & INR CONVERSION
-- Version: 4.0
-- Description: Add 30 Tamil Nadu hotels and convert all prices to INR
-- =====================================================

-- ==================== CONVERT EXISTING PRICES TO INR ====================
-- Exchange rate: 1 USD = 83 INR (approximate)
UPDATE rooms SET price_per_night = price_per_night * 83 WHERE hotel_id IN (
    SELECT id FROM hotels WHERE country NOT IN ('India')
);

-- Update country for existing international hotels
UPDATE hotels SET country = 'USA' WHERE country = 'United States';

-- ==================== TAMIL NADU HOTELS ====================

-- Hotel 32: Chennai - The Leela Palace Chennai
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'The Leela Palace Chennai',
    'Experience unparalleled luxury at The Leela Palace Chennai, an iconic heritage-style hotel offering world-class amenities, stunning sea views, and authentic South Indian hospitality in the heart of Chennai.',
    '50 Adyar Seaface, MRC Nagar',
    'Chennai',
    'India',
    '600028',
    '+91 44 3366 1234',
    'reservations.chennai@theleela.com',
    5,
    'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800',
    '["Free WiFi", "Swimming Pool", "Spa", "Gym", "Restaurant", "Bar", "Room Service", "Concierge", "Valet Parking", "Business Center"]',
    '14:00:00',
    '12:00:00',
    TRUE
);

-- Hotel 33: Chennai - ITC Grand Chola
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'ITC Grand Chola',
    'A magnificent tribute to the great Chola dynasty, ITC Grand Chola is one of the largest luxury hotels in India, featuring exquisite architecture, award-winning restaurants, and exceptional service.',
    '63 Mount Road, Guindy',
    'Chennai',
    'India',
    '600032',
    '+91 44 2220 0000',
    'reservations@itcgrandchola.in',
    5,
    'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800',
    '["Free WiFi", "Swimming Pool", "Spa & Wellness", "Fitness Center", "Fine Dining", "Bar", "24/7 Room Service", "Conference Rooms", "Helipad", "Limousine Service"]',
    '14:00:00',
    '12:00:00',
    TRUE
);

-- Hotel 34: Chennai - Taj Coromandel
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Taj Coromandel Chennai',
    'Located in the cultural heart of Chennai, Taj Coromandel combines timeless elegance with modern luxury, offering world-renowned hospitality and culinary excellence.',
    '37 Mahatma Gandhi Road, Nungambakkam',
    'Chennai',
    'India',
    '600034',
    '+91 44 6600 2827',
    'coromandel.chennai@tajhotels.com',
    5,
    'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800',
    '["Free WiFi", "Outdoor Pool", "Spa", "Gym", "Multiple Restaurants", "Rooftop Bar", "Room Service", "Butler Service", "Parking"]',
    '15:00:00',
    '12:00:00',
    TRUE
);

-- Hotel 35: Chennai - The Park Chennai
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'The Park Chennai',
    'A contemporary design hotel in Chennai offering chic accommodations, vibrant nightlife, and innovative dining experiences in a stylish urban setting.',
    '601 Anna Salai',
    'Chennai',
    'India',
    '600006',
    '+91 44 4267 6000',
    'chennai@theparkhotels.com',
    4,
    'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800',
    '["Free WiFi", "Rooftop Pool", "Spa", "Gym", "Restaurant", "Nightclub", "Room Service", "Parking"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 36: Coimbatore - Le Méridien Coimbatore
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Le Méridien Coimbatore',
    'Discover creative inspiration at Le Méridien Coimbatore, featuring contemporary design, artistic elements, and modern amenities in the Manchester of South India.',
    '50 Mahatma Gandhi Street, Ramnagar',
    'Coimbatore',
    'India',
    '641009',
    '+91 422 223 3000',
    'reservations.coimbatore@lemeridien.com',
    5,
    'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800',
    '["Free WiFi", "Infinity Pool", "Spa", "Fitness Center", "Restaurant", "Bar", "Room Service", "Business Center", "Parking"]',
    '14:00:00',
    '12:00:00',
    TRUE
);

-- Hotel 37: Coimbatore - Vivanta Coimbatore
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Vivanta Coimbatore',
    'Experience sophisticated comfort at Vivanta Coimbatore, offering premium accommodations, excellent dining options, and warm hospitality near the Western Ghats.',
    '364-365 Sathy Road, Saravanampatti',
    'Coimbatore',
    'India',
    '641035',
    '+91 422 670 1234',
    'vivanta.coimbatore@tajhotels.com',
    4,
    'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800',
    '["Free WiFi", "Swimming Pool", "Spa", "Gym", "Restaurant", "Bar", "Room Service", "Conference Rooms"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 38: Ooty - Savoy Hotel (IHCL)
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Savoy Hotel Ooty',
    'A legendary heritage hotel set amidst 6 acres of beautiful gardens in the Queen of Hill Stations. Experience colonial charm with modern luxuries in the Nilgiris.',
    '77 Sylks Road',
    'Ooty',
    'India',
    '643001',
    '+91 423 222 5500',
    'savoy.ooty@tajhotels.com',
    5,
    'https://images.unsplash.com/photo-1600011689032-8b628b8a8747?w=800',
    '["Free WiFi", "Heritage Gardens", "Spa", "Restaurant", "Room Service", "Bonfire", "Trekking Arrangements", "Parking"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 39: Ooty - Sterling Ooty Elk Hill
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Sterling Ooty Elk Hill',
    'Nestled on a hilltop with panoramic views of the Nilgiri mountains, Sterling Ooty offers a perfect blend of comfort and natural beauty for a memorable hill station getaway.',
    '248/14 Elk Hill, Fingerpost',
    'Ooty',
    'India',
    '643006',
    '+91 423 244 2234',
    'ooty@sterlingresorts.com',
    4,
    'https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800',
    '["Free WiFi", "Mountain View", "Restaurant", "Indoor Games", "Bonfire", "Children Play Area", "Parking"]',
    '14:00:00',
    '10:00:00',
    TRUE
);

-- Hotel 40: Ooty - Sinclairs Retreat Ooty
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Sinclairs Retreat Ooty',
    'A charming retreat surrounded by eucalyptus and pine forests, offering colonial-style comfort and breathtaking views of the lush Nilgiri hills.',
    'Gorishola Road, Fernhill',
    'Ooty',
    'India',
    '643004',
    '+91 423 244 2666',
    'ooty@sinclairshotels.com',
    3,
    'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800',
    '["Free WiFi", "Restaurant", "Garden", "Room Service", "Parking", "Tour Desk"]',
    '12:00:00',
    '10:00:00',
    TRUE
);

-- Hotel 41: Kodaikanal - Carlton Hotel
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Carlton Hotel Kodaikanal',
    'Perched on the edge of a cliff with stunning views of the valley, Carlton Hotel offers heritage accommodation and warm hospitality in the Princess of Hill Stations.',
    'Lake Road',
    'Kodaikanal',
    'India',
    '624101',
    '+91 4542 240 056',
    'reservations@carltonkodaikanal.com',
    5,
    'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?w=800',
    '["Free WiFi", "Valley View", "Spa", "Multi-Cuisine Restaurant", "Room Service", "Campfire", "Trekking", "Boating"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 42: Kodaikanal - Sterling Kodai Valley
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Sterling Kodai Valley',
    'A serene getaway offering breathtaking views of the Palani hills, misty mornings, and the charm of Kodaikanal in a comfortable and elegant setting.',
    '24 Gymkhana Road',
    'Kodaikanal',
    'India',
    '624101',
    '+91 4542 241 646',
    'kodai@sterlingresorts.com',
    4,
    'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800',
    '["Free WiFi", "Hill View", "Restaurant", "Indoor Games", "Bonfire", "Children Area", "Parking"]',
    '14:00:00',
    '10:00:00',
    TRUE
);

-- Hotel 43: Kodaikanal - Villa Retreat
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Villa Retreat Kodaikanal',
    'A boutique heritage property surrounded by lush greenery and colorful flowers, offering an intimate and peaceful stay in the heart of Kodaikanal.',
    'Coakers Walk Road',
    'Kodaikanal',
    'India',
    '624101',
    '+91 4542 240 940',
    'info@villaretreat.in',
    3,
    'https://images.unsplash.com/photo-1568495248636-6432b97bd949?w=800',
    '["Free WiFi", "Garden", "Restaurant", "Room Service", "Parking", "Tour Desk"]',
    '12:00:00',
    '10:00:00',
    TRUE
);

-- Hotel 44: Madurai - Heritage Madurai
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Heritage Madurai',
    'A restored colonial mansion set in 60 acres of manicured gardens, offering a blend of old-world charm and modern comforts near the magnificent Meenakshi Temple.',
    '11 Melakkal Main Road, Kochadai',
    'Madurai',
    'India',
    '625016',
    '+91 452 238 5455',
    'reservations@heritagemadurai.com',
    5,
    'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800',
    '["Free WiFi", "Swimming Pool", "Spa", "Gym", "Restaurants", "Bar", "Room Service", "Heritage Walk", "Cooking Classes"]',
    '14:00:00',
    '12:00:00',
    TRUE
);

-- Hotel 45: Madurai - Fortune Pandiyan Hotel
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Fortune Pandiyan Hotel',
    'A well-appointed hotel in the temple city of Madurai, offering comfortable accommodations and excellent dining options for business and leisure travelers.',
    'Race Course Road',
    'Madurai',
    'India',
    '625002',
    '+91 452 453 7070',
    'madurai@fortunehotels.in',
    4,
    'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800',
    '["Free WiFi", "Swimming Pool", "Restaurant", "Gym", "Room Service", "Business Center", "Parking"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 46: Madurai - GRT Regency
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'GRT Regency Madurai',
    'Strategically located in the heart of Madurai, GRT Regency offers modern comforts and traditional hospitality, making it ideal for exploring the ancient city.',
    '38 Bypass Road, K. Pudur',
    'Madurai',
    'India',
    '625007',
    '+91 452 437 1234',
    'madurai@grthotels.com',
    3,
    'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800',
    '["Free WiFi", "Restaurant", "Room Service", "Conference Room", "Parking", "Tour Desk"]',
    '12:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 47: Pondicherry - Palais de Mahe
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Palais de Mahe Pondicherry',
    'A stunning French colonial mansion transformed into an elegant boutique hotel, featuring a rooftop infinity pool and proximity to the famous Promenade Beach.',
    '4 Bussy Street, White Town',
    'Pondicherry',
    'India',
    '605001',
    '+91 413 234 5678',
    'reservations@palaisdemahe.com',
    5,
    'https://images.unsplash.com/photo-1561501900-3701fa6a0864?w=800',
    '["Free WiFi", "Rooftop Pool", "Spa", "French Restaurant", "Bar", "Room Service", "Heritage Tours", "Bicycle Rental"]',
    '14:00:00',
    '12:00:00',
    TRUE
);

-- Hotel 48: Pondicherry - Le Pondy Resort
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Le Pondy Beach Resort',
    'A serene beachfront resort offering luxurious villas, Ayurvedic treatments, and stunning Bay of Bengal views in the French Riviera of the East.',
    'ECR Main Road, Villianur',
    'Pondicherry',
    'India',
    '605110',
    '+91 413 261 5000',
    'info@lepondy.com',
    5,
    'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800',
    '["Free WiFi", "Private Beach", "Swimming Pool", "Ayurvedic Spa", "Multi-Cuisine Restaurant", "Room Service", "Water Sports"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 49: Pondicherry - Villa Shanti
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Villa Shanti Pondicherry',
    'A lovingly restored French heritage home in the heart of the French Quarter, offering intimate accommodation and one of the best restaurants in town.',
    '14 Suffren Street',
    'Pondicherry',
    'India',
    '605001',
    '+91 413 420 0028',
    'stay@villashanti.com',
    4,
    'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800',
    '["Free WiFi", "Courtyard", "Restaurant", "Room Service", "Heritage Walk", "Bicycle Rental"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 50: Trichy - Grand Gardenia
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Grand Gardenia Trichy',
    'A premier business and leisure hotel in Tiruchirappalli, offering modern amenities and convenient access to the Rock Fort Temple and Ranganathaswamy Temple.',
    '4/7B Mannarpuram Junction, Trichy',
    'Trichy',
    'India',
    '620020',
    '+91 431 400 5005',
    'info@grandgardenia.in',
    4,
    'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800',
    '["Free WiFi", "Swimming Pool", "Spa", "Restaurant", "Bar", "Room Service", "Banquet Hall", "Parking"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 51: Trichy - Sangam Hotel
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Hotel Sangam Trichy',
    'One of the oldest heritage hotels in Trichy, offering comfortable accommodations with a blend of traditional hospitality and modern conveniences.',
    'Collectors Office Road',
    'Trichy',
    'India',
    '620001',
    '+91 431 241 4700',
    'reservations@hotelsangam.com',
    3,
    'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800',
    '["Free WiFi", "Swimming Pool", "Restaurant", "Room Service", "Conference Room", "Parking"]',
    '12:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 52: Kanyakumari - Sparsa Resort
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Sparsa Resort Kanyakumari',
    'A beautiful seaside resort at the southern tip of India, offering spectacular sunrise and sunset views where three seas meet.',
    'Beach Road, Kanyakumari',
    'Kanyakumari',
    'India',
    '629702',
    '+91 4652 247 041',
    'kanyakumari@sparsaresorts.com',
    5,
    'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800',
    '["Free WiFi", "Sea View", "Swimming Pool", "Spa", "Multi-Cuisine Restaurant", "Room Service", "Sunrise/Sunset Deck"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 53: Kanyakumari - Hotel Sea View
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Hotel Sea View Kanyakumari',
    'Experience the magical convergence of the Arabian Sea, Bay of Bengal, and Indian Ocean from this well-located hotel near Vivekananda Rock Memorial.',
    'East Car Street',
    'Kanyakumari',
    'India',
    '629702',
    '+91 4652 246 257',
    'info@seaviewkk.com',
    3,
    'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800',
    '["Free WiFi", "Sea View Rooms", "Restaurant", "Room Service", "Parking", "Tour Desk"]',
    '12:00:00',
    '10:00:00',
    TRUE
);

-- Hotel 54: Mahabalipuram - Radisson Blu Resort Temple Bay
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Radisson Blu Temple Bay Mahabalipuram',
    'A stunning beachfront resort near the UNESCO World Heritage Shore Temple, featuring private villas, multiple pools, and exceptional coastal dining.',
    '57 Kovalam Road',
    'Mahabalipuram',
    'India',
    '603104',
    '+91 44 2744 3636',
    'reservations@radissontemplebay.com',
    5,
    'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800',
    '["Free WiFi", "Private Beach", "Multiple Pools", "Spa", "Ayurveda Center", "Restaurants", "Bar", "Water Sports", "Temple Tours"]',
    '14:00:00',
    '12:00:00',
    TRUE
);

-- Hotel 55: Mahabalipuram - Chariot Beach Resort
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Chariot Beach Resort Mahabalipuram',
    'A charming resort inspired by the stone chariots of Mahabalipuram, offering comfortable beach cottages and a relaxing seaside experience.',
    'Othavadai Street',
    'Mahabalipuram',
    'India',
    '603104',
    '+91 44 2744 2800',
    'info@chariotbeach.com',
    4,
    'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800',
    '["Free WiFi", "Beach Access", "Swimming Pool", "Spa", "Restaurant", "Room Service", "Heritage Tours"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 56: Rameswaram - Hyatt Place Rameswaram
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Hyatt Place Rameswaram',
    'A modern hotel in the sacred island town of Rameswaram, offering comfortable accommodations for pilgrims visiting the famous Ramanathaswamy Temple.',
    '5/32A Pamban Road',
    'Rameswaram',
    'India',
    '623526',
    '+91 4573 223 456',
    'rameswaram@hyatt.com',
    4,
    'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800',
    '["Free WiFi", "Restaurant", "Gym", "Room Service", "Temple Tour Arrangements", "Parking"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 57: Rameswaram - Daiwik Hotels
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Daiwik Hotels Rameswaram',
    'A spiritual retreat hotel designed for pilgrims, offering pure vegetarian dining and peaceful accommodations near the sacred Agni Theertham beach.',
    'Near Ramanathaswamy Temple',
    'Rameswaram',
    'India',
    '623526',
    '+91 4573 221 777',
    'rameswaram@daiwikhotels.com',
    3,
    'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800',
    '["Free WiFi", "Vegetarian Restaurant", "Prayer Hall", "Room Service", "Temple Tour Desk", "Parking"]',
    '12:00:00',
    '10:00:00',
    TRUE
);

-- Hotel 58: Thanjavur - Svatma
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Svatma Thanjavur',
    'An award-winning luxury heritage hotel celebrating Tamil culture through art, architecture, cuisine, and the performing arts near the magnificent Brihadeeswara Temple.',
    '4/862 Trichy Road',
    'Thanjavur',
    'India',
    '613007',
    '+91 4362 230 234',
    'reservations@svatma.in',
    5,
    'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800',
    '["Free WiFi", "Swimming Pool", "Spa", "Cultural Performances", "Traditional Cuisine", "Heritage Walks", "Art Gallery"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 59: Thanjavur - Ideal River View Resort
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Ideal River View Resort Thanjavur',
    'A riverside resort on the banks of River Vennar, offering tranquil views and easy access to the UNESCO World Heritage Chola Temples.',
    '229 Vendanai Village, Punnainallur',
    'Thanjavur',
    'India',
    '613006',
    '+91 4362 250 533',
    'info@idealresorts.com',
    4,
    'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800',
    '["Free WiFi", "River View", "Swimming Pool", "Restaurant", "Room Service", "Boat Rides", "Temple Tours"]',
    '14:00:00',
    '10:00:00',
    TRUE
);

-- Hotel 60: Coonoor - Gateway Coonoor
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Gateway Coonoor - A Taj Hotel',
    'Set on the site of the former Coonoor Club, this heritage hotel offers stunning views of the Nilgiris and colonial-era charm in a tranquil tea garden setting.',
    'Church Road, Upper Coonoor',
    'Coonoor',
    'India',
    '643101',
    '+91 423 223 0021',
    'gateway.coonoor@tajhotels.com',
    5,
    'https://images.unsplash.com/photo-1600011689032-8b628b8a8747?w=800',
    '["Free WiFi", "Tea Garden Views", "Spa", "Restaurant", "Bar", "Room Service", "Nature Walks", "Tea Factory Visits"]',
    '14:00:00',
    '11:00:00',
    TRUE
);

-- Hotel 61: Coonoor - Kurumba Village Resort
INSERT INTO hotels (name, description, address, city, country, postal_code, phone, email, star_rating, hero_image_url, amenities, check_in_time, check_out_time, is_active)
VALUES (
    'Kurumba Village Resort Coonoor',
    'An eco-friendly boutique resort offering rustic luxury amidst the tea plantations and pristine forests of the Nilgiris, perfect for nature lovers.',
    'Nonsuch Tea Estate, Coonoor',
    'Coonoor',
    'India',
    '643103',
    '+91 423 250 0500',
    'reservations@kurumbaresort.com',
    4,
    'https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800',
    '["Free WiFi", "Infinity Pool", "Spa", "Organic Restaurant", "Nature Trails", "Bird Watching", "Tea Tours"]',
    '14:00:00',
    '10:00:00',
    TRUE
);

-- ==================== ROOMS FOR TAMIL NADU HOTELS ====================

-- Rooms for The Leela Palace Chennai (Hotel 32)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Royal Club Room', 'Elegant room with sea view and club access', 'DELUXE', 18500.00, 2, 'King', 45.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "Sea View", "Mini Bar", "TV", "Club Access"]', TRUE FROM hotels WHERE name = 'The Leela Palace Chennai';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Premier Suite', 'Luxurious suite with living area and panoramic views', 'SUITE', 35000.00, 3, 'King', 85.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Free WiFi", "Sea View", "Living Room", "Butler Service", "Jacuzzi"]', TRUE FROM hotels WHERE name = 'The Leela Palace Chennai';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '301', 'Presidential Suite', 'Ultimate luxury with private terrace and dining', 'PRESIDENTIAL', 75000.00, 4, 'King', 150.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?w=800', '["AC", "Private Terrace", "Dining Room", "Butler", "Limousine", "Helicopter Transfer"]', TRUE FROM hotels WHERE name = 'The Leela Palace Chennai';

-- Rooms for ITC Grand Chola (Hotel 33)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'ITC One Room', 'Premium room with exclusive lounge access', 'DELUXE', 16500.00, 2, 'King', 42.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Lounge Access", "Mini Bar", "TV"]', TRUE FROM hotels WHERE name = 'ITC Grand Chola';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Chola Suite', 'Heritage-inspired suite with traditional decor', 'SUITE', 32000.00, 3, 'King', 80.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Free WiFi", "Living Area", "Butler Service", "Bathtub"]', TRUE FROM hotels WHERE name = 'ITC Grand Chola';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '501', 'Rajendra Chola Suite', 'Royal suite with private pool', 'PRESIDENTIAL', 85000.00, 4, 'King', 200.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?w=800', '["AC", "Private Pool", "Dining Room", "Personal Chef", "Limousine"]', TRUE FROM hotels WHERE name = 'ITC Grand Chola';

-- Rooms for Taj Coromandel (Hotel 34)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Superior Room', 'Comfortable room with city views', 'DOUBLE', 12500.00, 2, 'Queen', 35.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV", "Mini Bar", "Safe"]', TRUE FROM hotels WHERE name = 'Taj Coromandel Chennai';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Luxury Suite', 'Elegant suite with premium amenities', 'SUITE', 28000.00, 3, 'King', 70.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Free WiFi", "Living Room", "Bathtub", "Butler"]', TRUE FROM hotels WHERE name = 'Taj Coromandel Chennai';

-- Rooms for The Park Chennai (Hotel 35)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Superior Room', 'Stylish room with contemporary design', 'DOUBLE', 8500.00, 2, 'Queen', 32.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'The Park Chennai';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Luxury Room', 'Premium room with pool view', 'DELUXE', 12000.00, 2, 'King', 40.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "Pool View", "TV", "Mini Bar", "Safe"]', TRUE FROM hotels WHERE name = 'The Park Chennai';

-- Rooms for Le Méridien Coimbatore (Hotel 36)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Deluxe Room', 'Contemporary room with work desk', 'DELUXE', 9500.00, 2, 'King', 38.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV", "Work Desk", "Coffee Maker"]', TRUE FROM hotels WHERE name = 'Le Méridien Coimbatore';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Executive Suite', 'Spacious suite with lounge access', 'SUITE', 18000.00, 3, 'King', 65.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Free WiFi", "Lounge Access", "Living Area", "Bathtub"]', TRUE FROM hotels WHERE name = 'Le Méridien Coimbatore';

-- Rooms for Vivanta Coimbatore (Hotel 37)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Superior Charm', 'Comfortable room with modern amenities', 'DOUBLE', 7500.00, 2, 'Queen', 32.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Vivanta Coimbatore';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Deluxe Delight', 'Premium room with pool view', 'DELUXE', 10500.00, 2, 'King', 38.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "Pool View", "TV", "Safe"]', TRUE FROM hotels WHERE name = 'Vivanta Coimbatore';

-- Rooms for Savoy Hotel Ooty (Hotel 38)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Heritage Room', 'Colonial-style room with garden views', 'DELUXE', 14000.00, 2, 'King', 40.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Fireplace", "Free WiFi", "Garden View", "Tea Maker", "TV"]', TRUE FROM hotels WHERE name = 'Savoy Hotel Ooty';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Garden Suite', 'Luxurious suite with private garden', 'SUITE', 25000.00, 3, 'King', 75.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["Fireplace", "Private Garden", "Living Room", "Butler", "Bathtub"]', TRUE FROM hotels WHERE name = 'Savoy Hotel Ooty';

-- Rooms for Sterling Ooty Elk Hill (Hotel 39)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Studio Room', 'Cozy room with mountain views', 'DOUBLE', 5500.00, 2, 'Queen', 28.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Heater", "Free WiFi", "Mountain View", "TV"]', TRUE FROM hotels WHERE name = 'Sterling Ooty Elk Hill';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Family Suite', 'Spacious suite for families', 'FAMILY', 8500.00, 4, 'Twin + Double', 50.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', '["Heater", "Free WiFi", "Living Area", "TV", "Kitchenette"]', TRUE FROM hotels WHERE name = 'Sterling Ooty Elk Hill';

-- Rooms for Sinclairs Retreat Ooty (Hotel 40)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Comfortable room with valley views', 'SINGLE', 3500.00, 1, 'Single', 22.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Heater", "Free WiFi", "TV"]', TRUE FROM hotels WHERE name = 'Sinclairs Retreat Ooty';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Deluxe Room', 'Premium room with forest views', 'DOUBLE', 5000.00, 2, 'Queen', 30.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["Heater", "Free WiFi", "Forest View", "TV", "Tea Maker"]', TRUE FROM hotels WHERE name = 'Sinclairs Retreat Ooty';

-- Rooms for Carlton Hotel Kodaikanal (Hotel 41)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Valley View Room', 'Room with stunning valley views', 'DELUXE', 12000.00, 2, 'King', 38.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Fireplace", "Free WiFi", "Valley View", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Carlton Hotel Kodaikanal';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Heritage Suite', 'Colonial suite with panoramic views', 'SUITE', 22000.00, 3, 'King', 70.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["Fireplace", "Private Balcony", "Living Room", "Butler", "Bathtub"]', TRUE FROM hotels WHERE name = 'Carlton Hotel Kodaikanal';

-- Rooms for Sterling Kodai Valley (Hotel 42)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Comfortable hill station room', 'DOUBLE', 4500.00, 2, 'Queen', 26.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Heater", "Free WiFi", "TV"]', TRUE FROM hotels WHERE name = 'Sterling Kodai Valley';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Family Suite', 'Spacious suite for families', 'FAMILY', 7500.00, 4, 'Twin + Double', 45.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', '["Heater", "Free WiFi", "Living Area", "TV", "Kitchenette"]', TRUE FROM hotels WHERE name = 'Sterling Kodai Valley';

-- Rooms for Villa Retreat Kodaikanal (Hotel 43)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Garden View Room', 'Cozy room with garden views', 'SINGLE', 3000.00, 1, 'Single', 20.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Heater", "Free WiFi", "Garden View"]', TRUE FROM hotels WHERE name = 'Villa Retreat Kodaikanal';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Double Room', 'Comfortable double room', 'DOUBLE', 4200.00, 2, 'Double', 28.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["Heater", "Free WiFi", "TV", "Garden View"]', TRUE FROM hotels WHERE name = 'Villa Retreat Kodaikanal';

-- Rooms for Heritage Madurai (Hotel 44)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Heritage Room', 'Colonial room with garden views', 'DELUXE', 11000.00, 2, 'King', 42.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Garden View", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Heritage Madurai';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Pool Villa', 'Private villa with pool access', 'SUITE', 22000.00, 3, 'King', 80.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Private Pool Access", "Living Area", "Butler", "Outdoor Shower"]', TRUE FROM hotels WHERE name = 'Heritage Madurai';

-- Rooms for Fortune Pandiyan Hotel (Hotel 45)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Comfortable city hotel room', 'DOUBLE', 5500.00, 2, 'Queen', 30.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV", "Work Desk"]', TRUE FROM hotels WHERE name = 'Fortune Pandiyan Hotel';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Fortune Club Room', 'Premium room with club access', 'DELUXE', 8000.00, 2, 'King', 38.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "Club Access", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Fortune Pandiyan Hotel';

-- Rooms for GRT Regency Madurai (Hotel 46)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Basic comfortable room', 'SINGLE', 3500.00, 1, 'Single', 22.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV"]', TRUE FROM hotels WHERE name = 'GRT Regency Madurai';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Deluxe Room', 'Spacious room with amenities', 'DOUBLE', 5000.00, 2, 'Queen', 30.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "TV", "Mini Bar", "Work Desk"]', TRUE FROM hotels WHERE name = 'GRT Regency Madurai';

-- Rooms for Palais de Mahe Pondicherry (Hotel 47)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'French Quarter Room', 'Elegant colonial-style room', 'DELUXE', 13500.00, 2, 'King', 40.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Heritage Decor", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Palais de Mahe Pondicherry';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Colonial Suite', 'Luxurious French-style suite', 'SUITE', 25000.00, 3, 'King', 75.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Rooftop Access", "Living Area", "Butler", "Bathtub", "Sea View"]', TRUE FROM hotels WHERE name = 'Palais de Mahe Pondicherry';

-- Rooms for Le Pondy Beach Resort (Hotel 48)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Beach Villa', 'Private villa with sea view', 'DELUXE', 15000.00, 2, 'King', 50.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Sea View", "Private Garden", "TV"]', TRUE FROM hotels WHERE name = 'Le Pondy Beach Resort';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Pool Villa', 'Luxury villa with private pool', 'SUITE', 30000.00, 3, 'King', 90.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Private Pool", "Sea View", "Living Area", "Butler", "Outdoor Shower"]', TRUE FROM hotels WHERE name = 'Le Pondy Beach Resort';

-- Rooms for Villa Shanti Pondicherry (Hotel 49)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Heritage Room', 'Charming colonial room', 'DOUBLE', 8000.00, 2, 'Queen', 32.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Courtyard View", "TV"]', TRUE FROM hotels WHERE name = 'Villa Shanti Pondicherry';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Shanti Suite', 'Elegant suite with antiques', 'SUITE', 14000.00, 2, 'King', 55.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Free WiFi", "Antique Furniture", "Sitting Area", "Bathtub"]', TRUE FROM hotels WHERE name = 'Villa Shanti Pondicherry';

-- Rooms for Grand Gardenia Trichy (Hotel 50)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Deluxe Room', 'Modern business hotel room', 'DOUBLE', 6000.00, 2, 'Queen', 32.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV", "Work Desk", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Grand Gardenia Trichy';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Executive Suite', 'Premium suite with living area', 'SUITE', 12000.00, 3, 'King', 60.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Free WiFi", "Living Room", "TV", "Mini Bar", "Bathtub"]', TRUE FROM hotels WHERE name = 'Grand Gardenia Trichy';

-- Rooms for Hotel Sangam Trichy (Hotel 51)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Comfortable basic room', 'SINGLE', 3000.00, 1, 'Single', 22.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV"]', TRUE FROM hotels WHERE name = 'Hotel Sangam Trichy';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Deluxe Room', 'Spacious room with amenities', 'DOUBLE', 4500.00, 2, 'Queen', 30.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Hotel Sangam Trichy';

-- Rooms for Sparsa Resort Kanyakumari (Hotel 52)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Sea View Room', 'Room with stunning sea views', 'DELUXE', 9500.00, 2, 'King', 38.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Sea View", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Sparsa Resort Kanyakumari';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Sunrise Suite', 'Premium suite with sunrise deck', 'SUITE', 18000.00, 3, 'King', 65.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Private Deck", "Sea View", "Living Area", "Bathtub"]', TRUE FROM hotels WHERE name = 'Sparsa Resort Kanyakumari';

-- Rooms for Hotel Sea View Kanyakumari (Hotel 53)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Basic room near temple', 'SINGLE', 2500.00, 1, 'Single', 20.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV"]', TRUE FROM hotels WHERE name = 'Hotel Sea View Kanyakumari';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Sea Facing Room', 'Room with partial sea view', 'DOUBLE', 4000.00, 2, 'Queen', 28.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "Sea View", "TV"]', TRUE FROM hotels WHERE name = 'Hotel Sea View Kanyakumari';

-- Rooms for Radisson Blu Temple Bay (Hotel 54)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Bay Villa', 'Private villa with garden', 'DELUXE', 16000.00, 2, 'King', 55.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Private Garden", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Radisson Blu Temple Bay Mahabalipuram';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Sea View Villa', 'Luxury villa with sea view', 'SUITE', 28000.00, 3, 'King', 85.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Private Pool", "Sea View", "Living Area", "Butler"]', TRUE FROM hotels WHERE name = 'Radisson Blu Temple Bay Mahabalipuram';

-- Rooms for Chariot Beach Resort (Hotel 55)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Beach Cottage', 'Charming cottage near beach', 'DOUBLE', 8500.00, 2, 'Queen', 35.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Beach Access", "TV"]', TRUE FROM hotels WHERE name = 'Chariot Beach Resort Mahabalipuram';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Sea View Cottage', 'Premium cottage with sea view', 'DELUXE', 12000.00, 2, 'King', 45.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "Sea View", "TV", "Mini Bar", "Balcony"]', TRUE FROM hotels WHERE name = 'Chariot Beach Resort Mahabalipuram';

-- Rooms for Hyatt Place Rameswaram (Hotel 56)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Comfortable pilgrimage stay', 'DOUBLE', 5500.00, 2, 'Queen', 30.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV", "Work Desk"]', TRUE FROM hotels WHERE name = 'Hyatt Place Rameswaram';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Premium Room', 'Upgraded room with amenities', 'DELUXE', 7500.00, 2, 'King', 38.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "TV", "Mini Bar", "Sea View"]', TRUE FROM hotels WHERE name = 'Hyatt Place Rameswaram';

-- Rooms for Daiwik Hotels Rameswaram (Hotel 57)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Standard Room', 'Simple pilgrim accommodation', 'SINGLE', 2500.00, 1, 'Single', 20.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "TV"]', TRUE FROM hotels WHERE name = 'Daiwik Hotels Rameswaram';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Family Room', 'Room for families on pilgrimage', 'FAMILY', 4500.00, 4, 'Twin + Double', 40.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', '["AC", "Free WiFi", "TV", "Vegetarian Menu"]', TRUE FROM hotels WHERE name = 'Daiwik Hotels Rameswaram';

-- Rooms for Svatma Thanjavur (Hotel 58)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Traditional Room', 'Authentic Tamil heritage room', 'DELUXE', 14000.00, 2, 'King', 45.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "Heritage Decor", "TV", "Cultural Performances"]', TRUE FROM hotels WHERE name = 'Svatma Thanjavur';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Chola Suite', 'Luxurious suite with art gallery', 'SUITE', 28000.00, 3, 'King', 80.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["AC", "Private Art Collection", "Living Area", "Butler", "Cultural Immersion"]', TRUE FROM hotels WHERE name = 'Svatma Thanjavur';

-- Rooms for Ideal River View Resort (Hotel 59)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'River View Room', 'Room overlooking River Vennar', 'DOUBLE', 6500.00, 2, 'Queen', 32.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["AC", "Free WiFi", "River View", "TV"]', TRUE FROM hotels WHERE name = 'Ideal River View Resort Thanjavur';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Deluxe Cottage', 'Premium cottage with pool access', 'DELUXE', 9500.00, 2, 'King', 45.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', '["AC", "Free WiFi", "Pool Access", "River View", "TV", "Balcony"]', TRUE FROM hotels WHERE name = 'Ideal River View Resort Thanjavur';

-- Rooms for Gateway Coonoor (Hotel 60)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Heritage Room', 'Colonial charm with tea garden views', 'DELUXE', 12000.00, 2, 'King', 40.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Fireplace", "Free WiFi", "Tea Garden View", "TV", "Mini Bar"]', TRUE FROM hotels WHERE name = 'Gateway Coonoor - A Taj Hotel';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Colonial Suite', 'Elegant suite with panoramic views', 'SUITE', 22000.00, 3, 'King', 70.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["Fireplace", "Panoramic View", "Living Room", "Butler", "Bathtub"]', TRUE FROM hotels WHERE name = 'Gateway Coonoor - A Taj Hotel';

-- Rooms for Kurumba Village Resort (Hotel 61)
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '101', 'Eco Cottage', 'Rustic eco-friendly cottage', 'DOUBLE', 9000.00, 2, 'Queen', 35.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', '["Heater", "Free WiFi", "Forest View", "Organic Amenities"]', TRUE FROM hotels WHERE name = 'Kurumba Village Resort Coonoor';
INSERT INTO rooms (hotel_id, room_number, name, description, room_type, price_per_night, capacity, bed_type, size_sqm, image_url, amenities, is_available)
SELECT id, '201', 'Valley View Villa', 'Private villa with infinity pool', 'SUITE', 18000.00, 3, 'King', 65.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', '["Heater", "Private Pool", "Valley View", "Living Area", "Outdoor Deck"]', TRUE FROM hotels WHERE name = 'Kurumba Village Resort Coonoor';

-- ==================== ADD HOTEL IMAGES ====================
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800', 'Hotel Exterior', 1 FROM hotels WHERE name = 'The Leela Palace Chennai';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800', 'Lobby', 2 FROM hotels WHERE name = 'The Leela Palace Chennai';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800', 'Grand Lobby', 1 FROM hotels WHERE name = 'ITC Grand Chola';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800', 'Hotel Exterior', 2 FROM hotels WHERE name = 'ITC Grand Chola';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1600011689032-8b628b8a8747?w=800', 'Heritage Building', 1 FROM hotels WHERE name = 'Savoy Hotel Ooty';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800', 'Gardens', 2 FROM hotels WHERE name = 'Savoy Hotel Ooty';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?w=800', 'Valley View', 1 FROM hotels WHERE name = 'Carlton Hotel Kodaikanal';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800', 'Heritage Property', 1 FROM hotels WHERE name = 'Heritage Madurai';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1561501900-3701fa6a0864?w=800', 'French Colonial', 1 FROM hotels WHERE name = 'Palais de Mahe Pondicherry';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800', 'Beach View', 1 FROM hotels WHERE name = 'Le Pondy Beach Resort';
INSERT INTO hotel_images (hotel_id, image_url, alt_text, sort_order) SELECT id, 'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800', 'Temple Bay View', 1 FROM hotels WHERE name = 'Radisson Blu Temple Bay Mahabalipuram';

-- Verify migration
SELECT 'Tamil Nadu Hotels Added Successfully!' as status;
SELECT COUNT(*) as total_hotels FROM hotels;
SELECT COUNT(*) as total_rooms FROM rooms;
SELECT COUNT(*) as tamilnadu_hotels FROM hotels WHERE country = 'India';
