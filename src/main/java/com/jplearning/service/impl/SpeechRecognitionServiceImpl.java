package com.jplearning.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jplearning.config.SpeechConfig;
import com.jplearning.service.SpeechRecognitionService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SpeechRecognitionServiceImpl implements SpeechRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(SpeechRecognitionServiceImpl.class);

    @Autowired
    private SpeechConfig speechConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String recognizeSpeech(MultipartFile audioFile, String language) throws IOException {
        // Default to Japanese if not specified
        if (language == null || language.isEmpty()) {
            language = "ja-JP";
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Prepare request to SpeechNote API
            HttpPost httpPost = new HttpPost(speechConfig.getSpeechnoteEndpoint());

            // Add API key as header
            httpPost.setHeader("X-API-Key", speechConfig.getSpeechnoteApiKey());

            // Prepare multipart form data
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("audio", audioFile.getInputStream(),
                    ContentType.APPLICATION_OCTET_STREAM, audioFile.getOriginalFilename());
            builder.addTextBody("language", language);

            // Set entity
            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpPost);

            // Process response
            String responseString = EntityUtils.toString(response.getEntity());

            // Parse JSON response
            JsonNode rootNode = objectMapper.readTree(responseString);

            // Extract recognized text
            if (rootNode.has("results") && rootNode.get("results").isArray() &&
                    rootNode.get("results").size() > 0) {

                JsonNode firstResult = rootNode.get("results").get(0);
                if (firstResult.has("alternatives") && firstResult.get("alternatives").isArray() &&
                        firstResult.get("alternatives").size() > 0) {

                    JsonNode firstAlternative = firstResult.get("alternatives").get(0);
                    if (firstAlternative.has("transcript")) {
                        return firstAlternative.get("transcript").asText();
                    }
                }
            }

            // Return empty string if no text recognized
            return "";
        }
    }

    @Override
    public double calculateAccuracyScore(String targetText, String recognizedText) {
        if (targetText == null || recognizedText == null) {
            return 0.0;
        }

        // Normalize text: remove spaces, convert to lowercase for comparison
        String normalizedTarget = targetText.replaceAll("\\s+", "").toLowerCase();
        String normalizedRecognized = recognizedText.replaceAll("\\s+", "").toLowerCase();

        // Calculate Levenshtein distance
        int distance = levenshteinDistance(normalizedTarget, normalizedRecognized);

        // Convert to accuracy score (0.0 to 1.0)
        int maxLength = Math.max(normalizedTarget.length(), normalizedRecognized.length());
        if (maxLength == 0) {
            return 1.0; // Both strings are empty
        }

        return 1.0 - ((double) distance / maxLength);
    }

    @Override
    public String generatePronunciationFeedback(String targetText, String recognizedText) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Prepare request to Gemini API
            HttpPost httpPost = new HttpPost(speechConfig.getGeminiEndpoint() + ":generateContent");

            // Add API key as query parameter
            httpPost.setURI(java.net.URI.create(httpPost.getURI() + "?key=" + speechConfig.getGeminiApiKey()));

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();

            Map<String, Object> content = new HashMap<>();

            Map<String, Object> part = new HashMap<>();
            String prompt = String.format(
                    "You are a Japanese language tutor evaluating a student's pronunciation. " +
                            "Compare the target Japanese text with what was recognized from their speech. " +
                            "Give specific feedback on pronunciation errors, and suggest improvements. " +
                            "Be concise but helpful, with up to 3 points of feedback.\n\n" +
                            "Target text: \"%s\"\n" +
                            "Recognized speech: \"%s\"\n\n" +
                            "Feedback in English:",
                    targetText, recognizedText
            );
            part.put("text", prompt);

            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            // Set JSON entity
            StringEntity entity = new StringEntity(objectMapper.writeValueAsString(requestBody),
                    ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpPost);

            // Process response
            String responseString = EntityUtils.toString(response.getEntity());

            // Parse JSON response
            JsonNode rootNode = objectMapper.readTree(responseString);

            // Extract generated text
            if (rootNode.has("candidates") && rootNode.get("candidates").isArray() &&
                    rootNode.get("candidates").size() > 0) {

                JsonNode firstCandidate = rootNode.get("candidates").get(0);
                if (firstCandidate.has("content") &&
                        firstCandidate.get("content").has("parts") &&
                        firstCandidate.get("content").get("parts").isArray() &&
                        firstCandidate.get("content").get("parts").size() > 0) {

                    JsonNode firstPart = firstCandidate.get("content").get("parts").get(0);
                    if (firstPart.has("text")) {
                        return firstPart.get("text").asText();
                    }
                }
            }

            return "Unable to generate pronunciation feedback.";
        }
    }

    // Helper method to calculate Levenshtein distance
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) != s2.charAt(j - 1)) ? 1 : 0;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}