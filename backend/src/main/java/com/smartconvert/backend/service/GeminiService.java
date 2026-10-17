package com.smartconvert.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GeminiService - Hybrid Document Summarization Service
 * 
 * Architecture:
 * 1. Cloud AI Mode (Google Gemini 1.5 Flash):
 *    - Makes outbound REST API calls using Spring's RestTemplate to Google's Generative Language API.
 *    - Leverages Gemini 1.5 Flash for high-speed, concise document summaries.
 * 
 * 2. Offline / Local Fallback Mode (Extractive NLP Algorithm):
 *    - Automatically activates when GEMINI_API_KEY is not configured or if cloud network fails.
 *    - Computes Term-Frequency (TF) scoring and sentence ranking entirely in-memory with zero external dependencies.
 */
@Service
public class GeminiService {

    // Injected from environment or application.properties
    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Summarizes the provided text document.
     * 
     * @param text Raw extracted document text
     * @return Map containing "summary" text and "type" ("AI", "Local", or "Local (Fallback)")
     */
    public Map<String, String> summarize(String text) {
        Map<String, String> responseMap = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            responseMap.put("summary", "Document is empty or contains no readable text.");
            responseMap.put("type", "Error");
            return responseMap;
        }

        // If API key is missing or blank, use the fast local summarizer
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_ACTUAL")) {
            responseMap.put("summary", localSummarize(text));
            responseMap.put("type", "Local");
            return responseMap;
        }

        // Attempt Cloud AI summarization via Google Gemini 1.5 Flash
        try {
            String url = API_URL + apiKey;

            // Construct Gemini API payload structure: { contents: [ { parts: [ { text: "..." } ] } ] }
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            parts.put("text", "Summarize this document concisely and professionally. Focus on key points and takeaways: " + text);
            contents.put("parts", Collections.singletonList(parts));
            requestBody.put("contents", Collections.singletonList(contents));

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            responseMap.put("summary", extractTextFromResponse(response));
            responseMap.put("type", "AI");
            return responseMap;
        } catch (Exception e) {
            // Graceful degradation: If Google API fails (e.g. quota limit, network glitch), fall back to local NLP
            responseMap.put("summary", localSummarize(text));
            responseMap.put("type", "Local (Fallback)");
            return responseMap;
        }
    }

    /**
     * In-Memory Extractive NLP Summarization Algorithm
     * 
     * How it works:
     * 1. Splits text into sentences using regex boundary lookbehinds.
     * 2. Filters out common English stop words ("the", "is", "at", etc.).
     * 3. Calculates word frequency distribution across the entire text.
     * 4. Scores each sentence by summing word frequencies and normalizing by sentence length.
     * 5. Returns the top 4 most representative sentences.
     */
    private String localSummarize(String text) {
        String cleanText = text.replaceAll("\\s+", " ").trim();
        String[] sentences = cleanText.split("(?<=[.!?])\\s+");
        
        if (sentences.length <= 3) return text;

        // 1. Calculate word frequencies (stop words excluded)
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
            // Normalize by word count to avoid bias toward longer sentences
            sentenceScores.put(sentence, score / Math.max(1, words.length));
        }

        // 3. Return top 4 highest-scoring sentences
        return sentenceScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(4)
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" "));
    }

    /**
     * Extracts the generated text from Gemini's nested JSON response.
     */
    @SuppressWarnings("unchecked")
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

