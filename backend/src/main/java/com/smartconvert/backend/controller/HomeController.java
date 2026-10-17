package com.smartconvert.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check & Root Controller
 * 
 * Purpose:
 * - Provides a lightweight root ("/") endpoint for server liveness checks.
 * - Cloud platforms like Render, AWS, and Railway use this URL for automated health probes
 *   to determine if the container is healthy and ready to receive incoming traffic.
 */
@RestController
public class HomeController {

    /**
     * Root endpoint returning a simple status message.
     * Visiting http://localhost:8080/ verifies that the Spring Boot server is active.
     */
    @GetMapping("/")
    public String home() {
        return "🚀 SmartConvert Backend is ALIVE and Running Locally!";
    }
}

