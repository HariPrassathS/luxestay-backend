package com.hotel.config;

import com.hotel.security.JwtAuthenticationEntryPoint;
import com.hotel.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration.
 * Configures JWT-based authentication and role-based authorization.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          UserDetailsService userDetailsService,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                
                // Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)
                
                // Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                
                // Session management - stateless for JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Health check endpoint (for Railway/monitoring)
                        .requestMatchers("/api/health").permitAll()
                        
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        
                        // Hotel registration - public access for prospective owners
                        .requestMatchers("/api/register-hotel/**").permitAll()
                        
                        // Chatbot endpoints - public access for AI concierge
                        .requestMatchers("/api/chatbot/**").permitAll()
                        
                        // Voice Assistant endpoints - public access for voice search
                        .requestMatchers("/api/voice/**").permitAll()
                        
                        // Loyalty public endpoints - levels info for unauthenticated users
                        .requestMatchers(HttpMethod.GET, "/api/loyalty/levels").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/loyalty/badges").permitAll()
                        
                        // Booking confidence - public access for booking intelligence
                        .requestMatchers(HttpMethod.GET, "/api/bookings/confidence").permitAll()
                        
                        // Reviews public endpoints - hotel reviews and stats
                        .requestMatchers(HttpMethod.GET, "/api/reviews/hotels/**").permitAll()
                        
                        // Trust Score endpoints - public access for trust metrics
                        .requestMatchers(HttpMethod.GET, "/api/trust/**").permitAll()
                        
                        // FlexBook/Cancellation Policy endpoints - public access
                        .requestMatchers(HttpMethod.GET, "/api/policies/**").permitAll()
                        
                        // Live Pulse endpoints - public access for activity metrics
                        .requestMatchers(HttpMethod.GET, "/api/pulse/**").permitAll()
                        
                        // Smart Alerts endpoints - session-based alerts are public
                        .requestMatchers(HttpMethod.GET, "/api/alerts/session").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/alerts/availability").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/alerts/price-drop/**").permitAll()
                        
                        // Price Genius endpoints - public access for price insights
                        .requestMatchers(HttpMethod.GET, "/api/price-genius/**").permitAll()
                        
                        // Stay Countdown endpoints - booking countdown is public for confirmation pages
                        .requestMatchers(HttpMethod.GET, "/api/countdown/booking/**").permitAll()
                        
                        // Guest Match endpoints - popular recommendations are public
                        .requestMatchers(HttpMethod.GET, "/api/guest-match/popular").permitAll()
                        
                        // VIP Concierge endpoints - benefits overview is public
                        .requestMatchers(HttpMethod.GET, "/api/vip/benefits").permitAll()
                        
                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // Hotel Owner endpoints - requires HOTEL_OWNER role
                        .requestMatchers("/api/owner/**").hasRole("HOTEL_OWNER")
                        
                        // Admin endpoints - requires ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                
                // Add JWT filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
