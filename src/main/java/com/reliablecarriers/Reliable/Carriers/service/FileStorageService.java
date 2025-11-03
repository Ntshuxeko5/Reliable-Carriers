package com.reliablecarriers.Reliable.Carriers.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * File Storage Service for handling photo uploads
 * Stores pickup and delivery photos securely
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.storage.path:uploads}")
    private String storagePath;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Store uploaded file and return URL
     */
    public String storeFile(MultipartFile file, String category) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }

            // Create directory if it doesn't exist
            Path categoryPath = Paths.get(storagePath, category);
            Files.createDirectories(categoryPath);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            
            String filename = UUID.randomUUID().toString() + extension;
            Path targetPath = categoryPath.resolve(filename);

            // Store file
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL
            String fileUrl = baseUrl + "/files/" + category + "/" + filename;
            logger.info("File stored successfully: {}", fileUrl);
            
            return fileUrl;

        } catch (IOException e) {
            logger.error("Failed to store file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract path from URL
            String relativePath = fileUrl.replace(baseUrl + "/files/", "");
            Path filePath = Paths.get(storagePath, relativePath);
            
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.info("File deleted successfully: {}", fileUrl);
            }
            
            return deleted;
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", e.getMessage(), e);
            return false;
        }
    }
}
