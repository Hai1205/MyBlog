package com.example.cloudinarycommon;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloudinaryService {

    private final ImageValidator imageValidator;

    @Value("${CLOUDINARY_URL}")
    private String cloudinaryUrl;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    private static final String FOLDER_NAME = "MyBlog";

    public Map<String, Object> uploadImage(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            StringBuilder errorMessage = new StringBuilder();
            if (!imageValidator.isValidImage(file, errorMessage, null)) {
                throw new IllegalArgumentException(errorMessage.toString());
            }

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", FOLDER_NAME,
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false,
                    "transformation", new Transformation<>()
                            .quality("auto")
                            .fetchFormat("auto"));

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            result.put("publicId", uploadResult.get("public_id"));
            result.put("url", uploadResult.get("secure_url"));
            result.put("format", uploadResult.get("format"));

        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            result.put("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    public List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            results.add(Map.of("error", "No files uploaded"));
            return results;
        }

        for (MultipartFile file : files) {
            results.add(uploadImage(file));
        }

        return results;
    }

    public Map<String, Object> deleteImage(String publicId) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (publicId == null || publicId.isBlank()) {
                throw new IllegalArgumentException("No public ID provided");
            }

            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            if (deleteResult.get("result").equals("ok")) {
                result.put("status", "deleted");
            } else {
                result.put("error", deleteResult.get("result"));
            }

        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    public List<Map<String, Object>> deleteMultipleImages(List<String> publicIds) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (publicIds == null || publicIds.isEmpty()) {
            results.add(Map.of("error", "No public IDs provided"));
            return results;
        }

        for (String publicId : publicIds) {
            results.add(deleteImage(publicId));
        }

        return results;
    }
}