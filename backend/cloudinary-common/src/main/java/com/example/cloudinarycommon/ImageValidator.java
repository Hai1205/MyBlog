package com.example.cloudinarycommon;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class ImageValidator {

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".bmp",
            ".webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public boolean isValidImage(MultipartFile file, StringBuilder errorMessage, Long maxSizeInBytes) {
        if (file == null || file.isEmpty()) {
            errorMessage.append("No file uploaded or file is empty");
            return false;
        }

        long maxSize = (maxSizeInBytes != null) ? maxSizeInBytes : MAX_FILE_SIZE;
        if (file.getSize() > maxSize) {
            errorMessage.append("File is too large. Maximum size is ")
                    .append(maxSize / 1024 / 1024).append("MB");
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            errorMessage.append("File type ").append(contentType)
                    .append(" is not supported. Allowed types: ").append(ALLOWED_MIME_TYPES);
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null || ALLOWED_EXTENSIONS.stream().noneMatch(filename.toLowerCase()::endsWith)) {
            errorMessage.append("File extension not supported. Allowed extensions: ")
                    .append(ALLOWED_EXTENSIONS);
            return false;
        }

        return true;
    }
}