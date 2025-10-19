package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.DriverResponse;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.service.DriverService;
 
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    /**
     * Get all drivers with vehicle information
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        List<DriverResponse> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get driver by ID with vehicle information
     */
    @GetMapping("/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER') or #driverId == authentication.principal.id")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long driverId) {
        DriverResponse driver = driverService.getDriverById(driverId);
        return ResponseEntity.ok(driver);
    }

    /**
     * Get drivers by role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DriverResponse>> getDriversByRole(@PathVariable UserRole role) {
        List<DriverResponse> drivers = driverService.getDriversByRole(role);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by vehicle make and model
     */
    @GetMapping("/vehicle/make-model")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversByVehicleMakeAndModel(
            @RequestParam String make,
            @RequestParam String model) {
        List<DriverResponse> drivers = driverService.getDriversByVehicleMakeAndModel(make, model);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by vehicle type
     */
    @GetMapping("/vehicle/type/{vehicleType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversByVehicleType(@PathVariable String vehicleType) {
        List<DriverResponse> drivers = driverService.getDriversByVehicleType(vehicleType);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by location
     */
    @GetMapping("/location")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversByLocation(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state) {
        List<DriverResponse> drivers = driverService.getDriversByLocation(city, state);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get online drivers
     */
    @GetMapping("/online")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getOnlineDrivers() {
        List<DriverResponse> drivers = driverService.getOnlineDrivers();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get offline drivers
     */
    @GetMapping("/offline")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getOfflineDrivers() {
        List<DriverResponse> drivers = driverService.getOfflineDrivers();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers with active packages
     */
    @GetMapping("/with-active-packages")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversWithActivePackages() {
        List<DriverResponse> drivers = driverService.getDriversWithActivePackages();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers without assigned vehicles
     */
    @GetMapping("/without-vehicles")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DriverResponse>> getDriversWithoutVehicles() {
        List<DriverResponse> drivers = driverService.getDriversWithoutVehicles();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Search drivers by name, email, or vehicle information
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> searchDrivers(@RequestParam String searchTerm) {
        List<DriverResponse> drivers = driverService.searchDrivers(searchTerm);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get driver statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, Object>> getDriverStatistics() {
        Map<String, Object> statistics = driverService.getDriverStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get driver performance metrics
     */
    @GetMapping("/{driverId}/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF') or #driverId == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> getDriverPerformanceMetrics(@PathVariable Long driverId) {
        Map<String, Object> metrics = driverService.getDriverPerformanceMetrics(driverId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get driver by vehicle registration number
     */
    @GetMapping("/vehicle/registration/{registrationNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<DriverResponse> getDriverByVehicleRegistration(@PathVariable String registrationNumber) {
        DriverResponse driver = driverService.getDriverByVehicleRegistration(registrationNumber);
        if (driver != null) {
            return ResponseEntity.ok(driver);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get drivers with vehicles due for maintenance
     */
    @GetMapping("/maintenance-due")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DriverResponse>> getDriversWithVehiclesDueForMaintenance() {
        List<DriverResponse> drivers = driverService.getDriversWithVehiclesDueForMaintenance();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by vehicle capacity range
     */
    @GetMapping("/vehicle/capacity")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversByVehicleCapacityRange(
            @RequestParam Double minCapacity,
            @RequestParam Double maxCapacity) {
        List<DriverResponse> drivers = driverService.getDriversByVehicleCapacityRange(minCapacity, maxCapacity);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by vehicle year range
     */
    @GetMapping("/vehicle/year")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversByVehicleYearRange(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        List<DriverResponse> drivers = driverService.getDriversByVehicleYearRange(startYear, endYear);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by vehicle fuel type
     */
    @GetMapping("/vehicle/fuel-type/{fuelType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversByVehicleFuelType(@PathVariable String fuelType) {
        List<DriverResponse> drivers = driverService.getDriversByVehicleFuelType(fuelType);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by vehicle color
     */
    @GetMapping("/vehicle/color/{color}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TRACKING_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getDriversByVehicleColor(@PathVariable String color) {
        List<DriverResponse> drivers = driverService.getDriversByVehicleColor(color);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get driver count by vehicle make
     */
    @GetMapping("/statistics/vehicle-make")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, Long>> getDriverCountByVehicleMake() {
        Map<String, Long> count = driverService.getDriverCountByVehicleMake();
        return ResponseEntity.ok(count);
    }

    /**
     * Get driver count by vehicle type
     */
    @GetMapping("/statistics/vehicle-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, Long>> getDriverCountByVehicleType() {
        Map<String, Long> count = driverService.getDriverCountByVehicleType();
        return ResponseEntity.ok(count);
    }

    /**
     * Get driver count by location
     */
    @GetMapping("/statistics/location")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, Long>> getDriverCountByLocation() {
        Map<String, Long> count = driverService.getDriverCountByLocation();
        return ResponseEntity.ok(count);
    }

    /**
     * Get driver count by status
     */
    @GetMapping("/statistics/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, Long>> getDriverCountByStatus() {
        Map<String, Long> count = driverService.getDriverCountByStatus();
        return ResponseEntity.ok(count);
    }

    /**
     * Get current driver's information (for driver dashboard)
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> getCurrentDriver() {
        // This would typically get the current authenticated user's ID
        // For now, we'll return a placeholder - in a real implementation,
        // you'd get the current user from the security context
        return ResponseEntity.ok().build();
    }
}
