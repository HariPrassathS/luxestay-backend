package com.hotel.service;

import com.hotel.domain.entity.Hotel;
import com.hotel.domain.entity.Room;
import com.hotel.domain.entity.RoomType;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for seeding hotel data for specific destinations.
 */
@Service
public class HotelSeedService {

    private static final Logger logger = LoggerFactory.getLogger(HotelSeedService.class);

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    public HotelSeedService(HotelRepository hotelRepository, RoomRepository roomRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Seed hotels for Las Vegas, Los Angeles, London, and Barcelona.
     * Each city gets 1 hotel with 4 rooms.
     */
    @Transactional
    public List<Hotel> seedMissingCityHotels() {
        List<Hotel> seededHotels = new ArrayList<>();
        
        // Seed Las Vegas hotel
        if (!hotelRepository.existsByCity("Las Vegas")) {
            Hotel lasVegas = seedLasVegasHotel();
            seededHotels.add(lasVegas);
            logger.info("Seeded Las Vegas hotel: {}", lasVegas.getName());
        }
        
        // Seed Los Angeles hotel
        if (!hotelRepository.existsByCity("Los Angeles")) {
            Hotel losAngeles = seedLosAngelesHotel();
            seededHotels.add(losAngeles);
            logger.info("Seeded Los Angeles hotel: {}", losAngeles.getName());
        }
        
        // Seed London hotel
        if (!hotelRepository.existsByCity("London")) {
            Hotel london = seedLondonHotel();
            seededHotels.add(london);
            logger.info("Seeded London hotel: {}", london.getName());
        }
        
        // Seed Barcelona hotel
        if (!hotelRepository.existsByCity("Barcelona")) {
            Hotel barcelona = seedBarcelonaHotel();
            seededHotels.add(barcelona);
            logger.info("Seeded Barcelona hotel: {}", barcelona.getName());
        }
        
        return seededHotels;
    }

    private Hotel seedLasVegasHotel() {
        Hotel hotel = Hotel.builder()
                .name("The Venetian Grand Las Vegas")
                .description("Experience the glamour of Las Vegas at The Venetian Grand. Stunning replica canals, world-class casinos, Michelin-starred restaurants, and legendary entertainment await. Your ultimate Vegas adventure starts here.")
                .address("3355 Las Vegas Blvd S")
                .city("Las Vegas")
                .country("USA")
                .postalCode("89109")
                .phone("+1 702-414-1000")
                .email("reservations@venetiangrand.com")
                .starRating(5)
                .heroImageUrl("https://images.unsplash.com/photo-1605833556294-ea5c7a74f57d?w=1200")
                .amenities("[\"Free WiFi\", \"Casino\", \"Multiple Pools\", \"Spa\", \"Fine Dining\", \"Show Tickets\", \"Gondola Rides\", \"Shopping Mall\"]")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .isActive(true)
                .latitude(36.1215)
                .longitude(-115.1739)
                .build();
        
        hotel = hotelRepository.save(hotel);
        
        // Create 4 rooms
        createRoom(hotel, "V101", "Strip View Deluxe Room", 
                "Stunning room overlooking the famous Las Vegas Strip with floor-to-ceiling windows", 
                RoomType.DELUXE, new BigDecimal("299.00"), 2, "King", new BigDecimal("42"),
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600",
                "[\"Free WiFi\", \"Strip View\", \"Minibar\", \"Smart TV\", \"Marble Bathroom\"]");
        
        createRoom(hotel, "V201", "Luxury Canal Suite", 
                "Elegant suite with views of the Grand Canal and Venetian architecture", 
                RoomType.SUITE, new BigDecimal("549.00"), 2, "King", new BigDecimal("65"),
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600",
                "[\"Free WiFi\", \"Canal View\", \"Living Room\", \"Jacuzzi\", \"Butler Service\"]");
        
        createRoom(hotel, "V301", "High Roller Family Suite", 
                "Spacious family suite with separate kids area and gaming setup", 
                RoomType.FAMILY, new BigDecimal("449.00"), 4, "2 Queens", new BigDecimal("75"),
                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600",
                "[\"Free WiFi\", \"Kids Area\", \"Gaming Console\", \"Two Bathrooms\", \"Pool Access\"]");
        
        createRoom(hotel, "V401", "Penthouse Casino Suite", 
                "Ultimate luxury penthouse with private poker room and Strip panorama", 
                RoomType.PRESIDENTIAL, new BigDecimal("1999.00"), 4, "King + Sofa Bed", new BigDecimal("150"),
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600",
                "[\"Free WiFi\", \"360° Strip View\", \"Private Poker Room\", \"Infinity Jacuzzi\", \"Personal Butler\"]");
        
        return hotel;
    }

    private Hotel seedLosAngelesHotel() {
        Hotel hotel = Hotel.builder()
                .name("Beverly Hills Luxe Resort")
                .description("Live like a Hollywood star at Beverly Hills Luxe Resort. Nestled in the heart of Beverly Hills, enjoy celebrity-worthy amenities, rooftop pools with city views, and easy access to Rodeo Drive shopping.")
                .address("9876 Wilshire Blvd")
                .city("Los Angeles")
                .country("USA")
                .postalCode("90210")
                .phone("+1 310-555-0123")
                .email("concierge@bhiluxe.com")
                .starRating(5)
                .heroImageUrl("https://images.unsplash.com/photo-1534190760961-74e8c1c5c3da?w=1200")
                .amenities("[\"Free WiFi\", \"Rooftop Pool\", \"Spa\", \"Fine Dining\", \"Fitness Center\", \"Tesla House Cars\", \"Hollywood Tours\"]")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(12, 0))
                .isActive(true)
                .latitude(34.0736)
                .longitude(-118.4004)
                .build();
        
        hotel = hotelRepository.save(hotel);
        
        createRoom(hotel, "LA101", "Hollywood Hills View Room", 
                "Chic room with stunning views of the Hollywood sign and hills", 
                RoomType.DELUXE, new BigDecimal("379.00"), 2, "King", new BigDecimal("38"),
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600",
                "[\"Free WiFi\", \"Hollywood View\", \"Smart TV\", \"Nespresso Machine\", \"Rain Shower\"]");
        
        createRoom(hotel, "LA201", "Rodeo Drive Suite", 
                "Luxury suite with walk-in closet and shopping concierge service", 
                RoomType.SUITE, new BigDecimal("699.00"), 2, "King", new BigDecimal("70"),
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600",
                "[\"Free WiFi\", \"Walk-in Closet\", \"Living Area\", \"Shopping Concierge\", \"Champagne Welcome\"]");
        
        createRoom(hotel, "LA301", "Celebrity Family Bungalow", 
                "Private bungalow perfect for families with kids pool access", 
                RoomType.FAMILY, new BigDecimal("599.00"), 4, "2 Queens", new BigDecimal("85"),
                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600",
                "[\"Free WiFi\", \"Private Patio\", \"Kids Pool\", \"Game Room Access\", \"Universal Studios Tickets\"]");
        
        createRoom(hotel, "LA401", "Sunset Boulevard Penthouse", 
                "Iconic penthouse with infinity pool and panoramic LA skyline views", 
                RoomType.PRESIDENTIAL, new BigDecimal("2499.00"), 4, "King + Sofa Bed", new BigDecimal("180"),
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600",
                "[\"Free WiFi\", \"Private Infinity Pool\", \"360° Views\", \"Home Theater\", \"Private Chef\", \"Rolls Royce Service\"]");
        
        return hotel;
    }

    private Hotel seedLondonHotel() {
        Hotel hotel = Hotel.builder()
                .name("The Royal Westminster London")
                .description("Experience British elegance at The Royal Westminster. Overlooking Big Ben and the Houses of Parliament, indulge in afternoon tea, world-class dining, and impeccable service in the heart of London.")
                .address("1 Parliament Square")
                .city("London")
                .country("United Kingdom")
                .postalCode("SW1A 0AA")
                .phone("+44 20 7123 4567")
                .email("reservations@royalwestminster.co.uk")
                .starRating(5)
                .heroImageUrl("https://images.unsplash.com/photo-1529290130-4ca3753253ae?w=1200")
                .amenities("[\"Free WiFi\", \"Afternoon Tea\", \"Spa\", \"Big Ben View\", \"Concierge\", \"Valet Parking\", \"Fine Dining\"]")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .isActive(true)
                .latitude(51.5007)
                .longitude(-0.1246)
                .build();
        
        hotel = hotelRepository.save(hotel);
        
        createRoom(hotel, "LDN101", "Classic British Room", 
                "Elegant room with traditional British décor and city views", 
                RoomType.DELUXE, new BigDecimal("349.00"), 2, "Queen", new BigDecimal("32"),
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600",
                "[\"Free WiFi\", \"Tea Set\", \"City View\", \"Minibar\", \"Marble Bathroom\"]");
        
        createRoom(hotel, "LDN201", "Big Ben View Suite", 
                "Magnificent suite with direct views of Big Ben and Parliament", 
                RoomType.SUITE, new BigDecimal("799.00"), 2, "King", new BigDecimal("65"),
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600",
                "[\"Free WiFi\", \"Big Ben View\", \"Afternoon Tea\", \"Living Area\", \"Butler Service\"]");
        
        createRoom(hotel, "LDN301", "Royal Family Suite", 
                "Spacious suite fit for a royal family visit to London", 
                RoomType.FAMILY, new BigDecimal("649.00"), 4, "2 Queens", new BigDecimal("80"),
                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600",
                "[\"Free WiFi\", \"Connecting Rooms\", \"Kids Amenities\", \"London Eye Tickets\", \"Harry Potter Tour\"]");
        
        createRoom(hotel, "LDN401", "Thames Presidential Suite", 
                "Ultimate luxury with panoramic Thames and Westminster views", 
                RoomType.PRESIDENTIAL, new BigDecimal("2799.00"), 4, "King + Sofa Bed", new BigDecimal("160"),
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600",
                "[\"Free WiFi\", \"Thames View\", \"Private Terrace\", \"Grand Piano\", \"Butler\", \"Bentley Service\"]");
        
        return hotel;
    }

    private Hotel seedBarcelonaHotel() {
        Hotel hotel = Hotel.builder()
                .name("Casa Gaudí Barcelona")
                .description("Discover the magic of Barcelona at Casa Gaudí. Our modernist hotel celebrates the genius of Antoni Gaudí with stunning architecture, rooftop pool with Sagrada Família views, and authentic Catalan hospitality.")
                .address("92 Passeig de Gràcia")
                .city("Barcelona")
                .country("Spain")
                .postalCode("08008")
                .phone("+34 93 272 1111")
                .email("reservations@casagaudi.es")
                .starRating(5)
                .heroImageUrl("https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=1200")
                .amenities("[\"Free WiFi\", \"Rooftop Pool\", \"Gaudí Tours\", \"Tapas Bar\", \"Spa\", \"Concierge\", \"Art Collection\"]")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .isActive(true)
                .latitude(41.3917)
                .longitude(2.1649)
                .build();
        
        hotel = hotelRepository.save(hotel);
        
        createRoom(hotel, "BCN101", "Modernist Design Room", 
                "Beautiful room inspired by Gaudí mosaic designs", 
                RoomType.DELUXE, new BigDecimal("289.00"), 2, "Queen", new BigDecimal("30"),
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600",
                "[\"Free WiFi\", \"Design Room\", \"Minibar\", \"Breakfast Included\", \"Balcony\"]");
        
        createRoom(hotel, "BCN201", "Sagrada Família View Suite", 
                "Stunning suite with direct Sagrada Família cathedral views", 
                RoomType.SUITE, new BigDecimal("599.00"), 2, "King", new BigDecimal("60"),
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600",
                "[\"Free WiFi\", \"Sagrada View\", \"Living Area\", \"Cava Welcome\", \"Private Gaudí Tour\"]");
        
        createRoom(hotel, "BCN301", "Mediterranean Family Suite", 
                "Spacious family suite with beach access and kids activities", 
                RoomType.FAMILY, new BigDecimal("499.00"), 4, "2 Queens", new BigDecimal("72"),
                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600",
                "[\"Free WiFi\", \"Beach Access\", \"Kids Club\", \"Family Cooking Class\", \"Aquarium Tickets\"]");
        
        createRoom(hotel, "BCN401", "La Rambla Penthouse", 
                "Iconic penthouse with rooftop terrace and 360° Barcelona views", 
                RoomType.PRESIDENTIAL, new BigDecimal("1899.00"), 4, "King + Sofa Bed", new BigDecimal("140"),
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600",
                "[\"Free WiFi\", \"Rooftop Terrace\", \"Private Pool\", \"360° Views\", \"Personal Butler\", \"Mercedes Service\"]");
        
        return hotel;
    }

    private void createRoom(Hotel hotel, String roomNumber, String name, String description, 
                           RoomType roomType, BigDecimal price, int capacity, String bedType,
                           BigDecimal sizeSqm, String imageUrl, String amenities) {
        Room room = Room.builder()
                .hotel(hotel)
                .roomNumber(roomNumber)
                .name(name)
                .description(description)
                .roomType(roomType)
                .pricePerNight(price)
                .capacity(capacity)
                .bedType(bedType)
                .sizeSqm(sizeSqm)
                .imageUrl(imageUrl)
                .amenities(amenities)
                .isAvailable(true)
                .build();
        
        roomRepository.save(room);
    }
}
