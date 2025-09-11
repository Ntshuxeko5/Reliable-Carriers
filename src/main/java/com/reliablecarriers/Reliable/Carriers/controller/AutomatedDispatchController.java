package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.AutomatedDispatch;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AutomatedDispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// @RestController
// @RequestMapping("/api/dispatch")
// @CrossOrigin(origins = "*")
public class AutomatedDispatchController {

    @Autowired
    private AutomatedDispatchService automatedDispatchService;

    // Create dispatch rule
    @PostMapping("/rules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<AutomatedDispatch> createDispatchRule(@RequestBody AutomatedDispatch rule) {
        try {
            AutomatedDispatch createdRule = automatedDispatchService.createDispatchRule(rule);
            return ResponseEntity.ok(createdRule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all dispatch rules
    @GetMapping("/rules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<AutomatedDispatch>> getAllDispatchRules() {
        try {
            List<AutomatedDispatch> rules = automatedDispatchService.getAllActiveDispatchRules();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch rule by ID
    @GetMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<AutomatedDispatch> getDispatchRuleById(@PathVariable Long id) {
        try {
            AutomatedDispatch rule = automatedDispatchService.getDispatchRuleById(id);
            return ResponseEntity.ok(rule);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update dispatch rule
    @PutMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<AutomatedDispatch> updateDispatchRule(
            @PathVariable Long id,
            @RequestBody AutomatedDispatch rule) {
        try {
            AutomatedDispatch updatedRule = automatedDispatchService.updateDispatchRule(id, rule);
            return ResponseEntity.ok(updatedRule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete dispatch rule
    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDispatchRule(@PathVariable Long id) {
        try {
            automatedDispatchService.deleteDispatchRule(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Toggle dispatch rule
    @PutMapping("/rules/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<AutomatedDispatch> toggleDispatchRule(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        try {
            AutomatedDispatch rule = automatedDispatchService.toggleDispatchRule(id, isActive);
            return ResponseEntity.ok(rule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch rules by service type
    @GetMapping("/rules/service-type/{serviceType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<AutomatedDispatch>> getDispatchRulesByServiceType(@PathVariable String serviceType) {
        try {
            List<AutomatedDispatch> rules = automatedDispatchService.getDispatchRulesByServiceType(serviceType);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch rules by location
    @GetMapping("/rules/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<AutomatedDispatch>> getDispatchRulesByLocation(
            @RequestParam String city,
            @RequestParam String state) {
        try {
            List<AutomatedDispatch> rules = automatedDispatchService.getDispatchRulesByLocation(city, state);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Auto-assign driver to shipment
    @PostMapping("/assign/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<User> assignDriverToShipment(@PathVariable Long shipmentId) {
        try {
            // This would require getting the shipment first
            // For now, we'll return a placeholder response
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDispatchStatistics() {
        try {
            Map<String, Object> stats = automatedDispatchService.getDispatchStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Validate dispatch rule
    @PostMapping("/rules/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> validateDispatchRule(@RequestBody AutomatedDispatch rule) {
        try {
            Map<String, Object> validation = automatedDispatchService.validateDispatchRule(rule);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Test dispatch rule
    @PostMapping("/rules/{id}/test")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> testDispatchRule(
            @PathVariable Long id,
            @RequestBody Shipment sampleShipment) {
        try {
            Map<String, Object> testResult = automatedDispatchService.testDispatchRule(id, sampleShipment);
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch history
    @GetMapping("/history/{driverId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getDispatchHistory(
            @PathVariable Long driverId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<Map<String, Object>> history = automatedDispatchService.getDispatchHistory(driverId, startDate, endDate);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch efficiency metrics
    @GetMapping("/efficiency")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDispatchEfficiencyMetrics() {
        try {
            Map<String, Object> metrics = automatedDispatchService.getDispatchEfficiencyMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Bulk assign drivers
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> bulkAssignDrivers(@RequestBody List<Long> shipmentIds) {
        try {
            List<Map<String, Object>> results = automatedDispatchService.bulkAssignDrivers(shipmentIds);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get driver availability status
    @GetMapping("/driver/{driverId}/availability")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDriverAvailabilityStatus(@PathVariable Long driverId) {
        try {
            Map<String, Object> status = automatedDispatchService.getDriverAvailabilityStatus(driverId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update driver availability
    @PutMapping("/driver/{driverId}/availability")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Void> updateDriverAvailability(
            @PathVariable Long driverId,
            @RequestParam Boolean isAvailable) {
        try {
            automatedDispatchService.updateDriverAvailability(driverId, isAvailable);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch queue status
    @GetMapping("/queue-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDispatchQueueStatus() {
        try {
            Map<String, Object> queueStatus = automatedDispatchService.getDispatchQueueStatus();
            return ResponseEntity.ok(queueStatus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Prioritize shipments
    @PostMapping("/prioritize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Void> prioritizeShipments(
            @RequestBody List<Long> shipmentIds,
            @RequestParam Integer priority) {
        try {
            automatedDispatchService.prioritizeShipments(shipmentIds, priority);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get dispatch rule performance
    @GetMapping("/rules/{id}/performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDispatchRulePerformance(@PathVariable Long id) {
        try {
            Map<String, Object> performance = automatedDispatchService.getDispatchRulePerformance(id);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get driver performance metrics
    @GetMapping("/driver/{driverId}/performance")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDriverPerformanceMetrics(@PathVariable Long driverId) {
        try {
            Map<String, Object> metrics = automatedDispatchService.getDriverPerformanceMetrics(driverId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Calculate driver workload
    @GetMapping("/driver/{driverId}/workload")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> calculateDriverWorkload(@PathVariable Long driverId) {
        try {
            Map<String, Object> workload = automatedDispatchService.calculateDriverWorkload(driverId);
            return ResponseEntity.ok(workload);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get optimal driver for shipment
    @PostMapping("/optimal-driver")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<User> getOptimalDriver(@RequestBody Shipment shipment) {
        try {
            User optimalDriver = automatedDispatchService.getOptimalDriver(shipment);
            return ResponseEntity.ok(optimalDriver);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get available drivers for shipment
    @PostMapping("/available-drivers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<User>> getAvailableDrivers(@RequestBody Shipment shipment) {
        try {
            List<User> availableDrivers = automatedDispatchService.getAvailableDrivers(shipment);
            return ResponseEntity.ok(availableDrivers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get best matching rule for shipment
    @PostMapping("/best-rule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<AutomatedDispatch> getBestMatchingRule(@RequestBody Shipment shipment) {
        try {
            AutomatedDispatch bestRule = automatedDispatchService.getBestMatchingRule(shipment);
            return ResponseEntity.ok(bestRule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
