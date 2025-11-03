package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.ProofOfDeliveryRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import com.reliablecarriers.Reliable.Carriers.service.ProofOfDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProofOfDeliveryServiceImpl implements ProofOfDeliveryService {

    private final ProofOfDeliveryRepository podRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.pod.storage.path:/uploads/pod}")
    private String storagePath;

    @Value("${app.pod.signature.enabled:true}")
    private boolean signatureEnabled;

    @Value("${app.pod.photo.enabled:true}")
    private boolean photoEnabled;

    @Value("${app.pod.id-verification.enabled:true}")
    private boolean idVerificationEnabled;

    // constructor injection used; removed unnecessary @Autowired
    public ProofOfDeliveryServiceImpl(ProofOfDeliveryRepository podRepository,
                                    ShipmentRepository shipmentRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService) {
        this.podRepository = podRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ProofOfDelivery createProofOfDelivery(Long shipmentId, Long driverId, String deliveryLocation) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));

        // Check if POD already exists for this shipment
        if (podRepository.findByShipment(shipment).isPresent()) {
            throw new RuntimeException("Proof of delivery already exists for shipment: " + shipmentId);
        }

        ProofOfDelivery pod = new ProofOfDelivery();
        pod.setShipment(shipment);
        pod.setDriver(driver);
        pod.setDeliveryDate(new Date());
        pod.setDeliveryLocation(deliveryLocation);
        pod.setDeliveryStatus("IN_PROGRESS");
        
        // Set requirements based on shipment type
        pod.setSignatureRequired(signatureEnabled);
        pod.setPhotoRequired(photoEnabled);
        pod.setIdVerificationRequired(idVerificationEnabled);

        ProofOfDelivery savedPod = podRepository.save(pod);
        
        // Update shipment status
        shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
        shipmentRepository.save(shipment);
        
        // Send notification to customer
        notificationService.sendShipmentOutForDeliveryNotification(shipment, 
            driver.getFirstName() + " " + driver.getLastName(), "Estimated delivery time");
        
        return savedPod;
    }

    @Override
    public ProofOfDelivery addSignature(Long podId, String signatureData) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        
        if (!pod.getSignatureRequired()) {
            throw new RuntimeException("Signature not required for this delivery");
        }
        
        pod.setRecipientSignature(signatureData);
        pod.setUpdatedAt(new Date());
        
        return podRepository.save(pod);
    }

    @Override
    public ProofOfDelivery uploadDeliveryPhoto(Long podId, MultipartFile photo) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        
        if (!pod.getPhotoRequired()) {
            throw new RuntimeException("Photo not required for this delivery");
        }
        
        try {
            String fileName = generatePhotoFileName(podId, "delivery");
            String filePath = savePhoto(photo, fileName);
            pod.setDeliveryPhotoUrl(filePath);
            pod.setUpdatedAt(new Date());
            
            return podRepository.save(pod);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload delivery photo", e);
        }
    }

    @Override
    public ProofOfDelivery uploadPackagePhoto(Long podId, MultipartFile photo) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        
        try {
            String fileName = generatePhotoFileName(podId, "package");
            String filePath = savePhoto(photo, fileName);
            pod.setPackagePhotoUrl(filePath);
            pod.setUpdatedAt(new Date());
            
            return podRepository.save(pod);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload package photo", e);
        }
    }

    @Override
    public ProofOfDelivery completeDelivery(Long podId, String recipientName, String recipientPhone,
                                          String recipientIdNumber, String deliveryNotes, String deliveryMethod) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        
        // Validate required fields
        if (pod.getSignatureRequired() && pod.getRecipientSignature() == null) {
            throw new RuntimeException("Signature is required but not provided");
        }
        
        if (pod.getPhotoRequired() && pod.getDeliveryPhotoUrl() == null) {
            throw new RuntimeException("Delivery photo is required but not provided");
        }
        
        if (pod.getIdVerificationRequired() && recipientIdNumber == null) {
            throw new RuntimeException("ID verification is required but not provided");
        }
        
        // Update POD details
        pod.setRecipientName(recipientName);
        pod.setRecipientPhone(recipientPhone);
        pod.setRecipientIdNumber(recipientIdNumber);
        pod.setDeliveryNotes(deliveryNotes);
        pod.setDeliveryMethod(deliveryMethod);
        pod.setDeliveryStatus("COMPLETED");
        pod.setUpdatedAt(new Date());
        
        ProofOfDelivery savedPod = podRepository.save(pod);
        
        // Update shipment status
        Shipment shipment = pod.getShipment();
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDeliveryDate(new Date());
        shipmentRepository.save(shipment);
        
        // Send delivery confirmation
        notificationService.sendShipmentDeliveredNotification(shipment, 
            pod.getDriver().getFirstName() + " " + pod.getDriver().getLastName(), 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        return savedPod;
    }

    @Override
    public ProofOfDelivery markDeliveryFailed(Long podId, String failureReason) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        
        pod.setDeliveryStatus("FAILED");
        pod.setFailureReason(failureReason);
        pod.setUpdatedAt(new Date());
        
        ProofOfDelivery savedPod = podRepository.save(pod);
        
        // Update shipment status
        Shipment shipment = pod.getShipment();
        shipment.setStatus(ShipmentStatus.FAILED_DELIVERY);
        shipmentRepository.save(shipment);
        
        // Send failure notification
        notificationService.sendShipmentFailedDeliveryNotification(shipment, failureReason, "Next attempt scheduled");
        
        return savedPod;
    }

    @Override
    public ProofOfDelivery getProofOfDeliveryByShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        return podRepository.findByShipment(shipment)
                .orElseThrow(() -> new RuntimeException("Proof of delivery not found for shipment: " + shipmentId));
    }

    @Override
    public ProofOfDelivery getProofOfDeliveryById(Long podId) {
        return podRepository.findById(podId)
                .orElseThrow(() -> new RuntimeException("Proof of delivery not found: " + podId));
    }

    @Override
    public List<ProofOfDelivery> getProofOfDeliveriesByDriver(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        return podRepository.findByDriverOrderByDeliveryDateDesc(driver);
    }

    @Override
    public List<ProofOfDelivery> getProofOfDeliveriesByDateRange(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            
            return podRepository.findByDeliveryDateBetweenOrderByDeliveryDateDesc(start, end);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Use yyyy-MM-dd", e);
        }
    }

    @Override
    public List<ProofOfDelivery> getProofOfDeliveriesByStatus(String status) {
        return podRepository.findByDeliveryStatusOrderByDeliveryDateDesc(status);
    }

    @Override
    public Map<String, Object> getDeliveryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalDeliveries = podRepository.count();
        long completedDeliveries = podRepository.countByDeliveryStatus("COMPLETED");
        long failedDeliveries = podRepository.countByDeliveryStatus("FAILED");
        long inProgressDeliveries = podRepository.countByDeliveryStatus("IN_PROGRESS");
        
        stats.put("totalDeliveries", totalDeliveries);
        stats.put("completedDeliveries", completedDeliveries);
        stats.put("failedDeliveries", failedDeliveries);
        stats.put("inProgressDeliveries", inProgressDeliveries);
        stats.put("successRate", totalDeliveries > 0 ? (double) completedDeliveries / totalDeliveries * 100 : 0);
        
        // Get today's statistics
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date todayEnd = cal.getTime();
        
        long todayDeliveries = podRepository.countByDeliveryDateBetween(todayStart, todayEnd);
        long todayCompleted = podRepository.countByDeliveryDateBetweenAndDeliveryStatus(todayStart, todayEnd, "COMPLETED");
        
        stats.put("todayDeliveries", todayDeliveries);
        stats.put("todayCompleted", todayCompleted);
        
        return stats;
    }

    @Override
    public Map<String, Object> validateDeliveryRequirements(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        Map<String, Object> validation = new HashMap<>();
        validation.put("shipmentId", shipmentId);
        validation.put("signatureRequired", signatureEnabled);
        validation.put("photoRequired", photoEnabled);
        validation.put("idVerificationRequired", idVerificationEnabled);
        validation.put("isValid", true);
        
        // Check if POD exists
        Optional<ProofOfDelivery> existingPod = podRepository.findByShipment(shipment);
        validation.put("podExists", existingPod.isPresent());
        
        if (existingPod.isPresent()) {
            ProofOfDelivery pod = existingPod.get();
            validation.put("currentStatus", pod.getDeliveryStatus());
            validation.put("hasSignature", pod.getRecipientSignature() != null);
            validation.put("hasDeliveryPhoto", pod.getDeliveryPhotoUrl() != null);
            validation.put("hasPackagePhoto", pod.getPackagePhotoUrl() != null);
        }
        
        return validation;
    }

    @Override
    public byte[] generateDeliveryReport(Long podId) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        
        // Generate PDF report
        StringBuilder report = new StringBuilder();
        report.append("DELIVERY REPORT\n");
        report.append("===============\n\n");
        report.append("Shipment ID: ").append(pod.getShipment().getId()).append("\n");
        report.append("Driver: ").append(pod.getDriver().getFirstName()).append(" ").append(pod.getDriver().getLastName()).append("\n");
        report.append("Delivery Date: ").append(pod.getDeliveryDate()).append("\n");
        report.append("Delivery Location: ").append(pod.getDeliveryLocation()).append("\n");
        report.append("Recipient: ").append(pod.getRecipientName()).append("\n");
        report.append("Status: ").append(pod.getDeliveryStatus()).append("\n");
        
        if (pod.getDeliveryNotes() != null) {
            report.append("Notes: ").append(pod.getDeliveryNotes()).append("\n");
        }
        
        return report.toString().getBytes();
    }

    @Override
    public void sendDeliveryConfirmation(Long podId) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        notificationService.sendShipmentDeliveredNotification(pod.getShipment(), 
            pod.getDriver().getFirstName() + " " + pod.getDriver().getLastName(), 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pod.getDeliveryDate()));
    }

    @Override
    public String getDeliveryPhotoUrl(Long podId) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        return pod.getDeliveryPhotoUrl();
    }

    @Override
    public String getPackagePhotoUrl(Long podId) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        return pod.getPackagePhotoUrl();
    }

    @Override
    public void deleteProofOfDelivery(Long podId) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        podRepository.delete(pod);
    }

    @Override
    public ProofOfDelivery updateDeliveryNotes(Long podId, String notes) {
        ProofOfDelivery pod = getProofOfDeliveryById(podId);
        pod.setDeliveryNotes(notes);
        pod.setUpdatedAt(new Date());
        return podRepository.save(pod);
    }

    @Override
    public List<ProofOfDelivery> getDeliveryHistory(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        return podRepository.findByShipmentOrderByDeliveryDateDesc(shipment);
    }

    // Helper methods
    private String generatePhotoFileName(Long podId, String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return String.format("pod_%d_%s_%s.jpg", podId, type, timestamp);
    }

    private String savePhoto(MultipartFile photo, String fileName) throws IOException {
        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        
        // Save file
        Path filePath = storageDir.resolve(fileName);
        Files.copy(photo.getInputStream(), filePath);
        
        return filePath.toString();
    }
}
