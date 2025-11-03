package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,application/pdf}")
    private String allowedTypes;

    private static final List<String> IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );


    @Override
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (!isValidFileSize(file, maxFileSize)) {
            throw new IOException("File size exceeds maximum allowed size");
        }

        String filename = generateUniqueFilename(file.getOriginalFilename(), directory);
        Path uploadDir = Paths.get(uploadPath, directory);
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return directory + "/" + filename;
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String directory) throws IOException {
        List<String> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String filePath = uploadFile(file, directory);
                uploadedFiles.add(filePath);
            }
        }
        
        return uploadedFiles;
    }

    @Override
    public String uploadDeliveryPhoto(MultipartFile photo, String trackingNumber) throws IOException {
        if (!isValidFileType(photo, IMAGE_TYPES)) {
            throw new IOException("Invalid file type. Only images are allowed for delivery photos.");
        }
        
        String directory = "delivery-photos/" + trackingNumber;
        return uploadFile(photo, directory);
    }

    @Override
    public String uploadSignature(MultipartFile signature, String trackingNumber) throws IOException {
        if (!isValidFileType(signature, IMAGE_TYPES)) {
            throw new IOException("Invalid file type. Only images are allowed for signatures.");
        }
        
        String directory = "signatures/" + trackingNumber;
        return uploadFile(signature, directory);
    }

    @Override
    public String uploadPackagePhoto(MultipartFile photo, String trackingNumber) throws IOException {
        if (!isValidFileType(photo, IMAGE_TYPES)) {
            throw new IOException("Invalid file type. Only images are allowed for package photos.");
        }
        
        String directory = "package-photos/" + trackingNumber;
        return uploadFile(photo, directory);
    }

    @Override
    public String uploadDriverPhoto(MultipartFile photo, Long driverId) throws IOException {
        if (!isValidFileType(photo, IMAGE_TYPES)) {
            throw new IOException("Invalid file type. Only images are allowed for driver photos.");
        }
        
        String directory = "driver-photos/" + driverId;
        return uploadFile(photo, directory);
    }

    @Override
    public String uploadVehiclePhoto(MultipartFile photo, Long vehicleId) throws IOException {
        if (!isValidFileType(photo, IMAGE_TYPES)) {
            throw new IOException("Invalid file type. Only images are allowed for vehicle photos.");
        }
        
        String directory = "vehicle-photos/" + vehicleId;
        return uploadFile(photo, directory);
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadPath, filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        // In production, this would return a CDN URL or cloud storage URL
        return "/uploads/" + filePath;
    }

    @Override
    public boolean isValidFileType(MultipartFile file, List<String> allowedTypes) {
        String contentType = file.getContentType();
        return contentType != null && allowedTypes.contains(contentType);
    }

    @Override
    public boolean isValidFileSize(MultipartFile file, long maxSize) {
        return file.getSize() <= maxSize;
    }

    @Override
    public String generateUniqueFilename(String originalFilename, String prefix) {
        if (!StringUtils.hasText(originalFilename)) {
            originalFilename = "file";
        }
        
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return prefix + "_" + timestamp + "_" + uuid + extension;
    }
}
