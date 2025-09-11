package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.ProofOfDelivery;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProofOfDeliveryService {
    
    /**
     * Create proof of delivery for a shipment
     */
    ProofOfDelivery createProofOfDelivery(Long shipmentId, Long driverId, String deliveryLocation);
    
    /**
     * Update proof of delivery with signature
     */
    ProofOfDelivery addSignature(Long podId, String signatureData);
    
    /**
     * Upload delivery photo
     */
    ProofOfDelivery uploadDeliveryPhoto(Long podId, MultipartFile photo);
    
    /**
     * Upload package condition photo
     */
    ProofOfDelivery uploadPackagePhoto(Long podId, MultipartFile photo);
    
    /**
     * Complete delivery with all required information
     */
    ProofOfDelivery completeDelivery(Long podId, String recipientName, String recipientPhone, 
                                   String recipientIdNumber, String deliveryNotes, String deliveryMethod);
    
    /**
     * Mark delivery as failed
     */
    ProofOfDelivery markDeliveryFailed(Long podId, String failureReason);
    
    /**
     * Get proof of delivery by shipment ID
     */
    ProofOfDelivery getProofOfDeliveryByShipment(Long shipmentId);
    
    /**
     * Get proof of delivery by ID
     */
    ProofOfDelivery getProofOfDeliveryById(Long podId);
    
    /**
     * Get all proof of deliveries for a driver
     */
    List<ProofOfDelivery> getProofOfDeliveriesByDriver(Long driverId);
    
    /**
     * Get proof of deliveries by date range
     */
    List<ProofOfDelivery> getProofOfDeliveriesByDateRange(String startDate, String endDate);
    
    /**
     * Get proof of deliveries by status
     */
    List<ProofOfDelivery> getProofOfDeliveriesByStatus(String status);
    
    /**
     * Get delivery statistics
     */
    Map<String, Object> getDeliveryStatistics();
    
    /**
     * Validate delivery requirements
     */
    Map<String, Object> validateDeliveryRequirements(Long shipmentId);
    
    /**
     * Generate delivery report
     */
    byte[] generateDeliveryReport(Long podId);
    
    /**
     * Send delivery confirmation to customer
     */
    void sendDeliveryConfirmation(Long podId);
    
    /**
     * Get delivery photo URL
     */
    String getDeliveryPhotoUrl(Long podId);
    
    /**
     * Get package photo URL
     */
    String getPackagePhotoUrl(Long podId);
    
    /**
     * Delete proof of delivery
     */
    void deleteProofOfDelivery(Long podId);
    
    /**
     * Update delivery notes
     */
    ProofOfDelivery updateDeliveryNotes(Long podId, String notes);
    
    /**
     * Get delivery history for a shipment
     */
    List<ProofOfDelivery> getDeliveryHistory(Long shipmentId);
}
