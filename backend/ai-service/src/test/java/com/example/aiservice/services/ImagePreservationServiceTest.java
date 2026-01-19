package com.example.aiservice.services;

import com.example.aiservice.dtos.ExtractionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImagePreservationService
 * Tests image extraction and restoration logic
 */
class ImagePreservationServiceTest {

    private ImagePreservationService service;

    @BeforeEach
    void setUp() {
        service = new ImagePreservationService();
    }

    @Test
    void testExtractAndRestoreSimpleImage() {
        // Given: HTML content with one base64 image
        String originalContent = "<p>Hello world!</p>" +
                "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA\" alt=\"test\">" +
                "<p>More text here</p>";

        // When: Extract images
        ExtractionResult result = service.extractImages(originalContent);

        // Then: Image should be replaced with placeholder
        assertNotNull(result);
        assertEquals(1, result.getImageMap().size());
        assertTrue(result.getCleanContent().contains("{{IMAGE_0}}"));
        assertFalse(result.getCleanContent().contains("data:image"));
        assertFalse(result.getCleanContent().contains("base64"));

        // When: Restore images
        String restored = service.restoreImages(result.getCleanContent(), result.getImageMap());

        // Then: Should match original
        assertEquals(originalContent, restored);
    }

    @Test
    void testExtractMultipleImages() {
        // Given: Content with multiple images
        String content = "<p>First image:</p>" +
                "<img src=\"data:image/png;base64,ABC123\" alt=\"first\">" +
                "<p>Second image:</p>" +
                "<img src=\"data:image/jpeg;base64,XYZ789\" alt=\"second\">" +
                "<p>Third image:</p>" +
                "<img src=\"https://example.com/image.jpg\" alt=\"third\">";

        // When: Extract
        ExtractionResult result = service.extractImages(content);

        // Then: All images extracted
        assertEquals(3, result.getImageMap().size());
        assertTrue(result.getCleanContent().contains("{{IMAGE_0}}"));
        assertTrue(result.getCleanContent().contains("{{IMAGE_1}}"));
        assertTrue(result.getCleanContent().contains("{{IMAGE_2}}"));
        assertFalse(result.getCleanContent().contains("<img"));
    }

    @Test
    void testPreserveImageAttributes() {
        // Given: Image with various attributes
        String content = "<img class=\"rounded\" width=\"100\" height=\"200\" " +
                "src=\"data:image/png;base64,TEST\" alt=\"My Image\" style=\"border:1px\">";

        // When: Extract and restore
        ExtractionResult result = service.extractImages(content);
        String restored = service.restoreImages(result.getCleanContent(), result.getImageMap());

        // Then: All attributes preserved
        assertTrue(restored.contains("class=\"rounded\""));
        assertTrue(restored.contains("width=\"100\""));
        assertTrue(restored.contains("height=\"200\""));
        assertTrue(restored.contains("alt=\"My Image\""));
        assertTrue(restored.contains("style=\"border:1px\""));
    }

    @Test
    void testEmptyContent() {
        // Given: Empty or null content
        ExtractionResult result1 = service.extractImages("");
        ExtractionResult result2 = service.extractImages(null);

        // Then: Should handle gracefully
        assertEquals("", result1.getCleanContent());
        assertEquals(0, result1.getImageMap().size());
        assertNull(result2.getCleanContent());
        assertEquals(0, result2.getImageMap().size());
    }

    @Test
    void testContentWithoutImages() {
        // Given: Content without images
        String content = "<p>Just some text</p><div>No images here</div>";

        // When: Extract
        ExtractionResult result = service.extractImages(content);

        // Then: Content unchanged
        assertEquals(content, result.getCleanContent());
        assertEquals(0, result.getImageMap().size());
    }

    @Test
    void testValidateExtractionResult() {
        // Given: Valid extraction
        String content = "<img src=\"data:image/png;base64,TEST\">";
        ExtractionResult result = service.extractImages(content);

        // Then: Validation should pass
        assertTrue(service.validateExtractionResult(result));
    }

    @Test
    void testComplexHTMLStructure() {
        // Given: Complex HTML with nested structures
        String content = "<div class=\"blog-content\">" +
                "<h1>My Blog Post</h1>" +
                "<p>Introduction text with <strong>bold</strong> and <em>italic</em>.</p>" +
                "<figure>" +
                "  <img src=\"data:image/png;base64,HEADER_IMAGE\" alt=\"Header\">" +
                "  <figcaption>Figure 1</figcaption>" +
                "</figure>" +
                "<p>More content here...</p>" +
                "<ul>" +
                "  <li>Item 1</li>" +
                "  <li>Item 2 with <img src=\"data:image/jpeg;base64,INLINE_IMG\"> inline image</li>" +
                "</ul>" +
                "</div>";

        // When: Process
        ExtractionResult result = service.extractImages(content);
        String restored = service.restoreImages(result.getCleanContent(), result.getImageMap());

        // Then: Structure preserved
        assertEquals(2, result.getImageMap().size());
        assertEquals(content, restored);
        assertTrue(restored.contains("<h1>My Blog Post</h1>"));
        assertTrue(restored.contains("<figcaption>Figure 1</figcaption>"));
    }

    @Test
    void testSingleQuoteAttributes() {
        // Given: Image with single quotes
        String content = "<img src='data:image/png;base64,TEST' alt='My Image'>";

        // When: Extract and restore
        ExtractionResult result = service.extractImages(content);
        String restored = service.restoreImages(result.getCleanContent(), result.getImageMap());

        // Then: Should work with single quotes
        assertEquals(1, result.getImageMap().size());
        assertEquals(content, restored);
    }
}
