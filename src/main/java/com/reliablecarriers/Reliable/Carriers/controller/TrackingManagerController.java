package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.service.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tracking-manager")
@PreAuthorize("hasRole('TRACKING_MANAGER') or hasRole('ADMIN')")
public class TrackingManagerController {

    private final ShipmentService shipmentService;
    private final ShipmentTrackingService shipmentTrackingService;
    private final UserService userService;
    private final VehicleRepository vehicleRepository;
    private final TrackingService trackingService;

    @Autowired
    public TrackingManagerController(ShipmentService shipmentService,
                                   ShipmentTrackingService shipmentTrackingService,
                                   UserService userService,
                                   VehicleRepository vehicleRepository,
                                   TrackingService trackingService) {
        this.shipmentService = shipmentService;
        this.shipmentTrackingService = shipmentTrackingService;
        this.userService = userService;
        this.vehicleRepository = vehicleRepository;
        this.trackingService = trackingService;
    }

    /**
     * Main tracking manager dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tracking Manager Dashboard");
        model.addAttribute("activePage", "tracking-manager-dashboard");
        return "tracking-manager/dashboard";
    }

    /**
     * Package management page
     */
    @GetMapping("/packages")
    public String packages(Model model) {
        model.addAttribute("pageTitle", "Package Management");
        model.addAttribute("activePage", "package-management");
        return "tracking-manager/packages";
    }

    /**
     * Driver management page
     */
    @GetMapping("/drivers")
    public String drivers(Model model) {
        model.addAttribute("pageTitle", "Driver Management");
        model.addAttribute("activePage", "driver-management");
        return "tracking-manager/drivers";
    }

    /**
     * Analytics and reports page
     */
    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("pageTitle", "Analytics & Reports");
        model.addAttribute("activePage", "analytics");
        return "tracking-manager/analytics";
    }

    /**
     * Real-time tracking map page
     */
    @GetMapping("/live-tracking")
    public String liveTracking(Model model) {
        model.addAttribute("pageTitle", "Live Tracking");
        model.addAttribute("activePage", "live-tracking");
        return "tracking-manager/live-tracking";
    }

    // API Endpoints

    /**
     * Get all packages with filtering options
     */
    @GetMapping("/api/packages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPackages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String driverId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Shipment> allPackages = shipmentService.getAllShipments();
            
            // Apply filters
            List<Shipment> filteredPackages = allPackages.stream()
                .filter(packageItem -> {
                    if (status != null && !status.isEmpty()) {
                        return packageItem.getStatus().toString().equalsIgnoreCase(status);
                    }
                    return true;
                })
                .filter(packageItem -> {
                    if (driverId != null && !driverId.isEmpty()) {
                        return packageItem.getAssignedDriver() != null && 
                               packageItem.getAssignedDriver().getId().toString().equals(driverId);
                    }
                    return true;
                })
                .filter(packageItem -> {
                    if (search != null && !search.isEmpty()) {
                        return packageItem.getTrackingNumber().toLowerCase().contains(search.toLowerCase()) ||
                               packageItem.getRecipientName().toLowerCase().contains(search.toLowerCase()) ||
                               packageItem.getSender().getEmail().toLowerCase().contains(search.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());

            // Pagination
            int start = page * size;
            List<Shipment> pagedPackages = filteredPackages.subList(start, Math.min(start + size, filteredPackages.size()));

            Map<String, Object> response = new HashMap<>();
            response.put("packages", pagedPackages);
            response.put("totalCount", filteredPackages.size());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) filteredPackages.size() / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get package statistics
     */
    @GetMapping("/api/package-statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPackageStatistics() {
        try {
            List<Shipment> allPackages = shipmentService.getAllShipments();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPackages", allPackages.size());
            stats.put("pendingPackages", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.PENDING).count());
            stats.put("inTransitPackages", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.IN_TRANSIT).count());
            stats.put("outForDeliveryPackages", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY).count());
            stats.put("deliveredPackages", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.DELIVERED).count());
            stats.put("failedDeliveryPackages", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.FAILED_DELIVERY).count());
            stats.put("cancelledPackages", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.CANCELLED).count());
            
            // Calculate total weight and value
            double totalWeight = allPackages.stream()
                .mapToDouble(p -> p.getWeight() != null ? p.getWeight() : 0.0)
                .sum();
            BigDecimal totalValue = allPackages.stream()
                .map(Shipment::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            stats.put("totalWeight", totalWeight);
            stats.put("totalValue", totalValue);
            
            // Packages by service type
            Map<String, Long> serviceTypeStats = allPackages.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getServiceType().toString(),
                    Collectors.counting()
                ));
            stats.put("serviceTypeStats", serviceTypeStats);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update package status
     */
    @PutMapping("/api/packages/{packageId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePackageStatus(
            @PathVariable Long packageId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String location = request.get("location");
            String notes = request.get("notes");
            
            ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
            Shipment updatedShipment = shipmentService.updateShipmentStatus(packageId, shipmentStatus, location, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Package status updated successfully");
            response.put("package", updatedShipment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assign driver to package
     */
    @PutMapping("/api/packages/{packageId}/assign-driver")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> assignDriverToPackage(
            @PathVariable Long packageId,
            @RequestBody Map<String, Long> request) {
        try {
            Long driverId = request.get("driverId");
            Shipment updatedShipment = shipmentService.assignDriverToShipment(packageId, driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Driver assigned successfully");
            response.put("package", updatedShipment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all drivers
     */
    @GetMapping("/api/drivers")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDrivers() {
        try {
            List<User> drivers = userService.getUsersByRole(UserRole.DRIVER);
            List<Map<String, Object>> driverData = drivers.stream().map(driver -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", driver.getId());
                data.put("name", driver.getFirstName() + " " + driver.getLastName());
                data.put("email", driver.getEmail());
                data.put("phone", driver.getPhone());
                data.put("isOnline", trackingService.isDriverOnline(driver.getId()));
                
                // Get assigned packages count
                List<Shipment> assignedPackages = shipmentService.getShipmentsByDriver(driver);
                data.put("assignedPackages", assignedPackages.size());
                data.put("pendingPackages", assignedPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.PENDING).count());
                data.put("inTransitPackages", assignedPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.IN_TRANSIT).count());
                
                // Get vehicle information
                List<Vehicle> vehicles = vehicleRepository.findByAssignedDriver(driver);
                if (!vehicles.isEmpty()) {
                    Vehicle vehicle = vehicles.get(0);
                    data.put("vehicle", Map.of(
                        "make", vehicle.getMake(),
                        "model", vehicle.getModel(),
                        "plate", vehicle.getRegistrationNumber()
                    ));
                }
                
                return data;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(driverData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get driver statistics
     */
    @GetMapping("/api/driver-statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDriverStatistics() {
        try {
            List<User> drivers = userService.getUsersByRole(UserRole.DRIVER);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDrivers", drivers.size());
            stats.put("onlineDrivers", drivers.stream().mapToLong(d -> trackingService.isDriverOnline(d.getId()) ? 1 : 0).sum());
            stats.put("offlineDrivers", drivers.stream().mapToLong(d -> trackingService.isDriverOnline(d.getId()) ? 0 : 1).sum());
            
            // Calculate average packages per driver
            double avgPackagesPerDriver = drivers.stream()
                .mapToDouble(d -> shipmentService.getShipmentsByDriver(d).size())
                .average()
                .orElse(0.0);
            stats.put("averagePackagesPerDriver", avgPackagesPerDriver);
            
            // Driver performance metrics
            Map<String, Object> performance = new HashMap<>();
            for (User driver : drivers) {
                List<Shipment> driverPackages = shipmentService.getShipmentsByDriver(driver);
                long deliveredCount = driverPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.DELIVERED).count();
                long totalCount = driverPackages.size();
                double deliveryRate = totalCount > 0 ? (double) deliveredCount / totalCount * 100 : 0;
                
                performance.put(driver.getId().toString(), Map.of(
                    "name", driver.getFirstName() + " " + driver.getLastName(),
                    "totalPackages", totalCount,
                    "deliveredPackages", deliveredCount,
                    "deliveryRate", Math.round(deliveryRate * 100.0) / 100.0
                ));
            }
            stats.put("driverPerformance", performance);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get live tracking data
     */
    @GetMapping("/api/live-tracking")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLiveTrackingData() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("activeDrivers", trackingService.getAllActiveDriverLocations());
            data.put("statistics", trackingService.getTrackingStatistics());
            data.put("allDrivers", trackingService.getAllDriversWithStatus());
            data.put("allVehicles", trackingService.getAllVehiclesWithStatus());
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get analytics data
     */
    @GetMapping("/api/analytics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAnalyticsData(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<Shipment> allPackages = shipmentService.getAllShipments();
            
            Map<String, Object> analytics = new HashMap<>();
            
            // Package trends
            Map<String, Long> dailyTrends = allPackages.stream()
                .collect(Collectors.groupingBy(
                    p -> new java.text.SimpleDateFormat("yyyy-MM-dd").format(p.getCreatedAt()),
                    Collectors.counting()
                ));
            analytics.put("dailyTrends", dailyTrends);
            
            // Service type distribution
            Map<String, Long> serviceDistribution = allPackages.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getServiceType().toString(),
                    Collectors.counting()
                ));
            analytics.put("serviceDistribution", serviceDistribution);
            
            // Status distribution
            Map<String, Long> statusDistribution = allPackages.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getStatus().toString(),
                    Collectors.counting()
                ));
            analytics.put("statusDistribution", statusDistribution);
            
            // Revenue trends
            Map<String, BigDecimal> revenueTrends = allPackages.stream()
                .collect(Collectors.groupingBy(
                    p -> new java.text.SimpleDateFormat("yyyy-MM-dd").format(p.getCreatedAt()),
                    Collectors.reducing(BigDecimal.ZERO, Shipment::getShippingCost, BigDecimal::add)
                ));
            analytics.put("revenueTrends", revenueTrends);
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Bulk update package statuses
     */
    @PutMapping("/api/packages/bulk-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkUpdatePackageStatuses(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> packageIds = (List<Long>) request.get("packageIds");
            String status = (String) request.get("status");
            String location = (String) request.get("location");
            String notes = (String) request.get("notes");
            
            ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
            int updatedCount = 0;
            
            for (Long packageId : packageIds) {
                try {
                    shipmentService.updateShipmentStatus(packageId, shipmentStatus, location, notes);
                    updatedCount++;
                } catch (Exception e) {
                    // Log error but continue with other packages
                    System.err.println("Failed to update package " + packageId + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Updated " + updatedCount + " out of " + packageIds.size() + " packages");
            response.put("updatedCount", updatedCount);
            response.put("totalCount", packageIds.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get package tracking history
     */
    @GetMapping("/api/packages/{packageId}/tracking-history")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getPackageTrackingHistory(@PathVariable Long packageId) {
        try {
            Shipment shipment = shipmentService.getShipmentById(packageId);
            List<com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking> trackingEntries = 
                shipmentTrackingService.getTrackingEntriesByShipment(shipment);
            
            List<Map<String, Object>> history = trackingEntries.stream().map(entry -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", entry.getId());
                data.put("status", entry.getStatus().toString());
                data.put("location", entry.getLocation());
                data.put("notes", entry.getNotes());
                data.put("timestamp", entry.getCreatedAt());
                data.put("formattedTimestamp", new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(entry.getCreatedAt()));
                return data;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
