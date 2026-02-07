package com.hotel.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Travel Knowledge Service - Tamil Nadu Intelligence
 * Server-side domain knowledge for the chatbot.
 * This is the SINGLE SOURCE OF TRUTH for travel information.
 */
@Service
public class TravelKnowledgeService {
    
    private Map<String, CityInfo> cities;
    private Map<String, DistanceInfo> distances;
    private List<TravelPackage> packages;
    
    @PostConstruct
    public void init() {
        initializeCities();
        initializeDistances();
        initializePackages();
    }
    
    private void initializeCities() {
        cities = new HashMap<>();
        
        cities.put("chennai", CityInfo.builder()
                .name("Chennai")
                .type("Metropolitan City")
                .description("Capital of Tamil Nadu, known as Gateway to South India. A vibrant metropolis blending ancient temples with modern IT parks.")
                .bestTime("November to February")
                .weather("Tropical wet and dry climate, hot summers (35-40°C), pleasant winters (20-25°C)")
                .food(Arrays.asList("Filter Coffee", "Idli-Sambar", "Dosa", "Chettinad Cuisine", "Kothu Parotta"))
                .attractions(Arrays.asList(
                    "Marina Beach - Second longest urban beach in the world",
                    "Kapaleeshwarar Temple - Ancient Dravidian architecture",
                    "San Thome Cathedral - Built over tomb of St. Thomas",
                    "Fort St. George - First English fortress in India",
                    "Government Museum - One of oldest museums in India"
                ))
                .priceRange(PriceRange.of("₹1,500-3,000", "₹3,500-7,000", "₹8,000-25,000"))
                .build());
        
        cities.put("madurai", CityInfo.builder()
                .name("Madurai")
                .type("Temple City")
                .description("One of the oldest continuously inhabited cities in the world. Known as the Athens of the East.")
                .bestTime("October to March")
                .weather("Semi-arid climate, hot summers, mild winters")
                .food(Arrays.asList("Jigarthanda", "Kari Dosa", "Paruthi Paal", "Meen Kulambu"))
                .attractions(Arrays.asList(
                    "Meenakshi Amman Temple - Iconic temple with 14 gopurams",
                    "Thirumalai Nayakkar Palace - 17th century palace",
                    "Gandhi Memorial Museum - Important historical museum",
                    "Vaigai Dam - Scenic reservoir"
                ))
                .priceRange(PriceRange.of("₹1,000-2,500", "₹2,500-5,000", "₹5,000-15,000"))
                .build());
        
        cities.put("ooty", CityInfo.builder()
                .name("Ooty")
                .type("Hill Station")
                .description("Queen of Hill Stations, nestled in the Nilgiri Hills at 2,240 meters. A colonial-era retreat with tea gardens and eucalyptus forests.")
                .bestTime("April to June, September to November")
                .weather("Cool and pleasant year-round. Summer: 15-25°C, Winter: 5-15°C")
                .food(Arrays.asList("Homemade Chocolates", "Varkey Biscuits", "Fresh Cheese", "Nilgiri Tea"))
                .attractions(Arrays.asList(
                    "Nilgiri Mountain Railway - UNESCO World Heritage toy train",
                    "Botanical Gardens - 55 acres of exotic plants",
                    "Ooty Lake - Boating and scenic views",
                    "Doddabetta Peak - Highest point in Nilgiris",
                    "Tea Factory - Learn about tea production"
                ))
                .priceRange(PriceRange.of("₹2,000-4,000", "₹4,000-8,000", "₹8,000-20,000"))
                .build());
        
        cities.put("kodaikanal", CityInfo.builder()
                .name("Kodaikanal")
                .type("Hill Station")
                .description("Princess of Hill Stations, famous for its star-shaped lake and pine forests. A romantic getaway at 2,133 meters altitude.")
                .bestTime("April to June, September to October")
                .weather("Cool climate year-round. Summer: 15-20°C, Winter: 8-15°C")
                .food(Arrays.asList("Homemade Chocolates", "Cheese", "Eucalyptus Products", "Fresh Fruits"))
                .attractions(Arrays.asList(
                    "Kodaikanal Lake - Star-shaped artificial lake",
                    "Coaker's Walk - Scenic 1km promenade",
                    "Pillar Rocks - 400-feet high granite pillars",
                    "Bryant Park - Beautiful botanical garden",
                    "Silver Cascade Falls - 180-feet waterfall"
                ))
                .priceRange(PriceRange.of("₹1,500-3,500", "₹3,500-7,000", "₹7,000-18,000"))
                .build());
        
        cities.put("pondicherry", CityInfo.builder()
                .name("Pondicherry")
                .type("Coastal Town")
                .description("Former French colony with a unique blend of French and Tamil cultures. Known for its beaches, ashrams, and colonial architecture.")
                .bestTime("October to March")
                .weather("Tropical climate, hot and humid. Best weather in winter months.")
                .food(Arrays.asList("French Cuisine", "Seafood", "Crepes", "Croissants", "Tamil Cuisine"))
                .attractions(Arrays.asList(
                    "Promenade Beach - Scenic seaside walkway",
                    "Auroville - Experimental international township",
                    "Sri Aurobindo Ashram - Spiritual retreat",
                    "French Quarter - Colonial architecture",
                    "Paradise Beach - Pristine beach accessible by boat"
                ))
                .priceRange(PriceRange.of("₹1,500-3,000", "₹3,500-7,000", "₹7,000-15,000"))
                .build());
        
        cities.put("thanjavur", CityInfo.builder()
                .name("Thanjavur")
                .type("Heritage City")
                .description("The rice bowl of Tamil Nadu, famous for the magnificent Brihadeeswarar Temple and classical arts.")
                .bestTime("November to February")
                .weather("Tropical climate, hot summers, pleasant winters")
                .food(Arrays.asList("Thanjavur Thali", "Traditional Sweets", "Filter Coffee"))
                .attractions(Arrays.asList(
                    "Brihadeeswarar Temple - UNESCO World Heritage Site",
                    "Thanjavur Palace - Maratha-era royal palace",
                    "Saraswathi Mahal Library - Ancient palm-leaf manuscripts",
                    "Art Gallery - Chola bronze collection"
                ))
                .priceRange(PriceRange.of("₹1,000-2,000", "₹2,500-5,000", "₹5,000-12,000"))
                .build());
        
        cities.put("rameswaram", CityInfo.builder()
                .name("Rameswaram")
                .type("Pilgrimage Town")
                .description("One of the holiest places in India, located on Pamban Island. Connected to mainland by the iconic Pamban Bridge.")
                .bestTime("October to April")
                .weather("Tropical, hot and humid, sea breeze provides relief")
                .food(Arrays.asList("Seafood", "South Indian Thali", "Fresh Fish Curry"))
                .attractions(Arrays.asList(
                    "Ramanathaswamy Temple - One of 12 Jyotirlingas",
                    "Pamban Bridge - Engineering marvel over sea",
                    "Dhanushkodi - Ghost town at India's tip",
                    "Agni Theertham - Sacred bathing ghat",
                    "APJ Abdul Kalam Memorial"
                ))
                .priceRange(PriceRange.of("₹800-2,000", "₹2,000-4,000", "₹4,000-10,000"))
                .build());
        
        cities.put("coimbatore", CityInfo.builder()
                .name("Coimbatore")
                .type("Industrial City")
                .description("Manchester of South India, known for its textile industry. Gateway to the Western Ghats with pleasant climate.")
                .bestTime("October to March")
                .weather("Moderate climate due to Western Ghats proximity")
                .food(Arrays.asList("Coimbatore Anjaneyar Koil Thattai", "Kovai Halwa", "Arisimitha Sadam"))
                .attractions(Arrays.asList(
                    "Marudamalai Temple - Hilltop temple",
                    "VOC Park and Zoo - Family attraction",
                    "Dhyanalinga - Yogic temple by Sadhguru",
                    "Siruvani Waterfalls - Scenic waterfall"
                ))
                .priceRange(PriceRange.of("₹1,200-2,500", "₹2,500-5,500", "₹5,500-15,000"))
                .build());
        
        cities.put("mahabalipuram", CityInfo.builder()
                .name("Mahabalipuram")
                .type("Heritage Town")
                .description("UNESCO World Heritage Site with stunning Pallava-era rock-cut temples and sculptures from the 7th-8th century.")
                .bestTime("November to February")
                .weather("Tropical coastal climate, pleasant in winter")
                .food(Arrays.asList("Fresh Seafood", "Beach Shack Cuisine", "South Indian"))
                .attractions(Arrays.asList(
                    "Shore Temple - Iconic seaside temple",
                    "Arjuna's Penance - World's largest open-air rock relief",
                    "Five Rathas - Monolithic rock temples",
                    "Tiger Cave - Rock-cut temple",
                    "Mahabalipuram Beach - Surfing and sunbathing"
                ))
                .priceRange(PriceRange.of("₹1,500-3,000", "₹3,500-7,000", "₹7,000-18,000"))
                .build());
        
        cities.put("kanyakumari", CityInfo.builder()
                .name("Kanyakumari")
                .type("Coastal Town")
                .description("Southernmost tip of mainland India where three seas meet. Famous for stunning sunrise and sunset views.")
                .bestTime("October to February")
                .weather("Tropical coastal, pleasant sea breeze")
                .food(Arrays.asList("Seafood", "Banana Chips", "Kerala Cuisine Influence"))
                .attractions(Arrays.asList(
                    "Vivekananda Rock Memorial - Iconic sea-rock monument",
                    "Thiruvalluvar Statue - 133-feet tall statue",
                    "Kanyakumari Temple - Ancient coastal temple",
                    "Sunrise & Sunset Point",
                    "Padmanabhapuram Palace - Wooden palace"
                ))
                .priceRange(PriceRange.of("₹1,000-2,500", "₹2,500-5,000", "₹5,000-12,000"))
                .build());
        
        cities.put("yelagiri", CityInfo.builder()
                .name("Yelagiri")
                .type("Hill Station")
                .description("Lesser-known hill station perfect for a quiet retreat. Known for rose gardens and adventure activities.")
                .bestTime("September to February")
                .weather("Pleasant hill climate, cooler than plains")
                .food(Arrays.asList("Local South Indian", "Fresh Mountain Produce"))
                .attractions(Arrays.asList(
                    "Yelagiri Lake - Boating and pedal boats",
                    "Swamimalai Hills - Trekking destination",
                    "Nature Park - Rose garden and orchids",
                    "Jalagamparai Waterfalls - Seasonal waterfall"
                ))
                .priceRange(PriceRange.of("₹1,000-2,000", "₹2,500-4,500", "₹4,500-10,000"))
                .build());
        
        cities.put("yercaud", CityInfo.builder()
                .name("Yercaud")
                .type("Hill Station")
                .description("Jewel of the South, a quiet coffee-growing hill station in Salem district at 1,515 meters altitude.")
                .bestTime("April to June, September to October")
                .weather("Cool and pleasant, similar to Ooty but less crowded")
                .food(Arrays.asList("Yercaud Coffee", "Local Fruits", "South Indian"))
                .attractions(Arrays.asList(
                    "Yercaud Lake - Central lake for boating",
                    "Lady's Seat - Panoramic viewpoint",
                    "Kiliyur Falls - 300-feet waterfall",
                    "Bear's Cave - Natural cave",
                    "Coffee Plantations - Estate tours"
                ))
                .priceRange(PriceRange.of("₹1,200-2,500", "₹2,500-5,000", "₹5,000-12,000"))
                .build());
    }
    
    private void initializeDistances() {
        distances = new HashMap<>();
        
        // From Chennai
        distances.put("chennai-madurai", DistanceInfo.of(462, "6-7 hours", "Via NH44, passing Trichy"));
        distances.put("chennai-ooty", DistanceInfo.of(560, "8-9 hours", "Via Salem, Erode and Mettupalayam. Tip: Take Nilgiri Mountain Railway from Mettupalayam"));
        distances.put("chennai-kodaikanal", DistanceInfo.of(528, "8-9 hours", "Via Trichy and Dindigul"));
        distances.put("chennai-pondicherry", DistanceInfo.of(150, "2.5-3 hours", "Via ECR - scenic coastal route"));
        distances.put("chennai-thanjavur", DistanceInfo.of(340, "5-6 hours", "Via Trichy"));
        distances.put("chennai-rameswaram", DistanceInfo.of(573, "8-9 hours", "Via Madurai"));
        distances.put("chennai-coimbatore", DistanceInfo.of(505, "7-8 hours", "Via Salem"));
        distances.put("chennai-mahabalipuram", DistanceInfo.of(58, "1-1.5 hours", "Via ECR - beautiful coastal drive"));
        distances.put("chennai-kanyakumari", DistanceInfo.of(705, "10-11 hours", "Via Madurai"));
        distances.put("chennai-yelagiri", DistanceInfo.of(228, "3.5-4 hours", "Via Vellore"));
        distances.put("chennai-yercaud", DistanceInfo.of(365, "5-6 hours", "Via Salem"));
        
        // From Madurai
        distances.put("madurai-ooty", DistanceInfo.of(290, "5-6 hours", "Via Coimbatore"));
        distances.put("madurai-kodaikanal", DistanceInfo.of(120, "2.5-3 hours", "Scenic ghat road"));
        distances.put("madurai-rameswaram", DistanceInfo.of(170, "3-3.5 hours", "Via Pamban Bridge"));
        distances.put("madurai-kanyakumari", DistanceInfo.of(242, "4-5 hours", "Via Tirunelveli"));
        distances.put("madurai-thanjavur", DistanceInfo.of(160, "3-3.5 hours", "Via Trichy"));
        
        // From Coimbatore
        distances.put("coimbatore-ooty", DistanceInfo.of(86, "2-2.5 hours", "Via Mettupalayam, 36 hairpin bends"));
        distances.put("coimbatore-kodaikanal", DistanceInfo.of(175, "3.5-4 hours", "Via Palani"));
        
        // From Ooty
        distances.put("ooty-kodaikanal", DistanceInfo.of(255, "5-6 hours", "Via Palani"));
        
        // From Pondicherry
        distances.put("pondicherry-mahabalipuram", DistanceInfo.of(95, "1.5-2 hours", "Via ECR coastal road"));
        distances.put("pondicherry-thanjavur", DistanceInfo.of(170, "3-3.5 hours", "Via Cuddalore"));
    }
    
    private void initializePackages() {
        packages = Arrays.asList(
            TravelPackage.builder()
                    .name("Weekend Temple Trail")
                    .duration("2 Days / 1 Night")
                    .cities(Arrays.asList("Chennai", "Mahabalipuram"))
                    .highlights(Arrays.asList("Shore Temple", "Five Rathas", "Kapaleeshwarar Temple"))
                    .priceFrom("₹5,999")
                    .build(),
            
            TravelPackage.builder()
                    .name("Hill Station Escape")
                    .duration("4 Days / 3 Nights")
                    .cities(Arrays.asList("Ooty", "Kodaikanal"))
                    .highlights(Arrays.asList("Nilgiri Toy Train", "Ooty Lake", "Kodai Lake", "Pine Forests"))
                    .priceFrom("₹12,999")
                    .build(),
            
            TravelPackage.builder()
                    .name("Temple & Heritage Tour")
                    .duration("5 Days / 4 Nights")
                    .cities(Arrays.asList("Chennai", "Thanjavur", "Madurai"))
                    .highlights(Arrays.asList("Brihadeeswarar Temple", "Meenakshi Temple", "Thirumalai Palace"))
                    .priceFrom("₹18,999")
                    .build(),
            
            TravelPackage.builder()
                    .name("Southern Tip Explorer")
                    .duration("3 Days / 2 Nights")
                    .cities(Arrays.asList("Madurai", "Rameswaram", "Kanyakumari"))
                    .highlights(Arrays.asList("Pamban Bridge", "Vivekananda Memorial", "Three Seas Confluence"))
                    .priceFrom("₹9,999")
                    .build(),
            
            TravelPackage.builder()
                    .name("French Riviera of India")
                    .duration("2 Days / 1 Night")
                    .cities(Arrays.asList("Pondicherry", "Auroville"))
                    .highlights(Arrays.asList("French Quarter", "Promenade Beach", "Sri Aurobindo Ashram"))
                    .priceFrom("₹4,999")
                    .build()
        );
    }
    
    // ========== PUBLIC API ==========
    
    @Cacheable("city-info")
    public CityInfo getCityInfo(String city) {
        if (city == null) return null;
        return cities.get(city.toLowerCase().trim());
    }
    
    public List<String> getAllCities() {
        return new ArrayList<>(cities.keySet());
    }
    
    public List<CityInfo> getAllCityDetails() {
        return new ArrayList<>(cities.values());
    }
    
    @Cacheable("distance")
    public DistanceInfo getDistance(String from, String to) {
        if (from == null || to == null) return null;
        
        String key1 = from.toLowerCase().trim() + "-" + to.toLowerCase().trim();
        String key2 = to.toLowerCase().trim() + "-" + from.toLowerCase().trim();
        
        return distances.getOrDefault(key1, distances.get(key2));
    }
    
    public List<TravelPackage> getAllPackages() {
        return packages;
    }
    
    public List<TravelPackage> getPackagesByCity(String city) {
        if (city == null) return packages;
        String lowerCity = city.toLowerCase().trim();
        return packages.stream()
                .filter(p -> p.getCities().stream()
                        .anyMatch(c -> c.toLowerCase().contains(lowerCity)))
                .toList();
    }
    
    /**
     * Extract city name from user message
     */
    public String extractCity(String message) {
        if (message == null) return null;
        String lower = message.toLowerCase();
        
        for (String city : cities.keySet()) {
            if (lower.contains(city)) {
                return cities.get(city).getName();
            }
        }
        return null;
    }
    
    /**
     * Extract two cities for distance calculation
     */
    public String[] extractCitiesForDistance(String message) {
        if (message == null) return null;
        String lower = message.toLowerCase();
        
        List<String> found = new ArrayList<>();
        for (String city : cities.keySet()) {
            if (lower.contains(city)) {
                found.add(city);
            }
        }
        
        if (found.size() >= 2) {
            return new String[]{found.get(0), found.get(1)};
        }
        return null;
    }
    
    // ========== DTOs ==========
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityInfo {
        private String name;
        private String type;
        private String description;
        private String bestTime;
        private String weather;
        private List<String> food;
        private List<String> attractions;
        private PriceRange priceRange;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistanceInfo {
        private int km;
        private String travelTime;
        private String route;
        
        public static DistanceInfo of(int km, String travelTime, String route) {
            return new DistanceInfo(km, travelTime, route);
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRange {
        private String budget;
        private String midRange;
        private String luxury;
        
        public static PriceRange of(String budget, String midRange, String luxury) {
            return new PriceRange(budget, midRange, luxury);
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelPackage {
        private String name;
        private String duration;
        private List<String> cities;
        private List<String> highlights;
        private String priceFrom;
    }
}
