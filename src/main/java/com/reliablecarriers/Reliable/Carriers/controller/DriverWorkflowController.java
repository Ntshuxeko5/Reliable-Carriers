package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.service.DriverWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Driver Workflow API Controller
 * Handles all driver workflow operations
 */
@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('DRIVER')")
public class DriverWorkflowController {

    @Autowired
    private DriverWorkflowService driverWorkflowService;

    /**
     * Get available packages for driver to accept
     */
    @GetMapping("/packages/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailablePackages(@RequestParam Long driverId) {
        try {
            List<Map<String, Object>> packages = driverWorkflowService.getAvailablePackages(driverId);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get driver's assigned packages
     */
    @GetMapping("/packages/assigned")
    public ResponseEntity<List<Map<String, Object>>> getAssignedPackages(@RequestParam Long driverId) {
        try {
            List<Map<String, Object>> packages = driverWorkflowService.getDriverPackages(driverId);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Accept a package assignment
     */
    @PostMapping("/packages/{bookingId}/accept")
    public ResponseEntity<Map<String, Object>> acceptPackage(
            @PathVariable Long bookingId,
            @RequestParam Long driverId) {
        
        Map<String, Object> result = driverWorkflowService.acceptPackage(bookingId, driverId);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Initiate pickup process with code verification
     */
    @PostMapping("/packages/{bookingId}/pickup/initiate")
    public ResponseEntity<Map<String, Object>> initiatePickup(
            @PathVariable Long bookingId,
            @RequestParam Long driverId,
            @RequestParam String pickupCode) {
        
        Map<String, Object> result = driverWorkflowService.initiatePickup(bookingId, driverId, pickupCode);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Complete pickup with signature and photo
     */
    @PostMapping("/packages/{bookingId}/pickup/complete")
    public ResponseEntity<Map<String, Object>> completePickup(
            @PathVariable Long bookingId,
            @RequestParam Long driverId,
            @RequestParam String signature,
            @RequestParam(required = false) MultipartFile pickupPhoto,
            @RequestParam(required = false) String notes) {
        
        Map<String, Object> result = driverWorkflowService.completePickup(
            bookingId, driverId, signature, pickupPhoto, notes);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Mark package as out for delivery
     */
    @PostMapping("/packages/{bookingId}/out-for-delivery")
    public ResponseEntity<Map<String, Object>> markOutForDelivery(
            @PathVariable Long bookingId,
            @RequestParam Long driverId,
            @RequestParam String estimatedDeliveryTime) {
        
        Map<String, Object> result = driverWorkflowService.markOutForDelivery(
            bookingId, driverId, estimatedDeliveryTime);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Verify delivery code
     */
    @PostMapping("/packages/{bookingId}/delivery/verify")
    public ResponseEntity<Map<String, Object>> verifyDeliveryCode(
            @PathVariable Long bookingId,
            @RequestParam Long driverId,
            @RequestParam String deliveryCode) {
        
        Map<String, Object> result = driverWorkflowService.verifyDeliveryCode(
            bookingId, driverId, deliveryCode);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Complete delivery with signature and photo
     */
    @PostMapping("/packages/{bookingId}/delivery/complete")
    public ResponseEntity<Map<String, Object>> completeDelivery(
            @PathVariable Long bookingId,
            @RequestParam Long driverId,
            @RequestParam String recipientName,
            @RequestParam(required = false) String recipientIdNumber,
            @RequestParam String signature,
            @RequestParam(required = false) MultipartFile deliveryPhoto,
            @RequestParam(required = false) String notes) {
        
        Map<String, Object> result = driverWorkflowService.completeDelivery(
            bookingId, driverId, recipientName, recipientIdNumber, signature, deliveryPhoto, notes);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
