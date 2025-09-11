package com.reliablecarriers.Reliable.Carriers.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileUploadService {
    
    /**
     * Upload a single file
     */
    String uploadFile(MultipartFile file, String directory) throws IOException;
    
    /**
     * Upload multiple files
     */
    List<String> uploadFiles(List<MultipartFile> files, String directory) throws IOException;
    
    /**
     * Upload proof of delivery photo
     */
    String uploadDeliveryPhoto(MultipartFile photo, String trackingNumber) throws IOException;
    
    /**
     * Upload signature image
     */
    String uploadSignature(MultipartFile signature, String trackingNumber) throws IOException;
    
    /**
     * Upload package photo
     */
    String uploadPackagePhoto(MultipartFile photo, String trackingNumber) throws IOException;
    
    /**
     * Upload driver profile photo
     */
    String uploadDriverPhoto(MultipartFile photo, Long driverId) throws IOException;
    
    /**
     * Upload vehicle photo
     */
    String uploadVehiclePhoto(MultipartFile photo, Long vehicleId) throws IOException;
    
    /**
     * Delete a file
     */
    boolean deleteFile(String filePath);
    
    /**
     * Get file URL
     */
    String getFileUrl(String filePath);
    
    /**
     * Validate file type
     */
    boolean isValidFileType(MultipartFile file, List<String> allowedTypes);
    
    /**
     * Validate file size
     */
    boolean isValidFileSize(MultipartFile file, long maxSize);
    
    /**
     * Generate unique filename
     */
    String generateUniqueFilename(String originalFilename, String prefix);
}
