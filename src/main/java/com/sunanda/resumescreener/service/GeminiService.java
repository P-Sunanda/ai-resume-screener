package com.sunanda.resumescreener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiService(WebClient.Builder builder) {
        this.webClient = builder
                .codecs(c -> c.defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public String analyzeResume(String resumeText, String jobDescription) {
        // Retry up to 3 times with delay
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Gemini API attempt {} of {}", attempt, maxRetries);

                // Wait between retries
                if (attempt > 1) {
                    log.info("Waiting 30 seconds before retry...");
                    Thread.sleep(30000);
                }

                String prompt = buildPrompt(resumeText, jobDescription);

                Map<String, Object> requestBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(
                                        Map.of("text", prompt)
                                ))
                        ),
                        "generationConfig", Map.of(
                                "temperature", 0.3,
                                "maxOutputTokens", 500
                        )
                );

                String responseStr = webClient.post()
                        .uri(GEMINI_URL + "?key=" + apiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Gemini response received successfully");

                JsonNode root = objectMapper.readTree(responseStr);
                JsonNode text = root
                        .path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text");

                return text.asText();

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during retry wait");
            } catch (Exception e) {
                log.error("Attempt {} failed: {}", attempt, e.getMessage());
                if (attempt == maxRetries) {
                    throw new RuntimeException("Gemini API failed after "
                            + maxRetries + " attempts: " + e.getMessage());
                }
            }
        }
        throw new RuntimeException("Gemini API failed");
    }

    private String buildPrompt(String resumeText, String jobDescription) {
        // Truncate resume to avoid token limits
        String truncatedResume = resumeText.length() > 3000
                ? resumeText.substring(0, 3000)
                : resumeText;

        return """
            You are an HR recruiter. Analyze this resume against the job description.
            Be concise. Respond in this EXACT format:
            
            SCORE: [0-100]
            SUMMARY: [one sentence]
            STRENGTHS: [comma separated skills]
            GAPS: [comma separated missing skills]
            RECOMMENDATION: [STRONG HIRE / HIRE / MAYBE / REJECT]
            
            JOB DESCRIPTION:
            %s
            
            RESUME:
            %s
            """.formatted(jobDescription, truncatedResume);
    }
}