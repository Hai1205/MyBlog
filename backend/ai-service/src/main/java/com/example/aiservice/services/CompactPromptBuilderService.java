package com.example.aiservice.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class CompactPromptBuilderService {

  public String buildTitlePrompt(String userTitle, List<String> relevantExamples) {
    String examples = relevantExamples.isEmpty() ? "No examples available" :
        relevantExamples.stream().collect(Collectors.joining("\n- "));

    return String.format("""
        You are an expert blog title generator. Create an engaging, SEO-optimized title based on the user's input.

        User Title: %s

        Relevant Examples from Knowledge Base:
        - %s

        Instructions:
        - Make it catchy and attention-grabbing
        - Include relevant keywords for SEO
        - Keep it under 60 characters
        - Use title case
        - Make it unique and original

        Return only the title, nothing else.
        """, userTitle, examples);
  }

  public String buildDescriptionPrompt(String title, String userDescription, List<String> relevantExamples) {
    String examples = relevantExamples.isEmpty() ? "No examples available" :
        relevantExamples.stream().collect(Collectors.joining("\n- "));

    return String.format("""
        You are an expert blog description writer. Create a compelling meta description for the blog post.

        Title: %s
        User Description: %s

        Relevant Examples from Knowledge Base:
        - %s

        Instructions:
        - Write a concise summary (120-160 characters)
        - Include call-to-action or hook
        - Incorporate SEO keywords naturally
        - Make it engaging and clickable
        - Focus on value proposition

        Return only the description, nothing else.
        """, title, userDescription, examples);
  }

  public String buildContentPrompt(String userContent, List<String> relevantExamples) {
    String examples = relevantExamples.isEmpty() ? "No examples available" :
        relevantExamples.stream().collect(Collectors.joining("\n\nExample:\n"));

    return String.format("""
        You are an expert content enhancer. Improve and expand the user's blog content using RAG techniques.

        User Content: %s

        Relevant Examples from Knowledge Base:
        %s

        Instructions:
        - Enhance the content with more details and examples
        - Maintain the original meaning and structure
        - Add relevant information from examples
        - Improve readability and engagement
        - Keep professional tone
        - Expand to comprehensive article length

        Return the enhanced content only.
        """, userContent, examples);
  }
}
