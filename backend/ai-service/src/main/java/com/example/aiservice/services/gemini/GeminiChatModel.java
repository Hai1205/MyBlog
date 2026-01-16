package com.example.aiservice.services.gemini;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

// REMOVED @Component - Bean is created in RAGConfig
public class GeminiChatModel implements ChatModel {

    private final GeminiService geminiService;

    public GeminiChatModel(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        // Extract text from prompt
        StringBuilder promptText = new StringBuilder();
        for (Message message : prompt.getInstructions()) {
            promptText.append(message.getText()).append("\n");
        }

        // Call Gemini
        String response = geminiService.generateContent(promptText.toString());

        // Build ChatResponse
        AssistantMessage assistantMessage = new AssistantMessage(response);
        Generation generation = new Generation(assistantMessage);
        return new ChatResponse(List.of(generation));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // For simplicity, just return a single response
        return Flux.just(call(prompt));
    }
}
