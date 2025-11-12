package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO;
import com.reliablecarriers.Reliable.Carriers.service.UnifiedPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified Package Controller
 * Provides seamless package API for Customers, Drivers, and Admins
 */
@RestController
@RequestMapping("/api/unified/packages")
@CrossOrigin(origins = "*")
public class UnifiedPackageController {

    @Autowired
    private UnifiedPackageService unifiedPackageService;

    /**
     * Get all packages (admin view)
     */
    @GetMapping
    public ResponseEntity<List<UnifiedPackageDTO>> getAllPackages() {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getAllPackages();
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get package by tracking number (all user types)
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<UnifiedPackageDTO> getPackageByTrackingNumber(@PathVariable String trackingNumber) {
        try {
            UnifiedPackageDTO packageDTO = unifiedPackageService.getPackageByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(packageDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get packages by customer email (customer view)
     */
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<UnifiedPackageDTO>> getPackagesByCustomerEmail(@PathVariable String email) {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getPackagesByCustomerEmail(email);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get packages by customer phone (customer view)
     */
    @GetMapping("/customer/phone/{phone}")
    public ResponseEntity<List<UnifiedPackageDTO>> getPackagesByCustomerPhone(@PathVariable String phone) {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getPackagesByCustomerPhone(phone);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get packages by driver ID (driver view)
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<UnifiedPackageDTO>> getPackagesByDriverId(@PathVariable Long driverId) {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getPackagesByDriverId(driverId);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get packages by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UnifiedPackageDTO>> getPackagesByStatus(@PathVariable String status) {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getPackagesByStatus(status);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get pending packages
     */
    @GetMapping("/pending")
    public ResponseEntity<List<UnifiedPackageDTO>> getPendingPackages() {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getPendingPackages();
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get in-transit packages
     */
    @GetMapping("/in-transit")
    public ResponseEntity<List<UnifiedPackageDTO>> getInTransitPackages() {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getInTransitPackages();
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get delivered packages
     */
    @GetMapping("/delivered")
    public ResponseEntity<List<UnifiedPackageDTO>> getDeliveredPackages() {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.getDeliveredPackages();
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Update package status (synchronizes Booking and Shipment)
     */
    @PutMapping("/{trackingNumber}/status")
    public ResponseEntity<UnifiedPackageDTO> updatePackageStatus(
            @PathVariable String trackingNumber,
            @RequestParam String status) {
        try {
            UnifiedPackageDTO packageDTO = unifiedPackageService.updatePackageStatus(trackingNumber, status);
            return ResponseEntity.ok(packageDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assign package to driver (synchronizes Booking and Shipment)
     */
    @PutMapping("/{trackingNumber}/assign")
    public ResponseEntity<UnifiedPackageDTO> assignPackageToDriver(
            @PathVariable String trackingNumber,
            @RequestParam Long driverId) {
        try {
            UnifiedPackageDTO packageDTO = unifiedPackageService.assignPackageToDriver(trackingNumber, driverId);
            return ResponseEntity.ok(packageDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Unassign package from driver (synchronizes Booking and Shipment)
     */
    @PutMapping("/{trackingNumber}/unassign")
    public ResponseEntity<UnifiedPackageDTO> unassignPackageFromDriver(@PathVariable String trackingNumber) {
        try {
            UnifiedPackageDTO packageDTO = unifiedPackageService.unassignPackageFromDriver(trackingNumber);
            return ResponseEntity.ok(packageDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get package statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<UnifiedPackageService.PackageStatistics> getPackageStatistics() {
        try {
            UnifiedPackageService.PackageStatistics stats = unifiedPackageService.getPackageStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search packages
     */
    @GetMapping("/search")
    public ResponseEntity<List<UnifiedPackageDTO>> searchPackages(@RequestParam String q) {
        try {
            List<UnifiedPackageDTO> packages = unifiedPackageService.searchPackages(q);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}

