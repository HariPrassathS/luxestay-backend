-- V12: Create attractions table and seed with real attraction data for smart itinerary generation

-- ==================== Create Attractions Table ====================
CREATE TABLE IF NOT EXISTS attractions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    city VARCHAR(120) NOT NULL,
    country VARCHAR(120) NOT NULL,
    address VARCHAR(255),
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    category ENUM('LANDMARK', 'RESTAURANT', 'ACTIVITY', 'NATURE', 'CULTURAL', 'SHOPPING', 'ENTERTAINMENT', 'WELLNESS', 'BEACH', 'TEMPLE') NOT NULL,
    best_time ENUM('MORNING', 'AFTERNOON', 'EVENING', 'ANY') NOT NULL DEFAULT 'ANY',
    duration_minutes INT NOT NULL DEFAULT 60,
    mood_tags JSON,
    rating DOUBLE DEFAULT 4.0,
    price_level INT DEFAULT 2,
    image_url VARCHAR(500),
    opening_hours VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_attractions_city (city),
    INDEX idx_attractions_category (category),
    INDEX idx_attractions_coordinates (latitude, longitude),
    INDEX idx_attractions_best_time (best_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== CHENNAI ATTRACTIONS ====================
INSERT INTO attractions (name, description, city, country, address, latitude, longitude, category, best_time, duration_minutes, mood_tags, rating, price_level, opening_hours) VALUES

-- Landmarks & Cultural
('Marina Beach', 'One of the longest urban beaches in the world, perfect for sunrise walks and evening relaxation', 'Chennai', 'India', 'Marina Beach Road, Chennai', 13.0500, 80.2824, 'BEACH', 'MORNING', 90, '["RELAXATION", "FAMILY_FUN", "ROMANTIC_GETAWAY"]', 4.3, 1, '05:00-22:00'),
('Kapaleeshwarar Temple', 'Iconic 7th-century Dravidian temple dedicated to Lord Shiva with stunning architecture', 'Chennai', 'India', 'Mylapore, Chennai', 13.0339, 80.2696, 'TEMPLE', 'MORNING', 60, '["RELAXATION", "FAMILY_FUN"]', 4.6, 1, '05:30-12:00,16:00-21:00'),
('Fort St. George', 'Historic British-era fort housing a museum with colonial artifacts', 'Chennai', 'India', 'Fort St George, Chennai', 13.0797, 80.2875, 'LANDMARK', 'MORNING', 90, '["FAMILY_FUN", "ADVENTURE"]', 4.2, 1, '09:00-17:00'),
('San Thome Cathedral', 'Beautiful 16th-century cathedral built over the tomb of St. Thomas', 'Chennai', 'India', 'San Thome, Chennai', 13.0331, 80.2781, 'LANDMARK', 'AFTERNOON', 45, '["RELAXATION", "ROMANTIC_GETAWAY"]', 4.4, 1, '06:00-20:00'),
('Government Museum', 'One of India oldest museums with rare archaeological and numismatic collections', 'Chennai', 'India', 'Egmore, Chennai', 13.0694, 80.2544, 'CULTURAL', 'AFTERNOON', 120, '["FAMILY_FUN", "ADVENTURE"]', 4.1, 1, '09:30-17:00'),

-- Nature & Parks
('Guindy National Park', 'Urban national park with diverse wildlife including deer and blackbucks', 'Chennai', 'India', 'Guindy, Chennai', 13.0067, 80.2206, 'NATURE', 'MORNING', 120, '["FAMILY_FUN", "ADVENTURE", "RELAXATION"]', 4.0, 2, '09:00-17:30'),
('Semmozhi Poonga', 'Beautiful botanical garden with rare plant species and peaceful walking paths', 'Chennai', 'India', 'Cathedral Road, Chennai', 13.0569, 80.2589, 'NATURE', 'MORNING', 60, '["RELAXATION", "ROMANTIC_GETAWAY", "FAMILY_FUN"]', 4.3, 1, '10:00-20:00'),
('Elliot Beach', 'Quieter beach perfect for romantic evenings and family picnics', 'Chennai', 'India', 'Besant Nagar, Chennai', 13.0003, 80.2717, 'BEACH', 'EVENING', 90, '["ROMANTIC_GETAWAY", "RELAXATION", "FAMILY_FUN"]', 4.2, 1, '05:00-22:00'),

-- Restaurants
('Dakshin - ITC Grand Chola', 'Award-winning South Indian fine dining with authentic regional cuisine', 'Chennai', 'India', 'ITC Grand Chola, Guindy', 13.0108, 80.2232, 'RESTAURANT', 'EVENING', 90, '["ROMANTIC_GETAWAY", "BUSINESS"]', 4.7, 4, '12:30-14:45,19:00-23:30'),
('Murugan Idli Shop', 'Legendary local eatery famous for soft idlis and crispy dosas', 'Chennai', 'India', 'T Nagar, Chennai', 13.0382, 80.2340, 'RESTAURANT', 'MORNING', 45, '["FAMILY_FUN", "ADVENTURE"]', 4.5, 1, '07:00-22:30'),
('Peshawri - ITC Grand Chola', 'Iconic Northwest Frontier cuisine with signature kebabs and breads', 'Chennai', 'India', 'ITC Grand Chola, Guindy', 13.0108, 80.2232, 'RESTAURANT', 'EVENING', 90, '["ROMANTIC_GETAWAY", "BUSINESS"]', 4.6, 4, '19:00-23:30'),
('Saravana Bhavan', 'World-famous vegetarian restaurant chain serving authentic South Indian meals', 'Chennai', 'India', 'Mylapore, Chennai', 13.0342, 80.2694, 'RESTAURANT', 'ANY', 60, '["FAMILY_FUN", "BUSINESS"]', 4.4, 2, '06:30-22:30'),

-- Shopping
('T Nagar Shopping District', 'Chennai busiest shopping area with silk sarees, jewelry, and traditional wear', 'Chennai', 'India', 'T Nagar, Chennai', 13.0400, 80.2341, 'SHOPPING', 'AFTERNOON', 180, '["FAMILY_FUN", "ADVENTURE"]', 4.3, 2, '10:00-21:00'),
('Express Avenue Mall', 'Premium shopping mall with international brands and entertainment', 'Chennai', 'India', 'Royapettah, Chennai', 13.0558, 80.2622, 'SHOPPING', 'AFTERNOON', 120, '["FAMILY_FUN", "RELAXATION"]', 4.2, 3, '10:00-22:00'),
('VR Chennai', 'Luxury shopping destination with high-end brands and gourmet dining', 'Chennai', 'India', 'Anna Nagar, Chennai', 13.0853, 80.2092, 'SHOPPING', 'AFTERNOON', 120, '["ROMANTIC_GETAWAY", "BUSINESS"]', 4.4, 4, '10:00-22:00'),

-- Activities & Entertainment
('Mahabalipuram Day Trip', 'UNESCO World Heritage shore temples and rock-cut caves (60km from Chennai)', 'Chennai', 'India', 'Mahabalipuram', 12.6269, 80.1927, 'ACTIVITY', 'MORNING', 360, '["ADVENTURE", "FAMILY_FUN", "ROMANTIC_GETAWAY"]', 4.8, 2, '06:00-18:00'),
('Muttukadu Boat House', 'Scenic lake with boating and water sports activities', 'Chennai', 'India', 'ECR, Muttukadu', 12.8144, 80.2450, 'ACTIVITY', 'AFTERNOON', 120, '["FAMILY_FUN", "ADVENTURE", "ROMANTIC_GETAWAY"]', 4.1, 2, '08:00-17:30'),
('DakshinaChitra', 'Living heritage museum showcasing South Indian art, architecture and culture', 'Chennai', 'India', 'ECR, Muttukadu', 12.8167, 80.2458, 'CULTURAL', 'AFTERNOON', 150, '["FAMILY_FUN", "ADVENTURE"]', 4.4, 2, '10:00-18:00'),

-- Wellness
('Kaya Kalp Spa - ITC Grand Chola', 'Luxury spa offering traditional Ayurvedic and international treatments', 'Chennai', 'India', 'ITC Grand Chola, Guindy', 13.0108, 80.2232, 'WELLNESS', 'ANY', 120, '["RELAXATION", "ROMANTIC_GETAWAY", "BUSINESS"]', 4.7, 4, '07:00-22:00');

-- ==================== COIMBATORE ATTRACTIONS ====================
INSERT INTO attractions (name, description, city, country, address, latitude, longitude, category, best_time, duration_minutes, mood_tags, rating, price_level, opening_hours) VALUES

('Marudamalai Temple', 'Ancient hilltop temple dedicated to Lord Murugan with panoramic city views', 'Coimbatore', 'India', 'Marudamalai, Coimbatore', 11.0431, 76.8997, 'TEMPLE', 'MORNING', 90, '["RELAXATION", "FAMILY_FUN"]', 4.5, 1, '05:00-13:00,16:00-20:30'),
('Isha Yoga Center', 'World-renowned spiritual center with the iconic Adiyogi statue', 'Coimbatore', 'India', 'Velliangiri Foothills', 11.0153, 76.7372, 'WELLNESS', 'MORNING', 240, '["RELAXATION", "ADVENTURE"]', 4.8, 2, '06:00-20:00'),
('Brookefields Mall', 'Modern shopping mall with brands, dining, and entertainment', 'Coimbatore', 'India', 'Brookefields, Coimbatore', 11.0226, 76.9659, 'SHOPPING', 'AFTERNOON', 120, '["FAMILY_FUN", "RELAXATION"]', 4.1, 3, '10:00-22:00'),
('VOC Park and Zoo', 'Large urban park with zoo, toy train, and recreational facilities', 'Coimbatore', 'India', 'Race Course, Coimbatore', 11.0003, 76.9624, 'NATURE', 'MORNING', 120, '["FAMILY_FUN"]', 4.0, 1, '09:00-18:00'),
('Kovai Kondattam', 'Popular amusement and water park for family entertainment', 'Coimbatore', 'India', 'Perur, Coimbatore', 10.9650, 76.9247, 'ENTERTAINMENT', 'ANY', 240, '["FAMILY_FUN", "ADVENTURE"]', 3.9, 2, '10:30-18:00'),
('Velliangiri Mountains Trek', 'Sacred mountain trek through seven hills for adventure seekers', 'Coimbatore', 'India', 'Velliangiri Hills', 10.9667, 76.7333, 'ACTIVITY', 'MORNING', 480, '["ADVENTURE"]', 4.6, 1, '05:00-18:00'),
('Gedee Car Museum', 'Vintage car museum featuring classic automobiles from different eras', 'Coimbatore', 'India', 'Avinashi Road, Coimbatore', 11.0206, 77.0036, 'CULTURAL', 'AFTERNOON', 90, '["FAMILY_FUN", "ADVENTURE"]', 4.2, 2, '10:00-19:00');

-- ==================== MADURAI ATTRACTIONS ====================
INSERT INTO attractions (name, description, city, country, address, latitude, longitude, category, best_time, duration_minutes, mood_tags, rating, price_level, opening_hours) VALUES

('Meenakshi Amman Temple', 'Magnificent 2500-year-old temple complex with stunning Dravidian architecture', 'Madurai', 'India', 'Madurai Temple City', 9.9195, 78.1193, 'TEMPLE', 'MORNING', 120, '["RELAXATION", "FAMILY_FUN", "ADVENTURE"]', 4.8, 1, '05:00-12:30,16:00-21:30'),
('Thirumalai Nayakkar Palace', 'Grand 17th-century palace showcasing Indo-Saracenic architecture', 'Madurai', 'India', 'Madurai Palace', 9.9167, 78.1217, 'LANDMARK', 'AFTERNOON', 90, '["FAMILY_FUN", "ADVENTURE"]', 4.4, 1, '09:00-17:00'),
('Gandhi Memorial Museum', 'Museum dedicated to Mahatma Gandhi with historical artifacts', 'Madurai', 'India', 'Tamukkam Grounds, Madurai', 9.9264, 78.1350, 'CULTURAL', 'AFTERNOON', 90, '["FAMILY_FUN"]', 4.2, 1, '10:00-17:30'),
('Vaigai Dam', 'Scenic dam with gardens and boating facilities', 'Madurai', 'India', 'Vaigai River, Madurai', 9.9833, 77.9333, 'NATURE', 'EVENING', 90, '["ROMANTIC_GETAWAY", "FAMILY_FUN"]', 4.0, 1, '10:00-17:30'),
('Banana Leaf Restaurant', 'Authentic Chettinad cuisine served on traditional banana leaves', 'Madurai', 'India', 'West Veli Street, Madurai', 9.9200, 78.1150, 'RESTAURANT', 'ANY', 60, '["FAMILY_FUN", "ADVENTURE"]', 4.3, 2, '11:00-23:00'),
('Pudhu Mandapam', 'Historic pillared hall with traditional markets and handicraft shops', 'Madurai', 'India', 'Near Meenakshi Temple', 9.9192, 78.1188, 'SHOPPING', 'AFTERNOON', 90, '["ADVENTURE", "FAMILY_FUN"]', 4.1, 1, '08:00-21:00');

-- ==================== OOTY (UDHAGAMANDALAM) ATTRACTIONS ====================
INSERT INTO attractions (name, description, city, country, address, latitude, longitude, category, best_time, duration_minutes, mood_tags, rating, price_level, opening_hours) VALUES

('Ooty Botanical Gardens', 'Sprawling 55-acre garden with rare plant species and flower shows', 'Ooty', 'India', 'Vannarapettai, Ooty', 11.4157, 76.7053, 'NATURE', 'MORNING', 120, '["RELAXATION", "ROMANTIC_GETAWAY", "FAMILY_FUN"]', 4.5, 1, '08:00-18:30'),
('Nilgiri Mountain Railway', 'UNESCO heritage toy train journey through scenic mountain passes', 'Ooty', 'India', 'Ooty Railway Station', 11.4086, 76.6972, 'ACTIVITY', 'MORNING', 240, '["ROMANTIC_GETAWAY", "FAMILY_FUN", "ADVENTURE"]', 4.7, 2, '07:10-16:00'),
('Ooty Lake', 'Serene artificial lake offering boating amidst eucalyptus trees', 'Ooty', 'India', 'Ooty Lake Road', 11.4100, 76.6900, 'NATURE', 'AFTERNOON', 90, '["ROMANTIC_GETAWAY", "RELAXATION", "FAMILY_FUN"]', 4.3, 2, '08:00-18:00'),
('Doddabetta Peak', 'Highest peak in Nilgiris offering breathtaking panoramic views', 'Ooty', 'India', 'Doddabetta, Ooty', 11.4017, 76.7353, 'NATURE', 'MORNING', 90, '["ADVENTURE", "ROMANTIC_GETAWAY"]', 4.4, 1, '07:00-18:00'),
('Tea Factory Visit', 'Learn about tea processing and enjoy fresh Nilgiri tea tasting', 'Ooty', 'India', 'Doddabetta Road, Ooty', 11.4050, 76.7200, 'ACTIVITY', 'AFTERNOON', 60, '["RELAXATION", "ADVENTURE", "FAMILY_FUN"]', 4.2, 2, '09:00-18:00'),
('Rose Garden', 'Beautiful terraced garden with over 20,000 rose varieties', 'Ooty', 'India', 'Elk Hill, Ooty', 11.4003, 76.6839, 'NATURE', 'MORNING', 60, '["ROMANTIC_GETAWAY", "RELAXATION"]', 4.3, 1, '08:30-18:30'),
('Savoy Restaurant', 'Heritage dining experience in colonial-era hotel with continental cuisine', 'Ooty', 'India', 'Savoy Hotel, Ooty', 11.4100, 76.6950, 'RESTAURANT', 'EVENING', 90, '["ROMANTIC_GETAWAY", "BUSINESS"]', 4.5, 4, '07:00-22:00'),
('Pykara Falls', 'Stunning waterfall surrounded by lush forests, great for nature lovers', 'Ooty', 'India', 'Pykara, Ooty', 11.4800, 76.6000, 'NATURE', 'AFTERNOON', 120, '["ADVENTURE", "ROMANTIC_GETAWAY", "FAMILY_FUN"]', 4.4, 1, '09:00-17:00');

-- ==================== PONDICHERRY ATTRACTIONS ====================
INSERT INTO attractions (name, description, city, country, address, latitude, longitude, category, best_time, duration_minutes, mood_tags, rating, price_level, opening_hours) VALUES

('Promenade Beach', 'Scenic seaside promenade with French colonial architecture backdrop', 'Pondicherry', 'India', 'Rock Beach, Pondicherry', 11.9315, 79.8365, 'BEACH', 'EVENING', 90, '["ROMANTIC_GETAWAY", "RELAXATION"]', 4.5, 1, '00:00-23:59'),
('Auroville', 'Experimental international township promoting human unity', 'Pondicherry', 'India', 'Auroville, Villupuram', 12.0066, 79.8098, 'CULTURAL', 'MORNING', 180, '["RELAXATION", "ADVENTURE"]', 4.6, 1, '09:00-17:00'),
('French Quarter', 'Charming heritage district with colorful colonial buildings and cafes', 'Pondicherry', 'India', 'White Town, Pondicherry', 11.9331, 79.8353, 'LANDMARK', 'AFTERNOON', 120, '["ROMANTIC_GETAWAY", "ADVENTURE"]', 4.4, 2, '00:00-23:59'),
('Sri Aurobindo Ashram', 'Peaceful spiritual community founded by Sri Aurobindo and The Mother', 'Pondicherry', 'India', 'Rue de la Marine, Pondicherry', 11.9350, 79.8342, 'WELLNESS', 'MORNING', 60, '["RELAXATION"]', 4.5, 1, '08:00-12:00,14:00-18:00'),
('Paradise Beach', 'Secluded beach accessible by boat, perfect for swimming and relaxation', 'Pondicherry', 'India', 'Chunnambar, Pondicherry', 11.8792, 79.8050, 'BEACH', 'AFTERNOON', 180, '["ADVENTURE", "ROMANTIC_GETAWAY", "FAMILY_FUN"]', 4.3, 2, '09:00-17:00'),
('Le Cafe', 'Iconic beachfront cafe serving French-inspired coffee and snacks', 'Pondicherry', 'India', 'Goubert Avenue, Pondicherry', 11.9325, 79.8361, 'RESTAURANT', 'EVENING', 60, '["ROMANTIC_GETAWAY", "RELAXATION"]', 4.2, 2, '08:00-23:00'),
('Villa Shanti', 'Award-winning restaurant in restored heritage villa with Indo-French cuisine', 'Pondicherry', 'India', 'Suffren Street, Pondicherry', 11.9342, 79.8350, 'RESTAURANT', 'EVENING', 90, '["ROMANTIC_GETAWAY", "BUSINESS"]', 4.6, 4, '12:00-15:00,19:00-22:30'),
('Serenity Beach', 'Quieter beach popular for surfing and peaceful sunsets', 'Pondicherry', 'India', 'Kottakuppam, Pondicherry', 12.0133, 79.8558, 'BEACH', 'EVENING', 90, '["ADVENTURE", "ROMANTIC_GETAWAY", "RELAXATION"]', 4.2, 1, '05:00-19:00'),
('Pondicherry Museum', 'Colonial-era museum with French artifacts and archaeological collections', 'Pondicherry', 'India', 'St Louis Street, Pondicherry', 11.9350, 79.8300, 'CULTURAL', 'AFTERNOON', 90, '["FAMILY_FUN", "ADVENTURE"]', 4.0, 1, '10:00-17:00');

-- ==================== TRICHY (TIRUCHIRAPPALLI) ATTRACTIONS ====================
INSERT INTO attractions (name, description, city, country, address, latitude, longitude, category, best_time, duration_minutes, mood_tags, rating, price_level, opening_hours) VALUES

('Rockfort Temple', 'Ancient temple complex atop a 273-step rock with stunning views', 'Trichy', 'India', 'Rockfort, Trichy', 10.8150, 78.6972, 'TEMPLE', 'MORNING', 120, '["ADVENTURE", "FAMILY_FUN", "RELAXATION"]', 4.6, 1, '06:00-20:00'),
('Sri Ranganathaswamy Temple', 'Largest functioning Hindu temple in the world spanning 156 acres', 'Trichy', 'India', 'Srirangam, Trichy', 10.8627, 78.6907, 'TEMPLE', 'MORNING', 180, '["RELAXATION", "FAMILY_FUN"]', 4.8, 1, '06:00-13:00,15:00-21:00'),
('Kallanai Dam', 'Ancient dam built by Chola kings, one of the oldest water-diversion structures', 'Trichy', 'India', 'Kallanai, Trichy', 10.8500, 78.8167, 'LANDMARK', 'AFTERNOON', 60, '["FAMILY_FUN", "ADVENTURE"]', 4.2, 1, '06:00-18:00');
