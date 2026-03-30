package com.smartconvert.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "🚀 SmartConvert Backend is ALIVE and Running! Please use the Vercel frontend to convert files.";
    }
}
