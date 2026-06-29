package com.smartconvert.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        System.out.println(">>> App Launching from: " + System.getProperty("user.dir"));
        
        // Ensure relative data directory exists for H2
        java.io.File directory = new java.io.File("data");
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            System.out.println(">>> Data directory initialization: " + (created ? "SUCCESS" : "FAILED"));
        } else {
            System.out.println(">>> Data directory: OK (already exists)");
        }
        
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
