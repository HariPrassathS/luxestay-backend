package com.hotel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time availability updates.
 * Uses STOMP protocol over WebSocket for bidirectional messaging.
 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics: /topic/availability, /topic/booking
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");
        
        // User-specific destinations (for private notifications)
        config.setUserDestinationPrefix("/user");
        
        log.info("WebSocket message broker configured");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint with SockJS fallback for browsers without native WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000)      // 25 second heartbeat
                .setDisconnectDelay(5000)      // 5 second disconnect delay
                .setSessionCookieNeeded(false);
        
        // Native WebSocket endpoint (for modern browsers/apps)
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
        
        log.info("WebSocket STOMP endpoints registered: /ws (SockJS), /ws-native");
    }
}
