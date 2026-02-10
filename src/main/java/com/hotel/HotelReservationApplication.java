package com.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Hotel Reservation System - Main Application Entry Point
 * 
 * This is a production-ready hotel reservation system built with Spring Boot.
 * Features:
 * - JWT-based authentication with role-based access control
 * - RESTful API design following industry best practices
 * - MySQL database with Flyway migrations
 * - Clean layered architecture (Controller -> Service -> Repository)
 * - Async email notifications for login events
 * 
 * @author Final Year Project
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.hotel", "com.luxestay"})
public class HotelReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelReservationApplication.class, args);
        System.out.println("\n" +
            "╔═══════════════════════════════════════════════════════════════╗\n" +
            "║       HOTEL RESERVATION SYSTEM - STARTED SUCCESSFULLY         ║\n" +
            "╠═══════════════════════════════════════════════════════════════╣\n" +
            "║  API Base URL  : http://localhost:8080/api                    ║\n" +
            "║  Swagger UI    : http://localhost:8080/swagger-ui.html        ║\n" +
            "║  API Docs      : http://localhost:8080/api-docs               ║\n" +
            "╚═══════════════════════════════════════════════════════════════╝\n"
        );
    }
}
