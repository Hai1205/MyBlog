package com.example.aiservice.services;

import com.example.aiservice.dtos.ExtractionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for preserving images in HTML content during AI processing
 * Extracts images (especially base64 data URLs) and replaces them with
 * placeholders
 * to prevent AI from corrupting the base64 strings
 */
@Service
@Slf4j
public class ImagePreservationService {

    // Regex to match <img> tags with src attribute
    // Captures the entire img tag and the src value
    private static final Pattern HTML_IMG_PATTERN = Pattern.compile(
            "<img[^>]*?\\s+src=[\"']([^\"']+)[\"'][^>]*?>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // Placeholder template
    private static final String PLACEHOLDER_TEMPLATE = "{{IMAGE_%d}}";

    /**
     * Extract images from HTML content and replace with placeholders
     * 
     * @param htmlContent Original HTML content with embedded images
     * @return ExtractionResult containing clean content and image mapping
     */
    public ExtractionResult extractImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return new ExtractionResult(htmlContent, new HashMap<>());
        }

        Map<String, String> imageMap = new HashMap<>();
        StringBuffer cleanContent = new StringBuffer();
        Matcher matcher = HTML_IMG_PATTERN.matcher(htmlContent);

        int imageIndex = 0;

        while (matcher.find()) {
            String fullImgTag = matcher.group(0); // Entire <img> tag
            String srcValue = matcher.group(1); // src attribute value

            // Check if it's a base64 image or any image that needs preservation
            // We preserve all images to be safe
            String placeholder = String.format(PLACEHOLDER_TEMPLATE, imageIndex);

            // Store the mapping: placeholder -> original img tag
            imageMap.put(placeholder, fullImgTag);

            // Replace the img tag with placeholder
            matcher.appendReplacement(cleanContent, Matcher.quoteReplacement(placeholder));

            imageIndex++;

            log.debug("Extracted image {}: {} bytes", imageIndex,
                    srcValue.length() > 100 ? "base64 data" : srcValue);
        }

        matcher.appendTail(cleanContent);

        log.info("Extracted {} images from content", imageIndex);

        return new ExtractionResult(cleanContent.toString(), imageMap);
    }

    /**
     * Restore images to processed content by replacing placeholders with original
     * img tags
     * 
     * @param processedContent Content processed by AI with placeholders
     * @param imageMap         Map of placeholders to original image tags
     * @return Final content with restored images
     */
    public String restoreImages(String processedContent, Map<String, String> imageMap) {
        if (processedContent == null || processedContent.isEmpty() || imageMap.isEmpty()) {
            return processedContent;
        }

        String result = processedContent;
        int restoredCount = 0;

        // Replace each placeholder with its original image tag
        for (Map.Entry<String, String> entry : imageMap.entrySet()) {
            String placeholder = entry.getKey();
            String originalImgTag = entry.getValue();

            if (result.contains(placeholder)) {
                result = result.replace(placeholder, originalImgTag);
                restoredCount++;
                log.debug("Restored image: {}", placeholder);
            } else {
                log.warn("Placeholder not found in processed content: {}", placeholder);
            }
        }

        log.info("Restored {} out of {} images", restoredCount, imageMap.size());

        return result;
    }

    /**
     * Validate extraction result
     * Useful for testing and debugging
     * 
     * @param result ExtractionResult to validate
     * @return true if result is valid
     */
    public boolean validateExtractionResult(ExtractionResult result) {
        if (result == null) {
            return false;
        }

        // Check that all placeholders in cleanContent exist in imageMap
        for (int i = 0; i < result.getImageMap().size(); i++) {
            String placeholder = String.format(PLACEHOLDER_TEMPLATE, i);
            if (!result.getCleanContent().contains(placeholder)) {
                log.warn("Validation failed: placeholder {} not found in clean content", placeholder);
                return false;
            }
            if (!result.getImageMap().containsKey(placeholder)) {
                log.warn("Validation failed: placeholder {} not found in image map", placeholder);
                return false;
            }
        }

        return true;
    }
}
