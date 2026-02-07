-- =====================================================
-- HOTEL RESERVATION SYSTEM - ADD COORDINATES FOR MAP FEATURE
-- Version: 5.0
-- Description: Add latitude/longitude columns and populate coordinates for all hotels
-- =====================================================

-- Add latitude and longitude columns to hotels table
ALTER TABLE hotels 
ADD COLUMN latitude DECIMAL(10, 7) NULL AFTER is_active,
ADD COLUMN longitude DECIMAL(10, 7) NULL AFTER latitude;

-- Create index for geo-spatial queries
CREATE INDEX idx_hotels_coordinates ON hotels (latitude, longitude);

-- ==================== USA HOTELS (1-6) ====================

-- Hotel 1: Aurora Grand Hotel & Spa - New York, Manhattan (Fifth Avenue)
UPDATE hotels SET latitude = 40.7614, longitude = -73.9776 WHERE id = 1;

-- Hotel 2: Harbor View Resort - Miami Beach (Ocean Boulevard)
UPDATE hotels SET latitude = 25.7907, longitude = -80.1300 WHERE id = 2;

-- Hotel 3: Summit Alpine Lodge - Aspen, Colorado
UPDATE hotels SET latitude = 39.1911, longitude = -106.8175 WHERE id = 3;

-- Hotel 4: The Metropolitan Suites - Chicago (Financial District)
UPDATE hotels SET latitude = 41.8781, longitude = -87.6298 WHERE id = 4;

-- Hotel 5: Serenity Wellness Retreat - Sedona, Arizona
UPDATE hotels SET latitude = 34.8697, longitude = -111.7610 WHERE id = 5;

-- Hotel 6: Oceanview LA (if exists)
UPDATE hotels SET latitude = 34.0195, longitude = -118.4912 WHERE id = 6;

-- ==================== PARIS HOTELS (7-9) ====================

-- Hotel 7: Le Château Royal Paris - Champs-Élysées
UPDATE hotels SET latitude = 48.8698, longitude = 2.3075 WHERE id = 7;

-- Hotel 8: Hotel Montmartre Boutique - Rue Lepic, Montmartre
UPDATE hotels SET latitude = 48.8867, longitude = 2.3350 WHERE id = 8;

-- Hotel 9: Seine River Grand Hotel - Quai de la Tournelle
UPDATE hotels SET latitude = 48.8505, longitude = 2.3522 WHERE id = 9;

-- ==================== LONDON HOTELS (10-12) ====================

-- Hotel 10: The Westminster Palace Hotel - Parliament Square
UPDATE hotels SET latitude = 51.5007, longitude = -0.1246 WHERE id = 10;

-- Hotel 11: Kensington Garden Suites - Kensington High Street
UPDATE hotels SET latitude = 51.5014, longitude = -0.1877 WHERE id = 11;

-- Hotel 12: Shoreditch Modern Hotel - Shoreditch High Street
UPDATE hotels SET latitude = 51.5246, longitude = -0.0778 WHERE id = 12;

-- ==================== TOKYO HOTELS (13-15) ====================

-- Hotel 13: Tokyo Imperial Grand - Marunouchi, Chiyoda
UPDATE hotels SET latitude = 35.6812, longitude = 139.7671 WHERE id = 13;

-- Hotel 14: Shibuya Sky Tower Hotel - Shibuya
UPDATE hotels SET latitude = 35.6580, longitude = 139.7016 WHERE id = 14;

-- Hotel 15: Kyoto-Style Tokyo Ryokan - Asakusa, Taito
UPDATE hotels SET latitude = 35.7148, longitude = 139.7967 WHERE id = 15;

-- ==================== DUBAI HOTELS (16-18) ====================

-- Hotel 16: Burj Al Arab View Resort - Jumeirah Beach
UPDATE hotels SET latitude = 25.1412, longitude = 55.1852 WHERE id = 16;

-- Hotel 17: Marina Skyline Hotel - Dubai Marina
UPDATE hotels SET latitude = 25.0772, longitude = 55.1333 WHERE id = 17;

-- Hotel 18: Desert Oasis Luxury Camp - Al Marmoom Desert
UPDATE hotels SET latitude = 24.9500, longitude = 55.5000 WHERE id = 18;

-- ==================== SYDNEY HOTELS (19-21) ====================

-- Hotel 19: Opera House Harbour Hotel - Circular Quay
UPDATE hotels SET latitude = -33.8568, longitude = 151.2153 WHERE id = 19;

-- Hotel 20: Bondi Beach Resort - Bondi Beach
UPDATE hotels SET latitude = -33.8915, longitude = 151.2767 WHERE id = 20;

-- Hotel 21: Blue Mountains Retreat - Katoomba
UPDATE hotels SET latitude = -33.7139, longitude = 150.3119 WHERE id = 21;

-- ==================== BARCELONA HOTELS (22-24) ====================

-- Hotel 22: Casa Gaudí Barcelona - Passeig de Gràcia
UPDATE hotels SET latitude = 41.3921, longitude = 2.1648 WHERE id = 22;

-- Hotel 23: Gothic Quarter Boutique - Carrer del Bisbe
UPDATE hotels SET latitude = 41.3839, longitude = 2.1764 WHERE id = 23;

-- Hotel 24: Barceloneta Beach Club Hotel - Passeig Marítim
UPDATE hotels SET latitude = 41.3797, longitude = 2.1925 WHERE id = 24;

-- ==================== SINGAPORE HOTELS (25-27) ====================

-- Hotel 25: Marina Bay Sands View Hotel - Bayfront Avenue
UPDATE hotels SET latitude = 1.2834, longitude = 103.8607 WHERE id = 25;

-- Hotel 26: Orchard Road Luxury Suites - Orchard Road
UPDATE hotels SET latitude = 1.3048, longitude = 103.8318 WHERE id = 26;

-- Hotel 27: Sentosa Island Resort - Sentosa Gateway
UPDATE hotels SET latitude = 1.2494, longitude = 103.8303 WHERE id = 27;

-- ==================== MALDIVES HOTELS (28-29) ====================

-- Hotel 28: Crystal Waters Overwater Resort - North Malé Atoll
UPDATE hotels SET latitude = 4.4167, longitude = 73.5000 WHERE id = 28;

-- Hotel 29: Sunset Beach Island Resort - South Ari Atoll
UPDATE hotels SET latitude = 3.8500, longitude = 72.8667 WHERE id = 29;

-- ==================== ROME HOTELS (30-31) ====================

-- Hotel 30: Colosseum View Grand Hotel - Via dei Fori Imperiali
UPDATE hotels SET latitude = 41.8902, longitude = 12.4922 WHERE id = 30;

-- Hotel 31: Trastevere Charm Hotel - Piazza di Santa Maria
UPDATE hotels SET latitude = 41.8897, longitude = 12.4694 WHERE id = 31;

-- ==================== CHENNAI HOTELS (32-35) ====================

-- Hotel 32: The Leela Palace Chennai - MRC Nagar, Adyar Seaface
UPDATE hotels SET latitude = 13.0120, longitude = 80.2707 WHERE id = 32;

-- Hotel 33: ITC Grand Chola - Mount Road, Guindy
UPDATE hotels SET latitude = 13.0103, longitude = 80.2209 WHERE id = 33;

-- Hotel 34: Taj Coromandel Chennai - Nungambakkam
UPDATE hotels SET latitude = 13.0603, longitude = 80.2478 WHERE id = 34;

-- Hotel 35: The Park Chennai - Anna Salai
UPDATE hotels SET latitude = 13.0567, longitude = 80.2532 WHERE id = 35;

-- ==================== COIMBATORE HOTELS (36-37) ====================

-- Hotel 36: Le Méridien Coimbatore - Ramnagar
UPDATE hotels SET latitude = 11.0168, longitude = 76.9558 WHERE id = 36;

-- Hotel 37: Vivanta Coimbatore - Saravanampatti
UPDATE hotels SET latitude = 11.0850, longitude = 76.9980 WHERE id = 37;

-- ==================== OOTY HOTELS (38-40) ====================

-- Hotel 38: Savoy Hotel Ooty - Sylks Road
UPDATE hotels SET latitude = 11.4102, longitude = 76.6950 WHERE id = 38;

-- Hotel 39: Sterling Ooty Elk Hill - Elk Hill
UPDATE hotels SET latitude = 11.4086, longitude = 76.6983 WHERE id = 39;

-- Hotel 40: Fortune Resort Sullivan Court - Selbourne Road
UPDATE hotels SET latitude = 11.4090, longitude = 76.7015 WHERE id = 40;

-- ==================== KODAIKANAL HOTELS (41-43) ====================

-- Hotel 41: The Carlton Kodaikanal - Lake Road
UPDATE hotels SET latitude = 10.2381, longitude = 77.4892 WHERE id = 41;

-- Hotel 42: Sterling Kodai Lake - Coakers Walk
UPDATE hotels SET latitude = 10.2342, longitude = 77.4908 WHERE id = 42;

-- Hotel 43: Club Mahindra Kodaikanal - Fernhill
UPDATE hotels SET latitude = 10.2320, longitude = 77.4862 WHERE id = 43;

-- ==================== MADURAI HOTELS (44-46) ====================

-- Hotel 44: The Gateway Hotel Pasumalai - Pasumalai Hills
UPDATE hotels SET latitude = 9.9252, longitude = 78.0989 WHERE id = 44;

-- Hotel 45: Courtyard by Marriott Madurai - Natham Road
UPDATE hotels SET latitude = 9.9200, longitude = 78.1230 WHERE id = 45;

-- Hotel 46: Fortune Pandiyan Hotel - Race Course Road
UPDATE hotels SET latitude = 9.9312, longitude = 78.1198 WHERE id = 46;

-- ==================== PONDICHERRY HOTELS (47-49) ====================

-- Hotel 47: Palais de Mahé - Rue Bussy
UPDATE hotels SET latitude = 11.9344, longitude = 79.8378 WHERE id = 47;

-- Hotel 48: The Promenade Pondicherry - Goubert Avenue
UPDATE hotels SET latitude = 11.9339, longitude = 79.8356 WHERE id = 48;

-- Hotel 49: Le Pondy Beach Resort - Chunnambar
UPDATE hotels SET latitude = 11.8810, longitude = 79.8256 WHERE id = 49;

-- ==================== MAHABALIPURAM HOTELS (50-51) ====================

-- Hotel 50: Radisson Blu Resort Temple Bay - Shore Temple
UPDATE hotels SET latitude = 12.6178, longitude = 80.2022 WHERE id = 50;

-- Hotel 51: InterContinental Chennai Mahabalipuram - East Coast Road
UPDATE hotels SET latitude = 12.6223, longitude = 80.1865 WHERE id = 51;

-- ==================== YERCAUD HOTELS (52-53) ====================

-- Hotel 52: Sterling Yercaud - Main Road
UPDATE hotels SET latitude = 11.7812, longitude = 78.2066 WHERE id = 52;

-- Hotel 53: GRT Nature Trails Yercaud - Servarayan Temple Road
UPDATE hotels SET latitude = 11.7856, longitude = 78.2012 WHERE id = 53;

-- ==================== THANJAVUR HOTEL (54) ====================

-- Hotel 54: Svatma Thanjavur - MC Road
UPDATE hotels SET latitude = 10.7870, longitude = 79.1378 WHERE id = 54;

-- ==================== CHETTINAD HOTEL (55) ====================

-- Hotel 55: Chidambara Vilas - Kadiapatti Village
UPDATE hotels SET latitude = 10.0667, longitude = 78.8333 WHERE id = 55;

-- ==================== KANYAKUMARI HOTEL (56) ====================

-- Hotel 56: Sparsa Resort Kanyakumari - Beach Road
UPDATE hotels SET latitude = 8.0883, longitude = 77.5385 WHERE id = 56;

-- ==================== RAMESWARAM HOTEL (57) ====================

-- Hotel 57: Daiwik Hotels Rameswaram - Agnitheertham Beach
UPDATE hotels SET latitude = 9.2876, longitude = 79.3129 WHERE id = 57;

-- ==================== TIRUCHIRAPALLI HOTEL (58) ====================

-- Hotel 58: Breeze Residency - Collectors Office Road
UPDATE hotels SET latitude = 10.8155, longitude = 78.6965 WHERE id = 58;

-- ==================== VELLORE HOTEL (59) ====================

-- Hotel 59: Hotel Darling Residency - Sathuvachari
UPDATE hotels SET latitude = 12.9165, longitude = 79.1325 WHERE id = 59;

-- ==================== ADDITIONAL CHENNAI HOTELS (60-61) ====================

-- Hotel 60: Taj Fisherman's Cove - Covelong Beach
UPDATE hotels SET latitude = 12.7894, longitude = 80.2519 WHERE id = 60;

-- Hotel 61: Crowne Plaza Chennai - TTK Road, Alwarpet
UPDATE hotels SET latitude = 13.0395, longitude = 80.2567 WHERE id = 61;

-- Verify coordinates are populated
-- SELECT id, name, city, country, latitude, longitude FROM hotels ORDER BY id;
