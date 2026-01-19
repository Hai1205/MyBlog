package com.example.aiservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for holding image extraction results
 * Used by ImagePreservationService to preserve images during AI content
 * processing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResult {
    /**
     * Content with images replaced by placeholders (e.g., {{IMAGE_0}}, {{IMAGE_1}})
     */
    private String cleanContent;

    /**
     * Map of placeholders to original image sources
     * Key: placeholder (e.g., "{{IMAGE_0}}")
     * Value: original image src (base64 data URL or regular URL)
     */
    private Map<String, String> imageMap;
}
