package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.MovingServiceRequest;
import com.reliablecarriers.Reliable.Carriers.dto.MovingServiceResponse;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.service.MovingServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moving-services")
public class MovingServiceController {

    @Autowired
    private MovingServiceService movingServiceService;

    // Create new moving service
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<MovingServiceResponse> createMovingService(
            @Valid @RequestBody MovingServiceRequest request,
            @RequestParam Long customerId) {
        try {
            MovingServiceResponse response = movingServiceService.createMovingService(request, customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get moving service by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<MovingServiceResponse> getMovingServiceById(@PathVariable Long id) {
        try {
            MovingServiceResponse response = movingServiceService.getMovingServiceById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get all moving services (Admin/Manager only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getAllMovingServices() {
        List<MovingServiceResponse> services = movingServiceService.getAllMovingServices();
        return ResponseEntity.ok(services);
    }

    // Get moving services by customer
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getMovingServicesByCustomer(@PathVariable Long customerId) {
        try {
            List<MovingServiceResponse> services = movingServiceService.getMovingServicesByCustomer(customerId);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get moving services by driver
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getMovingServicesByDriver(@PathVariable Long driverId) {
        try {
            List<MovingServiceResponse> services = movingServiceService.getMovingServicesByDriver(driverId);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get moving services by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getMovingServicesByStatus(@PathVariable ShipmentStatus status) {
        List<MovingServiceResponse> services = movingServiceService.getMovingServicesByStatus(status);
        return ResponseEntity.ok(services);
    }

    // Get moving services by type
    @GetMapping("/type/{serviceType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getMovingServicesByType(@PathVariable ServiceType serviceType) {
        List<MovingServiceResponse> services = movingServiceService.getMovingServicesByType(serviceType);
        return ResponseEntity.ok(services);
    }

    // Get pending services (no driver assigned)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getPendingServices() {
        List<MovingServiceResponse> services = movingServiceService.getPendingServices();
        return ResponseEntity.ok(services);
    }

    // Assign driver to moving service
    @PostMapping("/{serviceId}/assign-driver")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<MovingServiceResponse> assignDriverToService(
            @PathVariable Long serviceId,
            @RequestParam Long driverId) {
        try {
            MovingServiceResponse response = movingServiceService.assignDriverToService(serviceId, driverId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update service status
    @PutMapping("/{serviceId}/status")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<MovingServiceResponse> updateServiceStatus(
            @PathVariable Long serviceId,
            @RequestParam ShipmentStatus status,
            @RequestParam(required = false) String notes) {
        try {
            MovingServiceResponse response = movingServiceService.updateServiceStatus(serviceId, status, notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update moving service details
    @PutMapping("/{serviceId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<MovingServiceResponse> updateMovingService(
            @PathVariable Long serviceId,
            @Valid @RequestBody MovingServiceRequest request) {
        try {
            MovingServiceResponse response = movingServiceService.updateMovingService(serviceId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete moving service
    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovingService(@PathVariable Long serviceId) {
        try {
            movingServiceService.deleteMovingService(serviceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Calculate price for a service
    @PostMapping("/calculate-price")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> calculatePrice(@RequestParam Double distanceKm) {
        try {
            Map<String, Object> breakdown = movingServiceService.getPricingBreakdown(distanceKm);
            return ResponseEntity.ok(breakdown);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get services by date range
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByDateRange(startDate, endDate);
        return ResponseEntity.ok(services);
    }

    // Get services by location
    @GetMapping("/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByLocation(@RequestParam String location) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByLocation(location);
        return ResponseEntity.ok(services);
    }

    // Get services by distance range
    @GetMapping("/distance-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByDistanceRange(
            @RequestParam Double minDistance,
            @RequestParam Double maxDistance) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByDistanceRange(minDistance, maxDistance);
        return ResponseEntity.ok(services);
    }

    // Get services by price range
    @GetMapping("/price-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(services);
    }

    // Get today's scheduled services
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getTodayScheduledServices() {
        List<MovingServiceResponse> services = movingServiceService.getTodayScheduledServices();
        return ResponseEntity.ok(services);
    }

    // Get services for a specific date
    @GetMapping("/date")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesForDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<MovingServiceResponse> services = movingServiceService.getServicesForDate(date);
        return ResponseEntity.ok(services);
    }

    // Get completed services in date range
    @GetMapping("/completed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getCompletedServicesInRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<MovingServiceResponse> services = movingServiceService.getCompletedServicesInRange(startDate, endDate);
        return ResponseEntity.ok(services);
    }

    // Get services by customer email
    @GetMapping("/customer-email")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByCustomerEmail(@RequestParam String email) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByCustomerEmail(email);
        return ResponseEntity.ok(services);
    }

    // Get services by customer phone
    @GetMapping("/customer-phone")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByCustomerPhone(@RequestParam String phone) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByCustomerPhone(phone);
        return ResponseEntity.ok(services);
    }

    // Get service statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getServiceStatistics() {
        Map<String, Object> stats = movingServiceService.getServiceStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get revenue statistics
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> stats = movingServiceService.getRevenueStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    // Get top services by price
    @GetMapping("/top-price")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getTopServicesByPrice(@RequestParam(defaultValue = "10") int limit) {
        List<MovingServiceResponse> services = movingServiceService.getTopServicesByPrice(limit);
        return ResponseEntity.ok(services);
    }

    // Get top services by distance
    @GetMapping("/top-distance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getTopServicesByDistance(@RequestParam(defaultValue = "10") int limit) {
        List<MovingServiceResponse> services = movingServiceService.getTopServicesByDistance(limit);
        return ResponseEntity.ok(services);
    }

    // Search services
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> searchServices(@RequestParam String query) {
        List<MovingServiceResponse> services = movingServiceService.searchServices(query);
        return ResponseEntity.ok(services);
    }

    // Get services by weight range
    @GetMapping("/weight-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByWeightRange(
            @RequestParam Double minWeight,
            @RequestParam Double maxWeight) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByWeightRange(minWeight, maxWeight);
        return ResponseEntity.ok(services);
    }

    // Get services by item count
    @GetMapping("/item-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<MovingServiceResponse>> getServicesByItemCount(
            @RequestParam Integer minItems,
            @RequestParam Integer maxItems) {
        List<MovingServiceResponse> services = movingServiceService.getServicesByItemCount(minItems, maxItems);
        return ResponseEntity.ok(services);
    }

    // Schedule service
    @PostMapping("/{serviceId}/schedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<MovingServiceResponse> scheduleService(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date scheduledDate) {
        try {
            MovingServiceResponse response = movingServiceService.scheduleService(serviceId, scheduledDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Complete service
    @PostMapping("/{serviceId}/complete")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<MovingServiceResponse> completeService(
            @PathVariable Long serviceId,
            @RequestParam(required = false) String completionNotes) {
        try {
            MovingServiceResponse response = movingServiceService.completeService(serviceId, completionNotes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cancel service
    @PostMapping("/{serviceId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<MovingServiceResponse> cancelService(
            @PathVariable Long serviceId,
            @RequestParam String cancellationReason) {
        try {
            MovingServiceResponse response = movingServiceService.cancelService(serviceId, cancellationReason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
