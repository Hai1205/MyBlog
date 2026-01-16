package com.example.aiservice.services.gemini;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final String embeddingModel;
    private final int maxTokens;
    private final Gson gson = new Gson();
    private static final Duration timeout = Duration.ofSeconds(60);

    public GeminiService(
            @Value("${GEMINI_URL}") String baseUrl,
            @Value("${GEMINI_API_KEY}") String apiKey,
            @Value("${GEMINI_CHAT_MODEL}") String model,
            @Value("${GEMINI_EMBEDDING_MODEL}") String embeddingModel,
            @Value("${GEMINI_MAX_TOKENS}") int maxTokens) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.model = model;
        this.embeddingModel = embeddingModel;
        this.maxTokens = maxTokens;
    }

    public String generateContent(String prompt) {
        try {
            log.debug("Calling Gemini API with model: {}", model);

            // Build request body
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            // Add generation config
            // JsonObject generationConfig = new JsonObject();
            // generationConfig.addProperty("temperature", temperature);
            // generationConfig.addProperty("maxOutputTokens", maxTokens);
            // requestBody.add("generationConfig", generationConfig);
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 1.0); // CHANGED: Disable reasoning mode
            generationConfig.addProperty("maxOutputTokens", maxTokens);
            generationConfig.addProperty("topK", 40); // ADD: Limit diversity for speed
            generationConfig.addProperty("topP", 0.95); // ADD: Nucleus sampling
            requestBody.add("generationConfig", generationConfig);

            // Call API
            // String response = webClient.post()
            //         .uri("/models/" + model + ":generateContent?key=" + apiKey)
            //         .header("Content-Type", "application/json")
            //         .bodyValue(gson.toJson(requestBody))
            //         .retrieve()
            //         .bodyToMono(String.class)
            //         .block();
            String response = webClient.post()
                    .uri("/models/" + model + ":generateContent?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(gson.toJson(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(timeout) // ADD THIS LINE
                    .block();

            log.debug("Received response from Gemini API: {}", response);

            // Parse response
            JsonObject responseJson = gson.fromJson(response, JsonObject.class);

            // Check for API errors first
            if (responseJson.has("error")) {
                JsonObject error = responseJson.getAsJsonObject("error");
                String errorMessage = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                log.error("Gemini API error: {}", errorMessage);
                throw new RuntimeException("Gemini API error: " + errorMessage);
            }

            JsonArray candidates = responseJson.getAsJsonArray("candidates");

            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();

                // Check for blocked content
                if (candidate.has("finishReason")) {
                    String finishReason = candidate.get("finishReason").getAsString();
                    log.warn("Gemini finish reason: {}", finishReason);

                    if (!"STOP".equals(finishReason)) {
                        log.error("Content blocked or filtered. Reason: {}", finishReason);
                        throw new RuntimeException("Content blocked by Gemini: " + finishReason);
                    }
                }

                if (candidate.has("content")) {
                    JsonObject contentObj = candidate.getAsJsonObject("content");

                    if (contentObj.has("parts")) {
                        JsonArray partsArray = contentObj.getAsJsonArray("parts");
                        if (partsArray != null && partsArray.size() > 0) {
                            JsonObject partObj = partsArray.get(0).getAsJsonObject();
                            if (partObj.has("text")) {
                                return partObj.get("text").getAsString();
                            }
                        }
                    }

                    log.error("Content object missing 'parts'. Content: {}", contentObj);
                } else {
                    log.error("Candidate missing 'content'. Candidate: {}", candidate);
                }
            }

            log.error("No valid response from Gemini API. Full response: {}", response);
            throw new RuntimeException("No valid response from Gemini API");

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to generate content: " + e.getMessage(), e);
        }
    }

    public float[] generateEmbedding(String text) {
        try {
            log.debug("Generating embedding for text");

            // Build request body
            JsonObject requestBody = new JsonObject();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", text);
            parts.add(part);
            content.add("parts", parts);
            requestBody.add("content", content);

            // Call embedding API
            String response = webClient.post()
                    .uri("/models/" + embeddingModel + ":embedContent?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(gson.toJson(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse response
            JsonObject responseJson = gson.fromJson(response, JsonObject.class);
            JsonObject embedding = responseJson.getAsJsonObject("embedding");
            JsonArray values = embedding.getAsJsonArray("values");

            float[] result = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                result[i] = values.get(i).getAsFloat();
            }

            return result;

        } catch (Exception e) {
            log.error("Error generating embedding", e);
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }
}
