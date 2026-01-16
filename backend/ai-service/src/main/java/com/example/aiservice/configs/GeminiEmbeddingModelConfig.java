package com.example.aiservice.configs;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.*;
import java.util.List;
import java.util.stream.Collectors;

public class GeminiEmbeddingModelConfig implements EmbeddingModel {

    private final String apiKey;
    private final String apiUrl;

    public GeminiEmbeddingModelConfig(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public float[] embed(Document document) {
        return embed(document.getText());
    }

    @Override
    public float[] embed(String text) {
        try {
            String requestBody = buildRequestBody(text);

            var client = java.net.http.HttpClient.newHttpClient();
            var httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = client.send(httpRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return parseEmbedding(response.body());
            } else {
                throw new RuntimeException("Gemini API error: " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Embedding failed", e);
        }
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> texts = request.getInstructions();

        List<Embedding> embeddings = texts.stream()
                .map(text -> new Embedding(embed(text), texts.indexOf(text)))
                .collect(Collectors.toList());

        return new EmbeddingResponse(embeddings);
    }

    @Override
    public int dimensions() {
        return 768;
    }

    private String buildRequestBody(String text) {
        return String.format(
                "{\"content\":{\"parts\":[{\"text\":\"%s\"}]},\"taskType\":\"RETRIEVAL_DOCUMENT\"}",
                escapeJson(text)
        );
    }

    private float[] parseEmbedding(String jsonResponse) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var values = mapper.readTree(jsonResponse)
                    .path("embedding").path("values");

            float[] embed = new float[values.size()];
            for (int i = 0; i < values.size(); i++)
                embed[i] = (float) values.get(i).asDouble();

            return embed;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse embedding response", e);
        }
    }

    private String escapeJson(String text) {
        return text == null ? "" : text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

