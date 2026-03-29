package com.smartconvert.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public String summarize(String text) {
        // Smart Simulation Fallback if API key is missing
        if (apiKey == null || apiKey.isEmpty()) {
            return "Professional Document Analysis (Simulated):\n\n" +
                   "The uploaded document appears to be a formal record. Based on initial structural analysis, this file contains clear headings and structured sections. \n\n" +
                   "Key Insights:\n" +
                   "• Document integrity: High\n" +
                   "• Primary focus: Informational/Instructional\n" +
                   "• Layout: Standard document formatting detected.\n\n" +
                   "[Note: To enable real-time AI context with Gemini, please provide a valid API Key in application.properties]";
        }

        if (text == null || text.trim().isEmpty()) {
            return "Error: No text found in document to summarize.";
        }

        // Limit text to avoid token limits for simple implementation (Flash supports 1M+, but let's keep it reasonable)
        String truncatedText = text.length() > 30000 ? text.substring(0, 30000) : text;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            
            part.put("text", "Please provide a professional and concise summary of the following document text. Focus on the main points and key takeaways:\n\n" + truncatedText);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(GEMINI_API_URL + apiKey, entity, Map.class);
            
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) resContent.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            
            return "Error: Could not parse response from Gemini API.";
        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest e) {
            if (e.getResponseBodyAsString().contains("API_KEY_INVALID") || e.getResponseBodyAsString().contains("API key not valid")) {
                return "AI Summary Error: Your Gemini API Key is invalid or not set. Please obtain a valid key from Google AI Studio and update your application.properties file.";
            }
            return "Error during summarization: " + e.getStatusText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during summarization: " + e.getMessage();
        }
    }
}
