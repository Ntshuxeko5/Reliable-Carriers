package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageInfo;
import com.reliablecarriers.Reliable.Carriers.dto.PackagePickupRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PackageDeliveryRequest;
import com.reliablecarriers.Reliable.Carriers.dto.WorkboardStats;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.DriverWorkboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/workboard")
@PreAuthorize("hasRole('DRIVER')")
@CrossOrigin(origins = "*")
public class DriverWorkboardController {

    @Autowired
    private DriverWorkboardService workboardService;

    @Autowired
    private AuthService authService;

    /**
     * Get workboard statistics and overview
     */
    @GetMapping("/stats")
    public ResponseEntity<WorkboardStats> getWorkboardStats(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        WorkboardStats stats = workboardService.getWorkboardStats(driverId, currentLat, currentLng);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get available packages for pickup (distance-based recommendations)
     */
    @GetMapping("/available-packages")
    public ResponseEntity<List<DriverPackageInfo>> getAvailablePackages(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng,
            @RequestParam(defaultValue = "10.0") Double maxDistance,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = workboardService.getAvailablePackagesForPickup(
            driverId, currentLat, currentLng, maxDistance, page, size);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get packages currently assigned to driver
     */
    @GetMapping("/assigned-packages")
    public ResponseEntity<List<DriverPackageInfo>> getAssignedPackages(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = workboardService.getAssignedPackages(driverId, currentLat, currentLng);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get optimized route for current packages
     */
    @GetMapping("/optimized-route")
    public ResponseEntity<List<DriverPackageInfo>> getOptimizedRoute(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> route = workboardService.getOptimizedRoute(driverId, currentLat, currentLng);
        return ResponseEntity.ok(route);
    }

    /**
     * Pick up a package with signature and photo
     */
    @PostMapping(value = "/packages/{packageId}/pickup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DriverPackageInfo> pickupPackage(
            @PathVariable Long packageId,
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) MultipartFile signaturePhoto,
            @RequestParam(required = false) MultipartFile packagePhoto,
            @RequestParam(required = false) String pickupNotes,
            @RequestParam(required = false) Double pickupLat,
            @RequestParam(required = false) Double pickupLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        
        PackagePickupRequest request = new PackagePickupRequest();
        request.setDriverId(driverId);
        request.setPackageId(packageId);
        request.setSignature(signature);
        request.setSignaturePhoto(signaturePhoto);
        request.setPackagePhoto(packagePhoto);
        request.setPickupNotes(pickupNotes);
        request.setPickupLat(pickupLat);
        request.setPickupLng(pickupLng);
        
        DriverPackageInfo updatedPackage = workboardService.pickupPackage(request);
        return ResponseEntity.ok(updatedPackage);
    }

    /**
     * Deliver a package with signature and photo
     */
    @PostMapping(value = "/packages/{packageId}/deliver", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DriverPackageInfo> deliverPackage(
            @PathVariable Long packageId,
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) MultipartFile signaturePhoto,
            @RequestParam(required = false) MultipartFile deliveryPhoto,
            @RequestParam(required = false) String recipientName,
            @RequestParam(required = false) String recipientPhone,
            @RequestParam(required = false) String recipientIdNumber,
            @RequestParam(required = false) String deliveryNotes,
            @RequestParam(required = false) Double deliveryLat,
            @RequestParam(required = false) Double deliveryLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        
        PackageDeliveryRequest request = new PackageDeliveryRequest();
        request.setDriverId(driverId);
        request.setPackageId(packageId);
        request.setSignature(signature);
        request.setSignaturePhoto(signaturePhoto);
        request.setDeliveryPhoto(deliveryPhoto);
        request.setRecipientName(recipientName);
        request.setRecipientPhone(recipientPhone);
        request.setRecipientIdNumber(recipientIdNumber);
        request.setDeliveryNotes(deliveryNotes);
        request.setDeliveryLat(deliveryLat);
        request.setDeliveryLng(deliveryLng);
        
        DriverPackageInfo updatedPackage = workboardService.deliverPackage(request);
        return ResponseEntity.ok(updatedPackage);
    }

    /**
     * Request to pick up additional packages
     */
    @PostMapping("/packages/{packageId}/request-pickup")
    public ResponseEntity<Map<String, String>> requestPackagePickup(@PathVariable Long packageId) {
        Long driverId = authService.getCurrentUser().getId();
        workboardService.requestPackagePickup(driverId, packageId);
        return ResponseEntity.ok(Map.of("message", "Pickup request submitted successfully"));
    }

    /**
     * Get nearby packages for potential pickup
     */
    @GetMapping("/nearby-packages")
    public ResponseEntity<List<DriverPackageInfo>> getNearbyPackages(
            @RequestParam Double currentLat,
            @RequestParam Double currentLng,
            @RequestParam(defaultValue = "5.0") Double radius) {
        
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = workboardService.getNearbyPackages(driverId, currentLat, currentLng, radius);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get package details with pickup/delivery history
     */
    @GetMapping("/packages/{packageId}/details")
    public ResponseEntity<Map<String, Object>> getPackageDetails(@PathVariable Long packageId) {
        Long driverId = authService.getCurrentUser().getId();
        Map<String, Object> details = workboardService.getPackageDetails(driverId, packageId);
        return ResponseEntity.ok(details);
    }

    /**
     * Update driver's current location
     */
    @PostMapping("/location")
    public ResponseEntity<Map<String, String>> updateLocation(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) String address) {
        
        Long driverId = authService.getCurrentUser().getId();
        workboardService.updateDriverLocation(driverId, lat, lng, address);
        return ResponseEntity.ok(Map.of("message", "Location updated successfully"));
    }

    /**
     * Get driver's current location
     */
    @GetMapping("/location")
    public ResponseEntity<Map<String, Object>> getCurrentLocation() {
        Long driverId = authService.getCurrentUser().getId();
        Map<String, Object> location = workboardService.getDriverLocation(driverId);
        return ResponseEntity.ok(location);
    }

    /**
     * Get today's work summary
     */
    @GetMapping("/today-summary")
    public ResponseEntity<Map<String, Object>> getTodaySummary() {
        Long driverId = authService.getCurrentUser().getId();
        Map<String, Object> summary = workboardService.getTodaySummary(driverId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Mark package as failed delivery
     */
    @PostMapping("/packages/{packageId}/failed-delivery")
    public ResponseEntity<DriverPackageInfo> markFailedDelivery(
            @PathVariable Long packageId,
            @RequestParam String reason,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) MultipartFile photo) {
        
        Long driverId = authService.getCurrentUser().getId();
        DriverPackageInfo updatedPackage = workboardService.markFailedDelivery(driverId, packageId, reason, notes, photo);
        return ResponseEntity.ok(updatedPackage);
    }

    /**
     * Get delivery history for today
     */
    @GetMapping("/delivery-history")
    public ResponseEntity<List<Map<String, Object>>> getDeliveryHistory(
            @RequestParam(required = false) String date) {
        
        Long driverId = authService.getCurrentUser().getId();
        List<Map<String, Object>> history = workboardService.getDeliveryHistory(driverId, date);
        return ResponseEntity.ok(history);
    }

    /**
     * Get earnings summary
     */
    @GetMapping("/earnings")
    public ResponseEntity<Map<String, Object>> getEarningsSummary(
            @RequestParam(required = false) String period) {
        
        Long driverId = authService.getCurrentUser().getId();
        Map<String, Object> earnings = workboardService.getEarningsSummary(driverId, period);
        return ResponseEntity.ok(earnings);
    }
}
