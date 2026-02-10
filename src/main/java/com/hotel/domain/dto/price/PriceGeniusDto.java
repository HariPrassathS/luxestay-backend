package com.hotel.domain.dto.price;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Price Genius insights
 * 
 * Provides:
 * - Price comparison with market
 * - Deal quality rating
 * - Best time to book suggestions
 * - Price history trends
 * 
 * Design Philosophy:
 * - Real data only (no fake "discounts")
 * - Helpful price context
 * - Builds booking confidence
 * - No dark patterns
 */
public class PriceGeniusDto {
    
    private Long hotelId;
    private Long roomId;
    private BigDecimal currentPrice;
    private DealRating dealRating;
    private PricePosition pricePosition;
    private BigDecimal marketAverage;
    private BigDecimal savingsVsMarket;
    private Double savingsPercent;
    private String priceInsight;
    private PriceTrend trend;
    private List<PriceDataPoint> priceHistory;
    private BookingAdvice bookingAdvice;
    private LocalDate analysisDate;
    
    public enum DealRating {
        EXCELLENT("Excellent Value", "This is a great price", 5),
        GOOD("Good Deal", "Below average for this area", 4),
        FAIR("Fair Price", "About average for this area", 3),
        AVERAGE("Average", "Standard pricing", 2),
        PREMIUM("Premium", "Above market average", 1);
        
        private final String label;
        private final String description;
        private final int stars;
        
        DealRating(String label, String description, int stars) {
            this.label = label;
            this.description = description;
            this.stars = stars;
        }
        
        public String getLabel() { return label; }
        public String getDescription() { return description; }
        public int getStars() { return stars; }
    }
    
    public enum PricePosition {
        LOWEST_IN_AREA("Lowest price in this area"),
        BELOW_AVERAGE("Below area average"),
        AT_AVERAGE("At area average"),
        ABOVE_AVERAGE("Above area average"),
        PREMIUM_TIER("Premium tier pricing");
        
        private final String description;
        
        PricePosition(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    public enum PriceTrend {
        DECREASING("Prices dropping", "fa-arrow-down", "green"),
        STABLE("Prices stable", "fa-minus", "gray"),
        INCREASING("Prices rising", "fa-arrow-up", "orange");
        
        private final String label;
        private final String icon;
        private final String color;
        
        PriceTrend(String label, String icon, String color) {
            this.label = label;
            this.icon = icon;
            this.color = color;
        }
        
        public String getLabel() { return label; }
        public String getIcon() { return icon; }
        public String getColor() { return color; }
    }
    
    /**
     * Historical price data point for trend chart
     */
    public static class PriceDataPoint {
        private LocalDate date;
        private BigDecimal price;
        private boolean isProjected;
        
        public PriceDataPoint() {}
        
        public PriceDataPoint(LocalDate date, BigDecimal price, boolean isProjected) {
            this.date = date;
            this.price = price;
            this.isProjected = isProjected;
        }
        
        // Getters and setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public boolean isProjected() { return isProjected; }
        public void setProjected(boolean projected) { isProjected = projected; }
    }
    
    /**
     * Booking timing advice
     */
    public static class BookingAdvice {
        private String recommendation;
        private String reasoning;
        private Urgency urgency;
        
        public enum Urgency {
            BOOK_NOW("Book now for best price"),
            BOOK_SOON("Good time to book"),
            NO_RUSH("No rush to book"),
            WAIT("Consider waiting for better prices");
            
            private final String label;
            
            Urgency(String label) {
                this.label = label;
            }
            
            public String getLabel() { return label; }
        }
        
        public BookingAdvice() {}
        
        public BookingAdvice(String recommendation, String reasoning, Urgency urgency) {
            this.recommendation = recommendation;
            this.reasoning = reasoning;
            this.urgency = urgency;
        }
        
        // Getters and setters
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        
        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
        
        public Urgency getUrgency() { return urgency; }
        public void setUrgency(Urgency urgency) { this.urgency = urgency; }
    }
    
    // Factory method
    public static PriceGeniusDto forRoom(Long hotelId, Long roomId, BigDecimal price) {
        PriceGeniusDto dto = new PriceGeniusDto();
        dto.hotelId = hotelId;
        dto.roomId = roomId;
        dto.currentPrice = price;
        dto.analysisDate = LocalDate.now();
        return dto;
    }
    
    // Getters and setters
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public DealRating getDealRating() { return dealRating; }
    public void setDealRating(DealRating dealRating) { this.dealRating = dealRating; }
    
    public PricePosition getPricePosition() { return pricePosition; }
    public void setPricePosition(PricePosition pricePosition) { this.pricePosition = pricePosition; }
    
    public BigDecimal getMarketAverage() { return marketAverage; }
    public void setMarketAverage(BigDecimal marketAverage) { this.marketAverage = marketAverage; }
    
    public BigDecimal getSavingsVsMarket() { return savingsVsMarket; }
    public void setSavingsVsMarket(BigDecimal savingsVsMarket) { this.savingsVsMarket = savingsVsMarket; }
    
    public Double getSavingsPercent() { return savingsPercent; }
    public void setSavingsPercent(Double savingsPercent) { this.savingsPercent = savingsPercent; }
    
    public String getPriceInsight() { return priceInsight; }
    public void setPriceInsight(String priceInsight) { this.priceInsight = priceInsight; }
    
    public PriceTrend getTrend() { return trend; }
    public void setTrend(PriceTrend trend) { this.trend = trend; }
    
    public List<PriceDataPoint> getPriceHistory() { return priceHistory; }
    public void setPriceHistory(List<PriceDataPoint> priceHistory) { this.priceHistory = priceHistory; }
    
    public BookingAdvice getBookingAdvice() { return bookingAdvice; }
    public void setBookingAdvice(BookingAdvice bookingAdvice) { this.bookingAdvice = bookingAdvice; }
    
    public LocalDate getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDate analysisDate) { this.analysisDate = analysisDate; }
}
