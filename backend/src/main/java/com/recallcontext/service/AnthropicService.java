package com.recallcontext.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recallcontext.exception.AnthropicApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AnthropicService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;
    private final String apiVersion;
    private final String promptTemplate;

    public AnthropicService(
            @Value("${anthropic.api.base-url}") String baseUrl,
            @Value("${anthropic.api.model}") String model,
            @Value("${anthropic.api.max-tokens}") int maxTokens,
            @Value("${anthropic.api.version}") String apiVersion,
            ObjectMapper objectMapper
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
        this.apiVersion = apiVersion;
        this.promptTemplate = loadPromptTemplate();
    }

    /**
     * Analyze meeting transcript using Claude API
     */
    public MeetingAnalysis analyzeMeetingTranscript(String transcript, String apiKey) {
        log.info("Analyzing meeting transcript with Claude API (length: {} chars)", transcript.length());

        try {
            // Create prompt from template
            String prompt = promptTemplate.replace("{transcript}", transcript);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("messages", List.of(
                    Map.of(
                            "role", "user",
                            "content", prompt
                    )
            ));

            // Call Anthropic API
            String response = webClient.post()
                    .uri("/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", apiVersion)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("Anthropic API error: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new AnthropicApiException(
                                "Anthropic API error: " + ex.getMessage(),
                                ex.getStatusCode().value(),
                                ex
                        ));
                    })
                    .onErrorResume(Exception.class, ex -> {
                        log.error("Error calling Anthropic API", ex);
                        return Mono.error(new AnthropicApiException(
                                "Failed to call Anthropic API: " + ex.getMessage(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                ex
                        ));
                    })
                    .block();

            // Parse response
            return parseAnthropicResponse(response);

        } catch (AnthropicApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during transcript analysis", e);
            throw new AnthropicApiException(
                    "Unexpected error during analysis: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e
            );
        }
    }

    /**
     * Parse Anthropic API response and extract meeting analysis
     */
    private MeetingAnalysis parseAnthropicResponse(String response) {
        try {
            log.debug("Parsing Anthropic API response");

            // Parse the API response
            JsonNode responseNode = objectMapper.readTree(response);

            // Extract the content from the first message
            JsonNode contentArray = responseNode.path("content");
            if (contentArray.isEmpty() || !contentArray.isArray()) {
                throw new AnthropicApiException(
                        "Invalid response format: no content array found",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
            }

            // Get the text content from the first content block
            String textContent = contentArray.get(0).path("text").asText();
            if (textContent.isEmpty()) {
                throw new AnthropicApiException(
                        "Invalid response format: no text content found",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
            }

            log.debug("Extracted text content: {}", textContent.substring(0, Math.min(200, textContent.length())));

            // Parse the JSON content
            JsonNode analysisNode = objectMapper.readTree(textContent);

            // Extract metadata for logging
            Map<String, Object> aiMetadata = new HashMap<>();
            aiMetadata.put("model", responseNode.path("model").asText());
            aiMetadata.put("usage", objectMapper.convertValue(responseNode.path("usage"), Map.class));
            aiMetadata.put("stop_reason", responseNode.path("stop_reason").asText());

            // Convert to MeetingAnalysis object
            MeetingAnalysis analysis = objectMapper.convertValue(analysisNode, MeetingAnalysis.class);
            analysis.setAiMetadata(aiMetadata);

            log.info("Successfully parsed meeting analysis: {} key points, {} decisions, {} actions",
                    analysis.getKeyPoints().size(),
                    analysis.getDecisions().size(),
                    analysis.getActionItems().size());

            return analysis;

        } catch (IOException e) {
            log.error("Error parsing Anthropic API response", e);
            throw new AnthropicApiException(
                    "Failed to parse API response: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e
            );
        }
    }

    /**
     * Load prompt template from resources
     */
    private String loadPromptTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/meeting-analysis-prompt.txt");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load prompt template", e);
            throw new RuntimeException("Failed to load prompt template", e);
        }
    }

    /**
     * Meeting analysis result from Claude
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MeetingAnalysis {
        private List<Participant> participants;
        private List<String> keyPoints;
        private List<String> decisions;
        private List<ActionItemData> actionItems;
        private String sentiment;
        private String tone;
        private String summaryText;
        private Map<String, Object> aiMetadata;

        @lombok.Data
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class Participant {
            private String name;
            private String role;
        }

        @lombok.Data
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ActionItemData {
            private String description;
            private String assignee;
            private String dueDate; // YYYY-MM-DD format or null
            private String priority;
        }
    }
}
