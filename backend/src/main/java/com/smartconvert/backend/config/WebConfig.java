package com.smartconvert.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Security & CORS Configuration
 * 
 * What is CORS?
 * - Cross-Origin Resource Sharing (CORS) is a browser security mechanism that blocks
 *   web pages from making requests to a different domain/port than the one that served it.
 * - Because our React frontend runs on one origin (e.g. localhost:5173 or Vercel) and the
 *   Spring Boot backend runs on another (e.g. localhost:8080 or Render), we must configure
 *   CORS to explicitly permit frontend requests.
 * 
 * How this works in Spring Boot:
 * - Implements WebMvcConfigurer to customize Spring MVC's request mapping rules.
 * - Adds a global mapping for all endpoints ("/**") allowing HTTP GET, POST, PUT, DELETE, and OPTIONS.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Reads allowed origins from application.properties (defaults to wildcard "*" if not configured)
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Split comma-separated origins if multiple domains are configured
        String[] origins = allowedOrigins.split(",");
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}

