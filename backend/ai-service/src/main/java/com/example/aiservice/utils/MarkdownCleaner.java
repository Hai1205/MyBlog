package com.example.aiservice.utils;

/**
 * Utility class for cleaning markdown formatting artifacts from AI-generated
 * content
 */
public class MarkdownCleaner {

    /**
     * Clean markdown formatting artifacts from content
     * Removes markdown headers (##, ###, etc.) and horizontal rules (---)
     * while preserving the actual content
     * 
     * @param content The content to clean
     * @return Cleaned content without markdown formatting
     */
    public static String clean(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // Remove markdown headers at the start of lines (##, ###, ####, etc.)
        content = content.replaceAll("(?m)^#{1,6}\\s*", "");

        // Remove horizontal rules (---, ***, ___)
        content = content.replaceAll("(?m)^[-*_]{3,}\\s*$", "");

        // Remove extra blank lines (more than 2 consecutive)
        content = content.replaceAll("\\n{3,}", "\n\n");

        // Trim leading and trailing whitespace
        return content.trim();
    }
}
