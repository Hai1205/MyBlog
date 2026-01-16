package com.example.aiservice.services.gemini;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;

import java.util.ArrayList;
import java.util.List;

// REMOVED @Component - Bean is created in EmbeddingConfig with @Primary
public class GeminiEmbeddingModel implements EmbeddingModel {

    private final GeminiService geminiService;

    public GeminiEmbeddingModel(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();

        for (String text : request.getInstructions()) {
            float[] vector = geminiService.generateEmbedding(text);
            embeddings.add(new Embedding(vector, 0));
        }

        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return geminiService.generateEmbedding(document.getText());
    }

    @Override
    public int dimensions() {
        return 768; // text-embedding-004 dimensions
    }
}
