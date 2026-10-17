package com.smartconvert.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * Main entry point for the SmartConvert Spring Boot application.
 * 
 * Key Annotations:
 * - @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan.
 * - @EnableScheduling: Allows Spring to execute background scheduled tasks (such as periodic file cleanup).
 */
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        // Ensure the local 'data' folder exists for the embedded H2 database
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Enable headless mode so server environments (without a GUI monitor) can process images and PDF rendering
        System.setProperty("java.awt.headless", "true");

        // Start the Spring Boot embedded Tomcat web server
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Bean definition for RestTemplate.
     * Used across services (e.g. Gemini AI Service) to make outbound HTTP requests.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
