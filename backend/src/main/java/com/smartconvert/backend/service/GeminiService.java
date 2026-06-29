package com.smartconvert.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public Map<String, String> summarize(String text) {
        Map<String, String> responseMap = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            responseMap.put("summary", "Document is empty or contains no readable text.");
            responseMap.put("type", "Error");
            return responseMap;
        }

        // If API key is missing or placeholder, use Local Summarizer
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_ACTUAL")) {
            responseMap.put("summary", localSummarize(text));
            responseMap.put("type", "Local");
            return responseMap;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = API_URL + apiKey;

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            parts.put("text", "Summarize this document concisely and professionally. Focus on key points and takeaways: " + text);
            contents.put("parts", Collections.singletonList(parts));
            requestBody.put("contents", Collections.singletonList(contents));

            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            responseMap.put("summary", extractTextFromResponse(response));
            responseMap.put("type", "AI");
            return responseMap;
        } catch (Exception e) {
            // Fallback to local if AI fails
            responseMap.put("summary", localSummarize(text));
            responseMap.put("type", "Local (Fallback)");
            return responseMap;
        }
    }

    private String localSummarize(String text) {
        // Simple extractive summarization algorithm
        String cleanText = text.replaceAll("\\s+", " ").trim();
        String[] sentences = cleanText.split("(?<=[.!?])\\s+");
        
        if (sentences.length <= 3) return text;

        // 1. Calculate word frequencies (simple scoring)
        Map<String, Integer> wordFrequencies = new HashMap<>();
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "is", "at", "which", "on", "and", "a", "an", "of", "to", "in", "it", "that", "this", "for", "with", "as", "was", "were", "be", "been"
        ));

        for (String word : cleanText.toLowerCase().split("\\W+")) {
            if (word.length() > 3 && !stopWords.contains(word)) {
                wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
            }
        }

        // 2. Score sentences based on word frequencies
        Map<String, Double> sentenceScores = new HashMap<>();
        for (String sentence : sentences) {
            double score = 0;
            String[] words = sentence.toLowerCase().split("\\W+");
            for (String word : words) {
                score += wordFrequencies.getOrDefault(word, 0);
            }
            // Normalize by length to avoid bias towards long sentences
            sentenceScores.put(sentence, score / Math.max(1, words.length));
        }

        // 3. Return top 3-4 sentences in original order
        return sentenceScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(4)
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" "));
    }

    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Error parsing AI response.";
        }
    }
}
